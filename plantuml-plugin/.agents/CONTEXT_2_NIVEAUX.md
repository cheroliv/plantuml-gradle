# 📚 Contexte Projet — PlantUML Gradle Plugin

> **Objectif** : Précisions contextuelles importantes pour éviter les confusions  
> **Session 106** : Clarification structure projet à 2 niveaux

---

## 🗂️ Structure à 2 Niveaux — DISTINCTION CRITIQUE

### Niveau 1 : Racine `plantuml-gradle/` (CONSOMMATEUR)

| Élément | Rôle | Fichiers |
|---------|------|----------|
| **Dossier** | Projet parent / consommateur du plugin | `/home/cheroliv/workspace/__repositories__/plantuml-gradle/` |
| **Configuration** | Fichiers de configuration DU plugin (usage local) | `plantuml-context.yml`, `postgresql.yml` |
| **Protection** | `.gitignore` à la racine protège les secrets | Ligne 17 : `plantuml-context.yml` |
| **Usage** | Tests manuels, démo, développement | On y utilise le plugin compilé |

**Exemple** :
```yaml
# plantuml-gradle/plantuml-context.yml
langchain4j:
  gemini:
    pool:
      - identity: "compte1@gmail.com"
        apiKey: AIzaSy...
      - identity: "compte2@gmail.com"
        apiKey: AIzaSy...
```

---

### Niveau 2 : `plantuml-gradle/plantuml-plugin/` (CODE SOURCE)

| Élément | Rôle | Fichiers |
|---------|------|----------|
| **Dossier** | Code source DU plugin Gradle | `/.../plantuml-gradle/plantuml-plugin/` |
| **Implementation** | Kotlin, classes, services, tasks | `src/main/kotlin/plantuml/` |
| **Tests** | Tests unitaires + fonctionnels | `src/test/`, `src/functionalTest/` |
| **Build** | Compilation du plugin | `build.gradle.kts`, `settings.gradle.kts` |
| **Publication** | Plugin publié vers Maven/Gradle Portal | `com.cheroliv.plantuml` |

**Exemple** :
```kotlin
// plantuml-plugin/src/main/kotlin/plantuml/LlmService.kt
class LlmService {
    fun createChatModel(config: ApiKeyConfig): ChatModel {
        // Utilise le pool de clés API
    }
}
```

---

## ⚠️ Confusion Éviter (Session 106)

### ❌ ERREUR
Chercher `plantuml-context.yml` dans `plantuml-plugin/`

### ✅ CORRECT
`plantuml-context.yml` est à la **racine** (`plantuml-gradle/`)

| Fichier | Localisation | Rôle |
|---------|--------------|------|
| `plantuml-context.yml` | `plantuml-gradle/` (racine) | Configuration consommateur |
| `PlantumlConfig.kt` | `plantuml-plugin/src/main/kotlin/` | Modèle de configuration (code) |
| `.gitignore` | `plantuml-gradle/` (racine) | Protège `plantuml-context.yml` |

---

## 🔐 Gestion des Secrets — Bonnes Pratiques

### Fichiers Protégés (`.gitignore` ligne 17)

```gitignore
# plantuml-gradle/.gitignore
plantuml-context.yml        # ✅ Protégé
plantuml-test-context.yml   # ✅ Protégé
postgresql.yml              # ✅ Protégé
local.properties            # ✅ Protégé
```

### Structure Recommandée pour Pool de Clés

```yaml
# plantuml-gradle/plantuml-context.yml
langchain4j:
  gemini:
    pool:
      - identity: "compte1@gmail.com"
        apiKey: ${GEMINI_KEY_1}  # Via env var (optionnel)
      - identity: "compte2@gmail.com"
        apiKey: ${GEMINI_KEY_2}
  mistral:
    pool:
      - identity: "pseudo1"
        apiKey: ${MISTRAL_KEY_1}
      - identity: "pseudo2"
        apiKey: ${MISTRAL_KEY_2}
  ollama:
    pool:
      - identity: "local"
        baseUrl: http://localhost:11434
      - identity: "cloud"
        baseUrl: https://api.ollama.com
        apiKey: ${OLLAMA_CLOUD_KEY}
```

---

## 📋 Implications pour le Développement

### Quand modifier `plantuml-plugin/`

- ✅ Ajout de features au plugin
- ✅ Correction de bugs
- ✅ Ajout de tests
- ✅ Refactoring code

**Commandes** :
```bash
cd plantuml-gradle/plantuml-plugin
./gradlew build
./gradlew test
./gradlew functionalTest
```

### Quand modifier `plantuml-gradle/` (racine)

- ✅ Configuration du plugin (usage local)
- ✅ Tests manuels avec vraies clés API
- ✅ Démo, POC

**Commandes** :
```bash
cd plantuml-gradle
./gradlew generatePlantuml  # Utilise le plugin
```

---

## 🔗 Liens Utiles

- **Architecture plugin** : `plantuml-plugin/.agents/ARCHITECTURE.md`
- **Configuration exemple** : `plantuml-gradle/plantuml-context.yml`
- **Protection secrets** : `plantuml-gradle/.gitignore` (ligne 17)

---

**Session 106** — Clarification contexte 2 niveaux ✅
