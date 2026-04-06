package plantuml.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests unitaires de RagIndexer.
 *
 * Pas de GradleRunner, pas de réseau — chaque test démarre en quelques ms.
 * On teste ici toute la logique qui était auparavant enfouie dans la tâche Gradle
 * et visible seulement via l'output texte de GradleRunner.
 */
class RagIndexerTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var ragDir: File

    @BeforeEach
    fun setup() {
        ragDir = File(tempDir, "generated/rag")
    }

    @Test
    fun `should create directory and report it when rag dir does not exist`() {
        val result = RagIndexer(ragDir).index()

        assertTrue(ragDir.exists())
        assertEquals(0, result.diagramsFound)
        assertTrue(result.messages.any { it.contains("Created RAG directory") })
    }

    @Test
    fun `should report no diagrams when directory is empty`() {
        ragDir.mkdirs()

        val result = RagIndexer(ragDir).index()

        assertEquals(0, result.diagramsFound)
        assertTrue(result.messages.any { it.contains("No PlantUML diagrams or training data found") })
    }

    @Test
    fun `should count diagrams and histories correctly`() {
        ragDir.mkdirs()
        File(ragDir, "a.puml").writeText("@startuml\nclass A\n@enduml")
        File(ragDir, "b.puml").writeText("@startuml\nclass B\n@enduml")
        File(ragDir, "history.json").writeText("{}")

        val result = RagIndexer(ragDir).index()

        assertEquals(2, result.diagramsFound)
        assertEquals(1, result.historiesFound)
        assertTrue(
            result.messages.any {
                it.contains("Found 2 PlantUML diagrams and 1 training histories")
            },
        )
    }

    @Test
    fun `should scan subdirectories recursively`() {
        ragDir.mkdirs()
        File(ragDir, "root.puml").writeText("@startuml\nclass Root\n@enduml")
        val sub = File(ragDir, "sub").also { it.mkdirs() }
        File(sub, "deep.puml").writeText("@startuml\nclass Deep\n@enduml")

        val result = RagIndexer(ragDir).index()

        assertEquals(3, result.diagramsFound) // root + deep + sub check
        // on accepte 2 si le sub lui-même n'est pas compté comme puml
        assertTrue(result.diagramsFound >= 2)
    }

    @Test
    fun `should handle empty puml files without crashing`() {
        ragDir.mkdirs()
        File(ragDir, "empty.puml").writeText("")
        File(ragDir, "valid.puml").writeText("@startuml\nclass Valid\n@enduml")

        val result = RagIndexer(ragDir).index()

        // Les deux fichiers sont trouvés, l'indexation ne crashe pas sur le vide
        assertEquals(2, result.diagramsFound)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 50])
    fun `should handle various diagram counts`(count: Int) {
        ragDir.mkdirs()
        repeat(count) { i ->
            File(ragDir, "diagram$i.puml").writeText("@startuml\nclass C$i\n@enduml")
        }

        val result = RagIndexer(ragDir).index()

        assertEquals(count, result.diagramsFound)
    }

    @Test
    fun `should report error when ragDir is a file not a directory`() {
        // Crée un fichier à la place du répertoire
        ragDir.parentFile.mkdirs()
        ragDir.createNewFile()

        val result = RagIndexer(ragDir).index()

        assertTrue(result.messages.any { it.contains("not a directory") })
        assertEquals(0, result.diagramsFound)
    }
}
