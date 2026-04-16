# 📦 Gestionnaire de Sessions

**Version** : 1.0  
**Dernière mise à jour** : 2026-04-16

---

## 🎯 Objectif

Éviter la saturation du contexte en maintenant les fichiers de session légers et archivés.

---

## 📋 Règles de Gestion

### 1. Fichiers Actifs (toujours chargés)

| Fichier | Rôle | Taille max |
|---------|------|------------|
| `PROMPT_REPRISE.md` | Contexte session actuelle | 200 lignes |
| `INDEX.md` | Vue d'ensemble projet | 300 lignes |

### 2. Archivage en Fin de Session

**Déclencheur** : Utilisateur dit "session terminée", "à plus tard", ou similaire

**Procédure** (5 étapes) :

```
1. ✅ Vérifier critères de fin (tests pass, tâches complètes)
2. 📝 Générer résumé session (voir template)
3. 📦 Archiver dans `.agents/sessions/{num}-{type}-{sujet}.md`
4. 📊 Mettre à jour `INDEX.md` (ligne dans tableau)
5. 🧹 Nettoyer `PROMPT_REPRISE.md` (garder squelette)
```

### 3. Classification par Type

| Type | Mots-clés détectés | Exemple de nom |
|------|-------------------|----------------|
| `debug` | "corriger", "bug", "crash", "échec", "fix" | `74-debug-couverture-tests.md` |
| `feature` | "ajouter", "nouvelle", "feature", "implémenter" | `75-feature-kdoc-generator.md` |
| `refactor` | "refactor", "nettoyer", "optimiser", "améliorer" | `73-refactor-thread-safety.md` |
| `test` | "test unitaire", "test fonctionnel", "couverture" | `72-test-migration-mockito.md` |
| `docs` | "documentation", "README", "guide", "traduction" | `71-docs-traduction-fr.md` |

---

## 📝 Template de Session (à générer)

```markdown
# Session {NUM} — {DATE} : {TITRE}

### 🎯 Objectif
{Description courte de l'objectif}

### ✅ Résultats
- {Résultat 1}
- {Résultat 2}
- {Résultat 3}

### 📊 Modifications
| Fichier | Action | Impact |
|---------|--------|--------|
| `{fichier}` | ✅ {action} | {impact} |

### 🔧 Correctifs appliqués
1. ✅ {correctif 1}
2. ✅ {correctif 2}

### 📋 Leçons apprises
- {leçon 1}
- {leçon 2}

### 🎯 Prochaine Session ({NUM+1})
- **Objectif** : {objectif suivant}
- **Score Roadmap** : {score}/10
```

---

## 📊 Tableau INDEX (à maintenir)

Ajouter une ligne à chaque fin de session :

```markdown
| # | Date | Type | Sujet | Fichier | Score |
|---|------|------|-------|---------|-------|
| 74 | 2026-04-16 | debug | Couverture tests | `74-debug-couverture-tests.md` | 9.0/10 |
| 73 | 2026-04-15 | refactor | Thread safety | `73-refactor-thread-safety.md` | 9.0/10 |
```

---

## 🤖 Instructions pour l'Agent

### En début de session
1. Lire `PROMPT_REPRISE.md` pour le contexte
2. Consulter `INDEX.md` pour historique récent (5 dernières sessions)
3. Charger fichiers archive si pertinent (via `@archive/...`)

### Pendant la session
1. Noter modifications importantes au fur et à mesure
2. Mettre à jour `PROMPT_REPRISE.md` en temps réel (max 200 lignes)

### En fin de session
1. **Détecter le type** via mots-clés de la session
2. **Générer le fichier archive** avec le template
3. **Mettre à jour INDEX.md** (ajouter ligne)
4. **Nettoyer PROMPT_REPRISE.md** (garder uniquement : objectif, résultat, prochaine session)
5. **Proposer à l'utilisateur** de valider l'archivage

---

## 🚨 Seuils d'Alerte

| Fichier | Seuil | Action |
|---------|-------|--------|
| `PROMPT_REPRISE.md` | > 200 lignes | ⚠️ Proposer nettoyage |
| `INDEX.md` | > 50 sessions | ⚠️ Archiver anciennes sessions dans `.agents/archives/YYYY-MM.md` |
| `.agents/sessions/` | > 20 fichiers | ⚠️ Compresser sessions > 30 jours |

---

## 📁 Structure des Dossiers

```
.agents/
├── AGENT_SESSION_MANAGER.md    # Ce fichier
├── INDEX.md                    # Tableau récapitulatif
├── PROMPT_REPRISE.md           # Session actuelle
├── sessions/                   # Archives par session
│   ├── 74-debug-couverture-tests.md
│   ├── 73-refactor-thread-safety.md
│   └── ...
└── archives/                   # Archives mensuelles (si > 50 sessions)
    ├── 2026-04.md
    └── ...
```

---

## 🧹 Procédure de Nettoyage (si seuil dépassé)

```bash
# Si INDEX.md > 50 lignes
1. Créer `.agents/archives/2026-04.md`
2. Déplacer sessions 1-20 dans archive
3. Garder sessions 21+ dans INDEX.md
4. Mettre à jour tableau avec lien vers archive
```

---

**Note** : Cette procédure est manuelle et déclenchée uniquement par l'agent quand un seuil est atteint.
