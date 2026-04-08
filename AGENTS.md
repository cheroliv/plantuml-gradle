# AGENTS.md — PlantUML Gradle Plugin

> **Langue** : Communication en français, code en anglais.

## 🎯 Contexte

Plugin Gradle qui génère des diagrammes PlantUML via IA (LangChain4j) à partir de fichiers `.prompt`.

- **Plugin ID** : `com.cheroliv.plantuml`
- **Package** : `plantuml`
- **Stack** : Kotlin, Gradle, LangChain4j, Cucumber BDD

---

## ⚠️ Points d'attention (Pièges à éviter)

- **PlantumlExtension** est une **nested class** de `PlantumlPlugin.kt` (PAS un fichier séparé)
- **PlantumlConfig** + 10 data classes sont dans **`models.kt`** (pas de fichiers individuels)
- **PlantumlManager** est un **objet Kotlin** (singleton), pas une classe
- **SyntaxValidationResult** est une **sealed class nested** dans `PlantumlService`
- **AttemptEntry** est une **data class top-level** dans `DiagramProcessor.kt`

---

## 🏗 Architecture

```
plantuml-plugin/src/main/kotlin/plantuml/
├── 📄 PlantumlPlugin.kt
│   ├── 🏛️ PlantumlPlugin : Plugin<Project>
│   └── 📦 PlantumlExtension (nested class)
│
├── 📄 models.kt (11 data classes)
│   ├── PlantumlConfig, InputConfig, OutputConfig, LangchainConfig
│   ├── GitConfig, OllamaConfig, ApiKeyConfig, RagConfig
│   └── PlantumlDiagram, PlantumlCode, ValidationFeedback
│
├── 📄 PlantumlManager.kt (objet Kotlin / singleton)
│   ├── Configuration (nested object) — charge config YAML
│   ├── Tasks (nested object) — registre les 3 tâches
│   └── Extensions (nested object)
│
├── 📁 service/
│   ├── 📄 PlantumlService.kt
│   │   └── SyntaxValidationResult (sealed class: Valid | Invalid)
│   ├── 📄 DiagramProcessor.kt
│   │   └── AttemptEntry (data class top-level)
│   └── 📄 LlmService.kt
│
└── 📁 tasks/ (héritent de DefaultTask)
    ├── ProcessPlantumlPromptsTask.kt
    ├── ValidatePlantumlSyntaxTask.kt
    └── ReindexPlantumlRagTask.kt
```

---

## 📊 État actuel

### ✅ Tests unitaires : 70/70 passent (100%)
- WireMock corrigé (endpoint `/api/chat`)
- Overlaps nettoyés (7 tests redondants supprimés)
- Voir : `TEST_COVERAGE_ANALYSIS.md`

### 🔄 TOP PRIORITÉ — Tests manquants
**6 fichiers à créer** (détails dans `TEST_COVERAGE_ANALYSIS.md`) :

| Fichier | À tester | Tests | Difficulté |
|---------|----------|-------|------------|
| `ValidatePlantumlSyntaxTaskTest.kt` | `validateSyntax()` | 5 | ⭐ Facile |
| `ModelsDataClassTest.kt` | 11 data classes | 11 | ⭐ Facile |
| `ProcessPlantumlPromptsTaskTest.kt` | `processPrompts()`, `processSinglePrompt()` | 5 | ⭐⭐ Moyen |
| `ReindexPlantumlRagTaskUnitTest.kt` | `reindexRag()`, `simulateIndexing()` | 7 | ⭐⭐ Moyen |
| `LlmServicePrivateMethodsTest.kt` | 7 méthodes privées | 8 | ⭐⭐⭐ Avancé |
| `DiagramProcessorPrivateMethodsTest.kt` | 5 méthodes privées | 8 | ⭐⭐⭐ Avancé |

**Objectif** : 40-50 tests, couverture >80%

### 📋 Backlog
- Optimiser `FilePermissionTest.kt` (~1min35sec)
- Configuration LLM via `gradle.properties`

---

## 🛠 Décisions techniques

- **Config** : YAML (`plantuml-context.yml`), pas de DSL Gradle
- **IA** : LangChain4j (Ollama, OpenAI, Gemini, Mistral, Claude, HuggingFace, Groq)
- **Boucle LLM** : max 5 itérations
- **RAG** : uniquement diagrammes valides
- **Tests** : JUnit5 + Cucumber BDD (pas de Spock)

---

## 🚀 Optimisation des tests

### Types de tests et localisation

| Type | Dossier | Outil | Règles |
|------|---------|-------|--------|
| **Tests unitaires** | `src/test/kotlin/plantuml/` | **ProjectBuilder** | ✅ Mocks complets, ❌ Jamais GradleRunner |
| **Tests fonctionnels** | `src/functionalTest/kotlin/plantuml/` | **GradleRunner** | ✅ Vrai build Gradle, ✅ smollm:135m |
| **Tests BDD (Cucumber)** | `src/test/features/`, `src/test/scenarios/` | **GradleRunner** | ✅ Template partagé (`PlantumlWorld.kt`) |

### Règles par type de test

#### 1. Tests unitaires (`src/test/kotlin/plantuml/`)
```kotlin
// ✅ OUI : ProjectBuilder + Mocks
val project = ProjectBuilder.builder().build()
val mockService = mock(PlantumlService::class.java)

// ❌ NON : GradleRunner (trop lent, réservé aux tests fonctionnels)
```

**Règles :**
- ✅ **Mocks complets** pour éviter les appels réseau
- ✅ **ProjectBuilder** (pas GradleRunner) → 60% plus rapide
- ✅ **WireMock** pour les tests HTTP (endpoint `/api/chat`)
- ✅ **1 itération max** pour les tests LLM
- ✅ **Objectif** : <10ms par test

#### 2. Tests fonctionnels (`src/functionalTest/kotlin/plantuml/`)
```kotlin
// ✅ OUI : GradleRunner avec vrais plugins
val result = GradleRunner.create()
    .withProjectDir(testDir)
    .withArguments("processPlantumlPrompts")
    .build()

// ✅ OUI : smollm:135m (léger) pour les tests nécessitant un LLM réel
// ✅ OUI : 1 itération max pour accélérer
```

**Règles :**
- ✅ **GradleRunner** obligatoire (teste le vrai build)
- ✅ **smollm:135m** pour les tests LLM (pas de gros modèles)
- ✅ **1 itération max** dans la config YAML
- ✅ **WireMock** si possible (mappings dans `src/test/resources/__files/`)
- ✅ **Objectif** : <30s par test

#### 3. Tests BDD / Cucumber (`src/test/features/`, `src/test/scenarios/`)
```kotlin
// ✅ OUI : Template de projet partagé
@BeforeAll
fun setup() {
    // Copier le template une fois pour toutes les classes
    templateDir.copyRecursively(testDir)
}

// ❌ NON : Flags incompatibles avec GradleTestKit
// Jamais : --no-daemon, --configuration-cache
```

**Règles :**
- ✅ **Template partagé** (`PlantumlWorld.kt`) → 61% de gain (46s → 18s)
- ✅ **Jamais `--no-daemon`** avec GradleRunner → Erreur `InternalUnsupportedBuildArgumentException`
- ✅ **Jamais `--configuration-cache`** avec GradleRunner
- ✅ **Objectif** : <30s par scénario

### ⚠️ Pièges connus

| Erreur | Conséquence | Solution |
|--------|-------------|----------|
| `GradleRunner` dans tests unitaires | Tests lents (minutes) | Utiliser `ProjectBuilder` |
| `--no-daemon` avec TestKit | Crash `InternalUnsupportedBuildArgumentException` | Supprimer le flag |
| Appels HTTP réels | Tests non-déterministes, lents | WireMock avec `/api/chat` |
| Gros modèles LLM (llama2) | Tests >10min | Utiliser `smollm:135m` |
| 5 itérations dans les tests | Tests très lents | Réduire à 1 itération |

### 📁 Sorties de test

- **Tests unitaires** : `test-output/` (à côté de `generated/`)
- **Tests fonctionnels** : `test-output/` (même répertoire)
- **RAG** : `generated/rag/` (pour l'historique d'entraînement)

**Pourquoi** : Ne pas fausser l'historique RAG avec des fichiers de test.

---

## 📦 Commandes

```bash
# Build & tests
./gradlew -p plantuml-plugin build -x test   # build rapide
./gradlew -p plantuml-plugin test            # tous les tests
./gradlew -p plantuml-plugin cucumberTest    # tests Cucumber
./gradlew -p plantuml-plugin functionalTest  # tests fonctionnels

# Tâches plugin
./gradlew processPlantumlPrompts
./gradlew validatePlantumlSyntax -Pplantuml.diagram=file.puml
./gradlew reindexPlantumlRag
```

---

## 📝 Méthodologie de travail

### Principe : Sessions atomiques

**Règle d'or** : **1 session = 1 test créé et validé**

| Métrique | Cible |
|----------|-------|
| **Durée** | 15-30 minutes |
| **Fichiers modifiés** | 1-3 maximum |
| **Tests créés** | 1 fichier |
| **Échanges LLM** | 5-10 messages |

---

### Processus itératif (à suivre pour chaque fichier)

```
1. Créer le fichier de test (ex: PlantumlManagerTest.kt)
2. ./gradlew -p plantuml-plugin test
3. ✅ Si passe → On passe au fichier suivant
4. ❌ Si échec → On corrige AVANT de continuer
```

**Pourquoi** :
- ✅ 1 erreur à la fois (pas de debugging chaos)
- ✅ Base de code toujours stable
- ✅ Feedback immédiat
- ✅ Progression mesurable

**À NE PAS faire** :
- ❌ Créer les 7 fichiers d'un coup
- ❌ Lancer les tests après avoir créé tous les fichiers
- ❌ Accumuler les erreurs de compilation

---

### 🔄 Quand changer de session

**✅ Situations idéales pour une nouvelle session :**
- 1 test créé et validé → Session terminée
- 1 bug fixé → Nouvelle session
- 1 feature complétée → Nouvelle session
- 3 échanges sans progrès → Reset mental → Nouvelle session

**❌ Ne PAS changer de session pour :**
- Un simple test qui échoue (corriger puis continuer)
- Une question rapide de clarification
- Une modification mineure (< 5 lignes)

---

### ✅ Fin de session

**Procédure :**
1. Vérifier : `./gradlew -p plantuml-plugin test`
2. Mettre à jour `AGENTS.md` (section "État actuel")
3. Déplacer tâches terminées vers `COMPLETED_TASKS_ARCHIVE.md`

**⚠️ Git — INTERDICTION :**
- ❌ **L'agent N'EST PAS autorisé à exécuter des commandes Git**
- ❌ **Pas de `git add`, `git commit`, `git push`**
- ✅ **C'est l'utilisateur qui gère Git manuellement**

---

### 🚀 Démarrage de nouvelle session

**Prompt d'ouverture :**
```
Nouvelle session. Contexte chargé :
1. AGENTS.md (architecture, décisions, méthodologie)
2. PROMPT_REPRISE.md (mission en cours)

Mission : [Décrire la tâche unique de cette session]

Contrainte : 1 fichier à la fois, validation après chaque changement.
```

---

## 📝 Mise à jour

En fin de session :
1. Déplacer le terminé vers `COMPLETED_TASKS_ARCHIVE.md`
2. Mettre à jour "État actuel"
3. Ne pas modifier "Architecture" et "Décisions techniques" sauf décision explicite
