package org.directivexiii.spotify;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.special.SearchResult;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.SearchItemRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchAlbumsRequest;

public class SpotifyDirector {

	public static SpotifyDirector instance;
	
	private Properties secrets;
	private SpotifyApi spotifyApi;
	
	public static void initialize() {
		if(instance != null) {
			instance = null;
		}
		instance = new SpotifyDirector();
		instance.initializeInternal();
	}
	
	public static SpotifyDirector getInstance() {
		if(instance == null) {
			throw new IllegalStateException("Director not yet initialized.");
		}
		return instance;
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
		ClientCredentialsRequest credentialsRequest = spotifyApi.clientCredentials().build();
		try {
			ClientCredentials response = credentialsRequest.execute();
			spotifyApi.setAccessToken(response.getAccessToken());
		} catch (SpotifyWebApiException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeInternal() {
		setupSecrets();
		String clientId = secrets.getProperty("SPOTIFY_CLIENT_ID");
		String clientSecret = secrets.getProperty("SPOTIFY_CLIENT_SECRET");
		
		spotifyApi = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).build();
		refreshAccessToken();
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
