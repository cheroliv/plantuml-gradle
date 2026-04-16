# 📋 Plan d'attaque - Epic Tests BDD Cucumber

## 🎯 Objectif
Rendre fonctionnelle la suite complète des tests Cucumber pour couvrir toutes les fonctionnalités du plugin PlantUML.

---

## 📊 État actuel

| Fichier Feature | Statut | Steps associés | Priorité |
|----------------|--------|----------------|----------|
| `1_minimal.feature` | ✅ Fonctionnel | `MinimalSteps.kt` | Maintenance |
| `2_plantuml_processing.feature` | ❌ Commenté | `PlantumlSteps.kt` (commenté) | **P0** |
| `3_syntax_validation.feature` | ❌ Commenté | `PlantumlSteps.kt` (commenté) | **P1** |
| `4_attempt_history.feature` | ❌ Commenté | `AttemptHistorySteps.kt` (obsolète) | **P2** |

---

## 🗓️ Phases du plan

### **Phase 1 : Fondation & Infrastructure** (1-2 sessions)
**Objectif** : Préparer l'infrastructure de test pour supporter tous les scénarios

| Tâche | Description | Critère d'acceptation |
|-------|-------------|----------------------|
| 1.1 | Analyser `PlantumlWorld` pour identifier les helpers manquants | Liste des méthodes à ajouter |
| 1.2 | Ajouter helper `createPromptFile()` dans `PlantumlWorld` | Méthode testée unitairement |
| 1.3 | Ajouter helper `createPlantUmlFile()` dans `PlantumlWorld` | Méthode testée unitairement |
| 1.4 | Ajouter helper `verifyFileExists()` / `verifyFileNotExists()` | Assertions réutilisables |
| 1.5 | Nettoyer le template de projet (supprimer `test-prompts` par défaut) | Template minimaliste |

**Livrable** : `PlantumlWorld` enrichi avec tous les helpers nécessaires

---

### **Phase 2 : Tests de traitement PlantUML** (2-3 sessions)
**Objectif** : Activer `2_plantuml_processing.feature`

| Tâche | Description | Critère d'acceptation |
|-------|-------------|----------------------|
| 2.1 | Décommenter `PlantumlSteps.kt` | Code compilé sans erreur |
| 2.2 | Corriger le format de réponse mock LLM (format Ollama natif) | Mock compatible avec `LlmService` |
| 2.3 | Implémenter step `"a prompt file {string} with content {string}"` | Fichier créé dans `prompts/` |
| 2.4 | Implémenter step `"a mock LLM that returns..."` | Utilise `PlantumlWorld.startMockLlm()` |
| 2.5 | Implémenter step `"I run processPlantumlPrompts task"` | Exécute le task avec mock |
| 2.6 | Implémenter steps de vérification (diagram, PNG, suppression) | Assertions fonctionnelles |
| 2.7 | Décommenter scénario 1 dans `2_plantuml_processing.feature` | Test passant ✅ |
| 2.8 | Décommenter scénario 2 (correction syntaxe) | Test passant ✅ |
| 2.9 | Décommenter scénario 3 (fichiers multiples) | Test passant ✅ |

**Livrable** : 3 scénarios fonctionnels dans `2_plantuml_processing.feature`

---

### **Phase 3 : Tests de validation syntaxe** (1 session)
**Objectif** : Activer `3_syntax_validation.feature`

| Tâche | Description | Critère d'acceptation |
|-------|-------------|----------------------|
| 3.1 | Ajouter step `"a valid PlantUML file {string} with content {string}"` | Crée fichier `.puml` |
| 3.2 | Ajouter step `"an invalid PlantUML file {string} with content {string}"` | Crée fichier invalide |
| 3.3 | Ajouter step `"I run validatePlantumlSyntax task with file {string}"` | Exécute task validation |
| 3.4 | Ajouter steps de vérification (valid/invalid/error details) | Assertions sur output |
| 3.5 | Décommenter les 3 scénarios dans `3_syntax_validation.feature` | Tests passants ✅ |

**Livrable** : 3 scénarios fonctionnels dans `3_syntax_validation.feature`

---

### **Phase 4 : Tests d'historique des tentatives** (2-3 sessions)
**Objectif** : Refondre et activer `4_attempt_history.feature`

| Tâche | Description | Critère d'acceptation |
|-------|-------------|----------------------|
| 4.1 | **Supprimer** l'ancienne implémentation `AttemptHistorySteps.kt` (Mockito) | Code obsolète retiré |
| 4.2 | Créer nouvelle implémentation basée sur `GradleRunner` | Architecture cohérente avec `MinimalSteps` |
| 4.3 | Ajouter helper `verifyAttemptHistory()` dans `PlantumlWorld` | Vérifie JSON d'historique |
| 4.4 | Implémenter steps pour mock LLM multi-réponses (itératif) | Supporte séquence de réponses |
| 4.5 | Corriger les chemins de sortie (`generated/diagrams` vs `generated/rag`) | Chemins cohérents |
| 4.6 | Réécrire scénarios avec steps corrects | Feature file à jour |
| 4.7 | Tester scénario 1 (succès après 2 tentatives) | Test passant ✅ |
| 4.8 | Tester scénario 2 (échec après max iterations) | Test passant ✅ |
| 4.9 | Tester scénario 3 (succès après 4 tentatives) | Test passant ✅ |

**Livrable** : 3 scénarios fonctionnels dans `4_attempt_history.feature`

---

### **Phase 5 : Consolidation & Qualité** (1 session)
**Objectif** : Assurer la qualité et la maintenabilité

| Tâche | Description | Critère d'acceptation |
|-------|-------------|----------------------|
| 5.1 | Exécuter tous les tests Cucumber (`./gradlew cucumberTest`) | 100% des scénarios passent |
| 5.2 | Vérifier le rapport HTML (`build/reports/cucumber.html`) | Rapport propre et lisible |
| 5.3 | Ajouter tags @wip pour tests en développement | Filtrage fonctionnel |
| 5.4 | Documenter les steps dans un README | `src/test/scenarios/README.md` |
| 5.5 | Nettoyer les fichiers temporaires après chaque test | Pas de fuite de fichiers |
| 5.6 | Ajouter test de performance (timeout < 30s par scénario) | Tests rapides |

**Livrable** : Suite de tests complète et documentée

---

## 📈 Métriques de succès

| Métrique | Cible |
|----------|-------|
| Nombre de scénarios fonctionnels | 10 (1 actuel + 9 nouveaux) |
| Couverture des fonctionnalités | 100% (processing, validation, history) |
| Temps d'exécution total | < 5 minutes |
| Taux de réussite | 100% (hors @wip) |

---

## 🔗 Dépendances entre phases

```
Phase 1 (Fondation)
    ↓
Phase 2 (Processing) ────┐
    ↓                    │
Phase 3 (Validation)     │
    ↓                    │
Phase 4 (History) ◄──────┘ (utilise helpers Phase 2)
    ↓
Phase 5 (Consolidation)
```

---

## 🚩 Points de vigilance

1. **Mock LLM** : Bien comprendre le format de réponse Ollama attendu par `LlmService`
2. **Isolation des tests** : Chaque scénario doit avoir son propre dossier projet temporaire
3. **Nettoyage** : S'assurer que `cleanup()` est appelé après chaque scénario
4. **Tags** : Utiliser `@wip` pour les tests en développement, `@integration` pour ceux nécessitant un vrai LLM
5. **Timeout** : Les tests GradleRunner peuvent être lents → ajuster les timeouts si nécessaire

---

## 📅 Estimation

| Phase | Sessions estimées | Complexité |
|-------|-------------------|------------|
| Phase 1 | 1-2 | Moyenne |
| Phase 2 | 2-3 | Élevée |
| Phase 3 | 1 | Faible |
| Phase 4 | 2-3 | Élevée |
| Phase 5 | 1 | Faible |
| **Total** | **7-10 sessions** | - |

---

## 📝 Historique des sessions

### Session 77 — 17 Avril 2026 — Initialisation Plan BDD Cucumber ✅
**Objectif** : Initialisation du plan d'attaque pour les tests Cucumber

**Réalisé** :
- ✅ Analyse de cohérence des fichiers `.feature` commentés
- ✅ Identification des incohérences (mock LLM, chemins de sortie, AttemptHistorySteps obsolète)
- ✅ Création du plan d'attaque en 5 phases dans `AGENT_PLAN.md`
- ✅ Restauration des fichiers méthodologiques supprimés (SESSIONS_HISTORY.md, EPIC_CONSOLIDATION_TESTS_FONCTIONNELS.md, etc.)
- ✅ Mise à jour de `PROMPT_REPRISE.md` pour Session 77
- ✅ Tests Cucumber passants (1/1 scénario)

**Phases complétées** : Aucune (session d'initialisation uniquement)

**Prochaine session** : Session 78 — Démarrage Phase 1 (Fondation & Infrastructure)

---

## 🔄 Procédure de fin de session (Rappel)

**⚠️ IMPORTANT** : Suivre la procédure dans `SESSION_PROCEDURE.md` avant chaque fin de session :

1. ✅ Vérifier les tests (`./gradlew test`)
2. ✅ Mettre à jour cette section avec le résumé de la session
3. ✅ Mettre à jour `SESSIONS_HISTORY.md` avec l'entrée de la session
4. ✅ Mettre à jour `COMPLETED_TASKS_ARCHIVE.md` avec les tâches terminées
5. ✅ Mettre à jour `PROMPT_REPRISE.md` avec la prochaine mission

**⚠️ Git** : L'utilisateur gère Git manuellement (commit, push)

---

## 📚 Fichiers de référence — Quand les utiliser

| Fichier | Rôle | Chargement |
|---------|------|------------|
| `AGENT_PLAN.md` | Plan d'attaque Epic BDD | **Toujours** |
| `SESSIONS_HISTORY.md` | Historique complet des sessions | **Fin de session** |
| `SESSION_PROCEDURE.md` | Procédure de fin de session | **Fin de session** |
| `SESSION_CHECKLIST.md` | Checklist transition session | **Fin de session** |
| `COMPLETED_TASKS_ARCHIVE.md` | Archive tâches terminées | **Fin de session** |
| `PROMPT_REPRISE.md` | Mission session en cours | **Début session** |

---

## 🗂️ Fichiers de référence

| Fichier | Description |
|---------|-------------|
| `src/test/features/1_minimal.feature` | Test canaire fonctionnel |
| `src/test/features/2_plantuml_processing.feature` | Tests de traitement (à activer) |
| `src/test/features/3_syntax_validation.feature` | Tests de validation (à activer) |
| `src/test/features/4_attempt_history.feature` | Tests d'historique (à refondre) |
| `src/test/scenarios/plantuml/scenarios/MinimalSteps.kt` | Steps fonctionnels |
| `src/test/scenarios/plantuml/scenarios/PlantumlSteps.kt` | Steps à décommenter/corriger |
| `src/test/scenarios/plantuml/scenarios/AttemptHistorySteps.kt` | Steps à refondre |
| `src/test/scenarios/plantuml/scenarios/PlantumlWorld.kt` | World à enrichir |
| `src/test/scenarios/plantuml/scenarios/CucumberTestRunner.kt` | Runner de tests |
