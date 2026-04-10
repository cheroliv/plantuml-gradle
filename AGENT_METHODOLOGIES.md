# 🧭 Mécanisme de Proposition de Méthodologie

**Date de création** : 2026-04-10  
**Version** : 1.0  
**Objectif** : Documentation du système de détection et proposition de méthodologies par l'agent

---

## 📋 Vue d'ensemble

Ce document décrit le **mécanisme de détection automatique** qui permet à l'agent de proposer la méthodologie appropriée selon le type de session.

**Principe** : L'agent détecte le type de session via des mots-clés et propose la méthodologie adaptée **uniquement si nécessaire** (approche just-in-time).

---

## 🎯 Menu des Méthodologies

### Tableau de détection (dans AGENTS.md)

| # | Type de session | Indices détectés | Méthodologie proposée | Fichier à charger |
|---|-----------------|------------------|----------------------|-------------------|
| 1 | **Optimisation test fonctionnel** | "optimiser", "réduire temps", "accélérer", `*FunctionalTest.kt` | "Veux-tu que j'applique la méthodologie d'optimisation ?" | `METHODOLOGIE_OPTIMISATION_TESTS.md` |
| 2 | **Création test unitaire** | "créer test", "ajouter test", "couverture", `src/test/*Test.kt` | "Veux-tu que je consulte l'analyse de couverture ?" | `TEST_COVERAGE_ANALYSIS.md` |
| 3 | **Debug test fonctionnel** | "debug", "exécuter test", "faire passer test", `*FunctionalTest.kt` | "Veux-tu que j'applique la méthodologie de debug ?" | `METHODOLOGIE_OPTIMISATION_TESTS.md` |
| 4 | **Correction bug** | "corriger", "bug", "fix", "réparer" | Aucune — agir directement | — |
| 5 | **Nouvelle feature** | "ajouter", "nouvelle", "feature", "implémenter" | Aucune — agir directement | — |
| 6 | **Fin de session** | "nouvelle session", "je quitte", "session terminée", "à plus tard" | **PROCÉDURE AUTOMATIQUE** (5 étapes) | `COMPLETED_TASKS_ARCHIVE.md` |

---

## 🤖 Comportement de l'Agent

### Règles de détection

#### 1. Analyse du prompt utilisateur

L'agent analyse le prompt pour détecter :
- ✅ **Mots-clés** (verbes d'action)
- ✅ **Noms de fichiers** (pattern `*Test.kt`)
- ✅ **Chemins** (`src/test/`, `src/functionalTest/`)
- ✅ **Contexte** (session précédente, backlog)

#### 2. Décision de proposition

**Flow de décision** :
```
Prompt utilisateur
    ↓
Détection mots-clés + patterns
    ↓
Match avec tableau méthodologies ?
    ↓
OUI → Proposer méthodologie (format standardisé)
NON → Agir directement (aucune proposition)
```

#### 3. Format de proposition

**Template obligatoire** :
```
🎯 Méthodologie détectée : [Type de session]

Je peux appliquer :
- [NOM_DU_FICHIER].md
- Checklist : [Point 1], [Point 2], [Point 3]

Veux-tu que je charge ce fichier et applique cette méthodologie ?
```

**Exemples** :

**Optimisation** :
```
🎯 Méthodologie détectée : Optimisation test fonctionnel

Je peux appliquer :
- METHODOLOGIE_OPTIMISATION_TESTS.md
- Checklist : Code inline, maxIterations=1, suppression flags inutiles

Veux-tu que je charge ce fichier et applique cette méthodologie ?
```

**Création test** :
```
🎯 Méthodologie détectée : Création test unitaire

Je peux appliquer :
- TEST_COVERAGE_ANALYSIS.md
- Checklist : Classes non-testées, méthodes privées, data classes

Veux-tu que je charge ce fichier et applique cette méthodologie ?
```

**Debug** :
```
🎯 Méthodologie détectée : Debug test fonctionnel

Je peux appliquer :
- METHODOLOGIE_OPTIMISATION_TESTS.md (Section "🔄 Processus itératif")
- Étapes : Run → Debug → Optimise

Veux-tu que je charge ce fichier et applique cette méthodologie ?
```

---

## ✅ Règles de Proposition

### Obligations (✅ À FAIRE)

1. ✅ **Toujours proposer** si la session matche un type connu (tableau ci-dessus)
2. ✅ **Attendre confirmation** avant de charger le fichier (sauf fin de session)
3. ✅ **Citer 3 points clés** de la méthodologie (checklist résumé)
4. ✅ **Utiliser le format standardisé** (emoji + 3 lignes + question)
5. ✅ **Respecter les exceptions** (liste ci-dessous)

### Interdictions (❌ À NE PAS FAIRE)

1. ❌ **Jamais charger sans confirmation** (sauf procédure fin de session)
2. ❌ **Jamais proposer si session claire** (ex: "corriger bug" → agir directement)
3. ❌ **Jamais proposer plusieurs méthodologies** (une seule à la fois)
4. ❌ **Jamais charger tous les fichiers** (approche just-in-time)

---

## 🚫 Exceptions — Ne PAS proposer

| Cas | Raison | Comportement |
|-----|--------|--------------|
| Session déjà en cours | Méthodologie déjà chargée | Continuer sans re-proposer |
| Utilisateur a spécifié la méthodologie | Déjà dans le prompt | Charger directement (pas de question) |
| Session urgente | "corriger bug critique", "réparer build cassé" | Agir immédiatement |
| Session exploratoire | "comment marche X ?", "où est Y ?" | Répondre sans méthodologie |
| Fin de session | "je quitte", "à plus tard" | **PROCÉDURE AUTOMATIQUE** (pas de question) |

---

## 📊 Exemples de Sessions

### Session 1 — Optimisation (PROPOSITION REQUISE)

**Prompt utilisateur** :
```
On attaque MegaOptimizedFunctionalTest.kt
```

**Agent détecte** :
- Pattern : `*FunctionalTest.kt`
- Contexte : Session précédente = optimisation (LlmHandshakeTest)
- Type : Optimisation test fonctionnel

**Agent propose** :
```
🎯 Méthodologie détectée : Optimisation test fonctionnel

Je peux appliquer :
- METHODOLOGIE_OPTIMISATION_TESTS.md
- Checklist : Code inline, maxIterations=1, suppression flags inutiles

Veux-tu que je charge ce fichier et applique cette méthodologie ?
```

**Utilisateur confirme** :
```
Oui
```

**Agent charge** : `METHODOLOGIE_OPTIMISATION_TESTS.md` et applique

---

### Session 2 — Création test (PROPOSITION REQUISE)

**Prompt utilisateur** :
```
Il manque des tests pour ProcessPlantumlPromptsTask
```

**Agent détecte** :
- Mots-clés : "manque des tests"
- Nom de classe : `ProcessPlantumlPromptsTask` (tâche Gradle)
- Type : Création test unitaire

**Agent consulte** : `TEST_COVERAGE_ANALYSIS.md` pour vérifier statut

**Agent propose** :
```
🎯 Méthodologie détectée : Création test unitaire

Je peux appliquer :
- TEST_COVERAGE_ANALYSIS.md
- Checklist : 3 tâches Gradle sans tests, méthodes privées, data classes

Veux-tu que je charge ce fichier et applique cette méthodologie ?
```

---

### Session 3 — Correction bug (AUCUNE PROPOSITION)

**Prompt utilisateur** :
```
Le test ValidatePlantumlSyntaxTaskTest échoue avec NullPointerException
```

**Agent détecte** :
- Mots-clés : "échoue", "NullPointerException"
- Type : Correction bug (urgent)

**Agent agit** :
- ❌ Aucune proposition de méthodologie
- ✅ Analyse directe du problème
- ✅ Correction immédiate

---

### Session 4 — Fin de session (PROCÉDURE AUTOMATIQUE)

**Prompt utilisateur** :
```
J'ai fini pour cette session
```

**Agent détecte** :
- Mots-clés : "fini", "session"
- Type : Fin de session

**Agent exécute** :
- ❌ Aucune question posée
- ✅ **PROCÉDURE AUTOMATIQUE** (5 étapes) :
  1. Vérifier tests (`./gradlew test`)
  2. Mettre à jour `AGENTS.md`
  3. Mettre à jour `COMPLETED_TASKS_ARCHIVE.md`
  4. Mettre à jour `PROMPT_REPRISE.md`
  5. Mettre à jour `TEST_COVERAGE_ANALYSIS.md` (si besoin)
- ✅ Réponse : "✅ Procédure exécutée" + résumé 3 lignes

---

## 🔄 Workflow Complet

```
┌─────────────────────────────────────────┐
│  Prompt utilisateur                     │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Analyse : mots-clés + patterns         │
│  - "optimiser", "créer", "debug", etc.  │
│  - Noms de fichiers (*Test.kt)          │
│  - Chemins (src/test/, src/functional/) │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Match avec tableau méthodologies ?     │
└──────────────┬──────────────────────────┘
               │
       ┌───────┴───────┐
       │               │
      OUI             NON
       │               │
       ▼               ▼
┌─────────────┐  ┌─────────────────┐
│ Exceptions ?│  │ Agir directement│
│ - Urgent ?  │  │ (aucune propo)  │
│ - Déjà en   │  └─────────────────┘
│   cours ?   │
│ - Spécifié  │
│   par user ?│
└──────┬──────┘
       │
   ┌───┴───┐
   │       │
  OUI     NON
   │       │
   ▼       ▼
┌─────────┐ ┌─────────────────────────────┐
│ Agir    │ │ Proposer méthodologie       │
│ direct  │ │ (format standardisé)        │
└─────────┘ └──────────────┬──────────────┘
                           │
                           ▼
                  ┌────────────────────────┐
                  │ Utilisateur confirme ? │
                  └────────┬───────────────┘
                           │
                    ┌──────┴──────┐
                    │             │
                   OUI           NON
                    │             │
                    ▼             ▼
           ┌─────────────┐ ┌──────────────┐
           │ Charger     │ │ Continuer    │
           │ fichier +   │ │ sans         │
           │ Appliquer   │ │ méthodologie │
           └─────────────┘ └──────────────┘
```

---

## 📚 Fichiers de Méthodologie

### 1. METHODOLOGIE_OPTIMISATION_TESTS.md

**Objectif** : Optimisation des tests fonctionnels

**Contenu** :
- Principes fondamentaux (5 règles)
- Techniques d'optimisation (4 techniques)
- Checklist d'optimisation (20 points)
- Exemples Avant/Après
- Statistiques d'optimisation
- Leçons apprises

**Quand charger** :
- ✅ Session d'optimisation (`*FunctionalTest.kt`)
- ✅ Session de debug de test fonctionnel
- ❌ Jamais pour les tests unitaires

**Points clés à citer** :
1. Code inline (suppression @BeforeEach)
2. maxIterations=1 (LLM)
3. Suppression flags inutiles (--stacktrace, --info)

---

### 2. TEST_COVERAGE_ANALYSIS.md

**Objectif** : Suivi couverture tests unitaires

**Contenu** :
- Statistiques de couverture
- Classes testées/non-testées
- Méthodes privées à tester
- Data classes à tester
- Recommandations par session

**Quand charger** :
- ✅ Création nouveaux tests unitaires
- ✅ Analyse de couverture
- ❌ Jamais pour les tests fonctionnels

**Points clés à citer** :
1. Classes sans tests directs
2. Méthodes privées non-testées
3. Data classes à tester

---

### 3. COMPLETED_TASKS_ARCHIVE.md

**Objectif** : Archive des sessions terminées

**Contenu** :
- Historique des sessions (23+ sessions)
- Détails des optimisations (avant/après)
- Statistiques (tests, temps, code)
- Leçons apprises

**Quand charger** :
- ✅ Fin de session (procédure automatique)
- ❌ Jamais pendant la session

**Utilisation** :
- Ajouter nouvelle section avec nom de session
- Détails des tests créés/optimisés
- Statistiques avant/après

---

### 4. PROMPT_REPRISE.md

**Objectif** : Reprise de session (contexte + mission)

**Contenu** :
- Session précédente (résumé)
- Mission en cours
- Suggestions pour session suivante
- Fichiers de référence

**Quand charger** :
- ✅ Début de chaque session
- ✅ Fin de session (mise à jour)

**Utilisation** :
- Début : Lire mission en cours
- Fin : Mettre à jour avec nouvelle mission

---

## 🎯 Critères de Détection Détaillés

### Type 1 : Optimisation test fonctionnel

**Indices forts** :
- ✅ Fichier : `*FunctionalTest.kt`, `*Optimized*.kt`
- ✅ Verbes : "optimiser", "accélérer", "réduire temps"
- ✅ Contexte : Session précédente = optimisation

**Indices faibles** :
- ⚠️ "améliorer" (ambigu : optimisation ou refactoring ?)
- ⚠️ "nettoyer" (ambigu : code ou tests ?)

**Décision** :
- 1+ indice fort → **PROPOSER**
- Seulement indices faibles → **DEMANDER CLARIFICATION**

---

### Type 2 : Création test unitaire

**Indices forts** :
- ✅ Fichier : `src/test/*Test.kt`
- ✅ Verbes : "créer test", "ajouter test", "couverture"
- ✅ Noms : "Test.kt", "UnitTest.kt"

**Indices faibles** :
- ⚠️ "tester" (ambigu : test unitaire ou fonctionnel ?)
- ⚠️ "vérifier" (ambigu : test ou debug ?)

**Décision** :
- 1+ indice fort → **PROPOSER**
- Seulement indices faibles → **DEMANDER CLARIFICATION**

---

### Type 3 : Debug test fonctionnel

**Indices forts** :
- ✅ Fichier : `*FunctionalTest.kt`
- ✅ Verbes : "debug", "exécuter test", "faire passer"
- ✅ Erreurs : "échec", "FAILED", "AssertionFailedError"

**Indices faibles** :
- ⚠️ "problème" (trop vague)
- ⚠️ "ne marche pas" (trop vague)

**Décision** :
- 1+ indice fort → **PROPOSER**
- Seulement indices faibles → **DEMANDER CLARIFICATION**

---

### Type 4 : Correction bug

**Indices forts** :
- ✅ Verbes : "corriger", "fix", "réparer"
- ✅ Noms : "bug", "erreur", "exception", "crash"
- ✅ Urgence : "critique", "urgent", "bloquant"

**Décision** :
- **AUCUNE PROPOSITION** → Agir directement

---

### Type 5 : Nouvelle feature

**Indices forts** :
- ✅ Verbes : "ajouter", "implémenter", "nouvelle"
- ✅ Noms : "feature", "fonctionnalité"

**Décision** :
- **AUCUNE PROPOSITION** → Agir directement

---

### Type 6 : Fin de session

**Indices forts** :
- ✅ Expressions : "je quitte", "session terminée", "à plus tard"
- ✅ Verbes : "fini", "termine"

**Décision** :
- **PROCÉDURE AUTOMATIQUE** (5 étapes, sans question)

---

## 📈 Métriques de Suivi

### Taux de détection

| Métrique | Cible | Mesure |
|----------|-------|--------|
| Détections correctes | >90% | Sessions où méthodologie proposée était pertinente |
| Faux positifs | <5% | Sessions où méthodologie proposée était incorrecte |
| Faux négatifs | <10% | Sessions où méthodologie aurait dû être proposée |
| Confirmations utilisateur | >80% | Utilisateurs acceptant la proposition |

### Améliorations continues

**Après chaque session** :
1. ✅ La détection était-elle correcte ?
2. ✅ La méthodologie proposée était-elle utile ?
3. ✅ Le format de proposition était-il clair ?
4. ✅ Faut-il ajouter/modifier des indices de détection ?

**Mise à jour** :
- Ajouter nouveaux indices détectés
- Ajuster les règles de décision
- Améliorer le format de proposition

---

## 🛠 Guide d'Utilisation pour l'Agent

### Avant de proposer

1. **Analyser le prompt** :
   - Mots-clés présents ?
   - Noms de fichiers correspondants ?
   - Contexte de session précédente ?

2. **Vérifier exceptions** :
   - Session déjà en cours ?
   - Utilisateur a déjà spécifié méthodologie ?
   - Session urgente ?

3. **Consulter AGENTS.md** :
   - Ligne 291-366 (Menu des méthodologies)
   - Match avec tableau

### Pendant la proposition

1. **Utiliser format standardisé** :
   ```
   🎯 Méthodologie détectée : [Type]
   
   Je peux appliquer :
   - [FICHIER].md
   - Checklist : [3 points]
   
   Veux-tu que je charge ce fichier et applique cette méthodologie ?
   ```

2. **Attendre confirmation** :
   - ✅ "Oui", "Oui charge-le", "Vas-y" → Charger
   - ❌ "Non", "Pas nécessaire" → Continuer sans

### Après confirmation

1. **Charger le fichier** :
   ```
   [Lecture du fichier méthodologie]
   ```

2. **Appliquer méthodologie** :
   - Suivre checklist
   - Citer sections pertinentes
   - Mesurer progrès

---

## 📝 Historique des Versions

| Version | Date | Changements |
|---------|------|-------------|
| 1.0 | 2026-04-10 | Création du document |

---

## 🔗 Références

- **AGENTS.md** : Lignes 291-366 (Menu des méthodologies)
- **METHODOLOGIE_OPTIMISATION_TESTS.md** : Techniques d'optimisation
- **TEST_COVERAGE_ANALYSIS.md** : Couverture des tests unitaires
- **COMPLETED_TASKS_ARCHIVE.md** : Historique des sessions
- **PROMPT_REPRISE.md** : Contexte de session
