package tests;
import static com.BryceBG.DatabaseTools.Database.DAORoot.*;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.BryceBG.DatabaseTools.Database.Genre.Genre;
import com.BryceBG.DatabaseTools.Database.Genre.GenreDao;

import testUtils.UtilsForTests;

public class testGenreDao {

	// global timeout to ensure no issues of endless runtime for tests
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
	public void testGetAllGenreNames() {
		ArrayList<String> genreNames = genreDao.getAllGenreNames();
		assertEquals(2, genreNames.size());
		assertTrue(genreNames.contains("TestGenre1"));
		assertTrue(genreNames.contains("TestGenre2"));

	}

	@Test
	public void testGetAllGenres() {
		ArrayList<Genre> genres = genreDao.getAllGenres();

		assertEquals(2, genres.size());

		for (Genre genreX : genres) {
			if (genreX.getGenreName().equals("TestGenre1")) {
				assertNull(genreX.getParent());
				assertEquals("TestGenre1", genreX.getGenreName());
				assertEquals("this genre is all for tests", genreX.getGenreDescription());
				assertEquals(0, genreX.getKeywords().length);
				assertNull(genreX.getMygdrdsEquiv());

			} else if (genreX.getGenreName().equals("TestGenre2")) {
				assertNull(genreX.getParent());
				assertEquals("TestGenre2", genreX.getGenreName());
				assertEquals("2this genre is all for testing", genreX.getGenreDescription());
				String[] keywords = genreX.getKeywords();

				assertNotNull(keywords);
				assertEquals(2, keywords.length);
				// since ordering can be random we check both possibilities.
				if (keywords[0].equals("Keyword1"))
					assertEquals("Keyword2", keywords[1]);
				else if (keywords[0].equals("Keyword2"))
					assertEquals("Keyword1", keywords[1]);
				else
					assertTrue(false);// the first entry in array was NOT the right value

				assertEquals("test_genre2", genreX.getMygdrdsEquiv());

			}
		}

	}

	@Test
	public void testGetGenre() {

		// Test 1: test null
		assertNull(genreDao.getGenre(null));

		// Test 2: test empty string
		assertNull(genreDao.getGenre(" "));

		// Test 3: test with genre not in database.
		assertNull(genreDao.getGenre("Hello"));

		// Test 4: verify we can correctly get a genre
		String genreName = "TestGenre2";
		assertNotNull(genreDao.getGenre(genreName));

		// Test 5: just ensure the they are not the same for two different genres.
		assertNotEquals(genreDao.getGenre("TestGenre1"), genreDao.getGenre(genreName));

	}

	// DEPENDENCIES getAllGenreNames()
	@Test
	public void testAddGenre() {
		ArrayList<String> genreNamesBefore = genreDao.getAllGenreNames();
		String genreDescription = "This is a progrmatically inserted test genre";
		String genreName = "";
		String genreNameForm = "Programing Books%s";
		String[] keywords = new String[] { "hello", "GoodBye#1" };
		String myGdrdsEquiv = "programing_books";
		String parent = genreNamesBefore.get(0);

		/** test various genre description related conditions **/

		// Test 1: add valid genre will all fields filled out
		genreName = String.format(genreNameForm, 1);
		assertTrue(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));
		assertTrue(genreDao.getAllGenreNames().contains(genreName));

		// Test 2: test and ensure it works with genre description being empty or null
		genreDescription = null;
		genreName = String.format(genreNameForm, 2);
		assertTrue(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));
		assertTrue(genreDao.getAllGenreNames().contains(genreName));

		genreName = String.format(genreNameForm, 3);
		genreDescription = "	";
		assertTrue(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));
		assertTrue(genreDao.getAllGenreNames().contains(genreName));

		// reset field
		genreDescription = "This is a progrmatically inserted test genre";

		/** test various genre_name related conditions **/
		// Test 3: test genre name = null
		genreName = null;
		assertFalse(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));

		// Test 4: genreName is empty
		genreName = " ";
		assertFalse(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));

		// Test 5: genreName already in system
		genreName = String.format(genreNameForm, 3);
		assertFalse(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));

		/** test various keyword related conditions **/
		// Test 6 null Keywords array
		genreName = String.format(genreNameForm, 4);
		keywords = null;
		assertTrue(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));

		// Test 5: keywords is valid array but consists of invalid entries
		genreName = String.format(genreNameForm, 5);
		keywords = new String[] { " ", "	", null };
		assertTrue(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));

		// Test 6: keywords is an empty array
		genreName = String.format(genreNameForm, 6);
		keywords = new String[0];
		assertTrue(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));
		keywords = new String[] { "hello", "GoodBye#1" };

		/** test various myGdrdsEquiv related conditions **/
		// TODO

		/** test various parent related conditions **/
		// Test 7: null parent
		genreName = String.format(genreNameForm, 7);
		parent = null;
		assertTrue(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));

		// Test 8: empty parent
		genreName = String.format(genreNameForm, 8);
		parent = "";
		assertFalse(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));

		// Test 9: parent not in database.
		genreName = String.format(genreNameForm, 9);
		parent = "I DON\"T EXIST";
		assertFalse(genreDao.addGenre(genreDescription, genreName, keywords, myGdrdsEquiv, parent));

	}

	@Test
	public void testRemoveGenre() {
		// Test 1: empty genre name to remove
		assertFalse(genreDao.removeGenre("	"));

		// Test 2: null genre name
		assertFalse(genreDao.removeGenre(null));

		// Test 3: genre not in database
		assertFalse(genreDao.removeGenre("HelloI am a new genre"));

		// Test 4: genre in database
		assertTrue(genreDao.removeGenre("TestGenre2"));
	}

	// Dependencies: getGenre()
	@Test
	public void testEditGenre_Description() {
		String newVal = "OMG the genre description changed";
		String genreName = "TestGenre2";
		// Test 1: change description
		assertTrue(genreDao.editGenre(GenreDao.GENRE_DESCRIPTION, genreName, newVal));
		assertEquals(newVal, genreDao.getGenre(genreName).getGenreDescription());

		// Test 2: empty genre name to edit
		assertFalse(genreDao.editGenre(GenreDao.GENRE_DESCRIPTION, " ", newVal));

		// Test 2: null genre name
		assertFalse(genreDao.editGenre(GenreDao.GENRE_DESCRIPTION, null, newVal));

		// Test 3: genre not in database
		assertFalse(genreDao.editGenre(GenreDao.GENRE_DESCRIPTION, "Hello I don't exist", newVal));

		// newVal tests
		// Test 4: newVal is wrong type
		String[] t = new String[3];
		assertFalse(genreDao.editGenre(GenreDao.GENRE_DESCRIPTION, genreName, t));

		// Test 5: newVal is null
		assertFalse(genreDao.editGenre(GenreDao.GENRE_DESCRIPTION, genreName, null));
	}

	// Dependencies: getGenre()
	@Test
	public void testEditGenre_GenreName() {
		String newVal = "OMG New GenreName";
		String genreName = "TestGenre2";
		// Test 1: change name
		assertTrue(genreDao.editGenre(GenreDao.GENRE_NAME, genreName, newVal));
		assertEquals(newVal, genreDao.getGenre(newVal).getGenreName());
		assertNull(genreDao.getGenre(genreName));

		// Test 2: empty genre name to edit
		assertFalse(genreDao.editGenre(GenreDao.GENRE_NAME, " ", newVal));

		// Test 2: null genre name
		assertFalse(genreDao.editGenre(GenreDao.GENRE_NAME, null, newVal));

		// Test 3: genre not in database
		assertFalse(genreDao.editGenre(GenreDao.GENRE_NAME, "Hello I don't exist", newVal));

		// newVal tests
		// Test 4: newVal is wrong type
		String[] t = new String[3];
		assertFalse(genreDao.editGenre(GenreDao.GENRE_NAME, genreName, t));

		// Test 5: newVal is null
		assertFalse(genreDao.editGenre(GenreDao.GENRE_NAME, genreName, null));

		// Test 6: new val is empty string.
		assertFalse(genreDao.editGenre(GenreDao.GENRE_NAME, genreName, ""));
	}

	// Dependencies: getGenre()
	@Test
	public void testEditGenre_Keywords() {
		String[] newVal = new String[] { "keyword1", "keyword 2" };
		String genreName = "TestGenre2";
		int editField = GenreDao.KEYWORDS;
		// Test 1: change keywords successfully
		assertTrue(genreDao.editGenre(editField, genreName, newVal));
		assertTrue(ArrayUtils.isEquals(newVal, genreDao.getGenre(genreName).getKeywords()));

		// Test 2: empty genre name to edit
		assertFalse(genreDao.editGenre(editField, " ", newVal));

		// Test 2: null genre name
		assertFalse(genreDao.editGenre(editField, null, newVal));

		// Test 3: genre not in database
		assertFalse(genreDao.editGenre(editField, "Hello I don't exist", newVal));

		// newVal tests
		// Test 4: newVal is wrong type
		String t = "String";
		assertFalse(genreDao.editGenre(editField, genreName, t));

		// Test 5: newVal is null
		assertFalse(genreDao.editGenre(editField, genreName, null));

		// Test 6: new val is empty string.
		assertFalse(genreDao.editGenre(editField, genreName, ""));

		// Test 7: new val contains invalid entries.
		assertTrue(genreDao.editGenre(editField, genreName, new String[] { "hello", null, " ", "goodbye" }));
		String[] returnedKeys = genreDao.getGenre(genreName).getKeywords();
		assertEquals(2, returnedKeys.length);
		assertTrue(ArrayUtils.contains(returnedKeys, "hello"));
		assertTrue(ArrayUtils.contains(returnedKeys, "goodbye"));

	}

	// Dependencies: getGenre()
	@Test
	public void testEditGenre_goodReadsEquiv() {
		int editField = GenreDao.MY_GOODREADS_EQUIVALENT;
		String genreName = "TestGenre2";
		String newVal = genreName.toLowerCase();
		// Test 1: change name
		assertTrue(genreDao.editGenre(editField, genreName, newVal));
		assertEquals(newVal, genreDao.getGenre(genreName).getMygdrdsEquiv());

		// Test 2: empty genre name to edit
		assertFalse(genreDao.editGenre(editField, " ", newVal));

		// Test 2: null genre name
		assertFalse(genreDao.editGenre(editField, null, newVal));

		// Test 3: genre not in database
		assertFalse(genreDao.editGenre(editField, "Hello I don't exist", newVal));

		// newVal tests
		// Test 4: newVal is wrong type
		String[] t = new String[3];
		assertFalse(genreDao.editGenre(editField, genreName, t));

		// Test 5: newVal is null
		assertFalse(genreDao.editGenre(editField, genreName, null));

		// Test 6: new val is empty string.
		assertTrue(genreDao.editGenre(editField, genreName, ""));
	}

	// Dependencies: getGenre()
	@Test
	public void testEditGenre_Parent() {
		int editField = GenreDao.PARENT;
		String genreName = "TestGenre2";
		String newVal = "TestGenre1";
		// Test 1: change valid
		assertTrue(genreDao.editGenre(editField, genreName, newVal));
		assertEquals(newVal, genreDao.getGenre(genreName).getParent());

		// Test 2: empty genre name to edit
		assertFalse(genreDao.editGenre(editField, " ", newVal));

		// Test 2: null genre name
		assertFalse(genreDao.editGenre(editField, null, newVal));

		// Test 3: genre not in database
		assertFalse(genreDao.editGenre(editField, "Hello I don't exist", newVal));

		// newVal tests
		// Test 4: newVal is wrong type
		String[] t = new String[3];
		assertFalse(genreDao.editGenre(editField, genreName, t));

		// Test 5: newVal is null
		assertFalse(genreDao.editGenre(editField, genreName, null));

		// Test 6: new val is empty string.
		assertFalse(genreDao.editGenre(editField, genreName, ""));
		
		//Test 7: parent doesn't exist
		assertFalse(genreDao.editGenre(editField, genreName, "TestGenre3"));

		assertFalse(genreDao.editGenre(8, genreName, "TestGenre3"));

	}
}
