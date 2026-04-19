# 🗺️ Roadmap — PlantUML Gradle Plugin

> **Dernière mise à jour** : Session 106 (19 avril 2026)  
> **Statut global** : ✅ EPICs principales terminées — 240/240 tests PASS (100%)

---

## ✅ EPICs Terminées

### EPIC 1-4 : Core Features
- ✅ **Feature 1** : Génération diagrammes depuis prompts
- ✅ **Feature 2** : Validation syntaxe PlantUML
- ✅ **Feature 3** : Gestion erreurs détaillée
- ✅ **Feature 4** : Historique des tentatives (archive)
- ✅ **Feature 5** : RAG Pipeline (sessions 97-100)
- ✅ **Feature 6** : Archive history (session 103)
- ✅ **Feature 7** : Error handling (session 102)

**Tests** : 190/190 unitaires + 50/50 fonctionnels = **240/240 PASS (100%)**

---

### EPIC BDD Cucumber
- ✅ **13 features** : 57/57 scénarios PASS
- ✅ **Features 1-11** : COMPLÈTES
- ⚪ **Features 12-13** : @wip (hors scope Cucumber — tests performance)

**Décision** : Features 12-13 déplacées vers tests d'intégration dédiés

---

### EPIC Consolidation Tests (Session 106)
- ✅ Correction 6 tests échoués (5 unit + 1 func)
- ✅ Couverture 100% maintenue
- ✅ Mocks optimisés pour rapidité

---

## ⚪ EPICs en Attente / Futures

### EPIC Pool de Clés API Rotatives (Session 107+)

**Objectif** : Gérer plusieurs clés API par provider avec rotation intelligente

**Priorité** : 🟡 **MOYENNE**

#### Features à Implémenter

| Feature | Description | Statut |
|---------|-------------|--------|
| **Structure YAML** | `pool: [{identity, apiKey, quota}]` par provider | ⚪ À faire |
| **Rotation round-robin** | Basculer entre clés selon quota | ⚪ À faire |
| **Gestion quotas** | Suivi requêtes/minute par clé | ⚪ À faire |
| **Fallback automatique** | Si clé X échoue → clé Y | ⚪ À faire |
| **Providers supportés** | Gemini, Ollama, Mistral, Groq, HuggingFace | ⚪ À faire |

#### Architecture Technique

```kotlin
// 1. models.kt
data class ApiKeyEntry(
    val identity: String,      // email ou pseudo
    val apiKey: String,
    val baseUrl: String? = null,
    val quota: Int = 100       // req/min
)

data class ApiKeyConfig(
    val pool: List<ApiKeyEntry> = emptyList(),
    // ... autres champs
)

// 2. ApiKeyPool.kt (nouveau)
class ApiKeyPool(
    private val entries: List<ApiKeyEntry>
) {
    fun getNextKey(): ApiKeyEntry  // Rotation intelligente
    fun resetCounters()            // Reset quotas périodique
}

// 3. LlmService.kt (modifié)
class LlmService(
    private val pool: ApiKeyPool
) {
    fun createChatModel(): ChatModel {
        val key = pool.getNextKey()
        // ...
    }
}
```

#### Configuration Utilisateur

```yaml
# plantuml-gradle/plantuml-context.yml
langchain4j:
  gemini:
    pool:
      - identity: "compte1@gmail.com"
        apiKey: AIzaSy...
        quota: 100
      - identity: "compte2@gmail.com"
        apiKey: AIzaSy...
        quota: 100
  mistral:
    pool:
      - identity: "pseudo1"
        apiKey: mistral-...
      - identity: "pseudo2"
        apiKey: mistral-...
  ollama:
    pool:
      - identity: "local"
        baseUrl: http://localhost:11434
      - identity: "cloud"
        baseUrl: https://api.ollama.com
        apiKey: ollama-...
```

#### Critères d'Acceptation

- [ ] 1 provider avec 2+ clés → rotation fonctionne
- [ ] Quota dépassé → bascule sur clé suivante
- [ ] Clé invalide → fallback automatique
- [ ] Logs détaillés (quelle clé utilisée, quota restant)
- [ ] Tests unitaires + fonctionnels

---

### EPIC Tests Production-Close (TODO.adoc)

**Objectif** : Tests avec vraies clés API (optionnel, via CLI flag)

**Priorité** : 🟢 **BASSE**

#### Features

| Feature | Description | Statut |
|---------|-------------|--------|
| **Mode réel** | Flag CLI `--test-mode=real` active vraies clés | ⚪ À faire |
| **Secrets locaux** | `~/.gradle/gradle.properties` ou env vars | ⚪ À faire |
| **GitHub Actions** | Secrets CI pour tests automatisés | ⚪ À faire |
| **Fallback mocks** | Si pas de clés → mode mock automatique | ⚪ À faire |

#### Configuration

```bash
# Ligne de commande
./gradlew functionalTest -Dplantuml.test.mode=real

# Ou via properties
./gradlew functionalTest -PuseRealApiKeys=true
```

---

### EPIC Documentation & Release

**Objectif** : Préparer release v0.0.5

**Priorité** : 🟡 **MOYENNE**

#### Tâches

| Tâche | Description | Statut |
|-------|-------------|--------|
| **README.md** | Guide utilisateur complet | ⚪ À faire |
| **CHANGELOG.md** | Historique versions | ⚪ À faire |
| **Release v0.0.5** | Tag Git + publication | ⚪ À faire |
| **GitHub Pages** | Site documentation | ⚪ À faire |

---

## 📊 État des Tests

| Type | Tests | Statut | Couverture |
|------|-------|--------|------------|
| **Unitaires** | 190 | ✅ 100% PASS | Classes, services, tasks |
| **Fonctionnels** | 50 | ✅ 100% PASS | Plugin integration, permissions, network |
| **Cucumber BDD** | 13 features | ✅ 57/57 scénarios | Features 1-11 |
| **Performance** | - | ⚪ @wip | Hors scope Cucumber |
| **Total** | **240** | **✅ 100%** | **Toutes features core** |

---

## 🎯 Prochaines Sessions

### Session 107 : Pool de Clés API — Phase 1
- [ ] Modifier `models.kt` → `ApiKeyEntry` + `pool`
- [ ] Créer `ApiKeyPool.kt` → Rotation round-robin
- [ ] Tests unitaires pour `ApiKeyPool`

### Session 108 : Pool de Clés API — Phase 2
- [ ] Modifier `LlmService.kt` → Utiliser pool
- [ ] Modifier `ConfigLoader.kt` → Parser structure pool
- [ ] Tests fonctionnels avec configuration pool

### Session 109 : Pool de Clés API — Phase 3
- [ ] Gestion quotas + fallback automatique
- [ ] Logs détaillés
- [ ] Documentation utilisateur

### Session 110 : Tests Production-Close
- [ ] Flag CLI `--test-mode=real`
- [ ] Configuration secrets locaux
- [ ] Tests avec vraies clés (optionnel)

### Session 111 : Documentation & Release
- [ ] README.md complet
- [ ] CHANGELOG.md
- [ ] Tag v0.0.5

---

## 🔗 Références

- **TODO.adoc** : `plantuml-plugin/TODO.adoc` (idées originales)
- **Contexte 2 niveaux** : `.agents/CONTEXT_2_NIVEAUX.md`
- **Procédures** : `.agents/PROCEDURES.md`
- **Archive Session 106** : `.agents/sessions/106-correction-tests-unitaires-fonctionnels.md`

---

**Session 106** ✅ — **Session 107** 🚀 (Pool de Clés API)
