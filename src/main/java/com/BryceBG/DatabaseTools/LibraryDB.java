package com.BryceBG.DatabaseTools;
import java.sql.*;
import java.util.ArrayList;

/**
 * 
 * @author Bryce Bodley-Gomes
 * This class is for interfacing with the postgres library database and allowing programmatic access.
 */
public class LibraryDB {
	/**
	 * TODO: 
	 * 1. Change it so resources are released when the instance is terminated.
	 * either try-with-resources or finally blocks
	 * https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
	 * 2. decide if we want to connect each time a call is made (getting rid of the constructors and connect_DB functions) or keep connection like we have it.
	 */
	/*https://www.postgresql.org/docs/7.4/jdbc-use.html*/
	//DEFAULT parameters 
	String DB_HOST = "localHost";
	String DB_PORT = "5432";
	String DB_NAME = "librarydatabase";
	String DB_PASSWORD = ""; //passed in presumably
	String DB_USER = "postgres"; 
	

	/**
	 * Function to create Credentials object for queries to database.
	 * @param username: Username as it should be in the DB.User table 
	 * @param password: Password corresponding to the Username passed in.
	 * @return: if user exists return valid credentials.
	 */
	public Credentials login(String username, String password) {
		
		
		Connection conn = null;
		PreparedStatement stmt = null; 
		Credentials user_credentials= null; 
		try {
			//1. connect to DB
			conn = connectToDB(DB_HOST, DB_PORT, DB_USER, DB_PASSWORD, DB_NAME);
			
			if( conn.isValid(0)) {
				//2. verify user exists in database (through query of DB.Users table)
		            String selectStatementStr =
		                    "SELECT * " +
		                    "FROM USERS" + 
		                    "WHERE users.username == ?";
		             stmt = conn.prepareStatement(selectStatementStr);
		             stmt.setString(1, username);
		            
		           boolean sOf = stmt.execute();
		            if (sOf == false) {
		            	//no results were returned (so user doesn't exist)
		            	return new Credentials();
		            }
		            else
		            {
		            	//3. extract results from result set needed to create credentials object
		            	ResultSet rs = stmt.getResultSet();
		            	String un = rs.getString("USERNAME"); 
		            	String pw = rs.getNString("PASSWORD");
		            	boolean admin = rs.getBoolean("IS_ADMIN");
		            	
						//4. create credentials object for the user
		            	return new Credentials(un, pw, admin);

		            } //end of else execute				

			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
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
	   }//end try

	}
	
	
	/**
	 * Connect to a postgresql database (librarydatabase) with default parameters
	 * @throws ClassNotFoundException: if no postgres driver was able to be used
	 * @throws SQLException: if the connection failed due to invalid parameters
	 */
	private Connection connectToDB() throws ClassNotFoundException, SQLException{
		Connection conn = null;
		Class.forName("org.postgresql.Driver"); //register the driver
		String url = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);
		
		//establish connection to db at the provided url
		try {
			conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
			return conn; 
		} catch (SQLException e) {
			System.out.println("Error in connecting to the database with the supplied parameters" + url); 
			throw e;
		}
	}
	
	/**
	 * Connect to a postgresql database with custom parameters (stored in the globals after they are recieved.
	 * @param host: address of the server for the database
	 * @param port: port to use when connecting
	 * @param username: username to login with
	 * @param password: password to login with
	 * @param dbname: what is the database name to connect to
	 * @throws ClassNotFoundException: if no postgres driver was able to be used
	 * @throws SQLException: if the connection failed due to invalid parameters
	 */
	private Connection connectToDB(String host, String port, String username, String password, String dbname) throws ClassNotFoundException, SQLException{
		DB_HOST = host;
		DB_PORT = port;
		DB_NAME = username;
		DB_PASSWORD = password;
		DB_USER = dbname;

		Connection conn;
		Class.forName("org.postgresql.Driver"); //load the driver
		String URL = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);
		
		//establish connection to db at the provided url
		//get username and password for database
		try {
			conn = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
			return conn; 
		} catch (SQLException e) {
			System.out.println("Error in connecting to the database with the supplied parameters" + URL); //TODO remove URL for release version.
			throw e;
		}
	}
	
	
	/*getter functions for DB checks*/
	
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
//		assert(genres in allGenres)
		//TODO implement me
	}
	

	public String[] getBooksAdvancedQuery(String fields[], String searchValues[]){
		assert(fields.length == searchValues.length);//TODO
		//assert(fields are acceptable fields (i.e. areas they can query in DB)
		
		return searchValues;
		//TODO implement me
		
	}
	
	/*Setters that allow adding to the database only allowed by admin users*/
	
	//add book
	
	//INT edition
	public boolean addBook(String Title, int rating, String series, float number_in_series, String authors[], Date publicationDate,  String genres[]) {
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

	
	
	
	

}
