# 📚 Index — PlantUML Gradle Plugin

> **Objectif** : Vue d'ensemble légère (chargée par défaut)  
> **Détails** : Consulter les fichiers `.agents/` sur besoin

---

## 🎯 Contexte

Plugin Gradle qui génère des diagrammes PlantUML via IA (LangChain4j) à partir de fichiers `.prompt`.

- **Plugin ID** : `com.cheroliv.plantuml`
- **Package** : `plantuml`
- **Stack** : Kotlin, Gradle, LangChain4j, Cucumber BDD

---

## 📁 Organisation des Fichiers

| Dossier | Rôle | Chargement |
|---------|------|------------|
| **Racine** | Fichiers courants | **Toujours** |
| `.agents/` | Documentation détaillée | **Sur besoin** |
| `.sessions/` | Résumés de sessions | **Sur besoin** |
| `.prompts/` | Prompts de reprise archivés | **Sur besoin** |

---

## 📄 Fichiers Courants (Racine)

| Fichier | Rôle | Taille |
|---------|------|--------|
| `INDEX.md` | **Ce fichier** — Index léger | ~80 lignes |
| `PROMPT_REPRISE.md` | Mission session en cours | ~200 lignes |
| `COMPLETED_TASKS_ARCHIVE.md` | Archive tâches terminées | ~1500 lignes |

---

## 📂 Documentation Détaillée (`.agents/`)

### Architecture & Référence
| Fichier | Rôle | Quand charger |
|---------|------|---------------|
| `.agents/ARCHITECTURE.md` | Architecture, décisions, méthodologie | Session début |
| `.agents/REFERENCE.md` | Commandes, providers, pièges | Sur besoin |
| `.agents/PROCEDURES.md` | Procédures de session | Fin de session |

### Roadmap & Historique
| Fichier | Rôle | Quand charger |
|---------|------|---------------|
| `.agents/ROADMAP.md` | Roadmap complète (4 Epics) | Planification |
| `.agents/SESSIONS_HISTORY.md` | Historique sessions (61-70) | Reprise session |

### Guides & Documentation
| Fichier | Rôle | Quand charger |
|---------|------|---------------|
| `.agents/TROUBLESHOOTING.md` | Guide dépannage (EN, 10 questions) | En cas d'erreur |
| `.agents/TROUBLESHOOTING_fr.md` | Guide dépannage (FR, 10 questions) | En cas d'erreur |
| `.agents/CODE_OF_CONDUCT.md` | Code de conduite (EN) | Contribution |
| `.agents/CODE_OF_CONDUCT_fr.md` | Code de conduite (FR) | Contribution |
| `.agents/CONTRIBUTING.md` | Guide contribution (EN) | Contribution |
| `.agents/CONTRIBUTING_fr.md` | Guide contribution (FR) | Contribution |

### Analyse Tests (`.agents/tests/`)
| Fichier | Rôle | Quand charger |
|---------|------|---------------|
| `.agents/tests/OVERLAP_ANALYSIS.md` | Analyse overlaps tests fonctionnels | Optimisation |
| `.agents/tests/COVERAGE_AFTER_CLEANUP.md` | Couverture après consolidation | Optimisation |
| `.agents/tests/METHODOLOGIE_OPTIMISATION.md` | Techniques d'optimisation | Session optimisation |
| `.agents/tests/COVERAGE_ANALYSIS.md` | Couverture tests unitaires | Création tests |
| `.agents/tests/EPIC_CONSOLIDATION.md` | EPIC consolidation tests fonctionnels | Planification |
| `.agents/tests/EPIC_CONSOLIDATION_FR.md` | EPIC consolidation (FR) | Planification |

---

## 🏗 Architecture (Résumé)

```
plantuml-plugin/src/main/kotlin/plantuml/
├── PlantumlPlugin.kt (Plugin + PlantumlExtension nested)
├── models.kt (11 data classes)
├── PlantumlManager.kt (objet Kotlin / singleton)
├── service/ (PlantumlService, DiagramProcessor, LlmService)
└── tasks/ (3 tâches Gradle)
```

**⚠️ Points critiques** :
- `PlantumlExtension` = nested class (PAS de fichier séparé)
- `PlantumlConfig` + 10 classes = `models.kt` (PAS de fichiers individuels)
- `PlantumlManager` = objet Kotlin (singleton, pas une classe)
- `SyntaxValidationResult` = sealed class nested dans `PlantumlService`
- `AttemptEntry` = data class top-level dans `DiagramProcessor.kt`

---

## 📊 État Actuel

**Session 69 TERMINÉE** — Story 4.2 ✅ (Troubleshooting)

**Résultats** :
- ✅ **198 tests unitaires** : 198/198 PASS (100%)
- ✅ **42 tests fonctionnels** : 36 PASS, 6 SKIP, 0 FAIL (100%)
- ✅ **Score Roadmap** : 9.0/10 ✅ **OPTIMAL ATTEINT**

**Prochaine session (70)** : Story 4.3 (Documentation API KDoc)

---

## ⚡ Commandes Utiles

```bash
# Tests fonctionnels (CI)
./gradlew functionalTest                    # ~30s

# Tests fonctionnels rapides (dév quotidien)
./gradlew functionalTest --tests "*quick*"  # ~23s

# Tests unitaires
./gradlew test

# Tous les tests
./gradlew check
```

**Tags de tests** :
- `@Tag("quick")` : 18 tests (< 5s) — dév quotidien
- `@Tag("slow")` : 18 tests (> 10s) — validation complète
- `@Disabled` : 6 tests cloud (requièrent credentials)

---

## 🔗 Liens Rapides

- **Architecture détaillée** : `.agents/ARCHITECTURE.md`
- **Roadmap complète** : `.agents/ROADMAP.md`
- **Historique sessions** : `.agents/SESSIONS_HISTORY.md`
- **Procédures** : `.agents/PROCEDURES.md`
- **Référence commandes** : `.agents/REFERENCE.md`
- **Troubleshooting** : `.agents/TROUBLESHOOTING.md` ou `_fr.md`

---

## 📝 Méthodologie

**Principe** : Sessions atomiques (1 session = 1 tâche unique)

- **Durée** : 15-30 minutes
- **Fichiers modifiés** : 1-3 maximum
- **Tests créés** : 1 fichier
- **Validation** : Après chaque changement

**Processus itératif** :
1. Créer/modifier le fichier
2. `./gradlew test`
3. ✅ Si passe → Session terminée
4. ❌ Si échec → Corriger AVANT de continuer

---

**Voir** : `.agents/` pour documentation détaillée
