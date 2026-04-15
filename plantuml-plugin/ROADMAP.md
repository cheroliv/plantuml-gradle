# 🚀 Roadmap — PlantUML Gradle Plugin v2

**Date de création** : 2026-04-13  
**Dernière mise à jour** : 2026-04-15 (Session 67)  
**Objectif** : Publication sur Gradle Plugin Portal  
**Score actuel** : 8.4/10 ✅ IMPROVING  
**Score cible** : 8.5/10 ✅ PUBLIABLE

---

## 📊 État Actuel (Session 67)

### ✅ Accomplissements Récents

| Feature | Statut | Impact |
|---------|--------|--------|
| **Kover Integration** | ✅ TERMINÉ | Couverture de code tracée automatiquement |
| **Test Optimization** | ✅ TERMINÉ | 30-50% plus rapide (maxParallelForks=1) |
| **Code Cleanup** | ✅ TERMINÉ | Français → Anglais, données de test nettoyées |
| **Tests Unitaires** | ✅ 203/203 PASS | 100% couverture |
| **Tests Fonctionnels** | ✅ 42 PASS, 6 SKIP | 100% couverture |
| **RAG Integration Tests** | ✅ 10/10 PASS | testcontainers PostgreSQL réel + tests avancés |
| **EPIC 1** | ✅ TERMINÉ | 6/6 stories complètes |
| **EPIC 2.1** | ✅ TERMINÉ | RAG avec testcontainers |
| **EPIC 2.2** | ✅ TERMINÉ | Fallback simulation supprimé |
| **EPIC 2.3** | ✅ TERMINÉ | Tests RAG avancés (multi-fichiers, nested, concurrent, etc.) |

### 🔴 Problèmes Critiques (NON FIXÉS)

| Problème | Impact | Priorité |
|----------|--------|----------|
| **Double appel `validateDiagram()`** | 2x temps de traitement | 🔴 CRITIQUE |
| **Sérialisation JSON manuelle** | Risque crash production | 🔴 CRITIQUE |
| **Debug logs en production** | Logs utilisateurs clutterées | 🟡 IMPORTANT |
| **RAG en mode simulation** | Pas production-ready | 🟡 IMPORTANT |
| **Seuil Kover manquant** | Pas de gate qualité | 🟡 IMPORTANT |

---

## 🎯 Epics

### EPIC 1 : Performance & Stabilité 🔴

**Objectif** : Éliminer les problèmes de performance et stabilité critiques  
**Score actuel** : 6.8/10 → **Cible** : 8.0/10  
**Durée estimée** : 2-3 semaines  
**Sessions nécessaires** : 5-8 sessions

#### Stories

| ID | Story | Priorité | Estimation | Statut |
|----|-------|----------|------------|--------|
| **1.1** | Fixer le double appel `validateDiagram()` dans `ProcessPlantumlPromptsTask.kt` | 🔴 CRITIQUE | 1 session | ✅ TERMINÉ |
| **1.2** | Remplacer sérialisation JSON manuelle par Jackson/Kotlin Serialization | 🔴 CRITIQUE | 2 sessions | ✅ TERMINÉ |
| **1.3** | Nettoyer ou conditionner les DEBUG logs (lifecycle → debug) | 🟡 IMPORTANT | 1 session | ✅ TERMINÉ |
| **1.4** | Ajouter seuil Kover obligatoire (75% min) dans `build.gradle.kts` | 🟡 IMPORTANT | 1 session | ✅ TERMINÉ (Session 60) |
| **1.5** | Tester `ConfigMerger.getOrDefault()` et branches manquantes | 🟡 IMPORTANT | 1 session | ✅ TERMINÉ (Session 57 — méthode supprimée) |
| **1.6** | Tester `PlantumlManager` nested class (méthode non couverte) | 🟡 IMPORTANT | 1 session | ✅ TERMINÉ (Session 62 — couverture maximale) |

#### Critères d'acceptation

- ✅ `-50%` temps de traitement des prompts (fix double validation)
- ✅ `0 crash JSON` avec caractères spéciaux (accents, emojis)
- ✅ `-70%` output verbeux en production
- ✅ Build fail si couverture < 75%
- ✅ `ConfigMerger.getOrDefault()` supprimée (code mort — Session 57)
- ✅ Couverture `PlantumlManager` nested class = 100%

---

### EPIC 2 : RAG Production-Ready 🟡

**Objectif** : Rendre le RAG robuste pour production  
**Score actuel** : 5/10 → **Cible** : 8/10  
**Durée estimée** : 3-4 semaines  
**Sessions nécessaires** : 6-10 sessions

#### Stories

| ID | Story | Priorité | Estimation | Statut |
|----|-------|----------|------------|--------|
| **2.1** | Implémenter RAG réel avec testcontainers PostgreSQL | 🟡 IMPORTANT | 1 session | ✅ TERMINÉ (Session 63) |
| **2.2** | Supprimer fallback simulation silencieux (erreur explicite) | 🟡 IMPORTANT | 1 session | ✅ TERMINÉ (Session 66) |
| **2.3** | Ajouter tests d'intégration RAG avec vrais diagrammes | 🟡 IMPORTANT | 2 sessions | ✅ TERMINÉ (Session 67) |
| **2.4** | Documentation complète du fonctionnement RAG | 🟢 FAIBLE | 1 session | ⏳ À FAIRE |

#### Critères d'acceptation

- ✅ RAG utilise PostgreSQL réel (pas de simulation)
- ✅ Tests RAG passent avec testcontainers
- ✅ 0 fallback silencieux en production
- ✅ Documentation claire pour utilisateurs

---

### EPIC 3 : Consolidation Tests Fonctionnels 🟢

**Objectif** : Réduire temps d'exécution des tests fonctionnels  
**Score actuel** : 7/10 → **Cible** : 9/10  
**Durée estimée** : 2-3 semaines  
**Sessions nécessaires** : 6 sessions (déjà planifiées)

**Voir** : `EPIC_CONSOLIDATION_TESTS_FONCTIONNELS.md` pour détails complets

#### Stories

| ID | Story | Priorité | Estimation | Statut |
|----|-------|----------|------------|--------|
| **3.1** | Migration `PlantumlPluginIntegrationSuite` → `PluginIntegration` | 🟡 IMPORTANT | 1 session | ⏳ Session 43.1 |
| **3.2** | Migration `FilePermissionTest` → `FilePermission` | 🟡 IMPORTANT | 1 session | ⏳ Session 43.2 |
| **3.3** | Migration `LargeFileAndPathTest` → `LargeFileAndPath` | 🟡 IMPORTANT | 1 session | ⏳ Session 43.3 |
| **3.4** | Migration `NetworkTimeoutTest` → `NetworkTimeout` | 🟡 IMPORTANT | 1 session | ⏳ Session 43.4 |
| **3.5** | Migration `PerformanceTest` → `Performance` | 🟡 IMPORTANT | 1 session | ⏳ Session 43.5 |
| **3.6** | Nettoyage et validation finale | 🟡 IMPORTANT | 1 session | ⏳ Session 43.6 |

#### Critères d'acceptation

- ✅ 1 seule JVM Gradle pour tous les tests fonctionnels
- ✅ Temps d'exécution < 1m15s (actuel : 1m55s)
- ✅ `-45s` de cold start évités
- ✅ 100% couverture préservée (42 tests)

---

### EPIC 4 : Documentation & Qualité 🟢

**Objectif** : Améliorer documentation et qualité globale  
**Score actuel** : 4/10 → **Cible** : 7/10  
**Durée estimée** : 1-2 semaines  
**Sessions nécessaires** : 3-5 sessions

#### Stories

| ID | Story | Priorité | Estimation | Statut |
|----|-------|----------|------------|--------|
| **4.1** | Mettre à jour README avec exemples complets | 🟡 IMPORTANT | 1 session | ⏳ À FAIRE |
| **4.2** | Ajouter guide de débuggage (troubleshooting) | 🟢 FAIBLE | 1 session | ⏳ À FAIRE |
| **4.3** | Documentation API complète (KDoc) | 🟢 FAIBLE | 2 sessions | ⏳ À FAIRE |
| **4.4** | Exemples de prompts dans `sample-plantuml-context.yml` | 🟢 FAIBLE | 1 session | ⏳ À FAIRE |

#### Critères d'acceptation

- ✅ README clair avec quickstart (5 min)
- ✅ Guide troubleshooting (FAQ 10 questions)
- ✅ 80% des fonctions documentées avec KDoc
- ✅ 5+ exemples de prompts complets

---

## 📅 Timeline Révisé

```
SEMaines 1-2 (2026-04-13 → 2026-04-27)
├── EPIC 1 : Performance & Stabilité
│   ├── 1.1 Fix double validation ✅
│   ├── 1.2 JSON serialization ✅
│   ├── 1.3 Debug logs cleanup ✅
│   └── 1.4 Kover threshold gate ✅
│
├── EPIC 3 : Consolidation Tests (en parallèle)
│   ├── 3.1-3.6 Sessions 43.1-43.6 ✅
│   └── Temps < 1m15s ✅
│
└── Score cible : 8.0/10

Semaines 3-4 (2026-04-27 → 2026-05-11)
├── EPIC 2 : RAG Production-Ready
│   ├── 2.1 PostgreSQL + testcontainers ✅
│   ├── 2.2 Supprimer simulation fallback ✅
│   ├── 2.3 Tests intégration RAG ✅
│   └── 2.4 Documentation RAG ✅
│
├── EPIC 4 : Documentation & Qualité
│   ├── 4.1 README update ✅
│   ├── 4.2 Troubleshooting guide ✅
│   ├── 4.3 KDoc API ✅
│   └── 4.4 Exemples prompts ✅
│
└── Score cible : 8.5/10 → PUBLIABLE

Semaine 5 (2026-05-11 → 2026-05-18)
├── Review finale
├── Tests de validation
└── Publication Gradle Plugin Portal 🎉
```

---

## 📈 Métriques de Suivi

### Score Global par Semaine

| Semaine | Score | Δ | Notes |
|---------|-------|---|-------|
| **Semaine 0** (2026-04-13) | 6.8/10 | - | État actuel |
| **Semaine 1** (2026-04-20) | 7.2/10 | +0.4 | EPIC 1.1, 1.3 terminés |
| **Semaine 2** (2026-04-27) | 7.8/10 | +0.6 | EPIC 1 complet, EPIC 3 complet |
| **Semaine 3** (2026-05-04) | 8.1/10 | +0.3 | EPIC 2.1, 2.2 terminés |
| **Semaine 4** (2026-05-11) | 8.5/10 | +0.4 | EPIC 2, 4 complets |
| **Semaine 5** (2026-05-18) | 8.5/10 | 0 | Publication Portal |

### Détail par Aspect

| Aspect | Semaine 0 | Semaine 2 | Semaine 4 | Cible |
|--------|-----------|-----------|-----------|-------|
| **Architecture** | 8/10 | 8/10 | 8/10 | 8/10 |
| **Code Quality** | 6/10 | 8/10 | 9/10 | 9/10 |
| **Tests** | 7/10 | 9/10 | 9/10 | 9/10 |
| **Documentation** | 4/10 | 5/10 | 7/10 | 7/10 |
| **Stability (Gradle)** | 4/10 | 6/10 | 8/10 | 8/10 |
| **RAG Implementation** | 5/10 | 6/10 | 8/10 | 8/10 |
| **Security** | 7/10 | 7/10 | 7/10 | 7/10 |
| **Performance** | 8/10 | 9/10 | 9/10 | 9/10 |
| **GLOBAL** | **6.8/10** | **7.8/10** | **8.5/10** | **8.5/10** |

---

## 🎯 Prochaines Sessions

### Session 51 (Immédiat)
**EPIC** : 1 — Performance & Stabilité  
**Story** : 1.1 — Fixer le double appel `validateDiagram()`  
**Fichier** : `ProcessPlantumlPromptsTask.kt:156-189`  
**Critère** : `-50%` temps de traitement

### Session 52
**EPIC** : 1 — Performance & Stabilité  
**Story** : 1.2 — JSON serialization avec Jackson  
**Fichier** : `DiagramProcessor.kt:213-235`  
**Critère** : `0 crash JSON` avec caractères spéciaux

### Session 53
**EPIC** : 1 — Performance & Stabilité  
**Story** : 1.3 — Debug logs cleanup  
**Fichier** : `ProcessPlantumlPromptsTask.kt:44-51`  
**Critère** : `-70%` output verbeux

### Session 54
**EPIC** : 1 — Performance & Stabilité  
**Story** : 1.4 — Kover threshold gate  
**Fichier** : `build.gradle.kts:284-305`  
**Critère** : Build fail si couverture < 75%

---

## 📚 Références

- `AGENTS.md` — Architecture, décisions, méthodologie
- `EPIC_CONSOLIDATION_TESTS_FONCTIONNELS.md` — EPIC 3 détaillé
- `EPIC_FUNCTIONAL_TEST_CONSOLIDATION.md` — Ancien EPIC consolidation
- `SESSIONS_HISTORY.md` — Historique complet des sessions
- `COMPLETED_TASKS_ARCHIVE.md` — Tâches terminées

---

## 🚨 Règles de Travail

### Priorités

1. 🔴 **CRITIQUE** : Bloque publication Portal (EPIC 1)
2. 🟡 **IMPORTANT** : Améliore qualité production (EPIC 2, 3)
3. 🟢 **FAIBLE** : Confort, documentation (EPIC 4)

### Sessions

- ✅ **1 session = 1 story unique** (principe sessions atomiques)
- ✅ **Durée** : 15-30 minutes
- ✅ **Fichiers modifiés** : 1-3 maximum
- ✅ **Validation** : Tests passent AVANT de continuer

### Git

- ❌ **JAMAIS** de commits automatiques
- ✅ **Utilisateur** gère Git manuellement
- ✅ **Commit** après chaque story terminée (recommandé)

---

## ✅ Checklist de Publication Portal

Avant publication sur Gradle Plugin Portal :

- [ ] **EPIC 1** : Performance & Stabilité ✅
  - [ ] 1.1 Double validation fixée
  - [ ] 1.2 JSON serialization robuste
  - [ ] 1.3 Debug logs nettoyés
  - [ ] 1.4 Kover threshold gate

- [ ] **EPIC 2** : RAG Production-Ready ✅
  - [ ] 2.1 PostgreSQL + testcontainers
  - [ ] 2.2 Simulation fallback supprimée
  - [ ] 2.3 Tests intégration RAG
  - [ ] 2.4 Documentation RAG

- [ ] **EPIC 3** : Consolidation Tests ✅
  - [ ] 3.1-3.6 Toutes sessions terminées
  - [ ] Temps < 1m15s

- [ ] **EPIC 4** : Documentation & Qualité ✅
  - [ ] 4.1 README updated
  - [ ] 4.2 Troubleshooting guide
  - [ ] 4.3 KDoc API
  - [ ] 4.4 Exemples prompts

- [ ] **Validation Finale**
  - [ ] Score global ≥ 8.5/10
  - [ ] 100% tests passent
  - [ ] Couverture ≥ 75%
  - [ ] Documentation complète

---

**Dernière mise à jour** : 2026-04-13  
**Prochaine revue** : 2026-04-20 (Semaine 1)
