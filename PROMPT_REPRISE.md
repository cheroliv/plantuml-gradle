# 🔄 Prompt de reprise — Session Suivante

> **Prérequis** : `AGENTS.md` est déjà chargé dans le contexte  
> **Règle** : 1 session = 1 tâche unique et validée

---

## ✅ Session précédente — TERMINÉE

**Tâche** : Activation et optimisation LargeFileAndPathTest  
**Statut** : ✅ **TERMINÉE**

### Fichiers modifiés
- `LargeFileAndPathTest.kt` : 4 tests activés (@Ignore retiré), code refactorisé

### Résultats
- ✅ **Tests activés** : 4/4 (large file, special chars, deep paths, unicode)
- ✅ **Tests unitaires** : 129/129 passent (100%)
- ✅ **Code optimisé** : 198 → 137 lignes (-31%)
- ✅ **Temps total** : ~99s → 44s (-55%)
- ✅ **Test optimisé** : deeply nested paths 43s → 18s (-58%)

---

## 🎯 TOP PRIORITÉ — Session Suivante

**Mission** : [À définir par l'utilisateur]

**Suggestions** :
- Documentation des 7 providers LLM (comment obtenir chaque clé API, coûts, limites)
- Tests fonctionnels avec vrais providers (avec @Disabled pour credentials réels)
- Autres optimisations de tests fonctionnels

**Fichiers de référence** :
- `AGENTS.md` — Section "État actuel" mise à jour
- `COMPLETED_TASKS_ARCHIVE.md` — Session 18 documentée
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
