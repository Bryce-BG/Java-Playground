package com.BryceBG.DatabaseTools.Database.User;

/**
 * This class is a Java representation of our user objects contained in the database.
 * @author Bryce-BG
 *
 */
public class User {
	private int userId; //unique
    private String username; //primary_key
    private String salt;
    private String hashedPassword;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isAdmin;

    public User(int userId, String username, String salt, String hashedPassword, String firstName, String lastName, String email, boolean isAdmin) {
        this.setUserId(userId);
    	this.setUsername(username);
        this.setSalt(salt);
        this.setHashedPassword(hashedPassword);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setAdmin(isAdmin);
    }


	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}

	
	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAdmin() {
		return isAdmin;
	}
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
}

