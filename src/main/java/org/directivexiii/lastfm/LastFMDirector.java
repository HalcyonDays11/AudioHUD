package org.directivexiii.lastfm;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

import de.umass.lastfm.Caller;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;

public class LastFMDirector {
	
	public static final boolean DEBUG_MODE = true;
	public static final String USER_AGENT = "org.directivexiii.AudioHUD";
	public static final String USERNAME = "HalcyonDays11";
	
	public static LastFMDirector instance;
	
	private Properties secrets;
	private String apiKey;
	
	public static void initialize() {
		if(instance != null) {
			instance = null;
		}
		instance = new LastFMDirector();
		instance.initializeInternal();
	}
	
	public static LastFMDirector getInstance() {
		if(instance == null) {
			throw new IllegalStateException("Director not yet initialized");
		}
		return instance;
	}
	
	public Optional<Track> getNowPlayingTrack(){
		return getNowPlayingTrack(USERNAME);
	}
	
	public Optional<Track> getNowPlayingTrack(String username) {
		List<Track> recentTracks = getRecentTracks(username, 1);
		if(recentTracks.isEmpty()) {
			return Optional.empty();
		}
		Track track = recentTracks.get(0);
		if(track.isNowPlaying()) {
			return Optional.of(track);
		}else {
			return Optional.empty();
		}
	}
	
	public List<Track> getRecentTracks(int limit){
		return getRecentTracks(USERNAME, limit);
	}
	
	public List<Track> getRecentTracks(String username, int limit) {
		PaginatedResult<Track> recentTracks = User.getRecentTracks(username, 1, limit, apiKey);
		Collection<Track> pageResults = recentTracks.getPageResults();
		ArrayList<Track> tracks = new ArrayList<>(pageResults);
		tracks.sort((track1, track2) -> {
			if(track1.isNowPlaying() && !track2.isNowPlaying()) {
				return -1;
			}else if(track2.isNowPlaying() && !track1.isNowPlaying()) {
				return 1;
			}else {
				return track2.getPlayedWhen().compareTo(track1.getPlayedWhen());
			}
		});
		return tracks;
	}
	
	private void initializeInternal() {
		Caller caller = Caller.getInstance();
		caller.setUserAgent(USER_AGENT);
		if(DEBUG_MODE) {
			caller.getLogger().setLevel(Level.ALL);
		}
		setupSecrets();
		apiKey = secrets.getProperty("LASTFM_API_KEY");
	}
	
	private void setupSecrets() {
		URL resource = getClass().getClassLoader().getResource("Secrets.properties");
		try(InputStream fis = resource.openStream()){
			secrets = new Properties();
			secrets.load(fis);
		} catch (Throwable t) {
			secrets = null;
		}
	}
}
