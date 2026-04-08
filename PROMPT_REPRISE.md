# 🔄 Prompt de reprise — Session Tests Unitaires

> **Prérequis** : `AGENTS.md` est déjà chargé dans le contexte  
> **Règle** : 1 session = 1 fichier de test créé et validé

---

## 🎯 Mission de CETTE session

**Objectif** : Créer **1 fichier de test** (parmi les 6 manquants)

### Fichiers restants à créer (par ordre de priorité)

| # | Fichier | À tester | Tests | Difficulté |
|---|---------|----------|-------|------------|
| 1 | `ValidatePlantumlSyntaxTaskTest.kt` | `validateSyntax()` | 5 | ⭐ Facile |
| 2 | `ModelsDataClassTest.kt` | 11 data classes | 11 | ⭐ Facile |
| 3 | `ProcessPlantumlPromptsTaskTest.kt` | `processPrompts()`, `processSinglePrompt()` | 5 | ⭐⭐ Moyen |
| 4 | `ReindexPlantumlRagTaskUnitTest.kt` | `reindexRag()`, `simulateIndexing()` | 7 | ⭐⭐ Moyen |
| 5 | `LlmServicePrivateMethodsTest.kt` | 7 méthodes privées | 8 | ⭐⭐⭐ Avancé |
| 6 | `DiagramProcessorPrivateMethodsTest.kt` | 5 méthodes privées | 8 | ⭐⭐⭐ Avancé |

**Recommandation** : Commencer par le **plus simple** (Priorité 1 ou 2)

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
→ Doit afficher : **70/70 tests passent (100%)**

### Étape 2 : Choisir 1 fichier à créer
**Recommandation** : `ValidatePlantumlSyntaxTaskTest.kt` (⭐ Facile, 5 tests)

### Étape 3 : Lire TEST_COVERAGE_ANALYSIS.md
Consulter la section correspondant au fichier choisi

### Étape 4 : Créer le fichier + Tester
```bash
# Après avoir créé le fichier
./gradlew -p plantuml-plugin test
```

### Étape 5 : Valider
- ✅ **Si passe** → Fin de session → Nouvelle session pour le fichier suivant
- ❌ **Si échec** → Corriger → Re-tester

---

## ✅ Critères de succès de CETTE session

- [ ] **1 fichier** créé dans `src/test/kotlin/plantuml/`
- [ ] Tests du fichier passent (`./gradlew -p plantuml-plugin test`)
- [ ] `AGENTS.md` mis à jour (section "État actuel")
- [ ] Tâche terminée → `COMPLETED_TASKS_ARCHIVE.md`

---

## 🔄 Fin de session

**Quand la session est terminée :**
1. Vérifier : `./gradlew -p plantuml-plugin test`
2. Mettre à jour `AGENTS.md`
3. Déplacer vers `COMPLETED_TASKS_ARCHIVE.md`
4. **Ouvrir une nouvelle session** pour le fichier suivant

**⚠️ Git — INTERDICTION :**
- ❌ **L'agent N'EST PAS autorisé à exécuter des commandes Git**
- ❌ **Pas de `git add`, `git commit`, `git push`**
- ✅ **C'est l'utilisateur qui gère Git manuellement**

**⚠️ Périmètre d'action :**
- ❌ **NE PAS toucher** à `src/test/scenarios/`, `src/test/resources/`, `src/test/features/`
- ✅ **Seul dossier autorisé** : `src/test/kotlin/plantuml/`

---

**Bonne session ! 🎉**
