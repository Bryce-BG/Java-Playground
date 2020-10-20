
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.BryceBG.DatabaseTools.Database.LibraryDB;
import com.BryceBG.DatabaseTools.utils.Utils;

public class LibraryDBTest {
	
	static LibraryDB library;	

	
	@BeforeClass
	public static void onlyOnce() 
	{
		//initilize library with parameters obtained from  our configuration file.
			library = new LibraryDB(Utils.getConfigString("app.dbhost", null), Utils.getConfigString("app.dbport", null), Utils.getConfigString("app.dbname", null),Utils.getConfigString("app.dbpass", null) , Utils.getConfigString("app.dbuser", null));
	}
	
	
	@Before
	public void initialize() {
		//do stuff we need to do before tests (just verify db is still there probbaly)
//		Credentials yourAdmin = library.login("admin", "admin"); //just a dummy admin inserted for start of the system into DB
//		library.delete_user("thief_lord", "testemail@yahoo.com");

	       
	}
	
	/**USER RELATED TESTS**/

//	@Test
//	public void test_add_user() {
//		//Test1. Ensure you can create new "normal" user user accounts
//		int rv = library.create_new_user("thief_lord", "Password1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
//		assertEquals("failed to create user for test", 0, rv);
//		
//		/**ensure creation fails for accounts already in the system **/
//		//Test2. Ensure you can't create users with same: username as an existing account
//		rv = library.create_new_user("thief_lord", "Password1", "Bodley-Gomes", "Bryce", "test2email@yahoo.com");
//		assertEquals("Creating the duplicate user for the test did not return -3 like expected", -2, rv); //-2 indicates same username in system
//
//		//Test3. Ensure you can't create users with same email as one  already in system
//		rv = library.create_new_user("hellocat", "Password1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
//		assertEquals("Creating the duplicate user for the test did not return -6 like expected", -6, rv); //-6 indicates email is already in system
//
//
//		//Test4. Ensure you can't create users with different capitalization for email
//		rv = library.create_new_user("thief_lordd", "Password1", "Bodley-Gomes", "Bryce", "TestEmail@yahoo.com");
//		assertEquals("Creating the duplicate user for the test did not return -6 like expected", -6, rv); //-6 indicates email is already in system
//
//		//Test5. Ensure you can't create users with different capitalization for pre-existing username
//		rv = library.create_new_user("Thief_Lord", "Password1", "Bodley-Gomes", "Bryce", "test2email@yahoo.com");
//		assertEquals("Creating the duplicate user for the test did not return -3 like expected", -2, rv); //-2 indicates same username in system
//	}
	
	// test adding existing user (should fail)

	
	// test account creation failure from various reasons (invalid email, invalid username, etc)
//	@Test
////	public void test_create_user_fail_on_requirements() {
////		//Test1. password is to short
////		int rv = library.create_new_user("thief_lord", "Pas1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
////		assertEquals("Creation of user account did not fail depsite password being to short", -3, rv); 
////
////		//Test2. password doesn't contain capital letter
////		rv = library.create_new_user("thief_lord", "password1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
////		assertEquals("Creation of user account did not fail depsite password not containing a upercase-letter", -3, rv); 
////
////		//Test3. password doesn't contain lowercase letter
////		rv = library.create_new_user("thief_lord", "PASSWORD1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
////		assertEquals("Creation of user account did not fail depsite password not containing a lowercase-letter", -3, rv); 
////		
////		//Test4. password doesn't contain a number
////		rv = library.create_new_user("thief_lord", "Passwords", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
////		assertEquals("Creation of user account did not fail depsite failing to contain a numerical value.", -3, rv); 
////		
////		//Test5. username is invalid?
////		//TODO implement me
////	}
////	
//	
//	
//
//
//
//	@Test
//	public void test_delete_user() {
//		
//		
//		int rv = library.create_new_user("thief_lord", "Password1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
//		assertEquals("Failed to create user for test", 0, rv);
//		
//		//Test1. Ensure you can delete an email that is in the system
//		rv = library.delete_user("thief_lord", "testemail@yahoo.com");
//		assertEquals("Deleting account should have been successful and it was not", 0, rv);
//		
//		//Test2. Ensure you can't delete accounts where the user is not present 
//		rv = library.delete_user("thief_lord", "testemail@yahoo.com"); //already deleted by Test1.
//		assertEquals("Deleting account should failed and it didn't or failed in a different way", -2, rv);
//		
//		//Test3. Ensure you can't delete accounts if the admin credentials are fake.
//
//		
//		//Test4. Ensure you can't delete users where the email and username don't match up.
//		rv = library.create_new_user("thief_lord", "Password1", "Bodley-Gomes", "Bryce", "testemail@yahoo.com");
//		//incorrect email.
//		rv = library.delete_user("thief_lord", "test2email@yahoo.com"); //already deleted by Test1.
//		assertEquals("Deleting account should failed and it didn't or failed in a different way", -2, rv);
//		//Incorrect username
//		rv = library.delete_user("thief_lord1", "testemail@yahoo.com"); //already deleted by Test1.
//		assertEquals("Deleting account should failed and it didn't or failed in a different way", -2, rv);
//		
//		
//	}
	

	
	//TODO implement this
	//3. test adding admin users to DB (create
	
	
	@Test
	public void test_get_version() {
		//Skeleton test
		assertEquals("0.0.5-SNAPSHOT(not_a_jar)",Utils.getThisJarVersion());
	}

	


}
