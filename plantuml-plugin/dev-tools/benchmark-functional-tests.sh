#!/bin/bash

echo "=== Benchmark des Tests Fonctionnels ==="
echo

cd plantuml-plugin

# Fonction pour mesurer le temps d'exécution
time_test_execution() {
    local test_name=$1
    local test_filter=$2
    
    echo "Exécution des tests ($test_name)..."
    
    START_TIME=$(date +%s%N)
    ./gradlew functionalTest --tests "$test_filter" --rerun-tasks --quiet >/dev/null 2>&1
    END_TIME=$(date +%s%N)
    
    # Calculer la durée en millisecondes
    duration=$((($END_TIME - $START_TIME)/1000000))
    echo "Durée: $duration ms"
    echo
}

# Exécuter les tests fonctionnels originaux
echo "Tests fonctionnels ORIGINAUX:"
time_test_execution "Plugin Functional Test" "*PlantumlPluginFunctionalTest"

# Exécuter les tests fonctionnels optimisés
echo "Tests fonctionnels OPTIMISÉS:"
time_test_execution "Optimized Plugin Functional Test" "*OptimizedPlantumlPluginFunctionalTest"

# Exécuter les tests LLM fonctionnels
echo "Tests fonctionnels LLM:"
time_test_execution "LLM Configuration Functional Test" "*LlmConfigurationFunctionalTest"

echo "=== Fin du benchmark ==="