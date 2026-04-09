package plantuml

import org.gradle.api.Project
import java.io.File

/**
 * Merges configuration from three sources with priority:
 * gradle.properties < YAML file < CLI parameters
 */
object ConfigMerger {

    fun merge(project: Project, yamlConfig: PlantumlConfig, cliParams: Map<String, Any?>): PlantumlConfig {
        val propertiesConfig = loadFromGradleProperties(project.projectDir)
        
        return PlantumlConfig(
            input = mergeInputConfig(propertiesConfig.input, yamlConfig.input, cliParams),
            output = mergeOutputConfig(propertiesConfig.output, yamlConfig.output, cliParams),
            langchain4j = mergeLangchain4jConfig(propertiesConfig.langchain4j, yamlConfig.langchain4j, cliParams),
            git = mergeGitConfig(propertiesConfig.git, yamlConfig.git, cliParams),
            rag = mergeRagConfig(propertiesConfig.rag, yamlConfig.rag, cliParams)
        )
    }

    fun merge(projectDir: File, yamlConfig: PlantumlConfig, cliParams: Map<String, Any?>): PlantumlConfig {
        val propertiesConfig = loadFromGradleProperties(projectDir)
        
        return PlantumlConfig(
            input = mergeInputConfig(propertiesConfig.input, yamlConfig.input, cliParams),
            output = mergeOutputConfig(propertiesConfig.output, yamlConfig.output, cliParams),
            langchain4j = mergeLangchain4jConfig(propertiesConfig.langchain4j, yamlConfig.langchain4j, cliParams),
            git = mergeGitConfig(propertiesConfig.git, yamlConfig.git, cliParams),
            rag = mergeRagConfig(propertiesConfig.rag, yamlConfig.rag, cliParams)
        )
    }

    internal fun loadFromGradleProperties(projectDir: File): PlantumlConfig {
        val props = mutableMapOf<String, String>()
        
        val propertiesFile = File(projectDir, "gradle.properties")
        if (propertiesFile.exists()) {
            propertiesFile.reader().useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("plantuml.") && !trimmed.startsWith("#")) {
                        val parts = trimmed.split("=", limit = 2)
                        if (parts.size == 2) {
                            props[parts[0].trim()] = parts[1].trim()
                        }
                    }
                }
            }
        }
        
        return buildConfigFromProperties(props)
    }

    private fun buildConfigFromProperties(props: Map<String, String>): PlantumlConfig {
        return PlantumlConfig(
            input = InputConfig(
                prompts = props["plantuml.input.prompts"] ?: "prompts",
                defaultLang = props["plantuml.input.defaultLang"] ?: "en"
            ),
            output = OutputConfig(
                diagrams = props["plantuml.output.diagrams"] ?: "generated/diagrams",
                images = props["plantuml.output.images"] ?: "generated/images",
                validations = props["plantuml.output.validations"] ?: "generated/validations",
                rag = props["plantuml.output.rag"] ?: "generated/rag",
                format = props["plantuml.output.format"] ?: "png",
                theme = props["plantuml.output.theme"] ?: "default"
            ),
            langchain4j = LangchainConfig(
                maxIterations = props["plantuml.langchain4j.maxIterations"]?.toIntOrNull() ?: 5,
                model = props["plantuml.langchain4j.model"] ?: "ollama",
                validation = props["plantuml.langchain4j.validation"]?.toBoolean() ?: true,
                ollama = OllamaConfig(
                    baseUrl = props["plantuml.langchain4j.ollama.baseUrl"] ?: "http://localhost:11434",
                    modelName = props["plantuml.langchain4j.ollama.modelName"] ?: "smollm:135m"
                ),
                gemini = ApiKeyConfig(apiKey = ""),
                mistral = ApiKeyConfig(apiKey = ""),
                openai = ApiKeyConfig(apiKey = ""),
                claude = ApiKeyConfig(apiKey = ""),
                huggingface = ApiKeyConfig(apiKey = ""),
                groq = ApiKeyConfig(apiKey = "")
            ),
            git = GitConfig(
                userName = props["plantuml.git.userName"] ?: "github-actions[bot]",
                userEmail = props["plantuml.git.userEmail"] ?: "github-actions[bot]@users.noreply.github.com",
                commitMessage = props["plantuml.git.commitMessage"] ?: "chore: update PlantUML diagrams [skip ci]",
                watchedBranches = props["plantuml.git.watchedBranches"]
                    ?.split(",")
                    ?.map { it.trim() }
                    ?: listOf("main", "develop")
            ),
            rag = RagConfig(
                databaseUrl = props["plantuml.rag.databaseUrl"] ?: "",
                username = props["plantuml.rag.username"] ?: "",
                password = props["plantuml.rag.password"] ?: "",
                tableName = props["plantuml.rag.tableName"] ?: "embeddings"
            )
        )
    }

    private fun mergeInputConfig(props: InputConfig, yaml: InputConfig, cli: Map<String, Any?>): InputConfig {
        return InputConfig(
            prompts = cli["input.prompts"]?.toString() ?: (if (yaml.prompts != "prompts") yaml.prompts else props.prompts),
            defaultLang = cli["input.defaultLang"]?.toString() ?: (if (yaml.defaultLang != "en") yaml.defaultLang else props.defaultLang)
        )
    }

    private fun mergeOutputConfig(props: OutputConfig, yaml: OutputConfig, cli: Map<String, Any?>): OutputConfig {
        return OutputConfig(
            diagrams = cli["output.diagrams"]?.toString() ?: (if (yaml.diagrams != "generated/diagrams") yaml.diagrams else props.diagrams),
            images = cli["output.images"]?.toString() ?: (if (yaml.images != "generated/images") yaml.images else props.images),
            validations = cli["output.validations"]?.toString() ?: (if (yaml.validations != "generated/validations") yaml.validations else props.validations),
            rag = cli["output.rag"]?.toString() ?: (if (yaml.rag != "generated/rag") yaml.rag else props.rag),
            format = cli["output.format"]?.toString() ?: (if (yaml.format != "png") yaml.format else props.format),
            theme = cli["output.theme"]?.toString() ?: (if (yaml.theme != "default") yaml.theme else props.theme)
        )
    }

    private fun mergeLangchain4jConfig(props: LangchainConfig, yaml: LangchainConfig, cli: Map<String, Any?>): LangchainConfig {
        return LangchainConfig(
            maxIterations = cli["langchain4j.maxIterations"] as? Int ?: (if (yaml.maxIterations != 5) yaml.maxIterations else props.maxIterations),
            model = cli["langchain4j.model"]?.toString() ?: (if (yaml.model != "ollama") yaml.model else props.model),
            validation = cli["langchain4j.validation"] as? Boolean ?: (if (yaml.validation != true) yaml.validation else props.validation),
            validationPrompt = cli["langchain4j.validationPrompt"]?.toString() ?: yaml.validationPrompt,
            ollama = OllamaConfig(
                baseUrl = cli["langchain4j.ollama.baseUrl"]?.toString() ?: (if (yaml.ollama.baseUrl != "http://localhost:11434") yaml.ollama.baseUrl else props.ollama.baseUrl),
                modelName = cli["langchain4j.ollama.modelName"]?.toString() ?: (if (yaml.ollama.modelName != "smollm:135m") yaml.ollama.modelName else props.ollama.modelName)
            ),
            gemini = yaml.gemini,
            mistral = yaml.mistral,
            openai = yaml.openai,
            claude = yaml.claude,
            huggingface = yaml.huggingface,
            groq = yaml.groq
        )
    }

    private fun mergeGitConfig(props: GitConfig, yaml: GitConfig, cli: Map<String, Any?>): GitConfig {
        return GitConfig(
            userName = cli["git.userName"]?.toString() ?: (if (yaml.userName != "github-actions[bot]") yaml.userName else props.userName),
            userEmail = cli["git.userEmail"]?.toString() ?: (if (yaml.userEmail != "github-actions[bot]@users.noreply.github.com") yaml.userEmail else props.userEmail),
            commitMessage = cli["git.commitMessage"]?.toString() ?: (if (yaml.commitMessage != "chore: update PlantUML diagrams [skip ci]") yaml.commitMessage else props.commitMessage),
            watchedBranches = cli["git.watchedBranches"] as? List<String> ?: (if (yaml.watchedBranches != listOf("main", "develop")) yaml.watchedBranches else props.watchedBranches)
        )
    }

    private fun mergeRagConfig(props: RagConfig, yaml: RagConfig, cli: Map<String, Any?>): RagConfig {
        return RagConfig(
            databaseUrl = cli["rag.databaseUrl"]?.toString() ?: (if (yaml.databaseUrl.isNotEmpty()) yaml.databaseUrl else props.databaseUrl),
            username = cli["rag.username"]?.toString() ?: (if (yaml.username.isNotEmpty()) yaml.username else props.username),
            password = cli["rag.password"]?.toString() ?: (if (yaml.password.isNotEmpty()) yaml.password else props.password),
            tableName = cli["rag.tableName"]?.toString() ?: (if (yaml.tableName != "embeddings") yaml.tableName else props.tableName)
        )
    }

    private fun <T> getOrDefault(cli: Map<String, Any?>, key: String, default: T): T {
        @Suppress("UNCHECKED_CAST")
        return cli[key] as? T ?: default
    }
}
