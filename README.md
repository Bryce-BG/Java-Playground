

#Class purposes

##Object classes
There are classes like:Series, Author, User, and Book. These are designed to contain the information extracted from one entry in their respective table (So User object contains information about a user in the database). These can and are used to pass information retrieved from the database about in an encapsulated manner 

## Dao classes
Any of the ___Dao.java classes except DAORoot) are generally designed to act as low level interfaces to their respective tables in the database. As such:
BookDao -> book table
SeriesDao -> series table
AuthorDao -> author table
UserDao -> user table
These classes provide an interface to add, delete, modify and get entries from their table. However, there is very little validation in these classes which could lead to illegal additions and deletions in the database (for example deleting an author when they have books that reference them (through book.author) in the database which would lead to an unwanted cascade delete to ensure consistency). As such, they have their respective: ___Controller classes.

##Controller classes
There classes are designed to provide a higher level access to the system to information. Any of the tables that have more complex systems (Users who must provide authentication, Authors who also are authenticated, Books where adding one cascades across other entries) will have an controller class used to protect our DB from items being added that are incorrect but not defined as such by the DB schema.


allowed:
Multiple series can have the same name as long as the primary author is different.