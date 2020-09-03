package com.BryceBG.DatabaseTools;
import java.sql.*;
import java.util.ArrayList;

/**
 * 
 * @author Bryce Bodley-Gomes
 * This class is for interfacing with the postgres library database and allowing programmatic access.
 */
public class LibraryDB {
	
	//DEFAULT parameters for DB access (can be overridden via constructor)
	String DB_HOST = "localHost";
	String DB_PORT = "5432";
	String DB_NAME = "librarydatabase";
	String DB_PASSWORD = "postgres"; 
	String DB_USER = "postgres"; 
	

	
	//constructor (optional override for default database parameters)
	public LibraryDB(String dbHost, String dbPort, String dbName, String dbPassword, String dbUsername) {
		DB_HOST = dbHost;
		DB_PORT = dbPort;
		DB_NAME = dbName;
		DB_PASSWORD = dbPassword;
		DB_USER = dbUsername; 
	}

	/** ############# USER RELATED DATABASE FUNCTIONS ############# **/
	
	 /** Function to create Credentials object which is used to authenticate user access to the database.
	 * @param username: Username as it should be in the DB.User table 
	 * @param password: Password corresponding to the Username passed in.
	 * @return: if user exists return valid credentials.
	 */
	public Credentials login(String username, String password) {
		Connection conn = null;
		PreparedStatement stmt = null; 
		try {
			//1. connect to DB
			conn = connectToDB();
			
			if( conn.isValid(0)) {
				//2. verify user exists in database (through query of DB.Users table)
		            String sql =
		                    "SELECT * " +
		                    "FROM USERS " + 
		                    "WHERE username=? AND password=?";		            
		            
		            stmt = conn.prepareStatement(sql);
		            stmt.setString(1, username);
		            stmt.setString(2, password);
		            ResultSet rs = stmt.executeQuery();
		            
		            
		            
		            if (rs.next()) { //make sure there WAS an entry	returned (otherwise no match was found	            
		            ResultSetMetaData rsmd = rs.getMetaData();
		            
		           	//3. extract results from result set needed to create credentials object
	            	String un = rs.getString("USERNAME"); 
	            	String pw = rs.getString("PASSWORD");
	            	boolean admin = rs.getBoolean("IS_ADMIN");
	            	
					//4. create credentials object for the user
	            	return new Credentials(un, pw, admin);
		            

		            }
		            else {
		            	return new Credentials(); //user was invalid 
		            }
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
	   }//end finally
		return new Credentials();

	}
	
	/**
	 * Function to allow the creation of additional users (non administive ones) for standard users in our library system.
	 * @param username: username user wants for their account.
	 * @param Password: password the user wants to initially set for their account.
	 * @param lName: Last name of user.
	 * @param fName: First name of user.
	 * @param email: email address linked to the user account.
	 * @return:
	 * 0 = successful creation of user.
	 * "-" values indicate failure:
	 * -1 = failure to connect to the library database.
	 * -2 = username is taken
	 * -3 = invalid password (try again, with the required specifications)
	 * -4 = first name is invalid
	 * -5 = last name is invalid
	 * -6 = invalid email
	 * -7 Unexpected error adding user to database
	 */
	public int create_new_user(String username, String password, String lName, String fName, String email) {
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
		                    "WHERE username=?"; 		              
		            stmt = conn.prepareStatement(sql);
		            stmt.setString(1, username);
		            ResultSet rs = stmt.executeQuery();
    
		            if (rs.next()) { 
		            	return -2; //username is taken
		            }
		            else {
	         			//2. check password meets requirements.
		            	if(check_password_meets_requirements(password))
		            	{
			    			//3. check fName and lName are valid (a-z only)
		            		fName = fName.strip();
		            		lName = lName.strip();
		            	    String fNameExpression = "^[a-zA-Z]+"; 
		            	    if(fName.matches(fNameExpression)) {
		            	    	String lNameExpression = "^[a-zA-Z \\-\\.\\']*$";//regular expression allowing: alpha chars, hyphens, periods, apostrophes and spaces
		            	    	if(lName.matches(lNameExpression))
		            	    	{
					    			//4. a. check email is "valid"
		            	    		if(is_valid_email(email))
		            	    		{
		            	    			//4.b check if email is already associated with account
		            	    			if(!stmt.isClosed()) {//close query based on username
		            	    				stmt.close();
		            	    			}
		            	    			sql =
		            		                    "SELECT * " +
		            		                    "FROM USERS " + 
		            		                    "WHERE email=?"; 
		            		            stmt = conn.prepareStatement(sql);
		            		            stmt.setString(1, email);
		            		            rs = stmt.executeQuery();
		            		            
		            		            if (rs.next()) { 
		            		            	return -6; //email is already used with some account.
		            		            }
		            		            else {
							    			//5. add user to database.
		            		            	if(!stmt.isClosed()) {//close query based on username
			            	    				stmt.close();
			            	    			}
			            	    			sql =
			            	    					"INSERT INTO USERS(username, password, first_name, last_name, is_admin, email) "
			            	    					+ "VALUES(?,?,?,?,?, ?)";
			            	    						            	    	          
			            		            stmt = conn.prepareStatement(sql);
			            		            stmt.setString(1, username);
			            		            stmt.setString(2, password);
			            		            stmt.setString(3, fName);
			            		            stmt.setString(4, lName);
			            		            stmt.setBoolean(5, false);
			            		            stmt.setString(6, email);

			            		             int rowsUpdated = stmt.executeUpdate();
			            		             if(rowsUpdated !=1)
			            		            	 return -7; //error occurred inserting new user into DB
			            		             else
			 		            	    		return 0; //successfully created new user
		            		            }      
		            	    		}
		            	    		else {
		            	    			return -6; //invalid email (via regex)
		            	    		}
		            	    	}
		            	    	else {
		            	    		return -5;
		            	    	}	
		            	    }
		            	    else {
		            	    	return -4;
		            	    	}
		            	}
		            	else {
		            		return -3; }//password was not of sufficent strength
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

	/**
	 * Function to allow the creation of additional admin users admin users in our library system.
	 * @param Credentials: the credentials of the admin required to create a new admin
	 * @param username: username user wants for their account.
	 * @param Password: password the user wants to initially set for their account.
	 * @param lName: Last name of user.
	 * @param fName: First name of user.
	 * @param email: email address linked to the user account.
	 * @return:
	 * 0 = successful creation of user.
	 * "-" values indicate failure:
	 * -1 = failure to connect to the library database.
	 * -2 = username is taken
	 * -3 = invalid password (try again, with the required specifications)
	 * -4 = first name is invalid
	 * -5 = last name is invalid
	 * -6 = invalid email
	 * -7 Unexpected error adding user to database
	 * -8 invalid credentials
	 */
	public int create_new_admin_user(Credentials yourAdmin, String username, String password, String lName, String fName, String email) {
		
		if(yourAdmin.is_valid_credentials() && validate_credentials(yourAdmin) && yourAdmin.get_permissions()) {
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
			                    "WHERE username=?"; 		            
			            
			            stmt = conn.prepareStatement(sql);
			            stmt.setString(1, username);
			            ResultSet rs = stmt.executeQuery();
	    
			            if (rs.next()) { 
			            	return -2; //username is taken
			            }
			            else {
		         			//2. check password meets requirements.
			            	if(check_password_meets_requirements(password))
			            	{
				    			//3. check fName and lName are valid (a-z only)
			            		fName = fName.strip();
			            		lName = lName.strip();
			            	    String fNameExpression = "^[a-zA-Z]+"; 
			            	    if(fName.matches(fNameExpression)) {
			            	    	String lNameExpression = "^[a-zA-Z \\-\\.\\']*$";//regular expression allowing: alpha chars, hyphens, periods, apostrophes and spaces
			            	    	if(lName.matches(lNameExpression))
			            	    	{
						    			//4. a. check email is "valid"
			            	    		if(is_valid_email(email))
			            	    		{
			            	    			//4.b check if email is already associated with account
			            	    			if(!stmt.isClosed()) {//close query based on username
			            	    				stmt.close();
			            	    			}
			            	    			sql =
			            		                    "SELECT * " +
			            		                    "FROM USERS " + 
			            		                    "WHERE email=?"; 
			            		            stmt = conn.prepareStatement(sql);
			            		            stmt.setString(1, email);
			            		            rs = stmt.executeQuery();
			            		            
			            		            if (rs.next()) { 
			            		            	return -6; //email is already used with some account.
			            		            }
			            		            else {
								    			//5. add user to database.
			            		            	if(!stmt.isClosed()) {//close query based on username
				            	    				stmt.close();
				            	    			}
				            	    			sql =
				            	    					"INSERT INTO USERS(username, password, first_name, last_name, is_admin, email) "
				            	    					+ "VALUES(?,?,?,?,?, ?)";
				            	    						            	    	          
				            		            stmt = conn.prepareStatement(sql);
				            		            stmt.setString(1, username);
				            		            stmt.setString(2, password);
				            		            stmt.setString(3, fName);
				            		            stmt.setString(4, lName);
				            		            stmt.setBoolean(5, true);
				            		            stmt.setString(6, email);

				            		             int rowsUpdated = stmt.executeUpdate();
				            		             if(rowsUpdated !=1)
				            		            	 return -7; //error occured inserting new user into DB
				            		             else
				 		            	    		return 0; //successfully created new user
			            		            }
			            	    		}
			            	    		else {
			            	    			return -6; //invalid email (via regex)
			            	    		}
			            	    	}
			            	    	else {
			            	    		return -5;
			            	    	}	
			            	    }
			            	    else {
			            	    	return -4;
			            	    	}
			            	}
			            	else {
			            		return -3; 
			            		}//password was not of sufficent strength
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
		}//end if"credentials"
		else {
			return -8;
		}
		
	}

	public int delete_user(Credentials yourAdmin) {
		//TODO implement me (more parameters needed)
		return -1;
	}
	public boolean edit_user(Credentials yourAdmin) {
		//TODO implement me (more parameters needed)
		//can only edit user if: is_admin=true OR your acount == the account being edited.
		return false;
	}
	
	
	
	
	
	
	
	
	

	
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

	

	/**
	 * Connect to a postgresql database (librarydatabase) with default parameters
	 * @throws ClassNotFoundException: if no postgres driver was able to be used
	 * @throws SQLException: if the connection failed due to invalid parameters
	 */
	private Connection connectToDB() throws SQLException, ClassNotFoundException{
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
	 * Helper function that is used to ensure password meets specifications.
	 * length>=8, 
	 * contains a capital letter
	 * Contains alpha and numeric characters
	 * 
	 * @param password: password to check
	 * @return: true if password meets all requirements. false otherwise
	 */
	private boolean check_password_meets_requirements(String password) {
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
	/**
	 * Helper function that ensures (currently) that an email is valid using regex. 
	 * TODO: revise so that it sends an email that requires confirmation before continuing.
	 * @param email: email to check
	 * @return
	 */
	private boolean is_valid_email(String email) {
		   String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		   return email.matches(regex);
		}
	
	/**
	 * Helper function to validate that credentials passed into a function actually exist in the database and are not completly fabricated
	 * @param user: user to check if it exists in the database.
	 * @return: True if the credentials check out.
	 */
	private boolean validate_credentials(Credentials user) {
		Connection conn = null;
		PreparedStatement stmt = null; 
		try {
			//1. connect to DB
			conn = connectToDB();
			
			if( conn.isValid(0)) {
				//2. verify user exists in database (through query of DB.Users table)
		            String sql =
		                    "SELECT * " +
		                    "FROM USERS " + 
		                    "WHERE username=? AND password=? AND is_admin=?";		            
		            
		            stmt = conn.prepareStatement(sql);
		            stmt.setString(1, user.get_username());
		            stmt.setString(2, user.get_password());
		            stmt.setBoolean(3, true);
		            
		            ResultSet rs = stmt.executeQuery();
		            if (rs.next()) { //make sure there WAS an entry	returned (otherwise no match was found	            
		            	return true;
		            }
		            else {
		            	return false;  
		            }
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
	   }//end finally
		return false;
		
	}

}
