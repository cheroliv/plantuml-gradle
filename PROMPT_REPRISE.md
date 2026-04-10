# 🔄 Prompt de reprise — Session Suivante

> **Prérequis** : `AGENTS.md` est déjà chargé dans le contexte  
> **Règle** : 1 session = 1 tâche unique et validée

---

## ✅ Session précédente — TERMINÉE

**Tâche** : Correction `langchain` → `langchain4j` dans les tests unitaires  
**Statut** : ✅ **TERMINÉE**

### Fichiers modifiés
- `PlantumlConfigFailureTest.kt` : 2 occurrences corrigées (lignes 69, 93)
- `PlantumlWorld.kt` : 1 occurrence corrigée (ligne 70)
- `PlantumlSteps.kt.backup-modified` : **Supprimé** (fichier backup obsolète)

### Résultat
- ✅ **Tests unitaires** : 129/129 passent (100%)
- ✅ **Cohérence** : 100% du codebase utilise `langchain4j`

---

## 🎯 TOP PRIORITÉ — Session Suivante

**Mission** : Debug des tests fonctionnels un par un

**Stratégie** :
```bash
# Exécuter chaque test fonctionnel individuellement
./gradlew -p plantuml-plugin functionalTest --tests "plantuml.NomDuTest.nom_du_test"
```

**Objectif** : 
1. Identifier le premier test qui échoue ou timeout
2. Documenter l'erreur dans `TEST_COVERAGE_ANALYSIS.md`
3. Corriger ou annoter avec `@Ignore` si nécessaire

**Fichiers à debugger** (21 fichiers, 55 tests @Ignore) :
- Voir : `src/functionalTest/kotlin/plantuml/`
- Priorité : Tests de base (BaselineFunctionalTest, PlantumlPluginFunctionalTest)

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
