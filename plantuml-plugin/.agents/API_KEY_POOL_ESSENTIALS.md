# 🗝️ Essentials — Pool de Clés API Rotatives

> **Chargement** : EAGER (automatique à chaque session)  
> **Taille** : ≤ 50 lignes  
> **Contenu** : Uniquement ce qui est critique pour la session en cours

---

## Concept (1 ligne)

Rotation automatique de clés API par provider pour maximiser les quotas freemium multi-comptes.

---

## Décisions Clés (Session 108)

| # | Décision | Impact Session 109 |
|---|----------|-------------------|
| 1 | **Stockage** : GitHub Secrets (JSON consolidé) | `keyRef` dans les modèles |
| 2 | **Rotation** : Avant appel + seuil 80% + échec 429 | Pas d'impact (Session 110) |
| 3 | **Poids** : SUPPRIMÉ | **NE PAS ajouter `weight`** |
| 4 | **1 clé = tous modèles** | Pas de `modelName` dans ApiKeyEntry |

---

## Session 109 — Mission

**Objectif** : Créer `models.kt` + `Provider.kt`

**Fichiers** :
```
src/main/kotlin/fr/plantuml/apikey/
├── models.kt          # ApiKeyEntry, QuotaConfig, ApiKeyPoolConfig
└── Provider.kt        # Provider, ServiceType, RotationStrategy, etc.
```

**Data classes** :
```kotlin
ApiKeyEntry(id, email, name, keyRef, provider, services, expirationDate, quota, metadata)
QuotaConfig(limitType, limitValue, consumedValue, thresholdPercent, periodStart, periodEnd, resetPolicy, lastManualSync)
ApiKeyPoolConfig(version, poolName, rotationStrategy, fallbackEnabled, auditEnabled, providers)
```

**Enums** : Provider (9), ServiceType (10), RotationStrategy (4), QuotaType (8), ResetPolicy (5)

---

## Providers (noms seulement)

GOOGLE, HUGGINGFACE, GROQ, OLLAMA, MISTRAL, GROK, OPENAI, ANTHROPIC, GITHUB

**Détails complets** : `.agents/API_KEY_POOL_REFERENCE.md` (LAZY)

---

## Règles Session 109

- ✅ 1 fichier à la fois (models.kt → Provider.kt)
- ✅ Compiler après chaque fichier (`./gradlew compileKotlin`)
- ❌ PAS de tests (Session 116)
- ❌ PAS de commit sans permission

---

**Session 109** 🚀 — **Archive** : `.agents/sessions/108-api-key-pool-design.md`
