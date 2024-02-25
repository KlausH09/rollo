package rollo.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.*

fun Application.configureMonitoring() {
    val rolloPositionRegex = Regex("/rollos/name/\\w+/position")

    install(CallLogging) {
        level = Level.INFO
        filter {
            val path = it.request.path()
            if (rolloPositionRegex.matches(path)) return@filter false
            it.request.path().startsWith("/")
        }
    }
}
