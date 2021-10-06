package org.directivexiii.plugins

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.routing.*
import io.ktor.http.content.*
import io.ktor.application.*
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.send
import io.ktor.http.cio.websocket.timeout
import io.ktor.response.*
import io.ktor.velocity.VelocityContent
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.apache.velocity.util.StringBuilderWriter
import org.directivexiii.CurrentMusicStatus
import org.directivexiii.MusicStatus
import java.io.StringWriter
import java.time.Duration

fun Application.configureWebsockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/ws/datastream") {
            application.log.warn("Client connected.")
            MusicStatus.sessionConnect()
            val objectMapper = jacksonObjectMapper()
            try{
                while(!closeReason.isCompleted){
                    val content = kotlin.runCatching {
                        objectMapper.writeValueAsString(MusicStatus.currentStatus)
                    }
                    send(content.getOrNull() ?: "")
                    delay(1000)
                }
                application.log.warn("Client disconnect :: ${closeReason.await()?.knownReason}")
                MusicStatus.sessionDisconnect()
            }catch(e: ClosedReceiveChannelException){
                application.log.warn("Client disconnect :: ${closeReason.await()?.knownReason}")
                MusicStatus.sessionDisconnect()
            }
        }
    }
}

fun Application.configureRouting() {
    routing {
        static("/bootstrap") {
            resources("/static/bootstrap")
        }
        static("/images") {
            resources("/static/images")
        }
        static("/css") {
            resources("/static/css")
        }

        get("/") {
            render(call, mapOf(), "velocity/index.vm")
        }
        get("/about") {
            render(call, mapOf(), "velocity/about.vm")
        }
    }
}

suspend fun render(call: ApplicationCall, incomingModel: Map<String, Object>, template: String){
    val model = incomingModel.toMutableMap()
    call.respond(VelocityContent(template, model))
}
