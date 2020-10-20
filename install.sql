
--TODO:
 --use assertion/CHECK in authors to ensure each alias refers to valid authorID
-- rewrite our foreign references see:
-- https://stackoverflow.com/questions/37054474/how-to-refer-to-a-table-which-is-not-yet-created
--this gets rid of irritating order of creation dependencies that we might have in our table (left as it is currently to provide glancing assurance for lack of circular references


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
--     TODO custom_shelves VARCHAR(30)[] /*genres that are not commonly used and so not accepted into DB default genres*/
);

--To prevent a lot of issues such as:
-- 1. admins deleting all other admins and then themselves (leaves the system in a state where without outside intervention no more users can be added)
-- 2. Can't delete admin accounts because of foreign key reference from an author they created.
--this rule was created to protect the "root" admin from deletion in the system (though it can be circumvented by changing userID of the admin).
CREATE RULE protect_root_admin_entry_delete as
  on delete to users
  where old.user_id = 1
  do instead nothing;



CREATE TABLE IF NOT EXISTS authors (
    author_id SERIAL,
    fname VARCHAR(100) NOT NULL, /*authors first name*/
    lname VARCHAR(100) NOT NULL, /*authors last name*/
	author_bib VARCHAR,
	verified_user_ID INT DEFAULT 1, --this field will be used to allow author to update their page once they create and account and are verified (by default all are owned by an admin)
	PRIMARY KEY (author_id),
	FOREIGN KEY (verified_user_ID) REFERENCES users(user_id) ON DELETE SET DEFAULT,
	UNIQUE (fname, lname)
--     TODO BELOW (requires triggers)
-- 	alias_ids INT[], /*TODO ensure alternative names the author are updated manually)*/
--   series_ids INT[], --TODO references series.series_id --removed because it currently just adds to much complexity
-- 	CHECK EACH element OF alias_id REFERENCES authors.author_id
-- https://dba.stackexchange.com/questions/154548/difference-between-assertion-and-trigger-in-postgresql
--https://dba.stackexchange.com/questions/60132/foreign-key-constraint-on-array-member

);

--TODO joint table to allow an author to own series
-- CREATE TABLE IF NOT EXISTS author_series (
--     series_id int REFERENCES series(series_id),
--     author_id int REFERENCES authors(author_id),
--     PRIMARY KEY (series_id, author_id)
-- );



CREATE TYPE series_status_enum AS ENUM ('COMPLETED', 'ONGOING', 'UNDETERMINED');

CREATE TABLE IF NOT EXISTS series (
    series_id SERIAL UNIQUE,
    series_name VARCHAR(40) NOT NULL, /*name of the series*/
    -- The author series belongs to though other authors may have written the book as well.
    primary_author_id INT, --FOREIGN KEY (altered below)
    number_books_in_series INT, /*THIS SHOULD BE DYNAMICALLY updated when new books are added to the series?*/
    series_status series_status_enum, /*has the series been finished or is it ongoing (presumably the check is implied)*/
    UNIQUE (series_name, primary_author_id),
    PRIMARY KEY (series_id)
     /*TODO add books_ids field?*/
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
    rating_overall NUMERIC(2,2) DEFAULT 0.0, /*2 places before decimal and 2 after the decimal*/
    rating_count INT DEFAULT 0, -- number of votes taken for rating
    series_id INT REFERENCES series(series_id) DEFAULT NULL, --TODO is this legal?
    number_in_series NUMERIC(2,2) DEFAULT 0.0,
    edition INT,
--     author_ids INT[ ] --moved to a junction table <book_authors> to make this validated.
    publish_date DATE,
    publisher VARCHAR,
--     genres int[], --moved to a junction table <book_genres> to make this validated.
    cover_location VARCHAR, --TODO make this in a nested file directory structure (and keep img name short)
    identifiers identifier[], --all the identifiers associated with a book.
    PRIMARY KEY (book_id),
    CHECK (rating_overall>0 AND rating_overall<=10)
    /*future: comment_stream_id: if I ever allow OTHER people to add comments */
);

--  a junction table between books and authors.
--https://stackoverflow.com/questions/7296846/how-to-implement-one-to-one-one-to-many-and-many-to-many-relationships-while-de?rq=1
/*
 We need a many-to-many relationship potentially in books as: a book may have MANY authors and an author probably has MANY books.
 As sql doesn't easily support foreign references in arrays. This means we want a junction table between these two entries
 */
CREATE TABLE IF NOT EXISTS book_authors (
    --  junction table between book and authors for the book
    book_id INT REFERENCES books(book_id),
    author_id INT REFERENCES authors(author_id),
     PRIMARY KEY (book_id, author_id)
);
CREATE TABLE IF NOT EXISTS book_genres (
    --junction table for the books and genres in said book
    book_id int, --REFERENCES books(book_id)
    genre_id int, --REFERENCES genres(genre_id)
    PRIMARY KEY (book_id, genre_id)
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
    reasons VARCHAR(1000), /*reasons to recommend series - possibly make this a list where each entry is the reasons by userX?*/
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


--junction table between Users and books
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

-- #####TABLE ALTERATIONS TO ADD FOREIGN KEYS

-- series table
ALTER TABLE series ADD FOREIGN KEY (primary_author_id) REFERENCES authors(author_id) ON DELETE CASCADE;

-- book_genres table
ALTER TABLE book_genres ADD FOREIGN KEY (book_id) REFERENCES books(book_id);
ALTER TABLE book_genres ADD FOREIGN KEY (genre_id) REFERENCES genres(genre_id);


--DATA ENTRIES SECTION (hashed password is Password1)
INSERT INTO users (username, hashedPassword, salt, first_name, last_name, email, is_admin) VALUES ('admin', '$2a$10$D0uvz6/IgaKHVjV7zdlXAe8L92nEexa4gkNV7zyLtCRUTIyJEVKxy','$2a$10$D0uvz6/IgaKHVjV7zdlXAe', 'admin', 'admin', 'admin@email.com', true);



