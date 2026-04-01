plugins { id("com.cheroliv.plantuml").version("0.0.1") }

plantuml {
    configPath = "plantuml-context.yml"
        .run(::file)
        .absolutePath
}

