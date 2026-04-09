#!/bin/bash

# Script pour configurer Ollama avec le modèle SmolLM 135M
# Utilisé pour accélérer les tests d'intégration

set -e

OLLAMA_MODEL="smollm:135m"

echo "Vérification de la présence du modèle $OLLAMA_MODEL..."

# Vérifier si le modèle est déjà présent
if ollama list | grep -q "$OLLAMA_MODEL"; then
  echo "Modèle $OLLAMA_MODEL déjà disponible."
else
  echo "Téléchargement du modèle $OLLAMA_MODEL..."
  ollama pull "$OLLAMA_MODEL"
  echo "Modèle $OLLAMA_MODEL téléchargé avec succès."
fi

echo "Ollama est configuré avec le modèle $OLLAMA_MODEL"