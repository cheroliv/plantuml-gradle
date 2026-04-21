# 📝 Décisions Architectures — PlantUML Gradle Plugin

**Dernière mise à jour** : 21 avril 2026  
**Session** : 114  
**Type** : LAZY (consultation sur besoin)

---

## 📁 Decision 001 — Emplacement de `plantuml-test-context.yml`

**Date** : 21 avril 2026  
**Statut** : ✅ ADOPTÉE  
**Alternative considérée** : 3 options

### Contexte

Le plugin PlantUML utilise des fichiers de configuration YAML pour :
- **Production** : `plantuml-context.yml` (racine du projet consommateur)
- **Test** : `plantuml-test-context.yml` (credentials, mocks, services réels)

**Problème** : Où placer `plantuml-test-context.yml` pour :
1. ✅ Être accessible aux tests (unitaires, fonctionnels, Cucumber)
2. ✅ Être protégé par `.gitignore`
3. ✅ Pouvoir être injecté par GitHub Secrets en CI
4. ✅ Ne pas modifier le build.gradle.kts (risque de cassure)
5. ✅ Être clairement séparé du consommateur

---

### Alternatives Considérées

#### Option 1 : `src/test/resources/`
```
plantuml-plugin/
└── src/test/resources/
    └── plantuml-test-context.yml
```

**Avantages** :
- ✅ Standard Gradle (classpath automatique)
- ✅ Cohérent avec `ollama-local-smollm-135.yml`
- ✅ Un seul fichier pour tous les tests

**Inconvénients** :
- ❌ Nécessite modification Gradle pour partager avec `functionalTest`
- ❌ Risque de cassure du build script
- ❌ `functionalTest` a son propre SourceSet (classpath isolé)

**Code requis** :
```kotlin
// build.gradle.kts — à ajouter après functionalTest SourceSet
dependencies {
    add(functionalTest.implementationConfigurationName, sourceSets.test.get().output)
}
```

**Verdict** : ❌ Rejeté — Trop risqué pour le build script

---

#### Option 2 : `src/functionalTest/resources/`
```
plantuml-plugin/
└── src/functionalTest/resources/
    └── plantuml-test-context.yml
```

**Avantages** :
- ✅ Pas de modification Gradle
- ✅ Ressources isolées dans functionalTest

**Inconvénients** :
- ❌ Tests unitaires ne peuvent pas y accéder
- ❌ Tests Cucumber ne peuvent pas y accéder
- ❌ Duplication requise si besoin dans plusieurs SourceSets
- ❌ Moins cohérent avec `ollama-local-smollm-135.yml`

**Verdict** : ❌ Rejeté — Ne couvre pas tous les types de tests

---

#### Option 3 : Racine du plugin (CHOISIE) ✅
```
plantuml-plugin/
├── plantuml-test-context.yml  # À la racine du plugin
├── .gitignore
└── src/...
```

**Avantages** :
- ✅ Accessible via chemin absolu : `project.rootDir.resolve("plantuml-test-context.yml")`
- ✅ Protégé par `.gitignore` du plugin
- ✅ GitHub Secrets peut l'injecter en CI
- ✅ **Aucune modification Gradle requise**
- ✅ Clairement séparé du consommateur (racine ≠ racine)
- ✅ Visible et explicite dans l'arborescence

**Inconvénients** :
- ⚠️ Chemin explicite requis dans les tests (mais simple)
- ⚠️ Pas dans le classpath (mais ce n'est pas nécessaire)

**Verdict** : ✅ **CHOISI** — Meilleur compromis

---

### Implémentation

#### 1. Fichier déplacé
```bash
mv plantuml-test-context.yml plantuml-plugin/
```

#### 2. `.gitignore` mis à jour
```gitignore
# plantuml-plugin/.gitignore
plantuml-test-context.yml
```

#### 3. Chargement dans les tests (Kotlin)
```kotlin
// Dans un test Gradle TestKit
val testConfigPath = projectDir.resolve("plantuml-test-context.yml").absolutePath

// Ou via propriété système
val testConfigPath = System.getProperty("plantuml.test.config")
    ?: projectDir.resolve("plantuml-test-context.yml").absolutePath
```

#### 4. Injection GitHub Secrets (CI)
```yaml
# .github/workflows/ci.yml
- name: Inject test credentials
  run: |
    cat > plantuml-plugin/plantuml-test-context.yml << 'EOF'
    ${{ secrets.PLANTUML_TEST_CONTEXT }}
    EOF
  working-directory: ${{ github.workspace }}
```

---

### Structure Finale

```
plantuml-gradle/
├── plantuml-context.yml              # Production (consommateur)
├── plantuml-context.example.yml      # Exemple (versionné)
├── plantuml-plugin/
│   ├── plantuml-test-context.yml     # Test (protégé .gitignore)
│   ├── .gitignore                    # Contient plantuml-test-context.yml
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       ├── test/
│       │   └── resources/
│       │       └── ollama-local-smollm-135.yml  # Mock local
│       └── functionalTest/
│           └── resources/
│               └── wiremock/         # Mocks WireMock
```

---

### Leçons Apprises

1. **Ne pas modifier un build.gradle.kts fonctionnel** — Risque de régressions
2. **Séparer plugin vs consommateur** — Le plugin a ses propres fichiers de test
3. **`.gitignore` au bon niveau** — Plugin `.gitignore` protège ses credentials
4. **Chemin absolu > classpath** — Plus simple pour TestKit et CI
5. **GitHub Secrets injection** — Fichier à la racine = injection triviale

---

### Références

- **Fichier** : `plantuml-plugin/plantuml-test-context.yml`
- **`.gitignore`** : `plantuml-plugin/.gitignore`
- **Workflow CI** : `.github/workflows/ci.yml` (à mettre à jour)
- **Secret GitHub** : `PLANTUML_TEST_CONTEXT` (à créer)

---

## 📁 Decision 002 — Structure des Archives de Sessions

**Date** : 21 avril 2026  
**Statut** : ✅ ADOPTÉE

### Contexte

Le projet a accumulé 113+ sessions depuis mars 2026.  
**Problème** : Comment archiver sans perdre l'historique pour future distillation/fine-tuning ?

### Décision

**3 niveaux d'archives** :

| Niveau | Fichier | Sessions | Détail |
|--------|---------|----------|--------|
| 1 | `SESSIONS_1-72_ARCHIVE.md` | 1-72 | Résumé + détails 61-72 |
| 2 | `SESSIONS_HISTORY_83-95.md` | 83-95 | Détails complets + workarounds |
| 3 | `SESSIONS_96-113.md` | 96-113 | Détails complets |
| 4 | `.agents/sessions/*.md` | 73-113 (partiel) | 28 fichiers individuels |
| 5 | `HISTORIQUE_COMPLET_1-113.md` | 1-113 | Vue d'ensemble consolidée |

### Rationale

1. **Préserver l'historique** — Sessions 1-72 dans archive unique
2. **Détails progressifs** — Plus on avance, plus c'est détaillé
3. **Fichiers individuels** — Sessions récentes (108-113) ont leur fichier
4. **Vue d'ensemble** : `HISTORIQUE_COMPLET_1-113.md` pour navigation rapide

### Pour Fine-tuning Dataset

**Fichiers prioritaires** (structurés, métadonnées) :
1. `.agents/sessions/*.md` (28 fichiers)
2. `.agents/archives/sessions_summaries/*.md` (13 fichiers)
3. `.agents/archives/prompts_archive/*.md` (8 fichiers)

**Fichiers secondaires** (bruts, à parser) :
1. `COMPLETED_TASKS_ARCHIVE_2026-04.md` (77k lignes)
2. `CODE_REVIEW_2026-04.md` (47k lignes)

---

## 📁 Decision 003 — Stratégie LAZY/EAGER pour OpenCode

**Date** : 20 avril 2026 (Session 109)  
**Statut** : ✅ ADOPTÉE  
**Document** : `AGENT_MODUS_OPERANDI.adoc` (900+ lignes)

### Contexte

Optimiser l'efficacité d'OpenCode sur des projets complexes (EPIC multi-sessions, 10+ fichiers, 100+ décisions).

### Décision

**Séparation EAGER/LAZY** :

| Type | Fichiers | Taille | Chargement |
|------|----------|--------|------------|
| **EAGER** | `PROMPT_REPRISE.md`, `*_ESSENTIALS.md`, `.agents/INDEX.md` | ≤ 100 lignes | **Toujours** (début session) |
| **LAZY** | Archives, références, procédures | Illimité | **Sur demande** ("Je charge X ?") |

### Analogie

- **EAGER** = Tableau de bord de voiture (vitesse, carburant, alertes)
- **LAZY** = Manuel du propriétaire (détails techniques, historique)

### Règles Absolues

1. ❌ **Jamais de commit sans permission explicite**
2. ❌ **Jamais de tests en fin de session sans demande**
3. ✅ **Archive systématique** en fin de session
4. ✅ **1 session = 1 objectif** (15-30 minutes)

### Références

- **Document principal** : `AGENT_MODUS_OPERANDI.adoc`
- **Essentials** : `API_KEY_POOL_ESSENTIALS.md` (50 lignes)
- **Procédures** : `.agents/PROCEDURES.md`

---

## 📁 Decision 004 — Fichier Maître INDEX.md Unique

**Date** : 21 avril 2026  
**Statut** : ✅ ADOPTÉE

### Contexte

Deux fichiers `INDEX.md` existaient :
1. `plantuml-plugin/INDEX.md` — Obsolète (session 83)
2. `plantuml-plugin/.agents/INDEX.md` — Plus récent (session 110)

### Décision

**Un seul fichier maître** : `.agents/INDEX.md`

**Rationale** :
- ✅ Centralise l'information
- ✅ Mis à jour avec sessions 108-113
- ✅ Contient règles absolues (commits, tests)
- ✅ Historique récent (15 dernières sessions)

**Suppression** : `plantuml-plugin/INDEX.md` (restauré car contenait données historiques pour distillation)

**Finalement** : **Les deux fichiers conservés** — `INDEX.md` racine = vue légère, `.agents/INDEX.md` = détail sessions

---

## 📁 Decision 005 — Historique Complet 1-113

**Date** : 21 avril 2026  
**Statut** : ✅ CRÉÉ

### Contexte

Besoin d'un historique unique pour :
- Navigation rapide
- Dataset fine-tuning
- Compréhension évolution du projet

### Décision

**Fichier créé** : `.agents/HISTORIQUE_COMPLET_1-113.md`

**Contenu** :
- Vue d'ensemble sessions 1-113
- Statistiques globales
- EPICs progression
- Liens vers toutes les archives
- Lacunes identifiées (sessions 1-60, 75-82)

**Pour dataset** :
- 28 sessions structurées (73-113 partiel)
- 13 résumés (61-76)
- 8 prompts de reprise (65-76)
- 77k lignes d'archive brute à exploiter

---

## 🔗 Index des Décisions

| # | Sujet | Fichier | Statut |
|---|-------|---------|--------|
| 001 | Emplacement `plantuml-test-context.yml` | Ce document | ✅ ADOPTÉE |
| 002 | Structure des archives | Ce document | ✅ ADOPTÉE |
| 003 | Stratégie LAZY/EAGER | `AGENT_MODUS_OPERANDI.adoc` | ✅ ADOPTÉE |
| 004 | Fichier INDEX.md unique | Ce document | ✅ ADOPTÉE (modifié) |
| 005 | Historique complet 1-113 | `HISTORIQUE_COMPLET_1-113.md` | ✅ CRÉÉ |

---

**Document créé** : 21 avril 2026 (Session 114)  
**Type** : LAZY (consultation sur besoin)  
**Maintenu par** : Agent OpenCode (fin de session)
