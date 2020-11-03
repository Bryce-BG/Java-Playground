package com.BryceBG.DatabaseTools.Database.Author;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.User.User;
import com.BryceBG.DatabaseTools.utils.DaoUtils;

/**
 * This is the base level object for interacting with our author table. Once
 * again: this should be mostly accessed through the AuthorController class to
 * prevent illegal use to our system.
 * 
 * @author Bryce-BG
 *
 */
public class AuthorDao {
	private static final Logger logger = LogManager.getLogger(AuthorDao.class.getName());

	/**
	 * Function to get an author from our database using their first and last name.
	 * 
	 * @param fName Author's first name
	 * @param lName Author's last name
	 * @return An author object representing the author if such an author exists.
	 *         Null otherwise.
	 */
	public Author getAuthor(String fName, String lName) {
		Author rtVal = null;
		String sql = "SELECT * " + "FROM AUTHORS " + "WHERE fname=? AND lname=?";
		// protect against null values
		if (DaoUtils.stringIsOk(fName) && DaoUtils.stringIsOk(lName)) {
			// 0. format author fields passed in
			fName = WordUtils.capitalizeFully(fName.strip());
			lName = WordUtils.capitalizeFully(lName.strip());
			// 1. establish connection to our database
			try (Connection conn = DAORoot.library.connectToDB();
					PreparedStatement pstmt = conn.prepareStatement(sql);) {
				pstmt.setString(1, fName);
				pstmt.setString(2, lName);
				// 2. execute our query.
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) { // 3. check if sql query for author returned an answer.
						// 4. extract results from result set needed to create Author object
						String author_bib = rs.getString("author_bib");
						int author_id = rs.getInt("author_id");
						String fName2 = rs.getString("fname");// Redundant as we have it.
						String lName2 = rs.getString("lname");
						int verified_user_ID = rs.getInt("verified_user_ID");
						// 5. create our return object with the values
						rtVal = new Author(author_id, fName2, lName2, author_bib, verified_user_ID);
					} else {
						logger.info(String.format(
								"The query for (fName: %s, lName: %s) returned null. I.e. no match was found in the database.",
								fName, lName));
					}

				} // end of try-with-resources: result set
			} // end of try-with-resources: connection
				// catch blocks for try-with-resources: connection
			catch (ClassNotFoundException e) {
				logger.error("Exception occured during connectToDB: " + e.getMessage());
			} catch (SQLException e) {
				logger.error("Exception occured during executing SQL statement: " + e.getMessage());
			}
		}
		return rtVal;
	}

	public ArrayList<Author> getAllAuthors() {
		ArrayList<Author> rtVal = new ArrayList<Author>();
		String sql = "SELECT * FROM authors";

		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			// 2. execute our query for series.
			try (ResultSet rs = pstmt.executeQuery()) {
				// 3. loop through records returned to parse our data.
				while (rs.next()) {
					// 4. extract results from result set needed to create Author object

					String author_bib = rs.getString("author_bib");
					int author_id = rs.getInt("author_id");
					String fName = rs.getString("fname");
					String lName = rs.getString("lName");
					int verified_user_ID = rs.getInt("verified_user_ID");

					// 5. create our return object with the values
					rtVal.add(new Author(author_id, fName, lName, author_bib, verified_user_ID));
				}

			} // end of try-with-resources: result set
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	public boolean addAuthor(String fName, String lName) {
		boolean rtVal = false;

		String sql = "INSERT INTO AUTHORS(fname, lname, verified_user_ID) VALUES (?, ?, ?);";
		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setString(1, fName);
			pstmt.setString(2, lName);
			pstmt.setInt(3, 1); // the admin that should never be deleted

			// 2. execute our update for adding author.
			int rs = pstmt.executeUpdate();
			// 3. check if sql update correctly modified 1 row.
			if (rs == 1) {
				// update was successful
				rtVal = true;
			} else {
				logger.info(String.format("The addAuthor() failed: the execute update returned: %i", rs));
				rtVal = false;
			}
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;
	}

	public boolean removeAuthor(int authorID) {
		boolean rtVal = false;
		String sql = "DELETE FROM AUTHORS WHERE author_id=?;";
		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setInt(1, authorID);

			// 2. execute our update for removing author.
			int rs = pstmt.executeUpdate();
			// 3. check if sql update correctly modified 1 row.
			if (rs == 1) {
				// update was successful
				rtVal = true;
			} else {
				logger.info(String.format("The removeAuthor() failed: the execute update returned: %i", rs));
				rtVal = false;
			}
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;

	}

	public boolean removeAuthor(String fName, String lName) {
		boolean rtVal = false;
		String sql = "DELETE FROM AUTHORS WHERE fname=? AND lname=?;";
		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setString(1, fName);
			pstmt.setString(2, lName);

			// 2. execute our update for removing author.
			int rs = pstmt.executeUpdate();
			// 3. check if sql update correctly modified 1 row.
			if (rs == 1) {
				// update was successful
				rtVal = true;
			} else {
				logger.info(String.format("The removeAuthor() failed: the execute update returned: %i", rs));
				rtVal = false;
			}
		} // end of try-with-resources: connection
			// catch blocks for try-with-resources: connection
		catch (ClassNotFoundException e) {
			logger.error("Exception occured during connectToDB: " + e.getMessage());
		} catch (SQLException e) {
			logger.error("Exception occured during executing SQL statement: " + e.getMessage());
		}
		return rtVal;

	}

	public boolean setVerifiedUserID(String fName, String lName, String username) {
		boolean rtVal = false;
		// 0. ensure author and user exist
		Author a = getAuthor(fName, lName);
		User u = DAORoot.userDao.getUserByUsername(username);
		if (a != null && u != null) {
			String sql = "UPDATE authors SET verified_user_ID=? WHERE fname=? AND lname=?";

			// 1. establish connection to our database
			try (Connection conn = DAORoot.library.connectToDB();
					PreparedStatement pstmt = conn.prepareStatement(sql);) {
				// 2. set parameters in the prepared statement
				pstmt.setLong(1, u.getUserId());
				pstmt.setString(2, fName);
				pstmt.setString(3, lName);

				// 3. execute our query.
				int rtUp = pstmt.executeUpdate();

				// 4. check if sql update performed operation and updated only one row.
				if (rtUp == 1) {
					rtVal = true;
				} else {
					logger.warn(String.format("Updating a verified_user_ID has incorrectly updated %s rows", rtUp));
				}
			} // end of try-with-resources: connection
				// catch blocks for try-with-resources: connection
			catch (ClassNotFoundException e) {
				logger.error("Exception occured during connectToDB: " + e.getMessage());
			} catch (SQLException e) {
				logger.error("Exception occured during executing SQL statement: " + e.getMessage());
			}
		} else {
			String au = fName + " " + lName;
			logger.info(String.format(
					"The query for author %s and user %s returned null. I.e. one of these entries does not exist.", au,
					username));
		}
		return rtVal;
	}

	public boolean addAuthorBib(String fName, String lName, String authorBib) {
		boolean rtVal = false;
		// 0. ensure author exists
		if (getAuthor(fName, lName) != null) {
			String sql = "UPDATE authors SET author_bib=? WHERE fname=? AND lname=?";

			// 1. establish connection to our database
			try (Connection conn = DAORoot.library.connectToDB();
					PreparedStatement pstmt = conn.prepareStatement(sql);) {
				// 2. set parameters in the prepared statement
				pstmt.setString(1, authorBib);
				pstmt.setString(2, fName);
				pstmt.setString(3, lName);

				// 3. execute our query.
				int rtUp = pstmt.executeUpdate();

				// 4. check if sql update performed operation and updated only one row.
				if (rtUp == 1) {
					rtVal = true;
				} else {
					logger.warn(String.format("Updating a verified_user_ID has incorrectly updated %s rows", rtUp));
				}
			} // end of try-with-resources: connection
				// catch blocks for try-with-resources: connection
			catch (ClassNotFoundException e) {
				logger.error("Exception occured during connectToDB: " + e.getMessage());
			} catch (SQLException e) {
				logger.error("Exception occured during executing SQL statement: " + e.getMessage());
			}
		} else {
			String au = fName + " " + lName;
			logger.info(String.format("The query for author %s returned null. I.e. author does not exist.", au));
		}
		return rtVal;
	}

//	public boolean addAuthorAlias(int authorID, int aliasID) {
//		return false;
//		TODO in future version of DB where we allow aliases for the authors
//	}

}
