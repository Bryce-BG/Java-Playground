package com.BryceBG.DatabaseTools.Database.User;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.mindrot.jbcrypt.BCrypt;

import com.BryceBG.DatabaseTools.Database.DAORoot;

/**
 * This class acts as a thin wrapper and provides higher level functionality to
 * the UserDao class. For example, it allows manipulation of user objects like
 * editing user accounts, creating and deleting accounts and authenticating
 * current users.
 * 
 * @author Bryce-BG
 *
 */
public class UserController {
	private static final Logger logger = LogManager.getLogger(UserController.class.getName());

	// Authenticate the user by hashing the inputted password using the stored salt,
	// then comparing the generated hashed password to the stored hashed password
	/**
	 * This function authenticates the user by hashing the inputted password using
	 * the stored salt and then comparing the generated hashed password to the
	 * stored hashed password
	 * 
	 * @param username The username for the user trying to login
	 * @param password The plaintext password of the user trying to login
	 * @return true if user exists and the password hashes to the correct value.
	 *         False otherwise.
	 */
	public static boolean authenticate(String username, String password) {
		if (username == null || username.isBlank() || username.isEmpty() || password == null) {
			return false;
		}
		User user = DAORoot.userDao.getUserByUsername(username);
		if (user == null) {
			return false;
		}
		String hashedPassword = BCrypt.hashpw(password, user.getSalt());
		return hashedPassword.equals(user.getHashedPassword()); // did it hash correctly or not
	}

	/**
	 * Set a new password for the provided username.
	 * 
	 * @param username    The username for the account of the the password we are
	 *                    changing.
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

	/**
	 * This function allows the creation of new users in our library system.
	 * 
	 * @param creatingUsername This is the admin's username who is creating the new
	 *                         account
	 * @param creatingUserPass This is the admin's password to ensure it is really
	 *                         the admin (authorization takes place)
	 * @param username         username for the new account (can't already be in
	 *                         system)
	 * @param password         password for the new account
	 * @param fName            first name of new account owner
	 * @param lName            last name of new account owner
	 * @param email            email for the new account owner (can't already be in
	 *                         system)
	 * @param is_admin         should the new account have administrative
	 *                         capabilities.
	 * @return
	 */
	public static Pair<Boolean, String> createNewUser(String creatingUsername, String creatingUserPass, String username,
			String password, String fName, String lName, String email, boolean is_admin) {
		// 1. Ensure user creating the new account is valid user
		if (!authenticate(creatingUsername, creatingUserPass)) {
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid user performing Create User action");
		}
		// 2. ensure user creating new account is an admin
		if (!DAORoot.userDao.getUserByUsername(creatingUsername).isAdmin()) {
			return new Pair<Boolean, String>(Boolean.FALSE,
					"Invalid user performing Create User action (no account creation permission)");
		}
		// 3.a. trim potential whitespace tabs and other such characters off from fields where it may matter
		username = username.strip().toLowerCase(); 
		email = email.strip().toLowerCase(); //email ignores casing

		//3.b. validate all required fields for new user meet specifications
		if (!validateUserFields(username, password, fName, lName, email).getValue0().booleanValue()) {
			return validateUserFields(username, password, fName, lName, email);
		}
		// 4. everything is valid so call userDao.addUser() to add user to DB
		String salt = BCrypt.gensalt();
		String hashedPassword = BCrypt.hashpw(password, salt);
		logger.info(String.format("User: %s is creating new User: %s", creatingUsername, username));
		boolean rtnedVal = DAORoot.userDao.addUser(username, hashedPassword, salt, fName, lName, email, is_admin);
		if (rtnedVal == false) {
			return new Pair<Boolean, String>(Boolean.FALSE, "Account Creation unexpectedly failed. Please try again.)");
		} else {
			return new Pair<Boolean, String>(Boolean.TRUE, "SUCCESS!");
		}
	}

	/**
	 * This function is to delete a user from our system. It can only be called by
	 * the owner of an account or an admin
	 * 
	 * @param deleteAuthorizingUsername The username for whoever called the delete
	 *                                  function
	 * @param deleteAuthorizingUserPass The plaintext password of whoever called the
	 *                                  delete function
	 * @param userToRemove              The username of the user we are removing
	 *                                  from the system.
	 * @return A pair consisting of: (Boolean.FALSE, <REASON_FOR_FAILURE>) or
	 *         (Boolean.TRUE, "SUCCESS!") indicating successful account deletion.
	 */
	public static Pair<Boolean, String> deleteUser(String deleteAuthorizingUsername, String deleteAuthorizingUserPass,
			String userToRemove) {
		// 1. call authenticate() to ensure user trying to delete account is a valid
		// user
		if (!authenticate(deleteAuthorizingUsername, deleteAuthorizingUserPass)) {
			// not a successful authentication
			logger.info(
					String.format("And invalid user attempted to perform delete operation on user %s", userToRemove));
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid user performing Delete User action");
		}
		// 2. check they have requisite permissions to delete user. I.e. they are an
		// administrator or the owner of the account.
		if (!deleteAuthorizingUsername.equalsIgnoreCase(userToRemove)
				&& !DAORoot.userDao.getUserByUsername(deleteAuthorizingUsername).isAdmin()) {
			logger.info(String.format(
					"User %s attempted to perform delete operation on user %s. However, they lack access to do so.",
					deleteAuthorizingUsername, userToRemove));
			return new Pair<Boolean, String>(Boolean.FALSE,
					"Invalid user to perform Delete User action on this account");
		}

		// 3. delete account
		logger.info(String.format("User: %s performed delete on User: %s", deleteAuthorizingUsername, userToRemove));
		boolean rtnedVal = DAORoot.userDao.removeUser( userToRemove);
		if (rtnedVal == false) {
			return new Pair<Boolean, String>(Boolean.FALSE, "Account deletion unexpectedly failed. Please try again.)");
		} else {
			return new Pair<Boolean, String>(Boolean.TRUE, "SUCCESS!");
		}

	}

	// Helper functions for createNewUser
	/**
	 * A helper function for createNewUser() This function takes in the parameters
	 * required to create a new user and ensures that they are not going to cause an
	 * issue.
	 * 
	 * @param username the username for the new account
	 * @param password password for the new account
	 * @param fName    new account holder's first name
	 * @param lName    new account holder's last name
	 * @param email    new account holder's email
	 * @return a tuple with (True, "") OR (False, <Reason for invalid>)
	 */
	private static Pair<Boolean, String> validateUserFields(String username, String password, String fName,
			String lName, String email) {
		if (username == null || username.isBlank() || username.isEmpty() || !is_valid_username(username)) {
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: Username.");
		}
		if (DAORoot.userDao.getUserByUsername(username) != null) {
			return new Pair<Boolean, String>(Boolean.FALSE, "Username is taken");
		}
		if (password == null || password.isBlank() || password.isEmpty()) {
			// TODO Add additional requirements for password length and complexity here:
			// check_password_meets_requirements()
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: Password");
		}
		if (fName == null || fName.isBlank() || fName.isEmpty()) {
			// TODO Add additional constraints here. Only alpha characters, etc.
			//also ensure length is in valid range for our database 30 chars
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: First name");
		}
		if (lName == null || lName.isBlank() || lName.isEmpty()) {
			// TODO Add additional constraints here. Only alpha characters, etc.
			//also ensure length is in valid range for our database 30 chars
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: Last name");
		}
		if (email == null || email.isBlank() || email.isEmpty() || !is_valid_email(email)) {
			// TODO replace is_valid_email() with a more sophisticated method to ensure
			// email validity
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid: Email address");
		}
		if (DAORoot.userDao.getUserByEmailAddress(email) != null) {
			return new Pair<Boolean, String>(Boolean.FALSE, "Email is already in use");
		}
		return new Pair<Boolean, String>(Boolean.TRUE, "");
	}

	/**
	 * Helper function that ensures that an email is semi-valid using regex.
	 * 
	 * @param email: email to check
	 * @return
	 */
	private static boolean is_valid_email(String email) {
		String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		return email.matches(regex);
	}
	
	/**
	 * Our helper function is used to ensure our username meets the requirements
	 * 1. username is 5-30 characters long
	 * 2. first character is lowercase a-z
	 * 3. username contains only a-z, 0-9 and _ - chars
	 * @param username user to check
	 * @return true if it matches the above constraints
	 */
	private static boolean is_valid_username(String username) {
		String regex = "^[a-z][a-z0-9_-]{4,29}$";
        return username.matches(regex);
	}

	/**
	 * Helper function that is used to ensure password meets specifications.
	 * length>=8, contains a capital letter Contains alpha and numeric characters
	 * 
	 * @param password: password to check
	 * @return: true if password meets all requirements. false otherwise
	 */
	private static boolean check_password_meets_requirements(String password) {
		if (password == null)
			return false;
		if (password.length() < 8) // length requirement
			return false;
		if (password.toLowerCase().equals(password) || password.toUpperCase().equals(password)) // was all uppercase or
																								// all lowercase
			return false;

		String n = ".*[0-9].*"; // numeric regex
		String a = ".*[A-Z].*"; // alpha regex
		if (!(password.matches(n) && password.matches(a)))// does NOT contain both letters and numbers
			return false;

		return true;
	}

}
