import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Author.Author;
import com.BryceBG.DatabaseTools.Database.Book.Book;
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
				assertEquals(0.0, bookIndex, 0.0f);

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
				assertEquals("ISBN", bookX.getIdentifiers()[0].getValue0());
				assertEquals("9780199535569", bookX.getIdentifiers()[0].getValue1());
			} else {
				// TASK7: book with 2 identifiers
				assertEquals("ISBN", bookX.getIdentifiers()[0].getValue0());
				assertEquals("0143105426", bookX.getIdentifiers()[0].getValue1());
				assertEquals("UUID", bookX.getIdentifiers()[1].getValue0());
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
		assertEquals(7, b1.length);// in our database "James Joyce" has authored or co-authored all 7 books.
		for (Book bookX : b1) {
			assertTrue(books.contains(bookX));
		}

		// Test 2: check if author 2 works;
		Pair<String, String> a2 = new Pair<String, String>("Test", "Author2");
		Author author2 = DAORoot.authorDao.getAuthor(a2.getValue0(), a2.getValue1());

		Book[] b2 = DAORoot.bookDao.getBooksByAuthor(author2.getAuthorID());
		assertEquals(1, b2.length); // of test entries he only wrote 1
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
		int[] authorIDs = new int[] {a1.getAuthorID()};
		
		//Test 1: add new book with single author.
		assertTrue(DAORoot.bookDao.addBook(authorIDs, "THIS BOOK IS BORING BUT NEW", -1, "NEWBOOK4TW"));
		ArrayList<Book> allBooksAfter = DAORoot.bookDao.getAllBooks();
		assertEquals(allBooksAfter.size()-1, allBooks.size());
		
		//Test 2: null description
		assertFalse(DAORoot.bookDao.addBook(authorIDs, null, -1, "NEWBOOK4TW2"));
		
		//Test 3: empty title
		assertFalse(DAORoot.bookDao.addBook(authorIDs, "THIS BOOK IS BORING BUT NEW", -1, " "));
		
		//Test 4: null title
		assertFalse(DAORoot.bookDao.addBook(authorIDs, "THIS BOOK IS BORING BUT NEW", -1, null));
		
		//Test 5: authorID not valid
		assertFalse(DAORoot.bookDao.addBook(new int[] {999}, "THIS BOOK IS BORING BUT NEW", -1, "NEWBOOK4TW2"));

		//Test 6: multiple authors
		authorIDs = new int[] {a1.getAuthorID(), a2.getAuthorID()};
		assertTrue(DAORoot.bookDao.addBook(authorIDs, "NEW book with multple authors", -1, "NEWBOOK4TW2"));

		
		//Test 7: pre-existing combo of author/title/edition of a book (fails due to DB constraint)
		assertFalse(DAORoot.bookDao.addBook(authorIDs, "NEW book with multple authors", -1, "NEWBOOK4TW2"));

		//Test 8: author/title combo already exists in db but new edition of the book
		assertTrue(DAORoot.bookDao.addBook(authorIDs, "NEW book with multple authors", 1, "NEWBOOK4TW2"));
		
	}
}
