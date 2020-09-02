import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadConfig {
	/**
	 * Helper class for reading in the configuration data related to the application.
	 * @param args
	 */
	private String dbHost;
	private String dbPort;
	private String dbName;
	private String dbPass;
	private String dbUser;
	
public ReadConfig() {
	load_config();
}
	
	
/**
 * Function reads in the app.config file
 * @return true if successful
 */
private boolean load_config() {
    Class myClass;
	Properties prop = new Properties();
	String fileName = "app.config";
	InputStream is = null;

	try {
		myClass = Class.forName("ReadConfig"); //TODO find somee other way to get the name?
		is = myClass.getResourceAsStream(fileName); 
		try {
		    prop.load(is);
		    
		    //assign to class variables
			dbHost = prop.getProperty("app.dbhost");
			dbPort = prop.getProperty("app.dbport");
			dbName = prop.getProperty("app.dbname");
			dbPass = prop.getProperty("app.dbpass");
			dbUser = prop.getProperty("app.dbuser");
			
		
			return true;
		} catch (IOException ex) {
		    System.out.println("Error occured while reading config file.");
		}
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	}
	return false; 
}

/* getter functions to get instance variables loaded form the config file */
public String get_host() {
	return dbHost;
}
public String get_port() {
	return dbPort;
}
public String get_name() {
	return dbName;
}
public String get_pass() {
	return dbPass;
}
public String get_user() {
	return dbUser;
}

/** 
 * Helper function that identifies if all the config parameters were successfully loaded
 * @return
 */
public boolean is_valid() {
	return (dbHost != null && dbPort != null && dbName != null && dbPass != null && dbUser != null);
}
//public static void main(String[] args) {
//	load_config();
//
//			
//}
}
