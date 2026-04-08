# Analyse de Couverture des Tests Unitaires

**Date**: 2026-04-08  
**État actuel**: 66/66 tests passent (100% de succès)  
**Problème**: Certaines classes principales n'ont PAS de tests unitaires directs

---

## 📊 Résumé de Couverture

| Classe Principale | Tests Unitaires Directs | Tests Fonctionnels | Couverture |
|------------------|------------------------|-------------------|------------|
| PlantumlPlugin | ✅ PlantumlPluginUnitTest | ✅ IntegrationTest | **Bonne** |
| PlantumlManager | ❌ AUCUN | ✅ Indirect | **Faible** |
| ProcessPlantumlPromptsTask | ❌ AUCUN | ✅ FunctionalTest | **Faible** |
| ValidatePlantumlSyntaxTask | ❌ AUCUN | ✅ FunctionalTest | **Faible** |
| ReindexPlantumlRagTask | ❌ AUCUN | ⚠️ Ignorés (lents) | **Très Faible** |
| PlantumlService | ✅ PlantumlServiceTest | ❌ Non nécessaire | **Bonne** |
| LlmService | ✅ LlmServiceTest + ErrorTest | ❌ Non nécessaire | **Moyenne** |
| DiagramProcessor | ✅ DiagramProcessorTest | ❌ Non nécessaire | **Moyenne** |
| models.kt (data classes) | ⚠️ Indirects via Config | ❌ Non nécessaire | **Moyenne** |

---

## ✅ Tests Unitaires Existants

### PlantumlPluginTest.kt
- `should create plantuml extension()` - teste création extension via ProjectBuilder
- `should register tasks()` - teste enregistrement des 3 tâches Gradle
- `should apply plugin successfully()` - teste application du plugin

### PlantumlPluginUnitTest.kt
- `should register plantuml extension when plugin is applied()` - teste avec mocks
- `should register all required tasks when plugin is applied()` - teste avec mocks
- `should create plantuml extension with configurable properties()` - teste PlantumlExtension

### PlantumlConfigTest.kt
- `should load configuration from YAML file()` - teste chargement complet YAML
- `should use default values when config is(missing|empty)()` - teste valeurs par défaut (paramétré)

### PlantumlConfigLoaderTest.kt
- `should load default configuration when no config file exists()` - teste config par défaut
- `should load configuration from valid YAML file()` - teste chargement YAML
- `should handle all LLM provider configurations()` - teste toutes configs LLM
- `should use default values for missing configuration()` - teste valeurs par défaut partielles

### PlantumlConfigFailureTest.kt
- `should handle invalid YAML syntax gracefully()` - teste YAML invalide
- `should handle missing required fields gracefully()` - teste champs manquants
- `should handle incorrect data types gracefully()` - teste types incorrects
- `should handle deeply nested invalid configuration gracefully()` - teste configurations imbriquées

### LlmConfigurationTest.kt
- `should create plugin extension with default configuration()` - teste extension
- `should load Ollama/Gemini/Mistral/OpenAI/Claude/HuggingFace/Groq configuration correctly()` - teste configs
- `should handle mixed provider configurations()` - teste configuration mixte
- `should register plantuml tasks when plugin is applied()` - teste tâches

### LlmServiceTest.kt
- `should create chat model for all supported providers()` - teste providers (paramétré: 6 providers)

### LlmServiceErrorTest.kt
- `should handle various error scenarios gracefully()` - teste erreurs (paramétré: 5 scénarios)
- `should handle malformed configuration gracefully()` - teste configuration malformée

### PlantumlServiceTest.kt
- `should validate plantuml syntax(valid|invalid)()` - teste validation syntaxe (paramétré)
- `should generate image from plantuml code()` - teste génération d'image

### DiagramProcessorTest.kt
- `should process prompt with mock llm response()` - teste traitement prompt
- `should handle syntax validation scenarios()` - teste itérations validation (paramétré)
- `should validate diagram quality()` - teste validation diagramme
- `should save for rag training()` - teste sauvegarde RAG

### PromptOrchestratorTest.kt
**Nested: WithMockDiagramProcessor**
- `should report no prompts when directory is missing/empty()` - teste répertoires
- `should process each prompt file and count successes()` - teste traitement fichiers
- `should count failures when processor returns null()` - teste échecs
- `should skip blank prompt files()` - teste fichiers vides
- `should write puml file to output diagrams directory()` - teste écriture sortie
- `should not call generateImage when validation is disabled()` - teste désactivation validation

**Nested: WithWireMockLlm**
- `should complete processing when ollama responds correctly()` - teste réponse LLM
- `should handle 503 from llm and count as failure()` - teste erreur 503
- `should send model name in request body()` - teste envoi modèle

### RagIndexerTest.kt
- `should create directory and report it when rag dir does not exist()` - teste création répertoire
- `should report no diagrams when directory is empty()` - teste répertoire vide
- `should count diagrams and histories correctly()` - teste comptage
- `should scan subdirectories recursively()` - teste scan récursif
- `should handle empty puml files without crashing()` - teste fichiers vides
- `should handle various diagram counts()` - teste différents nombres (paramétré: 1, 5, 50)
- `should report error when ragDir is a file not a directory()` - teste erreur type chemin

---

## ❌ Classes NON couvertes par tests unitaires

### PlantumlManager.kt

#### Objet: Configuration
- `load(project: Project): PlantumlConfig` - **NON TESTÉE UNITAIREMENT**
  - Gestion de configPath depuis l'extension
  - Fallback vers plantuml-context.yml par défaut
  - Gestion fichier absent ou vide
  - Gestion YAML invalide avec fallback

#### Objet: Tasks
- `registerTasks(project: Project)` - **NON TESTÉE UNITAIREMENT**
  - Enregistrement des 3 tâches Gradle

#### Objet: Extensions
- `configureExtensions(project: Project)` - **NON TESTÉE** (fonction vide)

---

### ProcessPlantumlPromptsTask.kt

- `processPrompts()` - **NON TESTÉE UNITAIREMENT**
  - Lecture de plantuml.prompts.dir depuis properties
  - Résolution du répertoire prompts
  - Initialisation des services (PlantumlService, LlmService, DiagramProcessor)
  - Boucle de traitement des fichiers .prompt

- `processSinglePrompt(...)` - **NON TESTÉE UNITAIREMENT**
  - Lecture du contenu du prompt
  - Appel à diagramProcessor.processPrompt()
  - Validation syntaxe
  - Génération d'image
  - Validation LLM avec scoring
  - Sauvegarde pour RAG training
  - Suppression du fichier prompt traité

- `loadConfiguration()` - **NON TESTÉE UNITAIREMENT**
  - Substitution du modèle LLM depuis plantuml.langchain.model

---

### ValidatePlantumlSyntaxTask.kt

- `validateSyntax()` - **NON TESTÉE UNITAIREMENT**
  - Lecture de plantuml.diagram depuis properties
  - Validation du fichier existant
  - Lecture du contenu PlantUML
  - Appel à PlantumlService.validateSyntax()
  - Logging des résultats

---

### ReindexPlantumlRagTask.kt

- `reindexRag()` - **NON TESTÉE UNITAIREMENT**
  - Chargement configuration
  - Vérification répertoire RAG (existe, est répertoire, permissions)
  - Scan des fichiers .puml et .json (historiques)
  - Initialisation embedding model (AllMiniLmL6V2EmbeddingModel)
  - Indexation avec PGVector ou mode simulation

- `simulateIndexing(...)` - **NON TESTÉE UNITAIREMENT**
  - Logique de simulation d'indexation
  - Splitting de documents
  - Génération d'embeddings

---

### LlmService.kt - Méthodes privées NON TESTÉES

- `createOllamaModel()` - NON TESTÉE
- `createOpenAiModel()` - NON TESTÉE
- `createGeminiModel()` - NON TESTÉE
- `createMistralModel()` - NON TESTÉE
- `createClaudeModel()` - NON TESTÉE
- `createHuggingFaceModel()` - NON TESTÉE
- `getTimeoutInSeconds()` - NON TESTÉE

**Cas manquant:**
- `createChatModel()` avec "groq" - TODO non implémenté

---

### DiagramProcessor.kt - Méthodes privées NON TESTÉES

- `buildHistoryContext(history: List<AttemptEntry>)` - NON TESTÉE
- `archiveAttemptHistory(history: List<AttemptEntry>)` - NON TESTÉE
- `convertHistoryToJson(history: List<AttemptEntry>)` - NON TESTÉE
- `fixCommonPlantUmlIssues(code: String)` - NON TESTÉE
- `generateSimulatedLlmResponse(prompt: String)` - NON TESTÉE

---

### models.kt - Data classes SANS tests directs

- `InputConfig` - Pas de tests d'instanciation directe
- `OutputConfig` - Pas de tests d'instanciation directe
- `LangchainConfig` - Pas de tests d'instanciation directe
- `GitConfig` - Pas de tests d'instanciation directe
- `OllamaConfig` - Pas de tests d'instanciation directe
- `ApiKeyConfig` - Pas de tests d'instanciation directe
- `RagConfig` - Pas de tests d'instanciation directe
- `PlantumlDiagram` - Pas de tests d'instanciation directe
- `PlantumlCode` - Pas de tests d'instanciation directe
- `ValidationFeedback` - Pas de tests d'instanciation directe

---

## 🎯 Recommandations - Tests Unitaires à Créer

### Priorité HAUTE

#### 1. PlantumlManagerTest.kt
```kotlin
class PlantumlManagerTest {
    fun `should load config from extension configPath`()
    fun `should use default config file when extension not set`()
    fun `should return defaults when config file is missing`()
    fun `should return defaults when config file is empty`()
    fun `should return defaults when YAML is invalid`()
    fun `should register all three tasks correctly`()
}
```

#### 2. ProcessPlantumlPromptsTaskTest.kt
```kotlin
class ProcessPlantumlPromptsTaskTest {
    fun `should exit early when prompts directory does not exist`()
    fun `should exit early when no prompt files found`()
    fun `should process all prompt files in directory`()
    fun `should respect plantuml.prompts.dir property override`()
    fun `should override LLM model from command line property`()
}
```

#### 3. ValidatePlantumlSyntaxTaskTest.kt
```kotlin
class ValidatePlantumlSyntaxTaskTest {
    fun `should exit gracefully when no diagram file specified`()
    fun `should throw exception when diagram file does not exist`()
    fun `should validate valid PlantUML file`()
    fun `should report invalid PlantUML syntax`()
    fun `should respect plantuml.diagram property override`()
}
```

#### 4. ReindexPlantumlRagTaskTest.kt (version unitaire)
```kotlin
class ReindexPlantumlRagTaskUnitTest {
    fun `should create RAG directory when not exists`()
    fun `should throw when ragDir is file not directory`()
    fun `should throw when directory is not readable`()
    fun `should scan puml files recursively`()
    fun `should scan history json files`()
    fun `should use database when configured`()
    fun `should fallback to simulation when database unavailable`()
}
```

### Priorité MOYENNE

#### 5. LlmServicePrivateMethodsTest.kt
```kotlin
class LlmServicePrivateMethodsTest {
    fun `createOllamaModel should set correct baseUrl and modelName`()
    fun `createOpenAiModel should set correct API key`()
    fun `createGeminiModel should set correct API key`()
    fun `createMistralModel should set correct API key`()
    fun `createClaudeModel should set correct API key`()
    fun `createHuggingFaceModel should use OpenAI client with custom baseUrl`()
    fun `getTimeoutInSeconds should return 5 in test environment`()
    fun `getTimeoutInSeconds should return 60 in production`()
}
```

#### 6. DiagramProcessorPrivateMethodsTest.kt
```kotlin
class DiagramProcessorPrivateMethodsTest {
    fun `buildHistoryContext should format history entries`()
    fun `buildHistoryContext should return empty string for empty history`()
    fun `archiveAttemptHistory should create directory if not exists`()
    fun `archiveAttemptHistory should not archive single attempts`()
    fun `convertHistoryToJson should escape special characters`()
    fun `fixCommonPlantUmlIssues should add missing startuml tag`()
    fun `fixCommonPlantUmlIssues should add missing enduml tag`()
    fun `generateSimulatedLlmResponse should create valid PlantUML`()
}
```

#### 7. ModelsDataClassTest.kt
```kotlin
class ModelsDataClassTest {
    fun `InputConfig should have correct defaults`()
    fun `OutputConfig should have correct defaults`()
    fun `LangchainConfig should have correct defaults`()
    fun `GitConfig should have correct defaults`()
    fun `OllamaConfig should have correct defaults`()
    fun `ApiKeyConfig should have correct defaults`()
    fun `RagConfig should have correct defaults`()
    fun `PlantumlDiagram should be instantiable`()
    fun `PlantumlCode should be instantiable`()
    fun `ValidationFeedback should be instantiable`()
    fun `PlantumlConfig should compose all configs correctly`()
}
```

---

## 📈 Statistiques Finales

- **Classes principales**: 9 fichiers Kotlin
- **Tests unitaires existants**: 12 fichiers de test (~66 tests)
- **Classes SANS tests unitaires directs**: 4 (PlantumlManager, 3 tâches Gradle)
- **Méthodes privées NON testées**: ~11 méthodes
- **Data classes SANS tests directs**: 10 data classes

**Tests unitaires à créer**: ~7 nouveaux fichiers de test, ~40-50 tests additionnels

---

## 🏗 Recommandation d'Architecture

Pour améliorer la testabilité des tâches Gradle, envisager d'extraire la logique métier:

```kotlin
// Extraire la logique métier dans des classes testables
class PromptProcessor(
    private val config: PlantumlConfig,
    private val diagramProcessor: DiagramProcessor,
    private val plantumlService: PlantumlService
) {
    fun processDirectory(promptsDir: File, outputDir: File): ProcessResult {
        // Logique actuelle de ProcessPlantumlPromptsTask.processPrompts()
    }
}

// La tâche Gradle devient un mince wrapper
class ProcessPlantumlPromptsTask : DefaultTask() {
    @TaskAction
    fun processPrompts() {
        val config = loadConfiguration()
        val processor = PromptProcessor(config, ...)
        processor.processDirectory(...)
    }
}
```

**Avantages:**
- Tests unitaires rapides (<10ms) au lieu de tests fonctionnels lents (minutes)
- Meilleure séparation des responsabilités
- Plus facile à maintenir et à faire évoluer
