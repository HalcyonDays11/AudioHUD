package org.directivexiii.web;

import org.directivexiii.web.controllers.AboutController;
import org.directivexiii.web.controllers.IndexController;
import org.directivexiii.web.controllers.NowPlayingDatastream;
import org.directivexiii.web.controllers.SuggestController;
import org.directivexiii.web.util.Path;

import spark.Spark;

public class Webserver {

	private int port;
	
	public Webserver(int port) {
		this.port = port;
	}
	
	public void initializeWebServer() {
		Spark.port(port);
		
		Spark.staticFiles.location("/public");
		Spark.staticFiles.expireTime(600L);
		
		
		//All websockets must be initialized before making a call to get/post/put/delete
		Spark.webSocket(Path.Web.WS_FETCH_DATA, NowPlayingDatastream.class);
		
		//All standard routes live here
		Spark.get(Path.Web.INDEX, IndexController.serveIndexPage);
		Spark.get(Path.Web.ABOUT, AboutController.serveAboutPage);
		Spark.get(Path.Web.SUGGEST, SuggestController.serveSuggestPage);
	}
	
}
