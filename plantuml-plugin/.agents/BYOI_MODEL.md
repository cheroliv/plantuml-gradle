# 🔄 Modèle "Bring Your Own Infrastructure" (BYOI)

**Classification** : 🟡 **LAZY** — Ne charger que si le sujet émerge  
**Dernière mise à jour** : Session 118 (21 avril 2026)  
**Statut** : Document conceptuel (ne pas committer dans les repos publics)

---

## ⚠️ Confidentialité

| Sujet | Visibilité |
|-------|------------|
| **Modèle BYOI** | 🔒 Interne (ce document) |
| **Plugins Gradle** | ✅ Public (Apache 2) |
| **SaaS** | 🔒 Privé (fonctionnalités uniquement) |

---

## 🎯 Concept BYOI

### Définition

**Bring Your Own Infrastructure (BYOI)** : Modèle décentralisé où les utilisateurs apportent leur propre infrastructure (CI, comptes, quotas, clés API) pour exécuter le processing généré par la plateforme.

### Analogie

```
Traditionnel (SaaS cloud) :
┌──────────────┐
│   SaaS       │ → Cloud centralisé (coût élevé)
│   + Cloud    │ → Utilisateurs = consommateurs
└──────────────┘

BYOI (Écosystème Gradle) :
┌──────────────┐
│   SaaS       │ → Plugins Gradle (blueprints)
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Users + CI   │ → Infrastructure décentralisée
│ Users + Comptes │ → Quotas utilisateurs
└──────────────┘
```

---

## 🏗️ Architecture BYOI

### Flux de Processing

```
1. Plateforme SaaS
   ↓ Génère blueprint
2. Plugin Gradle (GitHub repo user)
   ↓ Push code
3. GitHub Actions (CI user)
   ↓ Docker PostgreSQL éphémère
4. YAML → DB (hydratation)
   ↓ R2DBC + Kotlin
5. API Key Pool (rotation auto)
   ↓ LLM providers (Gmail users)
6. Résultats commités
   ↓ Clean (Docker détruit)
```

### Acteurs & Apports

| Acteur | Apport | Coût | Bénéfice |
|--------|--------|------|----------|
| **SaaS** | Plugins + orchestration | ~0 (marginal) | Revenue (abonnement/usage) |
| **User** | CI + comptes + quotas | Quotas (gratuits/payants) | Pas d'infra à gérer |

---

## ⚠️ Risques & Mitigations

| Risque | Niveau | Mitigation |
|--------|--------|------------|
| **GitHub change les règles** | 🟡 Moyen | **Migration GitLab CI prête** (architecture CI-agnostic) |
| **Users perdent leurs clés** | 🟢 Faible | Docs + recovery procedures |
| **Complexité onboarding** | 🟡 Moyen | Scripts d'init, templates prêts à l'emploi |
| **Providers limitent les quotas** | 🟡 Moyen | Rotation + multi-comptes déjà prévu |
| **Fork commercial** | 🟢 Faible | Apache 2 = attribution requise + communauté |
| **Adoption communauté** | 🟠 Élevé | Marketing, docs, examples concrets |

---

## 🔄 Portabilité CI (Stratégie de Migration)

### Architecture CI-Agnostic

```
YAML Config (portable)
    ├── GitHub Actions (.github/workflows/)
    └── GitLab CI (.gitlab-ci.yml)
            ↓
    Docker PostgreSQL (éphémère)
            ↓
    Gradle + Kotlin (identique)
            ↓
    R2DBC + API Key Pool (identique)
```

### Comparatif GitHub vs GitLab

| Critère | GitHub Actions | GitLab CI | Migration |
|---------|----------------|-----------|-----------|
| **Syntaxe** | YAML (workflows) | YAML (.gitlab-ci.yml) | ✅ Traduction auto |
| **Secrets** | GitHub Secrets | GitLab CI Variables | ✅ 1:1 mapping |
| **Docker** | ✅ Supporté | ✅ Supporté | ✅ Identique |
| **PostgreSQL** | ✅ Services | ✅ Services | ✅ Identique |
| **Minutes gratuites** | 2000/mois (free) | 400/mois (free) | ⚠️ Moins généreux |
| **Self-hosted** | ✅ Runners | ✅ Runners | ✅ Identique |

### Plan de Migration (si nécessaire)

```
1. Fork repos vers GitLab
2. .github/workflows/*.yml → .gitlab-ci.yml (script de conversion)
3. GitHub Secrets → GitLab CI Variables (export/import)
4. Users migrent leurs Device Keys (Ollama, etc.)
5. Tests de validation
6. Bascule progressive
```

**Temps estimé** : 1-2 semaines pour migration complète  
**Risque utilisateur** : Faible (docs de migration fournies)

### Pourquoi GitLab en Backup ?

| Raison | Pourquoi |
|--------|----------|
| **Open source** | GitLab = plus aligné avec valeurs OSS |
| **Self-hosted** | Option de hoster soi-même (contrôle total) |
| **CI mature** | GitLab CI = pionnier, très stable |
| **Indépendance** | Moins de risque de changement brutal vs Microsoft |

---

## ✅ Avantages du Modèle BYOI

### Pour le SaaS

| Aspect | Bénéfice |
|--------|----------|
| **Coût** | Cloud déporté chez les users (~0) |
| **Scalabilité** | N users = N infrastructures |
| **Résilience** | Pas de single point of failure |
| **Confidentialité** | Clés chez les users (pas de risque centralisé) |
| **Maintenance** | Users gèrent leurs comptes |

### Pour les Users

| Aspect | Bénéfice |
|--------|----------|
| **Simplicité** | Pas d'infra à déployer |
| **Contrôle** | Clés API chez eux (pas de partage) |
| **Coût** | Utilisent leurs quotas existants |
| **Flexibilité** | Peuvent changer de providers |

---

## 🔐 Sécurité & Confidentialité

### Clés API

| Approche | Risque | Solution BYOI |
|----------|--------|---------------|
| **Centralisé** | 🔴 Élevé (single point of failure) | ❌ Non utilisé |
| **BYOI** | 🟢 Faible (clés chez les users) | ✅ Utilisé |

### Données

| Type | Stockage | Visibilité |
|------|----------|------------|
| **Clés API** | GitHub Secrets (user) | 🔒 User uniquement |
| **Processing** | CI user (éphémère) | 🔒 User uniquement |
| **Résultats** | Repo user | 🔒 User (ou public si choisi) |

---

## 📊 Comparaison : SaaS Traditionnel vs BYOI

| Critère | SaaS Traditionnel | BYOI (Écosystème Gradle) |
|---------|-------------------|--------------------------|
| **Infrastructure** | Cloud centralisé (SaaS) | Décentralisée (users) |
| **Coût cloud** | Élevé (SaaS paie) | ~0 (users paient) |
| **Scalabilité** | Limitée (budget SaaS) | Illimitée (N users) |
| **Clés API** | Centralisées (risque) | Décentralisées (sécurisé) |
| **Maintenance** | SaaS gère tout | Users gèrent leurs comptes |
| **Résilience** | Single point of failure | Distribué (résilient) |
| **Confidentialité** | SaaS voit tout | User garde le contrôle |

---

## 🎯 Cas d'Usage

### Idéal pour BYOI

| Scénario | Pourquoi |
|----------|----------|
| **Processing LLM** | Quotas freemium multi-comptes |
| **CI/CD parallèle** | N workers = N pools indépendants |
| **Génération de contenu** | Users apportent leurs comptes |
| **Communauté open source** | Plugins Apache 2, contribs |

### Moins adapté

| Scénario | Pourquoi |
|----------|----------|
| **Temps réel strict** | CI = asynchrone (minutes) |
| **Données sensibles** | GitHub Actions = externe |
| **Users non-tech** | Nécessite GitHub + CLI |

---

## 🛠️ Implémentation Technique

### Composants Communs

```
Plugins Gradle (Kotlin)
    ├── Langchain4j (abstraction LLM)
    ├── API Key Pool (rotation, quota)
    ├── R2DBC (PostgreSQL éphémère)
    └── GitHub Actions (CI)
```

### YAML → DB (Pattern Commun)

```yaml
# YAML (versionné, Git)
langchain4j:
  apiKeyPool:
    google:
      - email: "user@gmail.com"
        apiKey: "AIzaSy-XXX"
```

```kotlin
// CI (hydratation)
@TaskAction
fun process() {
    val config = yamlLoader.load("plantuml-context.yml")
    val db = postgresContainer.start()
    r2dbcRepository.saveAll(config.providers)
    // Processing avec R2DBC
}
```

---

## 📈 Métriques de Succès

| Métrique | Cible | Mesure |
|----------|-------|--------|
| **Users actifs** | N users/mois | GitHub Insights |
| **Processing déporté** | 100% en CI | GitHub Actions logs |
| **Coût cloud SaaS** | ~0 | Facture cloud |
| **Contributions** | N PRs/mois | GitHub PRs |
| **Plugins installés** | N installations | GitHub Downloads |

---

## 🔗 Références Internes

- **`ECOSYSTEM_OVERVIEW.md`** — Vue d'ensemble écosystème
- **`API_KEY_POOL_CONTEXT.md`** — Pool de clés API rotatives
- **`.agents/sessions/118-ollama-cloud-ci-auth.md`** — Auth Ollama Cloud CI

---

## ⚠️ Notes de Confidentialité

**Ce document** :
- 🟡 **LAZY** : Ne charger que si le sujet émerge
- 🔒 **Interne** : Ne pas committer dans les repos publics
- 📝 **Conceptuel** : Pas de détails d'implémentation SaaS

**Plugins Gradle** :
- ✅ **Public** : Documentation complète autorisée
- ✅ **Apache 2** : Fork, modification, distribution autorisés

---

**Dernière mise à jour** : Session 118 (21 avril 2026)  
**Prochaine révision** : Session 120+ (si modèle évolue)
