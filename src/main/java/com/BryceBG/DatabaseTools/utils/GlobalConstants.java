package com.BryceBG.DatabaseTools.utils;

import org.apache.logging.log4j.Level;

/**
 * This class just holds some of our fields that we repeatedly use throughout
 * our project.
 * 
 * @author Bryce-BG
 *
 */
public class GlobalConstants {
	// name of our system
	public static final String APP_NAME = "Ebook Library System";
	public static final String APP_LOGGER_PATTERN = "%d %p [%t] %c | %m%n";

	/* Test related constants */
	// pattern used to initialize our log4j system when running tests
	//https://stackoverflow.com/questions/28604171/how-to-print-logs-in-color-using-log4j2-highlight-pattern
	//https://logging.apache.org/log4j/2.x/manual/layouts.html
	public static final String TEST_LOGGER_PATTERN ="%d %p [%t] %c{1}.%M()| %m%n";

	//the level at which we log during test executions
	public static final Level TEST_LOGGER_LEVEL = Level.DEBUG;

	// basic filename used to output logger results when running tests (though
	// overflows will result in a date being appended to the file name to uniquely
	// identify.
	public static final String TEST_LOGGER_OUT_FILE_NAME = "test_log.txt";

	// this is the database we use that is an alternative to our main library
	// database.
	public static final String TEST_DBNAME = "librarytest";
	
	public static final String DB_INSTALL_SCRIPT_PATH = "install.sql"; 

	
	/**error messages used in our functions to indicate reason for failure**/
	public static final String MSG_SUCCESS = "Success!";
	public static final String MSG_INVALID_USER = "Invalid Username/Password combo";
	public static final String MSG_INVALID_USER_PERMISSIONS = "User lacks permissions to perform selected operation";
	
	
}
