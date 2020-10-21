import static com.BryceBG.DatabaseTools.utils.GlobalConstants.*;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.javatuples.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Author.Author;
import com.BryceBG.DatabaseTools.Database.Author.AuthorController;
import com.BryceBG.DatabaseTools.Database.Series.Series;
import com.BryceBG.DatabaseTools.Database.Series.Series.series_status_enum;
import com.BryceBG.DatabaseTools.Database.Series.SeriesController;
import com.BryceBG.DatabaseTools.Database.Series.SeriesDao.UpdateType;
import com.BryceBG.DatabaseTools.utils.Utils;

public class TestSeriesController {
	// credentials for a user who can perform any of the actions required for the
	// test on the database.

	final String username = "admin";
	final String password = "Password1";
	// User in our database (capitilization is wrong but it SHOULD be handled by our
	// functions)
	Pair<String, String> authorName = new Pair<String, String>("james", "joyce");

	@BeforeClass
	public static void runOnce() {
		// set up our logger
		com.BryceBG.DatabaseTools.utils.Utils.initializeAppLogger(TEST_LOGGER_OUT_FILE_NAME, TEST_LOGGER_PATTERN);
	}

	@Before
	public void beforeTest() {
		// reset changes to database from tests
		testUtils.resetDB(Utils.getConfigString("app.dbname", null)); // reset database to initial state
	}

	


	// DEPENDENCIES: SeriesDao.getAllSeries(), AuthorController.createAuthor()
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateSeries() {
		// TODO implement more tests with 1 valid author and one invalid author (for
		// various reasons).

		String series_name = "new series";

		Pair<String, String>[] authorNames = new Pair[1];
		authorNames[0] = authorName;

		ArrayList<Series> seriesBefore = DAORoot.seriesDao.getAllSeries();
		// Test 1: create series with valid parameters.
		Pair<Boolean, String> rtnedVal = SeriesController.createSeries(username, password, series_name, authorNames);
		assertTrue(rtnedVal.getValue1(), rtnedVal.getValue0().booleanValue());

		ArrayList<Series> seriesAfter = DAORoot.seriesDao.getAllSeries();
		assertEquals(seriesBefore.size() + 1, seriesAfter.size());
		// ensure it was correctly added.
		boolean contains = false;
		for (Series x : seriesAfter) {
			if (x.getSeriesName().equalsIgnoreCase(series_name)) {
				contains = true;
				break;
			}
		}
		assertTrue(contains); // the new series should have been in the list.

		// Test 2: create series with two authors
		seriesBefore = DAORoot.seriesDao.getAllSeries();

		// 2.1. create new author for our test
		String fName = "hello";
		String lName = "its me";
		series_name = "New series 2";
		assertTrue(AuthorController.createAuthor(username, password, fName, lName).getValue0().booleanValue());

		Pair<String, String> authorName2 = new Pair<String, String>("Hello", "Its Me");
		authorNames = new Pair[2];
		authorNames[0] = authorName;
		authorNames[1] = authorName2;

		Pair<Boolean, String> t = SeriesController.createSeries(username, password, series_name, authorNames);
		assertTrue(t.getValue1(), t.getValue0().booleanValue());
		seriesAfter = DAORoot.seriesDao.getAllSeries();

		assertEquals(seriesBefore.size() + 1, seriesAfter.size());
		// ensure it was correctly added.
		contains = false;
		for (Series x : seriesAfter) {
			if (x.getSeriesName().equalsIgnoreCase(series_name)) {
				contains = true;
				break;
			}
		}
		assertTrue(contains); // the new series should have been in the list.

		// Test 3: test with invalid author (does not exist)
		fName = "Not AN";
		lName = "AUTHOR";

		Pair<String, String> invalidAuthorName = new Pair<String, String>(fName, lName);
		authorNames = new Pair[1];
		authorNames[0] = invalidAuthorName;
		assertFalse(
				SeriesController.createSeries(username, password, series_name, authorNames).getValue0().booleanValue());

		// Test 4: test with empty series name
		authorNames = new Pair[1];
		authorNames[0] = authorName;
		assertFalse(SeriesController.createSeries(username, password, "", authorNames).getValue0().booleanValue());

		// Test 5: test with null series_name
		authorNames = new Pair[1];
		authorNames[0] = authorName;
		assertFalse(SeriesController.createSeries(username, password, null, authorNames).getValue0().booleanValue());

		// Test 6: test with series and author that already exist.
		authorNames = new Pair[1];
		authorNames[0] = authorName;

		assertFalse(
				SeriesController.createSeries(username, password, series_name, authorNames).getValue0().booleanValue());

		// Test 7: invalid user to perform update
		series_name = "test series2";
		authorNames = new Pair[1];
		authorNames[0] = authorName;

		rtnedVal = SeriesController.createSeries(username, "wrong password", series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		String expectedMsg = "User performing createSeries is invalid";
		assertEquals("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1());

		// Test 8: null authorlist
		series_name = "test series2";
		authorNames = null;

		rtnedVal = SeriesController.createSeries(username, password, series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "No authors were included for the new series (required field).";
		assertEquals("method returned but not with the expected message.", rtnedVal.getValue1(), expectedMsg);

		// Test 9: null entry in author list.
		series_name = "test series2";
		authorNames = new Pair[1];
		authorNames[0] = null; // authorName;

		rtnedVal = SeriesController.createSeries(username, password, series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "A null author was passed in";
		assertEquals("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1());

		// Test 10: partially null author(first name for example)
		series_name = "test series2";
		authorNames = new Pair[1];
		authorNames[0] = authorName.setAt0(null);

		rtnedVal = SeriesController.createSeries(username, password, series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = String.format(
				"Author - First Name: %s Last Name: %s - is not a valid author. Either add the author to the database or correct the spelling of the author",
				null, authorName.getValue1());
		assertEquals("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1());

	
		
	}

	// tests for removeSeries() were split up so we could try to do the tests with
	// minimal dependencies (like addSeries()
	@Test
	public void testRemoveSeriesFORNullSN() {
		// Test 1: pass in null for series name
		String series_name = null;
		@SuppressWarnings("unchecked")
		Pair<String, String>[] authorNames = new Pair[1];
		authorNames[0] = authorName;

		Pair<Boolean, String> rtnedVal = SeriesController.removeSeries(username, password, series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		String expectedMsg = "Series name is invalid";
		assertEquals(String.format("method returned but not with the expected message. \n Expected: %s\n Got: %s",
				expectedMsg, rtnedVal.getValue1()), expectedMsg, rtnedVal.getValue1());

	}

	@Test
	public void testRemoveSeriesFORNullAL() {
		// Test 2: pass in null for authorlist
		String series_name = "New Series Name";
		Pair<String, String>[] authorNames = null;

		Pair<Boolean, String> rtnedVal = SeriesController.removeSeries(username, password, series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		String expectedMsg = "No authors were included for the series (required field)";
		assertEquals(String.format("method returned but not with the expected message. \n Expected: %s\n Got: %s",
				expectedMsg, rtnedVal.getValue1()), expectedMsg, rtnedVal.getValue1());
	}

	@Test
	public void testRemoveSeriesFORNullALEntry() {
		// Test 3: pass in null an author in the author list
		String series_name = "hello again";
		@SuppressWarnings("unchecked")
		Pair<String, String>[] authorNames = new Pair[1];
//		authorNames[0] = authorName;

		Pair<Boolean, String> rtnedVal = SeriesController.removeSeries(username, password, series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		String expectedMsg = "A null author was passed in";
		assertEquals(String.format("method returned but not with the expected message. \n Expected: %s\n Got: %s",
				expectedMsg, rtnedVal.getValue1()), expectedMsg, rtnedVal.getValue1());

	}

	@Test
	public void testRemoveSeriesFORPartiallyNullALEntry() {
		// Test 4: pass in null an author in the author list
		String series_name = "hello again";
		@SuppressWarnings("unchecked")
		Pair<String, String>[] authorNames = new Pair[1];
		authorNames[0] = authorName.setAt1(null); // pass in last name being null

		Pair<Boolean, String> rtnedVal = SeriesController.removeSeries(username, password, series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		String expectedMsg = String.format(
				"Author - First Name: %s Last Name: %s - is not a valid author. Either add the author to the database or correct the spelling of the author",
				authorName.getValue0(), null);
		assertEquals(String.format("method returned but not with the expected message. \n Expected: %s\n Got: %s",
				expectedMsg, rtnedVal.getValue1()), expectedMsg, rtnedVal.getValue1());

	}

	@Test
	public void testRemoveSeriesFOREmptySN() {
		// Test 5: pass in "" for series_name
		String series_name = "";
		@SuppressWarnings("unchecked")
		Pair<String, String>[] authorNames = new Pair[1];
		authorNames[0] = authorName;

		Pair<Boolean, String> rtnedVal = SeriesController.removeSeries(username, password, series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		String expectedMsg = "Series name is invalid";
		assertEquals(String.format("method returned but not with the expected message. \n Expected: %s\n Got: %s",
				expectedMsg, rtnedVal.getValue1()), expectedMsg, rtnedVal.getValue1());
	}

	@Test
	public void testRemoveSeriesFORValidSeries() {
		// Test 6: remove valid series

		String series_name = "test series";
		@SuppressWarnings("unchecked")
		Pair<String, String>[] authorNames = new Pair[1];
		authorNames[0] = authorName;

		Pair<Boolean, String> rtnedVal = SeriesController.removeSeries(username, password, series_name, authorNames);
		// even though it is valid user, series_name and authors to perform the delete,
		// the number of books in the series is still > 0 so we fail to remove the
		// series do to DB schema restrictions.
		assertFalse(rtnedVal.getValue0().booleanValue());
		String expectedMsg = "Series removal failed because there are currently books in the series. Remove the books and then try again.";
		assertEquals(String.format("method returned but not with the expected message. \n Expected: %s\n Got: %s",
				expectedMsg, rtnedVal.getValue1()), expectedMsg, rtnedVal.getValue1());

		// Test 7: decrement count and remove series
		Author a = DAORoot.authorDao.getAuthor("James", "Joyce");
		UpdateType updateType = UpdateType.DEC;
		DAORoot.seriesDao.updateSeriesBookCount(series_name, a.getAuthorID(), updateType);
		rtnedVal = SeriesController.removeSeries(username, password, series_name, authorNames);

		assertTrue(rtnedVal.getValue0().booleanValue());
		expectedMsg = "Success!";
		assertEquals(
				String.format("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1()),
				expectedMsg, rtnedVal.getValue1());

	}

	@Test
	public void testRemoveSeriesMISC() {

		// Test 8 invalid user performing removeSeries action.
		String series_name = "test series";
		@SuppressWarnings("unchecked")
		Pair<String, String>[] authorNames = new Pair[1];
		authorNames[0] = authorName;

		Pair<Boolean, String> rtnedVal = SeriesController.removeSeries(username, "wrong password", series_name,
				authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		String expectedMsg = "User performing removeSeries is invalid";
		assertEquals(
				String.format("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1()),
				expectedMsg, rtnedVal.getValue1());

		// Test 9:
		series_name = "not in our database";
		rtnedVal = SeriesController.removeSeries(username, password, series_name, authorNames);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "No series from database matches passed in criteria. Please make sure of the series name and authors";
		assertEquals(
				String.format("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1()),
				expectedMsg, rtnedVal.getValue1());

	}

	// DEPENDENCIES: AuthorDao.getAuthor() AND
	// SeriesDao.getSeriesByNameAndAuthorID()
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateSeries() {
		// Test 1: perform valid dec
		String series_name = "test series";
		Pair<String, String>[] authorNames = new Pair[1];
		authorNames[0] = authorName;
		UpdateType updateType = UpdateType.DEC;

		Pair<Boolean, String> rtnedVal = SeriesController.updateSeries(username, password, series_name, authorNames,
				updateType, null);

		assertTrue(rtnedVal.getValue0().booleanValue());
		String expectedMsg = "Success!";
		assertEquals(
				String.format("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1()),
				expectedMsg, rtnedVal.getValue1());

		// Test 2: perform invalid dec (count is already at 0)
		rtnedVal = SeriesController.updateSeries(username, password, series_name, authorNames, updateType, null);
		assertFalse(rtnedVal.getValue0().booleanValue());

		// Test 3: preform INC operation on series
		updateType = UpdateType.INC;
		rtnedVal = SeriesController.updateSeries(username, password, series_name, authorNames, updateType, null);
		assertTrue(rtnedVal.getValue0().booleanValue());
		
		// Test 4: perform INC operation on series (with invalid user)
		updateType = UpdateType.INC;
		rtnedVal = SeriesController.updateSeries(username, "password wrong", series_name, authorNames, updateType, null);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "User performing update is invalid";
		assertEquals(expectedMsg, rtnedVal.getValue1());

		
		//TESTS RELATING TO CHANGING SERIES STATUS
		series_name = "test series";

		authorNames = new Pair[1];
		authorNames[0] = authorName;
		updateType = UpdateType.STATUS_CHANGE;
		Series.series_status_enum newSeriesStatus = Series.series_status_enum.ONGOING;
		Author author = DAORoot.authorDao.getAuthor("James", "Joyce");

		// Test 1: change series status to ONGOING
		assertTrue(SeriesController.updateSeries(username, password, series_name, authorNames, updateType, newSeriesStatus)
				.getValue0().booleanValue());
		Series s = DAORoot.seriesDao.getSeriesByNameAndAuthorID(series_name, author.getAuthorID());
		assertEquals(newSeriesStatus, s.getSeriesStatus());

		// Test 2: change series status to COMPLETED
		newSeriesStatus = Series.series_status_enum.COMPLETED;
		assertTrue(SeriesController.updateSeries(username, password, series_name, authorNames, updateType, newSeriesStatus)
				.getValue0().booleanValue());
		s = DAORoot.seriesDao.getSeriesByNameAndAuthorID(series_name, author.getAuthorID());
		assertEquals(newSeriesStatus, s.getSeriesStatus());

		// Test 3: change series status to UNDETERMINED
		newSeriesStatus = Series.series_status_enum.UNDETERMINED;
		assertTrue(SeriesController.updateSeries(username, password, series_name, authorNames, updateType, newSeriesStatus)
				.getValue0().booleanValue());
		s = DAORoot.seriesDao.getSeriesByNameAndAuthorID(series_name, author.getAuthorID());
		assertEquals(newSeriesStatus, s.getSeriesStatus());

		// Test 4: set series status to null
		newSeriesStatus = null;
		assertFalse(SeriesController.updateSeries(username, password, series_name, authorNames, updateType, newSeriesStatus)
				.getValue0().booleanValue());
		s = DAORoot.seriesDao.getSeriesByNameAndAuthorID(series_name, author.getAuthorID());
		assertEquals(Series.series_status_enum.UNDETERMINED, s.getSeriesStatus());

		// Test 5: invalid user to perform update
		series_name = "test series";
		authorNames = new Pair[1];
		authorNames[0] = authorName;

		rtnedVal = SeriesController.updateSeries(username, "wrong password", series_name, authorNames, updateType, series_status_enum.ONGOING);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "User performing update is invalid";
		assertEquals("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1());

		// Test 8: null authorlist
		series_name = "test series";
		authorNames = null;

		rtnedVal = SeriesController.updateSeries(username, password, series_name, authorNames, updateType, Series.series_status_enum.ONGOING);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "No authors were included for the series (required field).";
		assertEquals("method returned but not with the expected message.", rtnedVal.getValue1(), expectedMsg);

		// Test 9: null entry in author list.
		series_name = "test series";
		authorNames = new Pair[1];
		authorNames[0] = null; // authorName;

		rtnedVal = SeriesController.updateSeries(username, password, series_name, authorNames, updateType, Series.series_status_enum.ONGOING);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "A null author was passed in";
		assertEquals("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1());

		// Test 10: partially null author(first name for example)
		series_name = "test series";
		authorNames = new Pair[1];
		authorNames[0] = authorName.setAt0(null);

		rtnedVal = SeriesController.updateSeries(username, password, series_name, authorNames, updateType, Series.series_status_enum.ONGOING);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = String.format(
				"Author - First Name: %s Last Name: %s - is not a valid author. Either add the author to the database or correct the spelling of the author",
				null, authorName.getValue1());
		assertEquals("method returned but not with the expected message.", expectedMsg, rtnedVal.getValue1());

		//NEW tests
		//Test 11: test empty series name
		series_name = "";
		authorNames = new Pair[1];
		authorNames[0] = authorName;
		newSeriesStatus = Series.series_status_enum.ONGOING;

		rtnedVal = SeriesController.updateSeries(username, password, series_name, authorNames, updateType, newSeriesStatus);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "Series name is invalid.";
		assertEquals(expectedMsg, rtnedVal.getValue1());
		
		//Test 12: test series not in our database but otherwise valid
		series_name = "not in DB";
		authorNames = new Pair[1];
		authorNames[0] = authorName;
		newSeriesStatus = Series.series_status_enum.ONGOING;

		rtnedVal = SeriesController.updateSeries(username, password, series_name, authorNames, updateType, newSeriesStatus);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "No series from database matches passed in criteria.";
		assertEquals(expectedMsg, rtnedVal.getValue1());
		
		//Test 13: test invalid type of update
		series_name = "test series";
		updateType = null;
		rtnedVal = SeriesController.updateSeries(username, password, series_name, authorNames, updateType, newSeriesStatus);
		assertFalse(rtnedVal.getValue0().booleanValue());
		expectedMsg = "Indicator for type of series update is invalid.";
		assertEquals(expectedMsg, rtnedVal.getValue1());
		
	}

}
