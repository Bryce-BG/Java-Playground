package com.BryceBG.DatabaseTools.Database.User;


import org.mindrot.jbcrypt.BCrypt;


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
        User user = UserDao.getUserByUsername(username);
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
            UserDao.changePassword(username, newSalt, newHashedPassword); //
        }
    }
    

}

