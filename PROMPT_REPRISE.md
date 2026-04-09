# 🔄 Prompt de reprise — Session Suivante

> **Prérequis** : `AGENTS.md` est déjà chargé dans le contexte  
> **Règle** : 1 session = 1 tâche unique et validée

---

## ✅ Session précédente — TERMINÉE

**Tâche** : Organisation des fichiers de configuration YAML  
**Statut** : ✅ **TERMINÉE**

### Fichiers créés
- `plantuml-context.yml` — Configuration personnelle (credentials réels)
- `plantuml-test-context.yml` — Configuration de test CI/CD (credentials réels pour GitHub Actions)
- `sample-plantuml-context.yml` — Exemple pour le repo (sans credentials)
- `sample-plantuml-test-config.yml` — Exemple de config de test (renommé de `plantuml-test-config.yml`)

### Fichiers modifiés
- `.gitignore` — Ajout de `plantuml-context.yml` et `plantuml-test-context.yml`

### Sécurité
- ✅ Les fichiers avec credentials sont dans `.gitignore`
- ✅ Jamais commités dans le repo

---

## 🎯 Mission de CETTE session

**Objectif** : Choisir une tâche dans le backlog ci-dessous

### 📋 Backlog — Tâches disponibles

| # | Tâche | Description | Estimation | Difficulté |
|---|-------|-------------|------------|------------|
| 1 | **Ajout providers LLM multiples** | OpenAI, Claude, HuggingFace, Groq + Ollama dans `plantuml-context.yml` | 2h | ⭐⭐ Moyen |
| 2 | **Documentation des providers** | README/wiki : comment configurer chaque provider | 1h | ⭐ Facile |
| 3 | **Tests avec vrais providers** | Tests fonctionnels avec credentials réels (utilisez `@Ignore`) | 3h | ⭐⭐⭐ Avancé |
| 4 | **Optimiser `FilePermissionTest.kt`** | Réduire temps d'exécution (~1min35sec) | 2h | ⭐⭐⭐ Avancé |

---

## 🚀 Démarrage rapide

### Étape 1 : Vérifier l'état actuel
```bash
./gradlew -p plantuml-plugin test
```
→ Doit afficher : **128/128 tests passent (100%)**

### Étape 2 : Choisir une tâche du backlog
- **Tâche 1** (Recommandée) : Ajouter providers LLM dans `plantuml-context.yml`
  - OpenAI, Anthropic/Claude, HuggingFace, Groq
  - Permettre switch via CLI : `-Pplantuml.langchain4j.model=gemini`

### Étape 3 : Travailler
- 1 fichier à la fois
- Validation après chaque changement

### Étape 4 : Valider
- ✅ **Si passe** → Fin de session → Nouvelle session
- ❌ **Si échec** → Corriger → Re-tester

---

## ✅ Critères de succès de CETTE session

- [ ] **1 tâche du backlog complétée**
- [ ] **Tous les tests passent** (`./gradlew -p plantuml-plugin test`)
- [ ] `AGENTS.md` mis à jour (section "Backlog")
- [ ] `COMPLETED_TASKS_ARCHIVE.md` mis à jour

---

## 📚 Fichiers de référence

- `AGENTS.md` — Section "🔵 NOUVELLE ÉPIC — Configuration multi-LLM avec credentials"
- `plantuml-context.yml` — Configuration personnelle (credentials réels)
- `sample-plantuml-context.yml` — Exemple pour le repo (placeholders)

---

## 🔄 Fin de session

**Quand la session est terminée :**
1. Vérifier : `./gradlew -p plantuml-plugin test`
2. Mettre à jour `AGENTS.md`
3. Déplacer vers `COMPLETED_TASKS_ARCHIVE.md`
4. **Ouvrir une nouvelle session** pour la tâche suivante

**⚠️ Git — INTERDICTION :**
- ❌ **L'agent N'EST PAS autorisé à exécuter des commandes Git**
- ❌ **Pas de `git add`, `git commit`, `git push`**
- ✅ **C'est l'utilisateur qui gère Git manuellement**

---

**Bonne session ! 🎉**
