package com.BryceBG.DatabaseTools.Database.User;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.Database.DAORoot; //for our instantiated objects inheritence
/**
 * This class is our Data Access object for querying the database for user related information.
 * @author Bryce-BG
 */
public class UserDao extends DAORoot{
	private static final Logger logger = LogManager.getLogger(UserDao.class.getName());
	

	/**
	 * Get a user from the database
	 * @param username the username of the user to lookup
	 * @return returns null if no user was found. Otherwise a User object containing data from the database is returned
	 */
	public User getUserByUsername(String username) {
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
    	
    /**
     * This function gets all usernames in the system and returns them.
     * @return
     */
    public Iterable<String> getAllUserNames() {
    	ArrayList<String> userNames = new ArrayList<String>();

    	String sql =
                "SELECT * " +
                "FROM USERS ";	
    	//Use of try with resources to auto close connection examples:
    	//https://stackoverflow.com/questions/38545507/postgresql-close-connection-after-method-has-finished
    	//https://stackoverflow.com/questions/8066501/how-should-i-use-try-with-resources-with-jdbc
    	
    	//1. establish connection to our database
    	try (Connection conn = DAORoot.library.connectToDB();        
	            PreparedStatement pstmt = conn.prepareStatement(sql);
	            ) {
    		//2. execute our query for user.
            try (ResultSet rs = pstmt.executeQuery()) {
	            while(rs.next()) { //3. loop through results and get usernames.
	            	String userName = rs.getString("USERNAME"); 
	            	userNames.add(userName);
	            }
            } //end of try-with-resources: result set
        } //end of try-with-resources: connection 
    	//catch blocks for try-with-resources: connection
    	catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		}catch (SQLException e) { 
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
        }
        return userNames;
    }
    	
    	


	public boolean changeUserPassword(String username, String newSalt, String newHashedPassword) {
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
	            
	            //4. check if sql update performed operation and updated only one row.
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
	
	
	public boolean addUser(String username, String hashedPassword, String salt, String fName, String lName, String email, boolean is_admin) {
		boolean rtVal = false;
		String sql = "INSERT INTO USERS(username, hashedPassword, salt, first_name, last_name, email, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?);";
		//1. establish connection to our database
    	try (Connection conn = library.connectToDB();        
	            PreparedStatement pstmt = conn.prepareStatement(sql);
	            ) {
    		pstmt.setString(1, username);
    		pstmt.setString(2, hashedPassword);
    		pstmt.setString(3, salt);
    		pstmt.setString(4, fName);
    		pstmt.setString(5, lName);
    		pstmt.setString(6, email);
    		pstmt.setBoolean(7, is_admin);

    		//2. execute our update for adding user.
            int rs = pstmt.executeUpdate();
          //3. check if sql query for user returned correct answer.
            if (rs == 1) { 
	           	//update was successful
            	rtVal = true;
            }
            else {
            	logger.info(String.format("The addUser failed: the execute update returned: %i", rs));
            	rtVal = false;
            }  
        } //end of try-with-resources: connection 
    	//catch blocks for try-with-resources: connection
    	catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		}catch (SQLException e) { 
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
        }
        return rtVal;
    	
	}
	

	

}
