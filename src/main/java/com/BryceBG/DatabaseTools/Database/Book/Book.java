package com.BryceBG.DatabaseTools.Database.Book;


import java.io.File;
import java.sql.Date;
import java.util.Arrays;

import org.javatuples.Pair;

import com.BryceBG.DatabaseTools.utils.Utils;

/**
 * This class represents a book object. It not only contains the fields for the
 * book table in our DB but also some fields that are extracted from
 * Supplementary "junction tables" such as author_names (which is used if there
 * is more than one author.
 * 
 * @author Bryce-BG
 *
 */

public class Book {

	// this enum is the fields that we allow to be modified post creation.
	enum BOOK_FIELD {

		TITLE, // maybe not let this get changed?
		RATING_OVERALL, RATING_COUNT, SERIES_ID, NUMBER_IN_SERIES, EDITION, PUBLISHER, PUBLISH_DATE, COVER_LOCATION,
		IDENTIFIERS, AVG_RATING,

	}

	private int[] authorIDs; // NOT initialized by constructor (ids if there is more than one author)
	private float average_rating = -1;
	private long book_id = -1; // used to uniquely identify book.
	private float book_index_in_series = -1;
	private int count_authors = 1;
	private String cover_location;
	private String cover_name; // Filename is for the cover image (append to cover_location to get full path).
	private String description;
	private int edition = -1;
	private String[] genres; // NOT initialized by constructor
	private boolean has_identifiers;
	private Pair<String, String>[] identifiers; // NOT initialized by constructor
	private int primary_author_id = -1;
	private java.sql.Date publish_date;
	private String publisher = "NA";
	private long rating_count = 0;
	private int series_id = -1;
	private String title;
	// user related info on book. (if user is logged in and has added the book to
	// their "read" selection).
	// these fields are NOT set by constructor and also require initilization from
	// drawing data from additional tables
	private float personal_rating = -1;
	private String[] personal_shelves;
	private String[] personal_quotes;
	private String personal_comment;
	private String personal_series_comment;

	/**
	 * constructor initializes everything that is obtainable DIRECTLY from the books
	 * table in database. The other fields can be manually initialized with the
	 * setters for this object
	 * 
	 * @param avgRating         The cumulative average rating from users who have
	 *                          read the book.
	 * @param bookID            The id of the book in our database.
	 * @param bookIndexInSeries The position that a book occupies in a series (if it
	 *                          is in a series) For example: HP and the Chamber of
	 *                          Secrets has bookIndexInSeries =2 according to the
	 *                          author. This field can be a floating point.
	 * @param coverLocation     The relative path from the "root directory" to where
	 *                          our cover image is located (does not include the
	 *                          actual file name appended).
	 * @param coverName         The name of the cover image for this book.
	 * @param description       The blurb associated with a book
	 * @param edition           The edition of the book.
	 * @param has_identifiers   indicates if book has identifiers we should pull
	 *                          from supplementary tables. For example: ("isbn",
	 *                          "0061964360")
	 * @param primaryAuthorID   The ID of the author who is set as the "primary
	 *                          author" for the book.
	 * @param publishDate       The date on which the book was published.
	 * @param publisher         The publisher for the book.
	 * @param ratingCount       How many people have rated the book in our system.
	 * @param seriesID          The ID for the series the book belongs in.
	 * @param title             The title of the book.
	 */
	public Book(float avgRating, long bookID, float bookIndexInSeries, int countAuthors, String coverLocation,
			String coverName, String description, int edition, boolean has_identifiers, int primaryAuthorID,
			Date publishDate, String publisher, long ratingCount, int seriesID, String title) {
		this.setAvgRating(avgRating);
		this.setBookID(bookID);
		this.setBookIndexInSeries(bookIndexInSeries);
		this.setCountAuthors(countAuthors);
		this.setCoverLocation(coverLocation);
		this.setCoverName(coverName);
		this.setDescription(description);
		this.setEdition(edition);
		this.setHasIdentifiers(has_identifiers);
		this.setPrimaryAuthorID(primaryAuthorID);
		this.setPublishDate(publishDate);
		this.setPublisher(publisher);
		this.setRatingCount(ratingCount);
		this.setSeriesID(seriesID);
		this.setTitle(title);
	}

	public long getBookID() {
		return book_id;
	}

	public void setBookID(long book_id) {
		this.book_id = book_id;
	}

	public void setIdentifiers(Pair<String, String>[] identifiers) {
		this.identifiers = identifiers;
	}

	public Pair<String, String>[] getIdentifiers() {
		return identifiers;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPrimaryAuthorID(int authorID) {
		this.primary_author_id = authorID;
	}

	public int getPrimaryAuthorID() {
		return primary_author_id;
	}

	public String getCoverLocation() {
		return cover_location;
	}

	public void setCoverLocation(String cover_location) {
		this.cover_location = cover_location;
	}

	public String[] getPersonalShelves() {
		return personal_shelves;
	}

	public void setPersonalShelves(String[] personal_shelves) {
		this.personal_shelves = personal_shelves;
	}

	public String[] getGenres() {
		return genres;
	}

	public void setGenres(String[] genres) {
		this.genres = genres;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public Date getPublishDate() {
		return publish_date;
	}

	public void setPublishDate(Date publishDate) {
		this.publish_date = publishDate;
	}

	public int getEdition() {
		return edition;
	}

	public void setEdition(int edition) {
		this.edition = edition;
	}

	public float getBookIndexInSeries() {
		return book_index_in_series;
	}

	public void setBookIndexInSeries(float book_index) {
		this.book_index_in_series = book_index;
	}

	public int getSeriesID() {
		return series_id;
	}

	public void setSeriesID(int seriesID) {
		this.series_id = seriesID;
	}

	public float getPersonalRating() {
		return personal_rating;
	}

	public void setPersonalRating(float personal_rating) {
		this.personal_rating = personal_rating;
	}

	public float getAvgRating() {
		return average_rating;
	}

	public void setAvgRating(float avg_rating) {
		this.average_rating = avg_rating;
	}

	public String[] getPersonalQuotes() {
		return personal_quotes;
	}

	public void setPersonalQuotes(String[] personalQuotes) {
		this.personal_quotes = personalQuotes;
	}

	public String getPersonalComment() {
		return personal_comment;
	}

	public void setPersonalComment(String personal_comment) {
		this.personal_comment = personal_comment;
	}

	public String getPersonalSeriesComment() {
		return personal_series_comment;
	}

	public void setPersonalSeriesComment(String personal_series_comment) {
		this.personal_series_comment = personal_series_comment;
	}

	public int[] getAuthorIDs() {
		return authorIDs; // includes primary author
	}

	public void setAuthorIDs(int[] authorIDs) {
		this.authorIDs = authorIDs;
	}

	public String getCoverName() {
		return cover_name;
	}

	public void setCoverName(String cover_name) {
		this.cover_name = cover_name;
	}

	/* special functions to get full path to cover images */
	public String getSmallCover() {
		// https://stackoverflow.com/questions/4416425/how-to-split-string-with-some-separator-but-without-removing-that-separator-in-j/4416576#4416576
		// https://stackoverflow.com/questions/4545937/java-splitting-the-filename-into-a-base-and-extension
		// this technique is called zero-width positive lookahead
		String[] tokens = cover_name.split("\\.(?=[^\\.]+$)");
		// tokens[0] = file name
		// tokens[1] = extension

		return Utils.getConfigString("app.root_cover_location", null) + File.separator + cover_location + File.separator
				+ tokens[0] + "-S." + tokens[1];
	}

	public String getLargeCover() {
		return Utils.getConfigString("app.root_cover_location", null) + File.separator + cover_location + File.separator + cover_name;
	}

	public long getRatingCount() {
		return rating_count;
	}

	public void setRatingCount(long rating_count) {
		this.rating_count = rating_count;
	}

	public int getCountAuthors() {
		return count_authors;
	}

	public void setCountAuthors(int count_authors) {
		this.count_authors = count_authors;
	}

	public boolean getHasIdentifiers() {
		return has_identifiers;
	}

	public void setHasIdentifiers(boolean has_identifiers) {
		this.has_identifiers = has_identifiers;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(authorIDs);
		result = prime * result + Float.floatToIntBits(average_rating);
		result = prime * result + (int) (book_id ^ (book_id >>> 32));
		result = prime * result + Float.floatToIntBits(book_index_in_series);
		result = prime * result + count_authors;
		result = prime * result + ((cover_location == null) ? 0 : cover_location.hashCode());
		result = prime * result + ((cover_name == null) ? 0 : cover_name.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + edition;
		result = prime * result + Arrays.hashCode(genres);
		result = prime * result + (has_identifiers ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(identifiers);
		result = prime * result + ((personal_comment == null) ? 0 : personal_comment.hashCode());
		result = prime * result + Arrays.hashCode(personal_quotes);
		result = prime * result + Float.floatToIntBits(personal_rating);
		result = prime * result + ((personal_series_comment == null) ? 0 : personal_series_comment.hashCode());
		result = prime * result + Arrays.hashCode(personal_shelves);
		result = prime * result + primary_author_id;
		result = prime * result + ((publish_date == null) ? 0 : publish_date.hashCode());
		result = prime * result + ((publisher == null) ? 0 : publisher.hashCode());
		result = prime * result + (int) (rating_count ^ (rating_count >>> 32));
		result = prime * result + series_id;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		if (!Arrays.equals(authorIDs, other.authorIDs))
			return false;
		if (Float.floatToIntBits(average_rating) != Float.floatToIntBits(other.average_rating))
			return false;
		if (book_id != other.book_id)
			return false;
		if (Float.floatToIntBits(book_index_in_series) != Float.floatToIntBits(other.book_index_in_series))
			return false;
		if (count_authors != other.count_authors)
			return false;
		if (cover_location == null) {
			if (other.cover_location != null)
				return false;
		} else if (!cover_location.equals(other.cover_location))
			return false;
		if (cover_name == null) {
			if (other.cover_name != null)
				return false;
		} else if (!cover_name.equals(other.cover_name))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (edition != other.edition)
			return false;
		if (!Arrays.equals(genres, other.genres))
			return false;
		if (has_identifiers != other.has_identifiers)
			return false;
		if (!Arrays.equals(identifiers, other.identifiers))
			return false;
		if (personal_comment == null) {
			if (other.personal_comment != null)
				return false;
		} else if (!personal_comment.equals(other.personal_comment))
			return false;
		if (!Arrays.equals(personal_quotes, other.personal_quotes))
			return false;
		if (Float.floatToIntBits(personal_rating) != Float.floatToIntBits(other.personal_rating))
			return false;
		if (personal_series_comment == null) {
			if (other.personal_series_comment != null)
				return false;
		} else if (!personal_series_comment.equals(other.personal_series_comment))
			return false;
		if (!Arrays.equals(personal_shelves, other.personal_shelves))
			return false;
		if (primary_author_id != other.primary_author_id)
			return false;
		if (publish_date == null) {
			if (other.publish_date != null)
				return false;
		} else if (!publish_date.equals(other.publish_date))
			return false;
		if (publisher == null) {
			if (other.publisher != null)
				return false;
		} else if (!publisher.equals(other.publisher))
			return false;
		if (rating_count != other.rating_count)
			return false;
		if (series_id != other.series_id)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
	
}
