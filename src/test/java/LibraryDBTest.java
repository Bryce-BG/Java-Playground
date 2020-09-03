
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
		ReadConfig cf = new ReadConfig();
		if(cf.is_valid())//ensure config loaded successfully
		{
		//2. call setters (to ensure database is initialized)
		library = new LibraryDB(cf.get_host(), cf.get_port(), cf.get_name(), cf.get_pass(), cf.get_user());

		}


	}
	//after test delete db
	
	
	@Before
	public void initialize() {
		//do stuff we need to do before tests (just verify db is still there probbaly)
		Credentials yourAdmin = library.login("admin", "admin"); //just a dummy admin inserted for start of the system into DB
		int rv = library.delete_user(yourAdmin, "thief_lord", "testemail@yahoo.com");

	       
	}
	
	/**USER RELATED TESTS**/

	@Test
	public void test_add_user() {
		//can create a normal user
		int rv = library.create_new_user("thief_lord", "Password1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
		assertEquals(0, rv);
	}
	
	// test adding existing user (should fail)
		//TODO implement me after remove_user
	// test failure from various reasons (invalid email, invalid username, etc)
	
	
	//1. test login works for users in db
	@Test
	public void test_login_valid_user() {
		//TODO implement me
		int rv = library.create_new_user("thief_lord", "Password1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
		if(rv == -2 || rv == 0)
		{
		Credentials crv = library.login("thief_lord", "Password1");
		assertTrue(crv.is_valid_credentials());
		}
		else
		{
			assertFalse("failed to create a user in the database", true);
		}
		
		
	}

	//2. test login fails for users not in DB
	@Test
	public void test_invalid_login_via_invalid_username() {
		Credentials creds = library.login("nouser", "");
		assertFalse(creds.is_valid_credentials()); //user should not exist and thus is invalid

	}
	
	//Test login fails for user's with invalid password
	//1. test login works for users in db
	@Test
	public void test_login_invalid_user_password() {
		//TODO implement me
		int rv = library.create_new_user("thief_lord", "Password1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
		if(rv == -2 || rv == 0)
		{
		Credentials crv = library.login("thief_lord", "Password");
		assertFalse(crv.is_valid_credentials());
		}
		else
		{
			assertFalse("failed to create a user in the database", true);
		}
		
		
	}
	
	
	//3. test adding admin users to DB
	//4. test removing user from DB
	
	
	
	@Test
	public void test() {
		

	}

}
