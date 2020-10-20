--MOCK DATA
--TODO remove these as they are just test entries to play with

--test use
DELETE FROM users WHERE username != 'admin';
--can't delete a series if this is > 0 so set all series values of this field to 0
UPDATE series SET number_books_in_series=0; 
--need to delete all series FIRST (as authors can't be deleted until no series reference them.
DELETE FROM series; 
DELETE FROM authors; 

UPDATE users set salt='$2a$10$D0uvz6/IgaKHVjV7zdlXAe', hashedPassword='$2a$10$D0uvz6/IgaKHVjV7zdlXAe8L92nEexa4gkNV7zyLtCRUTIyJEVKxy' WHERE username='admin'

INSERT INTO users (username, hashedPassword, salt, first_name, last_name, email, is_admin) VALUES ('JamesJoyce', '$2a$10$D0uvz6/IgaKHVjV7zdlXAe8L92nEexa4gkNV7zyLtCRUTIyJEVKxy','$2a$10$D0uvz6/IgaKHVjV7zdlXAe', 'James', 'Joyce', 'jjoyce@email.com', false);
INSERT INTO authors(fname, lname, author_bib) VALUES ('James', 'Joyce', 'TEST AUTHOR');
--INSERT INTO SERIES(series_name, author_id, number_books_in_series, series_status) VALUES ('test series',1 , 1, 'COMPLETED');
--use select to determine the one and only author we are assigning this series to
INSERT INTO series (series_name, primary_author_id, number_books_in_series, series_status) SELECT  'test series', authors.author_id, 1, 'COMPLETED' FROM authors WHERE authors.fname='James' AND authors.lname='Joyce';

