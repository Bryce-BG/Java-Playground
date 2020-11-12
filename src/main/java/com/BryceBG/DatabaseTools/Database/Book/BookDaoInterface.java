package com.BryceBG.DatabaseTools.Database.Book;

import com.BryceBG.DatabaseTools.Database.Series.Series;

import java.util.ArrayList;

import org.javatuples.Pair;

public interface BookDaoInterface {

	/* GETTER functions */

	public abstract ArrayList<Book> getAllBooks();

	// non unique so may return many books
	public abstract Book[] getBooksByAuthor(int author_id);

	// Primary key so will return only one book
	public abstract Book getBookByBookID(long bookID);

	// SHOULD be unique. However, as this field is not designed by me, if this
	// assumption is violated only the first book that matches is returned.
	public abstract Book getBookByIdentifier(String identifier_name, String identifier);

	public abstract Book getRandomBook();

	// non unique so may return many books
	public abstract Book[] getBooksBySeries(Series series);

	// non unique so may return many books
	public abstract Book[] getBooksByTitle(String title);

	/* other functions such as add/remove/modify */

	

	public abstract boolean addBook(int[] authorIDs, String description, int edition, String title);

	public abstract boolean removeBook(long book_id);

	// should be broken down more extensively as some are modifications of existing
	// values whereas the lists may be add/delete or modify for list
	public abstract <T> boolean editBook(long bookID, BookDaoInterface.EDIT_TYPE editType, T newVal);

	
	// this enum is the fields that we allow to be modified post creation.
	public static enum EDIT_TYPE {
		ADD_AUTHOR("Add an author to a book", Integer.class),
		REMOVE_AUTHOR("Remove an author from a book", Integer.class),
		SET_AVG_RATING("Set the rating of a book based on cumulative ratings", Float.class),
		SET_BOOK_INDEX_IN_SERIES("Set the index the book occupies in the series", Float.class),
		SET_COVER_LOCATION("Set the path for where the cover image for the book is located", String.class),
		SET_COVER_NAME("Set the name of the file that is the cover image for the book", String.class),
		SET_DESCRIPTION("Set the description of the book (the blurb)", String.class),
		SET_EDITION("Set the edition of the book", Integer.class),
		SET_GENRES ("Set the genres of the book", String[].class), //TODO this may cause errors as it as an array
		SET_IDENTIFIERS("Set identifiers for the book (ISBN and the like)", Pair[].class),
		SET_PUBLISH_DATE("Set the date for when the book was published", java.sql.Date.class),
		SET_PUBLISHER("Set the field of who published the book", String.class),
		SET_RATING_COUNT("Set the count for how many people have rated the book", Integer.class),
		SET_SERIES_ID("Set the series that the book is in", Integer.class);
		
		private final String description;
	    private final Class<?> requiredType;
	    private EDIT_TYPE(String description, Class<?> required_class) {
	        this.description = description;
	        this.requiredType = required_class;
	    }
	    public String getRequiredType() {
	    	return requiredType.getTypeName();	    	
	    }
	    public String getEditTypeDescription() {
	    	return description;
	    }
	    public <T> boolean checkFitsRequiredType(T value) {
	    	if (value == null)
	    		return false;
	    	else
	    		return value.getClass().getTypeName().equals(requiredType.getTypeName());
	    }
	}
}
