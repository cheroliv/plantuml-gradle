# 🔄 Prompt de reprise — Session Tests Unitaires

> **Prérequis** : `AGENTS.md` est déjà chargé dans le contexte

---

## 🎯 Mission de la session

**Objectif** : Créer les **7 fichiers de tests unitaires manquants** pour atteindre >80% de couverture

### Les 7 fichiers à créer (par ordre de priorité)

| # | Fichier | À tester | Tests | Difficulté |
|---|---------|----------|-------|------------|
| 1 | `PlantumlManagerTest.kt` | `Configuration.load()`, `Tasks.registerTasks()` | 6 | ⭐ Facile |
| 2 | `ValidatePlantumlSyntaxTaskTest.kt` | `validateSyntax()` | 5 | ⭐ Facile |
| 3 | `ModelsDataClassTest.kt` | 11 data classes | 11 | ⭐ Facile |
| 4 | `ProcessPlantumlPromptsTaskTest.kt` | `processPrompts()`, `processSinglePrompt()` | 5 | ⭐⭐ Moyen |
| 5 | `ReindexPlantumlRagTaskUnitTest.kt` | `reindexRag()`, `simulateIndexing()` | 7 | ⭐⭐ Moyen |
| 6 | `LlmServicePrivateMethodsTest.kt` | 7 méthodes privées | 8 | ⭐⭐⭐ Avancé |
| 7 | `DiagramProcessorPrivateMethodsTest.kt` | 5 méthodes privées | 8 | ⭐⭐⭐ Avancé |

**Total** : ~50 tests à créer  
**Contraintes** : Tests <10ms, utiliser ProjectBuilder (pas GradleRunner)

---

## 📚 Fichiers complémentaires

- `TEST_COVERAGE_ANALYSIS.md` — Exemples de code pour chaque test
- `COMPLETED_TASKS_ARCHIVE.md` — Historique (pour ne pas refaire)

---

## 🚀 Démarrage rapide

### Étape 1 : Vérifier l'état actuel
```bash
./gradlew -p plantuml-plugin test
```
→ Doit afficher : **66/66 tests passent (100%)**

### Étape 2 : Commencer par le plus simple
**Recommandation** : `PlantumlManagerTest.kt` ou `ValidatePlantumlSyntaxTaskTest.kt`

### Étape 3 : Suivre TEST_COVERAGE_ANALYSIS.md
Chaque section contient :
- Signatures des fonctions à tester
- Exemples de code Kotlin
- Assertions attendues

---

## ✅ Critères de succès

- [ ] 7 fichiers créés dans `src/test/kotlin/plantuml/`
- [ ] ~50 tests ajoutés
- [ ] Tous les tests passent
- [ ] `AGENTS.md` mis à jour (section "État actuel")
- [ ] Tâches terminées → `COMPLETED_TASKS_ARCHIVE.md`

---

**Bonne session ! 🎉**
