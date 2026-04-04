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
&lt;!-- L'agent met à jour cette section en fin de session --&gt;
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
- **Mise à jour de la documentation développeur du plugin dans plantuml-plugin/**
  - Adaptation des README.adoc et README_fr.adoc au contexte PlantUML
  - Documentation de l'architecture interne spécifique au plugin PlantUML
  - Diagrammes PlantUML pour illustrer le pipeline de génération
- **Correction des tests unitaires NetworkTimeoutTest.kt**
  - Adaptation des assertions pour gérer les vrais messages d'erreur réseau
  - Correction du test "should handle DNS resolution failure gracefully"
- **Correction des tests unitaires LlmConfigurationTest.kt**
  - Modification des tests pour utiliser buildAndFail() avec configurations factices
  - Adaptation des assertions pour vérifier le chargement sans crash du plugin
- **Correction des tests unitaires FilePermissionTest.kt**
  - Modification du test "should handle directory permission denied gracefully" pour vérifier les messages d'avertissement au lieu de s'attendre à des échecs complets
  - Simplification de la méthode de test pour rendre les permissions refusées
- **Optimisation des tests pour réduire le temps d'exécution**
  - Configuration des mocks pour remplacer les vrais services LLM dans les tests unitaires
  - Remplacement des gros modèles par smollm:135m dans les tests fonctionnels
  - Création d'un script setupOllama.sh pour pré-charger le modèle SmolLM
  - Réduction du nombre d'itérations dans les tests à 1 pour accélérer l'exécution
  - Mise à jour des workflows GitHub Actions pour utiliser le modèle SmolLM
  - Configuration de la séparation des sorties de test dans un répertoire dédié (test-output)
  - **Optimisation des tests unitaires pour réduire le temps d'exécution**
    - Refactorisation de PlantumlConfigTest.kt avec des tests paramétrés
    - Refactorisation de LargeFileAndPathTest.kt avec des tests paramétrés
    - Refactorisation de DiagramProcessorTest.kt avec des tests paramétrés
    - Refactorisation de PlantumlServiceTest.kt avec des tests paramétrés
    - Refactorisation de LlmServiceTest.kt avec des tests paramétrés
    - Refactorisation de LlmServiceErrorTest.kt avec des tests paramétrés
    - Refactorisation de ReindexPlantumlRagTaskTest.kt avec des tests paramétrés
    - Refactorisation de PerformanceTest.kt avec des tests consolidés
- **Créer un dossier dédié pour les fichiers générés par IA lors des tests, séparé du dossier de build et à côté du dossier prévu pour le RAG**
  - Configuration du plugin pour enregistrer les fichiers de test dans `generated/diagrams` lors de l'exécution en mode test (`-Dplantuml.test.mode=true`)
  - Suppression du champ redondant `aiTestOutput` de la configuration YAML
  - Mise à jour du code dans `DiagramProcessor.kt` et `ReindexPlantumlRagTask.kt` pour utiliser le même répertoire configuré
- **Correction des tests échoués liés aux changements de répertoire**
  - Mise à jour des chemins de répertoires dans PlantumlPluginIntegrationTest.kt pour utiliser les chemins par défaut
  - Correction des chemins dans PerformanceTest.kt pour utiliser generated/diagrams, generated/images, generated/rag
  - Mise à jour des chemins dans LargeFileAndPathTest.kt pour utiliser les chemins par défaut
- **Ajout de la dépendance WireMock pour mocker les appels aux fournisseurs d'LLM**
  - Ajout de la version WireMock 3.9.1 dans libs.versions.toml
  - Ajout de la dépendance WireMock aux configurations testImplementation et functionalTest
  - Configuration pour permettre le mocking par défaut des appels LLM dans les tests

### 📋 Backlog — À faire
&lt;!-- Liste des tests unitaires à corriger par ordre de priorité --&gt;
- Optimiser les performances des tests les plus lents
- Permettre aux tâches Gradle de spécifier le LLM à utiliser pour surcharger la config YAML
- Configuration par gradle.properties comme troisième niveau de priorité (DSL > YAML > gradle.properties)
- Implémenter le mocking avec WireMock pour simuler les appels aux fournisseurs d'LLM par défaut
- PlantumlConfigTest.kt
- LargeFileAndPathTest.kt
- DiagramProcessorTest.kt
- PlantumlServiceTest.kt
- LlmServiceErrorTest.kt
- LlmServiceTest.kt
- ReindexPlantumlRagTaskTest.kt
- PerformanceTest.kt (tests restants)

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

## Optimisation des tests

- Utilisation de modèles légers (smollm:135m) pour les tests fonctionnels nécessitant un LLM réel
- Mise en place de scripts d'initialisation pour pré-charger les modèles Ollama
- Configuration de timeouts stricts (<10s) pour éviter les blocages lors des tests
- Utilisation de mocks complets pour les tests unitaires afin d'éviter les appels réseau
- Limitation du nombre d'itérations dans les tests à 1 pour accélérer l'exécution
- Séparation des sorties de test dans un répertoire dédié (test-output) pour ne pas fausser l'historique d'entraînement

---

## Commandes utiles

```bash
./gradlew -p plantuml-plugin build -x test   # build rapide
./gradlew -p plantuml-plugin  test            # tous les tests
./gradlew -p plantuml-plugin  cucmberTest            # tous les tests cucumber
./gradlew -p plantuml-plugin  functionalTest            # tous les tests d'integration
./gradlew processPlantumlPrompts
./gradlew validatePlantumlSyntax -Pplantuml.diagram=file.puml
./gradlew reindexPlantumlRag
./scripts/setupOllama.sh                         # pré-charger le modèle SmolLM
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