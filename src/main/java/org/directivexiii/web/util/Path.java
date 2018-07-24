package org.directivexiii.web.util;

public class Path {

	public static class Web {
		public static final String INDEX = "/";
		public static final String ABOUT = "/about";
		public static final String SUGGEST = "/suggest";
		
		public static final String SEARCH_API = "/api/search";
		
		public static final String WS_FETCH_DATA = "/ws/connectToDatastream";
		
		public static String getIndex() {
			return INDEX;
		}
		
		public static String getAbout() {
			return ABOUT;
		}
		
		public static String getSuggest() {
			return SUGGEST;
		}
		
		public static String getSearchAPI() {
			return SEARCH_API;
		}
				
		public static String getWsFetchData() {
			return WS_FETCH_DATA;
		}
	}
	
	public static class Template {
		public static final String INDEX = "/velocity/index.vm";
		public static final String ABOUT = "/velocity/about.vm";
		public static final String SUGGEST = "/velocity/suggest.vm";
		
		public static final String POST_SEARCH_RESULTS = "/velocity/searchResults.vm";
		
		public static final String WS_FETCH_DATA = "/velocity/nowPlaying.vm";
	}
}
