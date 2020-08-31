
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.BryceBG.DatabaseTools.LibraryDB;

public class LibraryDBTest {
	
	LibraryDB library;	
	String password = ""; //TODO fill this in when running tests 
	String databaseUsername = "postgres";
	
	@BeforeClass
	public static void onlyOnce() //get username and password ot use for the test.
	{
		//1 createMockDB
		
		//2 add Mock entries 
	}
	//after test delete db
	
	
	@Before
	public void initialize() {
		library = new LibraryDB();
		
		//call setters
		
	       
	}
	//1. test login works for users in db
	//2. test login fails for users not already in DB
	//3. test adding users to DB
	//4. test removing user from DB
	
	
	
	@Test
	public void test() {
		

	}

}
