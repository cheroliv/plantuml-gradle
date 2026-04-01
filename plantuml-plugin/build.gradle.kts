import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    `java-library`
    `maven-publish`
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("com.gradle.plugin-publish") version "2.1.0"
}

group = "com.cheroliv"
version = "0.0.1"
kotlin {
    jvmToolchain(11)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

// Configuration des sources sets pour les différents types de tests
val functionalTest: SourceSet by sourceSets.creating {
    java.srcDirs("src/functionalTest/kotlin")
    resources.srcDirs("src/functionalTest/resources")
}

val scenarios: SourceSet by sourceSets.creating {
    java.srcDirs("src/scenarios/kotlin")
    resources.srcDirs("src/scenarios/resources")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    // PlantUML dependency
    implementation("net.sourceforge.plantuml:plantuml:1.2026.0")
    
    // LangChain4j dependencies for AI integration
    implementation("dev.langchain4j:langchain4j:1.12.1")
    implementation("dev.langchain4j:langchain4j-ollama:1.12.1")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:1.12.1")
    implementation("dev.langchain4j:langchain4j-mistral-ai:1.12.1")
    implementation("dev.langchain4j:langchain4j-pgvector:1.12.2-beta22")
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:1.12.2-beta22")
    
    // YAML processing
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.21.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.21.1")
    
    // Docker Java for container management
    implementation("com.github.docker-java:docker-java-core:3.7.0")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:3.7.0")
    
    // Unit test dependencies
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.6.1")
    
    // Functional test dependencies
    add(functionalTest.implementationConfigurationName, gradleTestKit())
    add(functionalTest.implementationConfigurationName, kotlin("stdlib-jdk8"))
    add(functionalTest.implementationConfigurationName, kotlin("test-junit5"))
    add(functionalTest.implementationConfigurationName, "org.assertj:assertj-core:3.27.7")
    
    // Cucumber dependencies for BDD testing
    testImplementation("io.cucumber:cucumber-java:7.34.3")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.34.3")
    testImplementation("io.cucumber:cucumber-picocontainer:7.34.3")
    testImplementation("org.junit.platform:junit-platform-suite:1.14.3")
    
    // Cucumber dependencies for scenarios
    add(scenarios.implementationConfigurationName, "io.cucumber:cucumber-java:7.34.3")
    add(scenarios.implementationConfigurationName, "io.cucumber:cucumber-junit-platform-engine:7.34.3")
    add(scenarios.implementationConfigurationName, "io.cucumber:cucumber-picocontainer:7.34.3")
    add(scenarios.implementationConfigurationName, "org.assertj:assertj-core:3.27.7")
    
    // Ajout des dépendances Gradle et Kotlin nécessaires
    add(scenarios.implementationConfigurationName, gradleApi())
    add(scenarios.implementationConfigurationName, gradleTestKit())
    add(scenarios.implementationConfigurationName, kotlin("test-junit5"))
    
    // Ajout de kotlinx-coroutines pour PlantumlWorld
    add(scenarios.implementationConfigurationName, "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    
    // Ajout de slf4j pour PlantumlWorld
    add(scenarios.implementationConfigurationName, "org.slf4j:slf4j-api:2.0.16")
    
    // Ajout de JUnit Suite pour CucumberTestRunner
    add(scenarios.implementationConfigurationName, "org.junit.platform:junit-platform-suite:1.14.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = FULL
    }
}

// Configuration des tâches de test
val functionalTestTask = tasks.register<Test>("functionalTest") {
    description = "Runs functional tests."
    group = "verification"
    testClassesDirs = functionalTest.output.classesDirs
    classpath = configurations[functionalTest.runtimeClasspathConfigurationName] + functionalTest.output
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
    failOnNoDiscoveredTests = false
}

val cucumberTest = tasks.register<Test>("cucumberTest") {
    description = "Runs Cucumber BDD tests"
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = configurations.testRuntimeClasspath.get() +
            sourceSets.test.get().output +
            scenarios.output
    useJUnitPlatform {
        excludeEngines("junit-jupiter")
    }
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = FULL
    }
    outputs.upToDateWhen { false }
    // S'assurer que functionalTest et main sont compilés avant
    dependsOn(functionalTest.classesTaskName)
    dependsOn(tasks.classes)
}

tasks.check {
    dependsOn(functionalTestTask)
    dependsOn(cucumberTest)
}

gradlePlugin {
    website.set("https://github.com/cheroliv/plantuml-gradle/")
    vcsUrl.set("https://github.com/cheroliv/plantuml-gradle.git")

    plugins {
        create("plantuml") {
            id = "com.cheroliv.plantuml"
            implementationClass = "plantuml.PlantumlPlugin"
            displayName = "PlantUML Generator Plugin"
            description = """
                Generates PlantUML diagrams from natural language prompts using LangChain4j.
                Processes prompts from a directory, validates syntax, generates images,
                and collects validated diagrams for RAG training.
            """.trimIndent()
            tags.set(
                listOf(
                    "plantuml",
                    "diagram",
                    "llm",
                    "langchain4j",
                    "ai",
                    "prompt"
                )
            )
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        withType<MavenPublication> {
            if (name == "pluginMaven") {
                pom {
                    name.set(gradlePlugin.plugins.getByName("plantuml").displayName)
                    description.set(gradlePlugin.plugins.getByName("plantuml").description)
                    url.set(gradlePlugin.website.get())
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("cheroliv")
                            name.set("cheroliv")
                            email.set("cheroliv.developer@gmail.com")
                        }
                    }
                    scm {
                        connection.set(gradlePlugin.vcsUrl.get())
                        developerConnection.set(gradlePlugin.vcsUrl.get())
                        url.set(gradlePlugin.vcsUrl.get())
                    }
                }
            }
        }
    }
}