# 📦 Gestion des Sessions — Guide Rapide

## 🎯 Nouveau Système (Session 75+)

### Structure

```
.agents/
├── INDEX.md                        # Vue d'ensemble (toujours chargé)
├── PROMPT_REPRISE.md               # Session actuelle (racine)
├── AGENT_SESSION_MANAGER.md        # Procédure d'archivage
├── SESSIONS_HISTORY.md             # Historique allégé
└── sessions/
    ├── 74-debug-couverture-tests.md    # Format: {num}-{type}-{sujet}.md
    ├── 73-debug-crash-functional-test.md
    └── legacy/                         # Ancien format (61-72)
        └── SESSION_XX_SUMMARY.md
```

---

## 🔄 Fin de Session (Procédure)

**Quand** : Utilisateur dit "session terminée", "à plus tard", etc.

**5 étapes** :

1. ✅ **Vérifier** : Tests pass, tâches complètes
2. 📝 **Résumer** : Créer `.agents/sessions/{num}-{type}-{sujet}.md`
3. 📊 **Indexer** : Ajouter ligne dans `INDEX.md`
4. 🧹 **Nettoyer** : `PROMPT_REPRISE.md` (max 200 lignes)
5. 📋 **Préparer** : Session N+1 dans `PROMPT_REPRISE.md`

---

## 🏷️ Types de Sessions

| Type | Mot-clé | Exemple |
|------|---------|---------|
| `debug` | corriger, bug, crash | `74-debug-couverture-tests.md` |
| `feature` | ajouter, nouvelle | `75-feature-kdoc-generator.md` |
| `refactor` | optimiser, nettoyer | `73-refactor-thread-safety.md` |
| `test` | test unitaire, couverture | `72-test-migration-mockito.md` |
| `docs` | documentation, README | `71-docs-traduction-fr.md` |

---

## 📊 Seuils

| Fichier | Seuil | Action |
|---------|-------|--------|
| `PROMPT_REPRISE.md` | > 200 lignes | ⚠️ Nettoyer |
| `INDEX.md` | > 50 sessions | ⚠️ Archiver mensuel |
| `SESSIONS_HISTORY.md` | > 20 lignes | ⚠️ Alléger |

---

## 🚀 Avantages

| Avant | Après |
|-------|-------|
| `SESSIONS_HISTORY.md` : 1432 lignes | `SESSIONS_HISTORY.md` : 50 lignes |
| Contexte saturé | Contexte léger |
| Archives mélangées | Archives organisées par type |
| Manuel | Semi-automatique |

---

**Session 75** : Première session avec ce système ✅
