package rollo

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import rollo.models.Rollo
import rollo.plugins.*
import rollo.services.RolloService


val rolloService = RolloService()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureMonitoring()
    configureTemplating()
    configureRouting()
}
