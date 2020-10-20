package com.BryceBG.DatabaseTools.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.App;
import com.BryceBG.DatabaseTools.utils.Utils;

/**
 * 
 * @author Bryce-BG
 * This class is for interfacing with the postgres library database and allowing programmatic access.
 */
public class LibraryDB {
	private static final Logger logger = LogManager.getLogger(App.class.getName());

	//DEFAULT parameters for DB access (can be overridden via constructor)
	private String DB_HOST = "localHost";
	private String DB_PORT = "5432";
	private String DB_NAME = "librarydatabase";
	private String DB_PASSWORD = "postgres"; 
	private String DB_USER = "postgres"; 
	

	
	//constructor (optional override for default database parameters)
	public LibraryDB(String dbHost, String dbPort, String dbName, String dbPassword, String dbUsername) {
		DB_HOST = dbHost;
		DB_PORT = dbPort;
		DB_NAME = dbName;
		DB_PASSWORD = dbPassword;
		DB_USER = dbUsername; 
	}

	
	

	
//	/**
//	 * Connect to a postgresql database with custom parameters (stored in the globals after they are recieved.
//	 * @param host: address of the server for the database
//	 * @param port: port to use when connecting
//	 * @param username: username to login with
	
	
	/**#############getter functions for DB checks############# **/
	

	/**
	 * Get all available book titles from the database.
	 * @return String array containing all available book titles.
	 */
	public String[] getAllTitles(){
		//https://www.postgresql.org/docs/7.4/jdbc-query.html
		ArrayList<String> books= new ArrayList<String>(); //the names of all books in database returned by our query
		Statement stmt = null; //sql statement
		ResultSet rs = null; //results
		Connection conn = null;

		try {
			conn = connectToDB();
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery("SELECT title FROM books");
			while (rs.next()) {
			    books.add(rs.getString(1));//add column 1 from current row to the list to be returned
			}
			return (String[]) books.toArray();
			
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		finally{
		      //finally block used to close resources
		      try{
		         if(stmt!=null)
		            conn.close();
		      }catch(SQLException se){
		      }// do nothing
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }//end finally try
		   }
		return null;


	}
	
	
	
	
	
	
	
	/**##########################################################*/
	

	

	/**
	 * Connect to a postgresql database (librarydatabase) with default parameters
	 * @throws ClassNotFoundException: if no postgres driver was able to be used
	 * @throws SQLException: if the connection failed due to invalid parameters
	 */
	public Connection connectToDB() throws SQLException, ClassNotFoundException{
		Connection conn = null;
		Class.forName("org.postgresql.Driver"); //register the driver
		String url = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);
		
		//establish connection to db at the provided url
		try {
			conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
			return conn; 
		} catch (SQLException e) {
			logger.error("Error in connecting to the database with the supplied parameters" + url); 
			throw e;
		}
	}
	


	

	
	/**
	 * This function is intended to be run once to initialize the database for future use.
	 * WARNING This WILL drop existing database if a database with same name exists in system
	 * @param libraryName the name for the postgresql database to create
	 */
	public static boolean createDB(String libraryName) {
		//TODO rewrite this function as at this time it is vulnerable to sql injection
		if(libraryName == null || libraryName.isEmpty() || libraryName.isBlank()) {
			libraryName = "librarydatabase";
            logger.warn(String.format("The name passed into CreateDB for the database was invalid so it was changed to: %s.", libraryName));
		}
	    try {
	    	
	    		List<String> dbNames = listAllDatabases(); //get names of current databases
	    		for(String x: dbNames) 
	    		{
	    			if(libraryName.equals(x)) {
		            	logger.warn(String.format("Existing database was found with name %s. (and then dropped).", libraryName));
		            	break;
	    			}
	    		}
	    	
	    		Connection connection = connectToPostGres();
	            Statement stmt = connection.createStatement();
	            //Drop database if it already exists  (reset the complete database)
	            String sql = String.format("DROP DATABASE %s", libraryName);
	            stmt.executeUpdate(sql);
	              
	          stmt = connection.createStatement();
	           
	          sql = String.format("CREATE DATABASE %s", libraryName); //Create Database
	          stmt.executeUpdate(sql); 
	          
	          logger.info(String.format("Successfully created database: %s", libraryName));
	          return true;
	    }
	     catch (Exception ex) {
	    	 logger.error("error occured during createDB + \n" + ex.getMessage());
	}
	    return false;
	}
	
	/**
	 * Helper function that connects to postgres and not our library db (so we can delete it with createDB())
	 * @return connection to default postgres database.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private static Connection connectToPostGres() throws SQLException, ClassNotFoundException {
		Connection conn = null;
		Class.forName("org.postgresql.Driver"); //register the driver
		String host = Utils.getConfigString("app.dbhost", null);
		String port = Utils.getConfigString("app.dbport", null);
		String url = String.format("jdbc:postgresql://%s:%s/?", host, port);
		
		//establish connection to db at the provided url
		try {
			conn = DriverManager.getConnection(url, Utils.getConfigString("app.dbuser", null), Utils.getConfigString("app.dbpass", null));
			return conn; 
		} catch (SQLException e) {
			logger.fatal("failed to connect to database" + e.getMessage());
			throw e;
		}
	}
	/**
	 * Helper function for createDB(). Queries postgresql to get database names in system.
	 * @return returns a list of names for all currently existing databases
	 */
	private static List<String> listAllDatabases() {
		List<String> names = new ArrayList<String>();
        try(Connection connection = connectToPostGres();)
        {
        	String sql = "SELECT datname FROM pg_database WHERE datistemplate = false;";
            PreparedStatement ps = connection.prepareStatement(sql);
            try(ResultSet rs = ps.executeQuery();){
	            while (rs.next()) {
	            	String name = rs.getString(1);
	            	names.add(name);
	            }
            } //end try-with-resource: ResultSet
        } 
        catch (Exception e) {
        	logger.error("An error occured trying to list all databases\n" + e.getMessage());
        }
		return names;
    }

	/**
	 * This function is used to run raw sql (vulnerable to injection and many other things), so it is ill advised to run it for anything except as debugging.
	 * TODO remove this function from final version of program to prevent misuse.
	 * @param sql the sql to be used.
	 * @return
	 */
	public boolean runRawSQL(String sql) {
		boolean rtVal = false;
		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {

			// 2. execute our update for removing series.
			int rs = pstmt.executeUpdate();
			// 3. check if sql query for series returned correct answer: should have added 1
			// unless it failed
			// row).
			if (rs == 1) {
				// update was successful
				rtVal = true;
			} else {
				rtVal = false;
			}
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
		
	}
	



}


