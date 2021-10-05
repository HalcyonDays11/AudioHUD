package org.directivexiii

import de.umass.lastfm.ImageSize
import de.umass.lastfm.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.directivexiii.gitlab.GitLabDirector
import org.directivexiii.lastfm.LastFMDirector
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object MusicStatus {
    private val watchedInterval = TimeUnit.SECONDS.toMillis(10)
    private val unwatchedInterval = TimeUnit.SECONDS.toMillis(30)

    var currentStatus = CurrentMusicStatus(false, null)
    private var pollingJob: Job? = null
    private val connectedSessions = AtomicInteger(0)

    fun initialize(){
        pollingJob = beginPolling()
    }

    fun sessionConnect() {
        connectedSessions.incrementAndGet()
    }

    fun sessionDisconnect() {
        val currentConnections = connectedSessions.decrementAndGet()
        if(currentConnections < 0){
            connectedSessions.set(0)
        }
    }

    private fun beginPolling(): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (this.isActive) {
                val recentTracks = LastFMDirector.fetchRecentTracks(limit = 5)
                if (recentTracks.isNotEmpty()) {
                    val nowPlaying = recentTracks[0].isNowPlaying
                    val currentTrack = if (nowPlaying) recentTracks[0] else null
                    if (nowPlaying) {
                        recentTracks.removeFirst()
                    }
                    val currentTrackInfo = buildTrackInfo(currentTrack, true)
                    val recentlyPlayed = recentTracks.map { buildTrackInfo(it, false)!! }
                    currentStatus = CurrentMusicStatus(nowPlaying, currentTrackInfo, recentlyPlayed)
                    if(currentTrackInfo != null){
                        GitLabDirector.broadcastTrackStatusAsync(currentTrackInfo)
                    }else{
                        GitLabDirector.broadcastTrackStatusAsync(recentlyPlayed[0])
                    }
                }
                delay(if(connectedSessions.get() > 0) watchedInterval else unwatchedInterval)
            }
        }
    }

    private fun buildTrackInfo(track: Track?, getAlbumArt: Boolean): TrackInfo?{
        if(track == null) return null
        return if(getAlbumArt){
            val album = runCatching { LastFMDirector.fetchAlbum(track) }.getOrNull()
            TrackInfo(track.name, track.album, track.artist, album?.getImageURL(ImageSize.LARGE) ?: "/images/no_cover_art.jpg")
        }else{
            TrackInfo(track.name, track.album, track.artist)
        }
    }
}

data class CurrentMusicStatus(
    val nowPlaying: Boolean,
    val currentTrack: TrackInfo?,
    val recentlyPlayed: List<TrackInfo> = listOf()
)

data class TrackInfo(
    val songTitle: String,
    val albumTitle: String,
    val artist: String,
    val albumArtURL: String = "/images/no_cover_art.jpg"
)
