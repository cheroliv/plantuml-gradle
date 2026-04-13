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

**Session 47 TERMINÉE** — Consolidation tests fonctionnels

**Résultats** :
- ✅ **134 tests unitaires** : 134/134 PASS (100%)
- ✅ **42 tests fonctionnels** : 40 PASS, 6 SKIP, 0 FAIL
- ✅ **AGENTS.md** : 94 lignes (stable)
- ✅ **Consolidation** : `PlantumlRealInfrastructureSuite.kt` + `ReindexPlantumlRagTaskTest.kt` → `PlantumlFunctionalSuite.kt`
- ✅ **Nettoyage** : `FunctionalTestTemplate.kt` supprimé, dossier `task/` supprimé

**Voir** : `SESSIONS_HISTORY.md` pour l'historique complet des sessions
**Voir** : `COMPLETED_TASKS_ARCHIVE.md` pour les tâches terminées

---

## 🛠 Décisions techniques

**Voir** : `AGENT_REFERENCE.md`

---

## 🚀 Optimisation des tests

**Voir** : `AGENT_REFERENCE.md`

---

## 📝 Méthodologie de travail

**Voir** : `AGENT_REFERENCE.md`

---

## 📚 Références & Procédures

**Voir** : `SESSION_PROCEDURE.md`
