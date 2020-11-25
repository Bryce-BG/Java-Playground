

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
There classes are designed to provide a higher level access to the system to information. Any frontend for this app should ONLY interface with the database through the controller classes (instead of dao classes). These classes provide authentication for users modifying the database and also protect against some invalid changes.


allowed:
Multiple series can have the same name as long as the primary author is different.