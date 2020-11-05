import static org.junit.Assert.*;

import org.javatuples.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.BryceBG.DatabaseTools.Database.User.User;
import com.BryceBG.DatabaseTools.Database.User.UserController;

import testUtils.UtilsForTests;

import com.BryceBG.DatabaseTools.Database.DAORoot;

/**
 * Tests for our UserController class.
 * 
 * @author Bryce-BG
 *
 */
public class TestUserController {

	@BeforeClass
	public static void runOnce() {
		UtilsForTests.setupForTests();
	}

	@Before
	public void beforeTest() {
		UtilsForTests.resetDB(false); // reset database to initial state
	}

	@Test
	public void testLogin() {
		String username = "admin";
		String password = "Password1";

		// Test 1: works for real user with correct password
		assertTrue(UserController.authenticate(username, password));

		// Test 2: doesn't work for an invalid user
		assertFalse(UserController.authenticate("NOTAUSER", password));

		// Test 3: fails when user exists but passwords don't match
		assertFalse(UserController.authenticate(username, "admin"));

		// Test 4: fails but does not crash when null or empty values are passed in.
		assertFalse(UserController.authenticate(null, "admin"));
		assertFalse(UserController.authenticate(null, null));
		assertFalse(UserController.authenticate("", ""));

	}

	@Test
	public void testSetPassword() {
		String username = "admin";
		String password = "Password1";

		User theAdminBefore = DAORoot.userDao.getUserByUsername("admin");

		// sanity check
		assertTrue(UserController.authenticate(username, password)); // should work with old password

		// Test 1: check that password change works if old password matches (with admin
		// account)
		String newPassword = "adminN0gh";
		UserController.setPassword(username, password, newPassword);

		User theAdminAfter = DAORoot.userDao.getUserByUsername("admin");

		assertTrue(UserController.authenticate(username, newPassword)); // should be able to log in with new password
		assertNotEquals("The password hashes are the same which was not expected after a password change",
				theAdminBefore.getHashedPassword(), theAdminAfter.getHashedPassword());
		assertFalse(UserController.authenticate(username, password)); // Can't log in with the old password.

		// REVERT password to initial state
		assertTrue(UserController.setPassword(username, newPassword, password).getValue0().booleanValue());

		// Test 2: fails to change password it doesn't meet requirements

		// Test 2.a: password is to short
		newPassword = "aD0h";
		assertFalse(UserController.setPassword(username, password, newPassword).getValue0().booleanValue());
		assertFalse(UserController.authenticate(username, newPassword));

		// Test 2.b: password lacks numerical characters
		newPassword = "Passwords";
		assertFalse(UserController.setPassword(username, password, newPassword).getValue0().booleanValue());
		assertFalse(UserController.authenticate(username, newPassword));

		// Test 2.c: password lacks alpha characters
		newPassword = "1234567890";
		assertFalse(UserController.setPassword(username, password, newPassword).getValue0().booleanValue());
		assertFalse(UserController.authenticate(username, newPassword));

		// Test 2.d: password lacks capital characters
		newPassword = "password1";
		assertFalse(UserController.setPassword(username, password, newPassword).getValue0().booleanValue());
		assertFalse(UserController.authenticate(username, newPassword));

		// Test 2.e: password is empty
		newPassword = "";
		assertFalse(UserController.setPassword(username, password, newPassword).getValue0().booleanValue());
		assertFalse(UserController.authenticate(username, newPassword));

		// Test 2.f: password is null
		newPassword = null;
		assertFalse(UserController.setPassword(username, password, newPassword).getValue0().booleanValue());
		assertFalse(UserController.authenticate(username, newPassword));

		// Sanity check: can still login with initial password as none of these have
		// worked
		assertTrue(UserController.authenticate(username, password)); // Can log in with the old password.

	}

	@Test
	public void testCreateUser() {
		String creatingUsername = "admin";
		String creatingUserPass = "Password1";
		String username = "newmember";
		String password = "Password1";
		String fName = "Wallee";
		String lName = "Dora";
		String email = "walleedora@email.com";

		// Test 1: create valid user succeeds
		int countUsersBefore = DAORoot.userDao.getAllUserNames().size();

		Pair<Boolean, String> res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password,
				fName, lName, email, false);
		assertTrue(res.getValue0().booleanValue()); // should have successfully created user.
		assertEquals(countUsersBefore + 1, DAORoot.userDao.getAllUserNames().size()); // our user count has gone up by 1

		helperRemoveUser(username); // remove so we don't have possible conflicts
		// Test 2: create user with invalid(username) fails
		// invalid username (5-30 chars with only alphanumeric and - _ chars

		username = "new"; // to short
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertFalse(res.getValue0().booleanValue()); // should have failed to create the user.

		username = "newqwertyuiopasdfghjkl876cvgyjk"; // to long (31 chars)
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertFalse(res.getValue0().booleanValue()); // should have failed to create the user.

		username = "new member"; // contains whitespace
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertFalse(res.getValue0().booleanValue()); // should have failed to create the user.

		username = "newmember"; // username is taken
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertFalse(res.getValue0().booleanValue()); // should have failed to create the user.

		helperRemoveUser(username);
		// Test 3: create user with invalid(password) fails
		// the same filter function is used for both createNewUser and setPassword so
		// only basic testing is needed here.
		password = "hi";
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertFalse(res.getValue0().booleanValue()); // should have failed to create the user.
		password = "Password1"; // reset field for net test

		// Test 4: create user with invalid(fName) fails
		fName = "hi"; // to short
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertFalse(res.getValue0().booleanValue()); // should have failed to create the user.
		fName = "Wallee"; // reset

		// Test 5: create user with invalid(lName) fails
		lName = "hi";
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertFalse(res.getValue0().booleanValue()); // should have failed to create the user.
		lName = "Dora";
		// Test 6: create user with invalid(email) fails
		email = "whatsup.com"; // invalid email
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertFalse(res.getValue0().booleanValue()); // should have failed to create the user.

		// email already in system
		email = "admin@email.com";
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertFalse(res.getValue0().booleanValue()); // should have failed to create the user.
	}

	@Test
	public void testDeleteUser() {
		// test users;
		User u1;
		User u2;

		String email = "walleedora@email.com";
		String creatingUsername = "admin";
		String creatingUserPass = "Password1";
		String username = "newmember";
		String password = "Password1";
		String fName = "Wallee";
		String lName = "Dora";
		// create user1
		Pair<Boolean, String> res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password,
				fName, lName, email, false);
		assertTrue(res.getValue0().booleanValue()); // should have created the user.
		u1 = DAORoot.userDao.getUserByUsername(username);

		// create user2
		email = "walleedoraTWO@email.com";
		username = "newmember2";
		res = UserController.createNewUser(creatingUsername, creatingUserPass, username, password, fName, lName, email,
				false);
		assertTrue(res.getValue0().booleanValue()); // should have created the user.
		u2 = DAORoot.userDao.getUserByUsername(username);

		// Test 1: can't delete if user exists and we are not admin or owner.
		res = UserController.deleteUser(u2.getUsername(), password, u1.getUsername());
		assertFalse(res.getValue0().booleanValue());
		assertEquals("Invalid user to perform Delete User action on this account", res.getValue1());

		// Test 2 can delete user if user exists and admin or owner
		// 2.a if owner is issuing the delete
		res = UserController.deleteUser(u2.getUsername(), password, u2.getUsername());
		assertTrue(res.getValue0().booleanValue());

		// 2.b if admin is issuing delete
		res = UserController.deleteUser(creatingUsername, creatingUserPass, u1.getUsername());
		assertTrue(res.getValue0().booleanValue());

		// Test 3: can't delete non existent users
		res = UserController.deleteUser(creatingUsername, creatingUserPass, u1.getUsername());
		assertFalse(res.getValue0().booleanValue());

	}

	// helper function for some tests
	public void helperRemoveUser(String username) {
		DAORoot.userDao.removeUser(username); // wipe out users created by other tests
	}
}
