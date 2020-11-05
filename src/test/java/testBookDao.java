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
		assertEquals("Wrong amount of books was returned", 5, books.size());
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

				assertEquals(0, bookX.getEdition());

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

//				bookX.getSeriesID();
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
			} else {
				// TASK5 create book in series
				assertNotEquals(-1, bookX.getSeriesID());

			}
		}

	}
	
	//Dependencies: getAllBooks()
	@Test
	public void testGetBookByID() {
		ArrayList<Book> books = DAORoot.bookDao.getAllBooks();
		for(Book bookX : books) {
			Book bookY = DAORoot.bookDao.getBookByBookID(bookX.getBookID());
			assertEquals(bookX, bookY);
		}
	}
	
	//Dependencies: getAllBooks(), authorDao.getAuthor()
	@Test
	public void testGetBooksByAuthor() {
		Pair<String, String> a1 = new Pair<String, String>("James", "Joyce");

		Author author1 = DAORoot.authorDao.getAuthor(a1.getValue0(), a1.getValue1());
		
		//Test 1: check for author 1
		Book[] b1 = DAORoot.bookDao.getBooksByAuthor(author1.getAuthorID());
		ArrayList<Book> books = DAORoot.bookDao.getAllBooks();
		assertEquals(5, b1.length);
		for(Book bookX : b1) {
			assertTrue(books.contains(bookX));
		}
		
		
		//Test 2: check if author 2 works;
		Pair<String, String> a2 = new Pair<String, String>("Test", "Author2");
		Author author2 = DAORoot.authorDao.getAuthor(a2.getValue0(), a2.getValue1());

		Book[] b2 = DAORoot.bookDao.getBooksByAuthor(author2.getAuthorID());
		assertEquals(1, b2.length); //of test entries he only wrote 1
		for(Book bookX : b2) {
			assertTrue(books.contains(bookX));
		}
		
	}
}
