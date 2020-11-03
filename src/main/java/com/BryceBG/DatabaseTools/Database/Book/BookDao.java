package com.BryceBG.DatabaseTools.Database.Book;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Book.Book.BOOK_FIELD;
import com.BryceBG.DatabaseTools.Database.Series.Series;

/**
 * This is the public DAO (data access object) for interfacing with the book
 * table from our database (also the related junction tables indirectly).
 * 
 * @author Bryce-BG
 *
 */
public class BookDao extends BookDaoInterface {
	private static final Logger logger = LogManager.getLogger(BookDao.class.getName());

	public ArrayList<Book> getAllBooks() {
		ArrayList<Book> rtVal = new ArrayList<Book>();
		String sql = "SELECT * FROM BOOKS";
		String sqlgetExtraAuthors = "SELECT author_id FROM book_authors WHERE book_id=?";
		String sqlGetGenres = "SELECT genre_name FROM book_genres WHERE book_id=?";
		String sqlGetIdentiers = "SELECT identifier_type, identifier_value FROM book_identifier WHERE book_id=?";

		// 1. establish connection to our database (and create our prepared statements
		try (Connection conn = DAORoot.library.connectToDB();
				PreparedStatement pstmt = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				PreparedStatement pstmtGetAuthors = conn.prepareStatement(sqlgetExtraAuthors, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				PreparedStatement pstmtGetGenre = conn.prepareStatement(sqlGetGenres, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				PreparedStatement pstmtgetIdentifiers = conn.prepareStatement(sqlGetIdentiers,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
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
							description, edition, has_identifiers, primaryAuthorID, publishDate, publisher, ratingCount, seriesID,
							title);
					// 6. fill additional fields as needed from other tables (authors (if multiple), identifiers, and genres.

					// 6.a get extra authors
					if (countAuthors == 1) { // there are no other authors so skip additional query.
						bookX.setAuthorIDs(new int[] { bookX.getPrimaryAuthorID() });
					} else {
						pstmtGetAuthors.setLong(1, bookID);
						try (ResultSet rs2 = pstmtGetAuthors.executeQuery()) {
							// 3. loop through records returned to parse our data.
							int rowcount = 0;
							if (rs2.last()) { // https://stackoverflow.com/questions/192078/how-do-i-get-the-size-of-a-java-sql-resultset
								rowcount = rs2.getRow();
								rs2.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing
													// the first element
							}

							int[] authorIDs = new int[rowcount];
							rowcount = 0; // reusing variable.
							while (rs2.next()) {
								authorIDs[rowcount] = rs2.getInt("author_id");
								rowcount++;
							}
							bookX.setAuthorIDs(authorIDs); // add authors
						} // end try rs2
					}
					// 6.b. get identifiers.
					if (bookX.getHasIdentifiers()) {
						pstmtgetIdentifiers.setLong(1, bookID);
						try (ResultSet rs2 = pstmtgetIdentifiers.executeQuery()) {
							// 3. loop through records returned to parse our data.
							int rowcount = 0;
							if (rs2.last()) {
								rowcount = rs2.getRow();
								rs2.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing
													// the first element
							}

							@SuppressWarnings("unchecked")
							Pair<String, String>[] bookIdentifiers = new Pair[rowcount];
							rowcount = 0; // reusing variable.
							while (rs2.next()) {
								bookIdentifiers[rowcount] = new Pair<String, String>(rs2.getString("identifier_type"),
										rs2.getString("identifier_value"));
								rowcount++;
							}
							bookX.setIdentifiers(bookIdentifiers); // add our identifiers
						} // end try rs2
					}

					// 6.c get genres.
					pstmtGetGenre.setLong(1, bookID);
					try (ResultSet rs2 = pstmtGetGenre.executeQuery()) {
						//loop through records returned to parse our data.
						int rowcount = 0;
						if (rs2.last()) {
							rowcount = rs2.getRow();
							rs2.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing
												// the first element
						}
						if(rowcount != 0) { //ensure we don't crash if no genres are listed for a book as you can't create an array of size 0
							String[] genres = new String[rowcount];
							rowcount = 0; // reusing variable.
							while (rs2.next()) {
								genres[rowcount] = rs2.getString("genre_name");
								rowcount++;
							}
							bookX.setGenres(genres); // add our genres
						}
						
					} // end try rs2
				
				//7. add our fully fleshed out book object to return list
				rtVal.add(bookX);
			}
		}
		// end of try-with-resources: result set
	} // end of try-with-resources: connection
		// catch blocks for try-with-resources: connection
	catch(

	ClassNotFoundException e)
	{
		logger.error("Exception occured during connectToDB: " + e.getMessage());
	}catch(
	SQLException e)
	{
		logger.error("Exception occured during executing SQL statement: " + e.getMessage());
	}

	return rtVal;

	}

	public Book getBookByIdentifier(String identifier_name, String identifier) {
//    	Pair<String, String> id_pair = Pair.with(identifier_name, identifier);
		// TODO implement me. Query DB for book with id tuple: <identifier_name, value>
		logger.info("Call to getBookByIdentifier() was made but this is a STUB");

		return null;
	}

	public Book[] getBooksByAuthor(String Author) {
		logger.info("Call to getBookByAuthor() was made but this is a STUB");

		// TODO implement me
		return null;
	}

	public Book getBookByBookID(String bookID) {
		logger.info("Call to getBookByBookID() was made but this is a STUB");

		// TODO implement me
		return null;
	}

	public Book getRandomBook() {
		// intended to be used on the front page of our website
		logger.info("Call to getRandomBook() was made but this is a STUB");

		return null;
	}

	@Override
	public Book[] getBooksBySeries(Series series) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Book[] getBooksByTitle(String title) {
		// TODO Auto-generated method stub
		return null;
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
	protected boolean helperUpdateBookAuthorsTable(Connection conn, long book_id, long authors_id) {
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

	@Override
	protected int[] helperGetBooksAuthors(Connection conn, long book_id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int[] helperGetBooksGenres(Connection conn, long book_id) {
		// TODO Auto-generated method stub
		return null;
	}

}
