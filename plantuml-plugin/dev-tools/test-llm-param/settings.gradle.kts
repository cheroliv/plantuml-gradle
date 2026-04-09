rootProject.name = "test-llm-param"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    
    includeBuild("../plantuml-plugin")
}