package org.directivexiii.web.util;

public class Path {

	public static class Web {
		public static final String INDEX = "/";
		public static final String WS_FETCH_DATA = "/ws/connectToDatastream";
		
		public static String getIndex() {
			return INDEX;
		}
		
		public static String getWsFetchData() {
			return WS_FETCH_DATA;
		}
	}
	
	public static class Template {
		public static final String INDEX = "/velocity/index.vm";
		public static final String WS_FETCH_DATA = "/velocity/nowPlaying.vm";
	}
}
