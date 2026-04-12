# AGENTS.md — PlantUML Gradle Plugin

> **Langue** : Communication en français, code en anglais.

## 🎯 Contexte

Plugin Gradle qui génère des diagrammes PlantUML via IA (LangChain4j) à partir de fichiers `.prompt`.

- **Plugin ID** : `com.cheroliv.plantuml`
- **Package** : `plantuml`
- **Stack** : Kotlin, Gradle, LangChain4j, Cucumber BDD

---

## ⚠️ Points d'attention (Pièges à éviter)

- **PlantumlExtension** est une **nested class** de `PlantumlPlugin.kt` (PAS un fichier séparé)
- **PlantumlConfig** + 10 data classes sont dans **`models.kt`** (pas de fichiers individuels)
- **PlantumlManager** est un **objet Kotlin** (singleton), pas une classe
- **SyntaxValidationResult** est une **sealed class nested** dans `PlantumlService`
- **AttemptEntry** est une **data class top-level** dans `DiagramProcessor.kt`

---

## 🏗 Architecture

```
plantuml-plugin/src/main/kotlin/plantuml/
├── 📄 PlantumlPlugin.kt
│   ├── 🏛️ PlantumlPlugin : Plugin<Project>
│   └── 📦 PlantumlExtension (nested class)
│
├── 📄 models.kt (11 data classes)
│   ├── PlantumlConfig, InputConfig, OutputConfig, LangchainConfig
│   ├── GitConfig, OllamaConfig, ApiKeyConfig, RagConfig
│   └── PlantumlDiagram, PlantumlCode, ValidationFeedback
│
├── 📄 PlantumlManager.kt (objet Kotlin / singleton)
│   ├── Configuration (nested object) — charge config YAML
│   ├── Tasks (nested object) — registre les 3 tâches
│   └── Extensions (nested object)
│
├── 📁 service/
│   ├── 📄 PlantumlService.kt
│   │   └── SyntaxValidationResult (sealed class: Valid | Invalid)
│   ├── 📄 DiagramProcessor.kt
│   │   └── AttemptEntry (data class top-level)
│   └── 📄 LlmService.kt
│
└── 📁 tasks/ (héritent de DefaultTask)
    ├── ProcessPlantumlPromptsTask.kt
    ├── ValidatePlantumlSyntaxTask.kt
    └── ReindexPlantumlRagTask.kt
```

---

## 📊 État actuel

**Voir** : `SESSIONS_HISTORY.md` pour l'historique complet des sessions
**Voir** : `COMPLETED_TASKS_ARCHIVE.md` pour les tâches terminées

---

## 🛠 Décisions techniques

**Voir** : `AGENT_REFERENCE.md` pour :
- Commandes de référence
- Providers LLM supportés
- Configuration YAML
- Sorties de test

---

## 🚀 Optimisation des tests

**Voir** : `AGENT_REFERENCE.md` pour :
- Types de tests et règles par type
- Pièges connus
- Méthodologie d'optimisation complète
- Exemples de gains réels

---

## 📝 Méthodologie de travail

**Voir** : `AGENT_REFERENCE.md` pour :
- Sessions atomiques
- Processus itératif
- Quand changer de session
- Procédure de fin de session (5 étapes)
- Démarrage de nouvelle session

---

## 📚 Références

| Fichier | Rôle | Chargement |
|---------|------|------------|
| `AGENTS.md` | Architecture, décisions, méthodologie | **Toujours** |
| `AGENT_REFERENCE.md` | Référence rapide (commandes, providers, pièges) | **Sur besoin** |
| `PROMPT_REPRISE.md` | Mission session en cours | **Début session** |
| `SESSIONS_HISTORY.md` | Historique complet sessions | **Sur besoin** |
| `COMPLETED_TASKS_ARCHIVE.md` | Archive tâches terminées | **Fin session** |

---

## 📝 Mise à jour

En fin de session :
1. Déplacer le terminé vers `COMPLETED_TASKS_ARCHIVE.md`
2. Mettre à jour "État actuel"
3. Ne pas modifier "Architecture" et "Décisions techniques" sauf décision explicite
