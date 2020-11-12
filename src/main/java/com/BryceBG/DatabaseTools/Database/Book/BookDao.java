package com.BryceBG.DatabaseTools.Database.Book;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Author.Author;
import com.BryceBG.DatabaseTools.Database.Series.Series;
import com.BryceBG.DatabaseTools.Database.Series.SeriesDao;
import com.BryceBG.DatabaseTools.utils.DaoUtils;
import com.BryceBG.DatabaseTools.utils.IdentifierUtils;

/**
 * This is the public DAO (data access object) for interfacing with the book
 * table from our database (also the related junction tables indirectly).
 * 
 * @author Bryce-BG
 *
 */
public class BookDao implements BookDaoInterface {
	private static final Logger logger = LogManager.getLogger(BookDao.class.getName());

	/**
	 * Function to get all books currently in our database. This should probably
	 * only be used for testing at this time as it is not setup to handle the
	 * massive loads of a fully loaded database.
	 */
	@Override
	public ArrayList<Book> getAllBooks() {
		ArrayList<Book> rtVal = new ArrayList<Book>();
		String sql = "SELECT * FROM BOOKS";

		// 1. establish connection to our database (and create our prepared statements
		try (Connection conn = DAORoot.library.connectToDB();
				PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);) {
			// 2. execute our query.
			try (ResultSet rs = pstmt.executeQuery()) {

				Pair<Boolean, Book[]> temp = helperProcessBookResultSet(conn, rs); // process this
				if (temp.getValue0().booleanValue()) // log that an error occurred.
					logger.warn("An error occured proccessing the results of the sql query");
				Collections.addAll(rtVal, temp.getValue1()); // convert to an arraylist
			} // end of try-with-resources: connection
		} // catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	/**
	 * A function to get a single book from the database based on a book_id value.
	 * 
	 * @param bookID The book_id for a book we want to retrieve from the database.
	 * @return Null if there was an error Or if no errror, the book that matches the
	 *         id passed in from the database
	 */
	@Override
	public Book getBookByBookID(long bookID) {
		Book bookX = null;
		String sql = "SELECT * FROM BOOKS WHERE book_id=?;";

		// 1. establish connection to our database (and create our prepared statements
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {

			pstmt.setLong(1, bookID);
			// 2. execute our query.
			try (ResultSet rs = pstmt.executeQuery()) {
				Pair<Boolean, Book[]> temp = helperProcessBookResultSet(conn, rs);
				if (temp.getValue0() == true || temp.getValue1().length != 1) {
					if (temp.getValue1().length != 1)
						logger.info("No results were returned from query for book_id {}", bookID);
					else
						logger.warn("An error occured proccessing the results of the sql query");
				} else {
					bookX = temp.getValue1()[0];
				}
			} // end of try-with-resources: result set
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return bookX;
	}

	/**
	 * A function that gets all books in the database that the author has
	 * authored/co-authored to the author provided.
	 * 
	 * @param author_id the id of the author we want to query for books.
	 * @return null if the author has no books listed in the database or an
	 *         exception occurred. Otherwise a list of all books the author has
	 *         authored/co-authored is returned
	 */
	@Override
	public Book[] getBooksByAuthor(int author_id) {
		Book[] rtVal = new Book[0];

		try (Connection conn = DAORoot.library.connectToDB();) {

			// 2. call helper function to get book_ids for where author has contributed
			long[] bookIDs = helperGetBookIDsForAuthor(conn, author_id);
			if (bookIDs != null) {
				// 3. query to get all books with ids from our list.
				// ugly workaround to dealing with IN array close (poor performance since we
				// can't pre-prepare it)
				// https://stackoverflow.com/questions/178479/preparedstatement-in-clause-alternatives
				// https://stackoverflow.com/questions/36929116/how-to-use-in-clause-with-preparedstatement-in-postgresql/36930781#36930781
				// https://stackoverflow.com/questions/3107044/preparedstatement-with-list-of-parameters-in-a-in-clause
				String valueClause = String.join(", ", Collections.nCopies(bookIDs.length, "?"));
				String sql = "select * from books WHERE book_id IN (" + valueClause + ")";

				try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
					int index = 1;
					for (long id : bookIDs) {
						pstmt.setLong(index++, id);
					}
					// 4. execute query to get book data and parse results
					try (ResultSet rs = pstmt.executeQuery()) {
						// 5. parse results
						Pair<Boolean, Book[]> temp = helperProcessBookResultSet(conn, rs);
						if (temp.getValue0()) {// error occurred
							// log error
							logger.warn("An error occured proccessing the results of the sql query");
						}
						rtVal = temp.getValue1();
					} // end of try-with-resources: result set
				} // end prepared statement try
			}
		} catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: {}", e.getMessage());
		}
		return rtVal;
	}

	/**
	 * A function to search for a book by some identifier scheme for example:
	 * ("ISBN", "0199535566")
	 * 
	 * @param identifier_name  The scheme that is used with this identifier. For
	 *                         example: ISBN, ASIN, UUID, etc.
	 * @param identifier_value The value of the identifier for the book we are
	 *                         looking for. For example "0199535566" for a value.
	 * @return The book if any matching entry was found. If nothing was found or an
	 *         error occurred then null will be returned.
	 */
	@Override
	public Book getBookByIdentifier(String identifier_name, String identifier_value) {
		Book rtVal = null;
		// 1. ensure no crashes due to null or empty strings passed in.
		if (DaoUtils.stringIsOk(identifier_name) && DaoUtils.stringIsOk(identifier_value)) {

			// 2. call helper function to format our identifier passed in
			@SuppressWarnings("unchecked")
			Pair<String, String>[] id_pair_array = new Pair[1];
			id_pair_array[0] = new Pair<String, String>(identifier_name, identifier_value);
			id_pair_array = IdentifierUtils.formatAndValidateIdentifiers(id_pair_array);

			if (id_pair_array.length != 0) { // just in case the id was invalid we want to note this and stop
				identifier_name = id_pair_array[0].getValue0();
				identifier_value = id_pair_array[0].getValue1();

				// 3. establish DB connection
				try (Connection conn = DAORoot.library.connectToDB();) {

					// 4. query for entry in book_identifiers
					long book_id = helperGetBookIDsFromIdentifiers(conn, id_pair_array[0]);
					if (book_id != 0) {
						rtVal = getBookByBookID(book_id);
					}
					// 5. get the book call getBookByBookID()

				} catch (ClassNotFoundException e) {
					logger.error("Exception occured during connectToDB: " + e.getMessage());
				} catch (SQLException e) {
					logger.error("Exception occured during executing SQL statement: " + e.getMessage());
				}
			} else {
				logger.info("The identifier passed in was invalid so no search was performed");
			}
		}
		return rtVal;
	}

	/**
	 * Gets a book randomly from our database and returns it.
	 * 
	 * @return returns null if an error occurred. Otherwise, returns a random book
	 *         from our database.
	 */
	@Override
	public Book getRandomBook() {
		// intended to be used on the front page of our website
		String sql = "select * from books offset random() * (select count(*) from books) limit 1 ;";
		Book bookX = null;

		// 1. establish connection to our database (and create our prepared statements
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {

			// 2. execute our query.
			try (ResultSet rs = pstmt.executeQuery()) {
				// 3. loop through records returned to parse our data.
				Pair<Boolean, Book[]> temp = helperProcessBookResultSet(conn, rs);
				if (temp.getValue0() || temp.getValue1().length != 1) {// error occurred
					// log error
					if (temp.getValue1().length != 1) {
						logger.warn(
								"unexpected amount of results returned from query for a single book. Instead got: {}",
								temp.getValue1().length);
						;
					}
					logger.warn("An error occured proccessing the results of the sql query");
				} else
					bookX = temp.getValue1()[0];
			} // end of try-with-resources: result set
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return bookX;
	}

	/**
	 * Gets all the books in the database that the series passed in contains.
	 * 
	 * @param series The series we want to inquire for.
	 * @return null if an error occurred otherwise returns all books which have the
	 *         series_id = to the id of the series passed in.
	 */
	@Override
	public Book[] getBooksBySeries(Series series) {
		// Template after getAllBooks()
		Book[] rtVal = new Book[0];
		if (series != null) {
			String sql = "SELECT * FROM books WHERE series_id=?";

			// 1. establish connection to our database (and create our prepared statements
			try (Connection conn = DAORoot.library.connectToDB();
					PreparedStatement pstmt = conn.prepareStatement(sql);) {
				pstmt.setInt(1, series.getSeriesID());
				// 2. execute our query.
				try (ResultSet rs = pstmt.executeQuery()) {
					// 3. loop through records returned to parse our data.
					Pair<Boolean, Book[]> temp = helperProcessBookResultSet(conn, rs);
					if (temp.getValue0()) {// error occurred
						// log error
						logger.warn("An error occured proccessing the results of the sql query");
					}
					rtVal = temp.getValue1();
				} // end of try-with-resources: result set
			} // end of try-with-resources: connection
				// catch blocks for try-with-resources: connection
			catch (ClassNotFoundException e) {
				logger.error("Exception occured during connectToDB: " + e.getMessage());
			} catch (SQLException e) {
				logger.error("Exception occured during executing SQL statement: " + e.getMessage());
			}
		}

		return rtVal;
	}

	/**
	 * A primitive implementation to allow searching of the database for books by
	 * title.
	 * 
	 * @param title The title or partial title of the book to look for.
	 * @return returns an array of books where the title matches the passed in
	 *         string. Array is empty if no results were found or an error occurred.
	 */
	@Override
	public Book[] getBooksByTitle(String title) {
		// modeled after getBySeries
		Book[] rtVal = new Book[0];
		// 1. validate title passed in isn't going to cause issues.
		if (DaoUtils.stringIsOk(title)) {
			title = "%" + title + "%";
			String sql = "SELECT * FROM books WHERE title ILIKE ?;";

			// 1. establish connection to our database (and create our prepared statements
			try (Connection conn = DAORoot.library.connectToDB();
					PreparedStatement pstmt = conn.prepareStatement(sql);) {
				pstmt.setString(1, title);
				// 2. execute our query.
				try (ResultSet rs = pstmt.executeQuery()) {
					Pair<Boolean, Book[]> temp = helperProcessBookResultSet(conn, rs);
					if (temp.getValue0()) {// error occurred
						// log error
						logger.warn("An error occured proccessing the results of the sql query");
					}
					rtVal = temp.getValue1();
				} // end of try-with-resources: result set
			} // end of try-with-resources: connection
				// catch blocks for try-with-resources: connection
			catch (ClassNotFoundException e) {
				logger.error("Exception occured during connectToDB: " + e.getMessage());
			} catch (SQLException e) {
				logger.error("Exception occured during executing SQL statement: " + e.getMessage());
			}
		}

		return rtVal;
	}

	/**
	 * Function to remove a book from the database and update all related table
	 * entries which are referencing the book.
	 * 
	 * @param bookID unique ID of the book which we are trying to remove.
	 * @return true if the book was successfully removed and all other tables
	 *         updated. False if the removal or update of other tables fails.
	 */
	@Override
	public boolean removeBook(long bookID) {
		boolean transactionShouldContinue = true;
		boolean rtVal = false;
		String sql = "DELETE FROM books WHERE book_id=?;";
		// 1. validate its a real book_id
		Book bookToDelete = getBookByBookID(bookID);
		if (bookToDelete == null) // book doesn't exist exit now
			return rtVal;

		// 2. start connection.
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			// 3. disable auto-commit (so we can act like a transaction)
			conn.setAutoCommit(false);
			pstmt.setLong(1, bookID);

			// 4. execute remove book from books table (and check success)
			// Note: Because of our cascade settings in database automatically will remove
			// referenced entries from: book_identifiers, book_authors, and book_genres
			int rs = pstmt.executeUpdate();
			// check if SQL update correctly modified 1 row.
			if (rs == 1) {
				// 5. if in series: update count booksInSeries
				if (bookToDelete.getSeriesID() != 0) {
					// get info needed to update our series
					Series theBookSeries = DAORoot.seriesDao.getSeriesBySeriesID(bookToDelete.getSeriesID());
					// decrement count books in series.
					boolean rtnedVal = DAORoot.seriesDao.updateSeriesBookCount(theBookSeries.getSeriesName(),
							bookToDelete.getPrimaryAuthorID(), SeriesDao.UpdateType.DEC);
					transactionShouldContinue = transactionShouldContinue & rtnedVal;
					if (!transactionShouldContinue) { // update failed
						logger.info("The updateSeriesBookCount() failed");
					}
				}
			} else { // update was either unsuccessful or modified more rows than expected
				logger.info("The removeBook() failed: the execute update returned: {}", rs);
				transactionShouldContinue = false;
			}
			// 6. if all updates succeeded, commit and otherwise abort transaction.
			if (transactionShouldContinue) {
				conn.commit();
				rtVal = true;
			} else {
				conn.rollback();
				rtVal = false;
			}
		} catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}

		return rtVal;
	}

	/**
	 * Function to add a book to our database.
	 * 
	 * @param authorIDs   The IDs of the authors who have authored/co-authored the
	 *                    book.
	 * @param description The blurb for what the book is about (can be left blank
	 *                    but NOT null if no such blurb exists)
	 * @param edition     The edition of the book. (if unknown set to -1)
	 * @param title       The title of our book.
	 * @return returns true if book was successfully added. False if an error
	 *         occurred
	 */
	@Override
	public boolean addBook(int[] authorIDs, String description, int edition, String title) {
		// a 1 way boolean to indicate if we should commit or abort the transaction
		boolean transactionShouldContinue = true;
		boolean rtVal = false; // indicate success or failure of adding a book to the system.
		String sql = "INSERT INTO books(count_authors, description, edition, primary_author_id, title) VALUES(?, ?, ?, ?, ?);";

		// 1. Perform very basic validation on input entries (more should be done at the
		// controller level)
		
		// 1 a. authorIDs are all greater than 0
		for (int id : authorIDs) {
			if (id <= 0) {
				logger.info("addBook failed because authorID \"{}\" was determined to be invalid", id);
				return false;
			}
		}
		
		// 1 b. ensure title is not empty or null and description is not null (can be empty
		// though)
		if (!DaoUtils.stringIsOk(title) || description == null) {
			logger.info("addBook failed because title: \"{}\" or description \"{}\" was determined to be invalid", title,
					description);
			return false;
		}

		// 2. establish db connection
		try (Connection conn = DAORoot.library.connectToDB();
				// this RETERN_GENERATED_KEYS is an alternative to our RETURNING book_id in sql
				PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);) {
			// 3. disable auto-commit (so we can act like a transaction)
			conn.setAutoCommit(false);

			// set fields for inserting into books
			pstmt.setInt(1, authorIDs.length);
			pstmt.setString(2, description);
			pstmt.setInt(3, edition);
			pstmt.setInt(4, DaoUtils.findPrimaryAuthor(authorIDs));
			pstmt.setString(5, title);

			// 4. update books table and validate success
			int rv = pstmt.executeUpdate();
			if (rv == 1) {

				// 5. get the book_id of new book we are adding.
				ResultSet rs_lastID = pstmt.getGeneratedKeys();

				if (rs_lastID.next()) {
					long bookID = rs_lastID.getLong("book_id");
					rs_lastID.close();

					// 6. update our book_authors table
					rv = helperAddBookAuthors(conn, bookID, authorIDs);
					// 7. check that we added the right amount of rows to the book_authors table
					if (rv != authorIDs.length) { // error occurred
						logger.info(
								"The addBook() failed: the execute update to book_authors table returned: {}, Expected: {}",
								rv, authorIDs.length);
						transactionShouldContinue = false;
					}
				} else // failed to get a return book_id for some reason
					transactionShouldContinue = false;
			} else {
				logger.info("The addBook() failed: the execute update to books table returned: {}", rv);
				transactionShouldContinue = false;
			}
			// 8. if all updates succeeded, commit and otherwise abort transaction.
			if (transactionShouldContinue) {
				conn.commit();
				rtVal = true;
			} else {
				conn.rollback();
				rtVal = false;
			}
		} catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	/**
	 * This function allows editing of a book's fields in our database.
	 * 
	 * @param <T>
	 * @param bookID   The id of the book we are editing.
	 * @param editType The type of edit we are performing to the book
	 * @param newVal   This is the specific value required for the field. For
	 *                 example if we are adding an author this should be a
	 */
	@Override
	public <T> boolean editBook(long bookID, EDIT_TYPE editType, T newVal) {
		boolean rtnedVal = false;
		// TODO Auto-generated method stub
		logger.error("editBook() was called but this function is currently a STUB");
		if (newVal == null)
			return false;

		switch (editType) {
		case ADD_AUTHOR:
			if (editType.checkFitsRequiredType(newVal)) // verify the class of the object isn't going to cause a crash
				rtnedVal = bookAddAuthor(bookID, (Integer) newVal);
			break;
		case REMOVE_AUTHOR:
			// TODO call helper
			break;
		case SET_AVG_RATING:
			// TODO call helper
			break;
		case SET_BOOK_INDEX_IN_SERIES:
			// TODO call helper
			break;
		case SET_COVER_LOCATION:
			// TODO call helper
			break;
		case SET_COVER_NAME:
			// TODO call helper
			break;
		case SET_DESCRIPTION:
			// TODO call helper
			break;
		case SET_EDITION:
			// TODO call helper
			break;
		case SET_GENRES:
			// TODO call helper
			break;
		case SET_IDENTIFIERS:
			// TODO call helper
			break;
		case SET_PUBLISH_DATE:
			// TODO call helper
			break;
		case SET_PUBLISHER:
			// TODO call helper
			break;
		case SET_RATING_COUNT:
			// TODO call helper
			break;
		case SET_SERIES_ID:
			// TODO call helper
			break;

		default: // was null
		}

		return rtnedVal;
	}

	// Helpers
	/** Start helpers for editBook function */

	/**
	 * Helper function that allows the addition of authors to a book in the
	 * database.
	 * 
	 * @param bookID      ID of the book in the database we are adding a new author
	 *                    too.
	 * @param newAuthorID ID of the new author for the book
	 * @return true if update was successful. false if it failed.
	 */
	private boolean bookAddAuthor(long bookID, Integer newAuthorID) {
		boolean rtVal = false;
		// 1. validate bookID by querying book table.
		Book bookX = getBookByBookID(bookID);
		if (bookX == null) {
			return false;	
		}

		// 2. validate new authorID? 
		Author authorX = DAORoot.authorDao.getAuthor(newAuthorID.intValue());
		if (authorX == null) {
			return false;
		}

		int[] authorIDs = bookX.getAuthorIDs();
		
		// 3. ensure author not already in list of authorIDs.
		for (int x : authorIDs) {
			if (x == newAuthorID.intValue()) {
				logger.info("new author id {} is already listed as an author for the book", newAuthorID.intValue());
				return false;
			}
		}

		// 4. check if primary_author_id in books table needs to be updated

		// create array of author IDs from old + new

		int[] newAuthorIDs = new int[authorIDs.length + 1];
		for (int x = 0; x < authorIDs.length; x++) {
			newAuthorIDs[x] = authorIDs[x];
		}
		newAuthorIDs[newAuthorIDs.length-1] = newAuthorID.intValue();
		


		//need to update book_authors table AND books count_authors so start at 2
		int countNeededRowUpdates = 2; //how many rows we need to update in our database.
		if (DaoUtils.findPrimaryAuthor(newAuthorIDs) != bookX.getPrimaryAuthorID()) {
			// ALSO need to update books table primary_author_id row
			countNeededRowUpdates++;
		}

		// establish connection
		try (Connection conn = DAORoot.library.connectToDB();) {
			conn.setAutoCommit(false); // disable auto-commit (transaction)
			if (countNeededRowUpdates == 3) {
				// call generic helper function that updates books table 
				if (helperUpdateBooks(conn, bookID, "primary_author_id", newAuthorID) && 
					helperUpdateBooks(conn, bookID, "count_authors", bookX.getCountAuthors()+1)) {
					countNeededRowUpdates-=2;
				}
			}
			else if(countNeededRowUpdates==2) {//just need to update count authors
				// call generic helper function that updates books table 
				if (helperUpdateBooks(conn, bookID, "count_authors", bookX.getCountAuthors()+1)) {
					countNeededRowUpdates--;
				}
			}

			// 5. update book_authors table. (if books update was successful or not needed)
			// no need to query if first update failed.
			if (countNeededRowUpdates == 1) {
				countNeededRowUpdates -= helperAddBookAuthors(conn, bookID, new int[] { newAuthorID.intValue() });
			}

			// 6. commit/abort transaction
			if (countNeededRowUpdates == 0) {
				conn.commit();
				rtVal = true;
			} else {
				conn.rollback();
				rtVal = false;
			}
		} catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	/** end helpers for editBook function */
	/**
	 * Helper function for books table. Updates a field
	 * 
	 * @param <T>           The type of the new value (usually, String, Integer,
	 * @param conn          An active connection to the database we are updating.
	 * @param bookID        ID of the book we are updating in the books table.
	 * @param fieldName     what field we are updating: for example
	 *                      primary_author_id, or description (see install.sql books
	 *                      table for full list of names)
	 * @param newFieldValue The value we are replacing the field of the entry with.
	 * @return True if update was successful, False if update failed.
	 */
	private <T> boolean helperUpdateBooks(Connection conn, long bookID, String fieldName, T newFieldValue) {
		boolean rtnVal = false;
		String sql = String.format("UPDATE books SET %s=? WHERE book_id=?", fieldName);
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// 1. set our first parameter which has unknown type
			// determine type of newFieldValue and appropriate jdbc setter for it.
			String typeString = newFieldValue.getClass().getTypeName();
			if ("java.lang.Integer".equals(typeString))
				pstmt.setInt(1, ((Integer) newFieldValue).intValue());
			else if ("java.lang.Float".equals(typeString))
				pstmt.setFloat(1, ((Float) newFieldValue).floatValue());
			else if ("java.lang.String".equals(typeString))
				pstmt.setString(1, ((String) newFieldValue));
			else if ("java.lang.Boolean".equals(typeString))
				pstmt.setBoolean(1, ((Boolean) newFieldValue).booleanValue());
			else if ("java.sql.Date".equals(typeString))
				pstmt.setDate(1, ((java.sql.Date) newFieldValue));
			else if ("java.lang.Long".equals(typeString))
				pstmt.setLong(1, ((Long) newFieldValue).longValue());
			else
				throw new Exception("type was not a supported type for book table edits");

			pstmt.setLong(2, bookID);
			// perform update and determine success
			int rv = pstmt.executeUpdate();
			if (rv == 1)
				rtnVal = true;

		} catch (SQLException e) {
			logger.error("An update to set book_id: {}, field: {}, to value: {} failed. Exception: {}", bookID,
					fieldName, newFieldValue, e.getMessage());
		} catch (Exception e) {
			logger.info("Failed to update books table because: {}", e.getMessage());
		}
		return rtnVal;
	}

	/**
	 * A function that takes the result set of a query for our "books" table and
	 * then compiles all necessary data from other tables to construct an array of
	 * book objects and returns them.
	 * 
	 * @param conn An active connection to the database (used to query other tables
	 *             to get data as needed)
	 * @param rs   The resultset from our book table query (should be a "Select *
	 *             from books ____" like query
	 * @return a pair where the boolean value indicates if anything went wrong
	 *         (false indicates no errors) during our processing of the result set.
	 *         and the array of books.
	 */
	private Pair<Boolean, Book[]> helperProcessBookResultSet(Connection conn, ResultSet rs) {
		Boolean errorsOccurred = Boolean.FALSE;
		ArrayList<Book> rtVal = new ArrayList<Book>();
		// 3. loop through records returned to parse our data.
		try {
			while (rs.next()) {
				try {
					// 4. extract results from result set needed to create objects
					float avgRating = rs.getFloat("average_rating");
					long bookID = rs.getLong("book_id");
					float bookIndexInSeries = rs.getFloat("book_index_in_series");
					int countAuthors = rs.getInt("count_authors");
					String coverLocation = rs.getString("cover_location");
					String coverName = rs.getString("cover_name");
					String description = rs.getString("description");
					int edition = rs.getInt("edition");
					boolean has_identifiers = rs.getBoolean("has_identifiers");
					int primaryAuthorID = rs.getInt("primary_author_id");
					Date publishDate = rs.getDate("publish_date");
					String publisher = rs.getString("publisher");
					long ratingCount = rs.getLong("rating_count");
					int seriesID = rs.getInt("series_id"); // if it is null (the default) the field is set to 0
					String title = rs.getString("title");

					// 5. create our return object with the values
					Book bookX = new Book(avgRating, bookID, bookIndexInSeries, countAuthors, coverLocation, coverName,
							description, edition, has_identifiers, primaryAuthorID, publishDate, publisher, ratingCount,
							seriesID, title);
					// 6. fill additional fields as needed from other tables (authors (if multiple),
					// identifiers, and genres.

					// 6.a get extra authors
					if (countAuthors == 1) {
						// there are no other authors so skip additional query to reduce query overhead.
						bookX.setAuthorIDs(new int[] { bookX.getPrimaryAuthorID() });
					} else {
						// call helper function to get authors from book_authors table
						int[] authorIDs = helperGetBooksAuthors(conn, bookID);
						if (authorIDs == null) {
							// use logger to warn something went wrong but continue silently
							errorsOccurred = Boolean.TRUE;
							logger.warn("An error was detected getting book {}'s authors", title);
						}
						bookX.setAuthorIDs(authorIDs); // add authors
					}
					// 6.b. get identifiers.
					if (bookX.getHasIdentifiers()) {
						Pair<String, String>[] bookIdentifiers = helperGetBookIdentifiers(conn, bookID);
						if (bookIdentifiers == null) {
							// should have been identifiers but we didn't get them so something went wrong
							// in helper function.
							errorsOccurred = Boolean.TRUE;
							logger.warn(
									"An error was detected getting book {}'s identifiers (it should have had identifiers but doesn't)",
									title);
						}
						bookX.setIdentifiers(bookIdentifiers); // add our identifiers
					}
					// 6.c get genres.
					String[] genres = helperGetBooksGenres(conn, bookID);
					bookX.setGenres(genres); // add our genres to book object
					// 7. add our fully fleshed out book object to return list
					rtVal.add(bookX);
				} catch (SQLException e) { // Redundant BUT lets us continue if there was an unexpected error processing
											// a single book in the list but the rest are fine
					errorsOccurred = Boolean.TRUE;
					logger.error("Exception occured during executing SQL statement: " + e.getMessage());
				}
			}
		} catch (SQLException e) {
			errorsOccurred = Boolean.TRUE;
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return new Pair<Boolean, Book[]>(errorsOccurred, rtVal.toArray(new Book[rtVal.size()]));
	}

	/**
	 * Helper function for book_authors table. Adds all authors provided paired with
	 * the book_id to the table.
	 * 
	 * @param conn      An active connection to our database.
	 * @param authorIDs IDs of the authors we want to set for a book
	 * @param bookID    the id of the book we are adding authors to.
	 * @return returns the numbers of rows in the database that the update
	 *         modified/created
	 */
	private int helperAddBookAuthors(Connection conn, long bookID, int[] authorIDs) {
		// https://www.postgresqltutorial.com/postgresql-jdbc/insert/
		String sql = "INSERT into book_authors (book_id, author_id) VALUES (?,?);";
		int rtVal = 0;
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (int author_id : authorIDs) {
				pstmt.setLong(1, bookID);
				pstmt.setInt(2, author_id);
				pstmt.addBatch();
			}
			int[] rv = pstmt.executeBatch();
			// sum number of modified rows for all the statements in the batch (each should
			// be "1")
			for (int x : rv)
				rtVal += x;

		} catch (SQLException e) {
			logger.error(
					"Exception occurred during attempt to add authors for a book to the book_authors table. Exception: {}",
					e.getMessage());
		}

		return rtVal;
	}

	/**
	 * Helper function for book_authors table. This function gets entries from the
	 * database from the table book_authors (a supplementary "junction table" for
	 * books)
	 * 
	 * @param conn    An active connection to the database
	 * @param book_id ID of the book we are getting authors for.
	 * @return null if an exception occurred OR an array containing the IDs of the
	 *         authors who wrote the book.
	 */
	private int[] helperGetBooksAuthors(Connection conn, long book_id) {
		String sqlgetExtraAuthors = "SELECT author_id FROM book_authors WHERE book_id=?";
		int[] authorIDs = null;
		try (PreparedStatement pstmtGetAuthors = conn.prepareStatement(sqlgetExtraAuthors,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
			pstmtGetAuthors.setLong(1, book_id);
			try (ResultSet rs2 = pstmtGetAuthors.executeQuery()) {
				// loop through records returned to parse our data.
				int rowcount = 0;
				if (rs2.last()) { // https://stackoverflow.com/questions/192078/how-do-i-get-the-size-of-a-java-sql-resultset
					rowcount = rs2.getRow();
					rs2.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing
										// the first element
				}
				if (rowcount > 0) { // always should be but just in case
					authorIDs = new int[rowcount];
					rowcount = 0;
					while (rs2.next()) {
						authorIDs[rowcount] = rs2.getInt("author_id");
						rowcount++;
					}
				}
			} // end try rs2
		} catch (SQLException e) {
			logger.error("An exception occured getting authors for book_id {}. Exception: {}", book_id, e.getMessage());
		}
		return authorIDs;
	}

	/**
	 * Helper function for book_authors table. Used to get all book_ids that an
	 * author has either written or contributed to writing
	 * 
	 * @param conn      An active connection to the database we are querying.
	 * @param author_id The id of the author we are looking for books by
	 * @return null if an error occurs or there are no books in the database by the
	 *         author. Otherwise: a list of all book_id's that the author has
	 *         written
	 */
	private long[] helperGetBookIDsForAuthor(Connection conn, int author_id) {
		String sqlgetBookIDsByAuthorID = "SELECT book_id FROM book_authors WHERE author_id=?";
		long[] bookIDs = null;
		try (PreparedStatement pstmtGetBooks = conn.prepareStatement(sqlgetBookIDsByAuthorID,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
			pstmtGetBooks.setInt(1, author_id);
			try (ResultSet rs2 = pstmtGetBooks.executeQuery()) {
				// loop through records returned to parse our data.
				int rowcount = 0;
				// https://stackoverflow.com/questions/192078/how-do-i-get-the-size-of-a-java-sql-resultset
				if (rs2.last()) {
					rowcount = rs2.getRow();
					rs2.beforeFirst();
				}
				if (rowcount > 0) { // always should be but just in case
					bookIDs = new long[rowcount];
					rowcount = 0;
					while (rs2.next()) {
						bookIDs[rowcount] = rs2.getLong("book_id");
						rowcount++;
					}
				}
			} // end try rs2
		} catch (SQLException e) {
			logger.error("An exception occured getting book_ids for author_id {}. Exception: {}", author_id,
					e.getMessage());
		}
		return bookIDs;
	}

	/**
	 * Helper function for book_genres table. Gets the genres a book has listed from
	 * the database
	 * 
	 * @param conn    An active connection to the database we are querying.
	 * @param book_id ID of the book we want to get the genres of.
	 * @return null if an error occurs OR there are no genres listed for the book. A
	 *         list of genre names in all other cases
	 */
	private String[] helperGetBooksGenres(Connection conn, long book_id) {
		String sqlGetGenres = "SELECT genre_name FROM book_genres WHERE book_id=?";
		String[] genres = null;
		try (PreparedStatement pstmtGetGenre = conn.prepareStatement(sqlGetGenres, ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);) {
			pstmtGetGenre.setLong(1, book_id);
			try (ResultSet rs2 = pstmtGetGenre.executeQuery()) {
				// loop through records returned to parse our data.
				int rowcount = 0;
				if (rs2.last()) {
					rowcount = rs2.getRow();
					rs2.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing
										// the first element
				}
				if (rowcount != 0) { // ensure we don't crash if no genres are listed for a book
					genres = new String[rowcount];
					rowcount = 0; // reusing variable.
					while (rs2.next()) {
						genres[rowcount] = rs2.getString("genre_name");
						rowcount++;
					}
				}
			} // end try rs2
		} catch (SQLException e) {
			logger.error("An exception occured getting genres for book_id {}. Exception: {}", book_id, e.getMessage());
		}
		return genres;
	}

	/**
	 * Helper function book_identifiers table. Gets identifiers such as ASIN and
	 * ISBN numbers for a book from the database.
	 * 
	 * @param conn    An active connection to the database
	 * @param book_id ID of the book we are looking up identifiers for.
	 * @return returns null if there is an error getting the identifiers for the
	 *         book. Otherwise returns an array of pairs such that the first value
	 *         is the identifier type (ISBN, ASIN, etc.) and the second value is the
	 *         id for the identifier.
	 */
	@SuppressWarnings("unchecked")
	private Pair<String, String>[] helperGetBookIdentifiers(Connection conn, long book_id) {
		String sqlGetIdentiers = "SELECT identifier_type, identifier_value FROM book_identifier WHERE book_id=?";
		Pair<String, String>[] bookIdentifiers = null;

		try (PreparedStatement pstmtgetIdentifiers = conn.prepareStatement(sqlGetIdentiers,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
			pstmtgetIdentifiers.setLong(1, book_id);
			try (ResultSet rs2 = pstmtgetIdentifiers.executeQuery()) {
				// 3. loop through records returned to parse our data.
				int rowcount = 0;
				if (rs2.last()) {
					rowcount = rs2.getRow();
					rs2.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing
										// the first element
				}
				if (rowcount > 0) { // because has_identifiers should only be true if there are identifiers this
									// should always be true.
					bookIdentifiers = new Pair[rowcount];
					rowcount = 0; // reusing variable. to identify where to store now
					while (rs2.next()) {
						bookIdentifiers[rowcount] = new Pair<String, String>(rs2.getString("identifier_type"),
								rs2.getString("identifier_value"));
						rowcount++;
					}
				}
			} // end try rs2
		} catch (SQLException e) {
			logger.error("An exception occured getting identifiers for book_id {}. Exception: {}", book_id,
					e.getMessage());
		}
		return bookIdentifiers;
	}

	/**
	 * Helper function book_identifiers table. Gets a book id based on an identifier
	 * passed in. If any such book is in the database with that identifier.
	 * 
	 * @param conn       An active connection to the database we are querying.
	 * @param identifier The identifier for the book in the form (identifier_scheme,
	 *                   identifier_value)
	 * @return 0 if no such book is found. Otherwise, returns the book_id of the
	 *         book in the database with the value specified.
	 */
	private long helperGetBookIDsFromIdentifiers(Connection conn, Pair<String, String> identifier) {
		long rtVal = 0;
		String sql = "SELECT book_id FROM book_identifier WHERE identifier_type=? AND identifier_value=?";

		try (PreparedStatement pstmtgetIdentifiers = conn.prepareStatement(sql);) {
			pstmtgetIdentifiers.setString(1, identifier.getValue0());
			pstmtgetIdentifiers.setString(2, identifier.getValue1());

			try (ResultSet rs2 = pstmtgetIdentifiers.executeQuery()) {
				// 3. loop through records returned to parse our data.
				if (rs2.next()) {
					rtVal = rs2.getLong("book_id");
				}
			} // end try rs2
		} catch (SQLException e) {
			logger.error("An exception occured getting book with ID ({}, {}) Exception: {}", identifier.getValue0(),
					identifier.getValue1(), e.getMessage());
		}
		return rtVal;
	}

}
