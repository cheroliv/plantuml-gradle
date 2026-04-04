# Prompt de démarrage pour la prochaine session Opencode

## Contexte actuel
Suite à l'optimisation des tests du plugin PlantUML Gradle avec WireMock, nous avons :
- Configuré WireMock pour mocker les appels LLM par défaut dans les tests
- Modifié LlmConfigurationTest pour utiliser WireMock avec port dynamique
- Implémenté option système pour activer vrais LLM via ligne de commande (-Dtest.use.real.llm=true)
- Validé que les tests Ollama passent avec succès utilisant WireMock
- Créé des mappings WireMock basés sur les réponses réelles du modèle SmolLM
- Mis à jour la documentation dans AGENTS.md

## Résultats du benchmark
Benchmark effectué pour comparer les performances :
- Tests avec mocks : ~19.8 secondes
- Tests avec vrais LLM : ~19.7 secondes

La différence de performance est négligeable, ce qui signifie que les gains de vitesse apportés par les mocks sont minimes dans ce cas précis.

## Prochaines étapes prioritaires

### 🎯 1. Analyser les tests les plus lents
- Identifier quels tests spécifiques prennent le plus de temps
- Profiler les performances des différents tests unitaires et fonctionnels
- Déterminer si la lenteur vient des appels LLM, du traitement PlantUML ou d'autres facteurs

### 2. Optimiser les performances des tests
- Appliquer des optimisations ciblées sur les tests les plus lents
- Explorer d'autres moyens d'accélérer les tests (cache, parallélisation, etc.)

### 3. Activer progressivement les autres configurations LLM
- Retirer @Ignore des tests pour Gemini, Mistral, Claude, etc.
- Valider que les mappings WireMock fonctionnent pour tous les fournisseurs

### 4. Améliorer le rapport de benchmark
- Créer un script de benchmark plus détaillé avec des mesures par test
- Générer des rapports comparatifs lisibles

## Questions à explorer
1. Pourquoi la différence de performance entre mocks et vrais LLM est-elle si faible ?
2. Quels sont les autres facteurs qui ralentissent les tests ?
3. Comment pouvons-nous encore réduire le temps d'exécution global des tests ?