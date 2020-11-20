--This is our main script to create our database tables for our system.
--TODO:
-- rewrite our foreign references see:
-- https://stackoverflow.com/questions/37054474/how-to-refer-to-a-table-which-is-not-yet-created
--this gets rid of irritating order of creation dependencies that we might have in our table (left as it is currently to provide glancing assurance for lack of circular references


-- DROP TABLE IF EXISTS books, authors, series, users CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    user_id        BIGSERIAL UNIQUE,            --must be unique otherwise we can't reference
    username       VARCHAR(30) PRIMARY KEY,     --always lowercase alphanumeric and 5-30 characters
    hashedPassword VARCHAR     NOT NULL,
    salt           VARCHAR     NOT NULL,
    first_name     VARCHAR(30) NOT NULL,
    last_name      VARCHAR(30) NOT NULL,
    email          TEXT        NOT NULL UNIQUE, --further constraints required see: https://hashrocket.com/blog/posts/working-with-email-addresses-in-postgresql
    is_admin       BOOLEAN     NOT NULL
--     TODO custom_shelves VARCHAR(30)[] /*genres that are not commonly used and so not accepted into DB default genres*/
);

--To prevent a lot of issues such as:
-- 1. admins deleting all other admins and then themselves (leaves the system in a state where without outside intervention no more users can be added)
-- 2. Can't delete admin accounts because of foreign key reference from an author they created.
--this rule was created to protect the "root" admin from deletion in the system (though it can be circumvented by changing userID of the admin).
--the username, and password can be updated though to keep this user from being a security vulnerability.
CREATE RULE protect_root_admin_entry_delete as
    on delete to users
    where old.user_id = 1
    do instead nothing;



CREATE TABLE IF NOT EXISTS authors
(
    author_id        SERIAL,
    fname            VARCHAR(100) NOT NULL, /*authors first name*/
    lname            VARCHAR(100) NOT NULL, /*authors last name*/
    author_bib       VARCHAR,
    verified_user_ID BIGINT DEFAULT 1, --this field will be used to allow author to update their page once they create and account and are verified (by default all are owned by an admin)
    PRIMARY KEY (author_id),
    FOREIGN KEY (verified_user_ID) REFERENCES users (user_id) ON DELETE SET DEFAULT,
    UNIQUE (fname, lname)
-- 	alias_ids INT[], /*TODO ensure alternative names the author are updated manually)*/
-- 	CHECK EACH element OF alias_id REFERENCES authors.author_id
-- https://dba.stackexchange.com/questions/154548/difference-between-assertion-and-trigger-in-postgresql
--https://dba.stackexchange.com/questions/60132/foreign-key-constraint-on-array-member

);



CREATE TYPE series_status_enum AS ENUM ('COMPLETED', 'ONGOING', 'UNDETERMINED');

CREATE TABLE IF NOT EXISTS series
(
    series_id              SERIAL UNIQUE,
    series_name            VARCHAR(40) NOT NULL, /*name of the series*/
    -- The author series belongs to (though other authors may have written the book as well they are listed in the other table)
    primary_author_id      INT, --FOREIGN KEY (altered below)
    number_books_in_series INT, /*TODO THIS SHOULD BE DYNAMICALLY updated when new books are added to the series?*/
    series_status          series_status_enum, /*has the series been finished or is it ongoing (presumably the check is implied)*/
    UNIQUE (series_name, primary_author_id),
    PRIMARY KEY (series_id)
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
-- https://manual.calibre-ebook.com/generated/en/ebook-meta.html
CREATE TABLE IF NOT EXISTS book_identifier
(
    book_id          BIGINT, --FOREIGN KEY altered below
    identifier_type  VARCHAR NOT NULL,
    identifier_value VARCHAR NOT NULL,
    PRIMARY KEY (book_id, identifier_type, identifier_value)
);



CREATE TABLE IF NOT EXISTS books
(
    average_rating       NUMERIC(4, 2)                     DEFAULT 0.0, /*2 places before decimal and 2 after the decimal*/
    book_id              BIGSERIAL,
    book_index_in_series NUMERIC(4, 2)                     DEFAULT -1,
    count_authors        INT                               DEFAULT 1,    --how many authors wrote the series if  more than 1, look in junction table <book_authors> for other author's ids
    cover_location       VARCHAR                           DEFAULT NULL,
    cover_name           VARCHAR                           DEFAULT NULL,
    description          VARCHAR						   DEFAULT '',
    edition              INT                               DEFAULT -1, -- -1 indicates unknown edition
    --     genres int[], --moved to a junction table <book_genres> to make this validated.
    --     identifiers identifier[], --moved to:book_identifier table
    has_identifiers      BOOLEAN                           DEFAULT FALSE,
    primary_author_id    INT     NOT NULL,                               --FOREIGN KEY references authors,
    publish_date         TIMESTAMP,
    publisher            VARCHAR						   DEFAULT '', --can't be null as apparently null is unique: https://www.postgresqltutorial.com/postgresql-indexes/postgresql-unique-index/#:~:text=PostgreSQL%20treats%20NULL%20as%20distinct,creates%20a%20corresponding%20UNIQUE%20index.
    rating_count         BIGINT                            DEFAULT 0,    -- number of votes taken for rating
    series_id            INT                               DEFAULT NULL, 
    title                VARCHAR                           NOT NULL,
    PRIMARY KEY (book_id),
    FOREIGN KEY (series_id) REFERENCES series (series_id) ON DELETE SET DEFAULT,
    UNIQUE (title, primary_author_id, edition, publisher), --needs last 2 fields to deal with issues of multple editions and versions by different publishers for a book
    CHECK (average_rating >= 0 AND average_rating <= 10),
    CHECK (count_authors > 0)
);

--  a junction table between books and authors.
--https://stackoverflow.com/questions/7296846/how-to-implement-one-to-one-one-to-many-and-many-to-many-relationships-while-de?rq=1
/*
 We need a many-to-many relationship potentially in books as: a book may have MANY authors and an author probably has MANY books.
 As sql doesn't easily support foreign references in arrays. This means we want a junction table between these two entries
 */
CREATE TABLE IF NOT EXISTS book_authors
(
    --  junction table between book and authors for the book
    book_id   BIGINT, --REFERENCES books(book_id)
    author_id INT,    --REFERENCES authors(author_id)
    PRIMARY KEY (book_id, author_id)
);
CREATE TABLE IF NOT EXISTS book_genres
(
    --junction table for the books and genres in said book
    book_id    BIGINT,  --REFERENCES books(book_id)
    genre_name VARCHAR, --REFERENCES genres(genre_name)
    PRIMARY KEY (book_id, genre_name)
);



CREATE TABLE IF NOT EXISTS genres
(
    genre_description   VARCHAR     DEFAULT NULL, /*a description on what kind of classifications exist*/
    genre_name          VARCHAR, /*name associated with this genre*/
	keywords		    VARCHAR[]   DEFAULT ARRAY[]::VARCHAR[],
	mygdrds_equiv	    VARCHAR,
	parent              VARCHAR     DEFAULT NULL, /*references to the overarching theme/genre that this class extends*/
    PRIMARY KEY (genre_name),
    FOREIGN KEY (parent) REFERENCES genres (genre_name) ON DELETE SET NULL 

);


-- #####COMMENTS TABLES
CREATE TABLE IF NOT EXISTS comments
(
    --this table is for comments for a specific book
    user_id BIGINT,
    book_id BIGINT,
    comment VARCHAR(1000),
    PRIMARY KEY (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (book_id) REFERENCES books (book_id)

);
CREATE TABLE IF NOT EXISTS comments_series
(
    user_id   BIGINT,
    series_id int,
    comment   VARCHAR(1000),
    PRIMARY KEY (user_id, series_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (series_id) REFERENCES series (series_id)

);
CREATE TABLE IF NOT EXISTS comments_author
(
    user_id   BIGINT,
    comment   VARCHAR(1000),
    author_id int,
    PRIMARY KEY (user_id, author_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (author_id) references authors (author_id) ON DELETE CASCADE
);


-- #####RECOMENDATIONS TABLES
CREATE TABLE IF NOT EXISTS recommendation_series_to_series
(
    series1_id int,
    series2_id int,
    reasons    VARCHAR(1000), /*reasons to recommend series - possibly make this a list where each entry is the reasons by userX?*/
    PRIMARY KEY (series1_id, series2_id),
    FOREIGN KEY (series1_id) references series (series_id),
    FOREIGN KEY (series2_id) references series (series_id)
);
CREATE TABLE IF NOT EXISTS recommendation_book_to_series
(
    series1_id int,
    book2_id   BIGINT,
    reasons    VARCHAR(1000), /*reasons to recommend series - possibly make this a list where each entry is the reasons by userX?*/
    PRIMARY KEY (series1_id, book2_id),
    FOREIGN KEY (series1_id) references series (series_id),
    FOREIGN KEY (book2_id) references books (book_id)
);
CREATE TABLE IF NOT EXISTS recommendation_book_to_book
(
    book1_id BIGINT,
    book2_id BIGINT,
    reasons  VARCHAR(1000), /*reasons to recommend series - possibly make this a list where each entry is the reasons by userX?*/
    PRIMARY KEY (book1_id, book2_id),
    FOREIGN KEY (book1_id) references books (book_id),
    FOREIGN KEY (book2_id) references books (book_id)
);


--junction table between Users and books
CREATE TABLE IF NOT EXISTS user_book_rating
(
    --this table is for users rating for books
    user_id BIGINT,
    book_id BIGINT,
    rating  NUMERIC(4, 2), /*2 places before decimal and 2 after the decimal (need to set range (FLOAT 0-10))*/
    PRIMARY KEY (user_id, book_id),
    CHECK ( rating > 0 AND rating >= 10),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (book_id) REFERENCES books (book_id)

);

-- #####TABLE ALTERATIONS TO ADD FOREIGN KEYS

-- series table
ALTER TABLE series
    ADD FOREIGN KEY (primary_author_id) REFERENCES authors (author_id) ON DELETE CASCADE;

-- book_genres table
ALTER TABLE book_genres
    ADD FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE; --cascade so entries are removed if book is deleted
ALTER TABLE book_genres
    ADD FOREIGN KEY (genre_name) REFERENCES genres (genre_name) ON DELETE CASCADE ON UPDATE CASCADE;

--book table
ALTER TABLE books
    ADD FOREIGN KEY (primary_author_id) REFERENCES authors (author_id) ON DELETE CASCADE;

ALTER TABLE book_identifier
    ADD FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE;

ALTER TABLE book_authors
    ADD FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE;
ALTER TABLE book_authors
    ADD FOREIGN KEY (author_id) REFERENCES authors (author_id) ON DELETE CASCADE;
--DATA ENTRIES SECTION (hashed password is Password1)
INSERT INTO users (username, hashedPassword, salt, first_name, last_name, email, is_admin)
VALUES ('admin', '$2a$10$D0uvz6/IgaKHVjV7zdlXAe8L92nEexa4gkNV7zyLtCRUTIyJEVKxy', '$2a$10$D0uvz6/IgaKHVjV7zdlXAe',
        'admin', 'admin', 'admin@email.com', true);



