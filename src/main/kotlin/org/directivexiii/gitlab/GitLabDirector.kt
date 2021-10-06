package org.directivexiii.gitlab

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.directivexiii.TrackInfo
import org.directivexiii.util.fetchProperty

object GitLabDirector {
    private val httpClient = HttpClient(CIO)
    private val gitLabKey by lazy {
        fetchProperty("ktor.security.gitLabToken")!!
    }

    fun broadcastTrackStatusAsync(trackInfo: TrackInfo) {
        CoroutineScope(Dispatchers.Default).launch {
            val status = "${trackInfo.albumTitle} - ${trackInfo.artist}"
            println("Updating GitLab status to: $status")
            httpClient.put<String>("https://gitlab.mimsoftware.com/api/v4/user/status") {
                contentType(ContentType.Application.Json)
                headers {
                    append("PRIVATE-TOKEN", gitLabKey)
                }
                body = jacksonObjectMapper().writeValueAsString(Status("headphones", status))
            }
        }
    }
}

data class Status(val emoji: String, val message: String)
