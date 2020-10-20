package com.BryceBG.DatabaseTools.Database.Author;

import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.User.User;
import com.BryceBG.DatabaseTools.Database.User.UserController;
import com.BryceBG.DatabaseTools.utils.DBUtils;
import static com.BryceBG.DatabaseTools.Database.DAORoot.*;

/**
 * This class acts as an interface to manipulate data related to authors.
 * 
 * @author Bryce-BG
 *
 */
public class AuthorController {
	private static final Logger logger = LogManager.getLogger(AuthorController.class.getName());

	public static Pair<Boolean, String> createAuthor(String username, String password, String authorFName,
			String authorLName) {
		Pair<Boolean, String> rtVal = new Pair<Boolean, String>(Boolean.FALSE, "");
		// 1. ensure user has permission to perform operation
		if (UserController.authenticate(username, password) && userDao.getUserByUsername(username).isAdmin()) {

			// 2. validate authorFName and authorLName arn't going to cause issues adding to
			// the database
			if (DBUtils.stringIsOk(authorFName) && DBUtils.stringIsOk(authorLName)) {

				// 3. format our fields (trim whitespace and capitalize names)
				authorFName = WordUtils.capitalizeFully(authorFName.strip());
				authorLName = WordUtils.capitalizeFully(authorLName.strip());

				// 4. ensure author doesn't already exist
				if (authorDao.getAuthor(authorFName, authorLName) == null) {
					// 5. create author
					boolean rtnedVal = authorDao.addAuthor(authorFName, authorLName);
					// 6. return result
					if (rtnedVal == false) {
						return new Pair<Boolean, String>(Boolean.FALSE,
								"Author creation unexpectedly failed. Please try again.");
					} else {
						return new Pair<Boolean, String>(Boolean.TRUE, "SUCCESS!");
					}
				} else {// user already exists
					return new Pair<Boolean, String>(Boolean.FALSE, "Author is already in database");

				}
			} else { // invalid author first name or last name passed in
				rtVal = rtVal.setAt1("author: first_name or last_name passed in is invalid");
			}
		} else {
			rtVal = rtVal.setAt1("User performing delete is invalid");
		}
		return rtVal;

	}

	/**
	 * This function allows the removal of an author from a database by an
	 * authorized party. Performing this action will also remove all associated
	 * series by them (which in turn means the books must be updated).
	 * 
	 * @param username    username of the admin performing the update
	 * @param password    password to the admin account performing the update
	 * @param authorFName first name of the author being removed
	 * @param authorLName last name of the author being removed
	 * @return Either (True, "Success!") or (False, <reason for failure>)
	 */
	public static Pair<Boolean, String> removeAuthor(String username, String password, String authorFName,
			String authorLName) {
		Pair<Boolean, String> rtVal = new Pair<Boolean, String>(Boolean.FALSE, "");
		// 1. ensure user has permission to perform operation
		if (UserController.authenticate(username, password) && userDao.getUserByUsername(username).isAdmin()) {

			// 2. validate authorFName and authorLName arn't going to cause issues adding to
			// the database
			if (DBUtils.stringIsOk(authorFName) && DBUtils.stringIsOk(authorLName)) {

				// 3. format our fields (trim whitespace and capitalize names)
				authorFName = WordUtils.capitalizeFully(authorFName.strip());
				authorLName = WordUtils.capitalizeFully(authorLName.strip());

				// 4. ensure author exists
				if (authorDao.getAuthor(authorFName, authorLName) != null) {
					// 5. delete author
					boolean rtnedVal = authorDao.removeAuthor(authorFName, authorLName);
					// 6. return result
					if (rtnedVal == false) {
						return new Pair<Boolean, String>(Boolean.FALSE,
								"Author deletion unexpectedly failed. Please try again.");
					} else {
						return new Pair<Boolean, String>(Boolean.TRUE, "SUCCESS!");
					}
				} else {// author they want to delete isn't in database
					return new Pair<Boolean, String>(Boolean.FALSE,
							String.format("The author: \"%s %s\" does not exist.", authorFName, authorLName));

				}
			} else { // invalid author first name or last name passed in
				rtVal = rtVal.setAt1("author: first_name or last_name passed in is invalid");
			}
		} else {
			rtVal = rtVal.setAt1("User performing delete is invalid");
		}
		return rtVal;
	}

	public static Pair<Boolean, String> verifyAuthorAccount(String username, String password, String authorFName,
			String authorLName, String verifiedUserUsername) {
		//0. ensure identifiers for author are not empty strings or null
		if (DBUtils.stringIsOk(authorFName) && DBUtils.stringIsOk(authorLName)) {
			// 1. ensure real user is performing update
			if (UserController.authenticate(username, password)) {
				// 2. format our fields (trim whitespace and capitalize names)
				authorFName = WordUtils.capitalizeFully(authorFName.strip());
				authorLName = WordUtils.capitalizeFully(authorLName.strip());
				// 3. ensure the author we are trying to update exists
				Author theAuthorToUpdate = authorDao.getAuthor(authorFName, authorLName);
				if (theAuthorToUpdate != null) {
					// 4. ensure user performing the update is an admin
					User theUpdateUser = userDao.getUserByUsername(username);
					if (theUpdateUser.isAdmin()) {
						//5. ensure verifiedUser exists in db
						if(DBUtils.stringIsOk(verifiedUserUsername) && userDao.getUserByUsername(verifiedUserUsername)!=null) {
							// 6. perform update
							boolean rtnedVal = DAORoot.authorDao.setVerifiedUserID(theAuthorToUpdate.getFirstName(),
									theAuthorToUpdate.getLastName(), verifiedUserUsername);
							if (rtnedVal == false) {
								return new Pair<Boolean, String>(Boolean.FALSE,
										"Updating author's verified_user status unexpectedly failed. Please try again.");
							} else {
								return new Pair<Boolean, String>(Boolean.TRUE, "SUCCESS!");
							}
						}
						else {
							return new Pair<Boolean, String>(Boolean.FALSE, "User attempting to verify as author does not exist!");
						}
					} else {// not a user who can update this author
						return new Pair<Boolean, String>(Boolean.FALSE,
								String.format("User %s lacks permissions modify this author", username));
					}
				} else {
					String authorName = authorFName + " " + authorLName;
					return new Pair<Boolean, String>(Boolean.FALSE, String
							.format("Attempted to update \"%s\". However, that author does not exist.", authorName));
				}
			} else {
				logger.info(String.format("Invalid user attempted to login %s ", username));
				return new Pair<Boolean, String>(Boolean.FALSE,
						"Invalid user to attempt transfer of author ownership.");
			}
		} else {
			return new Pair<Boolean, String>(Boolean.FALSE,
					"Passed in author first name or last name was not valid.");

		}
	}

	/**
	 * Designed to allow a verified_author or admin to update an author item from
	 * DB.
	 * 
	 * @param username      Username of user performing update (must be admin or the
	 *                      user account associated with the author)
	 * @param password      Password of the user performing update.
	 * @param updatedAuthor The updatedAuthor item filled out with desired
	 *                      properties (may just make this a few independent fields
	 *                      so Author can be a protected class.
	 * @return True if successful or False and the reason for failing if update was
	 *         unsuccessful.
	 */
	public static Pair<Boolean, String> updateAuthorBio(String username, String password, String authorFName,
			String authorLName, String author_bio) {
		// 1. ensure real user is performing update
		if (UserController.authenticate(username, password)) {
			// 2. ensure the author we are trying to update exists
			Author theAuthorToUpdate = authorDao.getAuthor(authorFName, authorLName);
			if (theAuthorToUpdate != null) {
				// 3. ensure user performing the update is either: author or an admin
				User theUpdateUser = userDao.getUserByUsername(username);
				if (theUpdateUser.isAdmin() || theUpdateUser.getUserId() == theAuthorToUpdate.getVerifiedUserID()) {
					boolean rtnedVal = DAORoot.authorDao.addAuthorBib(theAuthorToUpdate.getFirstName(),
							theAuthorToUpdate.getLastName(), author_bio);
					if (rtnedVal == false) {
						return new Pair<Boolean, String>(Boolean.FALSE,
								"updating author's bibliography unexpectedly failed. Please try again.");
					} else {
						return new Pair<Boolean, String>(Boolean.TRUE, "SUCCESS!");
					}
				} else {// not a user who can update this author
					return new Pair<Boolean, String>(Boolean.FALSE,
							String.format("User %s lacks permissions modify this author", username));
				}
			} else {
				String authorName = authorFName + " " + authorLName;
				return new Pair<Boolean, String>(Boolean.FALSE, String.format(
						"Attempted to update \"%s\" bibliography. However, that author does not exist.", authorName));
			}
		} else {
			logger.info(String.format("Invalid user (%s) attempted to login", username));
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid user to attempt bibliography update.");
		}

	}
}
