package org.directivexiii.web.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.directivexiii.lastfm.LastFMDirector;
import org.directivexiii.spotify.SpotifyDirector;
import org.directivexiii.web.util.Path;
import org.directivexiii.web.util.ViewUtil;

import de.umass.lastfm.Track;
import spark.Request;
import spark.Response;
import spark.Route;

public class IndexController {

	public static Route serveIndexPage = (Request request, Response response) -> {
		Map<String,Object> model = new HashMap<>();
		return ViewUtil.render(request, model, Path.Template.INDEX);
	};
}
