package org.directivexiii.spotify;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonArray;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.Device;
import com.wrapper.spotify.model_objects.special.SearchResult;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest.Builder;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import com.wrapper.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import com.wrapper.spotify.requests.data.search.SearchItemRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;

public class SpotifyDirector {

	public static SpotifyDirector instance;
	
	private Properties secrets;
	private SpotifyApi spotifyApi;
	
	private String authorizationCode;
	
	public static void initialize() {
		if(instance != null) {
			instance = null;
		}
		instance = new SpotifyDirector();
		instance.initializeInternal();
		instance.authorize();
	}
	
	public static SpotifyDirector getInstance() {
		if(instance == null) {
			throw new IllegalStateException("Director not yet initialized.");
		}
		return instance;
	}
	
	public void setAuthorizationCode(String code) {
		this.authorizationCode = code;
		refreshAccessToken(code);
	}
	
	public void authorize() {
		try {
			URI magicURI = spotifyApi.authorizationCodeUri().scope("user-read-playback-state streaming").build().execute();
			Desktop.getDesktop().browse(magicURI);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void playSong(String uri) {
		JsonArray uriArray = new JsonArray();
		uriArray.add(uri);
		GetUsersAvailableDevicesRequest availableDevicesRequest = spotifyApi.getUsersAvailableDevices().build();
		try {
			Device[] devices = availableDevicesRequest.execute();
			Optional<Device> possibleDevice = Arrays.stream(devices).filter(Device::getIs_active).findFirst();
			if(!possibleDevice.isPresent()) {
				return;
			}
			Device device = possibleDevice.get();
			StartResumeUsersPlaybackRequest playbackRequest = spotifyApi.startResumeUsersPlayback().uris(uriArray).device_id(device.getId()).build();
			playbackRequest.execute();
		} catch (SpotifyWebApiException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public Track[] searchForSongs(String searchString) {
		String query = searchString;
		SearchTracksRequest tracksRequest = spotifyApi.searchTracks(query).limit(10).build();
		try {
			Paging<Track> result = tracksRequest.execute();
			Track[] items = result.getItems();
			return items;
		} catch (SpotifyWebApiException | IOException e) {
			e.printStackTrace();
		}
		return new Track[] {};
	}
	
	public String getAlbumCoverUrl(String artist, String album) {
		String query = "artist:" + artist + " album:" + album;
		SearchAlbumsRequest albumRequest = spotifyApi.searchAlbums(query).limit(1).build();
		AlbumSimplified albumObject = null;
		try {
			Paging<AlbumSimplified> result = albumRequest.execute();
			AlbumSimplified[] albums = result.getItems();
			if(albums.length > 0) {
				albumObject = albums[0];
			}
		} catch (SpotifyWebApiException | IOException e) {
			e.printStackTrace();
		}
		
		if(albumObject != null) {
			String url = albumObject.getImages()[0].getUrl();
			return url;
		}
		return null;
	}
	
	private void refreshAccessToken() {
		AuthorizationCodeRefreshRequest authorizationCodeRefresh = spotifyApi.authorizationCodeRefresh().build();
		try {
			AuthorizationCodeCredentials creds = authorizationCodeRefresh.execute();
			spotifyApi.setAccessToken(creds.getAccessToken());
			spotifyApi.setRefreshToken(creds.getRefreshToken());
			
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					refreshAccessToken();
				}
			}, TimeUnit.SECONDS.toMillis(creds.getExpiresIn()));
			
		}catch(Exception e) {
			e.printStackTrace();
			refreshAccessToken(authorizationCode);
		}
	}
	
	private void refreshAccessToken(String code) {
		AuthorizationCodeRequest codeRequest = spotifyApi.authorizationCode(code).build();
		try {
			AuthorizationCodeCredentials creds = codeRequest.execute();
			spotifyApi.setAccessToken(creds.getAccessToken());
			spotifyApi.setRefreshToken(creds.getRefreshToken());
			
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					refreshAccessToken();
				}
			}, TimeUnit.SECONDS.toMillis(creds.getExpiresIn()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initializeInternal() {
		setupSecrets();
		String clientId = secrets.getProperty("SPOTIFY_CLIENT_ID");
		String clientSecret = secrets.getProperty("SPOTIFY_CLIENT_SECRET");
		URI redirect_uri = null;
		try {
			redirect_uri = new URI("http://localhost:8080/callback");
		} catch (URISyntaxException e) { }
		spotifyApi = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).setRedirectUri(redirect_uri).build();
		ClientCredentialsRequest credentialsRequest = spotifyApi.clientCredentials().build();
		try {
			ClientCredentials response = credentialsRequest.execute();
			spotifyApi.setAccessToken(response.getAccessToken());
		} catch (SpotifyWebApiException | IOException e) {
			e.printStackTrace();
		}
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
