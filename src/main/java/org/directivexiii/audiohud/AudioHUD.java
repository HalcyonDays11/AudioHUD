package org.directivexiii.audiohud;

import java.util.Date;
import java.util.List;

import org.directivexiii.lastfm.LastFMDirector;
import org.directivexiii.spotify.SpotifyDirector;
import org.directivexiii.web.Webserver;

import de.umass.lastfm.Track;

public class AudioHUD {

	public static void main(String[] args) {
		LastFMDirector.initialize();
		SpotifyDirector.initialize();
		
		Webserver webserver = new Webserver(8080);
		webserver.initializeWebServer();
	}
}
