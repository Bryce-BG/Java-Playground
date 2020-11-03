
import static org.junit.Assert.*;

import org.junit.Test;

import com.BryceBG.DatabaseTools.utils.Utils;

public class TestMiscellaneous {

	@Test
	public void test_get_version() {
		/*
		 * Test we are able to get the version of the app correctly. It doesn't actually
		 * return the pom version because it is not a jar when test is run. So instead
		 * we get the default value specified in Utils.java
		 */
		assertEquals("0.0.5-SNAPSHOT(not_a_jar)", Utils.getThisJarVersion());
	}

}
