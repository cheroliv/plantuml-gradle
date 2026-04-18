# Session 95 — Feature 8 (fin) + Feature 9 Incremental Processing

**Date** : 18 avril 2026
**Statut** : ✅ TERMINÉE

## Objectifs

| Objectif | Statut | Détails |
|----------|--------|---------|
| Fix Feature 8 @wip scenarios (2) | ✅ | Custom directories + env vars |
| Feature 9 Incremental Processing (5 scénarios) | ✅ | 5/5 scénarios implémentés |
| Retirer tags @wip | ✅ | Feature 8 + Feature 9 |
| Compilation | ✅ | compileKotlin + compileTestKotlin OK |

## Modifications apportées

| Fichier | Modification | Impact |
|--------|--------------|--------|
| `ConfigMerger.kt` | Ajout `loadFromEnvironment()` + support env vars | Priorité : CLI > YAML > env > gradle.properties |
| `ConfigMerger.kt` | Merge functions avec param `env` | 4 niveaux de priorité |
| `ConfigurationSteps.kt` | Fix chemin images `my-generated/images` | Custom directories OK |
| `ConfigurationSteps.kt` | Ajout `runProcessPlantumlPromptsTask()` | Step When pour env vars |
| `ConfigurationSteps.kt` | `environmentVariables` map | Propagation env vars |
| `PlantumlWorld.kt` | Ajout `environmentVariables` | Stockage env vars par scénario |
| `PlantumlWorld.kt` | `executeGradle()` propage env vars | System.setProperty() avant exécution |
| `IncrementalProcessingSteps.kt` | **CRÉÉ** — 280 lignes | 5 scénarios Feature 9 |
| `8_configuration.feature` | Tags @wip retirés | 6/6 scénarios actifs |
| `9_incremental_processing.feature` | Tag @wip retiré | 5/5 scénarios actifs |

## Scénarios Feature 8 corrigés

1. **Use custom input/output directories** — ✅ PASS
   - Correction : `images: "my-generated/images"` au lieu de `"my-generated"`
   - Fichier : `ConfigurationSteps.kt` (ligne 92)

2. **Override config with environment variables** — ✅ PASS
   - Ajout `loadFromEnvironment()` dans `ConfigMerger.kt`
   - Support `System.getenv()` + `System.getProperties()` fallback
   - Variable `PLANTUML_LLM_PROVIDER` lue correctement

## Scénarios Feature 9 implémentés

1. **Skip unchanged prompts on re-run** — Checksum + UP-TO-DATE
2. **Reprocess modified prompts** — Détection modification + reprocess
3. **Cleanup outputs when prompts are deleted** — Suppression orphelins
4. **Use checksum-based change detection** — SHA-256 checksums
5. **Force reprocessing with clean flag** — --rerun-tasks

## Leçons apprises

1. **Env vars vs System properties** — `System.getenv()` pour env vars, `System.getProperties()` pour -D flags
2. **4 niveaux de priorité** — CLI > YAML > Environment > gradle.properties
3. **Byte array literal** — Kotlin requiert `.toByte()` pour valeurs > 127

## Prochaine session

- Feature 10 : File Edge Cases (6 scénarios @wip)
- Feature 11 : Diagram Types (7 scénarios @wip)
- Objectif : 78/78 scénarios passants