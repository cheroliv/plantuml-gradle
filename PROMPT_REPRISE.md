# 🔄 Prompt de reprise — Session Tests Unitaires

> **Prérequis** : `AGENTS.md` est déjà chargé dans le contexte  
> **Règle** : 1 session = 1 fichier de test créé et validé

---

## 🎯 Mission de CETTE session

**Objectif** : Créer **1 fichier de test** (parmi les 7 manquants)

### Fichier à traiter dans cette session

**Recommandation** : Commencer par le **plus simple** en premier

| Priorité | Fichier | À tester | Tests | Difficulté |
|----------|---------|----------|-------|------------|
| 1 | `PlantumlManagerTest.kt` | `Configuration.load()`, `Tasks.registerTasks()` | 6 | ⭐ Facile |
| 2 | `ValidatePlantumlSyntaxTaskTest.kt` | `validateSyntax()` | 5 | ⭐ Facile |
| 3 | `ModelsDataClassTest.kt` | 11 data classes | 11 | ⭐ Facile |
| 4 | `ProcessPlantumlPromptsTaskTest.kt` | `processPrompts()`, `processSinglePrompt()` | 5 | ⭐⭐ Moyen |
| 5 | `ReindexPlantumlRagTaskUnitTest.kt` | `reindexRag()`, `simulateIndexing()` | 7 | ⭐⭐ Moyen |
| 6 | `LlmServicePrivateMethodsTest.kt` | 7 méthodes privées | 8 | ⭐⭐⭐ Avancé |
| 7 | `DiagramProcessorPrivateMethodsTest.kt` | 5 méthodes privées | 8 | ⭐⭐⭐ Avancé |

**À faire** : Cocher la ligne du fichier en cours de traitement

---

## 📚 Fichiers complémentaires

- `TEST_COVERAGE_ANALYSIS.md` — Exemples de code pour le fichier choisi
- `COMPLETED_TASKS_ARCHIVE.md` — Historique (pour ne pas refaire)

---

## 🚀 Démarrage rapide

### Étape 1 : Vérifier l'état actuel
```bash
./gradlew -p plantuml-plugin test
```
→ Doit afficher : **66/66 tests passent (100%)**

### Étape 2 : Choisir 1 fichier à créer
**Recommandation** : Commencer par `PlantumlManagerTest.kt` (le plus simple)

### Étape 3 : Lire TEST_COVERAGE_ANALYSIS.md
Consulter la section correspondant au fichier choisi

### Étape 4 : Créer le fichier + Tester
```bash
# Après avoir créé le fichier
./gradlew -p plantuml-plugin test
```

### Étape 5 : Valider
- ✅ **Si passe** → Commit → Fin de session → Nouvelle session pour le fichier suivant
- ❌ **Si échec** → Corriger → Re-tester → Puis commit

---

## ✅ Critères de succès de CETTE session

- [ ] **1 fichier** créé dans `src/test/kotlin/plantuml/`
- [ ] Tests du fichier passent (`./gradlew -p plantuml-plugin test`)
- [ ] `AGENTS.md` mis à jour (section "État actuel")
- [ ] Tâche terminée → `COMPLETED_TASKS_ARCHIVE.md`
- [ ] Commit effectué

---

## 🔄 Fin de session

**Quand la session est terminée :**
1. Vérifier : `./gradlew -p plantuml-plugin test`
2. Mettre à jour `AGENTS.md`
3. Déplacer vers `COMPLETED_TASKS_ARCHIVE.md`
4. Commit : `git add -A && git commit -m "Test: [nom du fichier]"`
5. **Ouvrir une nouvelle session** pour le fichier suivant

---

**Bonne session ! 🎉**
