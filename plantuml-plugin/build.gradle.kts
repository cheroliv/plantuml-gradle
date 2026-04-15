import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.plugin.compatibility.compatibility
import java.time.Duration

plugins {
    `java-library`
    signing
    `maven-publish`
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publish)
    alias(libs.plugins.kover)
}

group = "com.cheroliv"
version = libs.plugins.plantuml.get().version
kotlin.jvmToolchain(JavaVersion.VERSION_24.ordinal)

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(libs.bundles.asciidoctor)
    implementation(libs.node.gradle)

    api(libs.bundles.plantuml)
    api(libs.bundles.jgit)
    api(libs.commons.io)
    api(libs.bundles.plantuml.ai)

    // Jackson for JSON serialization
    api(libs.jackson.module.kotlin)
    api(libs.jackson.dataformat.yaml)
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

    // Coroutines - IMPORTANT for the asynchronous tests
    testImplementation(libs.bundles.coroutines)

    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.slf4j)
    testRuntimeOnly(libs.logback)

    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.junit.platform.params)
    testImplementation(libs.wiremock)

    // Cucumber dependencies
    testImplementation(libs.bundles.cucumber)
}

configurations.all {
    resolutionStrategy {
        // Force la version de Groovy utilisée par Gradle
        force(libs.groovy)
        force(libs.groovy.nio)
    }
}

// Exclure les dépendances Groovy conflictuelles uniquement pour certaines configurations
configurations.configureEach {
    // Ne pas exclure pour testImplementation car cela peut casser les tests
    if (name != "testImplementation" && name != "testRuntimeOnly") {
        exclude(group = "org.codehaus.groovy")
    }
}


tasks.withType<Test> {
    useJUnitPlatform {
        // Les tests @Tag("real-llm") sont exclus par défaut.
        // Pour les activer : ./gradlew test -Ptest.tags="real-llm"
        val runRealLlm = project.findProperty("test.tags")
            ?.toString()
            ?.contains("real-llm") == true

        if (!runRealLlm) {
            excludeTags("real-llm")
        }

        // Timeout global par test — évite qu'un GradleRunner reste bloqué
        // si Ollama ne répond pas (couvert par WireMock dans les tests unitaires)
        timeout.set(Duration.ofSeconds(30))

        // Exécution parallèle des classes de test (les nested partagent leur état
        // via companion object, donc la parallélisation est au niveau classe)
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    }
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }

    // OPTIMISATION : 1 seul worker JVM pour maximiser le partage
    // Les nested classes partagent WireMock + GradleRunner + sharedProjectDir
    // via le companion object de PlantumlFunctionalSuite
    maxParallelForks = 1 // ← 1 seule JVM : réutilisation maximale
    forkEvery = 0 // ← Ne jamais redémarrer le worker (0 = illimité)

    // Timeout stricte pour éviter les blocages
    timeout.set(Duration.ofSeconds(60))

    // Réutilisation des sorties pour accélérer les exécutions
    outputs.cacheIf { true }

    // Options JVM optimisées pour les tests
    jvmArgs("-XX:+UseSerialGC") // GC plus rapide pour les tests courts
    jvmArgs("-XX:MaxMetaspaceSize=256m") // Limite mémoire plus stricte
    jvmArgs("-XX:TieredStopAtLevel=1") // Désactiver JIT pour démarrage rapide
}

tasks.named<Test>("test") {
    filter {
        // Exclure les classes dans le package 'plantuml.scenarios' (tests Cucumber)
        excludeTestsMatching("plantuml.scenarios.**")
        // Exclure également les classes de functionalTest
        excludeTestsMatching("plantuml.PlantUmlPluginFunctionalTests")
    }
}


// 1. Créer le SourceSet functionalTest
val functionalTest: SourceSet by sourceSets.creating {
    java.srcDirs("src/functionalTest/kotlin")
    resources.srcDirs("src/functionalTest/resources")
}

// 2. Ajouter GradleTestKit à functionalTest (SANS hériter de testImplementation)
dependencies {
    add(functionalTest.implementationConfigurationName, gradleTestKit())
    add(functionalTest.implementationConfigurationName, kotlin("stdlib-jdk8"))
    add(functionalTest.implementationConfigurationName, kotlin("test"))
    add(functionalTest.implementationConfigurationName, kotlin("test-junit5"))

    // Ajouter les dépendances nécessaires explicitement
    add(functionalTest.implementationConfigurationName, "org.slf4j:slf4j-api:2.0.17")
    add(functionalTest.runtimeOnlyConfigurationName, "ch.qos.logback:logback-classic:1.5.26")
    add(functionalTest.runtimeOnlyConfigurationName, "org.junit.platform:junit-platform-launcher")

    // CORRECTION: Ajouter AssertJ pour les assertions
    add(functionalTest.implementationConfigurationName, libs.assertj.core)

    // Ajouter Mockito si nécessaire
    add(functionalTest.implementationConfigurationName, libs.mockito.kotlin)
    add(functionalTest.implementationConfigurationName, libs.mockito.junit.jupiter)
    add(functionalTest.implementationConfigurationName, libs.wiremock)
    add(functionalTest.implementationConfigurationName, libs.junit.platform.params)

    libs.bundles.coroutines.get().forEach { dep ->
        add(functionalTest.implementationConfigurationName, dep)
    }

    // CORRECTION: Ajouter LangChain4j pour accéder aux classes ChatModel
    add(functionalTest.implementationConfigurationName, libs.langchain4j)
    add(functionalTest.implementationConfigurationName, libs.langchain4j.ollama)

    // CORRECTION: Ajouter la dépendance vers le code source principal pour accéder aux classes du plugin
    add(functionalTest.implementationConfigurationName, sourceSets.main.get().output)
}

// 3. Tâche pour les tests fonctionnels
val functionalTestTask = tasks.register<Test>("functionalTest") {
    description = "Runs functional tests."
    group = "verification"
    testClassesDirs = functionalTest.output.classesDirs
    classpath = configurations[functionalTest.runtimeClasspathConfigurationName] + functionalTest.output
    useJUnitPlatform()
//    useJUnitPlatform { includeTags("real-llm") }
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
    failOnNoDiscoveredTests = false

    // Timeout pour les tests fonctionnels - les mocks WireMock évitent les appels réseau réels
    timeout.set(Duration.ofMinutes(5))

    // Ajouter des propriétés système pour les tests de permissions
    systemProperty("test.timeout.multiplier", "2")

    // SÉQUENTIEL STRICT : 1 seul test à la fois pour éviter OOM
    // Chaque test lance un GradleRunner (~500MB+), la parallélisation crashe le système
    maxParallelForks = 1
    forkEvery = 0
    jvmArgs("-XX:+UseSerialGC")
    jvmArgs("-XX:MaxMetaspaceSize=256m")
    jvmArgs("-XX:TieredStopAtLevel=1")
}

// CORRECTION: Gérer les duplications de ressources pour functionalTest
tasks.named<ProcessResources>(functionalTest.processResourcesTaskName) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// 4. Configurer les sources sets pour Cucumber (test standard)
sourceSets.test {
    resources.srcDir("src/test/features")
    java.srcDir("src/test/scenarios")  // Steps dans scenarios/
}


// 5. Faire hériter testImplementation de functionalTest (pas l'inverse !)
configurations.named("testImplementation").configure {
    extendsFrom(configurations.named(functionalTest.implementationConfigurationName).get())
}

configurations.named("testRuntimeOnly").configure {
    extendsFrom(configurations.named(functionalTest.runtimeOnlyConfigurationName).get())
}

// 6. Ajouter les classes compilées de functionalTest au classpath de test
dependencies {
    testImplementation(functionalTest.output)
}

// Configuration spécifique pour les tests de plugin
tasks.named<Test>("test") {
    // Ajouter le jar du plugin au classpath des tests
    classpath += files(tasks.named("jar"))

    // Ajouter les propriétés système nécessaires
    systemProperty("gradle.plugin.repository", project.rootDir.resolve("build/libs").absolutePath)
}

configurations {
    // Exclure logback-classic du classpath de test
    named("testRuntimeClasspath") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    named("testImplementation") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    // Exclure logback-classic du classpath de functionalTest
    named(functionalTest.runtimeClasspathConfigurationName) {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
}

// 7. Tâche dédiée aux tests Cucumber
val cucumberTest = tasks.register<Test>("cucumberTest") {
    description = "Runs Cucumber BDD tests"
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = configurations.testRuntimeClasspath.get() +
            sourceSets.test.get().output +
            sourceSets.main.get().output
    useJUnitPlatform {
        // CORRECTION: Ne pas filtrer par tag ici, ça filtre les engines JUnit
        // Le filtrage des scénarios Cucumber se fait dans le runner via FILTER_TAGS_PROPERTY_NAME
        excludeEngines("junit-jupiter")
    }
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = FULL
    }
    outputs.upToDateWhen { false }
    // S'assurer que main est compilé avant
    dependsOn(tasks.classes)

    // OPTIMISATION : 1 seul worker JVM pour les tests Cucumber
    maxParallelForks = 1 // ← 1 seule JVM pour état partagé
    forkEvery = 0 // ← Ne jamais redémarrer le worker
    jvmArgs("-XX:+UseSerialGC")
    jvmArgs("-XX:MaxMetaspaceSize=256m")
    jvmArgs("-XX:TieredStopAtLevel=1")
}

tasks.withType<Test>().configureEach {
    // Permet de masquer l'avertissement relatif au chargement dynamique d'agents
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

tasks.check {
    dependsOn(functionalTestTask)
    dependsOn(cucumberTest)
}

kover {
    currentProject {
        sources {
            // Inclure main + functionalTest dans la couverture
            // Par défaut, Kover inclut déjà 'main' et exclut 'test'
            // On ajoute explicitement functionalTest
            includedSourceSets.addAll("main", "functionalTest")
        }
    }
    reports {
        total {
            html {
                onCheck.set(true)
                htmlDir.set(layout.buildDirectory.dir("reports/kover/html"))
            }
            xml {
                onCheck.set(true)
                xmlFile.set(layout.buildDirectory.file("reports/kover/xml/report.xml"))
            }
        }
    }
}

// Kover verification - fail build if coverage < 75%
tasks.register("koverThresholdCheck") {
    doLast {
        val reportFile = layout.buildDirectory.file("reports/kover/xml/report.xml").get().asFile
        if (!reportFile.exists()) {
            throw GradleException("Kover report not found. Run 'koverXmlReport' first.")
        }
        val xml = reportFile.readText()
        // Aggregate all INSTRUCTION counters from the report
        val coverageRegex = Regex("""<counter type="INSTRUCTION" missed="(\d+)" covered="(\d+)"/>""")
        val matches = coverageRegex.findAll(xml)
        var totalMissed = 0L
        var totalCovered = 0L
        for (match in matches) {
            totalMissed += match.groupValues[1].toLong()
            totalCovered += match.groupValues[2].toLong()
        }
        val total = totalMissed + totalCovered
        val coverage = if (total > 0) (totalCovered.toDouble() / total) * 100 else 0.0
        println("Instruction coverage: ${String.format("%.2f", coverage)}% (missed=$totalMissed, covered=$totalCovered)")
        if (coverage < 75.0) {
            throw GradleException("Coverage ${String.format("%.2f", coverage)}% is below threshold 75%")
        }
    }
}

tasks.check {
    dependsOn("koverThresholdCheck")
}

gradlePlugin {
    plugins {
        vcsUrl = "https://github.com/cheroliv/plantuml-gradle.git"
        website = "https://cheroliv.com"
        create("plantuml") {
            id = libs.plugins.plantuml.get().pluginId
            implementationClass = "plantuml.PlantumlPlugin"
            displayName = "Plantuml Plugin"
            description = "Gradle plugin for plantuml generation."
            listOf(
                "plantuml",
                "jgit",
                "langchain4j",
                "ollama",
                "kotlin-DSL"
            ).run(tags::set)

            @Suppress("UnstableApiUsage")
            compatibility {
                features {
                    // asciidoctorRevealJs runs OUT_OF_PROCESS via JRuby — not compatible
                    // with Configuration Cache. Will be revisited when asciidoctor-gradle
                    // stabilises beyond 5.0.0-alpha.1.
                    configurationCache = false
                }
            }
        }
    }
    testSourceSets(functionalTest)
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
    repositories {
        maven {
            name = "sonatype"
            url = (if (version.toString().endsWith("-SNAPSHOT"))
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            else uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"))
            credentials {
                username = project.findProperty("ossrhUsername") as? String
                password = project.findProperty("ossrhPassword") as? String
            }
        }
        mavenCentral()
    }
}

signing {
    val isReleaseVersion = !version.toString().endsWith("-SNAPSHOT")
    if (isReleaseVersion) sign(publishing.publications)
    useGpgCmd()
}
