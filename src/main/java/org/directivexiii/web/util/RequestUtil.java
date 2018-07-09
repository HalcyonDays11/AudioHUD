package org.directivexiii.web.util;

import spark.Request;
import spark.Response;

public class RequestUtil {

	public static String getSessionCurrentUser(Request request) {
        return request.session().attribute("currentUser");
	}
	
}
