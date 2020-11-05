import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.javatuples.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.BryceBG.DatabaseTools.Database.Author.AuthorController;

import testUtils.UtilsForTests;

public class TestAuthorController {

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
	public void testAddAuthor() {
		// Test 1: add a valid author
		Pair<Boolean, String> result = AuthorController.createAuthor("admin", "Password1", "hello", "its me");
		assertTrue(result.getValue1(), result.getValue0().booleanValue());

		// Test 2: add invalid author (fails but doesn't crash)
		// Test 2.a: invalid first name for author
		assertFalse(AuthorController.createAuthor("admin", "Password1", "", "its me").getValue0().booleanValue());
		// Test 2.b: invalid password
		assertFalse(AuthorController.createAuthor("admin", "Password1", "hi", "").getValue0().booleanValue());
		// Test 2.c: null first name for author
		assertFalse(AuthorController.createAuthor("admin", "Password1", null, "its me").getValue0().booleanValue());
		// Test 2.d: null last name.
		assertFalse(AuthorController.createAuthor("admin", "Password1", "HI", null).getValue0().booleanValue());

		// Test 2.e: invalid authentication username
		assertFalse(AuthorController.createAuthor("admn", "Password1", "hello2", "its me").getValue0().booleanValue());
		// Test 2.f: author is already in db
		assertFalse(AuthorController.createAuthor("admin", "Password1", "hello", "its me").getValue0().booleanValue());

	}

	@Test
	public void testRemoveAuthor() {
		// Test 1: add a valid author
		Pair<Boolean, String> result = AuthorController.removeAuthor("admin", "Password1", "James", "Joyce");
		assertTrue(result.getValue1(), result.getValue0().booleanValue());

		// Test 2: add invalid author (fails but doesn't crash)
		// Test 2.a: invalid first name for author
		assertFalse(AuthorController.removeAuthor("admin", "Password1", "", "its me").getValue0().booleanValue());
		// Test 2.b: invalid last name for author
		assertFalse(AuthorController.removeAuthor("admin", "Password1", "hi", "").getValue0().booleanValue());
		// Test 2.c: null first name for author
		assertFalse(AuthorController.removeAuthor("admin", "Password1", null, "its me").getValue0().booleanValue());
		// Test 2.d: null last name for author.
		assertFalse(AuthorController.removeAuthor("admin", "Password1", "HI", null).getValue0().booleanValue());

		// Test 2.e: invalid authentication username
		assertFalse(AuthorController.removeAuthor("admn", "Password1", "hello2", "its me").getValue0().booleanValue());
		// Test 2.f: author is already in database
		assertFalse(AuthorController.removeAuthor("admin", "Password1", "hello", "its me").getValue0().booleanValue());
	}

	@Test
	public void testUpdateAuthorBio() {
		String author_bio = "I live in the woods and love to write!";
		// Test 1: valid update to user bio
		assertTrue(AuthorController.updateAuthorBio("admin", "Password1", "James", "Joyce", author_bio).getValue0()
				.booleanValue());

		// Test 2: preform invalid updates (fails but doesn't crash)
		// Test 2.a: empty bio update (should succeed)
		assertTrue(AuthorController.updateAuthorBio("admin", "Password1", "James", "Joyce", author_bio).getValue0()
				.booleanValue());

		// Test 2.b: misspelled identifier (jmes)
		assertFalse(AuthorController.updateAuthorBio("admin", "Password1", "jmes", "Joyce", author_bio).getValue0()
				.booleanValue());
		// Test 2.c: null author first name field
		assertFalse(AuthorController.updateAuthorBio("admin", "Password1", null, "Joyce", author_bio).getValue0()
				.booleanValue());
		// Test 2.d: empty first author first name
		assertFalse(AuthorController.updateAuthorBio("admin", "Password1", "", "Joyce", author_bio).getValue0()
				.booleanValue());

		// Test 2.e: invalid authentication username
		assertFalse(AuthorController.updateAuthorBio("admn", "Password1", null, "Joyce", author_bio).getValue0()
				.booleanValue());
		// Test 2.f: invalid password
		assertFalse(AuthorController.updateAuthorBio("admin", "Password", null, "Joyce", author_bio).getValue0()
				.booleanValue());
		// Test 2.g: user preforming update who isn't the verified_owner or an admin
		assertFalse(AuthorController.updateAuthorBio("JamesJoyce", "Password1", "James", "Joyce", author_bio)
				.getValue0().booleanValue());

	}

	@Test
	public void testVerifyAuthorAccount() {
		// Test 1: valid update to user verified_id
		assertTrue(AuthorController.verifyAuthorAccount("admin", "Password1", "James", "Joyce", "JamesJoyce")
				.getValue0().booleanValue());

		// Test 2: preform invalid updates (fails but doesn't crash)
		// Test 2.a: empty username for validated_user update (should fail)
		assertFalse(AuthorController.verifyAuthorAccount("admin", "Password1", "James", "Joyce", "").getValue0()
				.booleanValue());

		// Test 2.b: misspelled identifier for the author (james)
		assertFalse(AuthorController.verifyAuthorAccount("admin", "Password1", "jaes", "Joyce", "JamesJoyce")
				.getValue0().booleanValue());
		// Test 2.c: null author first name field
		assertFalse(AuthorController.verifyAuthorAccount("admin", "Password1", null, "Joyce", "JamesJoyce").getValue0()
				.booleanValue());
		// Test 2.d: empty first author first name
		assertFalse(AuthorController.verifyAuthorAccount("admin", "Password1", "", "Joyce", "JamesJoyce").getValue0()
				.booleanValue());

		// Test 2.e: invalid authentication username
		assertFalse(AuthorController.verifyAuthorAccount("admn", "Password1", "James", "Joyce", "JamesJoyce")
				.getValue0().booleanValue());
		// Test 2.f: invalid password
		assertFalse(AuthorController.verifyAuthorAccount("admin", "Password", "James", "Joyce", "JamesJoyce")
				.getValue0().booleanValue());
		// Test 2.g: user preforming update isn't an admin
		assertFalse(AuthorController.verifyAuthorAccount("JamesJoyce", "Password1", "James", "Joyce", "JamesJoyce")
				.getValue0().booleanValue());
	}
}
