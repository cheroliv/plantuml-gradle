import org.gradle.api.JavaVersion.VERSION_21
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    signing
    `java-library`
    `maven-publish`
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publish)
}

group = "com.cheroliv"
version = libs.plugins.plantuml.get().version
kotlin.jvmToolchain(VERSION_21.ordinal)

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    // PlantUML and LangChain4j dependencies
    implementation(libs.plantuml)
    implementation(libs.bundles.plantuml-langchain)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.dataformat.yaml)

    // Unit test dependencies
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.assertj.core)
    testImplementation(libs.kotlin.test.junit5)

    // Cucumber dependencies (unit test scope)
    testImplementation(libs.bundles.cucumber)
}

// ── Test logging ─────────────────────────────────────────────────────────────
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = FULL
    }
}

// ── Gradle plugin metadata ────────────────────────────────────────────────────
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