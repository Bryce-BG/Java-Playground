package testUtils;

import static com.BryceBG.DatabaseTools.utils.GlobalConstants.TEST_LOGGER_OUT_FILE_NAME;
import static com.BryceBG.DatabaseTools.utils.GlobalConstants.TEST_LOGGER_PATTERN;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.LibraryDB;
import com.BryceBG.DatabaseTools.utils.GlobalConstants;
import com.BryceBG.DatabaseTools.utils.Utils;


public class UtilsForTests {
	private static final Logger logger = LogManager.getLogger(UtilsForTests.class.getName());

	static {
		// Initialize our logger
		com.BryceBG.DatabaseTools.utils.Utils.initializeAppLogger(TEST_LOGGER_OUT_FILE_NAME, TEST_LOGGER_PATTERN);

		// 1. call our function once to create our test database
		createTestDB();
		
	}

	public static void main(String[] args) {
		
		LibraryDB.createDB(Utils.getConfigString("app.dbname", null));



	}

	public static void createTestDB() {
		// 1. create our empty testing database
		if (!LibraryDB.createDB(GlobalConstants.TEST_DBNAME)) 
			logger.error("Trying to create our test database: {} failed", GlobalConstants.TEST_DBNAME);
		else {
			//set our system to use the new database.
			DAORoot.changeDB(Utils.getConfigString("app.dbhost", null), Utils.getConfigString("app.dbport", null),
					GlobalConstants.TEST_DBNAME, Utils.getConfigString("app.dbpass", null),
					Utils.getConfigString("app.dbuser", null));
			}

	}

	/**
	 * This method resets our TEST database to restore the table entries to the very
	 * limited initial state with just a few test entries TODO rewrite this all as
	 * it is a very brittle/vulnerable way to modify our database. potential
	 * alternatives:
	 * https://stackoverflow.com/questions/2071682/how-to-execute-sql-script-file-in-java
	 * 
	 * 
	 */
	public static void resetDB() {
		// 1. ensure we are using our test database and NOT the main database.
		DAORoot.changeDB(Utils.getConfigString("app.dbhost", null), Utils.getConfigString("app.dbport", null), GlobalConstants.TEST_DBNAME,
				Utils.getConfigString("app.dbpass", null), Utils.getConfigString("app.dbuser", null));

		File resetFile = new File("resetDBEntries.sql");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(resetFile));
			String line = reader.readLine();
			while (line != null) {
				if (line.strip().startsWith("--") || line.isBlank()) {
				} else {
					DAORoot.library.runRawSQL(line);
				}

				line = reader.readLine();// read next line
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void executeSqlScript(Connection conn, File inputFile) {

	    // Delimiter
	    String delimiter = ";";

	    // Create scanner
	    Scanner scanner;
	    try {
	        scanner = new Scanner(inputFile).useDelimiter(delimiter);
	    } catch (FileNotFoundException e1) {
	        e1.printStackTrace();
	        return;
	    }

	    // Loop through the SQL file statements 
	    Statement currentStatement = null;
	    while(scanner.hasNext()) {

	        // Get statement 
	        String rawStatement = scanner.next() + delimiter;
	        try {
	            // Execute statement
	            currentStatement = conn.createStatement();
	            currentStatement.execute(rawStatement);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        } finally {
	            // Release resources
	            if (currentStatement != null) {
	                try {
	                    currentStatement.close();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                }
	            }
	            currentStatement = null;
	        }
	    }
	scanner.close();
	}
}
