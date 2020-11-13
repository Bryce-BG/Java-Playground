package com.BryceBG.DatabaseTools.Database;

import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.utils.GlobalConstants;
import com.BryceBG.DatabaseTools.utils.Utils;

/**
 * 
 * @author Bryce-BG This class is for interfacing with the postgres library
 *         database and allowing programmatic access.
 */
public class LibraryDB {
	private static final Logger logger = LogManager.getLogger(LibraryDB.class.getName());

	// DEFAULT parameters for DB access (can be overridden via constructor)
	private String DB_HOST = "localHost";
	private String DB_PORT = "5432";
	private String DB_NAME = "librarydatabase";
	private String DB_PASSWORD = "postgres";
	private String DB_USER = "postgres";

	// constructor (optional override for default database parameters)
	public LibraryDB(String dbHost, String dbPort, String dbName, String dbPassword, String dbUsername) {
		DB_HOST = dbHost;
		DB_PORT = dbPort;
		DB_NAME = dbName;
		DB_PASSWORD = dbPassword;
		DB_USER = dbUsername;
	}

	/** #############getter functions for DB checks############# **/

	/** ########################################################## */

	/**
	 * Connect to a postgresql database (librarydatabase) with default parameters
	 * 
	 * @throws ClassNotFoundException: if no postgres driver was able to be used
	 * @throws SQLException:           if the connection failed due to invalid
	 *                                 parameters
	 */
	public Connection connectToDB() throws SQLException, ClassNotFoundException {
		Connection conn = null;
		Class.forName("org.postgresql.Driver"); // register the driver
		String url = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

		// establish connection to db at the provided url
		try {
			conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
			return conn;
		} catch (SQLException e) {
			logger.error("Error in connecting to the database with the supplied parameters" + url);
			throw e;
		}
	}

	/**
	 * This function is intended to be run once to initialize the database for
	 * future use. WARNING This WILL drop existing database if a database with same
	 * name exists in system. It also will execute our install.sql script on that
	 * database and switch the DAORoot over to use the new database
	 * 
	 * @param libraryName the name for the postgresql database to create
	 * @param showResults This tells our system if it should be verbose and have the
	 *                    function print out the script as it is executing or not
	 * @return returns true if database was successfully created (though the
	 *         install.sql script may not have run correctly). False if the database
	 *         could not be created/recreated
	 */
	public static boolean createDB(String libraryName, boolean showResults) {
		if (libraryName == null || libraryName.isEmpty() || libraryName.isBlank()) {
			libraryName = "librarydatabase";

			logger.warn(String.format(
					"The name passed into CreateDB for the database was invalid so it was changed to: %s.",
					libraryName));
		}
		try {
			String sql = String.format("DROP DATABASE %s;", libraryName);

			Connection connection = connectToPostGres();
			Statement stmt = connection.createStatement();

			// 1. check if database with same name already exists and if it does drop it
			List<String> dbNames = listAllDatabases(); // get names of current databases
			for (String x : dbNames) {
				if (libraryName.equalsIgnoreCase(x)) {
					logger.debug("Existing database was found with name {}.", libraryName);
					// Drop database so we can recreate it
					stmt.executeUpdate(sql);
					break;
				}
			}
			// 2. create our new database.
			sql = String.format("CREATE DATABASE %s;", libraryName); // Create Database
			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			logger.info("Successfully created database: {}", libraryName);

			// 3. now apply our install.sql script to initialize the tables.
			// 3.a. change our system to use the new database we just created
			boolean rtnedVal = DAORoot.changeDB(Utils.getConfigString("app.dbhost", null),
					Utils.getConfigString("app.dbport", null), libraryName, Utils.getConfigString("app.dbpass", null),
					Utils.getConfigString("app.dbuser", null));
			if (!rtnedVal) { // can't connect with new parameters
				logger.error("Unexpected error in changing the database occured in createDB function");
				return false;
			}
			// 3.b. establish connection with new database
			try (Connection conn = DAORoot.library.connectToDB();) {

				// Initialize the script runner
				ScriptRunner sr = new ScriptRunner(conn);
				if (!showResults) // kill the log writing if they didn't want it
					sr.setLogWriter(null);
				// Creating a reader object
				Reader r = Resources.getResourceAsReader(GlobalConstants.DB_INSTALL_SCRIPT_PATH);
				// Reader reader = new
				// InputStreamReader(UtilsForTests.class.getClassLoader().getResourceAsStream(GlobalConstants.DB_INSTALL_SCRIPT_PATH));

				// 3.c.Running the script to add tables and rules
				sr.runScript(r);

				// 4. commit results
				conn.commit();
				r.close();

			} catch (ClassNotFoundException e) {
				logger.error("Exception occured during connectToDB: {}", e.getMessage());
			} catch (SQLException e) {
				logger.error("Error occured during execution of our sql during DB creation: {}", e.getMessage());
			} catch (IOException e) {
				logger.error("Exception occured during trying to read install script {}. The exception was: {}",
						GlobalConstants.DB_INSTALL_SCRIPT_PATH, e.getMessage());
			}

			// TODO determine if script actually worked correctly. if an actual exception
			// Occurred it will return false but if individual statements fail this can
			// still return true (incorrectly)
			return true;
		} catch (Exception ex) {
			logger.error("An error occured during createDB: " + ex.getMessage());
		}
		return false;
	}

	/**
	 * Helper function that connects to postgres and not our library db (so we can
	 * delete it with createDB())
	 * 
	 * @return connection to default postgres database.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private static Connection connectToPostGres() throws SQLException, ClassNotFoundException {
		Connection conn = null;
		Class.forName("org.postgresql.Driver"); // register the driver
		String host = Utils.getConfigString("app.dbhost", null);
		String port = Utils.getConfigString("app.dbport", null);
		String url = String.format("jdbc:postgresql://%s:%s/?", host, port);

		// establish connection to db at the provided url
		try {
			conn = DriverManager.getConnection(url, Utils.getConfigString("app.dbuser", null),
					Utils.getConfigString("app.dbpass", null));
			return conn;
		} catch (SQLException e) {
			logger.fatal("failed to connect to database" + e.getMessage());
			throw e;
		}
	}

	/**
	 * Helper function for createDB(). Queries postgresql to get database names in
	 * system.
	 * 
	 * @return returns a list of names for all currently existing databases
	 */
	private static List<String> listAllDatabases() {
		List<String> names = new ArrayList<String>();
		try (Connection connection = connectToPostGres();) {
			String sql = "SELECT datname FROM pg_database WHERE datistemplate = false;";
			PreparedStatement ps = connection.prepareStatement(sql);
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					String name = rs.getString(1);
					names.add(name);
				}
			} // end try-with-resource: ResultSet
		} catch (Exception e) {
			logger.error("An error occured trying to list all databases\n" + e.getMessage());
		}
		return names;
	}

}
