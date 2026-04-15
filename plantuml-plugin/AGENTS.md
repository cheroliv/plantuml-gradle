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

**Session 60 TERMINÉE** — Couverture 77,10% (stable) ✅

**Résultats Session 60** :
- ✅ **198 tests unitaires** : 198/198 PASS (100%)
- ✅ **42 tests fonctionnels** : 42 PASS, 6 SKIP, 0 FAIL (100%)
- ✅ **Couverture Kover** : **77,10%** (stable)
- ✅ **EPIC 1** : 4/6 stories terminées (1.1 ✅, 1.2 ✅, 1.3 ✅, 1.4 ✅, 1.5 ✅, 1.6 ✅)
- ✅ **Score Roadmap** : 7.5/10

**Modifications Session 60** :
- ✅ `ROADMAP.md` : Story 1.4 marquée ✅ TERMINÉ (Session 60)
- ✅ Vérification : `koverThresholdCheck` fonctionne (77,10% > 75%)

**Prochaine session (61)** : EPIC 1 — Story 1.5 (Tester ConfigMerger.getOrDefault())

**Voir** : `SESSIONS_HISTORY.md` pour l'historique complet des sessions
**Voir** : `COMPLETED_TASKS_ARCHIVE.md` pour les tâches terminées
**Voir** : `ROADMAP.md` pour la roadmap complète

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
./gradlew -i functionalTest

# Lancer les tests unitaires
./gradlew test

# Lancer tous les tests
./gradlew check
```
