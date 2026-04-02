# AGENTS.md — PlantUML Gradle Plugin

## Projet

Plugin Gradle pour le traitement de diagrammes PlantUML via IA (LangChain4j).
Suit les patterns architecturaux des projets `slider-gradle`(les sources sont dans le dossier plantuml-plugin/slider-plugin/slider/src) et `plantuml-plugin/readme-gradle`(les sources sont dans le dossier readme-plugin/readme/src).

- **Plugin ID** : `com.cheroliv.plantuml`
- **Package root** : `plantuml` (pas `com.cheroliv.*`)
- **Stack** : Kotlin, Gradle, LangChain4j, Cucumber BDD

---

## État actuel

### ✅ Fait — Ne plus retravailler
<!-- L'agent met à jour cette section en fin de session -->
- Architecture du plugin définie (PlantumlPlugin, Extension, Config, Manager, tasks/, service/)
- Configuration YAML via `plantuml-context.yml`
- GitHub Actions workflow pour le processing automatique
- Intégration LangChain4j (providers : Ollama, Gemini, Mistral)
- RAG : indexation et retrieval des diagrammes valides
- Structure des tests unitaires Kotlin corrigée et fonctionnelle

### 🔄 En cours
- **Structure des tests** (session active)
  - Tests BDD Cucumber

### 📋 Backlog — À faire
- [ ] Implémenter `ProcessPlantumlPromptsTask`
- [ ] Implémenter `ValidatePlantumlSyntaxTask`
- [ ] Implémenter `ReindexPlantumlRagTask`
- [ ] Implémenter `PlantumlService` et `DiagramProcessor`
- [ ] Tests d'intégration Gradle plugin
- [ ] Documentation README

---

## Architecture de référence

```
plantuml-plugin/src/main/kotlin/plantuml/
├── PlantumlPlugin.kt
├── PlantumlExtension.kt
├── PlantumlConfig.kt
├── models.kt
├── PlantumlManager.kt
├── tasks/
│   ├── ProcessPlantumlPromptsTask.kt
│   ├── ValidatePlantumlSyntaxTask.kt
│   └── ReindexPlantumlRagTask.kt
└── service/
    ├── PlantumlService.kt
    └── DiagramProcessor.kt
```

---

## Décisions techniques — Ne pas remettre en question

- Pattern de configuration : YAML (`plantuml-context.yml`), pas de DSL Gradle custom
- LangChain4j pour toutes les interactions IA (pas d'appel HTTP direct)
- Boucle LLM : max 5 itérations par prompt
- RAG : stocker uniquement les diagrammes valides
- Tests : JUnit5 + Cucumber BDD (pas de Spock)

---

## Commandes utiles

```bash
./gradlew build -x test   # build rapide
./gradlew test            # tous les tests
./gradlew processPlantumlPrompts
./gradlew validatePlantumlSyntax -Pplantuml.diagram=file.puml
./gradlew reindexPlantumlRag
```

---

## Instruction de mise à jour

À la fin de chaque session, mettre à jour ce fichier :
1. Déplacer ce qui est terminé dans **✅ Fait**
2. Mettre à jour **🔄 En cours**
3. Cocher / ajouter dans **📋 Backlog**

Ne pas modifier les sections "Architecture de référence" et "Décisions techniques" sauf décision explicite.