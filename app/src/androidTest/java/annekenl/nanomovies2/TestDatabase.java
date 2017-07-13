/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annekenl.nanomovies2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import annekenl.nanomovies2.favdata.FavoritesContract;
import annekenl.nanomovies2.favdata.FavoritesDbHelper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;


//modified from Udacity Android Nanodegree Sunshine project (resource qualifiers)

/**
 * Used to test the database. Within these tests, we
 * test the following:
 *
 * 1) Creation of the database with proper table(s)
 * 2) Insertion of single record into our weather table
 * 3) When a record is already stored in the favorites table with a particular movie_id, a new record
 * with the same movie_id will overwrite that record.
 * 4) Verify that NON NULL constraints are working properly on record inserts
 * 5) Verify auto increment is working with the _ID
 * 6) Test the onUpgrade functionality
 */
@RunWith(AndroidJUnit4.class)
public class TestDatabase {

    /*
     * Context used to perform operations on the database and create DbHelpers.
     */
    private final Context context = InstrumentationRegistry.getTargetContext();


    private static final String packageName = "annekenl.nanomovies2";
    //private static final String dataPackageName = packageName + ".data";

    private SQLiteDatabase database;
    private SQLiteOpenHelper dbHelper;

    /**
     * @Before annotation, this method will be called
     * before every single method with an @Test annotation.
     */
    @Before
    public void before()
    {
        //fresh start
        context.deleteDatabase(FavoritesDbHelper.DATABASE_NAME);

        /* Use DbHelper to get access to a writable database */
        dbHelper = new FavoritesDbHelper(context);
        database = dbHelper.getWritableDatabase();
    }


    @Test
    //reuses the same movie_id
    public void testDuplicateMovieIDInsertBehaviorShouldReplace()
    {

        /* Obtain values from TestUtilities */
        ContentValues testValues = TestUtilities.createTestContentValues();

        /*
         * Get the original title of the testValues to ensure we use a different
         * title for our next insert.
         */
        String originalTitle = testValues.getAsString(FavoritesContract.FavoriteEntry.COLUMN_TITLE);

        /* Insert the ContentValues with old movie ID into database */
        database.insert(
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                null,
                testValues);

        /*
         * We don't really care what this title is, just that it is different than the original and
         * that we can use it to verify our "new" entry has been made.
         */
        String newTitle = "MY NEW TITLE";

        testValues.put(FavoritesContract.FavoriteEntry.COLUMN_TITLE, newTitle);

        /* Insert the ContentValues with new title into database */
        database.insert(
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                null,
                testValues);

        /* Query for a record with our new title */
        String whereClause = FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID+"=?";
        String [] whereArgs = {"999999"};  //*to do* remove hard coded string

        Cursor newRecordCursor = database.query(
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                null, //all columns
                whereClause, //selection
                whereArgs, //selectionargs
                null,
                null,
                null);

        String recordWithNewDataNotFound =
                "New record did not overwrite the previous record for the same movie id.";
        assertTrue(recordWithNewDataNotFound,
                newRecordCursor.getCount() == 1);  //should only be 1 record

        newRecordCursor.moveToFirst();

        int index = newRecordCursor.getColumnIndex(FavoritesContract.FavoriteEntry.COLUMN_TITLE);

            /* Test to see if the column is contained within the cursor */
        String columnNotFoundError = "Column '" + FavoritesContract.FavoriteEntry.COLUMN_TITLE + "' not found. ";
        assertFalse(columnNotFoundError, index == -1);

            /* Test to see if the expected value equals the actual value (from the Cursor) */
        String expectedValue = newTitle; //"just to see actual value";
        String actualValue = newRecordCursor.getString(index);

        String valuesDontMatchError = "Actual value '" + actualValue
                + "' did not match the expected value '" + expectedValue + "'. ";

        assertEquals(valuesDontMatchError,
                expectedValue,
                actualValue);

        /* Always close the cursor after you're done with it */
        newRecordCursor.close();
    }


    /**
     * Tests the columns with null values cannot be inserted into the database.
     */
    @Test
    public void testNullColumnConstraints()
    {
         //We need a cursor from the table query to access the column names
        Cursor tableCursor = database.query(
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                 //We don't care about specifications, we just want the column names
                null, null, null, null, null, null);

        // Store the column names and close the cursor
        String[] tableColumnNames = tableCursor.getColumnNames();
        tableCursor.close();

        // Obtain values from TestUtilities and make a copy to avoid altering singleton
        ContentValues testValues = TestUtilities.createTestContentValues();
        // Create a copy of the testValues to save as a reference point to restore values
        ContentValues testValuesReferenceCopy = new ContentValues(testValues);

        for (String columnName : tableColumnNames) {

             //We don't need to verify the _ID column value is not null, the system does
            if (columnName.equals(FavoritesContract.FavoriteEntry._ID)) continue;

             //Set the value to null
            testValues.putNull(columnName);

             //Insert ContentValues into database and get a row ID back
            long shouldFailRowId = database.insert(
                    FavoritesContract.FavoriteEntry.TABLE_NAME,
                    null,
                    testValues);

            // If the insert fails, which it should in this case, database.insert returns -1
            String nullRowInsertShouldFail =
                    "Insert should have failed due to a null value for column: '" + columnName + "'"
                            + ", but didn't."
                            + "\n Check that you've added NOT NULL "
                            + " in your create table statement in the FavoriteEntry class."
                            + "\n Row ID: ";
            assertEquals(nullRowInsertShouldFail,
                    -1,
                    shouldFailRowId);

            // "Restore" the original value in testValues
            testValues.put(columnName, testValuesReferenceCopy.getAsString(columnName));
        }

         //Close database
        dbHelper.close();
    }


    /**
     * Tests to ensure that inserts into your database results in automatically
     * incrementing row IDs.
     */
     @Test
     public void testIntegerAutoincrement()
    {
         //First, let's ensure we have some values in our table initially
        testInsertSingleRecordIntoWeatherTable();

        // Obtain values from TestUtilities
        ContentValues testValues = TestUtilities.createTestContentValues();


         // Get the original movie_id of the testValues to ensure we use a different
         // movie_id later
        String originalMovID = testValues.getAsString(FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID);

         //Insert ContentValues into database and get a row ID back
        long firstRowId = database.insert(
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                null,
                testValues);

        // Delete the row we just inserted to see if the database will reuse the rowID
        database.delete(
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                "_ID == " + firstRowId,
                null);


        //Now we need to change the movie_id associated with our test content values because the
         // database policy is to replace identical movie_ids on conflict.
        String newTestMovID = "111111";
        testValues.put(FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID, newTestMovID);

        // Insert ContentValues into database and get another row ID back
        long secondRowId = database.insert(
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                null,
                testValues);

        String sequentialInsertsDoNotAutoIncrementId =
                "IDs were reused and shouldn't be if autoincrement is setup properly.";
        assertNotSame(sequentialInsertsDoNotAutoIncrementId,
                firstRowId, secondRowId);

        //adding another check
         /* Query for a record with our new movie_id */
        String whereClause = FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID+"=?";
        String [] whereArgs = {"111111"};  //*to do* remove hard coded string

        Cursor newRecordCursor = database.query(
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                null, //all columns
                whereClause, //selection
                whereArgs, //selectionargs
                null,
                null,
                null);

        String recordWithNewIDFound =
                "New record with new row id was not created.";
        assertTrue(recordWithNewIDFound,
                newRecordCursor.getCount() == 1);  //should only be 1 record
    }


    @Test
    public void testOnUpgradeBehavesCorrectly()
    {
        /* TO DO UPDATE and KEEP USER'S EXISTING FAVORITED MOVIES */
    }


    /**
     * This method tests that our database contains all of the tables that we think it should
     * contain. Although in our case, we just have one table that we expect should be added
     *
     * Despite only needing to check one table name, we set this method up so that
     * you can use it in other apps to test databases with more than one table.
     */
    @Test
    public void testCreateDb()
    {

         /* Will contain the name of every table in our database. Even though in our case, we only
         * have only table, in many cases, there are multiple tables. Because of that, we are
         * showing you how to test that a database with multiple tables was created properly. */

        final HashSet<String> tableNameHashSet = new HashSet<>();

       //  Here, we add the name of our only table in this particular database
        tableNameHashSet.add(FavoritesContract.FavoriteEntry.TABLE_NAME);

        // We think the database is open, let's verify that here
        String databaseIsNotOpen = "The database should be open and isn't";
        assertEquals(databaseIsNotOpen,
                true,
                database.isOpen());

         //This Cursor will contain the names of each table in our database
        Cursor tableNameCursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null);


        // * If tableNameCursor.moveToFirst returns false from this query, it means the database
        // * wasn't created properly. In actuality, it means that your database contains no tables.

        String errorInCreatingDatabase =
                "Error: This means that the database has not been created correctly";
        assertTrue(errorInCreatingDatabase,
                tableNameCursor.moveToFirst());


        // * tableNameCursor contains the name of each table in this database. Here, we loop over
       //  * each table that was ACTUALLY created in the database and remove it from the
       //  * tableNameHashSet to keep track of the fact that was added. At the end of this loop, we
       //  * should have removed every table name that we thought we should have in our database.
       //  * If the tableNameHashSet isn't empty after this loop, there was a table that wasn't
        // * created properly.

        do {
            tableNameHashSet.remove(tableNameCursor.getString(0));
        } while (tableNameCursor.moveToNext());

        // If this fails, it means that your database doesn't contain the expected table(s)
        assertTrue("Error: Your database was created without the expected tables.",
                tableNameHashSet.isEmpty());

        // Always close the cursor when you are finished with it
        tableNameCursor.close();
    }

    /**
     * This method tests inserting a single record into an empty table from a brand new database.
     * It will fail for the following reasons:
     *
     * 1) Problem creating the database
     * 2) A value of -1 for the ID of a single, inserted record
     * 3) An empty cursor returned from query on the table
     * 4) Actual values of weather data not matching the values from TestUtilities
     */
    @Test
    public void testInsertSingleRecordIntoWeatherTable()
    {
        // Obtain values from TestUtilities
        ContentValues testValues = TestUtilities.createTestContentValues();

       //  Insert ContentValues into database and get a row ID back
        long rowId = database.insert(
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                null,
                testValues);

       //  If the insert fails, database.insert returns -1
        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Unable to insert into the database";
        assertNotSame(insertFailed,
                valueOfIdIfInsertFails,
                rowId);


       //  * Query the database and receive a Cursor. A Cursor is the primary way to interact with
       //  * a database in Android.

        Cursor recordCursor = database.query(
                // Name of table on which to perform the query
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                // Columns; leaving this null returns every column in the table
                null,
                // Optional specification for columns in the "where" clause above
                null,
               //  Values for "where" clause
                null,
               //  Columns to group by
                null,
                 //Columns to filter by row groups
                null,
                // Sort order to return in Cursor
                null);

        // Cursor.moveToFirst will return false if there are no records returned from your query
        String emptyQueryError = "Error: No Records returned from weather query";
        assertTrue(emptyQueryError,
                recordCursor.moveToFirst());

         //Verify that the returned results match the expected results
        String expectedWeatherDidntMatchActual =
                "Expected values didn't match actual values.";
        TestUtilities.validateCurrentRecord(expectedWeatherDidntMatchActual,
                recordCursor,
                testValues);


        // * Since before every method annotated with the @Test annotation, the database is
        // * deleted, we can assume in this method that there should only be one record in our
        // * table because we inserted it. If there is more than one record, an issue has
        // * occurred.

        assertFalse("Error: More than one record returned from weather query",
                recordCursor.moveToNext());

        //Close cursor
        recordCursor.close();
    }
}