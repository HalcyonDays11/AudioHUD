package org.directivexiii.web.controllers;

import java.util.HashMap;
import java.util.Map;

import org.directivexiii.web.util.Path;
import org.directivexiii.web.util.ViewUtil;

import spark.Request;
import spark.Response;
import spark.Route;

public class SuggestController {

	public static Route serveSuggestPage = (Request request, Response response) -> {
		Map<String,Object> model = new HashMap<>();
		return ViewUtil.render(request, model, Path.Template.SUGGEST);
	};
	
}
