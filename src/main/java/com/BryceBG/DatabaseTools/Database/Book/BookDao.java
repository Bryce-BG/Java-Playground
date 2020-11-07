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
import com.BryceBG.DatabaseTools.Database.Book.Book.BOOK_FIELD;
import com.BryceBG.DatabaseTools.Database.Series.Series;
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
				// 3. loop through records returned to parse our data.
				while (rs.next()) {
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
					int seriesID = rs.getInt("series_id");
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
							logger.warn("An error was detected getting book {}'s identifiers", title);
						}
						bookX.setIdentifiers(bookIdentifiers); // add our identifiers
					}
					// 6.c get genres.
					String[] genres = helperGetBooksGenres(conn, bookID);
					bookX.setGenres(genres); // add our genres to book object
					// 7. add our fully fleshed out book object to return list
					rtVal.add(bookX);
				}
			}
			// end of try-with-resources: result set
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
				// 3. loop through records returned to parse our data.
				if (rs.next()) {
					// 4. extract results from result set needed to create objects
					float avgRating = rs.getFloat("average_rating");
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
					int seriesID = rs.getInt("series_id");
					String title = rs.getString("title");

					// 5. create our return object with the values
					bookX = new Book(avgRating, bookID, bookIndexInSeries, countAuthors, coverLocation, coverName,
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
							logger.warn("An error was detected getting book {}'s identifiers", title);
						}
						bookX.setIdentifiers(bookIdentifiers); // add our identifiers
					}
					// 6.c get genres.
					String[] genres = helperGetBooksGenres(conn, bookID);
					bookX.setGenres(genres); // add our genres to book object
				} else {
					logger.warn("unexpectedly no results were returned from query for book_id {}", bookID);
				}
			}
			// end of try-with-resources: result set
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
		ArrayList<Book> rtVal = new ArrayList<Book>();

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

						// 3. loop through records returned to parse our data.
						while (rs.next()) {
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
							int seriesID = rs.getInt("series_id");
							String title = rs.getString("title");

							// 5. create our return object with the values
							Book bookX = new Book(avgRating, bookID, bookIndexInSeries, countAuthors, coverLocation,
									coverName, description, edition, has_identifiers, primaryAuthorID, publishDate,
									publisher, ratingCount, seriesID, title);
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
									logger.warn("An error was detected getting book {}'s identifiers", title);
								}
								bookX.setIdentifiers(bookIdentifiers); // add our identifiers
							}

							// 6.c get genres.
							String[] genres = helperGetBooksGenres(conn, bookID);
							bookX.setGenres(genres); // add our genres to book object

							// 7. add our fully fleshed out book object to return list
							rtVal.add(bookX);
						}
					} // end of try-with-resources: result set
				} // end prepared statement try
			}
		} catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: {}", e.getMessage());
		}

		return rtVal.toArray(new Book[rtVal.size()]);
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
	 * @return returns null if an error occured. Otherwise, returns a random book
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
				if (rs.next()) {
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
					int seriesID = rs.getInt("series_id");
					String title = rs.getString("title");

					// 5. create our return object with the values
					bookX = new Book(avgRating, bookID, bookIndexInSeries, countAuthors, coverLocation, coverName,
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
							logger.warn("An error was detected getting book {}'s identifiers", title);
						}
						bookX.setIdentifiers(bookIdentifiers); // add our identifiers
					}
					// 6.c get genres.
					String[] genres = helperGetBooksGenres(conn, bookID);
					bookX.setGenres(genres); // add our genres to book object
				} else {
					logger.warn("unexpectedly no results were returned from query for a random book");
				}
			}
			// end of try-with-resources: result set
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
		ArrayList<Book> rtVal = new ArrayList<Book>();
		if (series != null) {
			String sql = "SELECT * FROM books WHERE series_id=?";

			// 1. establish connection to our database (and create our prepared statements
			try (Connection conn = DAORoot.library.connectToDB();
					PreparedStatement pstmt = conn.prepareStatement(sql);) {
				pstmt.setInt(1, series.getSeriesID());
				// 2. execute our query.
				try (ResultSet rs = pstmt.executeQuery()) {
					// 3. loop through records returned to parse our data.
					while (rs.next()) {
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
						int seriesID = rs.getInt("series_id");
						String title = rs.getString("title");

						// 5. create our return object with the values
						Book bookX = new Book(avgRating, bookID, bookIndexInSeries, countAuthors, coverLocation,
								coverName, description, edition, has_identifiers, primaryAuthorID, publishDate,
								publisher, ratingCount, seriesID, title);
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
								logger.warn("An error was detected getting book {}'s identifiers", title);
							}
							bookX.setIdentifiers(bookIdentifiers); // add our identifiers
						}
						// 6.c get genres.
						String[] genres = helperGetBooksGenres(conn, bookID);
						bookX.setGenres(genres); // add our genres to book object
						// 7. add our fully fleshed out book object to return list
						rtVal.add(bookX);
					}
				}
				// end of try-with-resources: result set
			} // end of try-with-resources: connection
				// catch blocks for try-with-resources: connection
			catch (ClassNotFoundException e) {
				logger.error("Exception occured during connectToDB: " + e.getMessage());
			} catch (SQLException e) {
				logger.error("Exception occured during executing SQL statement: " + e.getMessage());
			}
		}

		return rtVal.toArray(new Book[rtVal.size()]);
	}

	/**
	 * A primitive implementation to allow searching of the database for books by title.
	 * @param title The title or partial title of the book to look for.
	 * @return returns an array of books where the title matches the passed in string. Array is empty if no results were found or an error occurred.
	 */
	@Override
	public Book[] getBooksByTitle(String title) {
		//modeled after getBySeries only change is: String sql, if (DaoUtils.stringIsOk(title)) {
		// pstmt.setString(1, title);
		ArrayList<Book> rtVal = new ArrayList<Book>();
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
					// 3. loop through records returned to parse our data.
					while (rs.next()) {
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
						int seriesID = rs.getInt("series_id");
						title = rs.getString("title"); 

						// 5. create our return object with the values
						Book bookX = new Book(avgRating, bookID, bookIndexInSeries, countAuthors, coverLocation,
								coverName, description, edition, has_identifiers, primaryAuthorID, publishDate,
								publisher, ratingCount, seriesID, title);
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
								logger.warn("An error was detected getting book {}'s identifiers", title);
							}
							bookX.setIdentifiers(bookIdentifiers); // add our identifiers
						}
						// 6.c get genres.
						String[] genres = helperGetBooksGenres(conn, bookID);
						bookX.setGenres(genres); // add our genres to book object
						// 7. add our fully fleshed out book object to return list
						rtVal.add(bookX);
					}
				}
				// end of try-with-resources: result set
			} // end of try-with-resources: connection
				// catch blocks for try-with-resources: connection
			catch (ClassNotFoundException e) {
				logger.error("Exception occured during connectToDB: " + e.getMessage());
			} catch (SQLException e) {
				logger.error("Exception occured during executing SQL statement: " + e.getMessage());
			}
		}

		return rtVal.toArray(new Book[rtVal.size()]);
	}

	@Override
	public boolean removeBook(long book_id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeBook(String title, Pair<String, String> authors) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addBook(String title, String cover_location, Pair<String, String>[] authors) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean editBook(BOOK_FIELD book_field, Object newVal) {
		// TODO Auto-generated method stub
		return false;
	}

	// Helpers
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
