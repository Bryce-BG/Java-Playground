package com.BryceBG.DatabaseTools.Database.Login;

import java.util.Map;

import com.BryceBG.DatabaseTools.Database.User.UserController;
import com.BryceBG.DatabaseTools.utils.Path;
import com.BryceBG.DatabaseTools.utils.RequestUtil;
import com.BryceBG.DatabaseTools.utils.ViewUtil;

import io.javalin.http.Handler;

public class LoginController {
	

    public static Handler serveLoginPage = ctx -> {
        Map<String, Object> model = ViewUtil.baseModel(ctx);
        model.put("loggedOut", RequestUtil.removeSessionAttrLoggedOut(ctx));
        model.put("loginRedirect", RequestUtil.removeSessionAttrLoginRedirect(ctx));
        ctx.render(Path.Template.LOGIN, model);
    };

    public static Handler handleLoginPost = ctx -> {
        Map<String, Object> model = ViewUtil.baseModel(ctx);
        if (!UserController.authenticate(RequestUtil.getQueryUsername(ctx), RequestUtil.getQueryPassword(ctx))) {
            model.put("authenticationFailed", true); //indicate login was unsuccessful

            ctx.render(Path.Template.LOGIN, model);
        } 
        else { //user was able to authenticate so:
            ctx.sessionAttribute("currentUser", RequestUtil.getQueryUsername(ctx));  
            model.put("authenticationSucceeded", true);
            model.put("currentUser", RequestUtil.getQueryUsername(ctx));
            if (RequestUtil.getQueryLoginRedirect(ctx) != null) {
                ctx.redirect(RequestUtil.getQueryLoginRedirect(ctx)); //go to new location that is not the default location?
            }
            ctx.render(Path.Template.LOGIN, model);
        }
    };

    public static Handler handleLogoutPost = ctx -> {
        ctx.sessionAttribute("currentUser", null);
        ctx.sessionAttribute("loggedOut", "true");
        ctx.redirect(Path.Web.LOGIN);
    };

    // The origin of the request (request.pathInfo()) is saved in the session so
    // the user can be redirected back after login
    public static Handler ensureLoginBeforeViewingBooks = ctx -> {
        if (!ctx.path().startsWith("/books")) {
            return;
        }
        if (ctx.sessionAttribute("currentUser") == null) {
            ctx.sessionAttribute("loginRedirect", ctx.path());
            ctx.redirect(Path.Web.LOGIN);
        }
    };

}
