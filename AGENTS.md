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

### ✅ Tests unitaires : 128/128 passent (100%)
- WireMock corrigé (endpoint `/api/chat`)
- Overlaps nettoyés (7 tests redondants supprimés)
- **ValidatePlantumlSyntaxTaskTest.kt** créé (5 tests)
- **ModelsDataClassTest.kt** créé (11 tests)
- **ReindexPlantumlRagTaskUnitTest.kt** créé (7 tests)
- **ProcessPlantumlPromptsTaskTest.kt** créé (5 tests)
- **LlmServicePrivateMethodsTest.kt** créé (8 tests)
- **ConfigLoader.kt** créé + **ConfigLoaderTest.kt** (5 tests) — Support `${VAR_NAME}` dans YAML
- **DiagramProcessorPrivateMethodsTest.kt** créé (8 tests) — Méthodes privées de DiagramProcessor
- **ConfigMerger.kt** créé + **ConfigMergerTest.kt** (8 tests) — Fusion properties < yaml < CLI
- **Tests 100% couverture** : 2 tests ajoutés (archiveAttemptHistory exception + groq TODO)
- **Renommage `langchain` → `langchain4j`** : 25+ fichiers mis à jour (YAML + Kotlin)
- Voir : `TEST_COVERAGE_ANALYSIS.md`

### 🔄 TOP PRIORITÉ — Tests manquants
**Tous les tests prioritaires sont TERMINÉS ✅**

| # | Fichier | À tester | Tests | Difficulté | Statut |
|---|---------|----------|-------|------------|--------|
| 1 | `DiagramProcessorPrivateMethodsTest.kt` | 5 méthodes privées | 8 | ⭐⭐⭐ Avancé | ✅ **TERMINÉ** |
| 2 | `ValidatePlantumlSyntaxTaskTest.kt` | Méthode `validateSyntax()` | 5 | ⭐⭐ Moyen | ✅ **TERMINÉ** |

**Objectif atteint** : 129 tests, couverture 100% ✅

### 📋 Backlog

#### 🔴 PRIORITÉ MAX — Sécurité & Confort

| # | Tâche | Description | Estimation | Statut |
|---|-------|-------------|------------|--------|
| 1 | **Support variables d'environnement dans YAML** | Permettre syntaxe `${VAR_NAME}` pour clés API | 2h | ✅ **TERMINÉ** |
| 2 | **Configuration LLM via gradle.properties** | Hiérarchie properties < yaml < CLI | 3h | ✅ **TERMINÉ** |
| 3 | **Renommage `langchain` → `langchain4j`** | Correction terminologie (Python vs JVM) | 4h | ✅ **TERMINÉ** |

**✅ Tâche #1 — TERMINÉE** :
- **Fichiers créés** : `ConfigLoader.kt`, `ConfigLoaderTest.kt`
- **Tests** : 5 tests passent (100%)
- **Fichiers modifiés** : `PlantumlManager.kt`
- **Fonctionnalité** : Syntaxe `${VAR_NAME}` résolue depuis `System.getenv()`
- **Fallback** : Si la variable n'existe pas, la syntaxe `${VAR_NAME}` est préservée

**✅ Tâche #2 — TERMINÉE** :
- **Fichiers créés** : `ConfigMerger.kt`, `ConfigMergerTest.kt` (8 tests)
- **Fichiers modifiés** : `PlantumlManager.kt`, `ConfigMerger.kt`
- **Fonctionnalité** : Fusion des 3 sources avec priorité `gradle.properties` < `plantuml-context.yml` < CLI
- **Tests** : 8 tests passent (100%)

**✅ Tâche #3 — TERMINÉE** :
- **Fichiers YAML modifiés** (6) : `plantuml-context.yml`, `plantuml-context.example.yml`, `plantuml-test-config.yml`, `ollama-local-smollm-135.yaml`, `test-llm-param/ollama-local-smollm-135.yml`, `src/test/resources/ollama-local-smollm-135.yml`
- **Fichiers Kotlin modifiés** (20+) : `models.kt`, `ConfigMerger.kt`, `LlmService.kt`, `DiagramProcessor.kt`, `ProcessPlantumlPromptsTask.kt` + tous les tests
- **Propriétés CLI** : `plantuml.langchain.*` → `plantuml.langchain4j.*`
- **Résultat** : 128/128 tests passent (100%)

#### 🟡 Autres tâches

- Optimiser `FilePermissionTest.kt` (~1min35sec)

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

### ✅ Fin de session — PROCÉDURE AUTOMATIQUE OBLIGATOIRE

**🚨 DÉCLENCHEUR** : Quand l'utilisateur dit "nouvelle session", "je quitte", "session terminée", "à plus tard", ou équivalent

**⚠️ OBLIGATION** : L'agent DOIT exécuter CETTE procédure AVANT de répondre à l'utilisateur

**🚫 RÈGLE D'OR — NE PAS PARLER, FAIRE :**
- ❌ **JAMAIS** demander "Veux-tu que je...?" ou "Dois-je...?"
- ❌ **JAMAIS** expliquer ce que tu vas faire avant de le faire
- ❌ **JAMAIS** répondre à l'utilisateur avant d'avoir fini les 5 étapes
- ✅ **FAIRE DIRECTEMENT** les 5 étapes SILENCIEUSEMENT
- ✅ **Répondre UNIQUEMENT** par "✅ Procédure exécutée" + résumé en 3 lignes maximum

**🎯 CIBLE DE CETTE PROCÉDURE :**
> Cette procédure vise à **AUTOMATISER la fin de session** pour que l'agent :
> 1. ✅ Exécute les 5 étapes **SANS DEMANDER CONFIRMATION**
> 2. ✅ Mette à jour les fichiers **SANS EXPLIQUER** ce qu'il fait
> 3. ✅ Réponde **UNIQUEMENT** par "✅ Procédure exécutée" + 3 lignes de résumé
> 
> **Objectif** : Zéro token gaspillé, zéro explication inutile, action directe.

**Procédure en 5 étapes (NON-NÉGOCIABLE) :**

1. **Vérifier les tests** : `./gradlew -p plantuml-plugin test`
   - ✅ Tous les tests doivent passer
   - ❌ Si échec → Corriger AVANT de continuer

2. **Mettre à jour `AGENTS.md`** (section "État actuel") :
   - ✅ Nombre de tests (ex: 70 → 75)
   - ✅ Fichier créé (ex: `ValidatePlantumlSyntaxTaskTest.kt`)
   - ✅ Liste des fichiers restants (supprimer celui terminé)
   - ✅ Priorités mises à jour

3. **Mettre à jour `COMPLETED_TASKS_ARCHIVE.md`** :
   - ✅ Ajouter nouvelle section avec nom de session
   - ✅ Détails des tests créés
   - ✅ Statistiques (avant/après)

4. **Mettre à jour `PROMPT_REPRISE.md`** :
   - ✅ Nombre de tests (ex: 70 → 75)
   - ✅ Nombre de fichiers restants (ex: 6 → 5)
   - ✅ Supprimer fichier terminé de la liste
   - ✅ Recommandation pour prochaine session

5. **Mettre à jour `TEST_COVERAGE_ANALYSIS.md`** (si besoin) :
   - ✅ Marquer classe comme "COUVERTE"
   - ✅ Mettre à jour statistiques

**🚫 INTERDICTIONS :**
- ❌ NE PAS répondre à l'utilisateur avant d'avoir fini les 5 étapes
- ❌ NE PAS dire "Je vais le faire" → LE FAIRE DIRECTEMENT
- ❌ NE PAS demander "Veux-tu que je...?" → C'EST AUTOMATIQUE

**⚠️ Git — INTERDICTION ABSOLUE :**
- ❌ **L'agent N'EST PAS autorisé à exécuter des commandes Git**
- ❌ **Pas de `git add`, `git commit`, `git push`, `git restore`, `git checkout`**
- ❌ **Jamais de modifications Git (créer, supprimer, déplacer des fichiers)**
- ✅ **C'est l'utilisateur qui gère Git manuellement**
- ⚠️ **Si un fichier doit être supprimé → Demander à l'utilisateur, ne JAMAIS le faire soi-même**

**⚠️ Périmètre d'action — Tests unitaires uniquement :**
- ❌ **NE PAS toucher** à `src/test/scenarios/` (tests Cucumber/BDD)
- ❌ **NE PAS toucher** à `src/test/resources/` (mappings WireMock, features)
- ❌ **NE PAS toucher** à `src/test/features/` (scénarios BDD)
- ❌ **NE PAS créer** de dossiers dans `src/test/` (ex: `kotlin/`, `resources/`)
- ✅ **Seul dossier autorisé** : `src/test/kotlin/plantuml/` (tests unitaires)
- ✅ **Seule action autorisée** : Créer/modifier/supprimer des fichiers `.kt` dans `src/test/kotlin/plantuml/`

**⚠️ Rappel important — Nature de l'agent :**
- 🤖 **Je suis un assistant IA, pas un développeur humain**
- 🤖 **Je ne dois JAMAIS agir avec une autonomie de développeur senior**
- 🤖 **Je dois TOUJOURS demander confirmation avant de :**
  - Supprimer un fichier ou dossier
  - Déplacer un fichier ou dossier
  - Toucher à un dossier en dehors de `src/test/kotlin/plantuml/`
  - Exécuter une commande Git
- 🤖 **En cas de doute → DEMANDER À L'UTILISATEUR, ne pas deviner**

**⚠️ Règle de sécurité maximale :**
> **Quand l'utilisateur dit "restaure", "annule", "répare" → DEMANDER confirmation avant d'agir**
> **Ne JAMAIS supposer qu'on peut utiliser Git pour restaurer des fichiers**
> **Toujours vérifier l'état original avec `git ls-tree` ou demander à l'utilisateur**

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
