# 🎯 ÉPIC : Consolidation des tests fonctionnels

> **Objectif** : 1 seule classe de test fonctionnel avec nested classes + instance Gradle partagée  
> **Gain attendu** : -50% temps d'exécution (2m25s → ~1m10s)  
> **Priorité** : 🔴 MAX

---

## 📊 État des lieux

### Problème identifié

```bash
# Timeout après 2 minutes
./gradlew -i test functionalTest --rerun-tasks
# BUILD FAILED in 2m 25s - Timeout has been exceeded
```

**Causes racines** :
1. **19 classes de tests fonctionnels dispersées** → 19 instances Gradle Runner
2. **Overlaps de couverture** → Mêmes scénarios testés plusieurs fois
3. **`@Ignore` (Kotlin) au lieu de `@Disabled` (JUnit5)** → Incompatibilité
4. **Pas de partage d'instance** → Chaque test recrée son environnement

### Inventaire des classes (19 fichiers)

| # | Classe | Tests @Test | @Disabled | @Ignore | Statut |
|---|--------|-------------|-----------|---------|--------|
| 1 | `BaselineFunctionalTest.kt` | ? | ? | ? | 📋 À auditer |
| 2 | `DebuggingFunctionalTest.kt` | ? | ? | ? | 📋 À auditer |
| 3 | `FilePermissionTest.kt` | 4 | 0 | 0 | ✅ PASS |
| 4 | `FinalOptimizedFunctionalTest.kt` | ? | ? | ? | 📋 À auditer |
| 5 | `LargeFileAndPathTest.kt` | 4 | 0 | 0 | ✅ PASS |
| 6 | `LlmCommandLineParameterTest.kt` | 2 | 0 | 0 | ✅ PASS |
| 7 | `LlmConfigurationFunctionalTest.kt` | 3 | 0 | 0 | ✅ PASS |
| 8 | `LlmHandshakeTest.kt` | 1 | 0 | 0 | ✅ PASS |
| 9 | `MegaOptimizedFunctionalTest.kt` | 1 | 0 | 0 | ✅ PASS |
| 10 | `NetworkTimeoutTest.kt` | 4 | 0 | 0 | ✅ PASS |
| 11 | `OptimizedPlantumlPluginFunctionalTest.kt` | 1 | 1 | 0 | ⚠️ SKIP |
| 12 | `PerformanceTest.kt` | 4 | 0 | 0 | 📋 À tester |
| 13 | `PlantumlFunctionalSuite.kt` | ? | ? | ? | 📋 À auditer |
| 14 | `PlantumlPluginFunctionalTest.kt` | 3 | 0 | 0 | ✅ PASS |
| 15 | `PlantumlPluginIntegrationTest.kt` | 3 | 3 | 0 | ⚠️ SKIP |
| 16 | `PlantumlRealInfrastructureSuite.kt` | ? | ? | ? | 📋 À auditer |
| 17 | `SharedGradleInstanceFunctionalTest.kt` | 4 | 0 | 4 | ❌ @Ignore (incorrect) |
| 18 | `SuperOptimizedFunctionalTest.kt` | 1 | 1 | 0 | ⚠️ SKIP |
| 19 | `ReindexPlantumlRagTaskTest.kt` | ? | ? | ? | 📋 À auditer |

**Totaux** :
- ✅ **15 tests PASS** (déjà debuggés et optimisés)
- ⚠️ **46 tests SKIP** (@Disabled intentionnels)
- ❌ **4 tests @Ignore** (à convertir en @Disabled)
- 📋 **~15 tests à auditer**

---

## 🏗 Architecture cible

### Structure consolidée

```
ConsolidatedFunctionalTest.kt
├── companion object {
│     lateinit var gradleRunner: GradleRunner
│     @BeforeAll fun setup()
│ }
│
├── nested class PluginTests {
│     fun `plugin applies successfully`()
│     fun `tasks are registered`()
│ }
│
├── nested class TaskTests {
│     fun `processPlantumlPrompts runs`()
│     fun `validatePlantumlSyntax runs`()
│     fun `reindexPlantumlRag runs`()
│ }
│
├── nested class LlmTests {
│     fun `uses ollama provider`()
│     fun `uses command line override`()
│     fun `handles network timeout`()
│ }
│
├── nested class PerformanceTests {
│     fun `processes single prompt quickly`()
│     fun `validates syntax quickly`()
│ }
│
└── nested class EdgeCasesTests {
    fun `handles large files`()
    fun `handles unicode paths`()
    fun `handles permission errors`()
}
```

### Avantages

| Avantage | Impact |
|----------|--------|
| **1 instance Gradle** | -60% temps de setup |
| **Pas de duplication** | -20% tests en moins |
| **Code partagé** | -30% lignes de code |
| **Maintenance facilitée** | 1 fichier au lieu de 19 |

---

## 📋 Checklist des tâches

### Phase 15 : Audit (1h30)

- [ ] **15.1** Lister les 19 classes dans `src/functionalTest/kotlin/plantuml/`
- [ ] **15.2** Compter tests @Test, @Disabled, @Ignore par classe
- [ ] **15.3** Identifier overlaps (tests redondants)

**Critères d'acceptation** :
- ✅ Tableau complet avec counts par classe
- ✅ Liste des overlaps identifiés (ex: 5x "plugin applies")
- ✅ Document `TEST_OVERLAP_ANALYSIS.md` créé

---

### Phase 16 : Structure (2h)

- [ ] **16.1** Créer `ConsolidatedFunctionalTest.kt`
- [ ] **16.2** Définir nested classes (PluginTests, TaskTests, LlmTests, etc.)

**Critères d'acceptation** :
- ✅ Fichier créé avec structure vide
- ✅ 5-6 nested classes définies
- ✅ Compilation OK

---

### Phase 17 : GradleRunner partagé (1h30)

- [ ] **17.1** Implémenter `lateinit var gradleRunner: GradleRunner`
- [ ] **17.2** Setup `@BeforeAll` pour initialiser une fois

**Critères d'acceptation** :
- ✅ Companion object avec GradleRunner
- ✅ Initialisation dans `@BeforeAll`
- ✅ Tests passent individuellement

---

### Phase 18 : Migration (3h)

- [ ] **18.1** Migration PluginTests (tests d'application du plugin)
- [ ] **18.2** Migration TaskTests (tests des 3 tâches)
- [ ] **18.3** Migration LlmTests (tests de configuration LLM)
- [ ] **18.4** Migration PerformanceTests
- [ ] **18.5** Migration EdgeCasesTests

**Critères d'acceptation** :
- ✅ Tous les tests migrés
- ✅ 100% des scénarios préservés
- ✅ Tests passent (ou @Disabled intentionnel)

---

### Phase 19 : Nettoyage overlaps (1h30)

- [ ] **19.1** Fusionner tests redondants
- [ ] **19.2** Consolider assertions (1 assertion forte par scénario)

**Critères d'acceptation** :
- ✅ Doublons supprimés
- ✅ Assertions consolidées
- ✅ Code réduit de ~30%

---

### Phase 20 : Validation (1h)

- [ ] **20.1** Exécuter suite complète : `./gradlew functionalTest`
- [ ] **20.2** Valider couverture 100%
- [ ] **20.3** Mesurer temps d'exécution (cible : <1m10s)

**Critères d'acceptation** :
- ✅ Tous les tests passent (ou @Disabled)
- ✅ Temps réduit de 50% (2m25s → ~1m10s)
- ✅ Anciennes classes supprimées

---

## 📊 Suivi de progression

### Métriques

| Métrique | Avant | Cible | Après |
|----------|-------|-------|-------|
| **Classes de tests** | 19 | 1 | 📋 - |
| **Lignes de code** | ~2000 | ~1400 | 📋 - |
| **Temps d'exécution** | 2m25s | 1m10s | 📋 - |
| **Tests @Disabled** | 46 | 46 | 📋 - |
| **Overlaps** | ~10 | 0 | 📋 - |

### Sessions

| Session | Date | Tâches | Résultat |
|---------|------|--------|----------|
| Session 35 | 📋 À venir | 15.1, 15.2, 15.3 | 📋 En attente |
| Session 36 | 📋 À venir | 16.1, 16.2 | 📋 En attente |
| Session 37 | 📋 À venir | 17.1, 17.2 | 📋 En attente |
| Session 38 | 📋 À venir | 18.1, 18.2, 18.3 | 📋 En attente |
| Session 39 | 📋 À venir | 18.4, 18.5 | 📋 En attente |
| Session 40 | 📋 À venir | 19.1, 19.2, 20.1, 20.2, 20.3 | 📋 En attente |

---

## ⚠️ Pièges à éviter

| Piège | Conséquence | Solution |
|-------|-------------|----------|
| **Oublier @BeforeAll** | Chaque test recrée GradleRunner | Vérifier companion object |
| **Migration trop agressive** | Perte de scénarios de test | Valider 100% couverture |
| **Garder anciennes classes** | Confusion, maintenance double | Supprimer après validation |
| **@Ignore au lieu de @Disabled** | Erreur JUnit5 | Toujours utiliser @Disabled |
| **Pas de mesure temps** | Optimisation non-quantifiée | `time ./gradlew` avant/après |

---

## 🔗 Références

- **Fichier cible** : `src/functionalTest/kotlin/plantuml/ConsolidatedFunctionalTest.kt`
- **Anciennes classes** : `src/functionalTest/kotlin/plantuml/*.kt` (à supprimer)
- **Documentation** : `METHODOLOGIE_OPTIMISATION_TESTS.md`
- **Backlog** : `AGENTS.md` (section "TOP PRIORITÉ — Refactorisation")

---

## 📝 Notes

- **Session 34** : LargeFileAndPathTest debuggé (4/4 PASS, 1m10s)
- **Session 35** : Démarrage épic de consolidation
- **Timeout actuel** : 2m25s (2026-04-12)
- **Cible** : 1m10s (-50%)
