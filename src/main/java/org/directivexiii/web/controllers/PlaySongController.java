package org.directivexiii.web.controllers;

import org.directivexiii.spotify.SpotifyDirector;

import spark.Request;
import spark.Response;
import spark.Route;

public class PlaySongController {

	public static Route handlePlayRequest = (Request request, Response response) -> {
		String trackURI = request.queryParams("trackURI");
		if(!trackURI.startsWith("spotify")) {
			response.status(400);
			return null;
		}
		
		SpotifyDirector instance = SpotifyDirector.getInstance();
		instance.playSong(trackURI);
		return "Success";
	};
}
