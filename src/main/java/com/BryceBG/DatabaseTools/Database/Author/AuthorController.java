package com.BryceBG.DatabaseTools.Database.Author;

import org.apache.commons.text.WordUtils;

import org.javatuples.Pair;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.User.User;
import com.BryceBG.DatabaseTools.Database.User.UserController;
import com.BryceBG.DatabaseTools.utils.DaoUtils;
import com.BryceBG.DatabaseTools.utils.GlobalConstants;

import static com.BryceBG.DatabaseTools.Database.DAORoot.*;

/**
 * This class acts as an interface to manipulate data related to authors.
 * 
 * @author Bryce-BG
 *
 */
public class AuthorController {

	public static Pair<Boolean, String> createAuthor(String username, String password, String authorFName,
			String authorLName) {
		Pair<Boolean, String> rtVal = new Pair<Boolean, String>(Boolean.FALSE, "");

		// 1. ensure user exists and has correct password
		if (!UserController.authenticate(username, password))
			return rtVal.setAt1(GlobalConstants.MSG_INVALID_USER);

		// 1.b ensure user has permission to perform operation
		if (userDao.getUserByUsername(username).isAdmin() == false)
			return rtVal.setAt1(GlobalConstants.MSG_INVALID_USER_PERMISSIONS);

		// 2. validate authorFName and authorLName arn't going to cause issues adding to
		// the database
		if (DaoUtils.stringIsOk(authorFName) && DaoUtils.stringIsOk(authorLName)) {

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
					return new Pair<Boolean, String>(Boolean.TRUE, GlobalConstants.MSG_SUCCESS);
				}
			} else {// user already exists
				return new Pair<Boolean, String>(Boolean.FALSE, "Author is already in database");

			}
		} else { // invalid author first name or last name passed in
			rtVal = rtVal.setAt1("author: first_name or last_name passed in is invalid");
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
	 * @return Either (True, GlobalConstants.MSG_SUCCESS) if successful or (False,
	 *         <reason for failure>)
	 */
	public static Pair<Boolean, String> removeAuthor(String username, String password, String authorFName,
			String authorLName) {
		Pair<Boolean, String> rtVal = new Pair<Boolean, String>(Boolean.FALSE, "");
		// 1. ensure user exists and has correct password
		if (!UserController.authenticate(username, password))
			return rtVal.setAt1(GlobalConstants.MSG_INVALID_USER);

		// 1.b ensure user has permission to perform operation
		if (userDao.getUserByUsername(username).isAdmin() == false)
			return rtVal.setAt1(GlobalConstants.MSG_INVALID_USER_PERMISSIONS);

		// 2. validate authorFName and authorLName arn't going to cause issues adding to
		// the database
		if (DaoUtils.stringIsOk(authorFName) && DaoUtils.stringIsOk(authorLName)) {

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
					return new Pair<Boolean, String>(Boolean.TRUE, GlobalConstants.MSG_SUCCESS);
				}
			} else {// author they want to delete isn't in database
				return new Pair<Boolean, String>(Boolean.FALSE,
						String.format("The author: \"%s %s\" does not exist.", authorFName, authorLName));

			}
		} else { // invalid author first name or last name passed in
			rtVal = rtVal.setAt1("author: first_name or last_name passed in is invalid");
		}

		return rtVal;
	}

	/**
	 * This function is used to set a user account as the "owner" of an author in
	 * the database
	 * 
	 * @param username             Username of the administrator performing the
	 *                             operation
	 * @param password             Password for the administrator performing the
	 *                             operation
	 * @param authorFName          First name of the author we are setting to be
	 *                             owned by the new user
	 * @param authorLName          Last name of the author we are setting to be
	 *                             owned by the new user
	 * @param verifiedUserUsername The user who will "own" the author page selected.
	 * @return Either (True, GlobalConstants.MSG_SUCCESS) if successful or (False,
	 *         <reason for failure>)
	 */
	public static Pair<Boolean, String> verifyAuthorAccount(String username, String password, String authorFName,
			String authorLName, String verifiedUserUsername) {

		Pair<Boolean, String> rtVal = new Pair<Boolean, String>(Boolean.FALSE, "");
		// 1. ensure user exists and has correct password
		if (!UserController.authenticate(username, password))
			return rtVal.setAt1(GlobalConstants.MSG_INVALID_USER);

		// 1.b ensure user has permission to perform operation
		if (userDao.getUserByUsername(username).isAdmin() == false)
			return rtVal.setAt1(GlobalConstants.MSG_INVALID_USER_PERMISSIONS);

		// 2. ensure identifiers for author are not empty strings or null
		if (DaoUtils.stringIsOk(authorFName) && DaoUtils.stringIsOk(authorLName)) {
			// 3. format our fields (trim whitespace and capitalize names)
			authorFName = WordUtils.capitalizeFully(authorFName.strip());
			authorLName = WordUtils.capitalizeFully(authorLName.strip());
			// 4. ensure the author we are trying to update exists
			Author theAuthorToUpdate = authorDao.getAuthor(authorFName, authorLName);
			if (theAuthorToUpdate != null) {
				// 5. ensure verifiedUser exists in db
				if (DaoUtils.stringIsOk(verifiedUserUsername)
						&& userDao.getUserByUsername(verifiedUserUsername) != null) {
					// 6. perform update
					boolean rtnedVal = DAORoot.authorDao.setVerifiedUserID(theAuthorToUpdate.getFirstName(),
							theAuthorToUpdate.getLastName(), verifiedUserUsername);
					if (rtnedVal == false) {
						return new Pair<Boolean, String>(Boolean.FALSE,
								"Updating author's verified_user status unexpectedly failed. Please try again.");
					} else {
						return new Pair<Boolean, String>(Boolean.TRUE, GlobalConstants.MSG_SUCCESS);
					}
				} else {
					return new Pair<Boolean, String>(Boolean.FALSE,
							"User attempting to verify as author does not exist!");
				}
			} else {
				String authorName = authorFName + " " + authorLName;
				return new Pair<Boolean, String>(Boolean.FALSE,
						String.format("Attempted to update \"%s\". However, that author does not exist.", authorName));
			}
		} else {
			return new Pair<Boolean, String>(Boolean.FALSE, "Passed in author first name or last name was not valid.");

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

		Pair<Boolean, String> rtVal = new Pair<Boolean, String>(Boolean.FALSE, "");
		// 1. ensure user exists and has correct password
		if (!UserController.authenticate(username, password))
			return rtVal.setAt1(GlobalConstants.MSG_INVALID_USER);

		// 1.b ensure user has permission to perform operation
		if (userDao.getUserByUsername(username).isAdmin() == false)
			return rtVal.setAt1(GlobalConstants.MSG_INVALID_USER_PERMISSIONS);

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
					return new Pair<Boolean, String>(Boolean.TRUE, GlobalConstants.MSG_SUCCESS);
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

	}
}
