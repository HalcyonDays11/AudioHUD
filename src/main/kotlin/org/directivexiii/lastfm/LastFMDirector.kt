package org.directivexiii.lastfm

import de.umass.lastfm.Album
import de.umass.lastfm.Caller
import de.umass.lastfm.Track
import de.umass.lastfm.User
import org.directivexiii.util.fetchProperty
import java.util.logging.Level

object LastFMDirector {
    private val userAgent by lazy {
        fetchProperty("config.userAgentToReport")!!
    }
    private val usernameToWatch by lazy {
        fetchProperty("config.lastFmUsername")!!
    }
    private val apiKey by lazy {
        fetchProperty("ktor.security.lastFMApiKey")!!
    }

    private val trackComparator = Comparator { track1: Track, track2: Track ->
        if (track1.isNowPlaying && !track2.isNowPlaying) {
            -1
        } else if (track2.isNowPlaying && !track1.isNowPlaying) {
            1
        } else {
            track2.playedWhen.compareTo(track1.playedWhen)
        }
    }

    fun initialize() {
        val caller = Caller.getInstance()
        caller.userAgent = userAgent
        caller.logger.level = Level.ALL
    }

    fun fetchNowPlayingTrack(): Track? {
        val recentTracks = fetchRecentTracks(usernameToWatch, 1)
        return if (recentTracks.isEmpty()) {
            null
        } else {
            if (recentTracks[0].isNowPlaying) {
                recentTracks[0]
            } else {
                null
            }
        }
    }

    fun fetchAlbum(track: Track): Album = Album.getInfo(track.artist, track.album, apiKey)

    fun fetchRecentTracks(username: String = usernameToWatch, limit: Int): MutableList<Track> {
        val recentTracks = User.getRecentTracks(username, 1, limit, apiKey)
        val trackList = recentTracks.pageResults.toMutableList()
        trackList.sortWith(trackComparator)
        return trackList
    }
}

