package com.BryceBG.DatabaseTools.Database.Book;

import com.BryceBG.DatabaseTools.Database.Series.Series;

import org.javatuples.Pair;

import java.util.ArrayList;

public interface BookDaoInterface {

	/* GETTER functions */

	public abstract ArrayList<Book> getAllBooks();

	// non unique so may return many books
	public abstract Book[] getBooksByAuthor(String fName, String lName);

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

	public abstract boolean addBook(String title, String cover_location, Pair<String, String>[] authors);

	public abstract boolean removeBook(long book_id);

	// We assume this pairing constitutes a unique pairing. though our database will
	// not enforce it due to the current design.
	public abstract boolean removeBook(String title, Pair<String, String> authors);

	// should be broken down more extensively as some are modifications of existing
	// values whereas the lists may be add/delete or modify for list
	public abstract boolean editBook(Book.BOOK_FIELD book_field, Object newVal);

	// https://docs.oracle.com/javase/tutorial/jdbc/basics/sqlcustommapping.html
	// (have I been doing the reading/writing wrong?

}
