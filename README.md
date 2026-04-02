# PlantUML Gradle Plugin

[![Build](https://github.com/cheroliv/plantuml-gradle/actions/workflows/build.yml/badge.svg)](https://github.com/cheroliv/plantuml-gradle/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Description

Le plugin PlantUML Gradle permet de générer des diagrammes PlantUML à partir de prompts texte en utilisant l'intelligence artificielle. Il s'appuie sur LangChain4j pour interagir avec différents modèles de langage (LLM) et intègre un système de RAG (Retrieval Augmented Generation) pour améliorer la qualité des diagrammes générés.

## Fonctionnalités

- ✅ Génération de diagrammes PlantUML à partir de prompts texte
- ✅ Support de multiples fournisseurs LLM : Ollama, OpenAI, Gemini, Mistral, Claude, HuggingFace, Groq
- ✅ Validation automatique de la syntaxe PlantUML
- ✅ Génération d'images à partir des diagrammes valides
- ✅ Indexation RAG pour l'apprentissage continu
- ✅ Intégration avec Git pour le versioning des diagrammes

## Installation

Dans votre fichier `build.gradle.kts` :

```kotlin
plugins {
    id("com.cheroliv.plantuml") version "1.2026.0"
}
```

## Configuration

Créez un fichier `plantuml-context.yml` à la racine de votre projet :

```yaml
input:
  prompts: "prompts"
  defaultLang: "en"

output:
  diagrams: "generated/diagrams"
  images: "generated/images"
  validations: "generated/validations"
  rag: "generated/rag"
  format: "png"
  theme: "default"

langchain:
  maxIterations: 5
  model: "ollama"  # Peut être : ollama, gemini, mistral, openai, claude, huggingface, groq
  validation: true
  validationPrompt: "Notez ce diagramme sur la clarté, l'exhaustivité et les bonnes pratiques. Retournez un JSON avec 'score' (1-10) et 'feedback' (chaîne) et 'recommendations' (tableau)."
  
  ollama:
    baseUrl: "http://localhost:11434"
    modelName: "smollm:135m"
  
  gemini:
    apiKey: "votre-clé-api-gemini-ici"
    
  mistral:
    apiKey: "votre-clé-api-mistral-ici"
    
  openai:
    apiKey: "votre-clé-api-openai-ici"
    
  claude:
    apiKey: "votre-clé-api-claude-ici"
    
  huggingface:
    apiKey: "votre-clé-api-huggingface-ici"
    
  groq:
    apiKey: "votre-clé-api-groq-ici"

git:
  userName: "github-actions[bot]"
  userEmail: "github-actions[bot]@users.noreply.github.com"
  commitMessage: "chore: mise à jour des diagrammes PlantUML [skip ci]"
  watchedBranches: 
    - "main"
    - "develop"

rag:
  databaseUrl: "jdbc:postgresql://localhost:5432/plantuml_rag"
  username: "plantuml_user"
  password: "plantuml_password"
  tableName: "plantuml_embeddings"
```

## Utilisation

### Tâches disponibles

1. **processPlantumlPrompts** - Génère des diagrammes à partir des fichiers .prompt
   ```bash
   ./gradlew processPlantumlPrompts
   ```

2. **validatePlantumlSyntax** - Valide la syntaxe d'un diagramme PlantUML
   ```bash
   ./gradlew validatePlantumlSyntax -Pplantuml.diagram=fichier.puml
   ```

3. **reindexPlantumlRag** - Réindexe les diagrammes pour le RAG
   ```bash
   ./gradlew reindexPlantumlRag
   ```

### Workflow typique

1. Créez des fichiers `.prompt` dans le répertoire `prompts/`
2. Exécutez `./gradlew processPlantumlPrompts`
3. Les diagrammes générés seront sauvegardés dans `generated/diagrams/`
4. Les images seront générées dans `generated/images/`
5. Les validations seront sauvegardées dans `generated/validations/`
6. Les diagrammes valides seront indexés pour le RAG dans `generated/rag/`

## Support des fournisseurs LLM

| Fournisseur | Modèle par défaut | Configuration |
|-------------|-------------------|---------------|
| Ollama | smollm:135m | `langchain.ollama.baseUrl`, `langchain.ollama.modelName` |
| OpenAI | gpt-4 | `langchain.openai.apiKey` |
| Gemini | gemini-pro | `langchain.gemini.apiKey` |
| Mistral | mistral-large-latest | `langchain.mistral.apiKey` |
| Claude | claude-3-opus-20240229 | `langchain.claude.apiKey` |
| HuggingFace | bigscience/bloom | `langchain.huggingface.apiKey` |
| Groq | - | `langchain.groq.apiKey` |

## Configuration RAG

Pour activer le stockage vectoriel RAG, configurez une base de données PostgreSQL avec l'extension pgvector :

1. Installez PostgreSQL avec l'extension pgvector
2. Configurez les propriétés de connexion dans `plantuml-context.yml`
3. Exécutez `./gradlew reindexPlantumlRag` pour indexer les diagrammes

## Développement

### Construction du projet

```bash
./gradlew build
```

### Exécution des tests

```bash
./gradlew test
```

### Publication locale

```bash
./gradlew publishToMavenLocal
```

## Licence

Ce projet est sous licence Apache 2.0. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## Contribution

Les contributions sont les bienvenues ! Veuillez lire le fichier [CONTRIBUTING.md](CONTRIBUTING.md) pour plus d'informations.