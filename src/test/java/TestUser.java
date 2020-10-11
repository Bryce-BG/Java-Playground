import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.BryceBG.DatabaseTools.Database.LibraryDB;
import com.BryceBG.DatabaseTools.Database.User.User;
import com.BryceBG.DatabaseTools.Database.User.UserController;
import com.BryceBG.DatabaseTools.Database.User.UserDao;


public class TestUser {
	
	static LibraryDB library;	

	
	@BeforeClass
	public static void onlyOnce() 
	{
		//initilize library with parameters obtained from  our configuration file.
		//it should auto do this now
//			library = new LibraryDB(Utils.getConfigString("app.dbhost", null), Utils.getConfigString("app.dbport", null), Utils.getConfigString("app.dbname", null),Utils.getConfigString("app.dbpass", null) , Utils.getConfigString("app.dbuser", null));
	}
	
	//###############TEST UserDao
	@Test
	public void testGetUserByUsername() {
		
		//Test 1: ensure existing admin user is found by the getUserByUsername() function.
		User theAdmin = UserDao.getUserByUsername("admin");
		//username
		assertEquals("admin", theAdmin.getUsername());

		//these two tests only work the first test run as future test runs dynamically create salt and so the hashedPassword is different
		//hashedPassword
//		assertEquals("$2a$10$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwFTGujlnEaZXPf/q7vM5wO", theAdmin.getHashedPassword()); //issue currently

		//salt
//		assertEquals("$2a$10$h.dl5J86rGH7I8bD9bZeZe", theAdmin.getSalt());

		//first_name
		assertEquals("admin", theAdmin.getFirstName());

		//last_name
		assertEquals("admin", theAdmin.getLastName());

		//email
		assertEquals("admin@email.com", theAdmin.getEmail());

		//is_admin
		assertTrue(theAdmin.isAdmin());
		
		//Test 2: ensure user not in db returns null but does not crash the program
		User notAUser = UserDao.getUserByUsername("NOTAUSER");
		assertNull(notAUser); 


	}


	//###############TEST UserController
	@Test
	public void testLogin() {
		String username = "admin";
		String password = "password";
		
		
		//Test 1: works for real user with correct password
		assertTrue(UserController.authenticate(username, password));
		
		//Test 2: doesn't work for an invalid user
		assertFalse(UserController.authenticate("NOTAUSER", password));
		
		//Test 3: fails when user exists but passwords don't match
		assertFalse(UserController.authenticate(username, "admin"));
	}
	
	@Test
	public void testsetPassword() {
		String username = "admin";
		String password = "password";
		
		User theAdminBefore = UserDao.getUserByUsername("admin"); 

		//sanity check
		assertTrue(UserController.authenticate(username, password)); //should work with old password

		//Test 1: check that password change works if old password matches (with admin account)
		String newPassword = "admin";
		UserController.setPassword(username, password, newPassword);
		User theAdminAfter = UserDao.getUserByUsername("admin");
		
		assertTrue(UserController.authenticate(username, newPassword)); //should be able to log in with new password
		assertNotEquals("The password hashes are the same which was not expected after a password change", theAdminBefore.getHashedPassword(), theAdminAfter.getHashedPassword());
		assertFalse(UserController.authenticate(username, password)); //Can't log in with the old password.
				
		
		
		//Test 2: password change does NOT work when the old password supplies does not match the one in the system.
		//TODO
		
		//REVERT password to initial state
		UserController.setPassword(username, newPassword, password);

		
		
		
		
		
		
	}
	
//	@Test
//	public void testresetPassword() {
//		String username = "admin";
//		String password = "admin";
//		String newPassword = "password";
//
//		UserController.setPassword(username, password, newPassword);
//
//	}
	
}
