# 📝 Procédure de Fin de Session — PlantUML Gradle Plugin

> **Objectif** : Mécanisme LAZY/EAGER pour persister l'information sans polluer le contexte
> **Principe** : EAGER = essentiel (toujours chargé), LAZY = détails (chargé sur demande)

---

## 🎯 Philosophie LAZY/EAGER

### Pourquoi ce mécanisme ?

**Problème** : Les LLM ont un contexte limité. Charger trop d'informations dilue l'attention et coûte des tokens.

**Solution** : Séparer l'information en 2 catégories :

| Type | Caractéristiques | Exemples |
|------|------------------|----------|
| **EAGER** | • Critique pour démarrer<br>• < 100 lignes<br>• Lu automatiquement | `PROMPT_REPRISE.md`, `AGENTS.md` |
| **LAZY** | • Détails complets<br>• Illimité<br>• Lu sur demande explicite | `.agents/sessions/{N}*.md`, `AGENT_REFERENCE.md` |

### Analogie

- **EAGER** = Tableau de bord de voiture (vitesse, carburant, alertes)
- **LAZY** = Manuel du propriétaire (détails techniques, historique entretien)

---

## 📋 Procédure de Fin de Session — 5 Étapes Obligatoires

### ÉTAPE 1 : Vérifier l'état des tests (optionnel)

```bash
# Seulement si demandé explicitement
./gradlew test
./gradlew cucumberTest
```

⚠️ **Jamais** exécuter de tests en procédure de fin de session sans demande explicite.

---

### ÉTAPE 2 : Créer l'archive LAZY de la session

**Fichier** : `.agents/sessions/{N}-{titre}.md`

**Structure obligatoire** :

```markdown
# Session {N} — {Titre descriptif}

**Date** : {date}  
**Statut** : ✅ TERMINÉE / ⚠️ INCOMPLÈTE / ❌ ÉCHEC  
**EPIC** : {lien vers EPIC si applicable}

---

## Contexte

{Pourquoi cette session existe ? Quel problème résout-elle ?}

---

## Actions Entreprises

### 1. {Action principale}

**Fichiers modifiés** :
| Fichier | Lignes | Modification |
|---------|--------|--------------|
| `path/to/file.kt` | 12-45 | Suppression duplication |

**Commandes exécutées** :
```bash
{commande 1}
{commande 2}
```

**Résultats** :
- ✅ Succès : {détail}
- ❌ Échec : {détail}

---

## Conflits/Problèmes Résolus

| Problème | Cause | Solution | Fichier |
|----------|-------|----------|---------|
| {description} | {root cause} | {action} | {file:line} |

---

## Patterns et Leçons Apprises

### ❌ Pattern à Éviter

```kotlin
// MAUVAIS : exemple de code problématique
```

### ✅ Pattern à Suivre

```kotlin
// BON : exemple de solution
```

---

## Résultats

| Critère | Statut | Détails |
|---------|--------|---------|
| Compilation | ✅/❌ | {détail} |
| Tests | ✅/❌ | {X/Y} scénarios PASS |
| Conflits | ✅/❌ | {nombre} résolus |

---

## Prochaines Étapes (Session {N+1})

1. **Action 1** : `{commande ou tâche}`
2. **Action 2** : `{commande ou tâche}`
3. **Validation** : `{critère de succès}`

---

## Références

- **Archive** : `.agents/sessions/{N}-{titre}.md` (ce fichier)
- **Reprise** : `PROMPT_REPRISE.md` (session {N+1})
- **EPIC** : `{lien}`

---

**Session {N}** — {statut emoji}  
**Session {N+1}** — {prêt/à faire}
```

---

### ÉTAPE 3 : Mettre à jour le résumé EAGER

**Fichier** : `PROMPT_REPRISE.md`

**Structure obligatoire** :

```markdown
# 🔄 Prompt de reprise — Session {N+1}

> **EPIC** : {nom}  
> **Statut** : Session {N} ✅ — {résumé 1 ligne}  
> **Mission** : {objectif session N+1}

---

## Session {N} — Résumé

**Date** : {date}  
**Résultat** : ✅ {accomplissement principal}

| Fichier | Modification |
|---------|--------------|
| `file1.kt` | -20 lignes |
| `file2.kt` | +15 lignes |

**Archive** : `.agents/sessions/{N}-{titre}.md`

---

## Session {N+1} — Priorités

```bash
# Commande principale
{commande}
```

### Critères d'Acceptation

- [ ] {critère 1}
- [ ] {critère 2}
- [ ] {critère 3}

---

## Couverture Tests

| Feature | Scénarios | Statut |
|---------|-----------|--------|
| {feature} | {N} | ✅/🟡/❌ |

**Total** : {X}/{Y} ({Z}%)

---

## Règles

- ❌ {règle 1}
- ❌ {règle 2}
- ✅ {règle 3}

---

**Session {N}** ✅ — **Session {N+1}** 🚀
```

**Contraintes** :
- Maximum **100 lignes**
- **1 référence** à l'archive LAZY
- **Commandes exécutables** pour session N+1
- **Critères d'acceptation** clairs

---

### ÉTAPE 4 : Nettoyer AGENTS.md (si besoin)

**Vérifier** : `AGENTS.md` dépasse-t-il 100 lignes ?

**Si OUI** :

1. Identifier sections détaillées (commandes, méthodologie, pièges)
2. Transférer vers `AGENT_REFERENCE.md` ou fichier dédié
3. Remplacer par renvoi court : `**Voir** : `AGENT_REFERENCE.md``

**Si NON** : ✅ Ne rien toucher

---

### ÉTAPE 5 : Valider la cohérence

**Checklist** :

- [ ] Archive LAZY créée : `.agents/sessions/{N}-{titre}.md`
- [ ] Résumé EAGER mis à jour : `PROMPT_REPRISE.md`
- [ ] Référence croisée présente (EAGER → LAZY)
- [ ] `PROMPT_REPRISE.md` < 100 lignes
- [ ] Commandes session N+1 testables
- [ ] Critères d'acceptation clairs

---

## 📊 Tableau des Fichiers — Politique de Chargement

| Fichier | Type | Taille Max | Quand Charger |
|---------|------|------------|---------------|
| `PROMPT_REPRISE.md` | **EAGER** | 100 lignes | **Début session** (auto) |
| `AGENTS.md` | **EAGER** | 100 lignes | **Toujours** (auto) |
| `AGENT_REFERENCE.md` | LAZY | Illimité | Sur besoin (commandes, pièges) |
| `.agents/sessions/{N}*.md` | LAZY | Illimité | Sur demande (détails session) |
| `.agents/ARCHITECTURE.md` | LAZY | Illimité | Sur besoin (architecture) |
| `.agents/INDEX.md` | LAZY | Illimité | Sur besoin (navigation) |
| `SESSIONS_HISTORY.md` | LAZY | Illimité | Sur besoin (historique) |
| `COMPLETED_TASKS_ARCHIVE.md` | LAZY | Illimité | Fin session (archive) |

---

## 🔍 Comment l'agent sait où chercher ?

### Début de Session

**Chargement automatique (EAGER)** :
1. `PROMPT_REPRISE.md` → Mission, priorités, contexte
2. `AGENTS.md` → Architecture, règles, points d'attention

**Si besoin de détails** :
- Commandes ? → `AGENT_REFERENCE.md`
- Détails session N ? → `.agents/sessions/{N}-{titre}.md` (référencé dans PROMPT_REPRISE.md)
- Architecture ? → `.agents/ARCHITECTURE.md`

### Fin de Session

**Mots-clés déclencheurs** :
- "nouvelle session"
- "je quitte"
- "session terminée"
- "à plus tard"
- "on arrête là"

**Action** : Exécuter les 5 étapes SILENCIEUSEMENT

---

## 📝 Template Rapide — Archive LAZY

```markdown
# Session {N} — {Titre}

**Date** : {date} | **Statut** : {✅/⚠️/❌}

## Contexte
{1-2 paragraphes}

## Actions
| Fichier | Modification |
|---------|--------------|
| `file.kt` | -X lignes |

## Commandes
```bash
{cmd}
```

## Résultats
| Critère | Statut |
|---------|--------|
| Compilation | ✅ |
| Tests | X/Y PASS |

## Prochaines Étapes
1. {action}
2. {action}

**Archive** : `.agents/sessions/{N}-{titre}.md`
```

---

## 📝 Template Rapide — Résumé EAGER

```markdown
# 🔄 Prompt de reprise — Session {N+1}

> **Statut** : Session {N} ✅ — {1 ligne}  
> **Mission** : {objectif}

## Session {N} — Résumé
**Résultat** : ✅ {accomplissement}  
**Archive** : `.agents/sessions/{N}-{titre}.md`

## Session {N+1} — Priorités
```bash
{commande}
```

### Critères
- [ ] {critère}

---
**Session {N}** ✅ — **Session {N+1}** 🚀
```

---

## ⚠️ Git — INTERDICTION ABSOLUE

- ❌ Pas de `git add`, `git commit`, `git push`, `git restore`, `git checkout`
- ✅ C'est l'utilisateur qui gère Git manuellement

---

## 🚨 Règle d'Or

- ❌ JAMAIS demander "Veux-tu que je...?"
- ❌ JAMAIS expliquer avant de faire
- ❌ JAMAIS répondre avant les 5 étapes terminées
- ✅ FAIRE DIRECTEMENT les 5 étapes SILENCIEUSEMENT
- ✅ Répondre UNIQUEMENT par "✅ Procédure exécutée" + 3 lignes max

---

## 📚 Références Internes

- **LAZY/EAGER** : Voir section "Philosophie LAZY/EAGER" ci-dessus
- **Templates** : Voir section "Template Rapide" ci-dessus
- **Tableau fichiers** : Voir section "Tableau des Fichiers" ci-dessus
