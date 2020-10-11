package com.BryceBG.DatabaseTools.utils;

import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.ErrorHandler;

import static com.BryceBG.DatabaseTools.utils.RequestUtil.*;

public class ViewUtil {

	//used to create object indicating current locale (read language) and login status.
    public static Map<String, Object> baseModel(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("msg", new MessageBundle(getSessionLocale(ctx))); //get appropriate text to display based on language set.
        model.put("currentUser", getSessionCurrentUser(ctx));
        return model;
    }

    public static ErrorHandler notFound = ctx -> {
        ctx.render(Path.Template.NOT_FOUND, baseModel(ctx));
    };
    
}
