
--TODO:
 --use assertion/CHECK in authors to ensure each alias refers to valid authorID



DROP TABLE IF EXISTS books, authors, series, users CASCADE;
DROP TYPE IF EXISTS series_status;

CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL UNIQUE, --must be unique otherwise we can't reference
	username VARCHAR(30) PRIMARY KEY, --always lowercase alphanumeric and 5-30 characters
	hashedPassword VARCHAR NOT NULL,
	salt VARCHAR NOT NULL,
	first_name VARCHAR(30) NOT NULL,
	last_name VARCHAR(30) NOT NULL,
  	email TEXT NOT NULL UNIQUE, --further constraints required see: https://hashrocket.com/blog/posts/working-with-email-addresses-in-postgresql
	is_admin BOOLEAN NOT NULL

    /*TODO add much in this table*/
--	its bad practice to store password in plain form. Replace this with hashed password once we implement hashing for the app (with salt possibly)
--     custom_genres VARCHAR(30)[] /*genres that are not commonly used and so not accepted into DB default genres*/

);
CREATE TABLE IF NOT EXISTS authors (
    author_id SERIAL,
    series_ids INT[],
    fname VARCHAR(100), /*authors first name*/
    lname VARCHAR(100), /*authors last name*/
	author_bib VARCHAR,
	alias_ids INT[], /*alternative names the author may go by (needs to be updated manually)*/
	PRIMARY KEY (author_id)
--     TODO BELOW
-- 	CHECK EACH element OF alias_id REFERENCES authors.author_id
-- https://dba.stackexchange.com/questions/154548/difference-between-assertion-and-trigger-in-postgresql

	/*books_id can be computed by scanning the DB, but might we may want to just link for speed */
);




CREATE TYPE series_status_enum AS ENUM ('COMPLETED', 'ONGOING', 'UNDETERMINED');

CREATE TABLE IF NOT EXISTS series (
    series_id SERIAL UNIQUE, --TODO might remove this
    series_name VARCHAR(40) NOT NULL, /*name of the series*/
    author_id INT, /*author series belongs to*/
    number_books_in_series INT, /*THIS SHOULD BE DYNAMICALLY updated when new books are added to the series?*/
    series_status series_status_enum, /*has the series been finished or is it ongoing (presumably the check is implied)*/
    UNIQUE(series_name, author_id),
    PRIMARY KEY (series_name, author_id),
    FOREIGN KEY (author_id) REFERENCES authors(author_id)
     /*ADD books_ids?*/

);
--this rule protects the integrity of the database by preventing deletion of series when it is currently referenced
--http://wiki.postgresql.org/wiki/Introduction_to_PostgreSQL_Rules_-_Making_entries_which_can%27t_be_altered
--https://stackoverflow.com/questions/810180/how-to-prevent-deletion-of-the-first-row-in-table-postgresql#:~:text=Basically%2C%20you'll%20have%20to,row%20will%20never%20be%20deleted.
CREATE RULE protect_in_use_series_entry_delete as
  on delete to series
  where old.number_books_in_series > 0
  do instead nothing;

--prevent the count from going into the negative from UPDATE operations
CREATE RULE protect_in_use_series_entry_update as
  on update to series
  where new.number_books_in_series < 0
  do instead nothing;

/*used as a tuple for book identifiers like: (ISBN: isbn_value) or: (MOBI-ASN: SHDA4N)*/
CREATE TYPE identifier AS (
    name            text,
    id_val		    text
);


CREATE TABLE IF NOT EXISTS books (
    book_id SERIAL,
    title VARCHAR NOT NULL,
    rating_overall NUMERIC(2,2), /*2 places before decimal and 2 after the decimal (need to set range (FLOAT 0-10))*/
    rating_count INT, -- number of votes taken for rating
    series_id INT REFERENCES series(series_id),
    number_in_series NUMERIC(2,2),
    edition INT,
    author_ids INT[ ] /*REFERENCES authors(author_id)*/, /*may be more than 1 author for a book so a list is required*/
    publish_date DATE,
    publisher VARCHAR(70),
    genres int[],
    cover_location VARCHAR, --TODO make this in a nested file directory structure (and keep img name short)
    identifiers identifier[], --all the identifiers associated with a book.
    PRIMARY KEY (book_id),
    CHECK (rating_overall>0 AND rating_overall<10)
    /*future: comment_stream_id: if I ever allow OTHER people to add comments */
);




CREATE TABLE IF NOT EXISTS genres (
    genre_id serial, /*primary key*/
    parent int, /*references to the overarching theme/genre that this class extends*/
    genre_name VARCHAR(60), /*name associated with this genre*/
    genre_description VARCHAR(300), /*a description on what kind of classifications exist*/
    /*future fields possibly not here but instead in the book info?*/
    /*
      main_char_genres: genres that apply directly to the main character (for example is the main character a witch?)
      overall_world_genres: general all purpose genres (for example are there witches in the book even if the main character isn't one?)
      setting_genre: basic setting (is it fantasy or science fiction)
      */
    PRIMARY KEY (genre_id),
    FOREIGN KEY (parent) REFERENCES genres(genre_id)
);



-- #####COMMENTS TABLES
CREATE TABLE IF NOT EXISTS comments (
    --this table is for comments for a specific book
    user_id int,
	book_id int,
	comment VARCHAR(1000),
	PRIMARY KEY (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
	FOREIGN KEY (book_id) REFERENCES books(book_id)

);
CREATE TABLE IF NOT EXISTS comments_series (
--     comment_id serial,
    user_id int,
	comment VARCHAR(1000),
	series_id int,
	PRIMARY KEY (user_id, series_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
	FOREIGN KEY (series_id) REFERENCES series(series_id)

);
CREATE TABLE IF NOT EXISTS comments_author (
--     comment_id serial PRIMARY KEY,
    user_id int,
	comment VARCHAR(1000),
	author_id int,
	PRIMARY KEY (user_id, author_id),
	FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (author_id) references authors(author_id)
);



-- #####RECOMENDATIONS TABLES
CREATE TABLE IF NOT EXISTS recommendation_series_to_series (
    series1_id int,
    series2_id int,
    reasons VARCHAR(1000), /*reasons to recomend series - possibly make this a list where each entry is the reasons by userX?*/
    PRIMARY KEY (series1_id, series2_id),
    FOREIGN KEY (series1_id) references series(series_id),
    FOREIGN KEY (series2_id) references series(series_id)
);
CREATE TABLE IF NOT EXISTS recommendation_book_to_series (
    series1_id int,
    book2_id int,
    reasons VARCHAR(1000), /*reasons to recommend series - possibly make this a list where each entry is the reasons by userX?*/
    PRIMARY KEY (series1_id, book2_id),
    FOREIGN KEY (series1_id) references series(series_id),
    FOREIGN KEY (book2_id) references books(book_id)
);
CREATE TABLE IF NOT EXISTS recommendation_book_to_book (
    book1_id int,
    book2_id int,
    reasons VARCHAR(1000), /*reasons to recommend series - possibly make this a list where each entry is the reasons by userX?*/
    PRIMARY KEY (book1_id, book2_id),
    FOREIGN KEY (book1_id) references books(book_id),
    FOREIGN KEY (book2_id) references books(book_id)
);



CREATE TABLE IF NOT EXISTS user_book_rating (
    --this table is for users rating for books
--     comment_id serial,
    user_id int,
	book_id int,
	rating numeric(2,2), /*2 places before decimal and 2 after the decimal (need to set range (FLOAT 0-10))*/
	PRIMARY KEY (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
	FOREIGN KEY (book_id) REFERENCES books(book_id)

);

--MOCK DATA
INSERT INTO users (username, hashedPassword, salt, first_name, last_name, email, is_admin) VALUES ('admin', '$2a$10$D0uvz6/IgaKHVjV7zdlXAe8L92nEexa4gkNV7zyLtCRUTIyJEVKxy','$2a$10$D0uvz6/IgaKHVjV7zdlXAe', 'admin', 'admin', 'admin@email.com', true);
--TODO remove these as they are just test entries to play with
INSERT INTO authors(fname, lname, series_ids, author_bib, alias_ids) VALUES ('James', 'Joyce', array[]::int[], 'TEST AUTHOR', array[]::int[]);
INSERT INTO SERIES(series_name, author_id, number_books_in_series, series_status) VALUES ('test series', 1, 1, 'COMPLETED');
