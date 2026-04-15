# Agent Reference — PlantUML Gradle Plugin

> **Objectif** : Référence rapide pour l'agent — **NE PAS charger automatiquement**
> 
> **Usage** : Consulter uniquement sur demande explicite ou besoin contextuel

---

## 📁 Architecture des Fichiers de Mémoire

| Niveau | Fichier | Chargement | Rôle |
|--------|---------|------------|------|
| **N1** | `AGENTS.md` | **Toujours** | Architecture, décisions, méthodologie |
| **N2** | `AGENT_REFERENCE.md` | **Sur besoin** | Référence rapide (ce fichier) |
| **N3** | `PROMPT_REPRISE.md` | **Début session** | Mission session en cours |
| **N4** | `SESSIONS_HISTORY.md` | **Sur besoin** | Historique complet sessions |
| **N5** | `COMPLETED_TASKS_ARCHIVE.md` | **Fin session** | Archive tâches terminées |

---

## 🏗 Architecture du Code (Rappel)

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

### ⚠️ Points d'attention (Pièges à éviter)

- **PlantumlExtension** est une **nested class** de `PlantumlPlugin.kt` (PAS un fichier séparé)
- **PlantumlConfig** + 10 data classes sont dans **`models.kt`** (pas de fichiers individuels)
- **PlantumlManager** est un **objet Kotlin** (singleton), pas une classe
- **SyntaxValidationResult** est une **sealed class nested** dans `PlantumlService`
- **AttemptEntry** est une **data class top-level** dans `DiagramProcessor.kt`

---

## 🧭 Commandes de Référence

```bash
# Build & tests
./gradlew build -x test   # build rapide
./gradlew test            # tous les tests
./gradlew cucumberTest    # tests Cucumber
./gradlew functionalTest  # tests fonctionnels

# Tâches plugin
./gradlew processPlantumlPrompts
./gradlew validatePlantumlSyntax -Pplantuml.diagram=file.puml
./gradlew reindexPlantumlRag

# Debug tests fonctionnels
time ./gradlew functionalTest --tests "plantuml.NomDuTest"
```

---

## 📊 État des Tests (Session 40)

| Type | Dossier | Outil | Statut |
|------|---------|-------|--------|
| **Tests unitaires** | `src/test/kotlin/plantuml/` | ProjectBuilder | ✅ 129/129 (100%) |
| **Tests fonctionnels** | `src/functionalTest/kotlin/plantuml/` | GradleRunner | ✅ 42/42 (100%) |
| **Tests Cucumber** | `src/test/features/`, `src/test/scenarios/` | GradleRunner | ✅ Tous PASS |

**Détail tests fonctionnels** :
- `PlantumlFunctionalSuite` : 15 tests (9 PASS, 6 SKIP)
- `PlantumlPluginIntegrationSuite` : 11 tests (11 PASS)
- `FilePermissionTest` : 4 tests (4 PASS)
- `LargeFileAndPathTest` : 4 tests (4 PASS)
- `NetworkTimeoutTest` : 4 tests (4 PASS)
- `PerformanceTest` : 4 tests (4 PASS)

---

## 🧪 Types de Tests — Règles par Type

### 1. Tests unitaires (`src/test/kotlin/plantuml/`)

```kotlin
// ✅ OUI : ProjectBuilder + Mocks
val project = ProjectBuilder.builder().build()
val mockService = mock(PlantumlService::class.java)

// ❌ NON : GradleRunner (trop lent)
```

**Règles :**
- ✅ Mocks complets pour éviter appels réseau
- ✅ ProjectBuilder (pas GradleRunner) → 60% plus rapide
- ✅ WireMock pour tests HTTP (endpoint `/api/chat`)
- ✅ 1 itération max pour tests LLM
- ✅ Objectif : <10ms par test

### 2. Tests fonctionnels (`src/functionalTest/kotlin/plantuml/`)

```kotlin
// ✅ OUI : GradleRunner avec vrais plugins
val result = GradleRunner.create()
    .withProjectDir(testDir)
    .withArguments("processPlantumlPrompts")
    .build()

// ✅ OUI : smollm:135m pour tests LLM
// ✅ OUI : 1 itération max
```

**Règles :**
- ✅ GradleRunner obligatoire (teste le vrai build)
- ✅ smollm:135m pour tests LLM (pas de gros modèles)
- ✅ 1 itération max dans config YAML
- ✅ WireMock si possible
- ✅ Objectif : <30s par test

### 3. Tests BDD / Cucumber (`src/test/features/`, `src/test/scenarios/`)

```kotlin
// ✅ OUI : Template de projet partagé
@BeforeAll
fun setup() {
    templateDir.copyRecursively(testDir)
}

// ❌ NON : Flags incompatibles avec GradleTestKit
// Jamais : --no-daemon, --configuration-cache
```

**Règles :**
- ✅ Template partagé (`PlantumlWorld.kt`) → 61% de gain
- ✅ Jamais `--no-daemon` avec GradleRunner
- ✅ Jamais `--configuration-cache` avec GradleRunner
- ✅ Objectif : <30s par scénario

---

## ⚠️ Pièges Connus

| Erreur | Conséquence | Solution |
|--------|-------------|----------|
| `GradleRunner` dans tests unitaires | Tests lents (minutes) | Utiliser `ProjectBuilder` |
| `--no-daemon` avec TestKit | Crash `InternalUnsupportedBuildArgumentException` | Supprimer le flag |
| Appels HTTP réels | Tests non-déterministes, lents | WireMock avec `/api/chat` |
| Gros modèles LLM (llama2) | Tests >10min | Utiliser `smollm:135m` |
| 5 itérations dans les tests | Tests très lents | Réduire à 1 itération |

---

## 📐 Méthodologie d'Optimisation

### Règle d'or

L'optimisation se mesure en **secondes gagnées**, pas en lignes supprimées.

### Checklist d'optimisation

1. **Mesurer le temps AVANT**
   ```bash
   time ./gradlew functionalTest --tests "plantuml.NomDuTest"
   ```

2. **Identifier les goulots** :
   - ❌ Appels Gradle multiples (2+)
   - ❌ Modèles LLM lourds
   - ❌ Itérations excessives (maxIterations: 5)
   - ❌ Flags inutiles (--no-daemon)
   - ❌ Setup redondant

3. **Appliquer optimisations** :
   - ✅ 1 appel Gradle par test
   - ✅ smollm:135m
   - ✅ maxIterations: 1
   - ✅ WireMock
   - ✅ Template partagé

4. **Mesurer le temps APRÈS**

5. **Vérifier couverture** :
   - ✅ Même nombre de tests passants
   - ✅ Mêmes assertions vérifiées
   - ✅ Commentaires préservés

### Exemples de gains

| Test | Avant | Après | Gain | Technique |
|------|-------|-------|------|-----------|
| `FilePermissionTest` | 1m59s | 17s | **-85%** | Code simplifié |
| `MegaOptimizedFunctionalTest` | 28s | 14s | **-50%** | 2 appels → 1 |
| `NetworkTimeoutTest` | ~40s | 29s | **-28%** | Code réduit -36% |
| `LlmHandshakeTest` | ~20s | 12s | **-40%** | Code réduit -40% |

---

## 🧩 Providers LLM Supportés

| Provider | Config YAML | CLI Parameter | Status |
|----------|-------------|---------------|--------|
| **Ollama** | `langchain4j.ollama` | `-Pplantuml.langchain4j.model=ollama` | ✅ Local, gratuit |
| **Gemini** | `langchain4j.gemini` | `-Pplantuml.langchain4j.model=gemini` | ⚠️ API key requis |
| **Mistral** | `langchain4j.mistral` | `-Pplantuml.langchain4j.model=mistral` | ⚠️ API key requis |
| **OpenAI** | `langchain4j.openai` | `-Pplantuml.langchain4j.model=openai` | ⚠️ API key requis |
| **Claude** | `langchain4j.anthropic` | `-Pplantuml.langchain4j.model=claude` | ⚠️ API key requis |
| **HuggingFace** | `langchain4j.huggingface` | `-Pplantuml.langchain4j.model=huggingface` | ⚠️ API key requis |
| **Groq** | `langchain4j.groq` | `-Pplantuml.langchain4j.model=groq` | ⚠️ API key requis |

**Config exemple** :
```yaml
langchain4j:
  model: "ollama"
  ollama:
    baseUrl: "http://localhost:11434"
    modelName: "smollm:135m"
  validation: false
  maxIterations: 1
```

---

## 📁 Sorties de Test

- **Tests unitaires** : `test-output/` (à côté de `generated/`)
- **Tests fonctionnels** : `test-output/` (même répertoire)
- **RAG** : `generated/rag/` (pour l'historique d'entraînement)

**Pourquoi** : Ne pas fausser l'historique RAG avec des fichiers de test.

---

## 🔧 Configuration YAML

### Hiérarchie des sources

1. **gradle.properties** (priorité basse)
2. **plantuml-context.yml** (priorité moyenne)
3. **CLI parameters** (priorité haute)

### Variables d'environnement

Syntaxe `${VAR_NAME}` résolue depuis `System.getenv()` :

```yaml
langchain4j:
  openai:
    apiKey: "${OPENAI_API_KEY}"
```

**Fallback** : Si la variable n'existe pas, la syntaxe `${VAR_NAME}` est préservée.

### Fichiers de config

| Fichier | Usage | Git |
|---------|-------|-----|
| `plantuml-context.yml` | Config personnelle | 🚫 .gitignore |
| `plantuml-test-context.yml` | Config test CI/CD | 🚫 .gitignore |
| `sample-plantuml-context.yml` | Exemple repo | ✅ Commit |

---

## 🚦 Fin de Session — Procédure Automatique

**🚨 DÉCLENCHEUR** : "nouvelle session", "je quitte", "session terminée", "à plus tard"

**⚠️ OBLIGATION** : Exécuter CETTE procédure AVANT de répondre

**🚫 RÈGLE D'OR** :
- ❌ JAMAIS demander "Veux-tu que je...?"
- ❌ JAMAIS expliquer avant de faire
- ❌ JAMAIS répondre avant les 5 étapes terminées
- ✅ FAIRE DIRECTEMENT les 5 étapes SILENCIEUSEMENT
- ✅ Répondre UNIQUEMENT par "✅ Procédure exécutée" + 3 lignes max

### 5 Étapes

1. **Vérifier les tests** : `./gradlew test`
2. **Mettre à jour `AGENTS.md`** (section "État actuel")
3. **Mettre à jour `COMPLETED_TASKS_ARCHIVE.md`**
4. **Mettre à jour `PROMPT_REPRISE.md`**
5. **Mettre à jour `TEST_COVERAGE_ANALYSIS.md`** (si besoin)

**⚠️ Git — INTERDICTION ABSOLUE** :
- ❌ Pas de `git add`, `git commit`, `git push`, `git restore`, `git checkout`
- ✅ C'est l'utilisateur qui gère Git manuellement

**⚠️ Périmètre d'action — Tests unitaires uniquement** :
- ✅ `src/test/kotlin/plantuml/` (tests unitaires)
- ❌ `src/test/scenarios/` (Cucumber/BDD)
- ❌ `src/test/resources/` (WireMock, features)
- ❌ `src/test/features/` (scénarios BDD)

---

## 📚 Fichiers de Référence

| Fichier | Quand charger |
|---------|---------------|
| `AGENTS.md` | **Toujours** (architecture, méthodologie) |
| `AGENT_REFERENCE.md` | **Sur besoin** (référence rapide) |
| `PROMPT_REPRISE.md` | **Début session** (mission en cours) |
| `METHODOLOGIE_OPTIMISATION_TESTS.md` | **Session optimisation** |
| `TEST_COVERAGE_ANALYSIS.md` | **Création tests unitaires** |
| `SESSIONS_HISTORY.md` | **Sur besoin** (historique sessions) |
| `COMPLETED_TASKS_ARCHIVE.md` | **Fin de session** (archive) |

---

## 🧠 Leçons Critiques (Sessions Antérieures)

### Session 29 — Optimisation sans mesure
- ❌ **Erreur** : Optimiser sans mesurer AVANT/APRÈS
- ✅ **Leçon** : Toujours mesurer le temps avant de refactoriser
- ✅ **Règle** : Secondes gagnées ≠ lignes supprimées

### Session 30 — Gains illusoires
- ❌ **Erreur** : 17s gagnées sur tests @Disabled (jamais exécutés)
- ✅ **Leçon** : Optimiser un test @Disabled = 0 gain réel
- ✅ **Règle** : Mesurer sur tests exécutés uniquement

### Session 33 — Test déjà optimal
- ❌ **Erreur** : Analyser un test déjà @Disabled
- ✅ **Leçon** : Vérifier statut avant d'optimiser
- ✅ **Règle** : @Disabled = conception intentionnelle

---

## 📝 Méthodologie de Travail

### Principe : Sessions atomiques

**Règle d'or** : **1 session = 1 tâche unique et validée**

| Métrique | Cible |
|----------|-------|
| **Durée** | 15-30 minutes |
| **Fichiers modifiés** | 1-3 maximum |
| **Tests créés** | 1 fichier |
| **Échanges LLM** | 5-10 messages |

### Processus itératif

```
1. Créer le fichier de test
2. ./gradlew test
3. ✅ Si passe → Session terminée
4. ❌ Si échec → Corriger AVANT de continuer
```

**Pourquoi** :
- ✅ 1 erreur à la fois
- ✅ Base de code toujours stable
- ✅ Feedback immédiat
- ✅ Progression mesurable

---

## 🎯 Menu des Méthodologies

| Type de session | Indices | Méthodologie | Fichier |
|-----------------|---------|--------------|---------|
| **Optimisation test fonctionnel** | "optimiser", "réduire temps", `*FunctionalTest.kt` | Mesurer AVANT/APRÈS | `METHODOLOGIE_OPTIMISATION_TESTS.md` |
| **Création test unitaire** | "créer test", "couverture", `*Test.kt` | Analyser couverture | `TEST_COVERAGE_ANALYSIS.md` |
| **Debug test fonctionnel** | "debug", "exécuter test", `*FunctionalTest.kt` | Run → Debug → Optimise | `METHODOLOGIE_OPTIMISATION_TESTS.md` |
| **Correction bug** | "corriger", "bug", "fix" | Aucune — agir directement | — |
| **Nouvelle feature** | "ajouter", "nouvelle", "feature" | Aucune — agir directement | — |
| **Fin de session** | "nouvelle session", "je quitte" | Procédure automatique (5 étapes) | `COMPLETED_TASKS_ARCHIVE.md` |

### Règles de proposition

1. ✅ **Toujours proposer** si session matche un type connu
2. ✅ **Attendre confirmation** avant de charger fichier
3. ✅ **Citer 3 points clés** de la méthodologie
4. ❌ **Jamais charger sans confirmation** (sauf fin de session)
5. ❌ **Jamais proposer si session claire**

---

## 🔄 Reprise de Session

**Prompt d'ouverture** :
```
Nouvelle session. Contexte chargé :
1. AGENTS.md (architecture, décisions, méthodologie)
2. PROMPT_REPRISE.md (mission en cours)

Mission : [Décrire la tâche unique de cette session]

Contrainte : 1 fichier à la fois, validation après chaque changement.
```
