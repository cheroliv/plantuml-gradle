# Session 104 — Correction Archive History Test

**Date** : 19 avril 2026  
**Statut** : ✅ COMPLÈTE — 57/57 PASS (100%)

## Problème

Le test `Archive history after max iterations with no success` échouait avec :
- Attendu : 6 entrées dans `generated/diagrams/attempt-history-*.json`
- Réel : Fichier JSON non trouvé ou avec 1 seule entrée

## Cause Racine

1. **Propriété système mal propagée** : `plugin.project.dir` était passé via `-P` (Gradle property) au lieu de `-D` (JVM system property). Le processus Gradle TestKit ne voyait pas la propriété.

2. **Exception non capturée** : Le test attend un échec de build, mais l'exception `UnexpectedBuildFailure` n'était pas capturée dans le step Cucumber.

3. **Mock LLM trop valide** : Le mock retournait `"actor User"` qui est du PlantUML valide, donc la boucle de correction ne s'exécutait pas.

## Corrections

### 1. PlantUmlProcessingSteps.kt
```kotlin
// Ajout try/catch pour capturer l'échec attendu
try {
    world.executeGradle("processPlantumlPrompts", properties = properties, systemProperties = systemProperties)
} catch (e: Exception) {
    world.exception = e
}

// Passage de plugin.project.dir en systemProperties
val systemProperties = mutableMapOf<String, String>()
world.projectDir?.let {
    systemProperties["plugin.project.dir"] = it.absolutePath
}
```

### 2. CommonSteps.kt
```kotlin
// Mock LLM avec syntaxe vraiment invalide (faute de frappe @endulm)
"code": "@startuml\nactor User\n@endulm"
```

### 3. Fichiers *Steps.kt (5 fichiers)
- `ConfigurationSteps.kt`
- `ErrorHandlingSteps.kt`
- `RagPipelineSteps.kt`
- `IncrementalProcessingSteps.kt`
- `LlmProvidersSteps.kt`

Tous ont été mis à jour pour utiliser `systemProperties` au lieu de `properties` pour `plugin.project.dir`.

## Résultat

✅ Test "Archive history after max iterations with no success" : **PASS**  
✅ Couverture totale : **57/57 (100%)**

## Fichiers Modifiés

- `PROMPT_REPRISE.md`
- `src/main/kotlin/plantuml/service/DiagramProcessor.kt` (logging)
- `src/test/scenarios/plantuml/scenarios/CommonSteps.kt`
- `src/test/scenarios/plantuml/scenarios/ConfigurationSteps.kt`
- `src/test/scenarios/plantuml/scenarios/ErrorHandlingSteps.kt`
- `src/test/scenarios/plantuml/scenarios/IncrementalProcessingSteps.kt`
- `src/test/scenarios/plantuml/scenarios/LlmProvidersSteps.kt`
- `src/test/scenarios/plantuml/scenarios/PlantUmlProcessingSteps.kt`
- `src/test/scenarios/plantuml/scenarios/RagPipelineSteps.kt`
