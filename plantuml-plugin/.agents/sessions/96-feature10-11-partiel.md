# Session 96 — Feature 10 File Edge Cases + Feature 11 Diagram Types (Partiel)

**Date** : 18 avril 2026
**Statut** : ⚠️ INCOMPLÈTE — Conflits de step definitions à résoudre

## Objectifs

| Objectif | Statut | Détails |
|----------|--------|---------|
| Feature 10 File Edge Cases (6 scénarios) | 🟡 | Steps créés mais conflits |
| Feature 11 Diagram Types (7 scénarios) | 🟡 | Steps créés mais conflits |
| Résoudre conflits de step definitions | ❌ | Non terminé |

## Modifications apportées

| Fichier | Modification | Impact |
|--------|--------------|--------|
| `FileEdgeCasesSteps.kt` | **CRÉÉ** — ~180 lignes | 6 scénarios Feature 10 |
| `DiagramTypesSteps.kt` | **CRÉÉ** — ~250 lignes | 7 scénarios Feature 11 |
| `IncrementalProcessingSteps.kt` | MODIFIÉ | Ajout step definition dupliqué (à supprimer) |

## Problèmes identifiés

### Conflits de Step Definitions

Plusieurs steps sont définis dans plusieurs fichiers :

1. **`@Given("a prompt file {string} with content {string}")`**
   - Défini dans : `CommonSteps.kt`, `DiagramTypesSteps.kt`, `IncrementalProcessingSteps.kt`
   - Solution : Garder uniquement dans `CommonSteps.kt`, supprimer des autres

2. **`@When("I run processPlantumlPrompts task")`**
   - Défini dans : `FileEdgeCasesSteps.kt`, `DiagramTypesSteps.kt`, `IncrementalProcessingSteps.kt`, `ConfigurationSteps.kt`
   - Solution : Garder uniquement dans `CommonSteps.kt` ou `PlantUmlProcessingSteps.kt`

3. **`@Given("a prompt file {string} exists")`**
   - Potentiel conflit à vérifier

## Leçons apprises

1. **Cartographier les steps AVANT de coder** — Utiliser `rg` pour lister tous les steps existants
2. **Un step = une seule définition** — Cucumber ne supporte pas les doublons
3. **Centraliser les steps génériques** — `CommonSteps.kt` pour les steps réutilisables

## Prochaine session (97)

### Priorités
1. Lister TOUS les steps existants avec `rg`
2. Identifier et supprimer les doublons dans `FileEdgeCasesSteps.kt` et `DiagramTypesSteps.kt`
3. Valider la compilation : `./gradlew compileTestKotlin`
4. Exécuter les tests Features 10 et 11

### Commandes utiles
```bash
# Lister tous les steps
rg -n '@(Given|When|Then)\("' src/test/scenarios/plantuml/scenarios/

# Compiler les tests
./gradlew compileTestKotlin

# Exécuter Feature 10 et 11
./gradlew cucumberTest --tests "*FileEdgeCases*" --tests "*DiagramTypes*"
```
