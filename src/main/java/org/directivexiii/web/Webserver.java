package org.directivexiii.web;

import org.directivexiii.web.controllers.IndexController;
import org.directivexiii.web.controllers.NowPlayingDatastream;
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
		
		Spark.webSocket(Path.Web.WS_FETCH_DATA, NowPlayingDatastream.class);
		Spark.get(Path.Web.INDEX, IndexController.serveIndexPage);
	}
	
}
