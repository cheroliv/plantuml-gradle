package app

import app.users.configuration.Loggers.startupLog
import app.users.configuration.Properties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(Properties::class)
class Server {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = runApplication<Server>(*args) {
            setAdditionalProfiles("ai")
        }.startupLog()
    }
}