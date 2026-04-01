# AGENTS.md

This file contains instructions and guidelines for agentic coding assistants working in this repository. It covers build processes, testing procedures, and code style conventions for developing with PlantUML and Gradle.

PlantUML is a component that allows to quickly write diagrams using text descriptions. These diagrams can represent UML diagrams, infrastructure diagrams, AWS resources, and much more. This project aims to maintain and enhance the PlantUML codebase.

## Build Commands

These commands are used to compile and build the PlantUML project:

```bash
# Standard build command
./gradlew build

# Build without running tests
./gradlew build -x test

# Clean build
./gradlew clean build

# Assemble JAR files
./gradlew assemble

# Create distribution packages
./gradlew distTar
./gradlew distZip

# Install to local Maven repository
./gradlew publishToMavenLocal
```

## Test Commands

Testing is essential for maintaining code quality:

```bash
# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "net.sourceforge.plantuml.TestClassName"

# Run a single test method
./gradlew test --tests "net.sourceforge.plantuml.TestClassName.testMethodName"

# Run tests with specific categories
./gradlew test -P categories=unit
./gradlew test -P categories=integration

# Generate test coverage report
./gradlew jacocoTestReport

# Run tests and build with continuous build (watch mode)
./gradlew test --continuous
```

## Linting and Formatting

Code quality tools help maintain consistency:

```bash
# Check code style
./gradlew check

# Run static analysis (SpotBugs, PMD, Checkstyle)
./gradlew spotbugsMain
./gradlew pmdMain
./gradlew checkstyleMain

# Auto-format code
./gradlew spotlessApply

# Check if code is properly formatted
./gradlew spotlessCheck
```

## Code Style Guidelines

### General Principles

1. **Clarity over brevity** - Write code that's easy to understand
2. **Consistency** - Follow existing patterns in the codebase
3. **Explicit over implicit** - Make intentions clear
4. **Fail fast** - Handle error conditions early
5. **Backward compatibility** - Maintain compatibility with existing PlantUML diagrams
6. **Performance** - Optimize for speed as PlantUML processes large diagrams

### Imports

1. Group imports in the following order:
   - Java standard library imports
   - Third-party library imports
   - Project-specific imports
2. Use explicit imports rather than wildcard imports
3. Remove unused imports regularly
4. Keep imports alphabetized within each group

```java
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import com.example.project.MyClass;
import com.example.project.util.Helper;
```

### Naming Conventions

#### Variables and Methods
- Use camelCase for variables and methods
- Choose descriptive names that reveal intent
- Boolean variables/methods should start with `is`, `has`, `can`, etc.
- Constants should be UPPER_SNAKE_CASE

```java
// Good
private static final int MAX_RETRY_COUNT = 3;
private boolean isValidUser = false;
private String userName;

public boolean isValid() { ... }
public void processUserData() { ... }

// Avoid
int x = 5;
boolean flag = false;
String str;
```

#### Classes and Interfaces
- Use PascalCase for classes and interfaces
- Prefer nouns for classes, adjectives for interfaces
- Use descriptive names but keep them reasonable length

```java
// Good
public class UserService { ... }
public interface Configurable { ... }
public class DatabaseConnectionManager { ... }

// Avoid
public class Manager { ... } // Too generic
```

### Formatting

#### Indentation and Spacing
1. Use 4 spaces for indentation (no tabs)
2. Place opening braces on the same line as the statement
3. Add a newline at the end of each file
4. Remove trailing whitespace

```java
// Good
if (condition) {
    doSomething();
} else {
    doSomethingElse();
}

// Avoid
if(condition){
  doSomething();}
else{
  doSomethingElse();}
```

#### Line Length and Wrapping
1. Limit lines to 120 characters
2. Wrap long method signatures at the parameter boundary
3. Align wrapped parameters with first parameter or indent by 8 spaces

```java
// Good
public Result processData(String param1, String param2, String param3,
        String param4, String param5) {
    // method body
}

// Also acceptable
public Result processData(
        String param1,
        String param2,
        String param3,
        String param4,
        String param5) {
    // method body
}
```

### Types and Declarations

#### Variable Declarations
- Declare variables close to their first use
- Initialize variables when declared if possible
- Use diamond operator for generics when possible
- Prefer immutable collections when applicable

```java
// Good
List<String> names = new ArrayList<>();
Map<String, Integer> counts = new HashMap<>();

// Modern Java
List<String> names = List.of("Alice", "Bob");
var result = computeResult(); // when the type is obvious
```

#### Method Signatures
- Limit method parameters to 3-4 where possible
- Extract parameter objects for complex method signatures
- Return early to reduce nesting

```java
// Good
public User findUser(String email) {
    if (email == null || email.isEmpty()) {
        return null;
    }
    
    // rest of implementation
}

// Avoid deep nesting
public User findUser(String email) {
    User result = null;
    if (email != null) {
        if (!email.isEmpty()) {
            // deeply nested implementation
        }
    }
    return result;
}
```

### Error Handling

#### Exception Handling
1. Catch specific exceptions rather than general ones
2. Don't ignore exceptions silently
3. Log exception details appropriately
4. Close resources using try-with-resources

```java
// Good
try (FileInputStream fis = new FileInputStream(file)) {
    // process file
} catch (IOException e) {
    logger.error("Failed to process file: {}", file.getName(), e);
    throw new ProcessingException("Failed to process file", e);
}

// Avoid
try {
    // some operation
} catch (Exception e) {
    // ignoring exception
}
```

#### Null Handling
1. Validate method parameters early
2. Use Optional for methods that might not return a value
3. Use defensive copies for mutable inputs
4. Document null expectations clearly

```java
/**
 * Processes user data
 * @param userData must not be null
 * @return processed result, never null
 */
public ProcessResult processUserData(UserData userData) {
    Objects.requireNonNull(userData, "userData must not be null");
    // implementation
}
```

### Testing Guidelines

1. **Test Structure** - Follow AAA pattern (Arrange, Act, Assert)
2. **Naming** - Use descriptive names in the format `methodUnderTest_condition_expectedResult`
3. **Isolation** - Tests should be independent and repeatable
4. **Coverage** - Aim for meaningful coverage, not just percentage metrics

```java
@Test
void createUser_withValidData_returnsSuccess() {
    // Arrange
    UserData userData = TestData.validUser();
    
    // Act
    Result result = userService.createUser(userData);
    
    // Assert
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getUserId()).isNotNull();
}
```

### Documentation

1. Document all public APIs
2. Use Javadoc for classes, methods, and complex fields
3. Include examples for complex functionality
4. Keep documentation up-to-date with code changes

```java
/**
 * Manages user authentication and authorization.
 * 
 * Example usage:
 * <pre>
 * {@code
 * UserManager userManager = new UserManager(authService);
 * UserSession session = userManager.login("user@example.com", "password");
 * }
 * </pre>
 */
public class UserManager {
    // implementation
}
```

## IDE Configuration

To ensure consistency across development environments, configure your IDE with:

1. **Formatter** - Import the provided code formatter settings
2. **Save Actions** - Configure format on save and organize imports
3. **EditorConfig** - Use the .editorconfig file for basic editor settings
4. **Checkstyle** - Enable Checkstyle plugin with project configuration

## Pre-commit Hooks

Before committing code, ensure you:

1. Run `./gradlew check` to validate code style
2. Run `./gradlew test` to ensure all tests pass
3. Review your changes for sensitive information or hardcoded credentials
4. Check that new files have appropriate license headers

```bash
# Recommended pre-commit script
./gradlew spotlessApply
./gradlew check
./gradlew test
git add .
```

## Directory Structure

The project follows the standard Gradle directory layout:

```
src/
  main/
    java/           # Main source code
    resources/      # Resource files
  test/
    java/           # Test source code
    resources/      # Test resource files
build/              # Generated build artifacts (not versioned)
gradle/             # Gradle Wrapper and version catalogs
```

PlantUML-specific code should be placed under the `net.sourceforge.plantuml` package hierarchy.

## PlantUML Gradle Plugin Implementation Plan

This repository contains a Gradle plugin for processing PlantUML diagrams with AI assistance using LangChain4j. The plugin follows the architectural patterns established in the slider-gradle and readme-gradle projects.

### Plugin Architecture

The plugin is structured as a standalone Gradle plugin that can be consumed by other projects:

```
plantuml-gradle/
├── build.gradle.kts                    # Consumer build script
├── settings.gradle.kts                 # Plugin management
├── gradle/
│   └── libs.versions.toml             # Dependencies version catalog
├── plantuml-context.yml               # Configuration file
├── prompts/                           # Prompt files for diagram generation
├── generated/                         # Generated content
│   ├── diagrams/                      # Processed PlantUML diagrams
│   ├── images/                        # Generated images
│   ├── validations/                   # LLM validation feedback
│   └── rag/                          # RAG training data
├── .github/
│   └── workflows/
│       └── plantuml-processing.yml   # GitHub Actions workflow
└── plantuml-plugin/                  # Plugin source code
    ├── src/
    │   ├── main/
    │   │   └── kotlin/plantuml/      # Root package
    │   │       ├── PlantumlPlugin.kt
    │   │       ├── PlantumlExtension.kt
    │   │       ├── PlantumlConfig.kt
    │   │       ├── models.kt
    │   │       ├── PlantumlManager.kt
    │   │       ├── tasks/
    │   │       │   ├── ProcessPlantumlPromptsTask.kt
    │   │       │   ├── ValidatePlantumlSyntaxTask.kt
    │   │       │   └── ReindexPlantumlRagTask.kt
    │   │       └── service/
    │   │           ├── PlantumlService.kt
    │   │           └── DiagramProcessor.kt
    │   └── test/
    └── build.gradle.kts
```

### Core Functionality

The plugin provides the following key features:

1. **Prompt Directory Monitoring**: Watches `prompts/` directory for new `.prompt` files
2. **LLM Interaction Loop**: Processes prompts through a maximum of 5 iterations for refinement
3. **Syntax Validation**: Validates generated PlantUML syntax and provides error feedback
4. **Image Generation**: Generates images (PNG, SVG, etc.) from valid PlantUML code
5. **LLM Validation**: Requests LLM evaluation of diagram quality with scoring
6. **RAG Training Data Collection**: Stores valid diagrams for future RAG training
7. **GitHub Actions Integration**: Automated processing via GitHub Actions workflows

### Key Tasks

The plugin registers the following Gradle tasks:

#### ProcessPlantumlPrompts
Processes prompt files in the `prompts/` directory:
```bash
./gradlew processPlantumlPrompts
```

Parameters:
- `-Pplantuml.prompts.dir=custom/prompts/path`
- `-Pplantuml.langchain.model=gemini`
- `-Pplantuml.langchain.maxIterations=3`

#### ValidatePlantumlSyntax
Validates PlantUML syntax for debugging:
```bash
./gradlew validatePlantumlSyntax -Pplantuml.diagram=file.puml
```

#### ReindexPlantumlRag
Rebuilds the RAG index with collected PlantUML diagrams:
```bash
./gradlew reindexPlantumlRag
```

### Configuration

The plugin is configured via `plantuml-context.yml`:

```yaml
input:
  prompts: "prompts"                    # Prompts directory

output:
  diagrams: "generated/diagrams"        # Processed diagrams directory
  images: "generated/images"            # Generated images directory
  validations: "generated/validations"  # Validation feedback directory
  rag: "generated/rag"                  # RAG training data directory
  format: "png"                         
  theme: "default"                      

langchain:
  maxIterations: 5                      
  model: "ollama"                       
  validation: true                      
  validationPrompt: "Rate this diagram on clarity, completeness, and best practices..."
  
  # Provider configurations
  ollama:
    baseUrl: "http://localhost:11434"
    modelName: "smollm:135m"
  gemini:
    apiKey: "<YOUR_GEMINI_API_KEY>"
  mistral:
    apiKey: "<YOUR_MISTRAL_API_KEY>"

git:
  userName: "github-actions[bot]"
  userEmail: "github-actions[bot]@users.noreply.github.com"
  commitMessage: "chore: update PlantUML diagrams [skip ci]"
  watchedBranches:
    - "main"
    - "develop"
```

### GitHub Actions Workflow

The repository includes a GitHub Actions workflow that automatically processes prompt files:

```yaml
name: PlantUML Processing

on:
  push:
    paths:
      - 'prompts/**'
    branches:
      - main
      - develop

jobs:
  process-diagrams:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Set up Gradle
        uses: gradle/gradle-build-action@v2
        
      - name: Process PlantUML Prompts
        run: ./gradlew processPlantumlPrompts
        
      - name: Commit and push results
        run: |
          git config --global user.name '${{ secrets.GIT_USER_NAME }}'
          git config --global user.email '${{ secrets.GIT_USER_EMAIL }}'
          git add generated/
          git commit -m "Process PlantUML prompts and generate diagrams" || exit 0
          git push
```

### RAG Integration

The plugin leverages the existing LangChain4j RAG implementation from slider-gradle:

1. **Document Indexing**: Automatically indexes valid PlantUML diagrams for RAG
2. **Context Retrieval**: Uses RAG to provide examples during diagram generation
3. **Training Data Collection**: Collects successful generations for improving future predictions

### Development Guidelines

When contributing to this plugin, follow these guidelines:

1. **Package Structure**: Use the `plantuml` root package (not `com.cheroliv.*`)
2. **Plugin ID**: Maintain the `com.cheroliv.plantuml` plugin ID
3. **Configuration**: Follow the YAML configuration pattern from readme-gradle
4. **AI Integration**: Use the LangChain4j patterns from slider-gradle
5. **Testing**: Include unit tests and Cucumber BDD tests for new features
6. **Documentation**: Update README files and inline documentation

## Additional Resources

1. [PlantUML Official Documentation](https://plantuml.com/)
2. [PlantUML Language Reference](https://plantuml.com/specification)
3. [Gradle Documentation](https://docs.gradle.org/)
4. [Graphviz Documentation](https://graphviz.org/documentation/)
5. [LangChain4j Documentation](https://docs.langchain4j.dev/)