package com.BryceBG.DatabaseTools.utils;

/**
 * This class just holds some of our fields that we repeatedly use throughout our project.
 * @author Bryce-BG
 *
 */
public class GlobalConstants {
	
	//pattern used to initialize our log4j system when running tests
	public static final String TEST_LOGGER_PATTERN = "%d %p %c [%t] function: %M| %m%n";
	//basic filename used to output logger results when runing tests (though overflows will result in a date being appended to the file name to uniquely identify.
	public static final String TEST_LOGGER_OUT_FILE_NAME = "test_log.txt";
	
	public static final String APP_NAME = "Ebook Library System";
}
