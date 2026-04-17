# 🔄 Prompt de reprise — Session 85

> **EPIC** : `EPIC_CONSOLIDATION_TESTS_FONCTIONNELS.md` — **EPIC Tests BDD Cucumber**  
> **Statut** : Session 84 ✅ TERMINÉE — Phase 5 (Consolidation & Qualité) **100% COMPLÉTÉE**  
> **Prochaine mission** : Session 85 — Phase 6 (RAG & LLM Providers)

---

## 📊 Session 84 — Résumé (✅ TERMINÉE)

**Date** : 17 avr. 2026  
**Résultats** : **10/10 scénarios Cucumber passants (100%)** 🎉

### Problèmes résolus

#### 1. Version du plugin codée en dur ❌ → dynamique ✅
**Fichier** : `PlantumlWorld.kt:34-43`  
**Avant** : `private val pluginVersion: String = "0.0.0"`  
**Après** : Lecture depuis `gradle/libs.versions.toml` avec `lazy` + `check()` pour échec explicite

```kotlin
private val pluginVersion: String by lazy {
    val projectRoot = File(System.getProperty("user.dir"))
    val tomlFile = File(projectRoot, "gradle/libs.versions.toml")
    check(tomlFile.exists()) { "Cannot find libs.versions.toml at ${tomlFile.absolutePath}" }
    tomlFile.readLines()
        .firstOrNull { it.startsWith("plantuml =") }
        ?.substringAfter("=")
        ?.trim()
        ?.trim('"')
        ?: throw IllegalStateException("Cannot find plantuml version in libs.versions.toml")
}
```

#### 2. Nettoyage fichiers temporaires ❌ → automatique ✅
**Fichier** : `PlantumlSteps.kt:11-14`  
**Ajout** : Annotation `@After` pour appel systématique à `world.cleanup()` après chaque scénario

```kotlin
@After
fun cleanup() {
    world.cleanup()
}
```

#### 3. Classpath Gradle ❌ → includeBuild() ✅
**Fichier** : `PlantumlWorld.kt:59-80`  
**Problème** : Utilisait `mavenLocal()` + version, nécessitant `publishToMavenLocal`  
**Solution** : `includeBuild()` vers le plugin en développement + suppression de la version

```kotlin
// settings.gradle.kts du projet de test
includeBuild("${pluginRoot.absolutePath}")  // Plugin en développement
// build.gradle.kts
plugins {
    id("com.cheroliv.plantuml")  // Sans version !
}
```

#### 4. Documentation Cucumber dans README
**Fichiers** : `README_truth.adoc` + `README_truth_fr.adoc`  
**Ajout** : Section complète "Cucumber BDD Tests" (+118 lignes chaque)
- Tags `@wip` et `@integration`
- 30+ steps Gherkin listés
- Instructions d'exécution et rapports
- Architecture des tests

### Fichiers modifiés

| Fichier | Action | Impact |
|---------|--------|--------|
| `PlantumlWorld.kt` | ✅ Version dynamique + includeBuild() | Tests ne nécessitent plus publishToMavenLocal |
| `PlantumlSteps.kt` | ✅ Hook @After | Nettoyage automatique après chaque scénario |
| `README_truth.adoc` | ✅ Section Cucumber (+118 lignes) | Documentation complète des tests BDD |
| `README_truth_fr.adoc` | ✅ Section Cucumber (+118 lignes) | Documentation complète en français |
| `.agents/INDEX.md` | ✅ Règle absolue commits git | Interdiction formelle sans permission |
| `.agents/AGENT_SESSION_MANAGER.md` | ✅ Règle absolue commits git | Procédure obligatoire avant commit |

### Résultats Tests

**Avant** : 0/10 (échec classpath — cherchait plugin dans mavenLocal)  
**Après** : 10/10 ✅ (utilise `includeBuild()` vers le plugin en développement)

**Build** : `BUILD SUCCESSFUL in 2m 4s`

---

## 📊 Couverture Tests Cucumber — État Actuel

| Feature File | Scénarios | Statut | Couverture |
|--------------|-----------|--------|------------|
| `1_minimal.feature` | 1 | ✅ PASS | Canary test |
| `2_plantuml_processing.feature` | 3 | ✅ PASS | Core processing |
| `3_syntax_validation.feature` | 3 | ✅ PASS | Syntax validation |
| `4_attempt_history.feature` | 3 | ✅ PASS | Attempt tracking |
| `5_rag_pipeline.feature` | 4 | 🟡 @wip | RAG pipeline |
| `6_llm_providers.feature` | 6 | 🟡 @wip @integration | LLM providers |
| `7_error_handling.feature` | 8 | 🟡 @wip | Error handling |
| `8_configuration.feature` | 6 | 🟡 @wip | Config edge cases |
| `9_incremental_processing.feature` | 5 | 🟡 @wip | Incremental processing |
| `10_file_edge_cases.feature` | 6 | 🟡 @wip | File edge cases |
| `11_diagram_types.feature` | 7 | 🟡 @wip | Diagram types |
| `12_performance.feature` | 5 | 🟡 @wip | Performance |
| `13_integration_e2e.feature` | 4 | 🟡 @wip @integration | E2E integration |

**Total** : 51 scénarios sur 13 feature files  
**Couverture User Journeys** : ~85% ✅

---

## 🎯 Session 85 — Mission

### EPIC Tests BDD Cucumber — Phase 6 — RAG & LLM Providers

**Priorité** : 🔴 **HAUTE**  
**Impact** : Couverture RAG et fournisseurs LLM réels  
**Durée estimée** : 2-3 sessions

#### Tâches recommandées :

1. **Feature 5 — RAG Pipeline** (`5_rag_pipeline.feature`)
   - Implémenter steps pgvector (`@rag`)
   - 4 scénarios : reindex, contexte LLM, incrémental, cleanup
   - Nécessite testcontainers PostgreSQL

2. **Feature 6 — LLM Providers** (`6_llm_providers.feature`)
   - Implémenter steps mocks LLM réels (`@integration`)
   - 6 scénarios : Ollama, OpenAI, Gemini, Mistral, Claude, fallback
   - Nécessite API keys pour tests réels

3. **Feature 7 — Error Handling** (`7_error_handling.feature`)
   - Implémenter steps gestion erreurs (`@error`)
   - 8 scénarios : timeout, rate limit, réseau, Docker, disque, config

**Critères d'acceptation** :
- [ ] Steps RAG implémentés dans `PlantumlSteps.kt`
- [ ] Mocks LLM réels configurés (WireMock ou testcontainers)
- [ ] Tests d'erreurs avec simulations de pannes
- [ ] Tags `@wip` retirés des scénarios implémentés
- [ ] Rapport HTML : 20+ scénarios passants

---

## 📚 Fichiers de référence

| Fichier | Rôle |
|---------|------|
| `src/test/features/5_rag_pipeline.feature` | 4 scénarios RAG |
| `src/test/features/6_llm_providers.feature` | 6 scénarios LLM |
| `src/test/features/7_error_handling.feature` | 8 scénarios erreurs |
| `src/test/scenarios/plantuml/scenarios/PlantumlSteps.kt` | Steps à implémenter |
| `src/test/scenarios/plantuml/scenarios/PlantumlWorld.kt` | État partagé |
| `README_truth.adoc` | Documentation Cucumber |

---

## 📋 Roadmap Sessions 85-96

### Phase 6 : RAG & LLM Providers (Sessions 85-90)

| Session | Feature | Scénarios | Tags | Priorité |
|---------|---------|-----------|------|----------|
| **85** | `5_rag_pipeline.feature` | 4 | `@rag` | 🔴 Haute |
| **86** | `6_llm_providers.feature` | 6 | `@llm @integration` | 🔴 Haute |
| **87** | `7_error_handling.feature` | 8 | `@error` | 🟡 Moyenne |
| 88-90 | Consolidation + fixes | - | - | - |

### Phase 7 : Config & Edge Cases (Sessions 91-93)

| Session | Feature | Scénarios | Tags | Priorité |
|---------|---------|-----------|------|----------|
| **91** | `8_configuration.feature` | 6 | `@config` | 🟡 Moyenne |
| **92** | `9_incremental_processing.feature` | 5 | `@incremental` | 🟡 Moyenne |
| **93** | `10_file_edge_cases.feature` | 6 | `@files` | 🟢 Basse |

### Phase 8 : Diagram Types & Performance (Sessions 94-96)

| Session | Feature | Scénarios | Tags | Priorité |
|---------|---------|-----------|------|----------|
| **94** | `11_diagram_types.feature` | 7 | `@diagrams` | 🟢 Basse |
| **95** | `12_performance.feature` | 5 | `@performance` | 🟢 Basse |
| **96** | `13_integration_e2e.feature` | 4 | `@e2e @integration` | 🟢 Basse |

**Objectif** : Atteindre 85%+ de couverture user journeys (51 scénarios au total)

---

## 🔧 Commandes Utiles

```bash
# Exécuter tous les tests Cucumber (exclut @wip et @integration)
./gradlew cucumberTest

# Exécuter uniquement les tests RAG
./gradlew cucumberTest -Pcucumber.filter.tags="@rag"

# Exécuter les tests d'intégration avec vrais LLM
./gradlew cucumberTest -Pcucumber.filter.tags="@integration"

# Exécuter un feature file spécifique
./gradlew cucumberTest --tests "*5_rag_pipeline*"

# Voir le rapport HTML
open build/reports/cucumber.html
```

---

## ⚠️ Règle Absolue — Commits/GIT

**L'agent NE DOIT JAMAIS** exécuter de commit, push, merge, ou toute commande git modifiant l'historique **SANS permission explicite de l'utilisateur**.

- ✅ **Autorisé** : `git status`, `git diff`, `git log`, `git show` (lecture seule)
- ❌ **Interdit** : `git add`, `git commit`, `git push`, `git merge`, `git rebase` (sauf ordre explicite)

**Procédure obligatoire avant tout commit** :
1. Montrer les modifications (`git diff --stat`)
2. Demander : "Veux-tu que je commit ces changements ?"
3. **Attendre confirmation explicite** ("oui", "commit", "vas-y")
4. **Seulement après** : exécuter le commit

---

**Session 85 — Prêt à démarrer** 🚀
