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

**Session 51 TERMINÉE** — Fix du double appel `validateDiagram()`

**Résultats** :
- ✅ **134 tests unitaires** : 134/134 PASS (100%)
- ✅ **42 tests fonctionnels** : 42 PASS, 6 SKIP, 0 FAIL (100%)
- ✅ **Sérialisation** : `maxParallelForks = 1` dans `functionalTest` (évite OOM)
- ✅ **Generated/rag** : Nettoyé (173 fichiers JSON supprimés)
- ✅ **Protection** : `.gitignore` ajoutés dans `diagrams/` et `rag/`
- ✅ **AGENTS.md** : 147 lignes (stable)
- ✅ **Performance** : `-50%` temps de validation LLM (double appel fixé)

**Voir** : `SESSIONS_HISTORY.md` pour l'historique complet des sessions
**Voir** : `COMPLETED_TASKS_ARCHIVE.md` pour les tâches terminées

---

## 🚀 Roadmap — Publication Gradle Plugin Portal

**Score actuel** : 6.8/10 ⚠️ IMPROVING  
**Score cible** : 8.5/10 ✅ PUBLIABLE  
**Timeline** : 5 semaines (2026-04-13 → 2026-05-18)

### 4 Epics Principaux

| EPIC | Objectif | Score | Sessions | Statut |
|------|----------|-------|----------|--------|
| **EPIC 1** : Performance & Stabilité | Fixer problèmes critiques | 6.8→8.0/10 | 5-8 | ⏳ À FAIRE |
| **EPIC 2** : RAG Production-Ready | PostgreSQL + testcontainers | 5→8/10 | 6-10 | ⏳ À FAIRE |
| **EPIC 3** : Consolidation Tests | 1 JVM Gradle, <1m15s | 7→9/10 | 6 | ⏳ Session 43.x |
| **EPIC 4** : Documentation & Qualité | README, KDoc, guides | 4→7/10 | 3-5 | ⏳ À FAIRE |

**Voir** : `ROADMAP.md` pour détails complets

---

## 🎯 Prochaine Session

**Session 52** — EPIC 1.2 (à définir dans ROADMAP.md)  
**Priorité** : Selon ROADMAP.md

**Session 51 TERMINÉE** — Fix du double appel `validateDiagram()`

**Résultats** :
- ✅ **134 tests unitaires** : 134/134 PASS (100%)
- ✅ **42 tests fonctionnels** : 42 PASS, 6 SKIP, 0 FAIL (100%)
- ✅ **Sérialisation** : `maxParallelForks = 1` dans `functionalTest` (évite OOM)
- ✅ **Generated/rag** : Nettoyé (173 fichiers JSON supprimés)
- ✅ **Protection** : `.gitignore` ajoutés dans `diagrams/` et `rag/`
- ✅ **AGENTS.md** : 147 lignes (stable)
- ✅ **Performance** : `-50%` temps de validation LLM (double appel fixé)

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

---

## ⚡ Commandes utiles

```bash
# Lancer les tests fonctionnels
./gradlew -p plantuml-plugin -i functionalTest

# Lancer les tests unitaires
./gradlew -p plantuml-plugin test

# Lancer tous les tests
./gradlew -p plantuml-plugin check
```
