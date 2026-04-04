#!/bin/bash

# Script pour tester le handshake avec les fournisseurs LLM
# sans aller jusqu'à l'authentification complète

echo "=== Test du Handshake LLM ==="
echo

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour tester la connectivité avec un endpoint
test_endpoint_connectivity() {
    local url=$1
    local name=$2
    
    echo "Test de connectivité avec $name ($url)..."
    
    # Tester uniquement le handshake initial (connexion TCP)
    if command -v curl &> /dev/null; then
        # Tentative de connexion rapide (5 secondes timeout)
        if curl -s --connect-timeout 5 "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ Handshake réussi avec $name${NC}"
            return 0
        else
            echo -e "${YELLOW}⚠ Handshake échoué avec $name (mais cela peut être normal dans certains contexts de test)${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠ curl non disponible, impossible de tester la connectivité${NC}"
        return 1
    fi
}

# Tester les endpoints LLM courants
echo "Test des endpoints LLM..."
echo

# Ollama (local)
test_endpoint_connectivity "http://localhost:11434/api/tags" "Ollama Local"

# OpenAI
test_endpoint_connectivity "https://api.openai.com/v1/models" "OpenAI"

# Gemini
test_endpoint_connectivity "https://generativelanguage.googleapis.com" "Gemini"

# Mistral
test_endpoint_connectivity "https://api.mistral.ai/v1/models" "Mistral"

# Claude (Anthropic)
test_endpoint_connectivity "https://api.anthropic.com/v1/messages" "Claude"

echo
echo "=== Fin des tests de handshake ==="
echo

# Instructions pour exécuter les tests Gradle avec paramètre LLM
echo "Pour exécuter les tests Gradle avec un paramètre LLM spécifique :"
echo "./gradlew processPlantumlPrompts -Pplantuml.langchain.model=ollama"
echo "./gradlew processPlantumlPrompts -Pplantuml.langchain.model=gemini"
echo "./gradlew processPlantumlPrompts -Pplantuml.langchain.model=mistral"
echo

# Créer un fichier de configuration de test si nécessaire
TEST_CONFIG_FILE="ollama-local-smollm-135.yaml"
if [ ! -f "$TEST_CONFIG_FILE" ]; then
    echo "Création du fichier de configuration de test : $TEST_CONFIG_FILE"
    cat > "$TEST_CONFIG_FILE" << EOF
langchain:
  model: "ollama"
  ollama:
    baseUrl: "http://localhost:11434"
    modelName: "smollm:135m"
  validation: false
  
input:
  prompts: "test/prompts"
  
output:
  diagrams: "test/generated/diagrams"
  images: "test/generated/images"
  validations: "test/generated/validations"
  rag: "test/generated/rag"
EOF
    echo -e "${GREEN}✓ Fichier de configuration de test créé${NC}"
fi