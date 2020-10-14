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

	/** ############# USER RELATED DATABASE FUNCTIONS ############# **/
	
	


	/**
	 * Function to allow an admin user to delete a user account from the system
	 * @param yourAdmin Credentials token of the admin performing the deletion function
	 * @param username username of the account we want to delete
	 * @param email secondary variable to ensure deletion of the correct account.
	 * @return
	 * -1 = unable to connect to the database
	 * -2 = user doesn't exist in database with provided email/username combo
	 * -3 = error occurred during the deletion of user
	 * -4 = invalid admin credentials
	 */
	@Deprecated
	public int delete_user(/*Credentials yourAdmin,*/ String username, String email) {
		if(true) //yourAdmin.is_valid_credentials() && validate_credentials(yourAdmin) && yourAdmin.get_permissions()) 
			{
			Connection conn = null;
			PreparedStatement stmt = null;
			email = email.toLowerCase();
			
			try {
				conn = connectToDB();
				username = username.toLowerCase(); 
				if(conn.isValid(0)) {
					
					//1. check username is unique (not in database already)
			            String sql =
			                    "SELECT * " +
			                    "FROM USERS " + 
			                    "WHERE username=? AND email=?"; 		            
			            
			            stmt = conn.prepareStatement(sql);
			            stmt.setString(1, username);
			            stmt.setString(2, email);
			            ResultSet rs = stmt.executeQuery();
			            if (rs.next()) {  //user DOES exist so delete it
			            	sql =
			                    "DELETE " +
			                    "FROM USERS " + 
			                    "WHERE username=? AND email=?"; 
				            stmt = conn.prepareStatement(sql);
				            stmt.setString(1, username);
				            stmt.setString(2, email);
				            int rv = stmt.executeUpdate();
				            if(rv == 1)
				            	return 0;
				            else
				            	return -3; //error occured during the deletion of user          	
			            }         	
			            else {
			            	return -2; //user doesn't exist in db (so can't perform delete)
			            }
				}
				else
					return -1;
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
				return -1;
			}
			finally{ //finally block used to close resources
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
			   }//end finally
		}
		else {
			return -4; //invalid admin credentials
		}
	}

	
	
	
	
	
	/**#############FUNCTIONS TO PERFORM QUERYING OF NON-USER RELATED DATA FROM DATABASE#############**/
	
	
	

	
//	/**
//	 * Connect to a postgresql database with custom parameters (stored in the globals after they are recieved.
//	 * @param host: address of the server for the database
//	 * @param port: port to use when connecting
//	 * @param username: username to login with
	
	
	/**#############getter functions for DB checks############# **/
	
	/**
	 * Get the names of all series currently listed in the database.
	 * @return String array of names of series from the DB.
	 */
	public String[] getAllSeries(){
		ArrayList<String> series_names = new ArrayList<String>(); //the names of all series in databae returned by our query
		Statement stmt = null; //sql statement
		ResultSet rs = null; //results
		Connection conn = null;
			try {
				conn = connectToDB();
				
				//https://www.postgresql.org/docs/7.4/jdbc-query.html
				stmt = conn.createStatement();
				
					rs = stmt.executeQuery("SELECT series_name FROM series");
					while (rs.next()) {
					    series_names.add(rs.getString(1));//add column 1 from current row to the list to be returned
					}

				
				return (String[]) series_names.toArray();
				
				
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
	
	/**
	 * Get all author names (fname lname) concatenated currently in the database.
	 * @return String array containing all authors in the database.
	 */
	public String[] getAllAuthors(){
		//https://www.postgresql.org/docs/7.4/jdbc-query.html
		ArrayList<String> authors = new ArrayList<String>(); //The names of all authors in database returned by our query
		Statement stmt = null; //sql statement
		ResultSet rs = null; //results
		Connection conn = null;
		
		try {
			conn = connectToDB();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT fname, lname FROM authors");
			while (rs.next()) {
				authors.add(rs.getString(1) + " " + rs.getString(2));//Concatenate authors first + last name and add them to the list to return
			}
			return (String[]) authors.toArray();
			
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

	
	/*The more useful queries available (searches)*/
	public String[] getByAuthor(String author) {
		return null;
		//TODO implement me
		
	}
	
	public String[] getByTitle(String title) {
		return null;
		//TODO implement me
		
	}
	
	public String[] getBySeries(String series) {
		return null;
		//TODO implement me
	}
	
	public String[] getByKeyword(String keyword) {
		return null;
		//TODO implement me
	}
	
	public String[] getByGenres(String genres[]) {
		return null;
//		for each genre in genres
		//assert(genres in allGenres)
		
		//TODO implement me
	}
	//public String[] getByCustomGenre
	

	public String[] getBooksAdvancedQuery(String fields[], String searchValues[]){
		assert(fields.length == searchValues.length);//TODO
		//assert(fields are acceptable fields (i.e. areas they can query in DB)
		
		return searchValues;
		//TODO implement me
		
	}
	
	/*Setters that allow adding to the database only allowed by admin users*/
	
	
	public boolean addBook(String Title, String series, float number_in_series, int edition, String authors[], Date publicationDate, String publisher, String genres[]) {
	    //take series string and identify the series_id associated with the series.
		//take strings of author ids and convert into author id's for DB store
		//TODO implement me
		return false;
	}
	
	public boolean removeBook(int book_id) {
		//TODO implement me
		return false;
		
	}

	//edit book (change metadata)
	
	
	//add series (helper function for addBook)
	private boolean addSeries(String series_name) {
		//TODO implement me
		return false;
	}
	
	//remove series. (helper series for removeBook)
	private boolean removeSeries(String series_name) {
		//TODO implement me
		//prompt for confirm delete
		//for each book in db cascade delete
		//remove series from series.list
		//remove from authors
		return false;
	}
	
	
	//list genres
	//add genre
	//remove genre
	//relabel-genre
	
	//addUser
	//removeUser
	//elevateUser?
	
	
	
	/*helper functions*/
	
	private int getSeriesID(String seriesName) {
		return 0;
		//TODO implement me
	}
	private int getBookID(String bookName) {
		return 0;
		//TODO implement me
	}

	

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

	



}


