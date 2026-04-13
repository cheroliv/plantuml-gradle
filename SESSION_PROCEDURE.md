# 📝 Procédure de Session — PlantUML Gradle Plugin

> **Objectif** : Procédures de mise à jour en fin de session

---

## 🔄 Mise à jour en fin de session

### 5 Étapes obligatoires

1. **Vérifier les tests**
   ```bash
   ./gradlew -p plantuml-plugin test
   ```

2. **Mettre à jour `AGENTS.md`** (section "État actuel")

3. **Mettre à jour `COMPLETED_TASKS_ARCHIVE.md`**

4. **Mettre à jour `PROMPT_REPRISE.md`**

5. **Mettre à jour `TEST_COVERAGE_ANALYSIS.md`** (si besoin)

---

## 🧹 Nettoyage AGENTS.md — Vérification systématique

**Objectif** : Identifier les sections à transférer vers d'autres fichiers pour alléger le contexte

### Étape 1 : Analyser le contenu actuel de AGENTS.md

**Question** : Y a-t-il des sections trop détaillées ou redondantes ?

| Section | Taille | Action |
|---------|--------|--------|
| Contexte | ~6 lignes | ✅ Garder (essentiel) |
| Points d'attention | ~6 lignes | ✅ Garder (critique) |
| Architecture | ~28 lignes | ✅ Garder (essentiel) |
| État actuel | ~8 lignes | ✅ Garder (utile) |
| Autres sections | Variable | ⚠️ Vérifier redondance |

### Étape 2 : Vérifier les transferts possibles

**Fichiers de destination** :

| Fichier | Rôle | Transferts acceptés |
|---------|------|---------------------|
| `AGENT_REFERENCE.md` | Commandes, providers, pièges, méthodologie | ✅ Sections techniques détaillées |
| `AGENT_METHODOLOGIES.md` | Mécanisme de proposition de méthodologie | ✅ Procédures de détection |
| `METHODOLOGIE_OPTIMISATION_TESTS.md` | Techniques d'optimisation | ✅ Exemples d'optimisation |
| `TEST_COVERAGE_ANALYSIS.md` | Couverture des tests unitaires | ✅ Statistiques de couverture |
| `EPIC_CONSOLIDATION_TESTS_FONCTIONNELS.md` | EPIC tests fonctionnels | ✅ Détails d'EPIC |
| `SESSION_PROCEDURE.md` | Procédure de fin de session | ✅ Références, procédures |
| `SESSIONS_HISTORY.md` | Historique des sessions | ✅ Détails de sessions |
| `COMPLETED_TASKS_ARCHIVE.md` | Archive des tâches | ✅ Résultats de sessions |

### Étape 3 : Critères de transfert

**Signes qu'un transfert est nécessaire** :
- 🔴 AGENTS.md dépasse 100 lignes
- 🔴 Sections trop détaillées (commandes, méthodologie, pièges)
- 🔴 Redondance avec d'autres fichiers de référence
- 🔴 Contenu qui peut être consulté "sur besoin" (pas "toujours")

**Comment transférer** :
1. ✅ Identifier la section redondante dans AGENTS.md
2. ✅ Vérifier si un fichier dédié existe déjà (tableau ci-dessus)
3. ✅ Déplacer le contenu vers le fichier dédié
4. ✅ Remplacer par un renvoi court dans AGENTS.md (ex: "**Voir** : `AGENT_REFERENCE.md`")
5. ✅ Mettre à jour ce fichier (SESSION_PROCEDURE.md) avec le nouveau transfert

### Étape 4 : Exemple Session 45

**Transferts effectués** :
| Section | Fichier destination | Statut |
|---------|---------------------|--------|
| "🛠 Décisions techniques" | AGENT_REFERENCE.md | ✅ Transféré |
| "🚀 Optimisation des tests" | AGENT_REFERENCE.md | ✅ Transféré |
| "📝 Méthodologie de travail" | AGENT_REFERENCE.md | ✅ Transféré |
| "📚 Références" | SESSION_PROCEDURE.md | ✅ Transféré |
| "📝 Mise à jour" | SESSION_PROCEDURE.md | ✅ Transféré |

**Résultat** : AGENTS.md 133 → 77 lignes (-42%)

### Étape 5 : Checklist de fin de vérification

- [ ] AGENTS.md < 100 lignes
- [ ] Sections essentielles préservées (Contexte, Points d'attention, Architecture, État actuel)
- [ ] Redondances transférées vers fichiers dédiés
- [ ] Renvois courts ajoutés pour sections transférées
- [ ] PROMPT_REPRISE.md mis à jour avec nouvelle mission

---

## 📚 Références — Quand charger chaque fichier

| Fichier | Rôle | Chargement |
|---------|------|------------|
| `AGENTS.md` | Architecture, décisions, méthodologie | **Toujours** |
| `AGENT_REFERENCE.md` | Référence rapide (commandes, providers, pièges) | **Sur besoin** |
| `PROMPT_REPRISE.md` | Mission session en cours | **Début session** |
| `SESSIONS_HISTORY.md` | Historique complet sessions | **Sur besoin** |
| `COMPLETED_TASKS_ARCHIVE.md` | Archive tâches terminées | **Fin session** |
| `SESSION_PROCEDURE.md` | Procédure de fin de session | **Fin de session** |

---

## 🗂️ État des Fichiers de Mémoire

**Objectif** : Permettre le transfert d'éléments depuis `AGENTS.md` vers d'autres fichiers

| Fichier | Contenu | Taille | Transfert depuis AGENTS.md |
|---------|---------|--------|---------------------------|
| `AGENTS.md` | Contexte, architecture, points d'attention, état actuel | ~77 lignes | **Source** |
| `AGENT_REFERENCE.md` | Commandes, providers LLM, méthodologie optimisation, pièges | ~410 lignes | ✅ Sections "Décisions techniques", "Optimisation des tests", "Méthodologie" |
| `SESSION_PROCEDURE.md` | Procédure fin de session, tableau des références | ~60 lignes | ✅ Sections "Références", "Mise à jour" |
| `PROMPT_REPRISE.md` | Mission session en cours, backlog | ~120 lignes | ❌ Indépendant |
| `SESSIONS_HISTORY.md` | Historique complet des sessions | Variable | ❌ Indépendant |
| `COMPLETED_TASKS_ARCHIVE.md` | Archive tâches terminées | Variable | ❌ Indépendant |

### Quand transférer du contenu depuis `AGENTS.md`

**Signes qu'un transfert est nécessaire** :
- 🔴 `AGENTS.md` dépasse 100 lignes
- 🔴 Sections trop détaillées (commandes, méthodologie, pièges)
- 🔴 Redondance avec d'autres fichiers de référence

**Comment transférer** :
1. ✅ Identifier la section redondante dans `AGENTS.md`
2. ✅ Vérifier si un fichier dédié existe déjà (`AGENT_REFERENCE.md`, `SESSION_PROCEDURE.md`)
3. ✅ Déplacer le contenu vers le fichier dédié
4. ✅ Remplacer par un renvoi court dans `AGENTS.md` (ex: "**Voir** : `AGENT_REFERENCE.md`")
5. ✅ Mettre à jour ce tableau avec le nouveau transfert

---

## ⚠️ Git — INTERDICTION ABSOLUE

- ❌ Pas de `git add`, `git commit`, `git push`, `git restore`, `git checkout`
- ✅ C'est l'utilisateur qui gère Git manuellement

---

## 🚨 Déclencheur de procédure

**Mots-clés** : "nouvelle session", "je quitte", "session terminée", "à plus tard"

**Règle d'or** :
- ❌ JAMAIS demander "Veux-tu que je...?"
- ❌ JAMAIS expliquer avant de faire
- ❌ JAMAIS répondre avant les 5 étapes terminées
- ✅ FAIRE DIRECTEMENT les 5 étapes SILENCIEUSEMENT
- ✅ Répondre UNIQUEMENT par "✅ Procédure exécutée" + 3 lignes max
