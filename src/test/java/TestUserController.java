import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.BryceBG.DatabaseTools.Database.LibraryDB;
import com.BryceBG.DatabaseTools.Database.User.User;
import com.BryceBG.DatabaseTools.Database.User.UserController;
import com.BryceBG.DatabaseTools.Database.DAORoot;

public class TestUserController {
	

	

	
	
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
		
		User theAdminBefore = DAORoot.userDao.getUserByUsername("admin"); 

		//sanity check
		assertTrue(UserController.authenticate(username, password)); //should work with old password

		//Test 1: check that password change works if old password matches (with admin account)
		String newPassword = "admin";
		UserController.setPassword(username, password, newPassword);
		User theAdminAfter = DAORoot.userDao.getUserByUsername("admin");
		
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
