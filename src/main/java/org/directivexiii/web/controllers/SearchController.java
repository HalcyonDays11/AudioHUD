package org.directivexiii.web.controllers;

import java.util.HashMap;
import java.util.Map;

import org.directivexiii.spotify.SpotifyDirector;
import org.directivexiii.web.util.Path;
import org.directivexiii.web.util.ViewUtil;

import com.wrapper.spotify.model_objects.specification.Track;

import spark.Request;
import spark.Response;
import spark.Route;

public class SearchController {

	public static Route handleSearch = (Request request, Response response) -> {
		Map<String,Object> model = new HashMap<>();
		String searchEntry = request.queryParams("search");
		SpotifyDirector spotifyDirector = SpotifyDirector.getInstance();
		
		if(searchEntry.toLowerCase().contains(" by ")) {
			String[] split = searchEntry.split(" by ");
			if(split.length == 2) {				
				searchEntry = "artist:" + split[1] + " track:" + split[0];
			}
		}
		
		Track[] songResults = spotifyDirector.searchForSongs(searchEntry);
		model.put("songs", songResults);
		return ViewUtil.render(request, model, Path.Template.POST_SEARCH_RESULTS);
	};
	
}
