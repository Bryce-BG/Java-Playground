package testUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.utils.Utils;

public class testUtils {
	static {
		//TODO use our mock database
//		createTestDB();
//		DAORoot.changeDB("localhost", "5432", "libraryTestDatabase", "superSafe1", "postgres");
	}
	public static void main(String[] args) {
//		resetDB(Utils.getConfigString("app.dbname", null));
	}

	public static void createDB() {
		//TODO implement me.
		
	}
	/**
	 * TODO rewrite this all as it is a very brittle/vulnerable way to modify our
	 * database. potential alternatives:
	 * https://stackoverflow.com/questions/2071682/how-to-execute-sql-script-file-in-java
	 * 
	 * @param dbName
	 */
	public static void resetDB(String dbName) {
		File resetFile = new File("resetDBEntries.sql");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(resetFile));
			String line = reader.readLine();
			while (line != null) {
				if (line.strip().startsWith("--") || line.isBlank()) {
				} else {
					DAORoot.library.runRawSQL(line);
//					System.out.print(DAORoot.library.runRawSQL(line) + " : " + line + "\n");
				}
				
				line = reader.readLine();// read next line
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Run this ONCE before running a suite of tests to create our database
	 */
	public static void createTestDB() {
		//TODO implement me
		System.out.println("createTestDB() was called but this function is currently a stub");
	}
}
