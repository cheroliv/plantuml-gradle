#!/bin/bash

# Script de benchmark pour les tests PlantUML
# Mesure le temps d'exécution des différents types de tests

set -e

echo "=== Benchmark des tests PlantUML ==="
echo "Date: $(date)"
echo "Répertoire: $(pwd)"
echo ""

# Fonction pour mesurer le temps d'exécution
time_command() {
    local description="$1"
    local command="$2"
    
    echo "🚀 $description"
    local start_time=$(date +%s.%N)
    eval "$command" > /dev/null 2>&1
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc)
    printf "⏱️  Temps: %.2f secondes\\n\\n" $duration
}

# Nettoyage avant les tests
echo "🧹 Nettoyage..."
./gradlew clean > /dev/null 2>&1

# Benchmark des différents types de tests
echo "📊 Début des benchmarks..."

time_command "Tests unitaires" "./gradlew test"
time_command "Tests fonctionnels" "./gradlew functionalTest"
time_command "Tests Cucumber" "./gradlew cucumberTest"
time_command "Tests LLM (configuration)" "./gradlew functionalTest --tests \"*LlmConfiguration*\""
time_command "Tests optimisés" "./gradlew functionalTest --tests \"*Optimized*\""

echo "✅ Benchmark terminé!"
echo ""
echo "💡 Conseils d'optimisation:"
echo "  - Utilisez --parallel pour exécuter les tests en parallèle"
echo "   - Activez le cache Gradle avec --build-cache"
echo "   - Utilisez --daemon pour réutiliser le processus Gradle"
echo "   - Configurez le nombre de workers avec org.gradle.workers.max"