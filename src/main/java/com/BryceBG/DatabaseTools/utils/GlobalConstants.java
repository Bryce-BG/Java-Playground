package com.BryceBG.DatabaseTools.utils;

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

	/* Test related constants */
	// pattern used to initialize our log4j system when running tests
	public static final String TEST_LOGGER_PATTERN = "%d %p %c [%t] function: %M| %m%n";

	// basic filename used to output logger results when running tests (though
	// overflows will result in a date being appended to the file name to uniquely
	// identify.
	public static final String TEST_LOGGER_OUT_FILE_NAME = "test_log.txt";

	// this is the database we use that is an alternative to our main library
	// database.
	public static final String TEST_DBNAME = "librarytest";
	
	public static final String DB_INSTALL_SCRIPT_PATH = "install.sql"; 

}
