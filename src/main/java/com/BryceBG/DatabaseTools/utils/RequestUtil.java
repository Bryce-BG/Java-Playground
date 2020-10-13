package com.BryceBG.DatabaseTools.utils;

import io.javalin.http.Context;

/**
 * Essentually this is a shortcut class that allows us to extract parameters from our http requests in a easy manner in a more easilly readible manner.
 * @author Bryce-BG
 *
 */
public class RequestUtil {

	/**
	 * Extract the parameter from context that indicates what locale the page is requesting so content can be loaded in the correct language.
	 * @param ctx Context parameter (i.e. the current state of system for the user).
	 * @return String containing the locale. (usually 'en' would be expected for our site currently).
	 */
    public static String getQueryLocale(Context ctx) {
        return ctx.queryParam("locale");
    }

    /**
     * Extract the "identifier" sent with request. This is two strings that form a tuple to identify a book uniquely according to the specified scheme. 
     * For example (ISBN: isbn_value) or: (MOBI-ASN: SHDA4N) are both "identifiers" for a book.
     * @param ctx The returned state of a request holding parameters that we can extract.
     * @return String array of length 2 where the str[0] = id_scheme, and str[1] = id_val 
     */
    public static String[] getParamIdentifier(Context ctx) {
    	String id_type = ctx.pathParam("identifier_name");
    	String id_val = ctx.pathParam("identifier_val");
    	
        return new String[] {id_type, id_val};
    }

    /**
     * Gets the currently logged in user performing the requests.
     * @param ctx The returned state of a request holding parameters that we can extract.
     * @return String containing the username.
     */
    public static String getQueryUsername(Context ctx) {
        return ctx.formParam("username");
    }

    public static String getQueryPassword(Context ctx) {
        return ctx.formParam("password");
    }

    public static String getQueryLoginRedirect(Context ctx) {
        return ctx.queryParam("loginRedirect");
    }

    public static String getSessionLocale(Context ctx) {
        return (String) ctx.sessionAttribute("locale");
    }

    public static String getSessionCurrentUser(Context ctx) {
        return (String) ctx.sessionAttribute("currentUser");
    }

    //actions that actually effect the model
    public static boolean removeSessionAttrLoggedOut(Context ctx) {
        String loggedOut = ctx.sessionAttribute("loggedOut");
        ctx.sessionAttribute("loggedOut", null);
        return loggedOut != null;
    }

    public static String removeSessionAttrLoginRedirect(Context ctx) {
        String loginRedirect = ctx.sessionAttribute("loginRedirect");
        ctx.sessionAttribute("loginRedirect", null);
        return loginRedirect;
    }

}
