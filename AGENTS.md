# AGENTS.md — PlantUML Gradle Plugin

> **Note sur la langue de communication** : Cette documentation et nos échanges sont en français, mais le code source, les commentaires et les noms de fonctions sont en anglais conformément aux standards de développement.

## Contexte du projet

Le plugin PlantUML Gradle génère automatiquement des diagrammes PlantUML à partir de descriptions textuelles en utilisant l'IA. Le plugin surveille les fichiers `.prompt`, les transforme en diagrammes PlantUML via des LLM, valide la syntaxe, génère des images et indexe les résultats pour le RAG.

- **Plugin ID** : `com.cheroliv.plantuml`
- **Package root** : `plantuml`
- **Stack** : Kotlin, Gradle, LangChain4j, Cucumber BDD

---

## 🎯 État actuel

### ✅ Fait — Tests unitaires 100% passants
- **66/66 tests passent (100%)** ✨
- Exécution réussie de `./gradlew -p plantuml-plugin -i clean test --rerun-tasks`
- Correction des tests WireMock (PromptOrchestratorTest.kt)
- Analyse complète de couverture créée (TEST_COVERAGE_ANALYSIS.md)

### 🔄 En cours - TOP PRIORITÉ
- **Création des tests unitaires manquants** (voir TEST_COVERAGE_ANALYSIS.md)
  - 7 fichiers de test à créer : PlantumlManager, 3 tâches Gradle, méthodes privées LlmService/DiagramProcessor, data classes
  - Objectif : 40-50 tests additionnels, couverture >80%

### 📋 Backlog
- Optimisation des tests fonctionnels (FilePermissionTest.kt ~1min35sec)
- Amélioration du script de benchmark
- Configuration LLM via gradle.properties

---

## Architecture

```
plantuml-plugin/src/main/kotlin/plantuml/
├── 📄 PlantumlPlugin.kt
│   ├── 🏛️ PlantumlPlugin : Plugin<Project>
│   └── 📦 PlantumlExtension (nested class)
│
├── 📄 models.kt (11 data classes)
│   ├── PlantumlConfig (+ companion object)
│   ├── InputConfig, OutputConfig, LangchainConfig
│   ├── GitConfig, OllamaConfig, ApiKeyConfig, RagConfig
│   ├── PlantumlDiagram, PlantumlCode, ValidationFeedback
│
├── 📄 PlantumlManager.kt
│   └── 🏛️ PlantumlManager (objet Kotlin / singleton)
│       ├── Configuration (nested object)
│       ├── Tasks (nested object)
│       └── Extensions (nested object)
│
├── 📁 service/
│   ├── 📄 PlantumlService.kt
│   │   ├── 🏛️ PlantumlService (classe)
│   │   └── 📦 SyntaxValidationResult (sealed class nested)
│   │
│   ├── 📄 DiagramProcessor.kt
│   │   ├── 📦 AttemptEntry (data class top-level)
│   │   └── 🏛️ DiagramProcessor (classe)
│   │
│   └── 📄 LlmService.kt
│       └── 🏛️ LlmService (classe)
│
└── 📁 tasks/
    ├── 📄 ProcessPlantumlPromptsTask.kt
    │   └── 🏛️ ProcessPlantumlPromptsTask : DefaultTask
    │
    ├── 📄 ValidatePlantumlSyntaxTask.kt
    │   └── 🏛️ ValidatePlantumlSyntaxTask : DefaultTask
    │
    └── 📄 ReindexPlantumlRagTask.kt
        └── 🏛️ ReindexPlantumlRagTask : DefaultTask
```

---

## Décisions techniques

- Configuration : YAML (`plantuml-context.yml`)
- LangChain4j pour toutes les interactions IA
- Boucle LLM : max 5 itérations
- RAG : uniquement diagrammes valides
- Tests : JUnit5 + Cucumber BDD

---

## Commandes utiles

```bash
./gradlew -p plantuml-plugin build -x test   # build rapide
./gradlew -p plantuml-plugin test            # tous les tests
./gradlew -p plantuml-plugin cucumberTest    # tests Cucumber
./gradlew -p plantuml-plugin functionalTest  # tests fonctionnels
./gradlew processPlantumlPrompts
./gradlew validatePlantumlSyntax -Pplantuml.diagram=file.puml
./gradlew reindexPlantumlRag
```

---

## Instruction de mise à jour

À la fin de chaque session :
1. Déplacer ce qui est terminé dans COMPLETED_TASKS_ARCHIVE.md
2. Mettre à jour cette section "État actuel"
3. Ne pas modifier "Architecture" et "Décisions techniques" sauf décision explicite
