package com.BryceBG.DatabaseTools;
import java.sql.*;


public class DatabaseTools {
	
	/*https://www.postgresql.org/docs/7.4/jdbc-use.html*/
	String databaseHost = "localHost";
	String databasePort = "5432";
	String databaseName = "librarydatabase";
	String databasePass = ""; //passed in presumably
	String databaseUsername = "postgres"; 
	Connection db;
	
	/**
	 * Connect to a postgresql database (librarydatabase) with default parameters
	 * @throws ClassNotFoundException: if no postgres driver was able to be used
	 * @throws SQLException: if the connection failed due to invalid parameters
	 */
	public void connectToDB() throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver"); //load the driver
		String url = String.format("jdbc:postgresql://%s:%s/%s", databaseHost, databasePort, databaseName);
		
		//establish connection to db at the provided url
		//get username and password for database
		try {
			db = DriverManager.getConnection(url, databaseUsername, databasePass);
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Connect to a postgresql database with custom parameters
	 * @param host: address of the server for the database
	 * @param port: port to use when connecting
	 * @param username: username to login with
	 * @param password: password to login with
	 * @param dbname: what is the database name to connect to
	 * @throws ClassNotFoundException: if no postgres driver was able to be used
	 * @throws SQLException: if the connection failed due to invalid parameters
	 */
	public void connectToDB(String host, String port, String username, String password, String dbname) throws ClassNotFoundException, SQLException{
		databaseHost = host;
		databasePort = port;
		databaseName = username;
		databasePass = password;
		databaseUsername = dbname;

		
		Class.forName("org.postgresql.Driver"); //load the driver
		String url = String.format("jdbc:postgresql://%s:%s/%s", databaseHost, databasePort, databaseName);
		
		//establish connection to db at the provided url
		//get username and password for database
		try {
			db = DriverManager.getConnection(url, databaseUsername, databasePass);
		} catch (SQLException e) {
			throw e;
		}
	}
	
	
	/*getter functions for DB checks*/
	public String[] getAllSeries(){
		return null;
		//TODO implement me
		
	}
	public String[] getAllTitles(){
		return null;
		//TODO implement me
		
	}
	public String[] getAllAuthors(){
		return null;
		//TODO implement me
		
	}

	
	/*The more useful queries available */
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
	
	/*setters that allow adding to the database only allowed by admin users*/
	
	//add book
	
	//INT edition
	public boolean addBook(String Title, int rating, String series, float number_in_series, String authors[], Date publicationDate,  String genres[]) {
		return false;
	}
	//remove book
	//edit book (change metadata)
	
	//add series
	//remove series
	
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
