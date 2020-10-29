package com.BryceBG.DatabaseTools.Database.Series;

import static com.BryceBG.DatabaseTools.Database.DAORoot.userDao;

import org.javatuples.Pair;

import com.BryceBG.DatabaseTools.Database.DAORoot;
import com.BryceBG.DatabaseTools.Database.Author.Author;
import com.BryceBG.DatabaseTools.Database.User.UserController;
import com.BryceBG.DatabaseTools.utils.DaoUtils;

public class SeriesController {
	/**
	 * This function takes a new series name, and an array of pairs representing
	 * authors who wrote the books in the series.
	 * 
	 * @param username    The username of an admin user who is calling this function
	 * @param password    The password for the admin attempting to execute this
	 *                    function
	 * @param series_name The name of the new series
	 * @param authorNames An array of pairs where each pair a tuple of form:
	 *                    (author_first_name, author_last_name)
	 * @return (TRUE, "SUCCESS!") or (FALSE and reason for failure).
	 */
	public static Pair<Boolean, String> createSeries(String username, String password, String series_name,
			Pair<String, String>[] authorNames) {
		// 0. authenticate user performing modification
		if (!(DaoUtils.stringIsOk(username) && DaoUtils.stringIsOk(password)
				&& UserController.authenticate(username, password) && userDao.getUserByUsername(username).isAdmin())) {
			return new Pair<Boolean, String>(Boolean.FALSE, "User performing createSeries is invalid");
		}

		// 1. validate inputs
		// 1.a. validate series name
		if (!DaoUtils.stringIsOk(series_name)) {
			return new Pair<Boolean, String>(Boolean.FALSE, "New series name is invalid.");
		}
		// 1.b ensure at least 1 author for the series was passed in
		if (authorNames == null || authorNames.length == 0) {
			return new Pair<Boolean, String>(Boolean.FALSE,
					"No authors were included for the new series (required field).");
		}
		// 1.c. ensure authors that were passed in exist in our database.
		int primary_author_id = Integer.MAX_VALUE;
		for (Pair<String, String> x : authorNames) {
			if (x == null) {
				return new Pair<Boolean, String>(Boolean.FALSE, "A null author was passed in");
			}
			Author authorX = DAORoot.authorDao.getAuthor(x.getValue0(), x.getValue1());
			if (authorX == null) { // author doesn't exist
				return new Pair<Boolean, String>(Boolean.FALSE,
						String.format("Author - First Name: %s Last Name: %s - is not a valid author. Either add the author to the database or correct the spelling of the author", x.getValue0(), x.getValue1()));
			} else {
				if (authorX.getAuthorID() < primary_author_id)
					primary_author_id = authorX.getAuthorID();
			}
		}

		// 2. format series_name
		// TODO maybe Capitalize? but some series may have lowercase words

		// 3. ensure series doesn't already exist
		if (DAORoot.seriesDao.getSeriesByNameAndAuthorID(series_name, primary_author_id) != null) {
			return new Pair<Boolean, String>(Boolean.FALSE, "Series already exists in database.");
		}

		// 4. call addSeries()
		boolean rtnedVal = DAORoot.seriesDao.addSeries(series_name, primary_author_id);

		// 5. check returned value to determine if series addition was successful
		if (rtnedVal)
			return new Pair<Boolean, String>(Boolean.TRUE, "Success!");
		else
			return new Pair<Boolean, String>(Boolean.FALSE, "Series addition unexpectedly failed. Please try again.");
	}

	/**
	 * This function takes a series_name and the authors who wrote it. This delete
	 * the series if it exists.
	 * 
	 * @param username    The username of an admin user who is calling this function
	 * @param password    The password for the admin attempting to execute this
	 *                    function
	 * @param series_name The name of the series to delete.
	 * @param authorNames an array of pairs where each pair a tuple of form:
	 *                    (author_first_name, author_last_name)
	 * @return (TRUE, "SUCCESS!") or (FALSE and reason for failure).
	 */
	public static Pair<Boolean, String> removeSeries(String username, String password, String series_name,
			Pair<String, String>[] authorNames) {
		// 0. authenticate user performing modification
		if (!(DaoUtils.stringIsOk(username) && DaoUtils.stringIsOk(password)
				&& UserController.authenticate(username, password) && userDao.getUserByUsername(username).isAdmin())) {
			return new Pair<Boolean, String>(Boolean.FALSE, "User performing removeSeries is invalid");
		}
		// 1. validate inputs
		// 1.a. Validate series name
		if (!DaoUtils.stringIsOk(series_name)) {
			return new Pair<Boolean, String>(Boolean.FALSE, "Series name is invalid");
		}
		// 1.b Ensure at least 1 author for the series was passed in
		if (authorNames == null || authorNames.length == 0) {
			return new Pair<Boolean, String>(Boolean.FALSE, "No authors were included for the series (required field)");
		}
		// 1.c. Ensure that the authors that were passed in exist in our database (and
		// determine which is the primary).
		int primary_author_id = Integer.MAX_VALUE;
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
				if (authorX.getAuthorID() < primary_author_id)
					primary_author_id = authorX.getAuthorID();
			}
		}
		// 2. format series_name
		// TODO maybe Capitalize? but some series may have lower case words
		// 3. ensure series exists
		if (DAORoot.seriesDao.getSeriesByNameAndAuthorID(series_name, primary_author_id) == null) {
			return new Pair<Boolean, String>(Boolean.FALSE,
					"No series from database matches passed in criteria. Please make sure of the series name and authors");
		}
		// 4. call removeSeries()
		boolean rtnedVal = DAORoot.seriesDao.removeSeries(series_name, primary_author_id);
		// 5. check returned value to determine if series addition was successful
		if (rtnedVal)
			return new Pair<Boolean, String>(Boolean.TRUE, "Success!");
		else {
			if (DAORoot.seriesDao.getSeriesByNameAndAuthorID(series_name, primary_author_id)
					.getNumberBooksInSeries() > 0) {
				return new Pair<Boolean, String>(Boolean.FALSE,
						"Series removal failed because there are currently books in the series. Remove the books and then try again.");
			} else {
				return new Pair<Boolean, String>(Boolean.FALSE,
						"Series removal unexpectedly failed. Please try again.");

			}

		}
	}


	/**
	 * This function is designed to allow updates to be applied to a series. For
	 * example, Incrementing or Decrementing the numbers off books associated with a
	 * series to be updated (when a new book is added for example). Or performing updates like changing the series status.
	 * 
	 * @param username        The username of an admin user who is calling this
	 *                        function
	 * @param password        The password for the admin attempting to execute this
	 *                        function
	 * @param series_name     The name for the series we are changing.
	 * @param authorNames     An array of pairs where each pair a tuple of form:
	 *                        (author_first_name, author_last_name)
	 * @param newUpdateType This parameter is used to indicate if you are either
	 *                        incrementing or decrementing the numbers of books in
	 *                        a series. Or changing the status of the series.
	 * @param newStatus This is what (if the update type is STATUS_CHANGE) to set the series status to. It can be left null otherwise
	 */
	public static Pair<Boolean, String> updateSeries(String username, String password, String series_name,
			Pair<String, String>[] authorNames, SeriesDao.UpdateType newUpdateType, Series.series_status_enum newSeriesStatus) {
		// 0. authenticate user performing modification
		if (!(DaoUtils.stringIsOk(username) && DaoUtils.stringIsOk(password)
				&& UserController.authenticate(username, password) && userDao.getUserByUsername(username).isAdmin())) {
			return new Pair<Boolean, String>(Boolean.FALSE, "User performing update is invalid");
		}
		// 1. validate inputs
		// 1.a. validate series_name
		if (!DaoUtils.stringIsOk(series_name)) {
			return new Pair<Boolean, String>(Boolean.FALSE, "Series name is invalid.");
		}
		// 1.b ensure type of update is not null
		if (newUpdateType == null) {
			return new Pair<Boolean, String>(Boolean.FALSE,
					"Indicator for type of series update is invalid.");
		}
		// 1.c ensure at least 1 author for the series was passed in
		if (authorNames == null || authorNames.length == 0) {
			return new Pair<Boolean, String>(Boolean.FALSE,
					"No authors were included for the series (required field).");
		}
		// 1.d. Ensure authors that were passed in exist in our database.
		int primary_author_id = Integer.MAX_VALUE;
		for (Pair<String, String> x : authorNames) {
			if(x==null) {
				return new Pair<Boolean, String>(Boolean.FALSE, "A null author was passed in");
			}
			Author authorX = DAORoot.authorDao.getAuthor(x.getValue0(), x.getValue1());
			if (authorX == null) { // author doesn't exist
				return new Pair<Boolean, String>(Boolean.FALSE,
						String.format("Author - First Name: %s Last Name: %s - is not a valid author. Either add the author to the database or correct the spelling of the author",x.getValue0(), x.getValue1()));
			} else {
				if (authorX.getAuthorID() < primary_author_id)
					primary_author_id = authorX.getAuthorID();
			}
		}

		// 2. format series_name
		// TODO maybe Capitalize? but some series may have lowercase words

		// 3. Ensure series exists
		if (DAORoot.seriesDao.getSeriesByNameAndAuthorID(series_name, primary_author_id) == null) {
			return new Pair<Boolean, String>(Boolean.FALSE, "No series from database matches passed in criteria.");
		}

		// 4. call appropriate function to apply selected type of update to series 
		boolean rtnedVal = false;

		//changing book count for series
		if(newUpdateType.equals(SeriesDao.UpdateType.DEC) || newUpdateType.equals(SeriesDao.UpdateType.INC))
			rtnedVal = DAORoot.seriesDao.updateSeriesBookCount(series_name, primary_author_id, newUpdateType);
		else {//changing series status
			if (newSeriesStatus == null) {
				return new Pair<Boolean, String>(Boolean.FALSE, "New series status is invalid (required field for this type of update).");
			}
			rtnedVal = DAORoot.seriesDao.setSeriesStatus(series_name, primary_author_id, newSeriesStatus);
		}
		
			
		// 5. check returned value to determine if series book count was updated
		// successfully
		if (rtnedVal)
			return new Pair<Boolean, String>(Boolean.TRUE, "Success!");
		else
			return new Pair<Boolean, String>(Boolean.FALSE,
					"Series update has unexpectedly failed to be updated. Please try again.");
	}

}
