# 🔄 Prompt de reprise — Nouvelle Session OpenCode

## 📋 Contexte de la session

**Session précédente** : Correction des tests WireMock + Analyse de couverture  
**Résultat** : 66/66 tests passent (100%) ✅  
**Fichiers clés** : `AGENTS.md` (contexte), `TEST_COVERAGE_ANALYSIS.md` (détails des tests)

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

---

## 📚 Fichiers à lire AVANT de commencer

### 1. En premier (obligatoire)
```bash
cat AGENTS.md
```
→ Contient : Architecture, points d'attention, décisions techniques

### 2. En second (détails)
```bash
cat plantuml-plugin/TEST_COVERAGE_ANALYSIS.md
```
→ Contient : Exemples de code pour chaque test, analyse détaillée

### 3. En troisième (historique)
```bash
cat COMPLETED_TASKS_ARCHIVE.md
```
→ Contient : Ce qui a déjà été fait (pour ne pas refaire)

---

## 🚀 Démarrage rapide

### Étape 1 : Vérifier l'état actuel
```bash
./gradlew -p plantuml-plugin test
```
→ Doit afficher : **66/66 tests passent**

### Étape 2 : Commencer par le test le plus simple
**Recommandation** : `PlantumlManagerTest.kt` ou `ValidatePlantumlSyntaxTaskTest.kt`

**Pourquoi** :
- Ne nécessite pas de mocks complexes
- Logique simple à tester
- Rapide à exécuter (<10ms)

### Étape 3 : Suivre le guide dans TEST_COVERAGE_ANALYSIS.md
Chaque section contient :
- La signature des fonctions à tester
- Des exemples de code Kotlin
- Les assertions attendues

---

## ⚠️ Pièges à éviter (déjà documentés dans AGENTS.md)

- ❌ **PlantumlExtension.kt** n'existe pas → C'est une nested class de `PlantumlPlugin.kt`
- ❌ **PlantumlConfig.kt** n'existe pas → Dans `models.kt` avec 10 autres data classes
- ❌ **PlantumlManager** n'est pas une classe → C'est un objet Kotlin (singleton)
- ❌ **GradleRunner** pour les tests unitaires → Utiliser **ProjectBuilder** (plus rapide)
- ❌ **Endpoint `/api/generate`** pour WireMock → Utiliser **`/api/chat`**

---

## 🛠 Techniques recommandées

### Pour les tâches Gradle
```kotlin
// Utiliser ProjectBuilder avec mocks
val project = ProjectBuilder.builder().build()
val mockService = mock(PlantumlService::class.java)
val task = project.tasks.create("testTask", ProcessPlantumlPromptsTask::class.java)
```

### Pour les méthodes privées
```kotlin
// Option 1 : Reflection (pour tests unitaires)
val method = llmService.javaClass.getDeclaredMethod("createOllamaModel")
method.isAccessible = true

// Option 2 : Extraire dans une classe testable (refactoring)
```

### Pour les data classes
```kotlin
// Tester les valeurs par défaut et copy()
val config = InputConfig()
assertEquals("prompts", config.prompts)
```

---

## ✅ Critères de succès en fin de session

- [ ] 7 fichiers de test créés dans `src/test/kotlin/plantuml/`
- [ ] ~50 tests ajoutés
- [ ] Tous les tests passent (`./gradlew -p plantuml-plugin test`)
- [ ] Couverture >80% (à vérifier avec Jacoco si configuré)
- [ ] `AGENTS.md` mis à jour
- [ ] Tâches terminées déplacées vers `COMPLETED_TASKS_ARCHIVE.md`

---

## 📞 En cas de doute

1. **Architecture** → Lire `AGENTS.md` section "Architecture"
2. **Exemples de tests** → Lire `TEST_COVERAGE_ANALYSIS.md`
3. **Conventions** → Regarder les tests existants (ex: `PlantumlPluginUnitTest.kt`)
4. **WireMock** → Voir `PromptOrchestratorTest.kt` (déjà corrigé)

---

**Bonne session ! 🎉**
