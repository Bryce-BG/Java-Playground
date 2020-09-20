package com.BryceBG.DatabaseTools.Database;

/**
 * Helper class that stores a user's information relating to database access.
 * @author Limited1
 *
 */
public class Credentials {
	private final String username;
	private final String password;
	private final boolean is_admin;
	
	Credentials(String ur, String pw, boolean admin){
		username = ur;
		password = pw;
		is_admin = admin;
		
	}
	public Credentials() { //Uninitialized credentials
		username = null;
		password = null;
		is_admin = false;
	}
	public String get_username() {
		return username;
	}
	public String get_password() {
		return password;
	}
	public boolean get_permissions() {
		return is_admin;
	}
	
	/**
	 * Simple helper function that indicates if credentials is a valid object in our system.
	 * @return
	 */
	public boolean is_valid_credentials() {
		return (username!=null && password != null);
	}
		

}
