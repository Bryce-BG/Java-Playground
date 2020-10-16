import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Series.Series;
import com.BryceBG.DatabaseTools.Database.Series.SeriesDao;

/**
 * Function to test our SeriesDao object. The order of the tests is intentional
 * so that getters which are used in later tests are tested first to ensure the
 * later tests are meaningful. Unfortunately: currently the @BEFORE function has
 * to perform cleanup which uses the removeSeries() function. As such, if the
 * remove function is broken it can lead to other tests failing as well.
 * 
 * @author Limited1
 *
 */
public class TestSeriesDao {
	
	//global timeout to ensure no issues
	@Rule
	public Timeout globalTimeout = Timeout.seconds(2);
	
	@BeforeClass
	public static void runOnce() {
		// set up our logger
		com.BryceBG.DatabaseTools.utils.Utils.initializeAppLogger("test_log.txt", "%d %p %c [%t] function: %M| %m%n");
	}

	@Before
	@After
	public void beforeTest() {
		// DEPENDENCY: removeSeries() and updateSeriesBookCount()
		// cleanup to remove series from DB that our tests create
		// TODO create function that runs our install.sql so none of these dependencies
		// exist for tests.
		ArrayList<Series> t = DAORoot.seriesDao.getAllSeries();
		for (Series x : t) {
			if (x.getSeriesID() != 1) {// ignore our one constant series in system
				String seriesName = x.getSeriesName();
				int authorID = x.getAuthorID();
				// loop until the number of books in series = 0 (only then can it be removed)
				while (DAORoot.seriesDao.updateSeriesBookCount(seriesName, authorID, SeriesDao.UpdateType.DEC)) {
				}
				DAORoot.seriesDao.removeSeries(x.getSeriesName(), x.getAuthorID());
			}
		}
	}

	@Test
	public void testGetAllSeries() {
		ArrayList<Series> t = DAORoot.seriesDao.getAllSeries();

		assertEquals(1, t.size()); // only should be 1 record in system
		Series sO = t.get(0);

		// check that the values were correctly filled
		// NOTE: we don't check series_id as other tests will remove it and as it is
		// never reused, it is hard to statically determine what this value should be
		assertEquals(1, sO.getAuthorID());
		assertEquals(1, sO.getNumberBooksInSeries());
		assertEquals("test series", sO.getSeriesName());
		assertEquals(Series.series_status_enum.COMPLETED, sO.getSeriesStatus());

	}

	@Test
	public void testGetSeriesByNameAndAuthorID() {
		// Test1: test series not in system (should return null but not crash our
		// system).
		Series t = DAORoot.seriesDao.getSeriesByNameAndAuthorID("test", 1);
		assertNull(t);

		// Test 2: test series known to be in system
		Series sO = DAORoot.seriesDao.getSeriesByNameAndAuthorID("test series", 1);
		// check that the values were correctly filled
		assertEquals(1, sO.getAuthorID());
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
		assertEquals(t2.get(0).getAuthorID(), sO.getAuthorID());
		assertEquals(t2.get(0).getNumberBooksInSeries(), sO.getNumberBooksInSeries());
		assertEquals(t2.get(0).getSeriesName(), sO.getSeriesName());
		assertEquals(t2.get(0).getSeriesStatus(), sO.getSeriesStatus());
	}

	@Test
	public void testAddSeries() {

		int count_before = DAORoot.seriesDao.getAllSeries().size();

		// Test 1: add valid series
		assertTrue(DAORoot.seriesDao.addSeries("this is a new series", 1)); // update should have succeeded
		assertEquals(count_before + 1, DAORoot.seriesDao.getAllSeries().size()); // should be 1 more entry

		// Test 2: add invalid series
		// 2.a. null should not crash
		assertFalse(DAORoot.seriesDao.addSeries(null, 1)); // update should fail

		// 2.b. authorID is invalid
		assertFalse(DAORoot.seriesDao.addSeries("legit series", -3)); // update should fail

		// 2.c. series already in system
		assertFalse(DAORoot.seriesDao.addSeries("this is a new series", 1)); // update should fail

		assertEquals(count_before + 1, DAORoot.seriesDao.getAllSeries().size()); // except the first test no new series
																					// were added

	}

	@Test
	public void testRemoveSeries() {

		DAORoot.seriesDao.addSeries("this is a new series", 1);
		assertEquals(2, DAORoot.seriesDao.getAllSeries().size());
		// TEST 1: remove legitimate series by id.
		ArrayList<Series> t = DAORoot.seriesDao.getAllSeries();
		for (Series x : t) {
			if (x.getSeriesID() != 1)
				DAORoot.seriesDao.removeSeries(x.getSeriesName(), x.getAuthorID());
		}
		// should only be 1 series remaining
		assertEquals(1, DAORoot.seriesDao.getAllSeries().size());

		// Test 2: try to remove series ID that doesn't exist
	}

	@Test
	public void testUpdateSeriesBookCount() {
		// Note our function utilizes getSeriesByNameAndAuthorID() function to verify
		// series exists. So there is no need to check if we can trick it with invalid
		// params passed in.
		DAORoot.seriesDao.addSeries("hello world", 1);// add a test series to play with
		ArrayList<Series> t = DAORoot.seriesDao.getAllSeries();
		Series testSeriesObject = t.get(1);

		// Test1: does count increment successfully for series in db
		int numBefore = testSeriesObject.getNumberBooksInSeries();
		// update should succeed
		assertTrue("increment should have been successful but was not", DAORoot.seriesDao.updateSeriesBookCount(testSeriesObject.getSeriesName(),
				testSeriesObject.getAuthorID(), SeriesDao.UpdateType.INC));
		
		Series series_after = DAORoot.seriesDao
				.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(), testSeriesObject.getAuthorID());
		int numAfter = series_after.getNumberBooksInSeries();
		assertEquals(numBefore + 1, numAfter);
		
		//decrement operation tests
		testSeriesObject = series_after;


		// Test 2: try DEC on valid series
		numBefore = numAfter;
		// update should succeed
		assertTrue(DAORoot.seriesDao.updateSeriesBookCount(testSeriesObject.getSeriesName(),
				testSeriesObject.getAuthorID(), SeriesDao.UpdateType.DEC));
		numAfter = DAORoot.seriesDao
				.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(), testSeriesObject.getAuthorID())
				.getNumberBooksInSeries();
		assertEquals(numBefore - 1, numAfter);

		// loop until we hit 0 so we can check for illegal additions (shouldn't require loop at all)
//		while (DAORoot.seriesDao
//				.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(), testSeriesObject.getAuthorID())
//				.getNumberBooksInSeries() > 0) {
//			DAORoot.seriesDao.updateSeriesBookCount(testSeriesObject.getSeriesName(),
//					testSeriesObject.getAuthorID()); // Perform deincrement
//		}
		// Test 3: try DEC (should fail) now that the count reaches 0
		assertFalse(DAORoot.seriesDao.updateSeriesBookCount(testSeriesObject.getSeriesName(),
				testSeriesObject.getAuthorID(), SeriesDao.UpdateType.DEC));

		

	}

	@Test
	public void testSetSeriesStatus() {
		DAORoot.seriesDao.addSeries("hello world", 1);// add a test series to play with
		ArrayList<Series> t = DAORoot.seriesDao.getAllSeries();
		// TODO add new series to work on.
		Series testSeriesObject = t.get(1);

		// Test 1: try if "ongoing" status works
		assertTrue(DAORoot.seriesDao.setSeriesStatus(testSeriesObject.getSeriesName(), testSeriesObject.getAuthorID(),
				Series.series_status_enum.ONGOING));
		Series afterUpdateSeries = DAORoot.seriesDao.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(),
				testSeriesObject.getAuthorID());
		assertEquals(afterUpdateSeries.getSeriesStatus(), Series.series_status_enum.ONGOING);

		// Test 2: try if "undetermined" status works"
		assertTrue(DAORoot.seriesDao.setSeriesStatus(testSeriesObject.getSeriesName(), testSeriesObject.getAuthorID(),
				Series.series_status_enum.UNDETERMINED));
		afterUpdateSeries = DAORoot.seriesDao.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(),
				testSeriesObject.getAuthorID());
		assertEquals(afterUpdateSeries.getSeriesStatus(), Series.series_status_enum.UNDETERMINED);

		// Test 3 try if "completed" status works
		assertTrue(DAORoot.seriesDao.setSeriesStatus(testSeriesObject.getSeriesName(), testSeriesObject.getAuthorID(),
				Series.series_status_enum.COMPLETED));
		afterUpdateSeries = DAORoot.seriesDao.getSeriesByNameAndAuthorID(testSeriesObject.getSeriesName(),
				testSeriesObject.getAuthorID());
		assertEquals(afterUpdateSeries.getSeriesStatus(), Series.series_status_enum.COMPLETED);

	}



};