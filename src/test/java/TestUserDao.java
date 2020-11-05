import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.User.User;

import testUtils.UtilsForTests;

public class TestUserDao {

	@BeforeClass
	public static void runOnce() {
		UtilsForTests.setupForTests();
	}

	@Before
	public void runBeforeTest() {
		UtilsForTests.resetDB(false); // reset database to initial state
	}

	@Test
	public void testGetUserByUsername() {

		// Test 1: ensure existing admin user is found by the getUserByUsername()
		// function.
		User theAdmin = DAORoot.userDao.getUserByUsername("admin");
		// username
		assertEquals("admin", theAdmin.getUsername());

		// first_name
		assertEquals("admin", theAdmin.getFirstName());

		// last_name
		assertEquals("admin", theAdmin.getLastName());

		// email
		assertEquals("admin@email.com", theAdmin.getEmail());

		// is_admin
		assertTrue(theAdmin.isAdmin());
	}

	@Test
	public void testGetUserByUsernameInvalidUser() {
		// Ensure invalid usernames for request returns null but do not crash the
		// program

		// Test 1: not in database
		User notAUser = DAORoot.userDao.getUserByUsername("NOTAUSER");
		assertNull(notAUser);

		// Test2: invalid username: empty
		notAUser = DAORoot.userDao.getUserByUsername("");
		assertNull(notAUser);

		// Test3: invalid username: only whitespace
		notAUser = DAORoot.userDao.getUserByUsername(" ");
		assertNull(notAUser);

		// Test2: invalid username: null
		notAUser = DAORoot.userDao.getUserByUsername(null);
		assertNull(notAUser);
	}

	@Test
	public void testGetUserByEmailAddress() {
		// Test 1: ensure existing admin user is found by the getUserByEmailAddress()
		// function.
		User theAdmin = DAORoot.userDao.getUserByEmailAddress("admin@email.com");
		// username
		assertEquals("admin", theAdmin.getUsername());

		// first_name
		assertEquals("admin", theAdmin.getFirstName());

		// last_name
		assertEquals("admin", theAdmin.getLastName());

		// email
		assertEquals("admin@email.com", theAdmin.getEmail());

		// is_admin
		assertTrue(theAdmin.isAdmin());

	}

	@Test
	public void testGetUserByEmailAddressInvalidUser() {
		// Ensure invalid usernames for request returns null but do not crash the
		// program

		// Test 1: not in database
		User notAUser = DAORoot.userDao.getUserByEmailAddress("NOTAUSER@random.com");
		assertNull(notAUser);

		// Test2: invalid username: empty
		notAUser = DAORoot.userDao.getUserByEmailAddress("");
		assertNull(notAUser);

		// Test3: invalid username: only whitespace
		notAUser = DAORoot.userDao.getUserByEmailAddress(" ");
		assertNull(notAUser);

		// Test2: invalid username: null
		notAUser = DAORoot.userDao.getUserByEmailAddress(null);
		assertNull(notAUser);
	}

	@Test
	public void testGetAllUserNames() {
		// Test 1: ensure existing admin user is found
		ArrayList<String> users = DAORoot.userDao.getAllUserNames();

		assertEquals(2, users.size()); // two users in the system (admin + mock user)

		// other users still exist unaffected.
		assertTrue((users.contains("admin") && users.contains("JamesJoyce")));
	}

	@Test
	public void testChangeUserPassword() {
		// 1. setup
		ArrayList<String> users = DAORoot.userDao.getAllUserNames();
		String theUserUsername = users.get(1);
		User theUser = DAORoot.userDao.getUserByUsername(theUserUsername); // get the user.
		String newSalt = BCrypt.gensalt();
		String newPassword = "this1Tests";
		String newHashedPassword = BCrypt.hashpw(newPassword, newSalt);

		// Test 1: change valid user accounts password (with valid password/salt)
		assertTrue(DAORoot.userDao.changeUserPassword(theUser.getUsername(), newSalt, newHashedPassword));

		User userAfterChange = DAORoot.userDao.getUserByUsername(theUserUsername);
		assertNotNull(userAfterChange);
		assertEquals(newHashedPassword, userAfterChange.getHashedPassword()); // hashed password stored in database
																				// should be the same.

		// Test 2: try changing password on user that does not exist
		String fakeUsername = "FakeUserName";
		assertFalse(DAORoot.userDao.changeUserPassword(fakeUsername, newSalt, newHashedPassword));

		// Test 3: try changing password on empty username
		fakeUsername = "";
		assertFalse(DAORoot.userDao.changeUserPassword(fakeUsername, newSalt, newHashedPassword));

		// Test 4: try changing password on a null user
		fakeUsername = null;
		assertFalse(DAORoot.userDao.changeUserPassword(fakeUsername, newSalt, newHashedPassword));

	}

	@Test
	public void testAddUser() {

		String username = "new_user";
		String salt = BCrypt.gensalt();
		String hashedPassword = BCrypt.hashpw("password", salt);
		String fName = "Code";
		String lName = "Master";
		String email = "codemaster@email.com";
		boolean is_admin = false;

		// Test1: create normal user
		boolean rtnedVal = DAORoot.userDao.addUser(username, hashedPassword, salt, fName, lName, email, is_admin);
		assertTrue(rtnedVal);

		ArrayList<String> users = DAORoot.userDao.getAllUserNames();
		assertTrue(users.contains(username)); // should have the user in the list of all users

		// Test2: create user where it conflicts with sql db specifications (null values
		// for instance)
		// should cause sql exception to be logged but the program continues
		rtnedVal = DAORoot.userDao.addUser(username, hashedPassword, salt, null, lName, email, is_admin);
		assertFalse(rtnedVal);

		// many of the more complex tests for this are handled by the UserContoller
		// class instead
	}

	@Test
	public void testRemoveUser() {
		String username = "new_user";
		String salt = BCrypt.gensalt();
		String hashedPassword = BCrypt.hashpw("password", salt);
		String fName = "Code";
		String lName = "Master";
		String email = "codemaster@email.com";
		boolean is_admin = false;

		boolean rtnedVal = DAORoot.userDao.addUser(username, hashedPassword, salt, fName, lName, email, is_admin);
		assertTrue(rtnedVal);

		ArrayList<String> users = DAORoot.userDao.getAllUserNames();
		// BEFORE remove
		assertEquals(3, users.size());
		assertTrue(users.contains(username)); // should have the user in the list of all users

		assertTrue(DAORoot.userDao.removeUser(username));
		// AFTER remove
		users = DAORoot.userDao.getAllUserNames();
		assertEquals(2, users.size());
		assertFalse(users.contains(username)); // should have the user in the list of all users

	}
}
