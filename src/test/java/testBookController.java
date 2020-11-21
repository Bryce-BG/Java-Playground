import static org.junit.Assert.*;

import java.util.ArrayList;

import org.javatuples.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Book.Book;
import com.BryceBG.DatabaseTools.Database.Book.BookController;
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

		
		
		/*generic authorization and input tests*/
		
		//Test 1: test null username (expectation: fail)
		Pair<Boolean, String> res = BookController.addBook(null, password, title, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());
				
		//Test 2: test empty username (expectation: fail)
		res = BookController.addBook("", password, title, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());
		
		//Test 3: invalid user/password combo (i.e. the wrong password) (expectation: fail)
		res = BookController.addBook(username, "wrong password", title, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());
		
		//Test 4: valid user but NOT an admin (expectation: fail)
		res = BookController.addBook("JamesJoyce", password, title, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER_PERMISSIONS, res.getValue1());
		
		
		/*end generic tests*/
		//Test 4: test null title (expectation: fail)
		res = BookController.addBook(username, password, null, description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Invalid title for new book", res.getValue1());
		
		//Test 5: test empty title (expectation: fail)
		res = BookController.addBook(username, password, "", description, edition, authorNames);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Invalid title for new book", res.getValue1());
		
		//Test 6: test null description (expectation: success)
		res = BookController.addBook(username, password, title, null, edition, authorNames);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
		title = title+"1"; //update title we use so we won't get failures due to existing title in db
		
		//Test 7: test empty description (expectation: success)
		res = BookController.addBook(username, password, title, " ", edition, authorNames);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
		title = title+"2"; //update title we use so we won't get failures due to existing title in db
		
		//Test 8: -edition (expectation: success (set to -1)
		res = BookController.addBook(username, password, title, description, -3, authorNames);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
		title = title+"3"; //update title we use so we won't get failures due to existing title in db
		
		
		//Test 9: null authorNames (expectation: fail)
		res = BookController.addBook(username, password, title, description, edition, null);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("No authors were included for the new Book (required field).", res.getValue1());
		
		//Test 10: authorNames array of length 0 is passed in (expectation: fail)
		Pair<String,String>[] authorNamesx  = new Pair[0];
		res = BookController.addBook(username, password, title, description, edition, authorNamesx);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("No authors were included for the new Book (required field).", res.getValue1());
		
		
		//Test 11: partially null entry in authorNames (expectation: fail)
		
		Pair<String, String>[] authorNames2 = authorNames;
		authorNames2[0] = authorNames2[0].setAt1(null);
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Author - First Name: james Last Name: null - is not a valid author. Either add the author to the database or correct the spelling of the author", res.getValue1());
		
		//Test 12: totally null author in the list
		authorNames2 = authorNames;
		authorNames2[0] = null;
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("A null author was passed in", res.getValue1());
		
		
		//Test 13: author in list is not in database (expectation: fail)
		Pair<String, String> authorNameWrong = new Pair<String, String>("Idont", "Exist");
		authorNames2[0] = authorNameWrong;
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Author - First Name: Idont Last Name: Exist - is not a valid author. Either add the author to the database or correct the spelling of the author", res.getValue1());
		
		//Test 14: test adding book with multiple authors (expectation: success)
		authorNames2 = new Pair[2];
		Pair<String, String> authorName2nd = new Pair<String, String>("Test", "Author2");
		authorNames2[0] = authorName;
		authorNames2[1] = authorName2nd;
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
		
		
		//Test 16: existing book in database (expectation: fail)
		res = BookController.addBook(username, password, title, description, edition, authorNames2);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals("Book already exists in database.", res.getValue1());
		
		
		//Test 17: existing book in database (expectation: fail)
		authorNames = new Pair[1];
		authorNames[0] = authorName;
		res = BookController.addBook(username, password, "TestBook", description, edition, authorNames);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
	}
	
	//DEPENDENCIES: getAllBooks();
	@Test
	public void testRemoveBook() {
/*generic authorization and input tests*/
		ArrayList<Book> booksBefore = DAORoot.bookDao.getAllBooks();
		long book_id = booksBefore.get(0).getBookID();
		
		//Test 1: test null username (expectation: fail)
		Pair<Boolean, String> res = BookController.removeBook(null, password, book_id);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());
				
		//Test 2: test empty username (expectation: fail)
		res = BookController.removeBook("", password, book_id);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());
		
		//Test 3: invalid user/password combo (i.e. the wrong password) (expectation: fail)
		res = BookController.removeBook(username, "wrong password", book_id);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER, res.getValue1());
		
		//Test 4: valid user but NOT an admin (expectation: fail)
		res = BookController.removeBook("JamesJoyce", password, book_id);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_INVALID_USER_PERMISSIONS, res.getValue1());
		
		/*end generic tests*/
		//Test 5: not a book id in the system
		res = BookController.removeBook(username, password, -1);
		assertFalse(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(String.format("book_id: %d. Does not exist", -1), res.getValue1());
		
		//Test 6: successful user/password combo and book_id to remove
		res = BookController.removeBook(username, password, book_id);
		assertTrue(res.getValue1(), res.getValue0().booleanValue());
		assertEquals(GlobalConstants.MSG_SUCCESS, res.getValue1());
	}
}
