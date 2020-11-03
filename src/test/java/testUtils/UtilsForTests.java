package testUtils;

import static com.BryceBG.DatabaseTools.utils.GlobalConstants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.LibraryDB;
import com.BryceBG.DatabaseTools.utils.Utils;

public class UtilsForTests {
	private static final Logger logger = LogManager.getLogger(UtilsForTests.class.getName());

	static {
		// Initialize our logger
		com.BryceBG.DatabaseTools.utils.Utils.initializeAppLogger(TEST_LOGGER_OUT_FILE_NAME, TEST_LOGGER_PATTERN);

		// 1. call our function once to create our test database
		createTestDB();

	}

	/**
	 * This function is used to create the database of name:
	 * GlobalConstants.TESTDB_NAME This database is a mockup of our real library
	 * database with the same schema defined in resources/install.sql
	 */
	public static void createTestDB() {
		// 1. create our empty testing database
		if (!LibraryDB.createDB(TEST_DBNAME, false))
			logger.error("Trying to create our test database: {} failed", TEST_DBNAME);
		else {
			// set our system to use the new database.
			DAORoot.changeDB(Utils.getConfigString("app.dbhost", null), Utils.getConfigString("app.dbport", null),
					TEST_DBNAME, Utils.getConfigString("app.dbpass", null), Utils.getConfigString("app.dbuser", null));
		}

	}

	/**
	 * This method resets our TEST database to restore the table entries to the very
	 * limited initial state with just a few test entries. It should be only run on
	 * the test database as it deletes all entries in the database.
	 * 
	 */
	public static void resetDB(boolean showExecution) {
		try (Connection conn = DAORoot.library.connectToDB();) {

			// Initialize the script runner
			ScriptRunner sr = new ScriptRunner(conn);
			if(!showExecution)
				sr.setLogWriter(null); //hide output from the scriptrunner
			// Creating a reader object

			File resetFile = new File("resetDBEntries.sql");
			BufferedReader reader;

			reader = new BufferedReader(new FileReader(resetFile));

//			Reader r = Resources.getResourceAsReader(GlobalConstants.DB_INSTALL_SCRIPT_PATH);

			// 3.c.Running the script to add tables and rules
			sr.runScript(reader);

			// 4. commit results
			conn.commit();
			reader.close();

		} catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: {}", e.getMessage());
		} catch (SQLException e) {
			logger.error("Error occured during execution of our sql during DB creation: {}", e.getMessage());
		} catch (IOException e) {
			logger.error("Exception occured during trying to read install script {}. The exception was: {}",
					DB_INSTALL_SCRIPT_PATH, e.getMessage());
		}

	}

}
