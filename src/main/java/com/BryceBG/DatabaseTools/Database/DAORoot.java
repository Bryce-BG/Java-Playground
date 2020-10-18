package com.BryceBG.DatabaseTools.Database;

import com.BryceBG.DatabaseTools.Database.Author.AuthorDao;
import com.BryceBG.DatabaseTools.Database.Book.BookDao;
import com.BryceBG.DatabaseTools.Database.Series.SeriesDao;
import com.BryceBG.DatabaseTools.Database.User.UserDao;
import com.BryceBG.DatabaseTools.utils.Utils;

/**
 * This class just adds and instantiates a bunch of static variables that
 * various parts of the app require. This permits access to functions in the
 * other Daos in a static context
 * 
 * @author Bryce-BG
 *
 */
public class DAORoot {
	public static BookDao bookDao;
	public static UserDao userDao;
	public static SeriesDao seriesDao;
	public static AuthorDao authorDao;
	public static LibraryDB library;

	static {
		library = new LibraryDB(Utils.getConfigString("app.dbhost", null), Utils.getConfigString("app.dbport", null),
				Utils.getConfigString("app.dbname", null), Utils.getConfigString("app.dbpass", null),
				Utils.getConfigString("app.dbuser", null));

		// Note: we can just make all the functions in the _Dao classes static instead
		// of how they currently are and then these below references aren't needed. I'm
		// not sure which is the best performance wise but this should be tested later
		bookDao = new BookDao();
		userDao = new UserDao();
		seriesDao = new SeriesDao();
		authorDao = new AuthorDao();

	}

}
