# Contributing to the PlantUML Gradle Plugin

> <img src="images/lang-fr-red.svg" alt="Français" title="Français" width="16" height="12"/> [Version française](CONTRIBUTING_fr.md)

Thank you for your interest in contributing to the PlantUML Gradle Plugin! This guide will explain how to participate in the development of this project.

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue including:
- A clear and concise description of the problem
- Steps to reproduce the bug
- The version of the plugin used
- Your environment (OS, Gradle version, etc.)

### Proposing Enhancements

You can propose enhancements by creating an issue with:
- A detailed description of the proposed enhancement
- Expected benefits
- Any relevant implementation information

### Submitting Fixes

To submit code fixes:

1. Fork the repository
2. Create a branch for your feature (`git checkout -b feature/feature-name`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/feature-name`)
5. Create a Pull Request

## Project Structure

```
plantuml-gradle/
├── plantuml-plugin/           # Main plugin module
│   ├── src/
│   │   ├── main/              # Source code
│   │   ├── test/              # Unit tests
│   │   └── functionalTest/    # Functional tests
│   └── build.gradle.kts       # Build configuration
├── prompts/                   # Example prompts
├── generated/                 # Generated files
└── docs/                      # Documentation
```

## Coding Standards

### Kotlin

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use modern Kotlin features (coroutines, etc.)
- Write clear, self-documenting code
- Add tests for each new feature

### Git

- Use commit messages in French or English consistently
- One commit per feature/work unit
- Commit messages should be formatted as follows:
  ```
  type(scope): description
  
  Message body (optional)
  ```

Valid types: feat, fix, chore, docs, style, refactor, perf, test

### Tests

- All new code must be accompanied by tests
- Use JUnit 5 for unit tests
- Use Cucumber for acceptance tests
- Cover error cases and edge cases

## Environment Setup

### Prerequisites

- Java 24+
- Gradle 9.4+
- Kotlin 2.3.20+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/cheroliv/plantuml-gradle.git
   ```

2. Import the project into your favorite IDE (IntelliJ IDEA recommended)

3. Build the project:
   ```bash
   ./gradlew build
   ```

## Running Tests

### Unit Tests

```bash
./gradlew test
```

### Functional Tests

```bash
./gradlew functionalTest
```

### All Tests

```bash
./gradlew check
```

## Publishing

Publishing is handled automatically by GitHub Actions during releases.
Only maintainers can publish new versions.

## Release Process

1. Update the version number in `gradle/libs.versions.toml`
2. Update CHANGELOG.md
3. Create a Git tag with the version number
4. Publish a release on GitHub

## Need Help?

If you have questions, feel free to:
- Create an issue
- Contact maintainers on Discord/Twitter
- Join the community on Kotlin/Gradle forums

Thank you again for your contribution!
