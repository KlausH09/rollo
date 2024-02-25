package rollo.plugins

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.serialization.Serializable
import rollo.models.RolloEvent
import rollo.models.RolloMoveDirection
import rollo.models.RolloPosition.Companion.toRolloPosition
import rollo.rolloService

fun Application.configureRouting() {
    install(Resources)
    install(SSE)
    install(ContentNegotiation) { json() }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get<Rollos.Configs> {
            call.respond(rolloService.getConfig())
        }
        get<Rollos.Name.Move> { url ->
            val name = url.parent.name
            val position = url.position.toRolloPosition()
            rolloService.move(name = name, position = position)
            call.respond(HttpStatusCode.OK)
        }
        get<Rollos.Name.Config> { url ->
            val name = url.parent.name
            call.respond(rolloService.getConfig(name))
        }
        get<Rollos.Name.Position> { url ->
            val position = rolloService.getPosition(name = url.parent.name)
            call.respondText(position.intPosition.toString())
        }
        get<Rollos.Name.MoveEvent> { url ->
            val event = RolloEvent(moveDirection = url.direction)
            rolloService.changeStatus(name = url.parent.name, event = event)
            call.respond(HttpStatusCode.OK)
        }

        sse("/hello") {
            send(ServerSentEvent("world"))
        }
    }
}


@Serializable
@Resource("/rollos")
class Rollos {
    @Resource("configs")
    class Configs(val parent: Name)

    @Resource("name/{name}")
    class Name(val parent: Rollos, val name: String) {
        @Resource("move")
        class Move(val parent: Name, val position: Int)

        @Resource("event")
        class MoveEvent(val parent: Name, val direction: RolloMoveDirection)

        @Resource("position")
        class Position(val parent: Name)

        @Resource("config")
        class Config(val parent: Name)
    }
}
