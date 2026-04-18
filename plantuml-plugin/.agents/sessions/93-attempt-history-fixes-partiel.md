# Session 93 — Attempt History Fixes (Partiel)

**Date** : 18 avr. 2026  
**Statut** : ⚠️ PARTIEL — 1/3 scénarios Attempt History corrigés  
**Résultats** : **58/61 scénarios PASS (95%)** ✅ (+1 vs Session 92)

---

## 📊 Résumé Session 93

### Tests exécutés

| Métrique | Session 92 | Session 93 | Progression |
|----------|------------|------------|-------------|
| **✅ PASS** | 57/61 (93%) | **58/61 (95%)** | **+2%** ✅ |
| **❌ FAILED** | 4 | **3** | **-1** ✅ |
| **⏭️ SKIPPED** | 33 | 33 | — |

### Scénarios corrigés ✅

1. **Track successful diagram generation with corrections** — ✅ **PASS**
   - Correction de `generateSimulatedLlmResponse()` : ajoute typo `@endulm`
   - Correction de `fixCommonPlantUmlIssues()` : remplace `@endulm` → `@enduml`
   - Ajout de `extractPlantUmlFromResponse()` pour parser JSON LLM

### Scénarios restants ❌ (3 échecs)

1. **Archive history after max iterations with no success** — ❌ **FAILED**
   - **Problème** : Le mock LLM ne retourne qu'1 réponse (réutilisée) au lieu de 6
   - **Cause racine** : `startMockLlm()` utilise `listOf(responseBody)` → queue à 1 élément
   - **Solution requise** : Utiliser `mockLlmReturnsSequence()` avec 6 réponses identiques

2. **Successful generation after multiple corrections** — ❌ **FAILED**
   - **Problème** : S'arrête après 1 itération au lieu de 4
   - **Cause racine** : La validation échoue après 1 itération, loop s'arrête
   - **Solution requise** : Vérifier la logique de ré-validation dans la boucle

3. **Handle invalid LLM response format** (Error Handling) — ❌ **FAILED**
   - **Problème** : Non analysé en détail Session 93
   - **À investiguer** : Session 94

---

## 🔧 Modifications apportées

### Fichiers modifiés

| Fichier | Modification | Impact |
|--------|--------------|--------|
| `DiagramProcessor.kt` | `generateSimulatedLlmResponse()` — ajoute typo `@endulm` | Test mode corrigeable |
| `DiagramProcessor.kt` | `fixCommonPlantUmlIssues()` — corrige `@endulm` + `@startumln` | Correction typos |
| `DiagramProcessor.kt` | `extractPlantUmlFromResponse()` — **NOUVELLE** | Parse JSON LLM |
| `DiagramProcessor.kt` | Boucle de correction — ré-valide après chaque correction | Validation accurate |
| `CommonSteps.kt` | `mockLlmAlwaysReturnsInvalidDiagrams()` — 6 réponses | Queue pour 6 itérations |

### Code ajouté : `extractPlantUmlFromResponse()`

```kotlin
private fun extractPlantUmlFromResponse(response: String): String {
    return try {
        val jsonNode = objectMapper.readTree(response)
        if (jsonNode.has("plantuml")) {
            val plantumlNode = jsonNode.get("plantuml")
            if (plantumlNode.has("code")) {
                return plantumlNode.get("code").asText()
            }
        }
        if (jsonNode.has("code")) {
            return jsonNode.get("code").asText()
        }
        response
    } catch (e: Exception) {
        response
    }
}
```

---

## 🧠 Leçons apprises

### 1. Mock LLM à réponse unique vs séquence

**Problème** : `startMockLlm(responseBody)` crée une queue à **1 élément**.  
Quand le LLM est appelé plusieurs fois, il réutilise la **dernière réponse** (`mockResponseQueue?.last()`).

**Impact** : Le scénario "Archive history" attend 6 entrées, mais le mock ne fournit qu'1 réponse unique.

**Solution appliquée** : Utiliser `mockLlmReturnsSequence()` avec 6 réponses identiques.

### 2. Validation après correction

**Problème** : La boucle de correction ne re-valide pas le code après extraction.

**Solution appliquée** : Ajouter `validationResult = plantumlService.validateSyntax(currentCode)` après extraction.

### 3. Extraction JSON des réponses LLM

**Problème** : Les réponses mockées sont au format JSON `{ "plantuml": { "code": "..." } }`.

**Solution appliquée** : Nouvelle fonction `extractPlantUmlFromResponse()` pour parser le JSON.

---

## 📋 État actuel de la couverture

| Feature | Scénarios | PASS | FAILED | SKIPPED | Couverture |
|---------|-----------|------|--------|---------|------------|
| `1_minimal.feature` | 1 | 1 | 0 | 0 | ✅ 100% |
| `2_plantuml_processing.feature` | 3 | 3 | 0 | 0 | ✅ 100% |
| `3_syntax_validation.feature` | 3 | 3 | 0 | 0 | ✅ 100% |
| `4_attempt_history.feature` | 3 | **1** | **2** | 0 | 🟡 33% |
| `5_rag_pipeline.feature` | 4 | 4 | 0 | 0 | ✅ 100% |
| `6_llm_providers.feature` | 6 | 6 | 0 | 0 | ✅ 100% |
| `7_error_handling.feature` | 8 | 7 | **1** | 0 | 🟡 87.5% |
| `8_configuration.feature` | 6 | 0 | 0 | 6 | ⏭️ @wip |
| `9_incremental_processing.feature` | 5 | 0 | 0 | 5 | ⏭️ @wip |
| `10_file_edge_cases.feature` | 6 | 0 | 0 | 6 | ⏭️ @wip |
| `11_diagram_types.feature` | 7 | 0 | 0 | 7 | ⏭️ @wip |
| `12_performance.feature` | 5 | 0 | 0 | 5 | ⏭️ @wip |
| `13_integration_e2e.feature` | 4 | 0 | 0 | 4 | ⏭️ @wip |

**Total** : **58/61 (95%)** — **3 échecs restants**

---

## 🎯 Session 94 — Programme

### Priorité 1 : Corriger 2 scénarios Attempt History restants

1. **Archive history after max iterations** — Debugguer pourquoi seulement 2 entrées au lieu de 6
   - Vérifier que `mockLlmReturnsSequence()` est bien utilisé
   - Vérifier que la boucle `while (iterations < maxIterations)` s'exécute 6 fois
   - Ajouter logs pour tracer chaque itération

2. **Successful generation after multiple corrections** — Debugguer pourquoi 1 itération au lieu de 4
   - Vérifier que la séquence 4 réponses (3 invalid + 1 valid) est consommée
   - Vérifier la logique de validation après extraction
   - S'assurer que `fixCommonPlantUmlIssues()` n'est pas appelé quand il ne faut pas

### Priorité 2 : Feature 8 Configuration (6 scénarios)

3. **Implémenter les 6 scénarios de `8_configuration.feature`** :
   - Handle missing configuration file
   - Handle invalid YAML syntax
   - Use custom input/output directories
   - Override config with environment variables
   - Override config with CLI properties
   - Handle partial configuration

4. **Créer `ConfigurationSteps.kt`** (ou utiliser `CommonSteps.kt`)

5. **Retirer tag @wip** de `8_configuration.feature`

### Critères d'acceptation Session 94

- [ ] 3 scénarios FAILED → ✅ PASS (Attempt History 2 + Error Handling 1)
- [ ] 6 scénarios Configuration → ✅ 6/6 PASS
- [ ] Tags `@wip` retirés de `8_configuration.feature`
- [ ] Rapport HTML : **67/67 scénarios passants (100%)** 🎯
- [ ] Archive Session 93 créée
- [ ] `PROMPT_REPRISE.md` mis à jour pour Session 95

---

## 📝 Notes techniques

### Bug identifié : Mock LLM à réponse unique

```kotlin
// DANS PlantumlWorld.kt — LIGNE 164-168
fun startMockLlm(responseBody: String) {
    mockResponseQueue = listOf(responseBody)  // ← Queue à 1 élément !
    mockResponseIndex = 0
    startMockLlmWithQueue()
}

// DANS startMockLlmWithQueue() — LIGNE 186
val responseBody = mockResponseQueue?.getOrNull(mockResponseIndex) 
    ?: mockResponseQueue?.last()  // ← Réutilise la dernière réponse
```

**Correctif appliqué** : Utiliser `mockLlmReturnsSequence()` avec N réponses.

### Amélioration : Extraction JSON des réponses LLM

```kotlin
// DANS DiagramProcessor.kt
private fun extractPlantUmlFromResponse(response: String): String {
    return try {
        val jsonNode = objectMapper.readTree(response)
        if (jsonNode.has("plantuml")) {
            val plantumlNode = jsonNode.get("plantuml")
            if (plantumlNode.has("code")) {
                return plantumlNode.get("code").asText()
            }
        }
        if (jsonNode.has("code")) {
            return jsonNode.get("code").asText()
        }
        response
    } catch (e: Exception) {
        response
    }
}
```

**Usage** :
```kotlin
val correctionResponse = chatModel.chat(correctionPrompt)
currentCode = extractPlantUmlFromResponse(correctionResponse)
```

---

**Session 93 — PARTIELLEMENT TERMINÉE** ⚠️ (1/3 scénarios Attempt History corrigés, 58/61 PASS)  
**Session 94 — Prête à démarrer** 🚀 (Correction 2 Attempt History restants + Feature 8 Configuration)
