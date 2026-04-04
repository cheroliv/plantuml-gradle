#!/bin/bash

echo "=== Benchmark des Tests avec Mocks vs Vrais LLM ==="
echo

cd plantuml-plugin

# Fonction pour mesurer le temps d'exécution
time_test_execution() {
    local test_type=$1
    local use_real_llm=$2
    
    echo "Exécution des tests ($test_type)..."
    
    if [ "$use_real_llm" = "true" ]; then
        START_TIME=$(date +%s%N)
        ./gradlew test --tests "*LlmConfigurationTest*" -Ptest.use.real.llm=true --rerun-tasks --quiet >/dev/null 2>&1
        END_TIME=$(date +%s%N)
        echo "Tests avec vrai LLM terminés"
    else
        START_TIME=$(date +%s%N)
        ./gradlew test --tests "*LlmConfigurationTest*" --rerun-tasks --quiet >/dev/null 2>&1
        END_TIME=$(date +%s%N)
        echo "Tests avec mocks terminés"
    fi
    
    # Calculer la durée en millisecondes
    duration=$((($END_TIME - $START_TIME)/1000000))
    echo "Durée: $duration ms"
    echo
}

# Exécuter les tests avec mocks
time_test_execution "Mocks" "false"

# Exécuter les tests avec vrais LLM
time_test_execution "Vrais LLM" "true"

echo "=== Fin du benchmark ==="