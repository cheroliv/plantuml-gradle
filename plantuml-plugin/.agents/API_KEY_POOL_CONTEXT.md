# 🗝️ Contexte — Pool de Clés API Rotatives

> **EPIC** : Pool de Clés API Rotatives (Phase 1)  
> **Objectif** : Maximiser quotas freemium multi-comptes via rotation automatique

---

## Concept (1 ligne)

Rotation automatique de clés API par provider pour maximiser l'utilisation des quotas freemium.

---

## Décisions Clés (Session 108)

| # | Décision | Valeur | Rationale |
|---|----------|--------|-----------|
| 1 | **Stockage** | GitHub Secrets (JSON consolidé) | 1 secret = pool complet, Jackson pour parsing |
| 2 | **Rotation** | Avant appel + seuil 80% + échec 429 | Proactif (80%) + réactif (429) |
| 3 | **Poids** | **SUPPRIMÉ** | 1 compte = 1 quota, abstraction inutile |
| 4 | **Fallback** | Oui, avec retry automatique | Continuité de service |
| 5 | **Audit log** | Oui, fichier séparé | Traçabilité des appels |
| 6 | **MCP** | Non (Phase 2) | Hors scope Phase 1 |
| 7 | **Sync quota** | Manuel (si API provider le permet) | Pas de scraping |
| 8 | **1 clé = tous modèles** | Oui | Simplification vs par modèle |

---

## Providers (Phase 1)

| Provider | Quota Freemium | Stratégie Multi-Comptes |
|----------|----------------|------------------------|
| **Google** | 60 req/min | 3 comptes = 180 req/min |
| **HuggingFace** | 30k tokens/mois | 5 comptes = 150k tokens/mois |
| **Groq** | 30 req/min | 3 comptes = 90 req/min |
| **Ollama Local** | Illimité | Prioritaire (pas d'auth) |
| **OpenAI** | $5 credits (3 mois) | Multiples comptes |
| **Anthropic** | Limité (essai) | Multiples comptes |

**Services supportés** : LLM_CHAT, LLM_GENERATION, DATASET, API_REST, EMBEDDING, IMAGE_GENERATION, SECRETS, ACTIONS

---

## Workflow de Rotation

```
1. DEMANDE → ApiKeyPoolManager.selectKey(context)
2. QUOTA → Si ≥ 80% → clé suivante
3. APPEL → client.call(selectedKey)
4. ÉCHEC 429 → markKeyExhausted() + retry fallback
5. AUDIT → auditLogger.log(...)
6. MAJ QUOTA → quotaTracker.update(keyId, delta)
```

---

## Architecture Fichiers

```
src/main/kotlin/fr/plantuml/apikey/
├── models.kt                  # Session 109 (ApiKeyEntry, QuotaConfig, etc.)
├── Provider.kt                # Session 109 (Enum Provider, ServiceType, etc.)
├── ApiKeyPoolManager.kt       # Session 110
├── rotation/
│   ├── RotationEngine.kt      # Session 110
│   └── FallbackStrategy.kt    # Session 110
├── quota/
│   ├── QuotaTracker.kt        # Session 111
│   └── QuotaSyncService.kt    # Session 111
├── audit/
│   └── AuditLogger.kt         # Session 112
└── providers/
    ├── GoogleClient.kt        # Session 113
    └── HuggingFaceClient.kt   # Session 114
```

---

## Format GitHub Secrets

### Secret consolidé : `API_KEY_POOL_CONFIG` (JSON)

```json
{
  "version": "1.0.0",
  "poolName": "PlantUML LLM Pool",
  "rotationStrategy": "WEIGHTED_QUOTA",
  "fallbackEnabled": true,
  "auditEnabled": true,
  "providers": [
    {
      "provider": "GOOGLE",
      "keys": [
        {
          "id": "google-1",
          "email": "compte1@gmail.com",
          "name": "Google Compte 1",
          "keyRef": "GOOGLE_API_KEY_1",
          "services": ["LLM_CHAT", "LLM_GENERATION"],
          "quota": {
            "limitType": "REQUESTS_PER_MINUTE",
            "limitValue": 60,
            "consumedValue": 0,
            "thresholdPercent": 80,
            "resetPolicy": "DAILY"
          }
        }
      ]
    }
  ]
}
```

### Secrets séparés (clés réelles)

```
GOOGLE_API_KEY_1       → AIzaSy...
GOOGLE_API_KEY_2       → AIzaSy...
HF_TOKEN_1             → hf_...
GROQ_API_KEY_1         → gsk_...
OPENAI_API_KEY_1       → sk-...
ANTHROPIC_API_KEY_1    → sk-ant-...
```

---

## Modèle de Données (Résumé)

```kotlin
data class ApiKeyEntry(
    val id: String,
    val email: String,          // Compte propriétaire
    val name: String,           // Nom descriptif
    val keyRef: String,         // Référence GitHub Secret
    val provider: Provider,
    val services: Set<ServiceType>,
    val expirationDate: Instant?,
    val quota: QuotaConfig,
    val metadata: ProviderMetadata
)

data class QuotaConfig(
    val limitType: QuotaType,
    val limitValue: Long,
    val consumedValue: Long,
    val thresholdPercent: Int = 80,
    val periodStart: Instant,
    val periodEnd: Instant,
    val resetPolicy: ResetPolicy,
    val lastManualSync: Instant?
)

data class ApiKeyPoolConfig(
    val version: String = "1.0.0",
    val poolName: String,
    val rotationStrategy: RotationStrategy,
    val fallbackEnabled: Boolean,
    val auditEnabled: Boolean,
    val providers: List<ProviderConfig>
)
```

---

## Roadmap Sessions

| Session | Objectif | Statut |
|---------|----------|--------|
| 108 | Architecture | ✅ 100% |
| **109** | **Data models** | 🟡 **0%** |
| 110 | Rotation engine | ⏳ Pending |
| 111 | Quota tracker | ⏳ Pending |
| 112 | Audit logger | ⏳ Pending |
| 113-114 | Providers | ⏳ Pending |
| 115 | GitHub Secrets | ⏳ Pending |
| 116-117 | Tests | ⏳ Pending |

---

## Critères d'Acceptation EPIC

- [ ] 9 providers supportés
- [ ] Rotation automatique avant chaque appel
- [ ] Seuil quota 80% déclenche changement
- [ ] Échec 429 déclenche fallback + retry
- [ ] Audit logging fichier séparé
- [ ] Configuration GitHub Secrets (JSON)
- [ ] Tests unitaires : 100% PASS
- [ ] Tests fonctionnels : 100% PASS

---

## Références

- **Archive Session 108** : `.agents/sessions/108-api-key-pool-design.md`
- **Prompt Reprise** : `PROMPT_REPRISE.md` (session 109)
- **Règles** : `.agents/INDEX.md`

---

**Dernière mise à jour** : Session 108 (20 avril 2026)
