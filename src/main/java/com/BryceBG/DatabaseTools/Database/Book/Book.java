package com.BryceBG.DatabaseTools.Database.Book;

import org.javatuples.Pair;

public class Book {
    private String title;
    private String[] author; // supports multiple authors. But, currently we are ignoring author aliases (though may need to add something for them)
    private Pair<String, String>[] identifiers;
    private int book_id; //used to uniquely identify book AND what we will saved the cover of the book as in our image collection.
    private float avg_rating;
    private String series;
    private float number_in_series;
    private int edition;
    private java.time.LocalDate publish_date;
    private String publisher;
    private String[] genres; 
    private String cover_location;
    
    //user related info on book. (if user is logged in and has added the book to thier "read" selection).
    private float personal_rating;
    private String[] personal_shelves; 
    private String[] personal_quotes;
    private String personal_comment;
    private String personal_series_comment;
    
    //TODO: potentially add some kind of comment/review stream field to this Object.
    
    
    public String getSmallCover() {
        return "rootCoverFolder" + cover_location + book_id + "-S.jpg"; 
    }
    public String getLargeCover() {
        return "someCoverURL" + book_id + "-L.jpg"; 
    }

    public Book(String title, String[] author, Pair<String, String>[] identifiers, String series, float avg_rating, 
    			String publisher, java.time.LocalDate publish_date, float number_in_series, String[] genres, 
    			int edition, String cover_location, int book_id) {
    	//constructor is not going to initialize an of the "personal_<tag> fields as more often than not they are probably non existent. 
    	//Later we may overload the constructor so we can do an: if (logged_in): call constructor with personal setter (as then those fields are relevent).
        this.setIdentifiers(identifiers);
		this.setTitle(title);
        this.setAuthor(author);
		this.setBookID(book_id);
		this.setAvgRating(avg_rating);
		this.setSeries(series);
		this.setNumberInSeries(number_in_series);
		this.setEdition(edition);
		this.setPublishDate(publish_date);
		this.setPublisher(publisher);
		this.setGenres(genres);
		this.setCoverLocation(cover_location);
    }
    public int getBookID() {
        return book_id;
    }
    public void setBookID(int book_id) {
    	this.book_id = book_id;
    }

    public void setIdentifiers(Pair<String,String>[] identifiers) {
    	this.identifiers = identifiers;
    }
    public Pair<String,String>[] getIdentifiers() {
        return identifiers;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
    	this.title = title;
    }

    public void setAuthor(String[] author) {
    	this.author = author;
    }
    public String[] getAuthor() {
        return author;
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
	
	public java.time.LocalDate getPublishDate() {
		return publish_date;
	}
	public void setPublishDate(java.time.LocalDate publish_date) {
		this.publish_date = publish_date;
	}
	
	public int getEdition() {
		return edition;
	}
	public void setEdition(int edition) {
		this.edition = edition;
	}
	
	public float getNumberInSeries() {
		return number_in_series;
	}
	public void setNumberInSeries(float number_in_series) {
		this.number_in_series = number_in_series;
	}
	
	public String getSeries() {
		return series;
	}
	public void setSeries(String series) {
		this.series = series;
	}
	
	public float getPersonalRating() {
		return personal_rating;
	}
	public void setPersonalRating(float personal_rating) {
		this.personal_rating = personal_rating;
	}
	
	public float getAvgRating() {
		return avg_rating;
	}
	public void setAvgRating(float avg_rating) {
		this.avg_rating = avg_rating;
	}
	
	public String[] getPersonal_quotes() {
		return personal_quotes;
	}
	public void setPersonal_quotes(String[] personal_quotes) {
		this.personal_quotes = personal_quotes;
	}
	
	public String getPersonal_comment() {
		return personal_comment;
	}
	public void setPersonal_comment(String personal_comment) {
		this.personal_comment = personal_comment;
	}
	
	public String getPersonal_series_comment() {
		return personal_series_comment;
	}
	public void setPersonal_series_comment(String personal_series_comment) {
		this.personal_series_comment = personal_series_comment;
	}
}

