# 📊 Dataset Fine-tuning — PlantUML Gradle Plugin

**Objectif** : Créer un dataset d'entraînement pour agent IA spécialisé (architecture Gradle, Kotlin, tests, RAG, API Key Pool)

**Période** : 8 avril 2026 — 21 avril 2026  
**Total sessions** : 115 sessions  
**Format cible** : JSONL (JSON Lines) pour fine-tuning LLM

---

## 📁 Structure du Dataset

### Format JSONL

Chaque ligne = un exemple d'entraînement :

```json
{
  "id": "session_114_001",
  "session": 114,
  "type": "architecture_decision",
  "prompt": "Contexte : Où placer plantuml-test-context.yml ?",
  "response": "Décision : Racine du plugin (plantuml-plugin/). Raisons : 1) Accessible via project.rootDir.resolve(), 2) Protégé par .gitignore, 3) GitHub Secrets peut l'injecter en CI, 4) Aucune modification Gradle requise.",
  "metadata": {
    "date": "2026-04-21",
    "files_created": ["DECISIONS_ARCHITECTURE.md"],
    "files_modified": [".gitignore"],
    "outcome": "success"
  }
}
```

---

## 📊 Sources de Données

### Niveau 1 — Données Structurées (Priorité Haute)

| Source | Fichiers | Exemples | Format |
|--------|----------|----------|--------|
| **Sessions individuelles** | `.agents/sessions/*.md` (29 fichiers) | 114 exemples | Markdown structuré |
| **Résumés de sessions** | `.agents/archives/sessions_summaries/*.md` (13 fichiers) | 13 exemples | Markdown + métadonnées |
| **Prompts de reprise** | `.agents/archives/prompts_archive/*.md` (8 fichiers) | 8 exemples | Prompt + contexte |
| **Décisions architecturales** | `.agents/DECISIONS_ARCHITECTURE.md` (1 fichier) | 5 décisions | Decision record |

**Total Niveau 1** : ~140 exemples structurés

---

### Niveau 2 — Données Semi-Structurées (Priorité Moyenne)

| Source | Fichiers | Exemples | Format |
|--------|----------|----------|--------|
| **SESSIONS_HISTORY_83-95.md** | 1 fichier | 13 sessions | Détails complets |
| **SESSIONS_96-113.md** | 1 fichier | 18 sessions | Détails complets |
| **SESSIONS_1-72_ARCHIVE.md** | 1 fichier | 72 sessions (résumé) | Résumé |
| **HISTORIQUE_COMPLET_1-113.md** | 1 fichier | 113 sessions | Vue globale |
| **HISTORIQUE_RECONSTRUCTION_GIT.md** | 1 fichier | 115 sessions | Reconstruction Git |

**Total Niveau 2** : ~230 exemples (moins détaillés)

---

### Niveau 3 — Données Brutes (Priorité Basse)

| Source | Fichiers | Taille | Format |
|--------|----------|--------|--------|
| **COMPLETED_TASKS_ARCHIVE_2026-04.md** | 1 fichier | 77 593 lignes | Archive brute |
| **CODE_REVIEW_2026-04.md** | 1 fichier | 47 067 lignes | Revues code |
| **METHODOLOGIE_OPTIMISATION_TESTS.md** | 1 fichier | 17 124 lignes | Méthodologie |
| **AGENT_MODUS_OPERANDI.adoc** | 1 fichier | 900+ lignes | Stratégie LAZY/EAGER |

**Total Niveau 3** : ~142 000 lignes de texte brut (à parser)

---

## 🎯 Catégories d'Entraînement

### 1. Architecture & Design (20%)

**Exemples** :
- Décisions d'architecture (DECISIONS_ARCHITECTURE.md)
- Structure du plugin (ARCHITECTURE.md)
- Organisation des fichiers (INDEX.md, HISTORIQUE_*.md)

**Prompt type** :
```
Contexte : Plugin Gradle avec tests unitaires, fonctionnels, Cucumber.
Question : Où placer les fichiers de configuration de test ?
```

**Response attendue** :
```
Décision : Racine du plugin (plantuml-plugin/).
Alternatives considérées :
1. src/test/resources/ — Rejeté (modif Gradle requise)
2. src/functionalTest/resources/ — Rejeté (pas accessible aux unit tests)
3. Racine — Choisi (accessible, .gitignore, CI-friendly)
```

---

### 2. Tests & Validation (30%)

**Exemples** :
- Tests unitaires (198 tests)
- Tests fonctionnels (57 tests)
- Tests Cucumber BDD (81 scénarios)
- Tests performance (mockés)

**Prompt type** :
```
Contexte : Test fonctionnel avec Gradle TestKit échoue avec timeout.
Question : Comment déboguer ?
```

**Response attendue** :
```
Étapes :
1. Vérifier WireMock configuré (port, mappings)
2. Ajouter timeout dans build.gradle.kts (30s)
3. Utiliser @TempDir pour isolation
4. Logger avec showStandardStreams = true
```

---

### 3. Code Kotlin & Gradle DSL (25%)

**Exemples** :
- PlantumlPlugin.kt (nested class PlantumlExtension)
- models.kt (11 data classes)
- PlantumlManager.kt (objet Kotlin singleton)
- build.gradle.kts (SourceSets, tasks)

**Prompt type** :
```
Contexte : Créer une tâche Gradle avec TestKit.
Question : Comment configurer functionalTest SourceSet ?
```

**Response attendue** :
```kotlin
val functionalTest: SourceSet by sourceSets.creating {
    java.srcDirs("src/functionalTest/kotlin")
    resources.srcDirs("src/functionalTest/resources")
}

dependencies {
    add(functionalTest.implementationConfigurationName, gradleTestKit())
    add(functionalTest.implementationConfigurationName, sourceSets.main.get().output)
}
```

---

### 4. RAG & LLM Integration (15%)

**Exemples** :
- RAG avec pgvector + testcontainers
- LlmService (Ollama, OpenAI, HuggingFace, Gemini, Mistral)
- API Key Pool (rotation, quotas, audit)
- DiagramProcessor ( Attempt History, raffinement)

**Prompt type** :
```
Contexte : Rotation de clés API avec gestion de quota.
Question : Comment implémenter QuotaTracker ?
```

**Response attendue** :
```kotlin
class QuotaTracker(
    private val keys: List<ApiKeyEntry>,
    private val threshold: Double = 0.8
) {
    private val usage = mutableMapOf<String, Int>()
    
    fun shouldRotate(keyId: String): Boolean {
        val current = usage[keyId] ?: 0
        val max = keys.find { it.id == keyId }?.quota?.maxRequests ?: 100
        return current >= max * threshold
    }
}
```

---

### 5. Documentation & Procédures (10%)

**Exemples** :
- PROCÉDURES.md (fin de session)
- AGENT_MODUS_OPERANDI.adoc (stratégie LAZY/EAGER)
- TROUBLESHOOTING.md (10 questions)
- README_truth.adoc (guide complet)

**Prompt type** :
```
Contexte : Fin de session agent IA.
Question : Quelle procédure suivre ?
```

**Response attendue** :
```
Procédure de fin de session :
1. Créer archive dans .agents/sessions/XX-sujet.md
2. Mettre à jour PROMPT_REPRISE.md pour session suivante
3. Mettre à jour SESSIONS_HISTORY.md
4. Mettre à jour .agents/INDEX.md (sessions récentes)
5. NE PAS lancer de tests (sauf demande explicite)
6. NE PAS committer (sauf permission explicite)
```

---

## 📈 Métriques du Dataset

### Volume Estimé

| Niveau | Exemples | Tokens (estim.) | Pourcentage |
|--------|----------|-----------------|-------------|
| Niveau 1 (structuré) | 140 | ~500k | 20% |
| Niveau 2 (semi-structuré) | 230 | ~300k | 30% |
| Niveau 3 (brut) | ~1000 (extrait) | ~500k | 50% |
| **Total** | **~1370** | **~1.3M** | **100%** |

### Répartition par Type

| Type | Exemples | Pourcentage |
|------|----------|-------------|
| Architecture & Design | 274 | 20% |
| Tests & Validation | 411 | 30% |
| Code Kotlin & Gradle | 342 | 25% |
| RAG & LLM Integration | 205 | 15% |
| Documentation & Procédures | 138 | 10% |

### Qualité des Données

| Critère | Cible | Actuel |
|---------|-------|--------|
| Exemples structurés | 100% | Niveau 1 : 100%, Niveau 2-3 : variable |
| Métadonnées complètes | 100% | Niveau 1 : 80%, Niveau 2-3 : 50% |
| Paires prompt/response | 100% | À extraire (semi-auto) |
| Diversité des tâches | 5 catégories | ✅ Couvert |

---

## 🔧 Pipeline d'Extraction

### Étape 1 : Extraction Sessions Individuelles

```bash
# Parser .agents/sessions/*.md
for file in .agents/sessions/*.md; do
    session_num=$(basename $file | grep -oP '^\d+')
    # Extraire : résumé, fichiers créés, fichiers modifiés, résultats
done
```

**Champs extraits** :
- `session` : numéro
- `date` : date de session
- `type` : test/docs/fix/feat/design
- `sujet` : titre de la session
- `resultat` : succès/échec/partiel
- `fichiers_crées` : liste
- `fichiers_modifies` : liste
- `tests_passes` : nombre
- `tests_total` : nombre

---

### Étape 2 : Extraction Décisions Architecturales

```bash
# Parser .agents/DECISIONS_ARCHITECTURE.md
# Extraire chaque décision avec :
# - context, alternatives, rationale, implementation
```

**Champs extraits** :
- `decision_id` : 001, 002, ...
- `sujet` : titre de la décision
- `context` : problème résolu
- `alternatives` : liste d'options (avec avantages/inconvénients)
- `decision` : option choisie
- `rationale` : justification
- `implementation` : code/bash/yaml

---

### Étape 3 : Extraction Prompts de Reprise

```bash
# Parser .agents/archives/prompts_archive/*.md
# Extraire : mission, contexte, priorités, commandes
```

**Champs extraits** :
- `session` : numéro
- `mission` : objectif de la session
- `contexte` : résumé session précédente
- `priorites` : liste de tâches
- `commandes` : bash commands
- `criteres_acceptation` : checklist

---

### Étape 4 : Extraction Code + Tests

```bash
# Parser src/**/*.kt + src/**/*.feature
# Extraire : fichier, fonction, tests associés
```

**Champs extraits** :
- `fichier` : chemin
- `type` : main/test/functionalTest
- `fonction` : nom de la fonction/classe
- `description` : KDoc ou commentaire
- `tests_associes` : liste de tests
- `exemples_code` : snippets

---

### Étape 5 : Génération Paires Prompt/Response

**Template pour architecture** :
```
Prompt: "Contexte : {context}. Question : {question}"
Response: "{decision}. Raisons : {rationale}. Alternatives : {alternatives}"
```

**Template pour tests** :
```
Prompt: "Contexte : {test_context}. Problème : {error}. Question : Comment corriger ?"
Response: "{solution}. Fichiers modifiés : {files}. Commandes : {commands}"
```

**Template pour code** :
```
Prompt: "Contexte : {feature}. Question : Comment implémenter {function} ?"
Response: "```kotlin {code} ``` Explication : {explanation}"
```

---

## 📁 Organisation des Fichiers de Dataset

```
dataset-finetuning/
├── raw/                          # Données brutes extraites
│   ├── sessions/                 # 29 fichiers .md
│   ├── summaries/                # 13 fichiers _SUMMARY.md
│   ├── prompts/                  # 8 fichiers PROMPT_REPRISE*.md
│   ├── decisions/                # 1 fichier DECISIONS_ARCHITECTURE.md
│   └── archives/                 # SESSIONS_*.md, HISTORIQUE_*.md
│
├── processed/                    # Données transformées
│   ├── sessions.jsonl            # 115 exemples (sessions)
│   ├── decisions.jsonl           # 5 exemples (décisions)
│   ├── prompts.jsonl             # 8 exemples (prompts de reprise)
│   ├── code.jsonl                # ~500 exemples (code + tests)
│   └── documentation.jsonl       # ~200 exemples (docs)
│
├── train/                        # Dataset final
│   ├── train.jsonl               # 80% pour training
│   ├── validation.jsonl          # 10% pour validation
│   └── test.jsonl                # 10% pour test
│
└── metadata/
    ├── statistics.json           # Stats du dataset
    ├── categories.json           # Répartition par catégorie
    └── quality_report.md         # Rapport qualité
```

---

## ✅ Checklist de Création

### Extraction (Semaine 1)

- [ ] Parser `.agents/sessions/*.md` (29 fichiers)
- [ ] Parser `.agents/archives/sessions_summaries/*.md` (13 fichiers)
- [ ] Parser `.agents/archives/prompts_archive/*.md` (8 fichiers)
- [ ] Parser `.agents/DECISIONS_ARCHITECTURE.md` (5 décisions)
- [ ] Parser `SESSIONS_HISTORY_83-95.md` (13 sessions)
- [ ] Parser `SESSIONS_96-113.md` (18 sessions)
- [ ] Parser `HISTORIQUE_COMPLET_1-113.md` (113 sessions)
- [ ] Parser `HISTORIQUE_RECONSTRUCTION_GIT.md` (115 sessions)

### Transformation (Semaine 2)

- [ ] Générer paires prompt/response pour sessions
- [ ] Générer paires prompt/response pour décisions
- [ ] Générer paires prompt/response pour code
- [ ] Ajouter métadonnées (date, type, succès)
- [ ] Valider format JSONL

### Validation (Semaine 3)

- [ ] Vérifier qualité des paires (lisibilité, cohérence)
- [ ] Split train/validation/test (80/10/10)
- [ ] Générer statistiques
- [ ] Créer rapport qualité

---

## 🎯 Prochaines Étapes

1. **Créer script d'extraction** — Python/Node.js pour parser Markdown
2. **Définir schema JSON** — Structure uniforme pour tous les exemples
3. **Extraire Niveau 1** — Sessions individuelles (priorité haute)
4. **Extraire Niveau 2** — Archives consolidées
5. **Extraire Niveau 3** — Données brutes (optionnel)
6. **Générer dataset final** — Split train/validation/test
7. **Fine-tuner modèle** — Test avec dataset

---

**Document créé** : 21 avril 2026 (Session 115)  
**Type** : Plan de création de dataset  
**Prochaine action** : Script d'extraction Niveau 1
