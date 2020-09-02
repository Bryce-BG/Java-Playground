
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.BryceBG.DatabaseTools.Credentials;
import com.BryceBG.DatabaseTools.LibraryDB;

public class LibraryDBTest {
	
	static LibraryDB library;	

	
	@BeforeClass
	public static void onlyOnce() 
	{
		//1. get username and password to use for the test.
		
		//2. call setters (to ensure database is initialized)
		library = new LibraryDB("localHost", "5432", "librarydatabase", "", "postgres");//TODO remove password at every attempt


		//3 createMockDB
		
		//4 add Mock entries 
	}
	//after test delete db
	
	
	@Before
	public void initialize() {
		//do stuff we need to do before tests (just verify db is still there probbaly)
		
		
	       
	}
	
	
	//1. test login fails for users not already in DB
	@Test
	public void test_invalid_login_via_invalid_username() {
		Credentials creds = library.login("nouser", "");
		assertEquals(false, creds.is_valid_credentials()); //user should not exist
		

	}
	//2. test login fails for user's with invalid password
	
	
	
	//1. test login works for users in db
	//3. test adding users to DB
	//4. test removing user from DB
	
	
	
	@Test
	public void test() {
		

	}

}
