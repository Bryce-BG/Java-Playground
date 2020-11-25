package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang.ArrayUtils;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Author.Author;
import com.BryceBG.DatabaseTools.Database.Book.Book;
import com.BryceBG.DatabaseTools.Database.Book.BookController;
import com.BryceBG.DatabaseTools.Database.Series.Series;
import com.BryceBG.DatabaseTools.utils.GlobalConstants;

import testUtils.UtilsForTests;

public class testBookController {

	final String username = "admin";
	final String password = "Password1";

	// User in our database (capitalization is wrong but it SHOULD be handled by our
	// functions)
	Pair<String, String> authorName = new Pair<String, String>("james", "joyce");

	// global timeout to ensure no issues
	@Rule
	public Timeout globalTimeout = Timeout.seconds(20);

	@BeforeClass
	public static void runOnce() {
		UtilsForTests.setupForTests();
	}

	@Before
	public void runBeforeTest() {
		UtilsForTests.resetDB(false); // reset database to initial state
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAddBook() {
		String title = "Hello World";
		String description = "";
		int edition = 2;
		Pair<String, String>[] authorNames = new Pair[1];
		authorNames[0] = authorName;

		/* generic authorization and input tests */

		// Test 1: test null username (expectation: fail)
		Pair<Boolean, String> res = BookController.addBook(null, password, title, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());

		// Test 2: test empty username (expectation: fail)
		res = BookController.addBook("", password, title, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());

		// Test 3: invalid user/password combo (i.e. the wrong password) (expectation:
		// fail)
		res = BookController.addBook(username, "wrong password", title, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());

		// Test 4: valid user but NOT an admin (expectation: fail)
		res = BookController.addBook("JamesJoyce", password, title, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER_PERMISSIONS, res.getValue1());

		/* end generic tests */
		// Test 4: test null title (expectation: fail)
		res = BookController.addBook(username, password, null, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Invalid title for new book", res.getValue1());

		// Test 5: test empty title (expectation: fail)
		res = BookController.addBook(username, password, "", description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Invalid title for new book", res.getValue1());

		// Test 6: test null description (expectation: success)
		res = BookController.addBook(username, password, title, null, edition, authorNames);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
		title = title + "1"; // update title we use so we won't get failures due to existing title in db

		// Test 7: test empty description (expectation: success)
		res = BookController.addBook(username, password, title, " ", edition, authorNames);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
		title = title + "2"; // update title we use so we won't get failures due to existing title in db

		// Test 8: -edition (expectation: success (set to -1)
		res = BookController.addBook(username, password, title, description, -3, authorNames);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
		title = title + "3"; // update title we use so we won't get failures due to existing title in db

		// Test 9: null authorNames (expectation: fail)
		res = BookController.addBook(username, password, title, description, edition, null);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("No authors were included for the new Book (required field).", res.getValue1());

		// Test 10: authorNames array of length 0 is passed in (expectation: fail)
		Pair<String, String>[] authorNamesx = new Pair[0];
		res = BookController.addBook(username, password, title, description, edition, authorNamesx);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("No authors were included for the new Book (required field).", res.getValue1());

		// Test 11: partially null entry in authorNames (expectation: fail)

		Pair<String, String>[] authorNames2 = authorNames;
		authorNames2[0] = authorNames2[0].setAt1(null);
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(
				"Author - First Name: james Last Name: null - is not a valid author. Either add the author to the database or correct the spelling of the author",
				res.getValue1());

		// Test 12: totally null author in the list
		authorNames2 = authorNames;
		authorNames2[0] = null;
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("A null author was passed in", res.getValue1());

		// Test 13: author in list is not in database (expectation: fail)
		Pair<String, String> authorNameWrong = new Pair<String, String>("Idont", "Exist");
		authorNames2[0] = authorNameWrong;
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(
				"Author - First Name: Idont Last Name: Exist - is not a valid author. Either add the author to the database or correct the spelling of the author",
				res.getValue1());

		// Test 14: test adding book with multiple authors (expectation: success)
		authorNames2 = new Pair[2];
		Pair<String, String> authorName2nd = new Pair<String, String>("Test", "Author2");
		authorNames2[0] = authorName;
		authorNames2[1] = authorName2nd;
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());

		// Test 16: existing book in database (expectation: fail)
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Book already exists in database.", res.getValue1());

		// Test 17: existing book in database (expectation: fail)
		authorNames = new Pair[1];
		authorNames[0] = authorName;
		res = BookController.addBook(username, password, "TestBook", description, edition, authorNames);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
	}

	// DEPENDENCIES: getAllBooks();
	@Test
	public void testRemoveBook() {
		/* generic authorization and input tests */
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long book_id = booksBefore.get(0).getBookID();

		// Test 1: test null username (expectation: fail)
		Pair<Boolean, String> res = BookController.removeBook(null, password, book_id);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());

		// Test 2: test empty username (expectation: fail)
		res = BookController.removeBook("", password, book_id);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());

		// Test 3: invalid user/password combo (i.e. the wrong password) (expectation:
		// fail)
		res = BookController.removeBook(username, "wrong password", book_id);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());

		// Test 4: valid user but NOT an admin (expectation: fail)
		res = BookController.removeBook("JamesJoyce", password, book_id);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER_PERMISSIONS, res.getValue1());

		/* end generic tests */
		// Test 5: not a book id in the system
		res = BookController.removeBook(username, password, -1);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(String.format("book_id: %d. Does not exist", -1), res.getValue1());

		// Test 6: successful user/password combo and book_id to remove
		res = BookController.removeBook(username, password, book_id);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
	}

	// DEPENDENCIES: getAllBooks()
	@Test
	public void testEditBook_basic_tests() {
		// This function test parts of the editBook() function that are the same no
		// matter what type of edit they choose to do.

		/* generic authorization and input tests */
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long book_id = booksBefore.get(0).getBookID();

		// Test 1: test null username (expectation: fail)
		String validCoverName = "hello World";
		Pair<Boolean, String> res = BookController.editBook(null, password, book_id, BookController.SET_COVER_NAME,
				validCoverName);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());

		// Test 2: test empty username (expectation: fail)
		res = BookController.editBook("", password, book_id, BookController.SET_COVER_NAME, validCoverName);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());

		// Test 3: invalid user/password combo (i.e. the wrong password) (expectation:
		// fail)
		res = BookController.editBook(username, "wrong password", book_id, BookController.SET_COVER_NAME,
				validCoverName);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());

		// Test 4: valid user but NOT an admin (expectation: fail)
		res = BookController.editBook("JamesJoyce", password, book_id, BookController.SET_COVER_NAME, validCoverName);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER_PERMISSIONS, res.getValue1());

		// Test 5: attempting to operate on a book that doesn't exist (expectation:
		// fail)
		res = BookController.editBook(username, password, -1, BookController.SET_COVER_NAME, validCoverName);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Book to edit with id: -1, does not exist", res.getValue1());

		// Test 6: attempt to pass null in for the new value (expectation: fail)
		res = BookController.editBook(username, password, book_id, BookController.SET_COVER_NAME, null);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		// TODO may need to change dao for this to and allow null in some cases if
		// allowed
		assertEquals("Value provided to edit book was null", res.getValue1());

		// Test 7: invalid type of edit operation to perform
		res = BookController.editBook(username, password, book_id, -9, validCoverName);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Invalid edit operation to perform", res.getValue1()); // TODO need to change dao for this OR get
																			// more generic error catching

		/* end generic tests */
	}

	// DEPENDENCIES: getAllBooks() getAllAuthors()
	@Test
	public void testEditBook_ADD_AUTHOR() {
		ArrayList<Author> authors = DAORoot.authorDao.getAllAuthors();
		int[] possibleAuthorIDs = new int[authors.size()];
		int storeInd = 0;
		for (Author authX : authors) {
			possibleAuthorIDs[storeInd] = authX.getAuthorID();
			storeInd++;
		}
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long bookID = booksBefore.get(0).getBookID();

		int editType = BookController.ADD_AUTHOR;
		// Test 1: not the valid type of data for add author (newVal should be an
		// Integer or int)
		Pair<Boolean, String> res = BookController.editBook(username, password, bookID, editType, (long) 3);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(
				"Invalid variable type for this class of edit operation. Expected: java.lang.Integer, Instead got: java.lang.Long",
				res.getValue1());

		// Test 2: add author NOT in our database.
		int newVal = -3;
		res = BookController.editBook(username, password, bookID, editType, newVal);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Author id: -3 does not exist in the database", res.getValue1());

		// Test 3: add author already listed for book
		int alreadyListedAuthID = booksBefore.get(0).getPrimaryAuthorID();
		res = BookController.editBook(username, password, bookID, editType, alreadyListedAuthID);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Can't add selected author to book as they are already listed as an author for it.",
				res.getValue1());

		// Test 4: valid author add to the book.
		storeInd = 0;
		// find and author that isn't listed for the book
		while (ArrayUtils.contains(booksBefore.get(0).getAuthorIDs(), possibleAuthorIDs[storeInd]))
			storeInd++;
		res = BookController.editBook(username, password, bookID, editType, possibleAuthorIDs[storeInd]);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());

	}

	// DEPENDENCIES: getAllBooks() getAllAuthors()
	@Test
	public void testEditBook_REMOVE_AUTHOR() {
		ArrayList<Author> authors = DAORoot.authorDao.getAllAuthors();
		int[] possibleAuthorIDs = new int[authors.size()];
		int storeInd = 0;
		for (Author authX : authors) {
			possibleAuthorIDs[storeInd] = authX.getAuthorID();
			storeInd++;
		}
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long bookID = booksBefore.get(0).getBookID();

		int editType = BookController.REMOVE_AUTHOR;
		// Test 1: not the valid type of data for add author (newVal should be an
		// Integer or int)
		Pair<Boolean, String> res = BookController.editBook(username, password, bookID, editType, (long) 3);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(
				"Invalid variable type for this class of edit operation. Expected: java.lang.Integer, Instead got: java.lang.Long",
				res.getValue1());

		// Test 2: remove author NOT in our database.
		int newVal = -3;
		res = BookController.editBook(username, password, bookID, editType, newVal);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Author id: -3 does not exist in the database", res.getValue1());

		// Test 3: remove author NOT listed for book
		storeInd = 0;
		// find and author that isn't listed for the book
		while (ArrayUtils.contains(booksBefore.get(0).getAuthorIDs(), possibleAuthorIDs[storeInd]))
			storeInd++;
		int notListedAuthID = possibleAuthorIDs[storeInd];
		res = BookController.editBook(username, password, bookID, editType, notListedAuthID);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Can't remove selected author as they are currently not listed as an author for the book.",
				res.getValue1());

		// Test 4: try to remove valid author from the book but only 1 author
		// (expectation: fail).
		res = BookController.editBook(username, password, bookID, editType, booksBefore.get(0).getPrimaryAuthorID());
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Can't remove selected author as they are currently the only author listed for the book.",
				res.getValue1());

		// Test 6: remove author from book with 2 authors
		boolean foundBookCouldRemoveFrom = false;
		for (Book b : booksBefore) {
			if (b.getCountAuthors() > 1) {
				res = BookController.editBook(username, password, bookID, editType, b.getPrimaryAuthorID());
				assertFalse(res.getValue1(), res.getValue0().booleanValue());
				assertEquals("Can't remove selected author as they are currently the only author listed for the book.",
						res.getValue1());
				foundBookCouldRemoveFrom = true;
				break;
			}
		}
		assertTrue(foundBookCouldRemoveFrom); // ensure we actually found a book and removed the author
	}

	@Test
	public void testEditBook_SET_AVG_RATING() {
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long bookID = booksBefore.get(0).getBookID();
		int editType = BookController.SET_AVG_RATING;

		// Test 1: not the valid type of data for set rating(newVal should be a float
		Pair<Boolean, String> res = BookController.editBook(username, password, bookID, editType, "1.0");
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(
				"Invalid variable type for this class of edit operation. Expected: java.lang.Float, Instead got: java.lang.String",
				res.getValue1());

		// Test 2: invalid value below acceptable range
		res = BookController.editBook(username, password, bookID, editType, -1f);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("New rating value was outside the acceptable range. (new value should be between 0-10)",
				res.getValue1());

		// Test 2: invalid value above acceptable range
		res = BookController.editBook(username, password, bookID, editType, 11f);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("New rating value was outside the acceptable range. (new value should be between 0-10)",
				res.getValue1());

	}

	// DEPENDENCIES: getAllBooks()
	@Test
	public void testEditBook_SET_GENRES() {
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long bookID = booksBefore.get(0).getBookID();
		int editType = BookController.SET_GENRES;

		// Test 1: not the valid type of data for set rating(newVal should be a String[]
		Pair<Boolean, String> res = BookController.editBook(username, password, bookID, editType, "1.0");
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(
				"Invalid variable type for this class of edit operation. Expected: java.lang.String[], Instead got: java.lang.String",
				res.getValue1());

		// Test 2: invalid genre list (genres not in database and null)
		String[] genresP = new String[] { "hello", null, "TestGenre1" };
		res = BookController.editBook(username, password, bookID, editType, genresP);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Some genres provided are invalid: hello, null", res.getValue1());

		// Test 3: valid genre list
		genresP = new String[] { "TestGenre2", "TestGenre1" };

		res = BookController.editBook(username, password, bookID, editType, genresP);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
	}

	// DEPENDENCIES: getAllBooks(), getBookByBookID
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testEditBook_SET_IDENTIFIERS() {
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long bookID = booksBefore.get(0).getBookID();
		int editType = BookController.SET_IDENTIFIERS;

		// Test 1: not the valid type of data for SET_IDENTIFIERS(newVal should be a
		// Pair<String,String>[]
		Pair<Boolean, String> res = BookController.editBook(username, password, bookID, editType, "1.0");
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(
				"Invalid variable type for this class of edit operation. Expected: org.javatuples.Pair[], Instead got: java.lang.String",
				res.getValue1());

		// Test 2: invalid identifier list (null identifier)
		Pair<String, String>[] identifiersP = new Pair[] { new Pair("isbn", "0393939"), null };
		res = BookController.editBook(username, password, bookID, editType, identifiersP);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Some identifiers provided are invalid: null", res.getValue1());

		// Test 3: invalid identifier list (partially null identifier)
		identifiersP = new Pair[] { new Pair("isbn", "0393939"), new Pair("isbn", null), new Pair(null, "0393939") };
		res = BookController.editBook(username, password, bookID, editType, identifiersP);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Some identifiers provided are invalid: (isbn : null), (null : 0393939)", res.getValue1());

		// Test 4: valid identifier list (with duplicate)
		identifiersP = new Pair[] { new Pair("isbn", "0393939"), new Pair("isbn13", "039393903033"),
				new Pair("isbn", "0393939") };
		res = BookController.editBook(username, password, bookID, editType, identifiersP);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
		Book bookX = DAORoot.bookDao.getBookByBookID(bookID);
		assertEquals(2, bookX.getIdentifiers().length); // ensure redundant identifier was removed
	}

	// DEPENDENCIES: getAllBooks()
	@Test
	public void testEditBook_SET_RATING_COUNT() {
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long bookID = booksBefore.get(0).getBookID();
		int editType = BookController.SET_RATING_COUNT;

		// Test 1: not the valid type of data for SET_RATING_COUNT(newVal should be a
		// Integer
		Pair<Boolean, String> res = BookController.editBook(username, password, bookID, editType, "1.0");
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(
				"Invalid variable type for this class of edit operation. Expected: java.lang.Integer, Instead got: java.lang.String",
				res.getValue1());

		// Test 2: invalid value for rating
		int countRaters = -1;
		res = BookController.editBook(username, password, bookID, editType, countRaters);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Invalid value for rating count -1. Value must be greater than 0", res.getValue1());

		// Test 3: valid setting
		countRaters = 3;
		res = BookController.editBook(username, password, bookID, editType, countRaters);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
	}

	// DEPENDENCIES: getAllBooks(), getAllSeries()
	@Test
	public void testEditBook_SET_SERIES_ID() {
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long bookID = booksBefore.get(0).getBookID();
		int editType = BookController.SET_SERIES_ID;
		ArrayList<Series> seriesBefore = DAORoot.seriesDao.getAllSeries();
		// Test 1: not the valid type of data for SET_RATING_COUNT(newVal should be a
		// Integer
		Pair<Boolean, String> res = BookController.editBook(username, password, bookID, editType, "1.0");
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(
				"Invalid variable type for this class of edit operation. Expected: java.lang.Integer, Instead got: java.lang.String",
				res.getValue1());

		// Test 2: invalid value for rating
		int seriesID = -1;
		res = BookController.editBook(username, password, bookID, editType, seriesID);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Can't set series as no series with id: -1, exists.", res.getValue1());

		// Test 3: valid setting
		seriesID = seriesBefore.get(0).getSeriesID();
		res = BookController.editBook(username, password, bookID, editType, seriesID);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
	}

	@Test
	public void testSearchBook_byIdentifier() {
		Pair<String, String> validSearchTerms = new Pair<String, String>("isbn", "9780199535569");

		// Test 1: perform type of search that doesn't exist
		Pair<Book[], String> res = BookController.searchBook(30, validSearchTerms);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Invalid search type!", res.getValue1());

		// Test 2: perform with null data
		Pair<String, String> invalidSearchTerms = null;
		res = BookController.searchBook(1, invalidSearchTerms);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Search field cannot be null", res.getValue1());

		// Test 3: perform with wrong type of search data (array of strings instead of a
		// tuple of strings)
		res = BookController.searchBook(1, new String[] { "isbn", "9780199535569" });
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Data type/value for query does not conform to expected format.", res.getValue1());

		// Test 4: perform with Pair data type but use integers.
		res = BookController.searchBook(1, new Pair<Integer, Integer>(7, 99535569));
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Data type/value for query does not conform to expected format.", res.getValue1());
		
		res = BookController.searchBook(1, new Pair<String, Integer>("7", 99535569));
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Data type/value for query does not conform to expected format.", res.getValue1());
		
		res = BookController.searchBook(1, new Pair<Integer, String>(7, "99535569"));
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Data type/value for query does not conform to expected format.", res.getValue1());

		// Test 5: perform with partially invalid data
		res = BookController.searchBook(1, new Pair<String, String>("", "9780199535569"));
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Search terms ('' : '9780199535569') is invalid. The terms can't be empty.", res.getValue1());

		res = BookController.searchBook(1, new Pair<String, String>("isbn", ""));
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Search terms ('isbn' : '') is invalid. The terms can't be empty.", res.getValue1());

		// Test 6: perform search with no expected results.
		res = BookController.searchBook(1, new Pair<String, String>("ufid", "9780199535569"));
		assertNull(res.getValue0()[0]);
		assertNull(res.getValue1());

		// Test 7: perform search with wrong capitalization on search terms (should
		// still get results)
		res = BookController.searchBook(1, new Pair<String, String>("iSbN", "9780199535569"));
		assertNotNull(res.getValue0()[0]);
		assertNull(res.getValue1());
	}

	@Test
	public void testSearchBook_byAuthorARRAY() {

		/**** AUTHOR SEARCH: ARRAY ****/
		String[] authorNames = new String[] { "James", "joyce" };
		int searchType = BookController.SEARCH_BY_AUTHOR;

		// Test 1: perform with null data
		Pair<Book[], String> res = BookController.searchBook(searchType, null);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Search field cannot be null", res.getValue1());

		// Test 2: perform with wrong type of search data
		res = BookController.searchBook(searchType, new Long[] { 90l, 9780199535569l });
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Data type/value for query does not conform to expected format.", res.getValue1());

		// Test 4: perform with partially null data
		res = BookController.searchBook(searchType, new String[] { "James", null });
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Expected two fields: (first name, last name) instead got: 2 (James, null)", res.getValue1());

		// Test 4 B: try with array being wrong length
		res = BookController.searchBook(searchType, new String[] { "James Joyce" });
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Expected two fields: (first name, last name) instead got: 1 (James Joyce)", res.getValue1());

		// Test 5: perform test with partially empty fields.
		res = BookController.searchBook(searchType, new String[] { "James", "" });
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Expected two fields: (first name, last name) instead got: 2 ('James', '')", res.getValue1());

		res = BookController.searchBook(searchType, new String[] { "", "Joyce" });
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Expected two fields: (first name, last name) instead got: 2 ('', 'Joyce')", res.getValue1());

		// Test 6: perform search for non existent author (but valid theoretically).
		res = BookController.searchBook(searchType, new String[] { "Who", "Dat" });
		assertNull(res.getValue1(), res.getValue0());
		assertNotNull(res.getValue1());
		String expectedMsg = String.format(
				"No results were returned for Author (i.e. author '%s' was not found in the database).", "Who Dat");
		assertEquals(expectedMsg, res.getValue1());

		// Test 7: perform search with wrong capitalization on search terms (should
		// work)
		res = BookController.searchBook(searchType, new String[] { "James", "jOyce" });
		assertNotNull(res.getValue1(), res.getValue0());
		assertNull(res.getValue1());
		assertEquals(6, res.getValue0().length);

		// Test 8: perform search with results expected and correct caps in the name.
		Pair<Book[], String> res2 = BookController.searchBook(searchType, authorNames);
		assertNotNull(res.getValue1(), res.getValue0());
		assertNull(res.getValue1());

		// sanity check ensure we get same result regardless of author capitalization.
		Book[] rtnedBooks1 = res.getValue0();
		Book[] rtnedBooks2 = res2.getValue0();
		assertEquals(rtnedBooks1.length, rtnedBooks2.length);
		for (Book bookX : rtnedBooks2) {
			assertTrue(ArrayUtils.contains(rtnedBooks1, bookX));
		}
	}

	@Test
	public void testSearchBook_byAuthorSINGLESTRING() {
		int searchType = BookController.SEARCH_BY_AUTHOR;

		// Test 1: perform with null search term
		Pair<Book[], String> res = BookController.searchBook(searchType, null);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Search field cannot be null", res.getValue1());

		// Test 2: perform test with empty field.
		res = BookController.searchBook(searchType, "	");
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Invalid author '	' for search.", res.getValue1());

		// Test 3: perform search with no results expected as author does no exist.
		res = BookController.searchBook(searchType, "Who Dat");
		assertNull(res.getValue1(), res.getValue0());
		assertNotNull(res.getValue1());
		String expectedMsg = String.format(
				"No results were returned for Author (i.e. author '%s' was not found in the database).", "Who Dat");
		assertEquals(expectedMsg, res.getValue1());

		// Test 4: perform search with wrong capitalization on search terms
		res = BookController.searchBook(searchType, "James jOyce");
		assertNotNull(res.getValue1(), res.getValue0());
		assertNull(res.getValue1());
		assertEquals(6, res.getValue0().length);

		// Test 5: perform search with results expected and correct caps in the name.
		Pair<Book[], String> res2 = BookController.searchBook(searchType, "James Joyce ");
		assertNotNull(res.getValue1(), res.getValue0());
		assertNull(res.getValue1());

		// sanity check ensure we get same result regardless of author capitalization.
		Book[] rtnedBooks1 = res.getValue0();
		Book[] rtnedBooks2 = res2.getValue0();
		assertEquals(rtnedBooks1.length, rtnedBooks2.length);
		for (Book bookX : rtnedBooks2) {
			assertTrue(ArrayUtils.contains(rtnedBooks1, bookX));
		}

		// Test 6: perform search with results expected
	}

	// Dependencies: getAllAuthors()
	@Test
	public void testSearchBook_byAuthorAUTHORID() {
		ArrayList<Author> authorsBefore = DAORoot.authorDao.getAllAuthors();
		int searchType = BookController.SEARCH_BY_AUTHOR;

		// Test 1: perform with null data
		Pair<Book[], String> res = BookController.searchBook(searchType, null);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Search field cannot be null", res.getValue1());

		// Test 2: perform search with no results (id of an author not in database)
		int searchVal = -1;
		res = BookController.searchBook(searchType, searchVal);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals(String.format(
				"No results were returned for Author (i.e. author with ID: '%d' was not found in the database).",
				searchVal), res.getValue1());

		// Test 3: perform search with results expected
		searchVal = authorsBefore.get(0).getAuthorID();
		res = BookController.searchBook(searchType, searchVal);
		assertNotNull(res.getValue0());
		assertNull(res.getValue1());
		assertEquals(6, res.getValue0().length);
	}

	// Dependencies: getAllSeries()
	@Test
	public void testSearchBook_bySeries() {
		int searchType = BookController.SEARCH_BY_SERIES;
		// Test 1: invalid data type to query with
		Pair<Book[], String> res = BookController.searchBook(searchType, "whats up");
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Data type/value for query does not conform to expected format.", res.getValue1());

		// Test 2: perform with null data
		res = BookController.searchBook(searchType, null);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Search field cannot be null", res.getValue1());

		// Test 3: perform search with no results (id of an series not in database)
		int searchVal = -1;
		res = BookController.searchBook(searchType, searchVal);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals(String.format("Search returned no results. (i.e. no series with ID: %d exists)", searchVal),
				res.getValue1());

		// Test 3: valid id:
		searchVal = DAORoot.seriesDao.getAllSeries().get(0).getSeriesID();
		res = BookController.searchBook(searchType, searchVal);
		assertNotNull(res.getValue0());
		assertNull(res.getValue1());
		assertEquals(3, res.getValue0().length);
	}

	@Test
	public void testSearchBook_byTitle() {
		int searchType = BookController.SEARCH_BY_TITLE;
		// Test 1: invalid data type to query with
		Pair<Book[], String> res = BookController.searchBook(searchType, 5);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Data type/value for query does not conform to expected format.", res.getValue1());

		// Test 2: perform with null data
		res = BookController.searchBook(searchType, null);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals("Search field cannot be null", res.getValue1());
		
		//Test 3: perform search with only whitespace
		String searchVal = " ";
		res = BookController.searchBook(searchType, searchVal);
		assertNull(res.getValue0());
		assertNotNull(res.getValue1());
		assertEquals(String.format("Invalid search term: '%s' for search.", searchVal), res.getValue1());

		// Test 4: perform search with no results (nothing matches in database)
		res = BookController.searchBook(searchType, "WHO DAT");
		assertNotNull(res.getValue0());
		assertNull(res.getValue1());
		assertEquals(0, res.getValue0().length);

		// Test 5: valid partial search with bad casing :
		searchVal = "bOO";
		res = BookController.searchBook(searchType, searchVal);
		assertNotNull(res.getValue0());
		assertNull(res.getValue1());
		assertEquals(7, res.getValue0().length);
	}
	
	@Test
	public void testGetRandomBook() {
		HashSet<Book> randomBook = new HashSet<Book>();
		for(int i = 0; i<5; i++) {
			randomBook.add(BookController.getRandomBook());
		}
		assertTrue(1<randomBook.size());
	}
}
