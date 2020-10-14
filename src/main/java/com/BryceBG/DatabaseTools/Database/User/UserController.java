package com.BryceBG.DatabaseTools.Database.User;


import org.javatuples.Pair;
import org.javatuples.Tuple;
import org.mindrot.jbcrypt.BCrypt;

import com.BryceBG.DatabaseTools.Database.DAORoot;

/**
 * This class acts as a thin wrapper and provides higher level functionality to the UserDao class. 
 * For example, it allows manipulation of user objects like editing user accounts, creating and deleting accounts and authenticating current users.
 * @author Bryce-BG
 *
 */
public class UserController {

    // Authenticate the user by hashing the inputted password using the stored salt,
    // then comparing the generated hashed password to the stored hashed password
	/**
	 * This function authenticates the user by hashing the inputted password using the stored salt and then comparing the generated hashed password to the stored hashed password
	 * @param username The username for the user trying to login
	 * @param password The plaintext password of the user trying to login
	 * @return true if user exists and the password hashes to the correct value. False otherwise.
	 */
    public static boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        User user = DAORoot.userDao.getUserByUsername(username);
        if (user == null) {
            return false;
        }
        String hashedPassword = BCrypt.hashpw(password, user.getSalt());
        return hashedPassword.equals(user.getHashedPassword()); //did it hash correctly or not
    }

    /**
     * Set a new password for the provided username.
     * @param username The username for the account of the the password we are changing.
     * @param oldPassword The current password in the system
     * @param newPassword The replacement password for the account.
     */
    public static void setPassword(String username, String oldPassword, String newPassword) {
        if (authenticate(username, oldPassword)) {
            String newSalt = BCrypt.gensalt();
            String newHashedPassword = BCrypt.hashpw(newPassword, newSalt);
            DAORoot.userDao.changeUserPassword(username, newSalt, newHashedPassword); 
        }
    }
    
    public static Pair<Boolean, String> createNewUser(String creatingUsername, String creatingUserPass, String username, String password, String fName, String lName, String email, boolean is_admin) {
    	//1. call authenticate() to ensure user creating the new account is a valid user
    	if(!authenticate(creatingUsername, creatingUserPass)) {
    		return new Pair<Boolean, String>(Boolean.FALSE, "Invalid user performing Create User action");
    	}
    	//2. ensure user creating new account is an admin
    	if(!DAORoot.userDao.getUserByUsername(creatingUsername).isAdmin()) {
    		return new Pair<Boolean, String>(Boolean.FALSE, "Invalid user performing Create User action (no account creation permission)");
    	}
    
    	//3. validate no fields required for user are empty or null (or have invalid values).
    	if(!validateUserFields(username, password, fName, lName, email).getValue0().booleanValue()) 
		{
			return validateUserFields(username, password, fName, lName, email); //return indicator for failed creation and why.
		}
    	//4. everything is valid so call userDao.addUser() to create the user
    	String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);
    	boolean rtVal = DAORoot.userDao.addUser(username, hashedPassword, salt, fName, lName, email, is_admin);
    	if(rtVal == false) {
    		return new Pair<Boolean, String>(Boolean.FALSE, "Account Creation Failed. Please try again.)");
    	}
    	else {
    		return new Pair<Boolean, String>(Boolean.TRUE, "SUCCESS!");
    	}    	
    }
    
    
    //Helper functions for createNewUser
    /**
     * A helper function for createNewUser()
     * This function takes in the parameters required to create a new user and ensures that they are not going to cause an issue.
     * @param username the username for the new account
     * @param password password for the new account
     * @param fName new account holder's first name
     * @param lName new account holder's last name
     * @param email new account holder's email
     * @return a tuple with (True, "") OR (False, <Reason for invalid>)
     */
    private static Pair<Boolean, String> validateUserFields(String username, String password, String fName, String lName, String email) {
    if (username == null || username.isBlank() || username.isEmpty()){
    	return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: Username");
    }
    if(DAORoot.userDao.getUserByUsername(username)!=null) {
    	return new Pair<Boolean, String>(Boolean.FALSE, "Username is taken");
    }
    if(password == null || password.isBlank() || password.isEmpty()) { //TODO Add additional requirements for password length and complexity here: check_password_meets_requirements()
    	return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: Password");
    }
    if(fName == null || fName.isBlank() || fName.isEmpty() ) { //TODO Add additional constraints here. Only alpha characters, no numerics, etc.
    	return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: First name");
    }
    if(lName == null || lName.isBlank() || lName.isEmpty()) {
    	return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: Last name");
    }
    if(email == null || email.isBlank() || email.isEmpty() || !is_valid_email(email)) { //TODO replace is_valid_email() with a more sophisticated method to ensure email validity
    	return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: Email address");
    }
    return new Pair<Boolean, String>(Boolean.TRUE, "");
    }
    /**
     * Helper function that ensures that an email is semi-valid using regex. 
     * @param email: email to check
     * @return
     */
    private static boolean is_valid_email(String email) {
    	   String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
    	   return email.matches(regex);
    	}
    
    /**
     * Helper function that is used to ensure password meets specifications.
     * length>=8, 
     * contains a capital letter
     * Contains alpha and numeric characters
     * 
     * @param password: password to check
     * @return: true if password meets all requirements. false otherwise
     */
    private static boolean check_password_meets_requirements(String password) {
    	if (password == null) 
    		return false;
    	if(password.length()<8) //length requirement
    		return false;
    	if (password.toLowerCase().equals(password) || password.toUpperCase().equals(password)) //was all uppercase or all lowercase
    		return false;
    	
        String n = ".*[0-9].*"; //numeric regex
        String a = ".*[A-Z].*"; //alpha regex
        if(!(password.matches(n) && password.matches(a)))//does NOT contain both letters and numbers
        	return false;

    	return true;
    }

}

