package com.BryceBG.DatabaseTools.Database.User;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.App;
import com.BryceBG.DatabaseTools.Database.LibraryDB;
import com.BryceBG.DatabaseTools.utils.Utils;

/**
 * This class is our Data Access object for querying the database for user related information.
 * @author Bryce-BG
 */
public class UserDao {
	private static final Logger logger = LogManager.getLogger(UserDao.class.getName());
	private static LibraryDB library;
	static {
		//need to run our config for library instance before any calls are made our the 
		library = new LibraryDB(Utils.getConfigString("app.dbhost", null), Utils.getConfigString("app.dbport", null), Utils.getConfigString("app.dbname", null),Utils.getConfigString("app.dbpass", null) , Utils.getConfigString("app.dbuser", null));
	}


	/**
	 * Get a user from the database
	 * @param username the username of the user to lookup
	 * @return returns null if no user was found. Otherwise a User object containing data from the database is returned
	 */
    public static User getUserByUsername(String username) {
    	User rtVal = null;
    	String sql =
                "SELECT * " +
                "FROM USERS " + 
                "WHERE username=?";	
    	//Use of try with resources to auto close connection examples:
    	//https://stackoverflow.com/questions/38545507/postgresql-close-connection-after-method-has-finished
    	//https://stackoverflow.com/questions/8066501/how-should-i-use-try-with-resources-with-jdbc
    	
    	//1. establish connection to our database
    	try (Connection conn = library.connectToDB();        
	            PreparedStatement pstmt = conn.prepareStatement(sql);
	            ) {
    		pstmt.setString(1, username);

    		//2. execute our query for user.
            try (ResultSet rs = pstmt.executeQuery()) {
            	
                
	            if (rs.next()) { //3. check if sql query for user returned an answer.
		           	//4. extract results from result set needed to create User object
	            	int userID = rs.getInt("user_id"); 
	            	String userName = rs.getString("USERNAME"); 
	            	String salt = rs.getString("salt"); 
	            	String hashedPassword = rs.getString("hashedPassword");
	            	String firstName = rs.getString("first_name");
	            	String lastName = rs.getString("last_name");
	            	String email = rs.getString("email");
	            	boolean admin = rs.getBoolean("IS_ADMIN");
					//5. create User object for the results
	            	rtVal = new User(userID, userName, salt, hashedPassword, firstName, lastName, email, admin);
	            }
	            else {
	            	logger.info(String.format("The query for user %s returned null. I.e. no user in the database was a match.", username));
	            }
	            
            } //end of try-with-resources: result set
        } //end of try-with-resources: connection 
    	//catch blocks for try-with-resources: connection
    	catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		}catch (SQLException e) { 
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
        }
        return rtVal;
    	}
    	
    	
    	
    	


	public static boolean changePassword(String username, String newSalt, String newHashedPassword) {
		boolean rtVal = false;
		if(getUserByUsername(username) != null) {			
			String sql = "UPDATE users SET salt=?, hashedPassword=? WHERE username=?";
					
			//1. establish connection to our database
	    	try (Connection conn = library.connectToDB();        
		            PreparedStatement pstmt = conn.prepareStatement(sql);
		            ) {
	    		//2. set parameters in the prepared statement
	    		pstmt.setString(1, newSalt);
	    		pstmt.setString(2, newHashedPassword);
	    		pstmt.setString(3, username);

	    		

	    		//3. execute our query for user.
	            int rtUp = pstmt.executeUpdate();
	            
	            //4. check if sql update preformed operation and updated only one row.
		            if (rtUp==1) {
		            	rtVal = true; 
		            }
		            else {
						logger.warn(String.format("Updating a user's password has incorrectly updated %s rows", rtUp));
		            }
		            
	        } //end of try-with-resources: connection 
	    	//catch blocks for try-with-resources: connection
	    	catch (ClassNotFoundException e) {
				logger.error("Exception occured during connectToDB: " + e.getMessage());
			}catch (SQLException e) { 
				logger.error("Exception occured during executing SQL statement: " + e.getMessage());
	        }		
		}
		else
		{
        	logger.info(String.format("The query for user %s returned null. I.e. no user in the database was a match.", username));
		}
		return rtVal;
		
	}

}
