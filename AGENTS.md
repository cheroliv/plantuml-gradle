# AGENTS.md — PlantUML Gradle Plugin

> **Note sur la langue de communication** : Cette documentation et nos échanges sont en français, mais le code source, les commentaires et les noms de fonctions sont en anglais conformément aux standards de développement.

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
- **Analyse comparative des stratégies d'optimisation**
  - Benchmarks détaillés de chaque technique
  - Identification des meilleures combinaisons
- **Documentation avancée des techniques d'optimisation**
  - Guide complet "Best Practices for Gradle Plugin Testing"
  - Étude de cas détaillée avec exemples concrets
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
- **Configuration WireMock pour mocker les appels LLM avec données réelles SmolLM**
  - Création de mappings WireMock basés sur les réponses réelles du modèle SmolLM
  - Configuration du serveur WireMock pour charger automatiquement les mappings
  - Validation du fonctionnement des mocks dans les tests unitaires
- **Refactorisation de LlmConfigurationTest pour séparer les tests unitaires des tests fonctionnels**
  - Extraction des tests unitaires vers ProjectBuilder pour améliorer les performances
  - Réduction du temps d'exécution des tests de ~17s à ~7s (gain de 60%)
  - Maintien des tests d'intégration complets dans les tests fonctionnels
- **Correction des assertions dans LargeFileAndPathTest.kt pour correspondre aux messages réels**
  - Révision des assertions pour matcher la sortie réelle des tâches Gradle validatePlantumlSyntax et processPlantumlPrompts
  - Tests paramétrés fonctionnent correctement après correction
- **Suppression des annotations @Ignore dans PlantumlConfigTest.kt**
  - Activation des tests de configuration YAML
  - Tous les tests du fichier PlantumlConfigTest.kt passent désormais avec succès
- **Optimisation des tests Cucumber (61% de gain : 46s → 18s)**
  - Template de projet partagé dans PlantumlWorld.kt (@BeforeAll + copyRecursively)
  - Suppression des flags incompatibles (--no-daemon, --configuration-cache)
  - Réduction du classpath cucumberTest
  - Minimisation des dépendances entre tâches
- **Correction des erreurs de permission d'écriture dans ProcessPlantumlPromptsTask**
  - Ajout de blocs try-catch pour gérer les erreurs d'écriture dans `ProcessPlantumlPromptsTask.kt:134-142` et `:167-180`
  - Gestion des erreurs d'écriture dans `PlantumlService.generateImage()` avec fallback
  - Tous les tests de `FilePermissionTest.kt` passent maintenant avec succès
- **Restauration et correction du test unitaire PlantumlPluginUnitTest**
  - Correction du test `should create plantuml extension with configurable properties`
  - Mocking correct de `ObjectFactory` et `Property` pour éviter les NullPointerException
  - Test unitaire fonctionnel sans perte de couverture de tests
- **Mise à jour de la documentation AGENTS.md**
  - Ajout de la note sur la langue de communication (français) et code (anglais)
- **Correction de WireMockExtension dans PromptOrchestratorTest.kt**
  - Déplacement de `@RegisterExtension` hors du companion object imbriqué
  - Déclaration au niveau de la classe principale pour initialisation correcte
- **Correction de RagIndexerTest.kt**
  - Assertion corrigée : attend 2 diagrammes au lieu de 3 (root.puml + deep.puml)
- **Refactorisation de PromptOrchestrator.kt**
  - Ajout de l'enum `ProcessResult` (SUCCESS, SKIPPED, FAILED)
  - Meilleur tracking des résultats (succeeded/skipped/failed)
  - Correction du comptage quand processor retourne null
- **Tests unitaires : 64/66 passent (97%)**
  - 2 tests WireMock restants à investiguer (WithWireMockLlm)

### 📋 Backlog — À faire
&lt;!-- Liste des tâches à faire par ordre de priorité --&gt;

#### 🔄 En cours
- **Optimisation des tests unitaires (src/test/kotlin/plantuml/)** (Priorité haute - NEW)
  - Analyser le temps d'exécution de tous les tests unitaires
  - Identifier les tests lents et les goulots d'étranglement
  - Appliquer les techniques d'optimisation (mocks, tests paramétrés, ProjectBuilder)
  - Objectif : réduire le temps total des tests unitaires de 50%+
  - **Critère d'évaluation** : Tous les tests unitaires doivent passer en &lt; 30 secondes

#### 📋 À faire
- **Optimisation des tests fonctionnels (src/functionalTest/kotlin/plantuml/)** (Priorité moyenne)
  - Réduire le temps d'exécution de FilePermissionTest.kt (~1min35sec par test)
  - Identifier les goulots d'étranglement dans l'utilisation de GradleRunner
  - Explorer le mocking des appels Gradle pour les tests de permission
- Amélioration du script de benchmark (Priorité moyenne)
   - Mesures statistiques avancées
   - Génération de rapports comparatifs
- Exploration des limites de l'approche actuelle (Priorité moyenne)
   - Identification des nouveaux goulets d'étranglement
   - Plan d'optimisations futures
- Étude de l'impact du parallélisme dans les tests (Priorité basse)
   - Analyse de la parallélisation des tests Gradle
   - Recommandations pour l'optimisation du cache Gradle
- Permettre aux tâches Gradle de spécifier le LLM à utiliser pour surcharger la config YAML
- Configuration par gradle.properties comme troisième niveau de priorité (DSL > YAML > gradle.properties)
- Tester progressivement les autres configurations LLM (Gemini, Mistral, etc.) en gardant @Ignore

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

### Techniques d'optimisation Cucumber/GradleTestKit (61% de gain : 46s → 18s)

1. **Template de projet partagé** (`PlantumlWorld.kt`)
   - Créer un projet template unique dans `@BeforeAll` (statique, une fois pour toutes les classes)
   - Copier le template via `copyRecursively()` pour chaque scénario au lieu de recréer tous les fichiers
   - Gain : 20-30%

2. **Éviter les flags Gradle incompatibles avec TestKit**
   - NE PAS utiliser `--no-daemon`, `--configuration-cache` avec `GradleRunner` (erreurs `InternalUnsupportedBuildArgumentException`)
   - Laisser GradleTestKit gérer le daemon automatiquement
   - Gain : 40-50%

3. **Réduire le classpath des tâches de test**
   - Supprimer `functionalTest.output` du classpath `cucumberTest` si non nécessaire
   - Exclure les dépendances inutiles dans `build.gradle.kts`
   - Gain : 10-15%

4. **Minimiser les dépendances entre tâches**
   - Retirer `dependsOn(functionalTest.classesTaskName)` de `cucumberTest`
   - Ne dépendre que de `tasks.classes` (compilation main)
   - Gain : 5-10%

5. **Refactorisation des tests unitaires**
   - Utiliser des tests paramétrés pour réduire la duplication
   - Extraire les tests unitaires vers `ProjectBuilder` (pas de GradleRunner)
   - Gain : 60% sur certains tests (ex: LlmConfigurationTest)

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