# 📊 Analyse des Overlaps — Tests Fonctionnels PlantUML

> **Date** : 2026-04-12  
> **Objectif** : Identifier les tests redondants pour la consolidation (ÉPIC Phase 15)

---

## 📈 Vue d'ensemble (MISE À JOUR — Phase 19 terminée)

| Métrique | Avant | Après | Gain |
|----------|-------|-------|------|
| **Fichiers** | 21 | 9 | **-57%** ✅ |
| **Tests @Test totaux** | 78 | 41 | **-47%** ✅ |
| **Tests @Disabled** | 36 | 3 | **-92%** ✅ |
| **Tests @Ignore** | 17 | 24 | +41% (concentration) |
| **Tests actifs** | 25 | 14 | -44% |

---

## ✅ Phase 19 — Fichiers supprimés (12)

| # | Fichier supprimé | Raison | Consolidé dans |
|---|------------------|--------|----------------|
| 1 | `BaselineFunctionalTest.kt` | Doublon | `PlantumlFunctionalSuite.PluginLifecycle` |
| 2 | `DebuggingFunctionalTest.kt` | Code de debug pur | Supprimé (pas de consolidation) |
| 3 | `FinalOptimizedFunctionalTest.kt` | Doublon | `PlantumlFunctionalSuite.PluginLifecycle` |
| 4 | `MegaOptimizedFunctionalTest.kt` | Doublon | `PlantumlFunctionalSuite.PluginLifecycle` |
| 5 | `OptimizedPlantumlPluginFunctionalTest.kt` | Doublon | `PlantumlFunctionalSuite.PluginLifecycle` |
| 6 | `SuperOptimizedFunctionalTest.kt` | Doublon | `PlantumlFunctionalSuite.PluginLifecycle` |
| 7 | `LlmConfigurationFunctionalTest.kt` | Doublon | `PlantumlFunctionalSuite.LlmProviderConfiguration` |
| 8 | `SharedGradleInstanceFunctionalTest.kt` | Doublon | `PlantumlFunctionalSuite.GradleSharedInstance` |
| 9 | `PlantumlPluginFunctionalTest.kt` | Doublon | `PlantumlFunctionalSuite.PluginLifecycle` |
| 10 | `PlantumlPluginIntegrationTest.kt` | Doublon | `PlantumlPluginIntegrationSuite` |
| 11 | `LlmHandshakeTest.kt` | Doublon | `PlantumlFunctionalSuite.LlmProviderConfiguration` |
| 12 | `LlmCommandLineParameterTest.kt` | Doublon | `PlantumlFunctionalSuite.LlmProviderConfiguration` |

---

## 📋 Tableau détaillé par fichier

| # | Fichier | @Test | @Disabled | @Ignore | Actifs | Statut |
|---|---------|-------|-----------|---------|--------|--------|
| 1 | `BaselineFunctionalTest.kt` | 1 | 0 | 0 | 1 | ✅ Actif |
| 2 | `DebuggingFunctionalTest.kt` | 1 | 0 | 0 | 1 | ✅ Actif |
| 3 | `FilePermissionTest.kt` | 4 | 0 | 0 | 4 | ✅ Actif |
| 4 | `FinalOptimizedFunctionalTest.kt` | 1 | 0 | 0 | 1 | ✅ Actif |
| 5 | `FunctionalTestTemplate.kt` | 0 | 0 | 0 | 0 | 🔧 Utils |
| 6 | `LargeFileAndPathTest.kt` | 4 | 0 | 0 | 4 | ✅ Actif |
| 7 | `LlmCommandLineParameterTest.kt` | 2 | 0 | 0 | 2 | ✅ Actif |
| 8 | `LlmConfigurationFunctionalTest.kt` | 8 | 0 | 0 | 8 | ✅ Actif |
| 9 | `LlmHandshakeTest.kt` | 1 | 0 | 0 | 1 | ✅ Actif |
| 10 | `MegaOptimizedFunctionalTest.kt` | 1 | 0 | 0 | 1 | ✅ Actif |
| 11 | `NetworkTimeoutTest.kt` | 4 | 0 | 0 | 4 | ✅ Actif |
| 12 | `OptimizedPlantumlPluginFunctionalTest.kt` | 1 | 1 | 0 | 0 | ⛔ Disabled |
| 13 | `PerformanceTest.kt` | 4 | 0 | 0 | 4 | ✅ Actif |
| 14 | `PlantumlFunctionalSuite.kt` | 14 | 0 | 1 | 13 | ⚠️ Ignored |
| 15 | `PlantumlPluginFunctionalTest.kt` | 3 | 0 | 0 | 3 | ✅ Actif |
| 16 | `PlantumlPluginIntegrationSuite.kt` | 13 | 0 | 13 | 0 | ⛔ Ignored |
| 17 | `PlantumlPluginIntegrationTest.kt` | 3 | 3 | 0 | 0 | ⛔ Disabled |
| 18 | `PlantumlRealInfrastructureSuite.kt` | 6 | 0 | 6 | 0 | ⛔ Ignored |
| 19 | `SharedGradleInstanceFunctionalTest.kt` | 4 | 0 | 4 | 0 | ⛔ Ignored |
| 20 | `SuperOptimizedFunctionalTest.kt` | 1 | 1 | 0 | 0 | ⛔ Disabled |
| 21 | `ReindexPlantumlRagTaskTest.kt` | 5 | 0 | 5 | 0 | ⛔ Ignored |

---

## 🔍 Overlaps identifiés

### 1. Tests d'application du plugin (7 tests redondants)

| Fichier | Test | Assertions |
|---------|------|------------|
| `BaselineFunctionalTest.kt` | `baseline test - traditional approach` | BUILD SUCCESSFUL + 3 tâches |
| `DebuggingFunctionalTest.kt` | `debug what's in the output` | BUILD SUCCESSFUL |
| `FinalOptimizedFunctionalTest.kt` | `final optimized functional test` | BUILD SUCCESSFUL + 3 tâches |
| `MegaOptimizedFunctionalTest.kt` | `mega optimized single test` | BUILD SUCCESSFUL + 3 tâches |
| `OptimizedPlantumlPluginFunctionalTest.kt` | `plugin loads and registers all tasks` | BUILD SUCCESSFUL + 3 tâches |
| `PlantumlPluginFunctionalTest.kt` | `should apply plugin successfully` | BUILD SUCCESSFUL |
| `SuperOptimizedFunctionalTest.kt` | `super optimized single test for all plugin functionality` | BUILD SUCCESSFUL + 3 tâches |

**✅ Consolidation** : `PlantumlFunctionalSuite.PluginLifecycle` (déjà fait)

---

### 2. Tests d'enregistrement des tâches (5 tests redondants)

| Fichier | Test | Assertions |
|---------|------|------------|
| `BaselineFunctionalTest.kt` | `baseline test - traditional approach` | 3 tâches |
| `FinalOptimizedFunctionalTest.kt` | `final optimized functional test` | 3 tâches |
| `MegaOptimizedFunctionalTest.kt` | `mega optimized single test` | 3 tâches |
| `PlantumlPluginFunctionalTest.kt` | `should register all tasks` | 3 tâches |
| `SuperOptimizedFunctionalTest.kt` | `super optimized single test for all plugin functionality` | 3 tâches |

**✅ Consolidation** : `PlantumlFunctionalSuite.PluginLifecycle.should register all three tasks`

---

### 3. Tests de configuration LLM (9 tests redondants)

| Fichier | Test | Provider |
|---------|------|----------|
| `LlmConfigurationFunctionalTest.kt` | `should handle Ollama configuration correctly` | Ollama |
| `LlmConfigurationFunctionalTest.kt` | `should handle API key provider configuration correctly` | 6 providers |
| `LlmConfigurationFunctionalTest.kt` | `should handle mixed provider configurations correctly` | Mixte |
| `LlmCommandLineParameterTest.kt` | `should use command line LLM parameter to override configuration` | Override CLI |
| `LlmCommandLineParameterTest.kt` | `should perform LLM handshake without full authentication` | Handshake |
| `LlmHandshakeTest.kt` | `should perform handshake with Ollama without full authentication` | Handshake |

**✅ Consolidation** : `PlantumlFunctionalSuite.LlmProviderConfiguration` (déjà fait)

---

### 4. Tests d'instance Gradle partagée (4 tests redondants)

| Fichier | Test | Objectif |
|---------|------|----------|
| `SharedGradleInstanceFunctionalTest.kt` | `test01 plugin applies successfully` | Help task |
| `SharedGradleInstanceFunctionalTest.kt` | `test02 all tasks are registered` | Tasks list |
| `SharedGradleInstanceFunctionalTest.kt` | `test03 extension configuration loads` | Properties |
| `SharedGradleInstanceFunctionalTest.kt` | `test04 llm provider configurations` | Config update |

**✅ Consolidation** : `PlantumlFunctionalSuite.GradleSharedInstance` (déjà fait)

---

### 5. Tests d'intégration (7 tests redondants)

| Fichier | Test | Tâche |
|---------|------|-------|
| `PlantumlPluginIntegrationTest.kt` | `should apply plugin and run processPlantumlPrompts task` | processPlantumlPrompts |
| `PlantumlPluginIntegrationTest.kt` | `should run validatePlantumlSyntax task` | validatePlantumlSyntax |
| `PlantumlPluginIntegrationTest.kt` | `should run reindexPlantumlRag task` | reindexPlantumlRag |
| `PlantumlPluginIntegrationSuite.kt` | `should register all three tasks` | Tasks |
| `PlantumlPluginIntegrationSuite.kt` | `should validate a correct puml file` | Validation |
| `PlantumlPluginIntegrationSuite.kt` | `should succeed with pre-existing rag directory` | RAG |
| `PlantumlPluginIntegrationSuite.kt` | `should complete in test mode without calling real llm` | Prompts |

**✅ Consolidation** : `PlantumlPluginIntegrationSuite` (déjà consolidé en nested classes)

---

## 🎯 Plan de consolidation (ÉPIC Phase 16-20)

### État actuel

**✅ Déjà consolidé** (fichier `PlantumlFunctionalSuite.kt`) :
- `BaselineFunctionalTest` → `PluginLifecycle`
- `DebuggingFunctionalTest` → Supprimé (code de debug)
- `FinalOptimizedFunctionalTest` → `PluginLifecycle`
- `MegaOptimizedFunctionalTest` → `PluginLifecycle`
- `OptimizedPlantumlPluginFunctionalTest` → `PluginLifecycle`
- `SuperOptimizedFunctionalTest` → `PluginLifecycle`
- `LlmConfigurationFunctionalTest` → `LlmProviderConfiguration`
- `SharedGradleInstanceFunctionalTest` → `GradleSharedInstance`

**📋 Reste à consolider** :

| Fichier | Tests | Action recommandée |
|---------|-------|-------------------|
| `PlantumlPluginFunctionalTest.kt` | 3 | → `PluginLifecycle` (doublons) |
| `PlantumlPluginIntegrationTest.kt` | 3 | → `PlantumlPluginIntegrationSuite` (doublons) |
| `PlantumlPluginIntegrationSuite.kt` | 13 | ⚠️ Tous @Ignore — à réactiver ou supprimer |
| `PlantumlRealInfrastructureSuite.kt` | 6 | ⚠️ Tous @Ignore — Tests réels LLM (tag "real-llm") |
| `SharedGradleInstanceFunctionalTest.kt` | 4 | ⚠️ Tous @Ignore — Déjà dans `PlantumlFunctionalSuite` |
| `SuperOptimizedFunctionalTest.kt` | 1 | ⛔ @Disabled — Déjà consolidé |
| `ReindexPlantumlRagTaskTest.kt` | 5 | ⚠️ Tous @Ignore — Tests lourds (tag "rag-heavy") |

---

## 📊 Gains potentiels

### Scénario 1 : Suppression des doublons

| Action | Fichiers | Tests supprimés | Gain estimé |
|--------|----------|-----------------|-------------|
| Supprimer `PlantumlPluginFunctionalTest.kt` | 1 | 3 | -5s |
| Supprimer `SharedGradleInstanceFunctionalTest.kt` | 1 | 4 | -5s |
| Supprimer `SuperOptimizedFunctionalTest.kt` | 1 | 1 | -2s |
| **Total** | **3** | **8** | **~12s** |

### Scénario 2 : Réactivation des tests @Ignore/@Disabled

| Fichier | Tests | Statut | Action |
|---------|-------|--------|--------|
| `PlantumlPluginIntegrationSuite.kt` | 13 | @Ignore | Réactiver ou supprimer |
| `PlantumlRealInfrastructureSuite.kt` | 6 | @Ignore | Garder pour tests réels LLM |
| `ReindexPlantumlRagTaskTest.kt` | 5 | @Ignore | Garder pour tests RAG lourds |

---

## 🏗 Architecture cible après consolidation

```
src/functionalTest/kotlin/plantuml/
├── 📄 ConsolidatedFunctionalTest.kt (NOUVEAU — unique)
│   ├── PluginLifecycle (nested)
│   ├── LlmProviderConfiguration (nested)
│   ├── GradleSharedInstance (nested)
│   ├── SyntaxValidation (nested)
│   ├── RagIndexing (nested)
│   └── PromptProcessing (nested)
│
├── 📁 SpecializedTests/
│   ├── FilePermissionTest.kt (gardé — tests spécifiques)
│   ├── LargeFileAndPathTest.kt (gardé — tests spécifiques)
│   ├── NetworkTimeoutTest.kt (gardé — tests spécifiques)
│   ├── PerformanceTest.kt (gardé — tests de performance)
│   └── ReindexPlantumlRagTaskTest.kt (gardé — tag "rag-heavy")
│
├── 📁 InfrastructureTests/
│   └── PlantumlRealInfrastructureSuite.kt (gardé — tag "real-llm")
│
└── 📁 Utils/
    └── FunctionalTestTemplate.kt (utils)
```

---

## 📈 Métriques cibles

| Métrique | Actuel | Cible | Gain |
|----------|--------|-------|------|
| **Fichiers** | 21 | 10 | **-52%** |
| **Tests totaux** | 78 | 50 | **-36%** |
| **Tests @Disabled/@Ignore** | 53 | 15 | **-72%** |
| **Temps d'exécution** | 2m25s | 1m10s | **-50%** |

---

## ✅ Prochaines étapes (Phases 16-20)

### Phase 16 : Créer `ConsolidatedFunctionalTest.kt`
- [ ] 16.1 : Créer la classe principale avec `companion object` pour GradleRunner partagé
- [ ] 16.2 : Définir 6 nested classes (PluginLifecycle, LlmProviderConfiguration, etc.)
- [ ] 17.1 : Implémenter GradleRunner unique (`lateinit var gradleRunner`)
- [ ] 17.2 : Setup `@BeforeAll` (initialiser une fois, réutiliser)

### Phase 18 : Migration des tests
- [ ] 18.1 : Migration `PlantumlPluginFunctionalTest` → `PluginLifecycle`
- [ ] 18.2 : Migration `PlantumlPluginIntegrationTest` → `SyntaxValidation` + `RagIndexing`
- [ ] 18.3 : Migration tests LLM → `LlmProviderConfiguration`

### Phase 19 : Nettoyage
- [ ] 19.1 : Supprimer fichiers consolidés (7 fichiers)
- [ ] 19.2 : Fusionner tests redondants (8 tests)
- [ ] 19.3 : Consolider assertions (garder 1 assertion forte par scénario)

### Phase 20 : Validation
- [ ] 20.1 : Exécuter suite complète (`./gradlew functionalTest`)
- [ ] 20.2 : Valider couverture (100% des scénarios préservés)

---

## 📝 Notes

1. **`PlantumlFunctionalSuite.kt`** est déjà une consolidation de 9 fichiers (voir commentaire lignes 19-29)
2. **`PlantumlPluginIntegrationSuite.kt`** est déjà structuré en nested classes (4 nested)
3. **`PlantumlRealInfrastructureSuite.kt`** est un cas spécial (tests réels LLM, tag "real-llm")
4. **`ReindexPlantumlRagTaskTest.kt`** est tagué "rag-heavy" (tests lourds avec chargement modèle ML)

---

**🎯 Conclusion** : La consolidation est **déjà partiellement implémentée** via `PlantumlFunctionalSuite.kt`.  
**Reste à faire** : Supprimer les doublons (3 fichiers) et réactiver les tests @Ignore si pertinent.
