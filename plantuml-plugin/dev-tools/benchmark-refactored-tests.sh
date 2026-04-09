#!/bin/bash

echo "=== Benchmark des Tests Refactorés ==="
echo

cd plantuml-plugin

# Fonction pour mesurer le temps d'exécution
time_test_execution() {
    local test_type=$1
    local test_task=$2
    
    echo "Exécution des tests ($test_type)..."
    
    START_TIME=$(date +%s%N)
    ./gradlew $test_task --tests "*" --rerun-tasks --quiet >/dev/null 2>&1
    END_TIME=$(date +%s%N)
    
    # Calculer la durée en millisecondes
    duration=$((($END_TIME - $START_TIME)/1000000))
    echo "Durée: $duration ms"
    echo
}

# Exécuter les tests unitaires
time_test_execution "Unit Tests" "test --tests *LlmConfigurationTest"

# Exécuter les tests fonctionnels
time_test_execution "Functional Tests" "functionalTest --tests *LlmConfigurationFunctionalTest"

echo "=== Fin du benchmark ==="