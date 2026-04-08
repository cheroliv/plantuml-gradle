# 🔄 Checklist de Transition de Session

## 📋 Quand changer de session

### ✅ Situations idéales pour une nouvelle session

- [ ] **1 test créé et validé** → Session terminée, ouvrir nouvelle session
- [ ] **1 bug fixé** → Commit → Nouvelle session
- [ ] **1 feature complétée** → Documentation → Nouvelle session
- [ ] **3 échanges sans progrès** → Reset mental → Nouvelle session
- [ ] **Contexte > 50k tokens** → Nettoyage → Nouvelle session

### ❌ Ne PAS changer de session pour

- Un simple test qui échoue (corriger puis continuer)
- Une question rapide de clarification
- Une modification mineure (< 5 lignes)

---

## 🚀 Procédure de fin de session

### 1. Vérifier l'état du code
```bash
./gradlew -p plantuml-plugin test
git status
```

### 2. Mettre à jour les fichiers de contexte
- [ ] `AGENTS.md` — Section "État actuel" (mettre à jour)
- [ ] `COMPLETED_TASKS_ARCHIVE.md` — Déplacer les tâches terminées
- [ ] `PROMPT_REPRISE.md` — Mettre à jour la mission si changée

### 3. Commiter (optionnel mais recommandé)
```bash
git add -A
git commit -m "Fix: [description courte]"
git push
```

### 4. Noter le point d'arrêt
Dans `COMPLETED_TASKS_ARCHIVE.md`, ajouter :
```markdown
## Session [DATE] — [RÉSUMÉ]

### Fait
- [Tâche 1] ✅
- [Tâche 2] ✅

### Prochaine session
- [Tâche suivante]
- [Fichier à lire en priorité]
```

---

## 🎯 Démarrage de nouvelle session

### Prompt d'ouverture (à copier-coller)

```
Nouvelle session. Contexte chargé :
1. AGENTS.md (architecture, décisions, méthodologie)
2. PROMPT_REPRISE.md (mission en cours)

Mission : [Décrire la tâche unique de cette session]

Contrainte : 1 fichier à la fois, validation après chaque changement.
```

### Vérification rapide
```bash
# 1. État des tests
./gradlew -p plantuml-plugin test --console=plain

# 2. Fichiers modifiés depuis dernière session
git status

# 3. Dernière tâche en cours
tail -20 COMPLETED_TASKS_ARCHIVE.md
```

---

## 📊 Métriques de session idéale

| Métrique | Cible |
|----------|-------|
| **Durée** | 15-30 minutes |
| **Fichiers modifiés** | 1-3 maximum |
| **Tests créés** | 1 fichier |
| **Échanges LLM** | 5-10 messages |
| **Contexte tokens** | < 50k |

---

## ⚠️ Signes qu'il faut changer de session

- [ ] Le LLM répète des erreurs déjà corrigées
- [ ] Plus de 3 fichiers modifiés en parallèle
- [ ] Tests échouent sur des problèmes différents
- [ ] Conversation > 50 messages
- [ ] Tu te dis "il faudrait qu'on reparte sur de bonnes bases"

---

**Règle d'or** : Mieux vaut 5 sessions de 20 minutes qu'une session de 2 heures avec debugging chaos. 🎯
