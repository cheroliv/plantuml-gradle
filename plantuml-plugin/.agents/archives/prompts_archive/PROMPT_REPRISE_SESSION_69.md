# 🔄 Prompt de reprise — Session 69 (ARCHIVÉ)

> **EPIC** : `ROADMAP.md` — EPIC 4 : Documentation & Qualité  
> **Statut** : Session 68 TERMINÉE — Story 4.1 ✅  
> **Mission** : Session 69 — Story 4.2 (Guide Troubleshooting — FAQ 10 questions)

---

## 📊 Session 68 — Résumé (TERMINÉE)

**Résultats** :
- ✅ **README_truth.adoc** : 760 lignes — Documentation complète (EN)
- ✅ **README_truth_fr.adoc** : 760 lignes — Documentation complète (FR)
- ✅ **Story 4.1** : ✅ TERMINÉE (README contributeur PlantUML)
- ✅ **Score Roadmap** : 8.6/10 → 8.8/10 ✅

**Modifications** :
- `README_truth.adoc` : Créé (760 lignes)
- `README_truth_fr.adoc` : Créé (760 lignes)

**Voir** : `SESSIONS_HISTORY.md` pour détails complets  
**Voir** : `.sessions/SESSION_68_SUMMARY.md` pour résumé détaillé

---

## 🎯 Session 69 — Mission

### Story 4.2 — Guide Troubleshooting (FAQ 10 questions)

**Priorité** : 🟢 **FAIBLE**  
**Impact** : Utilisateurs bloqués → autonomes  
**Fichiers cibles** :
- `TROUBLESHOOTING.md` (à créer)
- `TROUBLESHOOTING_fr.md` (à créer)

**Durée estimée** : 1 session

#### Problème
Les utilisateurs rencontrent des erreurs courantes sans guide de résolution.

#### Solution attendue
1. Créer FAQ 10 questions/réponses (erreurs fréquentes)
2. Inclure solutions étape-par-étape
3. Traduire FR/EN

#### Critères d'acceptation
- ✅ **10 questions FAQ** (erreurs LLM, RAG, permissions, network, etc.)
- ✅ **Solutions concrètes** (commands, configs, workarounds)
- ✅ **Bilingue** (EN + FR)

#### Questions FAQ suggérées
1. "Plugin not found" — Comment appliquer le plugin ?
2. "Task not found" — Pourquoi les tâches n'apparaissent pas ?
3. "Connection refused" — LLM ne répond pas (Ollama, etc.)
4. "Timeout" — Requête LLM trop lente
5. "RAG directory not found" — Index RAG manquant
6. "Permission denied" — Fichiers non lisibles
7. "JSON parsing error" — Prompt mal formaté
8. "PlantUML syntax error" — Diagramme invalide
9. "Out of memory" — Gradle manque de mémoire
10. "Configuration not loaded" — YAML/properties ignorés

---

## 📊 État des Tests

### Tests fonctionnels (42 tests)

**Tags** :
- `@Tag("quick")` : 18 tests (< 5s) — dév quotidien
- `@Tag("slow")` : 18 tests (> 10s) — validation complète
- `@Disabled` : 6 tests cloud (requièrent credentials)

| Nested Class | Tests | Statut |
|--------------|-------|--------|
| PluginLifecycle | 6 | ✅ PASS |
| LlmProviderConfiguration | 8 | 2 PASS, 6 SKIP |
| GradleSharedInstance | 4 | ✅ PASS |
| PluginIntegration | 11 | ✅ PASS |
| FilePermission | 4 | ✅ PASS |
| LargeFileAndPath | 4 | ✅ PASS |
| NetworkTimeout | 4 | ✅ PASS |
| Performance | 4 | ✅ PASS |
| **Total** | **45** | **36 PASS, 6 SKIP** |

### Tests unitaires (203 tests)

- ✅ **203/203 PASS** (100%)

---

## 🔧 Commandes de Référence

### Tests fonctionnels
```bash
# Dév quotidien — tests rapides
./gradlew functionalTest --tests "*quick*"     # ~23s

# Validation complète — tous les tests
./gradlew functionalTest                       # ~30s

# Tests lents uniquement (RAG, permissions, network)
./gradlew functionalTest --tests "*slow*"      # ~15s

# Avec configuration cache (encore plus rapide)
./gradlew functionalTest --configuration-cache
```

### Tests unitaires
```bash
./gradlew test
```

### Tous les tests
```bash
./gradlew check
```

### Générer rapport Kover
```bash
./gradlew koverHtmlReport
```

---

## 📚 Fichiers de Référence

| Fichier | Rôle |
|---------|------|
| `ROADMAP.md` | Roadmap complète (4 Epics, 5 semaines) |
| `AGENTS.md` | Architecture, décisions, méthodologie |
| `SESSIONS_HISTORY.md` | Historique complet des sessions |
| `.sessions/` | Résumés de sessions archivés (61-68) |
| `.prompts/` | Prompts de reprise archivés (65-68) |
| `PROMPT_REPRISE.md` | Prompt de reprise **courant** (Session 69) |
| `README_truth.adoc` | README contributeur (EN, 760 lignes) |
| `README_truth_fr.adoc` | README contributeur (FR, 760 lignes) |

---

## 🏗 Architecture — Organisation des Fichiers de Session

Depuis la Session 69, les fichiers sont organisés ainsi :

```
plantuml-plugin/
├── PROMPT_REPRISE.md              # Prompt de reprise COURANT (Session N)
├── SESSIONS_HISTORY.md            # Historique complet (toutes sessions)
├── .prompts/                      # Archives prompts de reprise
│   ├── PROMPT_REPRISE_SESSION_65.md
│   ├── PROMPT_REPRISE_SESSION_66.md
│   └── PROMPT_REPRISE_SESSION_67.md
└── .sessions/                     # Archives résumés de sessions
    ├── SESSION_61_SUMMARY.md
    ├── SESSION_62_SUMMARY.md
    ├── SESSION_63_SUMMARY.md
    ├── SESSION_64_SUMMARY.md
    ├── SESSION_65_SUMMARY.md
    ├── SESSION_66_SUMMARY.md
    ├── SESSION_67_SUMMARY.md
    └── SESSION_68_SUMMARY.md
```

**Règle** :
- **Fichiers courants** : Racine (`PROMPT_REPRISE.md`)
- **Archives** : `.prompts/` et `.sessions/`
- **Historique global** : `SESSIONS_HISTORY.md` (racine)

---

## 🎯 Roadmap — État Actuel

### EPIC 1 : Performance & Stabilité ✅
- **Score** : 6.8/10 → 8.0/10 ✅ **TERMINÉ**
- **Stories** : 6/6 terminées (1.1 ✅, 1.2 ✅, 1.3 ✅, 1.4 ✅, 1.5 ✅, 1.6 ✅)

### EPIC 2 : RAG Production-Ready ✅
- **Score** : 8/10 → 8/10 ✅ **TERMINÉ**
- **Stories** : 4/4 terminées (2.1 ✅, 2.2 ✅, 2.3 ✅, 2.4 ✅)

### EPIC 3 : Consolidation Tests Fonctionnels ✅
- **Score** : 7/10 → 9/10 ✅ **TERMINÉ**
- **Stories** : 6/6 terminées (3.1 ✅ à 3.6 ✅)

### EPIC 4 : Documentation & Qualité 🟡
- **Score** : 4/10 → 6/10 🟡 **EN COURS**
- **Stories** : 1/4 terminées (4.1 ✅, 4.2 ⏳, 4.3 ⏳, 4.4 ⏳)

**Score Global** : **8.8/10** ✅ **PUBLIABLE** → **Cible** : 9.0/10 ✅ **OPTIMAL**

---

## ⚠️ Pièges à Éviter (Rappel)

1. ❌ **Oublier de mettre à jour SESSIONS_HISTORY.md** — Toujours documenter en fin de session
2. ❌ **Mélanger archives et fichiers courants** — Respecter `.prompts/` et `.sessions/`
3. ❌ **Créer des fichiers dans la racine** — Utiliser les dossiers d'archives
4. ❌ **Oublier la traduction FR** — Toujours créer EN + FR pour la documentation

---

## 📝 Procédure de Fin de Session (Rappel)

**Voir** : `SESSION_PROCEDURE.md`

**Étapes** :
1. ✅ Vérifier que tous les tests passent
2. ✅ Mettre à jour `SESSIONS_HISTORY.md`
3. ✅ Mettre à jour `ROADMAP.md` (si story terminée)
4. ✅ Créer `SESSION_N_SUMMARY.md` → `.sessions/`
5. ✅ Créer `PROMPT_REPRISE_SESSION_N.md` → `.prompts/` (si nécessaire)
6. ✅ Mettre à jour `PROMPT_REPRISE.md` pour Session N+1
7. ✅ Commit git (si demandé)

---

**Session 69 PRÊTE** — Objectif : Story 4.2 (Guide Troubleshooting)
