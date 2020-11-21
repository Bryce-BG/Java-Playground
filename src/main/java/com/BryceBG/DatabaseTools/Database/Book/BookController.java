package com.BryceBG.DatabaseTools.Database.Book;

import static com.BryceBG.DatabaseTools.Database.DAORoot.userDao;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Author.Author;
import com.BryceBG.DatabaseTools.Database.User.UserController;
import com.BryceBG.DatabaseTools.utils.DaoUtils;
import com.BryceBG.DatabaseTools.utils.GlobalConstants;

/**
 * This class will handle the higher logic for manipulating book related data in
 * our database. It also handles logging modifications made to the books in the
 * database.
 * 
 * This class I implemented differently than most of the other controllers. It
 * is a VERY thin wrapper around the bookDao where most of the verification of
 * data occurs. With the other controllers I shunted most of the data
 * verification to the controller to ensure quick and detailed error catching
 * when something goes wrong.
 * 
 * 
 * @author Bryce-BG
 *
 */
public class BookController {
	private static final Logger logger = LogManager.getLogger(BookController.class.getName());

	/**
	 * Adds a book to our database.
	 * 
	 * @param username    User attempting to perform the add operation (must be an
	 *                    administrator account)
	 * @param password    The password for the admin adding the book to our system
	 * @param title       The title of the new book.
	 * @param description The description of the new book (can be left empty or null
	 *                    if it doesn't have one).
	 * @param edition     the edition of the book. If unknown leave as a negative
	 *                    number
	 * @param authorNames Pairs of the authors <first name, last name> who
	 *                    authored/co-authored the book.
	 * @return <True, Global.SuccessMsg> if book was successfully added to the
	 *         database or <False, reason for failure> if book addition failed.
	 */
	public static Pair<Boolean, String> addBook(String username, String password, String title, String description,
			int edition, Pair<String, String>[] authorNames) {

		// 1. authenticate user and authenticate.
		if (UserController.authenticate(username, password) == false) {
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER);
		}

		// 1.b. ensure they have permissions to perform operation
		if (userDao.getUserByUsername(username).isAdmin() == false)
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER_PERMISSIONS);

		// 1.c ensure at least 1 author for the series was passed in
		if (authorNames == null || authorNames.length == 0) {
			return new Pair<Boolean, String>(Boolean.FALSE,
					"No authors were included for the new Book (required field).");
		}

		// 1.d. ensure authors that were passed in exist in our database.
		ArrayList<Integer> authorIDsArrayList = new ArrayList<Integer>();
		for (Pair<String, String> x : authorNames) {
			if (x == null) {
				return new Pair<Boolean, String>(Boolean.FALSE, "A null author was passed in");
			}
			Author authorX = DAORoot.authorDao.getAuthor(x.getValue0(), x.getValue1());
			if (authorX == null) { // author doesn't exist
				return new Pair<Boolean, String>(Boolean.FALSE, String.format(
						"Author - First Name: %s Last Name: %s - is not a valid author. Either add the author to the database or correct the spelling of the author",
						x.getValue0(), x.getValue1()));
			} else {
				authorIDsArrayList.add(authorX.getAuthorID());
			}
		}

		// 1.e. validate title
		if (DaoUtils.stringIsOk(title) == false)
			return new Pair<Boolean, String>(Boolean.FALSE, "Invalid title for new book");
		title = title.strip();

		// 2. ensure book doesn't already exist
		int[] authorIDs = new int[authorIDsArrayList.size()];
		for (int i = 0; i < authorIDs.length; i++)
			authorIDs[i] = authorIDsArrayList.get(i).intValue();
		int primary_author_id = DaoUtils.findPrimaryAuthor(authorIDs);

		Book[] tempBooks = DAORoot.bookDao.getBooksByAuthor(primary_author_id);
		for (Book bookX : tempBooks) {
			// TODO can't actually check if publisher is the same here as publisher is set
			// post book creation.
			// make sure no book was returned with same (title, edition, publisher,
			// primary_author_id) combo
			if (bookX.getTitle().equalsIgnoreCase(title) && bookX.getEdition() == edition
					&& "".equals(bookX.getPublisher())) {
				return new Pair<Boolean, String>(Boolean.FALSE, "Book already exists in database.");
			}
		}

		// 3. call dao to insert book into database()
		boolean rtnedVal = DAORoot.bookDao.insertBookIntoDB(authorIDs, description, edition, title);

		// 5. check returned value to determine if book addition was successful
		if (rtnedVal)
			return new Pair<Boolean, String>(Boolean.TRUE, GlobalConstants.MSG_SUCCESS);
		else
			return new Pair<Boolean, String>(Boolean.FALSE, "Book addition unexpectedly failed. Please try again.");

	}

	/**
	 * A function to remove a book from the database
	 * 
	 * @param username User attempting to perform the add operation (must be an
	 *                 administrator account)
	 * @param password The password for the admin adding the book to our system
	 * @param book_id  ID of the book we are removing from the database.
	 * @return <True, GlobalConstants.MSG_SUCCESS> if operation was successful.
	 *         Otherwise returns <False, reason_for_failure>
	 */
	public static Pair<Boolean, String> removeBook(String username, String password, long book_id) {
		// 1. authenticate user and authenticate.
		if (UserController.authenticate(username, password) == false) {
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER);
		}
		// 1.b. ensure they have permissions to perform operation
		if (userDao.getUserByUsername(username).isAdmin() == false)
			return new Pair<Boolean, String>(Boolean.FALSE, GlobalConstants.MSG_INVALID_USER_PERMISSIONS);

		// 2. attempt to remove book.
		boolean res = DAORoot.bookDao.removeBook(book_id);

		// 3. determine success or failure of operation
		if (res)
			return new Pair<Boolean, String>(Boolean.TRUE, GlobalConstants.MSG_SUCCESS);
		else {
			// 3.b rather than just return failure, check if we can determine reason for
			// failure (through log or simple checks)
			// failure because of invalid book_id
			if (DAORoot.bookDao.getBookByBookID(book_id) == null) {
				// book id was invalid by checking only IF we fail we reduce runtime for
				// standard cases
				return new Pair<Boolean, String>(Boolean.FALSE, String.format("book_id: %d. Does not exist", book_id));
			}
			// return generic exception
			return new Pair<Boolean, String>(Boolean.FALSE, "Removing book unexpectedly failed.");

		}
	}

	public static Pair<Boolean, String> editBook() {
		logger.warn("editBook was called but this function is currently a stub"); // TODO remove

		// TODO implement me.
		// if type is identifier make sure we verify identifiers

		// strip out duplicate entries (which violates primary key constraint)
		// Set<Pair<String,String>> mySet = new
		// HashSet<Pair<String,String>>(Arrays.asList(newVal));
		// newVal = mySet.toArray(new Pair[mySet.size()]);
		return null;
	}

	public static Pair<Boolean, Book[]> searchBook() {
		logger.warn("searchBook was called but this function is currently a stub"); // TODO remove
		// TODO implement me
		return null;

	}
}
