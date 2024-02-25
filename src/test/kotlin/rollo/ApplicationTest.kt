package rollo

import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.sse.*
import io.ktor.server.testing.*
import io.ktor.sse.*
import kotlin.test.*
import kotlinx.coroutines.flow.*
import rollo.plugins.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun testServerSentEvents() = testApplication {
        install(SSE)
        routing {
            sse("/events") {
                repeat(100) {
                    send(ServerSentEvent("event $it"))
                }
            }
        }

        createClient {
            install(io.ktor.client.plugins.sse.SSE)
        }.sse("/events") {
            incoming.collectIndexed { i, event ->
                assertEquals("event $i", event.data)
            }
        }
    }
}
