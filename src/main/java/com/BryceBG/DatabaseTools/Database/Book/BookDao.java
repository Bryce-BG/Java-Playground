package com.BryceBG.DatabaseTools.Database.Book;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;



/**
 * This is the public DAO (data access object) for getting books from our database.
 * @author Bryce
 *
 */
public class BookDao {
	private static final Logger logger = LogManager.getLogger(BookDao.class.getName());


	

	public Iterable<Book> getAllBooks() {
    	logger.info("Call to getAllBooks() was made but this is a STUB");
        return null; //TODO implement me.
    }

    public Book getBookByIdentifier(String identifier_name, String identifier) {
    	Pair<String, String> id_pair = Pair.with(identifier_name, identifier);
		//TODO implement me. Query DB for book with id type: identifier_name and value: identifier
    	logger.info("Call to getBookByIdentifier() was made but this is a STUB");

        return null;
    }
    
    public Book getBookByAuthor(String Author) {
    	logger.info("Call to getBookByAuthor() was made but this is a STUB");

    	//TODO implement me
		return null;
    }
    
    public Book getBookByBookID(String bookID) {
    	logger.info("Call to getBookByBookID() was made but this is a STUB");

    	//TODO implement me
		return null;
    }
    
    

    public Book getRandomBook() {
    	//intended to be used on the front page of our website
        return null;
    }

}
