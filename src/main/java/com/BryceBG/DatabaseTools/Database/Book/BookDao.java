package com.BryceBG.DatabaseTools.Database.Book;

import java.util.List;

import org.javatuples.Pair;

/**
 * This is the public DAO (data access object) for getting books from our database.
 * @author Bryce
 *
 */
public class BookDao {
    public static Iterable<Book> getAllBooks() {
        return null; //TODO implement me.
    }

    public static Book getBookByIdentifier(String identifier_name, String identifier) {
    	Pair<String, String> id_pair = Pair.with(identifier_name, identifier);
		//TODO implement me. Query DB for book with id type: identifier_name and value: identifier

        return null;
    }
    
    public static Book getBookByAuthor(String Author) {
    	//TODO implement me
		return null;
    }
    
    public static Book getBookByBookID(String bookID) {
    	//TODO implement me
		return null;
    }
    
    

    public static Book getRandomBook() {
    	//intended to be used on the front page of our website
        return null;
    }

}
