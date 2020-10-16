package com.BryceBG.DatabaseTools.Database.Series;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.BryceBG.DatabaseTools.Database.DAORoot; //for our instantiated objects inheritence


public class SeriesDao {
	private static final Logger logger = LogManager.getLogger(SeriesDao.class.getName());

	/**
	 * enum used by updateSeriesBookCount to indicate if we want to increment (INC) or decrement (DEC) the number of books in a series.
	 * @author Bryce-BG
	 *
	 */
	public enum UpdateType {
		INC, //increment the count
		DEC //decrement the count
	};
	
	
	/**
	 * This function allows the addition of a new series to the database. Should
	 * only be called directly by Series_controller.
	 * 
	 * @param series_name Name for the new series
	 * @param authorID    ID for the author that is writing the series.
	 * @return True if the update succeeds. False if the update failed for some
	 *         reason.
	 */
	public boolean addSeries(String series_name, int authorID) {
		boolean rtVal = false;
		if (series_name != null) {
			series_name = series_name.strip();
			String sql = "INSERT INTO SERIES(series_name, author_id, number_books_in_series, series_status) VALUES (?, ?, ?, ?);";
			// 1. establish connection to our database
			try (Connection conn = DAORoot.library.connectToDB();
					PreparedStatement pstmt = conn.prepareStatement(sql);) {
				pstmt.setString(1, series_name);
				pstmt.setInt(2, authorID);
				pstmt.setInt(3, 0); // as a new series it has no books
				pstmt.setObject(4, Series.series_status_enum.UNDETERMINED.name(), Types.OTHER);

				// 2. execute our update for adding series.
				int rs = pstmt.executeUpdate();
				// 3. check if sql query for series correctly modified 1 row.
				if (rs == 1) {
					// update was successful
					rtVal = true;
				} else {
					logger.info(String.format("The addSeries failed: the execute update returned: %i", rs));
					rtVal = false;
				}
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

	/**
	 * Function to remove series from our database. This will fail if:
	 * number_books_in_series> 0 as to remove it while referenced would cause
	 * consistency issues.
	 * 
	 * @param seriesID The ID of the series we want to remove.
	 * @return True if update succeeds. False otherwise.
	 */
	public boolean removeSeries(String series_name, int authorID) {
		boolean rtVal = false;
		String sql = "DELETE FROM SERIES WHERE series_name=? AND author_id=?;";
		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setString(1, series_name);
			pstmt.setInt(2, authorID);

			// 2. execute our update for removing series.
			int rs = pstmt.executeUpdate();
			// 3. check if sql query for series returned correct answer: should have added 1
			// unless it failed
			// row).
			if (rs == 1) {
				// update was successful
				rtVal = true;
			} else {
				logger.info(String.format("The removeSeries failed: the execute update returned: %d", rs));
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

	/**
	 * Function to get an existing series from our database given the UNIQUE
	 * (seriesName, authorID) parameters.
	 * 
	 * @param seriesName The name of the series we want to look up.
	 * @param authorID   The ID of the author who wrote the series.
	 * @return The series object representing the data from the row located in the
	 *         database. or NULL if no match was found.
	 */
	public Series getSeriesByNameAndAuthorID(String seriesName, int authorID) {
		Series rtVal = null;
		String sql = "SELECT * " + "FROM SERIES " + "WHERE series_name=? AND author_id=?";

		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setString(1, seriesName);
			pstmt.setInt(2, authorID);
			// 2. execute our query for series.
			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) { // 3. check if sql query for series returned an answer.
					// 4. extract results from result set needed to create Series object
					int series_id = rs.getInt("series_id");
					String series_name = rs.getString("series_name");
					int number_books_in_series = rs.getInt("number_books_in_series");
					Series.series_status_enum status = Series.series_status_enum.valueOf(rs.getString("series_status"));
					// 5. create our return object with the values
					rtVal = new Series(series_id, series_name, authorID, number_books_in_series, status);
				} else {
					logger.info(String.format(
							"The query for (series: %s, authorID: %d) returned null. I.e. no match was found in the database.",
							seriesName, authorID));
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

	/**
	 * Function to get an existing series from our database given the PRIMARY KEY:
	 * seriesID
	 * 
	 * @param seriesID The ID of the series to lookup.
	 * @return The series object representing the data from the row located in the
	 *         database. or NULL if no match was found.
	 */
	public Series getSeriesBySeriesID(int seriesID) {
		Series rtVal = null;
		String sql = "SELECT * " + "FROM SERIES " + "WHERE series_id=?";

		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setInt(1, seriesID);
			// 2. execute our query for series.
			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) { // 3. check if sql query for series returned an answer.
					// 4. extract results from result set needed to create Series object
					int authorID = rs.getInt("author_id");
					String series_name = rs.getString("series_name");
					int number_books_in_series = rs.getInt("number_books_in_series");
					Series.series_status_enum status = Series.series_status_enum.valueOf(rs.getString("series_status"));
					// 5. create our return object with the values
					rtVal = new Series(seriesID, series_name, authorID, number_books_in_series, status);
				} else {
					logger.info(String.format(
							"The query for series with ID %d returned null. I.e. no match was found in the database.",
							seriesID));
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

	/**
	 * Function that gets all series currently in the system.
	 * 
	 * @return Arraylist containing all series currently in the database.
	 */
	public ArrayList<Series> getAllSeries() {
		ArrayList<Series> rtVal = new ArrayList<Series>();
		String sql = "SELECT * FROM SERIES";

		// 1. establish connection to our database
		try (Connection conn = DAORoot.library.connectToDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			// 2. execute our query for series.
			try (ResultSet rs = pstmt.executeQuery()) {
				// 3. loop through records returned to parse our data.
				while (rs.next()) {
					// 4. extract results from result set needed to create Series object

					int authorID = rs.getInt("author_id");
					int seriesID = rs.getInt("series_id");
					String series_name = rs.getString("series_name");
					int number_books_in_series = rs.getInt("number_books_in_series");
					Series.series_status_enum status = Series.series_status_enum.valueOf(rs.getString("series_status"));
					// 5. create our return object with the values
					rtVal.add(new Series(seriesID, series_name, authorID, number_books_in_series, status));
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

	/**
	 * This function is to allow the increment and decrement of series.number_books_in_series
	 * count. This should be called whenever a new book is added or removed from a series.
	 * 
	 * @param seriesName The name of the series we are modifying.
	 * @param authorID   The ID of the author who wrote the series.
	 * @return True if update was successful. False otherwise.
	 */
	public boolean updateSeriesBookCount(String seriesName, int authorID, UpdateType updateType) {
		boolean rtVal = false;
		// 0. ensure series exists
		Series theSeries = DAORoot.seriesDao.getSeriesByNameAndAuthorID(seriesName, authorID);
		if (theSeries != null && updateType!=null) {
			String sql = "UPDATE series SET number_books_in_series=? WHERE series_name=? AND author_id=?";

			// 1. establish connection to our database
			try (Connection conn = DAORoot.library.connectToDB();
					PreparedStatement pstmt = conn.prepareStatement(sql);) {
				// 2. set parameters in the prepared statement
		        int updateBy = (updateType.equals(UpdateType.INC)) ? 1 : -1; //should we increment or deincrement
				pstmt.setInt(1, theSeries.getNumberBooksInSeries()+updateBy);
				pstmt.setString(2, seriesName);
				pstmt.setInt(3, authorID);

				// 3. execute our query for to update count.
				int rtUp = pstmt.executeUpdate();

				// 4. check if sql update performed operation and updated only one row.
				if (rtUp == 1) {
					rtVal = true;
				} else {
					logger.warn(
							String.format("Updating the number_books_in_series has incorrectly updated %s rows", rtUp));
				}
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

	/**
	 * This function allows the status of a series in our database to be changed to one of the preset statuses
	 * @param seriesName Name of the series we want to change
	 * @param authorID ID of the author who wrote the series.
	 * @param seriesStatus New status we want to set the series to
	 * @return True if update was successful. False otherwise.
	 */
	public boolean setSeriesStatus(String seriesName, int authorID, Series.series_status_enum seriesStatus) {
		boolean rtVal = false;
		// 0. ensure series exists 
		Series theSeries = DAORoot.seriesDao.getSeriesByNameAndAuthorID(seriesName, authorID);
		if (theSeries != null) {
			String sql = "UPDATE series SET series_status=? WHERE series_name=? AND author_id=?";

			// 1. establish connection to our database
			try (Connection conn = DAORoot.library.connectToDB();
					PreparedStatement pstmt = conn.prepareStatement(sql);) {
				// 2. set parameters in the prepared statement
				pstmt.setObject(1, seriesStatus.name(), Types.OTHER);
				pstmt.setString(2, seriesName);
				pstmt.setInt(3, authorID);

				// 3. execute our query for to update series_status.
				int rtUp = pstmt.executeUpdate();

				// 4. check if sql update performed operation and updated only one row.
				if (rtUp == 1) {
					rtVal = true;
				} else {
					logger.warn(
							String.format("Updating the number_books_in_series has incorrectly updated %s rows", rtUp));
				}
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

	
	

}
