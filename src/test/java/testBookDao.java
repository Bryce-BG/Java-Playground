import static org.junit.Assert.*;

import static com.BryceBG.DatabaseTools.Database.DAORoot.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import com.BryceBG.DatabaseTools.Database.Book.BookDaoInterface;
import com.BryceBG.DatabaseTools.Database.Book.BookDaoInterface.EDIT_TYPE;
import com.BryceBG.DatabaseTools.Database.Series.Series;

import testUtils.UtilsForTests;

public class testBookDao {

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

	@Test
	public void testGetAllBooks() {
		ArrayList<Book> books = DAORoot.bookDao.getAllBooks();
		// 1. should have 5 books based on resetDBEntries.sql script
		assertEquals("Wrong amount of books was returned", 7, books.size());
		List<String> titles = new ArrayList<String>(books.size()); // make sure we have titles
		for (int x = 1; x <= books.size(); x++) {
			titles.add(String.format("TestBook%d", x));
		}
		// 2. should have all the titles enumerated in resetDBEntries.sql script
		for (Book bookX : books) {
			assertTrue(titles.contains(bookX.getTitle()));
			if (bookX.getTitle().equals(titles.get(0))) {
				// verify most fields are filled correctly from DB pull.
				int[] authIDs = bookX.getAuthorIDs();
				assertEquals(1, authIDs.length);

				float avgRating = bookX.getAvgRating();
				assertEquals(0.0, avgRating, 0.0f);

				long bookID = bookX.getBookID();
				assertTrue(bookID > 0);

				float bookIndex = bookX.getBookIndexInSeries();
				assertEquals(-1.0, bookIndex, 0.0f);

				int countAuthors = bookX.getCountAuthors();
				assertEquals(1, countAuthors);

				String coverPath = bookX.getCoverLocation();
				assertEquals("cover/1/2", coverPath);

				String coverName = bookX.getCoverName();
				assertEquals("coverImage.jpg", coverName);

				assertEquals("This is a test book, it does not exist", bookX.getDescription());

				assertEquals(-1, bookX.getEdition());

				assertNull(bookX.getGenres());

				assertFalse(bookX.getHasIdentifiers());
				assertNull(bookX.getIdentifiers());

//				bookX.getLargeCover();
				assertNull(bookX.getPersonalComment());
				assertNull(bookX.getPersonalQuotes());
				assertEquals(-1, bookX.getPersonalRating(), 0.0f);
				assertNull(bookX.getPersonalSeriesComment());
				assertNull(bookX.getPersonalShelves());

				int primary_auth_id = bookX.getPrimaryAuthorID();
				assertEquals(primary_auth_id, bookX.getAuthorIDs()[0]);

				assertNull(bookX.getPublishDate());
				assertNull(bookX.getPublisher());
				assertEquals(0, bookX.getRatingCount());

				// TODO bookX.getSeriesID();
//				bookX.getSmallCover();

				assertEquals(titles.get(0), bookX.getTitle());

			} else if (bookX.getTitle().equals(titles.get(1))) {
				// TASK2: create book with a single genre tagged
				String genres[] = bookX.getGenres();
				assertEquals(1, genres.length);
				assertEquals(genres[0], "TestGenre1");

			} else if (bookX.getTitle().equals(titles.get(2))) {
				// TASK3: create a book with both genres
				String genres[] = bookX.getGenres();
				assertEquals(2, genres.length);
				boolean checkGenres = (genres[0].equals("TestGenre1") || genres[0].equals("TestGenre2"))
						&& (genres[1].equals("TestGenre1") || genres[1].equals("TestGenre2"));
				assertTrue(checkGenres);
			} else if (bookX.getTitle().equals(titles.get(3))) {
				// Task4: create book with multiple authors
				assertEquals(2, bookX.getCountAuthors());
				assertEquals(2, bookX.getAuthorIDs().length);
			} else if (bookX.getTitle().equals(titles.get(4))) {
				// TASK5 create book in series
				assertNotEquals(-1, bookX.getSeriesID());
			} else if (bookX.getTitle().equals(titles.get(5))) {
				// TASK6: book with 1 identifier
				assertEquals(1, bookX.getIdentifiers().length);
				assertEquals("isbn", bookX.getIdentifiers()[0].getValue0());
				assertEquals("9780199535569", bookX.getIdentifiers()[0].getValue1());
			} else {
				// TASK7: book with 2 identifiers
				assertEquals("isbn", bookX.getIdentifiers()[0].getValue0());
				assertEquals("0143105426", bookX.getIdentifiers()[0].getValue1());
				assertEquals("uuid", bookX.getIdentifiers()[1].getValue0());
				assertEquals("50f9f8b1-8a81-4dd5-b104-0766188d7d2c", bookX.getIdentifiers()[1].getValue1());
			}
		}
	}

	// Dependencies: getAllBooks()
	@Test
	public void testGetBookByID() {
		ArrayList<Book> books = DAORoot.bookDao.getAllBooks();
		for (Book bookX : books) {
			Book bookY = DAORoot.bookDao.getBookByBookID(bookX.getBookID());
			assertEquals(bookX, bookY);
		}
	}

	// Dependencies: getAllBooks(), authorDao.getAuthor()
	@Test
	public void testGetBooksByAuthor() {
		Pair<String, String> a1 = new Pair<String, String>("James", "Joyce");

		Author author1 = DAORoot.authorDao.getAuthor(a1.getValue0(), a1.getValue1());

		// Test 1: check for author 1
		Book[] b1 = DAORoot.bookDao.getBooksByAuthor(author1.getAuthorID());
		ArrayList<Book> books = DAORoot.bookDao.getAllBooks();
		assertEquals(6, b1.length);// in our database "James Joyce" has authored or co-authored all 6 books.
		for (Book bookX : b1) {
			assertTrue(books.contains(bookX));
		}

		// Test 2: check if author 2 works;
		Pair<String, String> a2 = new Pair<String, String>("Test", "Author2");
		Author author2 = DAORoot.authorDao.getAuthor(a2.getValue0(), a2.getValue1());

		Book[] b2 = DAORoot.bookDao.getBooksByAuthor(author2.getAuthorID());
		assertEquals(2, b2.length); // of test entries he only wrote 2 (1 solo 1 co-authored)
		for (Book bookX : b2) {
			assertTrue(books.contains(bookX));
		}

	}

	@Test
	public void testGetRandomBook() {
		List<Book> books = new ArrayList<Book>();
		Book book = DAORoot.bookDao.getRandomBook();
		books.add(book);

		for (int x = 0; x < 5; x++) {
			book = DAORoot.bookDao.getRandomBook();
			if (!books.contains(book))
				books.add(book);
		}
		// Theoretically this test CAN fail since it is random however, with 5 tries and
		// 5 books in the database the probability of this occurring is: 0.00000121426
		assertTrue(books.size() > 1);
	}

	// DEPENDENCY
	@Test
	public void testGetBookByIdentifier() {
		// Test 1: invalid isbn
		Book x = DAORoot.bookDao.getBookByIdentifier("isbn", "0199535570");
		assertNull(x);
		// Test 2: isbn not in our system.
		x = DAORoot.bookDao.getBookByIdentifier("isbn", "1905921055");
		assertNull(x);

		// Test 3: valid isbn (except spaces)
		x = DAORoot.bookDao.getBookByIdentifier("isbn", " 9780199535569");
		assertNotNull(x);

		// Test 4: hyphens in isbn:
		x = DAORoot.bookDao.getBookByIdentifier("isbn", " 9780-199-535-569");
		assertNotNull(x);

		// Test 5: null identifier value
		x = DAORoot.bookDao.getBookByIdentifier("isbn", null);
		assertNull(x);
	}

	@Test
	public void testGetBooksBySeries() {
		ArrayList<Series> series = DAORoot.seriesDao.getAllSeries();
		assertEquals(1, series.size());
		Book[] results = DAORoot.bookDao.getBooksBySeries(series.get(0));
		assertEquals(3, results.length); // ensure there are correct amount of books returned
		ArrayList<String> titles = new ArrayList<String>();
		for (Book x : results) {
			titles.add(x.getTitle());
		}

		// ensure that the titles are correct
		ArrayList<String> titlesThatShouldBeInList = new ArrayList<String>();
		for (int i = 5; i <= 7; i++) {
			titlesThatShouldBeInList.add(String.format("TestBook%d", i));
		}

		assertTrue(titles.containsAll(titlesThatShouldBeInList));
	}

	@Test
	public void testGetBooksByTitle() {
		// Test 1: check if we get book for one of titles exactly
		Book[] t = DAORoot.bookDao.getBooksByTitle("TestBook1");
		assertEquals(1, t.length);

		// Test 2: check if we get book even with bad casing.
		t = DAORoot.bookDao.getBooksByTitle("TESTBOOK1");
		assertEquals(1, t.length);

		// Test 3: check book name not in database
		t = DAORoot.bookDao.getBooksByTitle("RandomBook");
		assertEquals(0, t.length);

		// Test 4: null title
		t = DAORoot.bookDao.getBooksByTitle(null);
		assertEquals(0, t.length);
		// Test 5: partial title match. beginning missing
		t = DAORoot.bookDao.getBooksByTitle("Book1");
		assertEquals(1, t.length);

		// Test 6: partial title match. end missing
		t = DAORoot.bookDao.getBooksByTitle("TestBoo");
		assertEquals(7, t.length);

	}

	// Dependencies getAllBooks()
	@Test
	public void testRemoveBookByBookID() {
		// Test 1: try removing all the books currently in database.
		ArrayList<Book> allBooks = DAORoot.bookDao.getAllBooks();
		for (Book bookX : allBooks) {
			assertTrue("failed to remove" + bookX.getTitle(), DAORoot.bookDao.removeBook(bookX.getBookID()));
		}
		ArrayList<Book> allBooksAfter = DAORoot.bookDao.getAllBooks();
		assertEquals(0, allBooksAfter.size());

		// Test 2: try removing invalid books.
		assertFalse(DAORoot.bookDao.removeBook(0));
		assertFalse(DAORoot.bookDao.removeBook(-1));

	}

	// Dependencies getAllBooks()
	@Test
	public void testAddBook() {
		ArrayList<Book> allBooks = DAORoot.bookDao.getAllBooks();
		Author a1 = DAORoot.authorDao.getAuthor("James", "Joyce");
		Author a2 = DAORoot.authorDao.getAuthor("Test", "Author2");
		int[] authorIDs = new int[] { a1.getAuthorID() };

		// Test 1: add new book with single author.
		assertTrue(DAORoot.bookDao.insertBookIntoDB(authorIDs, "THIS BOOK IS BORING BUT NEW", -1, "NEWBOOK4TW"));
		ArrayList<Book> allBooksAfter = DAORoot.bookDao.getAllBooks();
		assertEquals(allBooksAfter.size() - 1, allBooks.size());

		// Test 2: null description
		assertTrue(DAORoot.bookDao.insertBookIntoDB(authorIDs, null, -1, "NEWBOOK4TW2"));

		// Test 3: empty title
		assertFalse(DAORoot.bookDao.insertBookIntoDB(authorIDs, "THIS BOOK IS BORING BUT NEW", -1, " "));

		// Test 4: null title
		assertFalse(DAORoot.bookDao.insertBookIntoDB(authorIDs, "THIS BOOK IS BORING BUT NEW", -1, null));

		// Test 5: authorID not valid (not in db)
		assertFalse(
				DAORoot.bookDao.insertBookIntoDB(new int[] { 999 }, "THIS BOOK IS BORING BUT NEW", -1, "NEWBOOK4TW2"));
		// Test 5.b: authorID is just invalid always
		assertFalse(
				DAORoot.bookDao.insertBookIntoDB(new int[] { -1 }, "THIS BOOK IS BORING BUT NEW", -1, "NEWBOOK4TW2"));

		// Test 6: multiple authors
		authorIDs = new int[] { a1.getAuthorID(), a2.getAuthorID() };
		int sizeBefore = bookDao.getAllBooks().size();
		assertTrue(bookDao.insertBookIntoDB(authorIDs, "NEW book with multple authors", -1, "NEWBOOK4THREE"));
		assertEquals(sizeBefore + 1, bookDao.getAllBooks().size()); // should have gone up 1

		// Test 7: pre-existing combo of author/title/edition of a book (fails due to DB
		// constraint)
		assertFalse(DAORoot.bookDao.insertBookIntoDB(authorIDs, "book already exists", -1, "NEWBOOK4TW2"));

		// Test 8: author/title combo already exists in db but new edition of the book
		assertTrue(DAORoot.bookDao.insertBookIntoDB(authorIDs, "NEW book with multple authors", 1, "NEWBOOK4TW2"));

	}

	/** functions for testing editBook() function **/
	// Dependencies getAllBooks(), getAllAuthors()
	@Test
	public void testEditBook_AddAuthor() {
		ArrayList<Book> booksBefore = bookDao.getAllBooks();
		ArrayList<Author> authors = authorDao.getAllAuthors();

		int[] possibleAuthorIDs = new int[authors.size()];
		for (int i = 0; i < possibleAuthorIDs.length; i++) {
			possibleAuthorIDs[i] = authors.get(i).getAuthorID();
		}

		// TEST 1: add authors for all books in db (where we have to change
		// primary_author_id and where we don't)
		for (Book bookX : booksBefore) {
			if (bookX.getCountAuthors() < authors.size()) { // can add authors
				// determine which authors arn't already in the book
				for (int authorXID : possibleAuthorIDs) {
					if (ArrayUtils.contains(bookX.getAuthorIDs(), authorXID) == false) // new author we can add to that
																						// book
						assertTrue(
								bookDao.editBook(bookX.getBookID(), BookDaoInterface.EDIT_TYPE.ADD_AUTHOR, authorXID));
				}
			} // end if (adding more authors
		}

		ArrayList<Book> booksAfter = bookDao.getAllBooks();
		for (Book bookX : booksAfter) {
			assertEquals(possibleAuthorIDs.length, bookX.getCountAuthors());
		}

		// TEST 2: try to add authors to a book that doesn't exist (invalid book_id)
		assertFalse(bookDao.editBook(-1, BookDaoInterface.EDIT_TYPE.ADD_AUTHOR, possibleAuthorIDs[0]));

		// TEST 3: try to add with null new authorID
		runBeforeTest(); // reset DB as otherwise this could fail for other reasons
		booksBefore = bookDao.getAllBooks();
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.ADD_AUTHOR, null));

		// TEST 4: authorID not in DB
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.ADD_AUTHOR, 50));

		// TEST 5: try to add an authorID already used with book
		authors = authorDao.getAllAuthors(); // need to re-acquire this
		possibleAuthorIDs = new int[authors.size()];
		for (int i = 0; i < possibleAuthorIDs.length; i++)
			possibleAuthorIDs[i] = authors.get(i).getAuthorID();

		// try to add all authors and one (at least should fail) for book as it already
		// has an author
		boolean allSucceeded = true;
		for (int authXID : possibleAuthorIDs) {
			allSucceeded = allSucceeded
					& bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.ADD_AUTHOR, authXID);
		}
		assertFalse(allSucceeded);

		// Test 5: pass in authorID to add as the wrong type
		for (int authXID : possibleAuthorIDs) {
			allSucceeded = allSucceeded & bookDao.editBook(booksBefore.get(0).getBookID(),
					BookDaoInterface.EDIT_TYPE.ADD_AUTHOR, Integer.toString(authXID));
		}

	}

	// Dependencies getAllBooks(), getAllAuthors(), editBook().AddAuthors
	@Test
	public void testEditBook_RemoveAuthor() {
		ArrayList<Book> booksBefore = bookDao.getAllBooks();
		ArrayList<Author> authors = authorDao.getAllAuthors();

		int[] possibleAuthorIDs = new int[authors.size()];
		for (int i = 0; i < possibleAuthorIDs.length; i++) {
			possibleAuthorIDs[i] = authors.get(i).getAuthorID();
		}

		// Test 1: remove_author invalid book_id
		assertFalse(bookDao.editBook(-1, BookDaoInterface.EDIT_TYPE.REMOVE_AUTHOR, possibleAuthorIDs[0]));

		// Test 2: remove_author with invalid author_ID (author exists but isn't listed
		// for a book.
		for (int potentialAuthor : possibleAuthorIDs) {
			// find an author not in the current book's listing
			if (ArrayUtils.contains(booksBefore.get(0).getAuthorIDs(), potentialAuthor) == false)
				assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.REMOVE_AUTHOR,
						potentialAuthor));
		}

		// Test 3: remove_author with author_id NOT in db
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.REMOVE_AUTHOR, -1));

		// Test 4: remove_author with a string author_ID
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.REMOVE_AUTHOR, "3"));

		// Test 5: remove_author with null author_ID
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.REMOVE_AUTHOR, null));

		// Test 6: remove_author with book only having 1 author.
		for (Book bookX : booksBefore) {
			if (bookX.getCountAuthors() == 1) {
				assertFalse(bookDao.editBook(bookX.getBookID(), BookDaoInterface.EDIT_TYPE.REMOVE_AUTHOR,
						bookX.getPrimaryAuthorID()));
				break;
			}
		}
		// Test 7: remove_author where author_removed is primary.
		// make it so book 0 has 2 authors.
		for (int authXID : possibleAuthorIDs) {
			bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.ADD_AUTHOR, authXID);
		}
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.REMOVE_AUTHOR,
				booksBefore.get(0).getPrimaryAuthorID()));

		// Test 8: remove_author where author removed is NOT primary.
		// make it so book 0 has multiple authors.
		for (int authXID : possibleAuthorIDs) {
			bookDao.editBook(booksBefore.get(0).getBookID(), BookDaoInterface.EDIT_TYPE.ADD_AUTHOR, authXID);
		}

		// have to get book again in case adding author changed primary.
		booksBefore = bookDao.getAllBooks();
		for (Book bookX : booksBefore) {
			if (bookX.getCountAuthors() > 1) {
				int[] possibleNotPrimaryIDs = ArrayUtils.removeElement(bookX.getAuthorIDs(),
						bookX.getPrimaryAuthorID());
				assertTrue(bookDao.editBook(bookX.getBookID(), BookDaoInterface.EDIT_TYPE.REMOVE_AUTHOR,
						possibleNotPrimaryIDs[0]));
			}

		}
	}

	// Dependencies getAllBooks(), getBookByBookID()
	@Test
	public void testEditBook_SetAvgRating() {
		ArrayList<Book> booksBefore = bookDao.getAllBooks();

		// Test 1: bookID is not in DB
		EDIT_TYPE editType = EDIT_TYPE.SET_AVG_RATING;
		assertFalse(bookDao.editBook(-1, editType, 1.0f));

		// Test 2: null float
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: not a float passed in.
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 9.9999));

		// Test 4: float with rating >10
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 10.1f));

		// Test 5: float with value <0
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, -0.01f));

		// Test 6: valid range to many decimals
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 9.9999f));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(10, bookX.getAvgRating(), 0.0f); // verify set worked

	}

	// Dependencies getAllBooks(), getBookByBookID()
	@Test
	public void testEditBook_SetBookIndexInSeries() {
		EDIT_TYPE editType = EDIT_TYPE.SET_BOOK_INDEX_IN_SERIES;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();

		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, 1.0f));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, "0.3"));

		// Test 4: valid setting
		float newVal = 1.5f;
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, newVal));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(newVal, bookX.getBookIndexInSeries(), 0.0f); // verify set worked

	}

	// Dependencies getAllBooks(), getBookByBookID()
	@Test
	public void testEditBook_SetCoverLocation() {
		EDIT_TYPE editType = EDIT_TYPE.SET_COVER_LOCATION;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();

		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, "1.0f"));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: valid setting
		String newVal = "path/foo/bar";
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, newVal));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(newVal, bookX.getCoverLocation()); // verify set worked
	}

	// Dependencies getAllBooks(), getBookByBookID()
	@Test
	public void testEditBook_SetCoverName() {
		EDIT_TYPE editType = EDIT_TYPE.SET_COVER_NAME;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();

		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, "cover.jpg"));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: valid setting
		String newVal = "23123.jpg";
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, newVal));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(newVal, bookX.getCoverName()); // verify set worked
	}

	// Dependencies getAllBooks(), getBookByBookID()
	@Test
	public void testEditBook_SetDescription() {
		EDIT_TYPE editType = EDIT_TYPE.SET_DESCRIPTION;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();

		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, "cover.jpg"));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: valid setting
		String newVal = "this is a book";
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, newVal));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(newVal, bookX.getDescription());

	}

	// Dependencies getAllBooks(), getBookByBookID()
	@Test
	public void testEditBook_SetEdition() {
		EDIT_TYPE editType = EDIT_TYPE.SET_EDITION;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();

		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, 1));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: valid setting
		int newVal = 4;
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, newVal));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(newVal, bookX.getEdition());

	}

	// Dependencies getAllBooks(), getBookByBookID()
	@Test
	public void testEditBook_SetPublishDate() {
		EDIT_TYPE editType = EDIT_TYPE.SET_PUBLISH_DATE;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();

		Calendar cal = Calendar.getInstance(); // This to obtain today's date in our Calendar var.
		java.sql.Timestamp validNewValue = new java.sql.Timestamp(cal.getTimeInMillis());

		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, validNewValue));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: valid setting
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, validNewValue));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		System.out.println(bookX.getPublishDate().toString());
		assertEquals(validNewValue, bookX.getPublishDate());
	}

	// Dependencies getAllBooks(), getBookByBookID()
	@Test
	public void testEditBook_SetPublisher() {
		EDIT_TYPE editType = EDIT_TYPE.SET_PUBLISHER;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();
		String validNewValue = "publishers inc.";
		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, validNewValue));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: valid setting
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, validNewValue));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(validNewValue, bookX.getPublisher());
	}

	// Dependencies getAllBooks(), getBookByBookID()
	@Test
	public void testEditBook_SetRatingCount() {
		EDIT_TYPE editType = EDIT_TYPE.SET_RATING_COUNT;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();
		Integer validNewValue = 4;
		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, validNewValue));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: not a valid value for field.
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, -1));

		// Test 5: valid setting
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, validNewValue));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(validNewValue.intValue(), bookX.getRatingCount());

	}

	// Dependencies getAllBooks(), getBookByBookID(), getAllSeries()
	@Test
	public void testEditBook_SetSeriesID() {
		EDIT_TYPE editType = EDIT_TYPE.SET_SERIES_ID;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();
		ArrayList<Series> series = seriesDao.getAllSeries();
		Integer validNewValue = series.get(0).getSeriesID();
		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, validNewValue));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: not a valid value for field.
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, -1));

		// Test 5: valid setting
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, validNewValue));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(validNewValue.intValue(), bookX.getSeriesID());

	}

	// Dependencies getAllBooks(), getBookByBookID(), getAllGenres()
	@Test
	public void testEditBook_SetGenres() {
		EDIT_TYPE editType = EDIT_TYPE.SET_GENRES;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();
		ArrayList<String> genreNames = genreDao.getAllGenreNames();
		String[] validNewValue = new String[] { genreNames.get(0) };
		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, validNewValue));

		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: not a valid value for field.
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, "HelloWorld"));

		// Test 5: valid setting
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, validNewValue));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(validNewValue[0], bookX.getGenres()[0]);

		// Test 6: setting multiple genres
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType,
				new String[] { genreNames.get(0), genreNames.get(1) }));
		bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(genreNames.get(0), bookX.getGenres()[0]);
		assertEquals(genreNames.get(1), bookX.getGenres()[1]);

		// TODO test setting partially incorrect arrays for newValue
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType,
				new String[] { genreNames.get(0), " ", " Hi" }));

	}

	// Dependencies getAllBooks(), getBookByBookID(),
	@Test
	public void testEditBook_SetIdentifiers() {
		EDIT_TYPE editType = EDIT_TYPE.SET_IDENTIFIERS;
		ArrayList<Book> booksBefore = bookDao.getAllBooks();
		@SuppressWarnings("unchecked")
		Pair<String, String>[] validNewValue = new Pair[1];
		validNewValue[0] = new Pair<String, String>("isbn", "98292921939");

		// Test 1: bookID is not in DB
		assertFalse(bookDao.editBook(-1, editType, validNewValue));

		System.out.println("TEST DEBUG: booksBefore[0].bookID = " + booksBefore.get(0).getBookID());
		// Test 2: null value for new value
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, null));

		// Test 3: NOT the correct type for newVal
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, 0.3));

		// Test 4: not a valid value for field. (PARTIALLY null identifier or fully null
		// identifier

		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType,
				new Pair[] { new Pair<String, String>(null, "98292921939") }));
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType,
				new Pair[] { new Pair<String, String>("isbn", null) }));
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType,
				new Pair[] { new Pair<String, String>("isbn", "98292921939"), null }));

		// Test 5: valid setting
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, validNewValue));
		Book bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertTrue(bookX.getHasIdentifiers()); // field should be set.
		assertEquals(1, bookX.getIdentifiers().length);
		assertEquals(validNewValue[0], bookX.getIdentifiers()[0]);

		// Test 6: setting multiple identifiers (identical value) --violates primary key
		// constraint
		int idCountBefore = bookX.getIdentifiers().length;
		assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, new Pair[] {
				new Pair<String, String>("isbn", "98292921939"), new Pair<String, String>("isbn", "98292921939") }));
		bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(idCountBefore, bookX.getIdentifiers().length); //make sure the update rolled back correctly

		// Test 7 set multiple identifiers
		@SuppressWarnings("unchecked")
		Pair<String, String>[] ids = new Pair[] { new Pair<String, String>("isbn", "98292921939"),
				new Pair<String, String>("isbn", "98292921940") };
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, ids));
		bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertEquals(ids.length, bookX.getIdentifiers().length);

		//Test 8: remove all identifiers
		ids = new Pair[0];
		assertTrue(bookDao.editBook(booksBefore.get(0).getBookID(), editType, ids));
		bookX = bookDao.getBookByBookID(booksBefore.get(0).getBookID());
		assertFalse(bookX.getHasIdentifiers());
		assertNull(bookX.getIdentifiers());

		// TODO test setting partially incorrect arrays for newValue
//			assertFalse(bookDao.editBook(booksBefore.get(0).getBookID(), editType, new String[] {genreNames.get(0), " ", " Hi"}));

	}

}
