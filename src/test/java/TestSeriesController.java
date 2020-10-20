import static com.BryceBG.DatabaseTools.utils.GlobalConstants.*;

import org.junit.Before;
import org.junit.BeforeClass;

import com.BryceBG.DatabaseTools.utils.Utils;

public class TestSeriesController {
	@BeforeClass
	public static void runOnce() {
		//set up our logger
		com.BryceBG.DatabaseTools.utils.Utils.initializeAppLogger(TEST_LOGGER_OUT_FILE_NAME, TEST_LOGGER_PATTERN);
	}
	
	@Before
	public void beforeTest() {
		//reset changes to database from tests
		testUtils.resetDB(Utils.getConfigString("app.dbname", null)); //reset database to initial state
	}

}
