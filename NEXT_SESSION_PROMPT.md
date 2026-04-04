# Prompt de démarrage pour la prochaine session Opencode

## Contexte actuel
Suite aux optimisations effectuées sur les tests du plugin PlantUML Gradle :

### ✅ Accomplissements
1. **Optimisation des tests unitaires LLM**
   - Refactorisation de `LlmConfigurationTest` pour séparer les tests unitaires des tests fonctionnels
   - Utilisation de `ProjectBuilder` au lieu de `GradleRunner` pour les tests unitaires
   - Réduction du temps d'exécution de ~17s à ~7s (gain de 60%)

2. **Correction du problème WireMock**
   - Copie des mappings WireMock vers le répertoire des tests fonctionnels
   - Résolution de l'erreur "No response could be served as there are no stub mappings"
   - Tous les tests LLM fonctionnels passent maintenant

3. **Optimisation des tests fonctionnels**
   - Création d'une version optimisée `OptimizedPlantumlPluginFunctionalTest`
   - Réduction du temps d'exécution de ~28% (de 14s à 10s)

### 🔧 Configuration actuelle
- Tests unitaires : ~7 secondes
- Tests fonctionnels : ~10 secondes (optimisés)
- Tests LLM fonctionnels : Passent tous correctement

## Objectifs prioritaires pour la prochaine session

### 🎯 1. Réduction supplémentaire du temps des tests fonctionnels
- Expliquer pourquoi certaines méthodes d'optimisation n'ont pas fonctionné (dry-run, etc.)
- Trouver des moyens supplémentaires pour accélérer les tests fonctionnels
- Appliquer les mêmes optimisations à tous les tests fonctionnels

### 2. Activation progressive des configurations LLM ignorées
- Retirer `@Ignore` des tests LLM pour Gemini, Mistral, Claude, etc.
- Valider que tous les mappings WireMock fonctionnent correctement
- Assurer une couverture complète des différentes configurations

### 3. Amélioration du rapport de benchmark
- Créer un script de benchmark plus détaillé avec des mesures par test
- Générer des rapports comparatifs lisibles
- Documenter les techniques d'optimisation pour les nouveaux développeurs

### 4. Documentation des optimisations
- Mettre à jour la documentation développeur avec les nouvelles pratiques
- Expliquer la séparation entre tests unitaires et fonctionnels
- Documenter l'utilisation optimale de WireMock

## Questions techniques à explorer

1. **Pourquoi `--dry-run` ne montre pas toutes les tâches dans les tests fonctionnels ?**
2. **Comment utiliser efficacement le cache de configuration de Gradle pour les tests ?**
3. **Quelles sont les limites de regrouper plusieurs vérifications dans un seul test fonctionnel ?**
4. **Comment équilibrer rapidité des tests vs granularité des vérifications ?**