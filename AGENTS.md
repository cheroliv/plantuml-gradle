# AGENTS.md — PlantUML Gradle Plugin

## Contexte du projet

Le plugin PlantUML Gradle est un outil qui permet de générer automatiquement des diagrammes PlantUML à partir de descriptions textuelles en utilisant l'intelligence artificielle. Le plugin surveille les fichiers `.prompt`, les transforme en diagrammes PlantUML via des modèles de langage (LLM), valide la syntaxe, génère des images et indexe les résultats pour une utilisation future via RAG (Retrieval Augmented Generation).

Le plugin utilise LangChain4j pour l'intégration avec différents fournisseurs d'IA (Ollama, OpenAI, Gemini, Mistral, Claude, HuggingFace, Groq) et permet une configuration flexible via un fichier YAML.

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
- Intégration LangChain4j (providers : Ollama, Gemini, Mistral, Claude, HuggingFace, Groq)
- RAG : indexation et retrieval des diagrammes valides
- Structure des tests unitaires Kotlin corrigée et fonctionnelle
- Exécution de la tâche `./gradlew -p plantuml-plugin -i check`
- Implémentation de `ProcessPlantumlPromptsTask`
- Implémentation de `ValidatePlantumlSyntaxTask`
- Implémentation de `ReindexPlantumlRagTask`
- Implémentation de `PlantumlService` et `DiagramProcessor`
- Documentation README.md, LICENSE, CONTRIBUTING.md, CODE_OF_CONDUCT.md, CHANGELOG.md
- Tests d'intégration Gradle plugin
- Ajout de tests pour les cas d'échec de configuration YAML
- Ajout de tests pour les erreurs d'API LLM et les fallbacks
- Ajout de tests sur les permissions de fichiers et refus d'accès
- Ajout de tests avec des fichiers volumineux et chemins spéciaux
- Ajout de tests de timeout réseau et scénarios de dégradation
- Implémentation de tests pour la tâche `reindexPlantumlRag`
- Ajout de tests sur les différentes configurations (Gemini, Mistral, Claude, HuggingFace, Groq)
- Ajout de tests de charge et performance

### ✅ Fait — Ne plus retravailler
- **Corrections des erreurs d'import dans les fichiers Kotlin**
  - Analyse des dépendances manquantes
  - Correction des erreurs de compilation
- **Correction du test d'intégration validatePlantumlSyntax**
  - Suppression du lancement d'exception pour les fichiers invalides
  - Mise en conformité avec le comportement attendu dans syntax_validation.feature
- **Création de la documentation développeur du plugin (README_truth.adoc & README_truth_fr.adoc)**
  - Adaptation de la structure du plugin slider au contexte PlantUML
  - Documentation de l'architecture interne et des composants
  - Diagrammes PlantUML pour illustrer les concepts clés

### 📋 Backlog — À faire
<!-- Toutes les tâches sont terminées -->

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

## Outils d'investigation
- Utilisation de `javap` pour explorer les APIs de bibliothèques externes (ex: LangChain4j)