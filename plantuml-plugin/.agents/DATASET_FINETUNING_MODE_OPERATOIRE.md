# 📘 Mode Opératoire — Création de Dataset Fine-tuning par Distillation

**Projet** : PlantUML Gradle Plugin  
**Objectif** : Créer un dataset d'entraînement pour agent IA spécialisé via distillation des données existantes  
**Version** : 1.0  
**Date de création** : 21 avril 2026 (Session 115)  
**Dernière mise à jour** : 21 avril 2026

---

## 🎯 Objectif

Créer un dataset de fine-tuning structuré (format JSONL) à partir des données existantes du projet PlantUML Gradle Plugin pour entraîner un agent IA spécialisé en :
- Architecture de plugins Gradle (Kotlin)
- Tests (unitaires, fonctionnels, Cucumber BDD)
- Intégration LLM (RAG, API Key Pool, rotation de clés)
- Documentation technique et procédures

**Format cible** : JSONL (JSON Lines) compatible avec les plateformes de fine-tuning (OpenAI, HuggingFace, etc.)

---

## 📊 Données Sources

### Inventaire Complet

| Catégorie | Emplacement | Fichiers | Taille | État |
|-----------|-------------|----------|--------|------|
| **Sessions individuelles** | `.agents/sessions/` | 29 fichiers `.md` | ~15 000 lignes | ✅ Structuré |
| **Résumés de sessions** | `.agents/archives/sessions_summaries/` | 13 fichiers `_SUMMARY.md` | ~2 000 lignes | ✅ Structuré |
| **Prompts de reprise** | `.agents/archives/prompts_archive/` | 8 fichiers `PROMPT_REPRISE*.md` | ~1 600 lignes | ✅ Structuré |
| **Décisions architecturales** | `.agents/` | 1 fichier `DECISIONS_ARCHITECTURE.md` | ~400 lignes | ✅ Structuré |
| **Archives consolidées** | `.agents/archives/` | 3 fichiers `SESSIONS_*.md` | ~3 000 lignes | ✅ Semi-structuré |
| **Historique complet** | `.agents/` | 2 fichiers `HISTORIQUE_*.md` | ~1 600 lignes | ✅ Semi-structuré |
| **Archive des tâches** | `.agents/archives/` | 1 fichier `COMPLETED_TASKS_ARCHIVE_2026-04.md` | 77 593 lignes | ⚠️ Brut |
| **Revues de code** | `.agents/archives/` | 1 fichier `CODE_REVIEW_2026-04.md` | 47 067 lignes | ⚠️ Brut |
| **Méthodologie** | `.agents/archives/` | 1 fichier `METHODOLOGIE_OPTIMISATION_TESTS.md` | 17 124 lignes | ⚠️ Brut |
| **Stratégie agent** | `.agents/` | 1 fichier `AGENT_MODUS_OPERANDI.adoc` | ~900 lignes | ⚠️ Brut |
| **Code source** | `src/` | ~50 fichiers `.kt`, `.feature` | ~10 000 lignes | ✅ Structuré |

**Total** : ~175 000 lignes de données brutes

---

## 🏗 Architecture du Pipeline

### Vue d'ensemble

```
┌─────────────────────────────────────────────────────────────┐
│                    DONNÉES SOURCES                          │
│  (Sessions, Archives, Code, Documentation, Prompts)         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    EXTRACTION                               │
│  (Parsing Markdown, Kotlin, YAML, Features Cucumber)        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    TRANSFORMATION                           │
│  (Normalisation, Génération paires prompt/response)         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    VALIDATION                               │
│  (Qualité, Cohérence, Complétude des métadonnées)           │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATASET FINAL                            │
│  (Split train/validation/test + statistiques)               │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 Procédure Détaillée

### Phase 1 : Préparation (1-2 heures)

#### Étape 1.1 — Créer l'arborescence

```bash
# À la racine du projet
mkdir -p dataset-finetuning/{raw,processed,train,metadata}
mkdir -p dataset-finetuning/raw/{sessions,summaries,prompts,decisions,archives,code}
mkdir -p dataset-finetuning/processed/{sessions,decisions,prompts,code,documentation}
```

**Structure attendue** :
```
dataset-finetuning/
├── raw/                          # Données brutes copiées
├── processed/                    # Données transformées (JSON intermédiaire)
├── train/                        # Dataset final (JSONL)
└── metadata/                     # Stats, rapports qualité
```

---

#### Étape 1.2 — Copier les données sources

```bash
# Sessions individuelles (29 fichiers)
cp .agents/sessions/*.md dataset-finetuning/raw/sessions/

# Résumés de sessions (13 fichiers)
cp .agents/archives/sessions_summaries/*.md dataset-finetuning/raw/summaries/

# Prompts de reprise (8 fichiers)
cp .agents/archives/prompts_archive/*.md dataset-finetuning/raw/prompts/

# Décisions architecturales (1 fichier)
cp .agents/DECISIONS_ARCHITECTURE.md dataset-finetuning/raw/decisions/

# Archives consolidées (5 fichiers)
cp .agents/archives/SESSIONS_*.md dataset-finetuning/raw/archives/
cp .agents/HISTORIQUE_*.md dataset-finetuning/raw/archives/

# Code source (optionnel, pour contexte)
find src -name "*.kt" -o -name "*.feature" | xargs cp --parents dataset-finetuning/raw/code/
```

---

#### Étape 1.3 — Installer les dépendances

**Python requis** (recommandé pour le parsing) :

```bash
# Créer un environnement virtuel
python3 -m venv dataset-env
source dataset-env/bin/activate

# Installer les dépendances
pip install markdown pyyaml python-frontmatter jsonlines tqdm
pip install beautifulsoup4 lxml  # Pour parsing HTML/Markdown complexe
pip install pytest black flake8  # Pour tests et qualité du code
```

**Fichier `requirements.txt`** :
```txt
markdown>=3.4.0
pyyaml>=6.0.0
python-frontmatter>=1.0.0
jsonlines>=3.1.0
tqdm>=4.65.0
beautifulsoup4>=4.12.0
lxml>=4.9.0
pytest>=7.4.0
black>=23.0.0
flake8>=6.1.0
```

---

### Phase 2 : Extraction (4-6 heures)

#### Étape 2.1 — Parser les sessions individuelles

**Script** : `scripts/extract_sessions.py`

**Entrée** : `.agents/sessions/*.md` (29 fichiers)  
**Sortie** : `dataset-finetuning/processed/sessions.json`

**Structure d'une session** :
```markdown
# Session 114 — Documentation Architecture

**Date** : 21 avril 2026  
**Statut** : ✅ TERMINÉE  
**Type** : docs/architecture

---

## 🎯 Mission

Session dédiée à la documentation...

---

## ✅ Résultats

### Fichiers Créés
| Fichier | Lignes | Rôle |
|---------|--------|------|
| `.agents/DECISIONS_ARCHITECTURE.md` | ~400 | 5 décisions |

### Fichiers Déplacés
| Fichier | Ancien | Nouveau |
|---------|--------|---------|
| `plantuml-test-context.yml` | Racine | `plantuml-plugin/` |
```

**Extraction** :
```python
import re
from pathlib import Path
import json
import frontmatter

def extract_session(filepath):
    """Extraire les métadonnées et contenu d'une session"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Extraire le numéro de session
    session_match = re.search(r'Session (\d+)', content)
    session_num = int(session_match.group(1)) if session_match else None
    
    # Extraire la date
    date_match = re.search(r'\*\*Date\*\* : (.+?)$', content, re.MULTILINE)
    date = date_match.group(1).strip() if date_match else None
    
    # Extraire le statut
    status_match = re.search(r'\*\*Statut\*\* : (.+?)$', content, re.MULTILINE)
    status = status_match.group(1).strip() if status_match else None
    
    # Extraire le type
    type_match = re.search(r'\*\*Type\*\* : (.+?)$', content, re.MULTILINE)
    session_type = type_match.group(1).strip() if type_match else None
    
    # Extraire les sections
    sections = {}
    for section in ['Mission', 'Résultats', 'Décisions', 'Fichiers Créés', 
                    'Fichiers Déplacés', 'Fichiers Modifiés', 'Leçons Apprises']:
        section_match = re.search(f'## {section}\\s*\\n(.*?)(?=##|$)', content, re.DOTALL)
        if section_match:
            sections[section] = section_match.group(1).strip()
    
    return {
        'session': session_num,
        'date': date,
        'status': status,
        'type': session_type,
        'content': content,
        'sections': sections,
        'source_file': str(filepath)
    }

# Traitement en batch
sessions_dir = Path('.agents/sessions')
sessions = []
for filepath in sessions_dir.glob('*.md'):
    session_data = extract_session(filepath)
    sessions.append(session_data)

# Sauvegarder
with open('dataset-finetuning/processed/sessions.json', 'w', encoding='utf-8') as f:
    json.dump(sessions, f, indent=2, ensure_ascii=False)

print(f"✅ {len(sessions)} sessions extraites")
```

**Résultat attendu** : 29 sessions extraites avec métadonnées complètes

---

#### Étape 2.2 — Parser les résumés de sessions

**Script** : `scripts/extract_summaries.py`

**Entrée** : `.agents/archives/sessions_summaries/*.md` (13 fichiers)  
**Sortie** : `dataset-finetuning/processed/summaries.json`

**Structure** :
```markdown
# 📊 Session 75 — Résumé

**Date** : 16 avr. 2026  
**EPIC** : EPIC 3 — Consolidation Tests Fonctionnels  
**Statut** : ✅ TERMINÉE

---

## 🎯 Mission

Correction de 2 tests échoués...

---

## ✅ Résultats

### Tests Fonctionnels (45 tests)
- ✅ 38 PASS (dont 1 corrigé)
- ⏭ 7 SKIP (tests cloud requis)
- ❌ 0 FAIL
```

**Extraction** : Similaire à Étape 2.1, adapter les regex pour le format `_SUMMARY.md`

---

#### Étape 2.3 — Parser les prompts de reprise

**Script** : `scripts/extract_prompts.py`

**Entrée** : `.agents/archives/prompts_archive/*.md` (8 fichiers)  
**Sortie** : `dataset-finetuning/processed/prompts.json`

**Structure** :
```markdown
# 🔄 Prompt de reprise — Session 65

> **EPIC** : Tests BDD Cucumber  
> **Statut** : Session 64 ✅ — Supprimer Fallbacks  
> **Mission** : Déboguer Tests RAG

---

## Contexte

Les tests Cucumber RAG échouent avec...

---

## Priorités

```bash
./gradlew cucumberTest --tests "*RagPipeline*"
```

### Critères d'Acceptation

- [ ] Tests RAG passent
- [ ] Mock LLM configuré
```

**Extraction** :
```python
def extract_prompt(filepath):
    """Extraire un prompt de reprise"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Extraire le numéro de session
    session_match = re.search(r'Session (\d+)', content)
    session_num = int(session_match.group(1)) if session_match else None
    
    # Extraire EPIC
    epic_match = re.search(r'\*\*EPIC\*\* : (.+?)$', content, re.MULTILINE)
    epic = epic_match.group(1).strip() if epic_match else None
    
    # Extraire Mission
    mission_match = re.search(r'\*\*Mission\*\* : (.+?)$', content, re.MULTILINE)
    mission = mission_match.group(1).strip() if mission_match else None
    
    # Extraire les sections
    sections = {}
    for section in ['Contexte', 'Priorités', 'Critères d\'Acceptation', 'Règles']:
        section_match = re.search(f'## {section}\\s*\\n(.*?)(?=##|$)', content, re.DOTALL)
        if section_match:
            sections[section] = section_match.group(1).strip()
    
    return {
        'session': session_num,
        'epic': epic,
        'mission': mission,
        'content': content,
        'sections': sections,
        'source_file': str(filepath)
    }
```

**Résultat attendu** : 8 prompts extraits avec contexte et priorités

---

#### Étape 2.4 — Parser les décisions architecturales

**Script** : `scripts/extract_decisions.py`

**Entrée** : `.agents/DECISIONS_ARCHITECTURE.md` (1 fichier, 5 décisions)  
**Sortie** : `dataset-finetuning/processed/decisions.json`

**Structure** :
```markdown
## 📁 Decision 001 — Emplacement de `plantuml-test-context.yml`

**Date** : 21 avril 2026  
**Statut** : ✅ ADOPTÉE  
**Alternative considérée** : 3 options

### Contexte

Le plugin PlantUML utilise des fichiers de configuration YAML...

### Alternatives Considérées

#### Option 1 : `src/test/resources/`
**Avantages** : ...
**Inconvénients** : ...
**Verdict** : ❌ Rejeté

#### Option 2 : `src/functionalTest/resources/`
**Avantages** : ...
**Inconvénients** : ...
**Verdict** : ❌ Rejeté

#### Option 3 : Racine du plugin (CHOISIE) ✅
**Avantages** : ...
**Inconvénients** : ...
**Verdict** : ✅ CHOISI

### Implémentation

```bash
mv plantuml-test-context.yml plantuml-plugin/
```

### Leçons Apprises

1. Ne pas modifier un build.gradle.kts fonctionnel...
```

**Extraction** :
```python
def extract_decisions(filepath):
    """Extraire les décisions architecturales"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    decisions = []
    
    # Trouver toutes les décisions
    decision_pattern = re.compile(
        r'## 📁 (Decision \d+) — (.+?)\n\n'
        r'\*\*Date\*\* : (.+?)\n'
        r'\*\*Statut\*\* : (.+?)\n'
        r'(.*?)\n\n'
        r'### Contexte\n\n(.*?)\n\n'
        r'### Alternatives Considérées\n\n(.*?)\n\n'
        r'### (?:Implémentation|Rationale)\n\n(.*?)\n\n'
        r'(?:### Leçons Apprises\n\n(.*?))?(?=\n\n## 📁 Decision|\Z)',
        re.DOTALL
    )
    
    for match in decision_pattern.finditer(content):
        decision = {
            'id': match.group(1),
            'sujet': match.group(2).strip(),
            'date': match.group(3).strip(),
            'status': match.group(4).strip(),
            'metadata': match.group(5).strip(),
            'context': match.group(6).strip(),
            'alternatives': match.group(7).strip(),
            'implementation': match.group(8).strip(),
            'lessons': match.group(9).strip() if match.group(9) else None,
            'source_file': str(filepath)
        }
        decisions.append(decision)
    
    return decisions
```

**Résultat attendu** : 5 décisions extraites avec alternatives et rationale

---

#### Étape 2.5 — Parser le code source (optionnel)

**Script** : `scripts/extract_code.py`

**Entrée** : `src/**/*.kt`, `src/**/*.feature`  
**Sortie** : `dataset-finetuning/processed/code.json`

**Extraction** :
```python
def extract_kotlin_file(filepath):
    """Extraire les informations d'un fichier Kotlin"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Extraire la KDoc (commentaire documentation)
    kdoc_match = re.search(r'/\*\*(.*?)\*/', content, re.DOTALL)
    kdoc = kdoc_match.group(1).strip() if kdoc_match else None
    
    # Extraire la classe/fonction principale
    class_match = re.search(r'(class|object|interface|fun)\s+(\w+)', content)
    main_element = class_match.group(2) if class_match else None
    element_type = class_match.group(1) if class_match else None
    
    # Extraire les imports
    imports = re.findall(r'import\s+(.+)', content)
    
    return {
        'file': str(filepath),
        'type': element_type,
        'name': main_element,
        'kdoc': kdoc,
        'imports': imports,
        'content': content,
        'line_count': len(content.split('\n'))
    }
```

**Résultat attendu** : ~50 fichiers Kotlin + ~15 fichiers Feature

---

### Phase 3 : Transformation (6-8 heures)

#### Étape 3.1 — Générer paires prompt/response pour sessions

**Script** : `scripts/generate_session_pairs.py`

**Entrée** : `dataset-finetuning/processed/sessions.json`  
**Sortie** : `dataset-finetuning/processed/sessions_pairs.jsonl`

**Template de génération** :

```python
def generate_session_pairs(sessions):
    """Générer des paires prompt/response pour les sessions"""
    pairs = []
    
    for session in sessions:
        # Type 1 : Question sur le résultat de la session
        prompt_type_1 = (
            f"Contexte : Session {session['session']} ({session['type']}).\n"
            f"Question : Quel était l'objectif et le résultat de cette session ?"
        )
        
        response_type_1 = (
            f"Session {session['session']} — {session.get('sections', {}).get('Mission', 'Non spécifié')}\n\n"
            f"**Résultat** : {session['status']}\n\n"
            f"**Fichiers créés** : {len(session.get('sections', {}).get('Fichiers Créés', '').split('\n'))}\n"
            f"**Fichiers modifiés** : {len(session.get('sections', {}).get('Fichiers Modifiés', '').split('\n'))}"
        )
        
        pairs.append({
            'id': f"session_{session['session']}_001",
            'session': session['session'],
            'type': 'session_summary',
            'prompt': prompt_type_1,
            'response': response_type_1,
            'metadata': {
                'date': session['date'],
                'status': session['status'],
                'source': session['source_file']
            }
        })
        
        # Type 2 : Question sur les leçons apprises
        if 'Leçons Apprises' in session.get('sections', {}):
            prompt_type_2 = (
                f"Contexte : Session {session['session']} — {session['type']}.\n"
                f"Question : Quelles leçons ont été apprises lors de cette session ?"
            )
            
            response_type_2 = session['sections']['Leçons Apprises']
            
            pairs.append({
                'id': f"session_{session['session']}_002",
                'session': session['session'],
                'type': 'lessons_learned',
                'prompt': prompt_type_2,
                'response': response_type_2,
                'metadata': {
                    'date': session['date'],
                    'source': session['source_file']
                }
            })
    
    return pairs

# Sauvegarder en JSONL
import jsonlines
with jsonlines.open('dataset-finetuning/processed/sessions_pairs.jsonl', 'w') as writer:
    writer.write_all(pairs)

print(f"✅ {len(pairs)} paires générées")
```

**Résultat attendu** : ~60 paires prompt/response (2 par session en moyenne)

---

#### Étape 3.2 — Générer paires prompt/response pour décisions

**Script** : `scripts/generate_decision_pairs.py`

**Entrée** : `dataset-finetuning/processed/decisions.json`  
**Sortie** : `dataset-finetuning/processed/decisions_pairs.jsonl`

**Template** :

```python
def generate_decision_pairs(decisions):
    """Générer des paires prompt/response pour les décisions"""
    pairs = []
    
    for decision in decisions:
        # Type 1 : Question sur la décision
        prompt = (
            f"Contexte : {decision['context'][:200]}...\n"
            f"Question : Quelle décision a été prise et pourquoi ?"
        )
        
        response = (
            f"**Décision** : {decision['sujet']}\n\n"
            f"**Statut** : {decision['status']}\n\n"
            f"**Rationale** : {decision['implementation'][:500]}\n\n"
            f"**Alternatives rejetées** : Voir {decision['id']}"
        )
        
        pairs.append({
            'id': f"decision_{decision['id'].replace(' ', '_').lower()}",
            'type': 'architecture_decision',
            'prompt': prompt,
            'response': response,
            'metadata': {
                'date': decision['date'],
                'status': decision['status'],
                'source': decision['source_file']
            }
        })
        
        # Type 2 : Question sur les alternatives
        prompt_alt = (
            f"Contexte : {decision['sujet']}.\n"
            f"Question : Quelles alternatives ont été considérées et pourquoi rejetées ?"
        )
        
        response_alt = decision['alternatives']
        
        pairs.append({
            'id': f"decision_{decision['id'].replace(' ', '_').lower()}_alt",
            'type': 'architecture_alternatives',
            'prompt': prompt_alt,
            'response': response_alt,
            'metadata': {
                'date': decision['date'],
                'source': decision['source_file']
            }
        })
    
    return pairs
```

**Résultat attendu** : ~10 paires (2 par décision)

---

#### Étape 3.3 — Générer paires prompt/response pour prompts de reprise

**Script** : `scripts/generate_prompt_pairs.py`

**Entrée** : `dataset-finetuning/processed/prompts.json`  
**Sortie** : `dataset-finetuning/processed/prompt_reprise_pairs.jsonl`

**Template** :

```python
def generate_prompt_pairs(prompts):
    """Générer des paires prompt/response pour les prompts de reprise"""
    pairs = []
    
    for prompt in prompts:
        # Type 1 : Question sur la mission
        prompt_text = (
            f"Contexte : EPIC {prompt['epic']}, Session {prompt['session']}.\n"
            f"Question : Quelle est la mission de la session {prompt['session']} ?"
        )
        
        response_text = (
            f"**Mission** : {prompt['mission']}\n\n"
            f"**Contexte** : {prompt['sections'].get('Contexte', 'Non spécifié')}\n\n"
            f"**Priorités** : {prompt['sections'].get('Priorités', 'Non spécifié')}"
        )
        
        pairs.append({
            'id': f"prompt_reprise_{prompt['session']}",
            'session': prompt['session'],
            'type': 'session_mission',
            'prompt': prompt_text,
            'response': response_text,
            'metadata': {
                'epic': prompt['epic'],
                'source': prompt['source_file']
            }
        })
    
    return pairs
```

**Résultat attendu** : ~8 paires (1 par prompt)

---

#### Étape 3.4 — Générer paires prompt/response pour le code

**Script** : `scripts/generate_code_pairs.py`

**Entrée** : `dataset-finetuning/processed/code.json`  
**Sortie** : `dataset-finetuning/processed/code_pairs.jsonl`

**Template** :

```python
def generate_code_pairs(code_files):
    """Générer des paires prompt/response pour le code"""
    pairs = []
    
    for code in code_files:
        if code['kdoc']:  # Seulement si documentation présente
            # Type 1 : Question sur le but du fichier
            prompt = (
                f"Contexte : Fichier {code['file']} ({code['type']} {code['name']}).\n"
                f"Question : Quel est le but de ce fichier ?"
            )
            
            response = (
                f"**Fichier** : {code['file']}\n\n"
                f"**Type** : {code['type']} {code['name']}\n\n"
                f"**Description** : {code['kdoc']}\n\n"
                f"**Lignes** : {code['line_count']}"
            )
            
            pairs.append({
                'id': f"code_{Path(code['file']).stem}",
                'type': 'code_documentation',
                'prompt': prompt,
                'response': response,
                'metadata': {
                    'file': code['file'],
                    'element_type': code['type'],
                    'element_name': code['name']
                }
            })
    
    return pairs
```

**Résultat attendu** : ~30 paires (fichiers avec KDoc)

---

### Phase 4 : Consolidation (2-3 heures)

#### Étape 4.1 — Fusionner toutes les paires

**Script** : `scripts/consolidate_dataset.py`

**Entrée** : Tous les fichiers `*_pairs.jsonl`  
**Sortie** : `dataset-finetuning/train/all_pairs.jsonl`

```python
import jsonlines
from pathlib import Path

def consolidate_dataset():
    """Fusionner toutes les paires en un seul dataset"""
    all_pairs = []
    
    processed_dir = Path('dataset-finetuning/processed')
    for filepath in processed_dir.glob('*_pairs.jsonl'):
        with jsonlines.open(filepath, 'r') as reader:
            for pair in reader:
                all_pairs.append(pair)
    
    # Mélanger aléatoirement
    import random
    random.shuffle(all_pairs)
    
    # Sauvegarder
    with jsonlines.open('dataset-finetuning/train/all_pairs.jsonl', 'w') as writer:
        writer.write_all(all_pairs)
    
    print(f"✅ {len(all_pairs)} paires consolidées")
    return all_pairs
```

---

#### Étape 4.2 — Split train/validation/test

**Script** : `scripts/split_dataset.py`

**Entrée** : `dataset-finetuning/train/all_pairs.jsonl`  
**Sortie** : `train.jsonl`, `validation.jsonl`, `test.jsonl`

```python
def split_dataset(input_file, train_ratio=0.8, val_ratio=0.1, test_ratio=0.1):
    """Split le dataset en train/validation/test"""
    import jsonlines
    import random
    
    # Lire toutes les paires
    with jsonlines.open(input_file, 'r') as reader:
        all_pairs = list(reader)
    
    # Mélanger
    random.shuffle(all_pairs)
    
    # Calculer les tailles
    n = len(all_pairs)
    train_size = int(n * train_ratio)
    val_size = int(n * val_ratio)
    
    # Split
    train_pairs = all_pairs[:train_size]
    val_pairs = all_pairs[train_size:train_size + val_size]
    test_pairs = all_pairs[train_size + val_size:]
    
    # Sauvegarder
    with jsonlines.open('dataset-finetuning/train/train.jsonl', 'w') as f:
        f.write_all(train_pairs)
    
    with jsonlines.open('dataset-finetuning/train/validation.jsonl', 'w') as f:
        f.write_all(val_pairs)
    
    with jsonlines.open('dataset-finetuning/train/test.jsonl', 'w') as f:
        f.write_all(test_pairs)
    
    print(f"✅ Split : {len(train_pairs)} train, {len(val_pairs)} val, {len(test_pairs)} test")
```

**Répartition attendue** (pour ~120 paires) :
- Train : ~96 exemples (80%)
- Validation : ~12 exemples (10%)
- Test : ~12 exemples (10%)

---

#### Étape 4.3 — Générer les statistiques

**Script** : `scripts/generate_statistics.py`

**Entrée** : `dataset-finetuning/train/*.jsonl`  
**Sortie** : `dataset-finetuning/metadata/statistics.json`

```python
def generate_statistics():
    """Générer les statistiques du dataset"""
    import jsonlines
    from collections import Counter
    from pathlib import Path
    
    stats = {
        'total_examples': 0,
        'by_type': Counter(),
        'by_session': Counter(),
        'avg_prompt_length': 0,
        'avg_response_length': 0,
        'total_tokens_estimate': 0
    }
    
    total_prompt_len = 0
    total_response_len = 0
    
    # Lire train.jsonl
    with jsonlines.open('dataset-finetuning/train/train.jsonl', 'r') as f:
        for pair in f:
            stats['total_examples'] += 1
            stats['by_type'][pair.get('type', 'unknown')] += 1
            stats['by_session'][str(pair.get('session', 'unknown'))] += 1
            total_prompt_len += len(pair.get('prompt', ''))
            total_response_len += len(pair.get('response', ''))
    
    # Calculer les moyennes
    if stats['total_examples'] > 0:
        stats['avg_prompt_length'] = total_prompt_len / stats['total_examples']
        stats['avg_response_length'] = total_response_len / stats['total_examples']
        stats['total_tokens_estimate'] = (total_prompt_len + total_response_len) // 4  # Approximation
    
    # Sauvegarder
    import json
    with open('dataset-finetuning/metadata/statistics.json', 'w', encoding='utf-8') as f:
        json.dump(stats, f, indent=2, ensure_ascii=False)
    
    print(f"✅ Statistiques générées : {stats['total_examples']} exemples")
    return stats
```

---

### Phase 5 : Validation (2-3 heures)

#### Étape 5.1 — Vérifier la qualité des paires

**Script** : `scripts/validate_quality.py`

**Critères de validation** :
- ✅ Prompt non vide
- ✅ Response non vide
- ✅ Longueur minimale (50 caractères)
- ✅ Longueur maximale (2000 caractères)
- ✅ Pas de caractères spéciaux problématiques
- ✅ Métadonnées complètes

```python
def validate_quality():
    """Valider la qualité des paires"""
    import jsonlines
    from pathlib import Path
    
    issues = []
    
    with jsonlines.open('dataset-finetuning/train/train.jsonl', 'r') as f:
        for i, pair in enumerate(f):
            # Vérifier prompt
            if not pair.get('prompt'):
                issues.append(f"Ligne {i}: Prompt vide")
            elif len(pair['prompt']) < 50:
                issues.append(f"Ligne {i}: Prompt trop court ({len(pair['prompt'])} chars)")
            elif len(pair['prompt']) > 2000:
                issues.append(f"Ligne {i}: Prompt trop long ({len(pair['prompt'])} chars)")
            
            # Vérifier response
            if not pair.get('response'):
                issues.append(f"Ligne {i}: Response vide")
            elif len(pair['response']) < 50:
                issues.append(f"Ligne {i}: Response trop court")
            
            # Vérifier métadonnées
            if 'type' not in pair:
                issues.append(f"Ligne {i}: Type manquant")
            if 'id' not in pair:
                issues.append(f"Ligne {i}: ID manquant")
    
    # Sauvegarder le rapport
    with open('dataset-finetuning/metadata/quality_report.md', 'w', encoding='utf-8') as f:
        f.write("# Rapport de Qualité du Dataset\n\n")
        f.write(f"**Total issues** : {len(issues)}\n\n")
        if issues:
            f.write("## Issues détectées\n\n")
            for issue in issues[:50]:  # Limiter à 50
                f.write(f"- {issue}\n")
    
    print(f"✅ Validation : {len(issues)} issues détectées")
    return issues
```

---

#### Étape 5.2 — Vérifier la diversité

**Script** : `scripts/validate_diversity.py`

**Critères** :
- ✅ 5 catégories représentées
- ✅ Sessions variées (pas de biais vers sessions récentes)
- ✅ Types de prompts variés (questions, code, décisions)

```python
def validate_diversity():
    """Valider la diversité du dataset"""
    import jsonlines
    from collections import Counter
    
    types = Counter()
    sessions = Counter()
    
    with jsonlines.open('dataset-finetuning/train/train.jsonl', 'r') as f:
        for pair in f:
            types[pair.get('type', 'unknown')] += 1
            sessions[str(pair.get('session', 'unknown'))] += 1
    
    # Rapport
    report = []
    report.append("# Rapport de Diversité\n\n")
    report.append("## Par Type\n\n")
    for type_name, count in types.most_common():
        report.append(f"- {type_name}: {count} ({count/sum(types.values())*100:.1f}%)")
    
    report.append("\n## Par Session\n\n")
    report.append(f"- Sessions uniques: {len(sessions)}")
    report.append(f"- Sessions avec >5 exemples: {sum(1 for c in sessions.values() if c > 5)}")
    
    with open('dataset-finetuning/metadata/diversity_report.md', 'w', encoding='utf-8') as f:
        f.write('\n'.join(report))
    
    print(f"✅ Diversité : {len(types)} types, {len(sessions)} sessions")
```

---

### Phase 6 : Documentation (1 heure)

#### Étape 6.1 — Créer le README du dataset

**Fichier** : `dataset-finetuning/README.md`

```markdown
# Dataset Fine-tuning — PlantUML Gradle Plugin

**Objectif** : Entraîner un agent IA spécialisé en développement de plugins Gradle

**Taille** : ~120 exemples (train + validation + test)  
**Format** : JSONL  
**Langue** : Français (prompts et responses)  
**Période** : Avril 2026 (115 sessions)

## Structure

```
dataset-finetuning/
├── train/
│   ├── train.jsonl (80%)
│   ├── validation.jsonl (10%)
│   └── test.jsonl (10%)
├── metadata/
│   ├── statistics.json
│   ├── quality_report.md
│   └── diversity_report.md
└── README.md
```

## Catégories

| Type | Exemples | Pourcentage |
|------|----------|-------------|
| session_summary | ~30 | 25% |
| architecture_decision | ~10 | 8% |
| session_mission | ~8 | 7% |
| code_documentation | ~30 | 25% |
| lessons_learned | ~30 | 25% |
| autres | ~12 | 10% |

## Utilisation

```python
import jsonlines

with jsonlines.open('train/train.jsonl', 'r') as f:
    for pair in f:
        prompt = pair['prompt']
        response = pair['response']
        # Entraîner le modèle...
```

## Qualité

- ✅ Toutes les paires validées
- ✅ Métadonnées complètes
- ✅ Diversité des sessions
- ✅ Équilibre des types

## Licence

Apache 2.0 — Voir LICENSE
```

---

## 📊 Métriques Cibles

| Métrique | Cible | Acceptable |
|----------|-------|------------|
| Total exemples | 100+ | 50+ |
| Qualité (issues) | < 5% | < 10% |
| Diversité (types) | 5+ | 3+ |
| Diversité (sessions) | 20+ | 10+ |
| Split (train/val/test) | 80/10/10 | 70/15/15 |
| Tokens totaux | 500k+ | 200k+ |

---

## ⏱ Timeline Estimée

| Phase | Durée | Priorité |
|-------|-------|----------|
| 1. Préparation | 1-2 heures | ✅ Haute |
| 2. Extraction | 4-6 heures | ✅ Haute |
| 3. Transformation | 6-8 heures | ✅ Haute |
| 4. Consolidation | 2-3 heures | ✅ Haute |
| 5. Validation | 2-3 heures | ✅ Haute |
| 6. Documentation | 1 heure | 🟡 Moyenne |
| **Total** | **16-23 heures** | |

---

## ✅ Checklist Finale

### Avant de commencer

- [ ] Environnement Python configuré
- [ ] Dépendances installées
- [ ] Arborescence créée
- [ ] Données sources copiées

### Extraction

- [ ] Sessions extraites (29 fichiers)
- [ ] Résumés extraits (13 fichiers)
- [ ] Prompts extraits (8 fichiers)
- [ ] Décisions extraites (5 décisions)
- [ ] Code extrait (~50 fichiers)

### Transformation

- [ ] Paires sessions générées (~60)
- [ ] Paires décisions générées (~10)
- [ ] Paires prompts générées (~8)
- [ ] Paires code générées (~30)
- [ ] Toutes paires fusionnées

### Validation

- [ ] Split train/val/test effectué
- [ ] Statistiques générées
- [ ] Qualité validée (< 10% issues)
- [ ] Diversité validée (5+ types)
- [ ] README créé

### Prêt pour fine-tuning

- [ ] Dataset uploadé (optionnel)
- [ ] Tests de fine-tuning effectués
- [ ] Résultats documentés

---

**Document créé** : 21 avril 2026 (Session 115)  
**Prochaine mise à jour** : Après première exécution complète  
**Maintenu par** : Agent OpenCode
