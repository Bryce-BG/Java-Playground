package tests;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Author.Author;
import com.BryceBG.DatabaseTools.Database.User.User;

import testUtils.UtilsForTests;

public class TestAuthorDao {

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
	public void testGetAllAuthors() {
		ArrayList<Author> authors = DAORoot.authorDao.getAllAuthors();
		assertEquals(2, authors.size());
		Author t = authors.get(0);

		String fName = "James";
		String lName = "Joyce";
		// Test 1: get user inserted with mock data at DB creation.
		assertNotNull(t);
		assertEquals("First name obtained was not the correct value", fName, t.getFirstName());
		assertEquals("Last name obtained was not the correct value", lName, t.getLastName());
		assertEquals("Author bib was not what it should have been", "TEST AUTHOR", t.getAuthorBib());
	}

	@Test
	public void testGetAuthor() {
		// pre-inserted entry as mock data
		String fName = "James";
		String lName = "Joyce";
		// Test 1: get user inserted with mock data at DB creation.
		Author t = DAORoot.authorDao.getAuthor(fName, lName);
		assertNotNull(t);
		assertEquals("First name obtained was not the correct value", fName, t.getFirstName());
		assertEquals("Last name obtained was not the correct value", lName, t.getLastName());
		assertEquals("Author bib was not what it should have been", "TEST AUTHOR", t.getAuthorBib());
	}

	
	
	@Test
	public void testGetAuthor_FULLNAME() {
		// pre-inserted entry as mock data
		String fName = "James";
		String lName = "Joyce";
		// Test 1: get user inserted with mock data at DB creation.
		Author t = DAORoot.authorDao.getAuthor(fName + " " + lName);
		assertNotNull(t);
		assertEquals("First name obtained was not the correct value", fName, t.getFirstName());
		assertEquals("Last name obtained was not the correct value", lName, t.getLastName());
		assertEquals("Author bib was not what it should have been", "TEST AUTHOR", t.getAuthorBib());
	}

	// DEPENDENCIES: AuthorDao.getAllAuthors()
	@Test
	public void testAddAuthor() {
		// Test 1: add a valid user
		ArrayList<Author> authors = DAORoot.authorDao.getAllAuthors();
		int sizeBefore = authors.size();
		assertTrue(DAORoot.authorDao.addAuthor("hello", "its me"));
		ArrayList<Author> authorsAfter = DAORoot.authorDao.getAllAuthors();
		int sizeAfter = authorsAfter.size();
		assertEquals(sizeBefore + 1, sizeAfter);

		Author t = authorsAfter.get(2); // should be the new item (maybe)
		assertNotNull(t);
		assertEquals("First name obtained was not the correct value", "hello", t.getFirstName());
		assertEquals("Last name obtained was not the correct value", "its me", t.getLastName());
		assertNull("Author bib was not what it should have been", t.getAuthorBib());

	}

	// DEPENDENCIES: AuthorDao.getAuthor()
	@Test
	public void addAuthorBib() {
		String fName = "James";
		String lName = "Joyce";
		String newBib = "This is a new bio for author James Joyce";

		// Test 1: does the new bio show up when we get user back.
		DAORoot.authorDao.addAuthorBib(fName, lName, newBib);

		Author t = DAORoot.authorDao.getAuthor(fName, lName);
		assertEquals(newBib, t.getAuthorBib());

		// Test 2: try to add author bib for author who does not exist.
		assertFalse(DAORoot.authorDao.addAuthorBib("IDon'tExist", lName, newBib));

	}

	// DEPENDENCIES: AuthorDao.getAuthor()
	@Test
	public void testRemoveAuthorByID() {

		String fName = "james";
		String lName = "joyce";
		// Test 1: remove author
		Author author = DAORoot.authorDao.getAuthor(fName, lName); // works before
		assertNotNull(author);
		assertTrue(DAORoot.authorDao.removeAuthor(author.getAuthorID()));
		assertNull(DAORoot.authorDao.getAuthor(fName, lName)); // shouldn't Afterwards

	}

	// DEPENDENCIES: AuthorDao.getAuthor()
	@Test
	public void testRemoveAuthorByFNameLName() {
		String fName = "james";
		String lName = "joyce";
		// Test 1: remove author should work (even with capitalization wrong)
		Author author = DAORoot.authorDao.getAuthor(fName, lName); // works before
		assertNotNull(author);
		assertTrue(DAORoot.authorDao.removeAuthor(author.getFirstName(), author.getLastName()));
		assertNull(DAORoot.authorDao.getAuthor(fName, lName)); // shouldn't be able to get author after delete
	}

	// DEPENDENCIES: AuthorDao.getAuthor(), UserDao.getUserByUsername()
	@Test
	public void testSetVerifiedUserID() {
		// Test 1: change the verified user of an author
		assertTrue(DAORoot.authorDao.setVerifiedUserID("James", "Joyce", "JamesJoyce"));
		User newOwner = DAORoot.userDao.getUserByUsername("JamesJoyce");
		Author after = DAORoot.authorDao.getAuthor("James", "Joyce");

		assertEquals(newOwner.getUserId(), after.getVerifiedUserID());
		// Test 2: change verified user to an user who doens't exist
		assertFalse(DAORoot.authorDao.setVerifiedUserID("James", "Joyce", "jhonsey"));

		// Test 3: change verified user on an author that doesn't exist
		assertFalse(DAORoot.authorDao.setVerifiedUserID("AuthorFakeFirst", "Joyce", "JamesJoyce"));

	}

	@Test
	public void testDeleteVerifiedOwnerForAuthor() {
		assertTrue(DAORoot.authorDao.setVerifiedUserID("James", "Joyce", "JamesJoyce"));
		User newOwner = DAORoot.userDao.getUserByUsername("JamesJoyce");

		// Test 1: ensure that it is possible to delete "author" users and having the
		// owner_id reset to default admin
		assertTrue(DAORoot.userDao.removeUser(newOwner.getUsername()));
		assertEquals(1, DAORoot.authorDao.getAuthor("James", "Joyce").getVerifiedUserID());// should have reset to 1

	}
}
