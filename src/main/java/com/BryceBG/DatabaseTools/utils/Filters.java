package com.BryceBG.DatabaseTools.utils;

import io.javalin.http.Handler;

public class Filters {

    // Locale change can be initiated from any page
    // The locale is extracted from the request and saved to the user's session
    public static Handler handleLocaleChange = ctx -> {
        if (RequestUtil.getQueryLocale(ctx) != null) {
            ctx.sessionAttribute("locale", RequestUtil.getQueryLocale(ctx));
            ctx.redirect(ctx.path());
        }
    };
	public static Handler stripTrailingSlashes = ctx -> {
		System.out.println("path requested is: " + ctx.path());
	};

}
