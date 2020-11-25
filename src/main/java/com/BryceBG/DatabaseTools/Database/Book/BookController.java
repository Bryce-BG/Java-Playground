package com.BryceBG.DatabaseTools.Database.Book;

import static com.BryceBG.DatabaseTools.Database.DAORoot.userDao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Author.Author;
import com.BryceBG.DatabaseTools.Database.Book.BookDaoInterface.EDIT_TYPE;
import com.BryceBG.DatabaseTools.Database.User.UserController;
import com.BryceBG.DatabaseTools.utils.DaoUtils;
import com.BryceBG.DatabaseTools.utils.GlobalConstants;

/**
 * This class will handle the higher logic for manipulating book related data in
 * our database. It also handles logging modifications made to the books in the
 * database.
 * 
 * This class I implemented differently than most of the other controllers. It
 * is a VERY thin wrapper around the bookDao where most of the verification of
 * data occurs.
 * 
 * 
 * With the other controllers I shunted most of the data verification to the
 * controller to ensure quick and detailed error catching when something goes
 * wrong. This works great in theory but I find that in practice I had to do a
 * lot of the validation of inputs and error catching AGAIN at the lower level
 * to protect the database. also some of the calls where I do something like:
 * getBook() at the upper level to verify that the book we are accessing exists,
 * is performed again at the dao level to get info about the item before
 * performing modifications.
 * 
 * With my new approach here, I basically toss the inputs to the dao (which
 * either succeed or fail). Then IFF they fail I can perform extra error
 * checking at the controller level to determine what caused the error. This
 * theoretically in some cases reduces the computation for standard function
 * calls. Instead I only perform the extra computation if the call causes an
 * error (which should be less common than successful calls)
 * 
 * @author Bryce-BG
 *
 */
public class BookController {
	private static final Logger logger = LogManager.getLogger(BookController.class.getName());

	/**
	 * Adds a book to our database.
	 * 
	 * @param username    User attempting to perform the add operation (must be an
	 *                    administrator account)
	 * @param password    The password for the admin adding the book to our system
	 * @param title       The title of the new book.
	 * @param description The description of the new book (can be left empty or null
	 *                    if it doesn't have one).
	 * @param edition     the edition of the book. If unknown leave as a negative
	 *                    number
	 * @param authorNames Pairs of the authors <first name, last name> who
	 *                    authored/co-authored the book.
	 * @return <True, Global.SuccessMsg> if book was successfully added to the
	 *         database or <False, reason for failure> if book addition failed.
	 */
	public static Pair<Boolean, String> addBook(String username, String password, String title, String description,
			int edition, Pair<String, String>[] authorNames) {

		// 1. authenticate user and authenticate.
		if (UserController.authenticate(username, password) == false) {
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER);
		}

		// 1.b. ensure they have permissions to perform operation
		if (userDao.getUserByUsername(username).isAdmin() == false)
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER_PERMISSIONS);

		// 1.c ensure at least 1 author for the series was passed in
		if (authorNames == null || authorNames.length == 0) {
			return new Pair<Boolean, String>(Boolean.FALSE,
					"No authors were included for the new Book (required field).");
		}

		// 1.d. ensure authors that were passed in exist in our database.
		ArrayList<Integer> authorIDsArrayList = new ArrayList<Integer>();
		for (Pair<String, String> x : authorNames) {
			if (x == null) {
				return new Pair<Boolean, String>(Boolean.FALSE, "A null author was passed in");
			}
			Author authorX = DAORoot.authorDao.getAuthor(x.getValue0(), x.getValue1());
			if (authorX == null) { // author doesn't exist
				return new Pair<Boolean, String>(Boolean.FALSE, String.format(
						"Author - First Name: %s Last Name: %s - is not a valid author. Either add the author to the database or correct the spelling of the author",
						x.getValue0(), x.getValue1()));
			} else {
				authorIDsArrayList.add(authorX.getAuthorID());
			}
		}

		// 1.e. validate title
		if (DaoUtils.stringIsOk(title) == false)
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid title for new book");
		title = title.strip();

		// 2. ensure book doesn't already exist
		int[] authorIDs = new int[authorIDsArrayList.size()];
		for (int i = 0; i < authorIDs.length; i++)
			authorIDs[i] = authorIDsArrayList.get(i).intValue();
		int primary_author_id = DaoUtils.findPrimaryAuthor(authorIDs);

		Book[] tempBooks = DAORoot.bookDao.getBooksByAuthor(primary_author_id);
		for (Book bookX : tempBooks) {

			// make sure no book was returned with same (title, edition, publisher,
			// primary_author_id) combo (which according to DB scheme must be unique)
			if (bookX.getTitle().equalsIgnoreCase(title) && bookX.getEdition() == edition
					&& "".equals(bookX.getPublisher())) {
				return new Pair<Boolean, String>(Boolean.FALSE, "Book already exists in database.");
			}
		}

		// 3. call dao to insert book into database()
		boolean rtnedVal = DAORoot.bookDao.insertBookIntoDB(authorIDs, description, edition, title);

		// 5. check returned value to determine if book addition was successful
		if (rtnedVal)
			return new Pair<Boolean, String>(Boolean.TRUE, GlobalConstants.MSG_SUCCESS);
		else
			return new Pair<Boolean, String>(Boolean.FALSE, "Book addition unexpectedly failed. Please try again.");

	}

	/**
	 * A function to remove a book from the database
	 * 
	 * @param username User attempting to perform the add operation (must be an
	 *                 administrator account)
	 * @param password The password for the admin adding the book to our system
	 * @param bookID   ID of the book we are removing from the database.
	 * @return <True, GlobalConstants.MSG_SUCCESS> if operation was successful.
	 *         Otherwise returns <False, reason_for_failure>
	 */
	public static Pair<Boolean, String> removeBook(String username, String password, long bookID) {
		// 1. authenticate user and authenticate.
		if (UserController.authenticate(username, password) == false) {
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER);
		}
		// 1.b. ensure they have permissions to perform operation
		if (userDao.getUserByUsername(username).isAdmin() == false)
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER_PERMISSIONS);

		// 2. attempt to remove book.
		boolean res = DAORoot.bookDao.removeBook(bookID);

		// 3. determine success or failure of operation
		if (res)
			return new Pair<Boolean, String>(Boolean.TRUE, GlobalConstants.MSG_SUCCESS);
		else {
			// 3.b rather than just return failure, check if we can determine reason for
			// failure (through log or simple checks)
			// failure because of invalid book_id
			if (DAORoot.bookDao.getBookByBookID(bookID) == null) {
				// book id was invalid by checking only IF we fail we reduce runtime for
				// standard cases
				return new Pair<Boolean, String>(Boolean.FALSE, String.format("book_id: %d. Does not exist", bookID));
			}
			// return generic exception
			return new Pair<Boolean, String>(Boolean.FALSE, "Removing book unexpectedly failed.");

		}
	}

	public static final int ADD_AUTHOR = 1;
	public static final int REMOVE_AUTHOR = 2;
	public static final int SET_AVG_RATING = 3;
	public static final int SET_BOOK_INDEX_IN_SERIES = 4;
	public static final int SET_COVER_LOCATION = 5;
	public static final int SET_COVER_NAME = 6;
	public static final int SET_DESCRIPTION = 7;
	public static final int SET_EDITION = 8;
	public static final int SET_GENRES = 9;
	public static final int SET_IDENTIFIERS = 10;
	public static final int SET_PUBLISH_DATE = 11;
	public static final int SET_PUBLISHER = 12;
	public static final int SET_RATING_COUNT = 13;
	public static final int SET_SERIES_ID = 14;

	private static HashMap<Integer, EDIT_TYPE> hashIntToEditType = new HashMap<Integer, EDIT_TYPE>();
	static {
		hashIntToEditType.put(1, EDIT_TYPE.ADD_AUTHOR);
		hashIntToEditType.put(2, EDIT_TYPE.REMOVE_AUTHOR);
		hashIntToEditType.put(3, EDIT_TYPE.SET_AVG_RATING);
		hashIntToEditType.put(4, EDIT_TYPE.SET_BOOK_INDEX_IN_SERIES);
		hashIntToEditType.put(5, EDIT_TYPE.SET_COVER_LOCATION);
		hashIntToEditType.put(6, EDIT_TYPE.SET_COVER_NAME);
		hashIntToEditType.put(7, EDIT_TYPE.SET_DESCRIPTION);
		hashIntToEditType.put(8, EDIT_TYPE.SET_EDITION);
		hashIntToEditType.put(9, EDIT_TYPE.SET_GENRES);
		hashIntToEditType.put(10, EDIT_TYPE.SET_IDENTIFIERS);
		hashIntToEditType.put(11, EDIT_TYPE.SET_PUBLISH_DATE);
		hashIntToEditType.put(12, EDIT_TYPE.SET_PUBLISHER);
		hashIntToEditType.put(13, EDIT_TYPE.SET_RATING_COUNT);
		hashIntToEditType.put(14, EDIT_TYPE.SET_SERIES_ID);

	}

	/**
	 * A function to set and modify the fields on the book.
	 * 
	 * @param <T>      This varies depending on the field being set. Below are the
	 *                 expected types depending on what is being set ADD_AUTHOR,
	 *                 Integer, REMOVE_AUTHOR, Integer, SET_AVG_RATING, Float,
	 *                 SET_BOOK_INDEX_IN_SERIES, Float, SET_COVER_LOCATION, String,
	 *                 SET_COVER_NAME, String, SET_DESCRIPTION, String, SET_EDITION,
	 *                 Integer, SET_GENRES, String[], SET_IDENTIFIERS, Pair[],
	 *                 SET_PUBLISH_DATE, java.sql.Timestamp, SET_PUBLISHER, String,
	 *                 SET_RATING_COUNT, Integer, SET_SERIES_ID, Integer
	 * @param username User attempting to perform the add operation (must be an
	 *                 administrator account)
	 * @param password The password for the admin adding the book to our system
	 * @param bookID   ID of the book we are removing from the database.
	 * @param editType for the editType we Recommend the use of the static class
	 *                 variables defined in BookController to determine the correct
	 *                 field of the book to set.
	 * @param newVal   The value we are setting the field in the book database to.
	 *                 (can't be null)
	 * @return <True, GlobalConstants.MSG_SUCCESS> if operation was successful.
	 *         Otherwise returns <False, reason_for_failure>
	 */
	@SuppressWarnings("unchecked")
	public static <T> Pair<Boolean, String> editBook(String username, String password, long bookID, int editType,
			T newVal) {
		// Generic error message to return.
		String genericErrorString = "Editing book %s unexpectedly failed. Check parameters and if issues persist contact system administrator";

		// 0. validate user performing operation
		if (UserController.authenticate(username, password) == false) {
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER);
		}
		// 0.b. ensure they have permissions to perform operation
		if (userDao.getUserByUsername(username).isAdmin() == false)
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER_PERMISSIONS);

		EDIT_TYPE editOp = hashIntToEditType.get(editType);
		// 1. call dao function to see if edit works.
		boolean rtVal = DAORoot.bookDao.editBook(bookID, editOp, newVal);

		// 2. check if edit was successful
		if (rtVal == true)
			return new Pair<Boolean, String>(Boolean.TRUE, GlobalConstants.MSG_SUCCESS);
		else {
			// 3. edit was NOT successful. Determine reason for failure.
			Book bookX = DAORoot.bookDao.getBookByBookID(bookID);

			// Error 1: if they didn't use one of our predefined int values that map to an
			// edit function
			if (editOp == null)
				return new Pair<Boolean, String>(Boolean.FALSE, "Invalid edit operation to perform");

			// Error 2: null newVal
			else if (newVal == null)
				return new Pair<Boolean, String>(Boolean.FALSE, "Value provided to edit book was null");

			// Error 3: Invalid data type for newVal
			else if (editOp.checkFitsRequiredType(newVal) == false)
				return new Pair<Boolean, String>(Boolean.FALSE,
						String.format(
								"Invalid variable type for this class of edit operation. Expected: %s, Instead got: %s",
								editOp.getRequiredType(), newVal.getClass().getTypeName()));

			// Error 4: invalid bookID
			else if (bookX == null)
				return new Pair<Boolean, String>(Boolean.FALSE,
						String.format("Book to edit with id: %d, does not exist", bookID));

			// Error 5: invalid value (costly)
			if (editOp.equals(EDIT_TYPE.ADD_AUTHOR)) {
				// step 1: ensure author provided exists
				if (DAORoot.authorDao.getAuthor((int) newVal) == null)
					return new Pair<Boolean, String>(Boolean.FALSE,
							String.format("Author id: %d does not exist in the database", ((int) newVal)));
				// Step 2: make sure author is NOT listed for book otherwise we can't add them
				else if (ArrayUtils.contains(bookX.getAuthorIDs(), ((int) newVal))) {
					return new Pair<Boolean, String>(Boolean.FALSE,
							"Can't add selected author to book as they are already listed as an author for it.");
				} else // unknown reason for crash. set return String for generic error
					genericErrorString = String.format(genericErrorString, "(adding author)");
			} else if (editOp.equals(EDIT_TYPE.REMOVE_AUTHOR)) {
				// step 1: ensure author provided exists
				if (DAORoot.authorDao.getAuthor((int) newVal) == null)
					return new Pair<Boolean, String>(Boolean.FALSE,
							String.format("Author id: %d does not exist in the database", ((int) newVal)));
				// Step 2: check to make sure author actually is listed for the book
				else if (ArrayUtils.contains(bookX.getAuthorIDs(), (Integer) newVal) == false)
					return new Pair<Boolean, String>(Boolean.FALSE,
							"Can't remove selected author as they are currently not listed as an author for the book.");
				// Step 3: check if only 1 author is listed for book
				else if (bookX.getCountAuthors() == 1)
					return new Pair<Boolean, String>(Boolean.FALSE,
							"Can't remove selected author as they are currently the only author listed for the book.");
				else // unknown reason for crash. set return String for generic error
					genericErrorString = String.format(genericErrorString, "(removing author)");
			} else if (editOp.equals(EDIT_TYPE.SET_AVG_RATING)) {
				Float newRating = ((Float) newVal);
				if (newRating.floatValue() < 0 || newRating.floatValue() > 10)
					return new Pair<Boolean, String>(Boolean.FALSE,
							"New rating value was outside the acceptable range. (new value should be between 0-10)");
				else // unknown reason for crash. set return String for generic error
					genericErrorString = String.format(genericErrorString, "(setting book rating)");
			} else if (editOp.equals(EDIT_TYPE.SET_GENRES)) {
				ArrayList<String> genres = DAORoot.genreDao.getAllGenreNames();
				ArrayList<String> invalidGenres = new ArrayList<String>();

				// check to ensure genres provided were valid
				for (String genreProvided : (String[]) newVal) {
					if (genres.contains(genreProvided) == false)
						invalidGenres.add(genreProvided);
				}
				if (invalidGenres.isEmpty() == false) {
					return new Pair<Boolean, String>(Boolean.FALSE,
							String.format("Some genres provided are invalid: %s", String.join(", ", invalidGenres)));
				} else // unknown reason for crash. set return String for generic error
					genericErrorString = String.format(genericErrorString, "(setting book genres)");
			} else if (editOp.equals(EDIT_TYPE.SET_IDENTIFIERS)) {

				// check to ensure identifiers provided were valid
				ArrayList<String> invalidIdentifiers = new ArrayList<String>(); // a list of the invalid identifiers
				for (Pair<String, String> identifierProvided : (Pair<String, String>[]) newVal) {
					// 1. check if null in list of identifiers?
					if (identifierProvided == null)
						invalidIdentifiers.add(null);
					// 2. check if partially null identifier in list?
					else if (identifierProvided.getValue0() == null | identifierProvided.getValue1() == null)
						invalidIdentifiers.add(
								"(" + identifierProvided.getValue0() + " : " + identifierProvided.getValue1() + ")");
				}
				if (invalidIdentifiers.isEmpty() == false) {
					return new Pair<Boolean, String>(Boolean.FALSE, String.format(
							"Some identifiers provided are invalid: %s", String.join(", ", invalidIdentifiers)));
				} else // unknown reason for crash. set return String for generic error
					genericErrorString = String.format(genericErrorString, "(setting book identifiers)");
			} else if (editOp.equals(EDIT_TYPE.SET_RATING_COUNT)) {
				if ((Integer) newVal < 0)
					return new Pair<Boolean, String>(Boolean.FALSE, String.format(
							"Invalid value for rating count %d. Value must be greater than 0", (Integer) newVal));
			} else if (editOp.equals(EDIT_TYPE.SET_SERIES_ID)) {
				if (DAORoot.seriesDao.getSeriesBySeriesID(((Integer) newVal).intValue()) == null) {
					// series doesn't exist
					return new Pair<Boolean, String>(Boolean.FALSE,
							String.format("Can't set series as no series with id: %d, exists.", (Integer) newVal));
				}
			}
		} // end of else (determine error cause)

		// Error 6: generic error
		// if genericError string contains %s (still is a format string so indicate what
		// we failed setting):
		if (genericErrorString.contains("%s"))
			genericErrorString = String.format(genericErrorString, editOp.toString());

		// log parameters input into function for future debugging
		logger.error("editBook failed for unknown reason with parameters (bookID: {}, editType: {}, newVal: {})",
				bookID, editType, newVal);
		return new Pair<Boolean, String>(Boolean.FALSE, genericErrorString);

	}

	public final static int SEARCH_BY_IDENTIFIER = 1;
	public final static int SEARCH_BY_AUTHOR = 2;
	public final static int SEARCH_BY_SERIES = 3;
	public final static int SEARCH_BY_TITLE = 4;

	public static Book getRandomBook() {
		return DAORoot.bookDao.getRandomBook();
	}
	
	/**
	 * A function to search for a book in the database.
	 * 
	 * @param <T>        If performing author_search: T is expected to be String
	 *                   (author_full_name), String[2] {fname, lname}, Integer
	 *                   (authorID) <br>
	 *                   If performing identifier_search: T is expected to be a
	 *                   Pair<String,String> (identifier_type, identifier_value)<br>
	 *                   If performing series search: T is expected to be an Integer
	 *                   <br>
	 *                   If performing series search: T is expected to be a String
	 * 
	 * @param searchType This can be 1: SEARCH_BY_IDENTIFIER, 2: SEARCH_BY_AUTHOR,
	 *                   3: SEARCH_BY_SERIES, 4: SEARCH_BY_TITLE
	 * @param searchVal  The value to search.
	 * @return if the first value in the pair is null, it indicates an error
	 *         occurred and the second value of the pair should be looked at to
	 *         determine the reason for failure. Otherwise, the first pair element
	 *         is an array of books returned by the search. The array will be
	 *         length==1 with only a null element if there are no results returned
	 *         (but also no errors Occurred)
	 */
	public static <T> Pair<Book[], String> searchBook(int searchType, T searchVal) {
		Pair<Book[], String> rtVal = null;
		if (searchVal == null) {
			return new Pair<Book[], String>(null, "Search field cannot be null");
		}
		// 1. determine what type of book search to perform.
		if (searchType == SEARCH_BY_IDENTIFIER) {
			rtVal = searchByIdentifier(searchVal);
		} else if (searchType == SEARCH_BY_AUTHOR) {
			rtVal = searchByAuthor(searchVal);
		} else if (searchType == SEARCH_BY_SERIES) {
			rtVal = searchBySeries(searchVal);
		} else if (searchType == SEARCH_BY_TITLE) {
			rtVal = searchByTitle(searchVal);
		}
		else { // program error (didn't try to perform a search of known type
			return new Pair<Book[], String>(null, "Invalid search type!");
		}
		return rtVal;

	}

	private static <T> Pair<Book[], String> searchByTitle(T searchVal) {
		Book[] rtBooks = null;
		String rtMsg = null;
		if (searchVal instanceof String) {// passed in as a full name
			// 1. ensure string is valid
			if (DaoUtils.stringIsOk((String) searchVal)) {
				// 3. perform query for books by title
				rtBooks = DAORoot.bookDao.getBooksByTitle((String) searchVal);
			} else {
				rtMsg = String.format("Invalid search term: '%s' for search.", (String) searchVal);
			}
		} else {
			rtMsg = "Data type/value for query does not conform to expected format.";
		}
		return new Pair<Book[], String>(rtBooks, rtMsg);
	}

	/**
	 * Helper function that enables searching by series
	 * 
	 * @param <T>       Expected to be an integer.
	 * @param searchVal the id of the series we are searching for.
	 * @return the book results or an error message indicate the cause of failure
	 */
	private static <T> Pair<Book[], String> searchBySeries(T searchVal) {
		Book[] rtBooks = null;
		String rtMsg = null;
		if (searchVal instanceof Integer) {
			if (DAORoot.seriesDao.getSeriesBySeriesID((int) searchVal) != null) {
				rtBooks = DAORoot.bookDao.getBooksBySeries((int) searchVal);
			} else {
				rtMsg = String.format("Search returned no results. (i.e. no series with ID: %d exists)",
						(int) searchVal);
			}
		} else { // wrong type of data format
			rtMsg = "Data type/value for query does not conform to expected format.";
		}
		return new Pair<Book[], String>(rtBooks, rtMsg);
	}

	private static <T> Pair<Book[], String> searchByIdentifier(T searchVal) {
		Book[] rtBooks = null;
		String rtMsg = null;

		// 1. check type of searchVal to ensure the correct data class.
		// ensure the data is class pair. and ensure both subfields are of class string
		if (searchVal instanceof Pair && ((Pair<?, ?>) searchVal).getValue0() instanceof String
				&& ((Pair<?, ?>) searchVal).getValue1() instanceof String) {
			@SuppressWarnings("unchecked")
			String identifierName = ((Pair<String, String>) searchVal).getValue0();
			@SuppressWarnings("unchecked")
			String identifierValue = ((Pair<String, String>) searchVal).getValue1();
			// 3. ensure both search fields are valid.
			if (DaoUtils.stringIsOk(identifierName) && DaoUtils.stringIsOk(identifierValue)) {
				// 4. call dao and perform search.
				Book result = DAORoot.bookDao.getBookByIdentifier(identifierName, identifierValue);
				// 5. set return value
				rtBooks = new Book[] { result };
			} else {
				// 4.b. search terms are valid so set rtMsg to indicate cause of failure
				rtMsg = String.format("Search terms ('%s' : '%s') is invalid. The terms can't be empty.",
						identifierName, identifierValue);
			}
		} else { // wrong type of data format (or null values)
			rtMsg = "Data type/value for query does not conform to expected format.";
		}
		return new Pair<Book[], String>(rtBooks, rtMsg);
	}

	/**
	 * Helper function to searchBook()
	 * 
	 * @param <T>       Expected to be a String[2], Pair<String,String>, String,
	 *                  Integer
	 * @param searchVal the author we are searching for
	 * @return A tuple where the first value is the search results of books that are
	 *         in the database written by the author provided. or Null and a string
	 *         explaining the error.
	 */
	private static <T> Pair<Book[], String> searchByAuthor(T searchVal) {
		Book[] rtBooks = null;
		String rtMsg = null;

		// 2. check to make sure type of newVal is correct.
		if (searchVal instanceof String[]) {
			String[] sVal = ((String[]) searchVal);
			if (sVal.length != 2 || ArrayUtils.contains(sVal, null)) { // array did not have expected number of elements
																		// or contained null values
				rtMsg = String.format("Expected two fields: (first name, last name) instead got: %d (%s)", sVal.length,
						String.join(", ", sVal));
				return new Pair<Book[], String>(rtBooks, rtMsg);
			} else if (!DaoUtils.stringIsOk(sVal[0]) || !DaoUtils.stringIsOk(sVal[1])) {
				// ensure valid non-whitespace only search terms
				rtMsg = String.format("Expected two fields: (first name, last name) instead got: %d ('%s', '%s')",
						sVal.length, sVal[0], sVal[1]);
				return new Pair<Book[], String>(rtBooks, rtMsg);
			} else {
				String fname = sVal[0];
				String lname = sVal[1];
				Author a = DAORoot.authorDao.getAuthor(fname, lname);// get author details and then query by authorID
				if (a != null) {
					rtBooks = DAORoot.bookDao.getBooksByAuthor(a.getAuthorID());
				} else {
					rtMsg = String.format(
							"No results were returned for Author (i.e. author '%s' was not found in the database).",
							String.join(" ", sVal));
				}
			}
		} else if (searchVal instanceof Integer) {
			Author a = DAORoot.authorDao.getAuthor((int) searchVal);
			if (a != null) {
				// 3. perform query for books by author
				rtBooks = DAORoot.bookDao.getBooksByAuthor(a.getAuthorID());
			} else {
				rtMsg = String.format(
						"No results were returned for Author (i.e. author with ID: '%d' was not found in the database).",
						(int) searchVal);
			}
		} else if (searchVal instanceof String) {// passed in as a full name
			// 1. ensure string is valid
			if (DaoUtils.stringIsOk((String) searchVal)) {
				// 2. call getAuthor to get authorID for our query.
				Author a = DAORoot.authorDao.getAuthor((String) searchVal);// get author details and then query by
																			// authorID
				if (a != null) {
					// 3. perform query for books by author
					rtBooks = DAORoot.bookDao.getBooksByAuthor(a.getAuthorID());
				} else {
					rtMsg = String.format(
							"No results were returned for Author (i.e. author '%s' was not found in the database).",
							(String) searchVal);
				}
			} else
				rtMsg = String.format("Invalid author '%s' for search.", (String) searchVal);
		} else {
			rtMsg = "Data type/value for query does not conform to expected format.";
		}
		return new Pair<Book[], String>(rtBooks, rtMsg);
	}

}
