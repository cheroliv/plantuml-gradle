plugins { id("com.cheroliv.plantuml").version("0.0.0") }

plantuml {
    configPath = "plantuml-context.yml"
        .run(::file)
        .absolutePath
}

