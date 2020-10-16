package com.BryceBG.DatabaseTools.Database.Author;

public class Author {
	private int author_id; // the id of the author in our database (PRIMARY KEY)
	private int[] series_ids; // IDs of series the author has written
	private String fName; // Author's first name
	private String lName; // Author's last name
	private String author_bib; // short bio about the author
	private int[] alias_ids; // IDs of other pseudonames the author writes under.
	private int verified_user_ID; // The id of the user who owns the author item (initially this is the admin who
									// creates it. But once the actual author creates an user account this can be
									// linked to thier account so they can modify their own page.

	public Author(int author_id, int[] series_ids, String fName, String lName, String author_bib, int[] alias_ids,
			int verified_user_ID) {
		this.setAuthorID(author_id);
		this.setSeriesIDs(series_ids);
		this.setFirstName(fName);
		this.setLastName(lName);
		this.setAuthorBib(author_bib);
		this.setAliasIDs(alias_ids);
		this.setVerifiedUserID(verified_user_ID);
	}

	public int getAuthorID() {
		return author_id;
	}

	public void setAuthorID(int author_id) {
		this.author_id = author_id;
	}

	public int[] getSeriesIDs() {
		return series_ids;
	}

	public void setSeriesIDs(int[] series_ids) {
		this.series_ids = series_ids;
	}

	public String getFirstName() {
		return fName;
	}

	public void setFirstName(String fName) {
		this.fName = fName;
	}

	public String getLastName() {
		return lName;
	}

	public void setLastName(String lName) {
		this.lName = lName;
	}

	public String getAuthorBib() {
		return author_bib;
	}

	public void setAuthorBib(String author_bib) {
		this.author_bib = author_bib;
	}

	public int[] getsetAliasIDs() {
		return alias_ids;
	}

	public void setAliasIDs(int[] alias_ids) {
		this.alias_ids = alias_ids;
	}

	public int getVerifiedUserID() {
		return verified_user_ID;
	}

	public void setVerifiedUserID(int verified_user_ID) {
		this.verified_user_ID = verified_user_ID;
	}

}
