package org.directivexiii.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.directivexiii.lastfm.LastFMDirector;
import org.directivexiii.spotify.SpotifyDirector;
import org.directivexiii.web.util.Path;
import org.directivexiii.web.util.ViewUtil;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import de.umass.lastfm.Track;


@WebSocket
public class NowPlayingDatastream {

	private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
	private static Timer broadcastTimer = null;
	
	@OnWebSocketConnect
	public void connected(Session session) {
		System.out.println("Session connected...");
		sessions.add(session);
		beginBroadcastTimerIfNeeded();
		
	}
	
	@OnWebSocketClose
	public void closed(Session session, int statusCode, String reason) {
		System.out.println("Session closed...");
		sessions.remove(session);
		stopBroadcastTimerIfNeeded();
	}
	
	public static void beginBroadcastTimerIfNeeded() {
		if(broadcastTimer != null) {
			return;
		}
		if(sessions.isEmpty()) {
			return;
		}
		broadcastTimer = new Timer();
		broadcastTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				LastFMDirector lastfm = LastFMDirector.getInstance();
				Optional<Track> nowPlayingTrack = lastfm.getNowPlayingTrack();
				List<Track> recentTracks = lastfm.getRecentTracks(5);
				Map<String,Object> model = new HashMap<>();
				if(nowPlayingTrack.isPresent()) {
					Track nowPlaying = nowPlayingTrack.get();
					String artUrl = SpotifyDirector.getInstance().getAlbumCoverUrl(nowPlaying.getArtist(), nowPlaying.getAlbum());
					recentTracks.remove(0);
					model.put("nowPlaying", nowPlaying);
					model.put("nowPlayingArt", artUrl);
				}
				model.put("recentTracks", recentTracks);
				
				String renderedHtml = ViewUtil.render(null, model, Path.Template.WS_FETCH_DATA);
				sessions.parallelStream().forEach((session)->{
					if(!session.isOpen()) {
						return;
					}
					try {
						session.getRemote().sendString(renderedHtml);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
			
		}, 0, 10000L);
		System.out.println("Session started... broadcasting.");
	}
	
	public static void stopBroadcastTimerIfNeeded() {
		if(broadcastTimer == null) {
			return;
		}
		if(!sessions.isEmpty()) {
			return;
		}
		broadcastTimer.cancel();
		broadcastTimer = null;
		System.out.println("No sessions connected, shutting down broadcast timer.");
	}
	
	
}
