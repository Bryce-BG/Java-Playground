import static com.BryceBG.DatabaseTools.utils.GlobalConstants.*;
import static org.junit.Assert.*;


import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Series.Series;
import com.BryceBG.DatabaseTools.Database.Series.SeriesDao;

import testUtils.UtilsForTests;

/**
 * Function to test our SeriesDao object. The order of the tests is intentional
 * so that getters which are used in later tests are tested first to ensure the
 * later tests are meaningful. Unfortunately: currently the @BEFORE function has
 * to perform cleanup which uses the removeSeries() function. As such, if the
 * remove function is broken it can lead to other tests failing as well.
 * 
 * @author Bryce-BG
 *
 */
public class TestSeriesDao {

	// global timeout to ensure no issues
	@Rule
	public Timeout globalTimeout = Timeout.seconds(2);

	@BeforeClass
	public static void runOnce() {
		// set up our logger
		com.BryceBG.DatabaseTools.utils.Utils.initializeAppLogger(TEST_LOGGER_OUT_FILE_NAME, TEST_LOGGER_PATTERN);
		UtilsForTests.createTestDB(); //set our tests to run on the mock database
	}

	@Before
	public void beforeTest() {
		UtilsForTests.resetDB(false); // reset database to initial state
	}

	@Test
	public void testGetAllSeries() {
		ArrayList<Series> t = DAORoot.seriesDao.getAllSeries();

		assertEquals(1, t.size()); // only should be 1 record in system
		Series sO = t.get(0);

		// check that the values were correctly filled
		// NOTE: we don't check series_id as other tests will remove it and as it is
		// never reused, it is hard to statically determine what this value should be
//		assertEquals(1, sO.getAuthorID());
		assertEquals(1, sO.getNumberBooksInSeries());
		assertEquals("test series", sO.getSeriesName());
		assertEquals(Series.series_status_enum.COMPLETED, sO.getSeriesStatus());

	}

	@Test
	public void testGetSeriesByNameAndAuthorID() {
		// Test1: test series not in system (should return null but not crash our
		// system).
		int author_id = DAORoot.authorDao.getAllAuthors().get(0).getAuthorID(); // author id of first (and only) author
		Series t = DAORoot.seriesDao.getSeriesByNameAndAuthorID("test", author_id);
		assertNull(t); //because series doesn't exist

		// Test 2: test series known to be in system
		Series sO = DAORoot.seriesDao.getSeriesByNameAndAuthorID("test series", author_id);
		// check that the values were correctly filled
		assertEquals(author_id, sO.getPrimaryAuthorID());
		assertEquals("test series", sO.getSeriesName());
		assertEquals(Series.series_status_enum.COMPLETED, sO.getSeriesStatus());

	}

	@Test
	public void testGetSeriesBySeriesID() {
		// Test1: test series not in system (should return null but not crash our
		// system).
		// NOTE: avoid testing positive values for series_id as if the tests are
		// repeatedly run, the series_id will go up each time a series is added/deleted
		Series t = DAORoot.seriesDao.getSeriesBySeriesID(0);
		assertNull(t);

		t = DAORoot.seriesDao.getSeriesBySeriesID(-3);
		assertNull(t);

		// Test 2: test series known to be in system
		ArrayList<Series> t2 = DAORoot.seriesDao.getAllSeries();

		Series sO = DAORoot.seriesDao.getSeriesBySeriesID(t2.get(0).getSeriesID());
		// check that the values were correctly filled
		assertEquals(t2.get(0).getPrimaryAuthorID(), sO.getPrimaryAuthorID());
		assertEquals(t2.get(0).getNumberBooksInSeries(), sO.getNumberBooksInSeries());
		assertEquals(t2.get(0).getSeriesName(), sO.getSeriesName());
		assertEquals(t2.get(0).getSeriesStatus(), sO.getSeriesStatus());
	}

	@Test
	public void testAddSeries() {

		int count_before = DAORoot.seriesDao.getAllSeries().size();
		int author_id = DAORoot.authorDao.getAllAuthors().get(0).getAuthorID(); // author id of first (and only) author
																				// in our mock db.
		// Test 1: add valid series
		assertTrue(DAORoot.seriesDao.addSeries("this is a new series", author_id)); // update should have succeeded
		assertEquals(count_before + 1, DAORoot.seriesDao.getAllSeries().size()); // should be 1 more entry

		// Test 2: add invalid series
		// 2.a. null should not crash
		assertFalse(DAORoot.seriesDao.addSeries(null, author_id)); // update should fail

		// 2.b. authorID is invalid
		assertFalse(DAORoot.seriesDao.addSeries("legit series", -3)); // update should fail

		// 2.c. series already in system
		assertFalse(DAORoot.seriesDao.addSeries("this is a new series", author_id)); // update should fail

		assertEquals(count_before + 1, DAORoot.seriesDao.getAllSeries().size()); // except the first test no new series
																					// were added

	}

	@Test
	public void testRemoveSeries() {
		int author_id = DAORoot.authorDao.getAllAuthors().get(0).getAuthorID(); // author id of first (and only) author

		DAORoot.seriesDao.addSeries("this is a new series", author_id);
		assertEquals(2, DAORoot.seriesDao.getAllSeries().size());
		// TEST 1: remove legitimate series by id.
		ArrayList<Series> t = DAORoot.seriesDao.getAllSeries();
		for (Series x : t) {
			if (x.getSeriesID() != 1)
				DAORoot.seriesDao.removeSeries(x.getSeriesName(), x.getPrimaryAuthorID());
		}
		// should only be 1 series remaining
		assertEquals(1, DAORoot.seriesDao.getAllSeries().size());

		// Test 2: try to remove series ID that doesn't exist
	}

	@Test
	public void testUpdateSeriesBookCount() {
		// Note our updateSeriesBookCount() function utilizes getSeriesByNameAndAuthorID() function to verify
		// series exists. So there is no need to check if we can trick it with invalid
		// params passed in.
		
		ArrayList<Series> t = DAORoot.seriesDao.getAllSeries();
		Series testSeriesObject = t.get(0);

		// Test1: does count increment successfully for series in db
		int numBefore = testSeriesObject.getNumberBooksInSeries();
		// update should succeed
		assertTrue("increment should have been successful but was not", DAORoot.seriesDao.updateSeriesBookCount(
				testSeriesObject.getSeriesName(), testSeriesObject.getPrimaryAuthorID(), SeriesDao.UpdateType.INC));

		Series series_after = DAORoot.seriesDao.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(),
				testSeriesObject.getPrimaryAuthorID());
		int numAfter = series_after.getNumberBooksInSeries();
		assertEquals(numBefore + 1, numAfter);

		// decrement operation tests
		testSeriesObject = series_after;

		// Test 2: try DEC on valid series
		numBefore = numAfter;
		// update should succeed
		assertTrue(DAORoot.seriesDao.updateSeriesBookCount(testSeriesObject.getSeriesName(),
				testSeriesObject.getPrimaryAuthorID(), SeriesDao.UpdateType.DEC));
		numAfter = DAORoot.seriesDao
				.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(), testSeriesObject.getPrimaryAuthorID())
				.getNumberBooksInSeries();
		assertEquals(numBefore - 1, numAfter);

		// loop until we hit 0 so we can ensure it won't allow updates below 0
		while (DAORoot.seriesDao
				.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(), testSeriesObject.getPrimaryAuthorID())
				.getNumberBooksInSeries() > 0) {
			DAORoot.seriesDao.updateSeriesBookCount(testSeriesObject.getSeriesName(),
					testSeriesObject.getPrimaryAuthorID(), SeriesDao.UpdateType.DEC); // Perform decrement
		}
		// Test 3: try DEC (should fail) now that the count reaches 0
		assertFalse(DAORoot.seriesDao.updateSeriesBookCount(testSeriesObject.getSeriesName(),
				testSeriesObject.getPrimaryAuthorID(), SeriesDao.UpdateType.DEC));

	}

	@Test
	public void testSetSeriesStatus() {
		ArrayList<Series> t = DAORoot.seriesDao.getAllSeries();
		Series testSeriesObject = t.get(0);

		// Test 1: try if "ongoing" status works
		assertTrue(DAORoot.seriesDao.setSeriesStatus(testSeriesObject.getSeriesName(), testSeriesObject.getPrimaryAuthorID(),
				Series.series_status_enum.ONGOING));
		Series afterUpdateSeries = DAORoot.seriesDao.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(),
				testSeriesObject.getPrimaryAuthorID());
		assertEquals(afterUpdateSeries.getSeriesStatus(), Series.series_status_enum.ONGOING);

		// Test 2: try if "undetermined" status works"
		assertTrue(DAORoot.seriesDao.setSeriesStatus(testSeriesObject.getSeriesName(), testSeriesObject.getPrimaryAuthorID(),
				Series.series_status_enum.UNDETERMINED));
		afterUpdateSeries = DAORoot.seriesDao.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(),
				testSeriesObject.getPrimaryAuthorID());
		assertEquals(afterUpdateSeries.getSeriesStatus(), Series.series_status_enum.UNDETERMINED);

		// Test 3 try if "completed" status works
		assertTrue(DAORoot.seriesDao.setSeriesStatus(testSeriesObject.getSeriesName(), testSeriesObject.getPrimaryAuthorID(),
				Series.series_status_enum.COMPLETED));
		afterUpdateSeries = DAORoot.seriesDao.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(),
				testSeriesObject.getPrimaryAuthorID());
		assertEquals(afterUpdateSeries.getSeriesStatus(), Series.series_status_enum.COMPLETED);

	}

};