--MOCK DATA

--remove all users that can be removed
DELETE
FROM users
WHERE username != 'admin';
--admin can't be deleted as it is a protected entry.

--can't delete a series if this is > 0 so set all series values of this field to 0
DELETE
FROM books;
UPDATE series
SET number_books_in_series=0;

--need to delete all series FIRST (as authors can't be deleted until no series reference them.
DELETE
FROM series;
DELETE
FROM authors;
DELETE
FROM genres;

--reset our admin accounts salt/hashedPassword
UPDATE users
set salt='$2a$10$D0uvz6/IgaKHVjV7zdlXAe',
    hashedPassword='$2a$10$D0uvz6/IgaKHVjV7zdlXAe8L92nEexa4gkNV7zyLtCRUTIyJEVKxy'
WHERE username = 'admin';

INSERT INTO users (username, hashedPassword, salt, first_name, last_name, email, is_admin)
VALUES ('JamesJoyce', '$2a$10$D0uvz6/IgaKHVjV7zdlXAe8L92nEexa4gkNV7zyLtCRUTIyJEVKxy', '$2a$10$D0uvz6/IgaKHVjV7zdlXAe',
        'James', 'Joyce', 'jjoyce@email.com', false);
INSERT INTO authors(fname, lname, author_bib)
VALUES ('James', 'Joyce', 'TEST AUTHOR');
INSERT INTO authors(fname, lname, author_bib)
VALUES ('Test', 'Author2', 'TEST AUTHOR');

--INSERT INTO SERIES(series_name, author_id, number_books_in_series, series_status) VALUES ('test series',1 , 1, 'COMPLETED');
--use select to determine the one and only author we are assigning this series to
--complex insertion relying on results from select query
INSERT INTO series (series_name, primary_author_id, number_books_in_series, series_status)
SELECT 'test series', authors.author_id, 3, 'COMPLETED'
FROM authors
WHERE authors.fname = 'James'
  AND authors.lname = 'Joyce';

--for testing book genre parsing
INSERT INTO genres (parent, genre_name, genre_description)
VALUES (null, 'TestGenre1', 'this genre is all for tests');
INSERT INTO genres (parent, genre_name, genre_description)
VALUES (null, 'TestGenre2', 'this genre is all for testing');

--TASK1: create book with no genres tagged
INSERT INTO books (cover_location, cover_name, description, has_identifiers, primary_author_id, publish_date, publisher,
                   series_id, title)
SELECT 'cover/1/2',
       'coverImage.jpg',
       'This is a test book, it does not exist',
       false,
       authors.author_id,
       null,
       null,
       null,
       'TestBook1'
FROM authors
WHERE authors.fname = 'James'
  AND authors.lname = 'Joyce';

--TASK2: create book with a single genre tagged
--1. insert our book into database.
INSERT INTO books (cover_location, cover_name, description, has_identifiers, primary_author_id, publish_date, publisher,
                   series_id, title)
SELECT 'cover/1/2',
       'coverImage.jpg',
       'This is a test book, it does not exist',
       false,
       authors.author_id,
       null,
       null,
       null,
       'TestBook2'
FROM authors
WHERE authors.fname = 'James'
  AND authors.lname = 'Joyce';
--2. add our genre tag to the book_genres table
INSERT INTO book_genres(book_id, genre_name)
SELECT book_id, genres.genre_name
FROM books,
     genres
WHERE genres.genre_name = 'TestGenre1'
  AND title = 'TestBook2';

--TASK3: create a book with both genres
--1. insert our book into database.
INSERT INTO books (cover_location, cover_name, description, has_identifiers, primary_author_id, publish_date, publisher,
                   series_id, title)
SELECT 'cover/1/2',
       'coverImage.jpg',
       'This is a test book, it does not exist',
       false,
       authors.author_id,
       null,
       null,
       null,
       'TestBook3'
FROM authors
WHERE authors.fname = 'James'
  AND authors.lname = 'Joyce';
--2. add our genre tag to the book_genres table
INSERT INTO book_genres(book_id, genre_name)
SELECT book_id, genres.genre_name
FROM books,
     genres
WHERE title = 'TestBook3';
--adds two rows to do unconstrianed join

--Go through books and ensure all "primary_author_id" for books are in book_authors table (this should NOT need to be done for books inserted using our library system as our functions should do it automatically
INSERT INTO book_authors(book_id, author_id)
SELECT books.book_id, books.primary_author_id
FROM books;

--Task4: create book with multiple authors
--1. insert our book into database
INSERT INTO books (count_authors, cover_location, cover_name, description, has_identifiers, primary_author_id,
                   publish_date, publisher, series_id, title)
SELECT 2,
       'cover/1/2',
       'coverImage.jpg',
       'This is a test book, it does not exist',
       false,
       authors.author_id,
       null,
       null,
       null,
       'TestBook4'
FROM authors
WHERE authors.fname = 'James'
  AND authors.lname = 'Joyce';
--2. insert author entries into our book_authors table
INSERT INTO book_authors (book_id, author_id)
SELECT books.book_id, authors.author_id
FROM books,
     authors
WHERE books.title = 'TestBook4';
--should add 2 rows as two authors exist currently


--For testing identifiers
--TODO create book with 1 identifiers
--TODO create book with 2 identifiers

--TASK5 create book in series
INSERT INTO books (count_authors, cover_location, cover_name, description, has_identifiers, primary_author_id,
                   publish_date, publisher, series_id, title)
SELECT 1,
       'cover/1/2',
       'coverImage.jpg',
       'This is a test book, it does not exist',
       false,
       authors.author_id,
       null,
       null,
       series.series_id,
       'TestBook5'
FROM authors,
     series
WHERE authors.fname = 'James'
  AND authors.lname = 'Joyce'
  AND series.series_name = 'test series';


INSERT INTO book_authors (book_id, author_id)
SELECT books.book_id, authors.author_id
FROM books,
     authors
WHERE books.title = 'TestBook5'
  AND authors.fname = 'James';


--For testing identifiers
--TASK6 create book with 1 identifiers
-- 1. create book
INSERT INTO books (count_authors, cover_location, cover_name, description, has_identifiers, primary_author_id,
                   publish_date, publisher, series_id, title)
SELECT 1,
       'cover/1/2',
       'coverImage.jpg',
       'This is a test book, it does not exist',
       true,
       authors.author_id,
       null,
       null,
       series.series_id,
       'TestBook6'
FROM authors,
     series
WHERE authors.fname = 'James'
  AND authors.lname = 'Joyce'
  AND series.series_name = 'test series';

-- 2. insert identifier for book
INSERT INTO book_identifier (book_id, identifier_type, identifier_value)
SELECT book_id, 'ISBN', '9780199535569'
FROM books
WHERE books.title = 'TestBook6';

-- 3. insert book author into author table.
INSERT INTO book_authors (book_id, author_id)
SELECT books.book_id, authors.author_id
FROM books,
     authors
WHERE books.title = 'TestBook6'
  AND authors.fname = 'James';

--TASK7 create book with 2 identifiers
-- 1. create book
INSERT INTO books (count_authors, cover_location, cover_name, description, has_identifiers, primary_author_id,
                   publish_date, publisher, series_id, title)
SELECT 1,
       'cover/1/2',
       'coverImage.jpg',
       'This is a test book, it does not exist',
       true,
       authors.author_id,
       null,
       null,
       series.series_id,
       'TestBook7'
FROM authors,
     series
WHERE authors.fname = 'James'
  AND authors.lname = 'Joyce'
  AND series.series_name = 'test series';

-- 2. insert identifier for book
INSERT INTO book_identifier (book_id, identifier_type, identifier_value)
SELECT book_id, 'ISBN', '0143105426'
FROM books
WHERE books.title = 'TestBook7';

INSERT INTO book_identifier (book_id, identifier_type, identifier_value)
SELECT book_id, 'UUID', '50f9f8b1-8a81-4dd5-b104-0766188d7d2c'
FROM books
WHERE books.title = 'TestBook7';

-- 3. insert book author into author table.
INSERT INTO book_authors (book_id, author_id)
SELECT books.book_id, authors.author_id
FROM books,
     authors
WHERE books.title = 'TestBook7'
  AND authors.fname = 'James';