plugins {
    alias(libs.plugins.plantuml)
}

plantuml {
    configPath = file("plantuml-context.yml").absolutePath
}