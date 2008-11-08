



http://www.onjava.com/pub/a/onjava/2007/01/31/tuning-derby.html?page=2
  SELECT * FROM tbl ORDER BY owner

If we now run our example with this query instead of the original one, the execution time will be an order of magnitude higher then before. Despite the fact that we paginated the results and dealt carefully with the number of rows to be fetched, the total execution time will again be about 8 seconds.

If we look at the query execution plan in the derby.log file, we can easily spot the problem:
Table Scan ResultSet for TBL at read committed isolation
level using instantaneous share row locking chosen
by the optimizer
This means that Derby performed look-up throughout the entire table in order to sort the row set. Now, what can we do to improve this situation? The answer is simple: create an index on this column. We can do that by issuing the following SQL statement:
CREATE INDEX tbl_owner ON tbl(owner)
If we now repeat our previous example, we should get a result similar to the one we got without ordering (under one second in my case).
Also, if you look into derby.log now, you will see a line like this (instead of a line like the previous one):

Index Scan ResultSet for TBL using index TBL_OWNER
at read committed isolation level using share row locking
chosen by the optimizer
which means you can be sure that Derby used our newly created index to get the appropriate rows.

grep "Table Scan" derby.log