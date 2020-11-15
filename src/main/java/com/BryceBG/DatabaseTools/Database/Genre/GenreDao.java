package com.BryceBG.DatabaseTools.Database.Genre;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.utils.DaoUtils;

public class GenreDao {
	private static final Logger logger = LogManager.getLogger(GenreDao.class.getName());

	// constants used for the editGenre function
	public static final int GENRE_DESCRIPTION = 1;
	public static final int GENRE_NAME = 2;
	public static final int KEYWORDS = 3;
	public static final int MY_GOODREADS_EQUIVALENT = 4;
	public static final int PARENT = 5;

	/**
	 * Gets the names of genres in the system.
	 * 
	 * @return an empty arraylist if an error occurs. Otherwise returns names of all
	 *         genres in the database.
	 */
	public ArrayList<String> getAllGenreNames() {
		ArrayList<String> rtVal = new ArrayList<String>();
		String sql = "SELECT genre_name FROM genres";

		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			// 2. execute our query.
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					rtVal.add(rs.getString("genre_name"));
				}
			} // end of try-with-resources: result set
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	/**
	 * Gets all genres in the system
	 * 
	 * @return an arraylist of all the genres in the system or an empty arraylist if
	 *         it fails.
	 */
	public ArrayList<Genre> getAllGenres() {
		ArrayList<Genre> rtVal = new ArrayList<Genre>();
		String sql = "SELECT * FROM genres";

		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			// 2. execute our query.
			try (ResultSet rs = pstmt.executeQuery()) {
				Pair<Boolean, Genre[]> temp = helperProcessGenreResultSet(conn, rs);
				if (temp.getValue0().booleanValue()) // log that an error occurred.
					logger.warn(
							"An error occured proccessing the results of the genre sql query (so one or more genre may be incorrect");
				Collections.addAll(rtVal, temp.getValue1()); // convert to an arraylist
			} // end of try-with-resources: result set
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	/**
	 * A function that gets a genre object from our database.
	 * 
	 * @param genreName The name of the genre we are looking up in the database.
	 * @return NULL if there was an error. Otherwise returns the genre specified.
	 */
	public Genre getGenre(String genreName) {
		// 1. verify acceptable lookup string.
		if (!DaoUtils.stringIsOk(genreName))
			return null;

		genreName = genreName.strip();
		Genre rtVal = null;
		String sql = "SELECT * FROM genres WHERE genre_name=?";

		// 2. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setString(1, genreName);
			// 3. execute our query.
			try (ResultSet rs = pstmt.executeQuery()) {
				Pair<Boolean, Genre[]> temp = helperProcessGenreResultSet(conn, rs);
				// need to check the length as if there were no result set entries the length
				// will be 0 which can cause issues
				if (temp.getValue0() == true || temp.getValue1().length != 1) { // log that an error occurred. or there
																				// were simply no results
					if (temp.getValue0() == true)
						logger.warn("An error may have occured proccessing the results of the genre sql query {}, {}",
								temp.getValue0(), temp.getValue1().length);
					else
						logger.debug("No results were returned for genre: {}", genreName);
				} else
					rtVal = temp.getValue1()[0];// set return

			} // end of try-with-resources: result set
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	/**
	 * This function allows addition of a new genre to the database.
	 * 
	 * @param genreDescription The description of what the genre covers and is used
	 *                         for.
	 * @param genreName        The name to display for the new genre.
	 * @param keywords         Keywords that are commonly associated with the genre.
	 * @param myGdrdsEquiv     The equivalent shelf name from our goodread shelves.
	 * @param parent           The parent that this is descended from. For example:
	 *                         we can have genre: "mythos". and genre "greek_mythos"
	 *                         which has "mythos" as its parent genre.
	 * @return True if update was successful, false if it failed.
	 */
	public boolean addGenre(String genreDescription, String genreName, String[] keywords, String myGdrdsEquiv,
			String parent) {
		boolean rtVal = false;
		// 1. validate and format our inputs
		if (!DaoUtils.stringIsOk(genreName)) {
			return rtVal;
		}
		genreName = genreName.strip();

		if (!DaoUtils.stringIsOk(genreDescription))
			genreDescription = null; // don't exit but make sure they didn't pass us a string of spaces.

		// format our keywords field
		if (keywords != null) {
			ArrayList<String> key2 = new ArrayList<String>();
			for (String key : keywords) {
				if (DaoUtils.stringIsOk(key))
					key2.add(key.strip());
			}
			keywords = key2.toArray(new String[key2.size()]);
		}
		// to protect against setting keyword array in database to null and overriding
		// our default (an empty array)
		else
			keywords = new String[0];

		String sql = "INSERT INTO genres(genre_description, genre_name, keywords, mygdrds_equiv, parent) VALUES (?, ?, ?, ?, ?);";

		// 2. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			conn.setAutoCommit(false);

			pstmt.setString(1, genreDescription);
			pstmt.setString(2, genreName); // as a new series it has no books

			Array arrayKeywords = conn.createArrayOf("VARCHAR", keywords);
			pstmt.setArray(3, arrayKeywords);
			pstmt.setString(4, myGdrdsEquiv);
			pstmt.setString(5, parent);

			// 3. execute our update for adding genre.
			int rs = pstmt.executeUpdate();

			// 4. check if sql query for series correctly modified 1 row.
			if (rs == 1) { // update was successful
				rtVal = true;
				conn.commit();
			} else {
				logger.info(String.format("The addGenre failed: the execute update returned: %i", rs));
				conn.rollback();
				rtVal = false;
			}
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}

		return rtVal;
	}

	/**
	 * Remove a genre from our database.
	 * 
	 * @param genreName The name of the genre in the database we want to remove.
	 * @return True if we were able to remove it from the database. False if we were
	 *         not.
	 */
	public boolean removeGenre(String genreName) {
		boolean rtVal = false;

		if (!DaoUtils.stringIsOk(genreName))
			return rtVal; // would fail due to invalid genreName so no point in establishing DB connection

		String sql = "DELETE FROM genres WHERE genre_name=?";
		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			conn.setAutoCommit(false);
			pstmt.setString(1, genreName);

			// 2. execute our update for removing series.
			int rs = pstmt.executeUpdate();
			// 3. check if sql query for series returned correct answer: should have updated
			// 1

			if (rs == 1) {
				// update was successful
				rtVal = true;
				conn.commit();
			} else {
				conn.rollback();
				logger.info(String.format("The removeSeries failed: the execute update returned: %d", rs));
				rtVal = false;
			}
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	/**
	 * A function that allows an existing genre in the database to be edited.
	 * 
	 * @param <T>       Should be a String if editing any field except keywords (in
	 *                  which case it should be a string[])
	 * @param field     What field we are trying to edit in the genre entry
	 * @param genreName what is the primary key of the entry
	 * @param value     what is the new value we are setting the field to.
	 * @return True if successful, false if update failed.
	 */
	public <T> boolean editGenre(int field, String genreName, T value) {
		boolean rtVal = false;
		Genre placeholderGenre = new Genre("", "", "", "", new String[0]);
		if (value == null)
			return rtVal;

		switch (field) {
		case GENRE_DESCRIPTION:
			// ensure it is a string
			if (value.getClass().getTypeName().equals(placeholderGenre.getGenreDescription().getClass().getTypeName()))
				rtVal = setGenreDescription(genreName, (String) value);
			break;
		case GENRE_NAME:
			if (value.getClass().getTypeName().equals(placeholderGenre.getGenreName().getClass().getTypeName()))
				rtVal = setGenreName(genreName, (String) value);
			break;
		case KEYWORDS:
			if (value.getClass().getTypeName().equals(placeholderGenre.getKeywords().getClass().getTypeName()))
				rtVal = setGenreKeywords(genreName, (String[]) value);
			break;
		case MY_GOODREADS_EQUIVALENT:
			if (value.getClass().getTypeName().equals(placeholderGenre.getMygdrdsEquiv().getClass().getTypeName()))
				rtVal = setGenreGoodReadsEquivalent(genreName, (String) value);
			break;
		case PARENT:
			if (value.getClass().getTypeName().equals(placeholderGenre.getParent().getClass().getTypeName()))
				rtVal = setGenreParent(genreName, (String) value);
			break;
		default:
			
		}
		return rtVal;

	}

	private boolean setGenreParent(String genreName, String value) {
		if(getGenre(value)!=null)
			return setGenreField(genreName, "parent", value.strip());
		else
		return false;
	}

	private boolean setGenreGoodReadsEquivalent(String genreName, String value) {
		return setGenreField(genreName, "mygdrds_equiv", value.strip());
	}

	private boolean setGenreKeywords(String genreName, String[] keywords) {
		// format our keywords field
		ArrayList<String> key2 = new ArrayList<String>();
		for (String key : keywords) {
			if (DaoUtils.stringIsOk(key))
				key2.add(key.strip());
		}
		keywords = key2.toArray(new String[key2.size()]);

		return setGenreField(genreName, "keywords", keywords);
	}

	private boolean setGenreName(String genreName, String value) {
		// 1. ensure new name is not empty or null
		if (DaoUtils.stringIsOk(value)) { // update will fail if genre already exists
			return setGenreField(genreName, "genre_name", value.strip());
		} else
			return false;
	}

	private boolean setGenreDescription(String genreName, String value) {
		return setGenreField(genreName, "genre_description", value);
	}

	/**
	 * A generic function that sets the field of a genre
	 * 
	 * @param <T>       should be either a string or String[] depending on the field
	 *                  being set.
	 * @param genreName The name of the genre we are editing.
	 * @param fieldName What field of the genre we are editing.
	 * @param newVal    The new value to set the field to.
	 * @return true if field was set successfully. False if it was not.
	 */
	private <T> boolean setGenreField(String genreName, String fieldName, T newVal) {
		boolean rtVal = false;
		// 1. validate genreName by querying genre table.
		Genre genreBeingEdited = getGenre(genreName);
		if (genreBeingEdited == null)
			return rtVal;

		// establish connection
		try (Connection conn = DAORoot.library.connectToDB();) {
			// 2. update genre entry
			rtVal = helperUpdateGenre(conn, genreBeingEdited.getGenreName(), fieldName, newVal);

		} catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	/**
	 * Helper function for genre table. Updates a field (determinde by fieldName)
	 * with newFieldValue
	 * 
	 * @param <T>           The type of the new value (usually, String, Integer,
	 *                      Boolean, Float)
	 * @param conn          An active connection to the database we are updating.
	 * @param genreName     name of the genre we are updating in the genre table.
	 * @param fieldName     what field we are updating: for example: description,
	 * @param newFieldValue The value we are replacing the field of the entry with.
	 * @return True if update was successful, False if update failed.
	 */
	private <T> boolean helperUpdateGenre(Connection conn, String genreName, String fieldName, T newFieldValue) {
		boolean rtnVal = false;
		String sql = String.format("UPDATE genres SET %s=? WHERE genre_name=?", fieldName);
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// 1. set our first parameter which has unknown type
			// determine type of newFieldValue and appropriate jdbc setter for it.
			String typeString = newFieldValue.getClass().getTypeName();
			if ("java.lang.String".equals(typeString))
				pstmt.setString(1, ((String) newFieldValue));
			else if ("java.lang.String[]".equals(typeString)) {
				Array arrayKeywords = conn.createArrayOf("VARCHAR", ((String[]) newFieldValue));
				pstmt.setArray(1, arrayKeywords);
			} else
				throw new Exception("type was not a supported type for genre table edits");

			pstmt.setString(2, genreName);
			// perform update and determine success
			int rv = pstmt.executeUpdate();
			if (rv == 1)
				rtnVal = true;

		} catch (SQLException e) {
			logger.error("An update to set genre: \'{}\', field: \'{}\', to value: \'{}\' failed. Exception: {}",
					genreName, fieldName, newFieldValue, e.getMessage());
		} catch (Exception e) {
			logger.info("Failed to update genre table because: {}", e.getMessage());
		}
		return rtnVal;
	}

	/**
	 * Helper function that processes a single or multiple entries resulting from a
	 * query to the genres table with all fields included
	 * 
	 * @param conn An active connection to our database.
	 * @param rs   The result set we need to process and convert into java objects
	 * @return <TRUE, java_objects> if an error occurred on processing one of the
	 *         books. Otherwise, returns <False, java_objects>
	 */

	private Pair<Boolean, Genre[]> helperProcessGenreResultSet(Connection conn, ResultSet rs) {
		Boolean errorsOccurred = Boolean.FALSE;
		ArrayList<Genre> rtVal = new ArrayList<Genre>();
		// 3. loop through records returned to parse our data.
		try {
			while (rs.next()) {
				try {
					String parent = rs.getString("parent");
					String genreName = rs.getString("genre_name");
					String genreDescription = rs.getString("genre_description");
					String mygdrdsEquiv = rs.getString("mygdrds_equiv");
					Array keywordsArray = rs.getArray("keywords");
					String[] keywords = null;
					if (keywordsArray != null)
						keywords = (String[]) keywordsArray.getArray();

					// 5. create our return object with the values
					rtVal.add(new Genre(parent, genreName, genreDescription, mygdrdsEquiv, keywords));

				} catch (SQLException e) { // Redundant with outer catch BUT lets us continue if there was an unexpected
											// error processing
											// a single book in the list but the rest are fine
					errorsOccurred = Boolean.TRUE;
					logger.error("Exception occured during executing SQL statement: " + e.getMessage());
				}
			}
		} catch (SQLException e) {
			errorsOccurred = Boolean.TRUE;
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return new Pair<Boolean, Genre[]>(errorsOccurred, rtVal.toArray(new Genre[rtVal.size()]));
	}

}
