# 🔄 Prompt de reprise — Session Suivante

> **Prérequis** : `AGENTS.md` est déjà chargé dans le contexte  
> **Règle** : 1 session = 1 tâche unique et validée

---

## ✅ Session précédente — TERMINÉE

**Tâche** : Debug des tests fonctionnels un par un  
**Statut** : ✅ **TERMINÉE**

### Fichiers modifiés
- `LargeFileAndPathTest.kt` : Converti @Ignore classe → 4 tests individuels @Disabled

### Résultats
- ✅ **Tests unitaires** : 129/129 passent (100%)
- ✅ **Tests fonctionnels debuggés** : 17/17 (100%)
- ✅ **Tests PASS** : 9 tests (BaselineFunctionalTest, DebuggingFunctionalTest, FilePermissionTest, LlmHandshakeTest, LlmConfigurationFunctionalTest, LlmCommandLineParameterTest, MegaOptimizedFunctionalTest, NetworkTimeoutTest 1/4, PlantumlPluginFunctionalTest 3/3)
- ⚠️ **Tests SKIP** : 46 tests (annotés @Disabled - conception intentionnelle)
- ✅ **Tests CORRIGÉS** : 2 tests (FinalOptimizedFunctionalTest, LargeFileAndPathTest)

---

## 🎯 TOP PRIORITÉ — Session Suivante

**Mission** : [À définir par l'utilisateur]

**Suggestions** :
- Documentation des providers LLM (comment obtenir chaque clé API, coûts, limites)
- Tests fonctionnels avec vrais providers (avec @Disabled pour credentials réels)
- Autres améliorations du plugin

**Fichiers de référence** :
- `AGENTS.md` — Section "État actuel" mise à jour
- `COMPLETED_TASKS_ARCHIVE.md` — Session 17 documentée
- `TEST_COVERAGE_ANALYSIS.md` — Couverture 100%

---

## 📚 Fichiers de référence

- `AGENTS.md` — Section "État actuel"
- `COMPLETED_TASKS_ARCHIVE.md` — Session 16 documentée
- `TEST_COVERAGE_ANALYSIS.md` — Couverture des tests

---

## 🔄 Fin de session

**Quand la session est terminée** :
1. Vérifier : `./gradlew -p plantuml-plugin test`
2. Mettre à jour `AGENTS.md`
3. Déplacer vers `COMPLETED_TASKS_ARCHIVE.md`
4. **Ouvrir une nouvelle session** pour la tâche suivante

**⚠️ Git — INTERDICTION :**
- ❌ **L'agent N'EST PAS autorisé à exécuter des commandes Git**
- ❌ **Pas de `git add`, `git commit`, `git push`**
- ✅ **C'est l'utilisateur qui gère Git manuellement**

---

**Bonne session ! 🎉**
