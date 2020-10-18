import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Author.AuthorController;

public class TestAuthorController {
	
	
	@Test
	public void testAddAuthor() {
		//Test 1: add a valid user
		assertTrue(AuthorController.createAuthor("admin", "Password1", "hello", "its me").getValue0().booleanValue());
		
		//Test 2: add invalid user (fails but doesn't crash)
		assertFalse(AuthorController.createAuthor("admin", "Password1", "", "its me").getValue0().booleanValue());
		assertFalse(AuthorController.createAuthor("admin", "Password1", "hi", "").getValue0().booleanValue());
		assertFalse(AuthorController.createAuthor("admin", "Password1", null, "its me").getValue0().booleanValue());
		assertFalse(AuthorController.createAuthor("admin", "Password1", "HI", null).getValue0().booleanValue());
		
	}

}
