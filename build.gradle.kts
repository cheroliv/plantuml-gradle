plugins {
    alias(libs.plugins.slider)
    alias(libs.plugins.readme)
    id("com.cheroliv.plantuml") version "0.0.1"
}

plantuml {
    configPath = "plantuml-context.yml"
        .run(::file)
        .absolutePath
}

slider {
    configPath = "slides-context.yml"
        .run(::file)
        .absolutePath
}

