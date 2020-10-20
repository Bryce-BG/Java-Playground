package com.BryceBG.DatabaseTools.Database.Series;



/**
 * This is a class representing series data objects extracted from our database.
 * 
 * @author Bryce-BG
 *
 */
public class Series {
	public enum series_status_enum {
		COMPLETED, //author(s) has finished writing the series
		ONGOING, //author(s) has indicated that there WILL be more books coming out for the series
		UNDETERMINED //maybe more books but the author(s) haven't declared either way
	};

	private int seriesID;
	private String seriesName;
	private int primaryAuthorID; 
	
	private int numberBooksInSeries;
	private series_status_enum seriesStatus;

	public Series(int series_id, String seriesName, int primaryAuthorID, int numberBooksInSeries,
			series_status_enum seriesStatus) {
		this.setSeriesID(series_id);
		this.setSeriesName(seriesName);
		this.setPrimaryAuthorID(primaryAuthorID);
		this.setNumberBooksInSeries(numberBooksInSeries);
		this.setSeriesStatus(seriesStatus);
	}

	public int getSeriesID() {
		return seriesID;
	}

	public void setSeriesID(int seriesID) {
		this.seriesID = seriesID;
	}

	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public int getNumberBooksInSeries() {
		return numberBooksInSeries;
	}

	public void setNumberBooksInSeries(int numberBooksInSeries) {
		this.numberBooksInSeries = numberBooksInSeries;
	}

	public series_status_enum getSeriesStatus() {
		return seriesStatus;
	}

	public void setSeriesStatus(series_status_enum seriesStatus) {
		this.seriesStatus = seriesStatus;
	}

	public int getPrimaryAuthorID() {
		return primaryAuthorID;
	}

	public void setPrimaryAuthorID(int primaryAuthorID) {
		this.primaryAuthorID = primaryAuthorID;
	}
}
