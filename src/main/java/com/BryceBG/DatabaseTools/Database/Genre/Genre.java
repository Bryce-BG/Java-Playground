package com.BryceBG.DatabaseTools.Database.Genre;

import java.util.Arrays;

/**
 * A class representing a single genre object from the database.
 * @author Bryce-BG
 *
 */
public class Genre {
	
	private String parent; //who the parent node is for the genre.
	private String genre_name; //name associated with this genre
	private String genre_description; //a description on what the genre is supposed to be used on
	private String mygdrds_equiv; //a translation system between my GoodReads shelve names and this systems genre_name
	private String[] keywords;
	
	
	
	
	public Genre(String parent, String genreName, String genreDescription, String myGdrdsEquiv, String[] keywords) {
		super();
		this.setParent(parent);
		this.setGenreName(genreName);
		this.setGenreDescription(genreDescription);
		this.setMygdrdsEquiv(myGdrdsEquiv);
		this.setKeywords(keywords);
	}
	
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getGenreName() {
		return genre_name;
	}
	public void setGenreName(String genre_name) {
		this.genre_name = genre_name;
	}
	public String getMygdrdsEquiv() {
		return mygdrds_equiv;
	}
	public void setMygdrdsEquiv(String mygdrds_equiv) {
		this.mygdrds_equiv = mygdrds_equiv;
	}
	public String getGenreDescription() {
		return genre_description;
	}
	public void setGenreDescription(String genre_description) {
		this.genre_description = genre_description;
	}
	public String[] getKeywords() {
		return keywords;
	}
	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((genre_description == null) ? 0 : genre_description.hashCode());
		result = prime * result + ((genre_name == null) ? 0 : genre_name.hashCode());
		result = prime * result + Arrays.hashCode(keywords);
		result = prime * result + ((mygdrds_equiv == null) ? 0 : mygdrds_equiv.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		Genre other = (Genre) obj;
		if (genre_description == null) {
			if (other.genre_description != null)
				return false;
		} else if (!genre_description.equals(other.genre_description))
			return false;
		if (genre_name == null) {
			if (other.genre_name != null)
				return false;
		} else if (!genre_name.equals(other.genre_name))
			return false;
		if (!Arrays.equals(keywords, other.keywords))
			return false;
		if (mygdrds_equiv == null) {
			if (other.mygdrds_equiv != null)
				return false;
		} else if (!mygdrds_equiv.equals(other.mygdrds_equiv))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}			
	
	

}
