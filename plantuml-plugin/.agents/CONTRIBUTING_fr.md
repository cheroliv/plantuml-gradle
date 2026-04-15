# Contribuer au plugin PlantUML Gradle

> <img src="images/lang-en-blue.svg" alt="English" title="English" width="16" height="12"/> [English version](CONTRIBUTING.md)

Merci de votre intérêt pour contribuer au plugin PlantUML Gradle ! Ce guide vous expliquera comment participer au développement de ce projet.

## Code de conduite

En participant à ce projet, vous acceptez de respecter notre [Code de conduite](CODE_OF_CONDUCT.md).

## Comment contribuer

### Signaler des bugs

Si vous trouvez un bug, veuillez créer une issue en incluant :
- Une description claire et concise du problème
- Les étapes pour reproduire le bug
- La version du plugin utilisée
- Votre environnement (OS, version de Gradle, etc.)

### Proposer des améliorations

Vous pouvez proposer des améliorations en créant une issue avec :
- Une description détaillée de l'amélioration proposée
- Les bénéfices attendus
- Toute information pertinente sur l'implémentation

### Soumettre des corrections

Pour soumettre des corrections de code :

1. Fork le dépôt
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/nom-de-la-fonctionnalite`)
3. Commitez vos changements (`git commit -am 'Ajout d'une nouvelle fonctionnalité'`)
4. Poussez vers la branche (`git push origin feature/nom-de-la-fonctionnalite`)
5. Créez une Pull Request

## Structure du projet

```
plantuml-gradle/
├── plantuml-plugin/           # Module principal du plugin
│   ├── src/
│   │   ├── main/              # Code source
│   │   ├── test/              # Tests unitaires
│   │   └── functionalTest/    # Tests fonctionnels
│   └── build.gradle.kts       # Configuration du build
├── prompts/                   # Exemples de prompts
├── generated/                 # Fichiers générés
└── docs/                      # Documentation
```

## Standards de codage

### Kotlin

- Suivez les [conventions de codage Kotlin](https://kotlinlang.org/docs/coding-conventions.html)
- Utilisez les fonctionnalités modernes de Kotlin (coroutines, etc.)
- Écrivez du code clair et auto-documenté
- Ajoutez des tests pour chaque nouvelle fonctionnalité

### Git

- Utilisez des messages de commit en français ou en anglais de manière cohérente
- Un commit par fonctionnalité/unité de travail
- Les messages de commit doivent être formatés comme suit :
  ```
  type(scope): description
  
  Corps du message (optionnel)
  ```

Types valides : feat, fix, chore, docs, style, refactor, perf, test

### Tests

- Tous les nouveaux codes doivent être accompagnés de tests
- Utilisez JUnit 5 pour les tests unitaires
- Utilisez Cucumber pour les tests d'acceptation
- Couvrez les cas d'erreur et les cas limites

## Configuration de l'environnement

### Prérequis

- Java 24+
- Gradle 9.4+
- Kotlin 2.3.20+

### Installation

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/cheroliv/plantuml-gradle.git
   ```

2. Importez le projet dans votre IDE favori (IntelliJ IDEA recommandé)

3. Construisez le projet :
   ```bash
   ./gradlew build
   ```

## Exécution des tests

### Tests unitaires

```bash
./gradlew test
```

### Tests fonctionnels

```bash
./gradlew functionalTest
```

### Tous les tests

```bash
./gradlew check
```

## Publication

La publication est gérée automatiquement par les GitHub Actions lors des releases.
Seuls les mainteneurs peuvent publier de nouvelles versions.

## Processus de release

1. Mettez à jour le numéro de version dans `gradle/libs.versions.toml`
2. Mettez à jour le CHANGELOG.md
3. Créez un tag Git avec le numéro de version
4. Publiez une release sur GitHub

## Besoin d'aide ?

Si vous avez des questions, n'hésitez pas à :
- Créer une issue
- Contacter les maintainers sur Discord/Twitter
- Joindre la communauté sur les forums Kotlin/Gradle

Merci encore pour votre contribution !