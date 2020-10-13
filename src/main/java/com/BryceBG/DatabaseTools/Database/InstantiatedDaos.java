package com.BryceBG.DatabaseTools.Database;

import com.BryceBG.DatabaseTools.Database.Book.BookDao;
import com.BryceBG.DatabaseTools.Database.User.UserDao;
import com.BryceBG.DatabaseTools.utils.Utils;
/**
 * This class just adds a bunch of static variables that various parts of the app require. 
 * @author Bryce-BG
 *
 */
public class InstantiatedDaos {
	public static BookDao bookDao; 
    public static UserDao userDao;
    public static LibraryDB library;
    
	static {
	      library = new LibraryDB(Utils.getConfigString("app.dbhost", null), Utils.getConfigString("app.dbport", null), Utils.getConfigString("app.dbname", null),Utils.getConfigString("app.dbpass", null) , Utils.getConfigString("app.dbuser", null));
	      bookDao = new BookDao();
	      userDao = new UserDao();

	}
	
	
    
    
    
}
