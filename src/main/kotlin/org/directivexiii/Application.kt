package org.directivexiii

import io.ktor.application.*
import org.directivexiii.lastfm.LastFMDirector
import org.directivexiii.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureWebsockets()
    configureRouting()
    configureMonitoring()
    configureTemplating()
    LastFMDirector.initialize()
    MusicStatus.initialize()
}
