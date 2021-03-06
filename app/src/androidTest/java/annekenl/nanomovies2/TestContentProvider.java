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

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import annekenl.nanomovies2.favdata.FavoritesContract;
import annekenl.nanomovies2.favdata.FavoritesDbHelper;
import annekenl.nanomovies2.favdata.FavoritesProvider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;


//modified from Udacity Android Nanodegree student lessons (lesson 9) & Sunshine project

@RunWith(AndroidJUnit4.class)
public class TestContentProvider {

    /* Context used to access various parts of the system */
    private final Context mContext = InstrumentationRegistry.getTargetContext();

    /**
     * Because we annotate this method with the @Before annotation, this method will be called
     * before every single method with an @Test annotation. We want to start each test clean, so we
     * delete all entries in the favorites directory to do so.
     */
    @Before
    public void setUp() {
        deleteAllRecordsFromFavoritesTable();
    }


    //================================================================================
    // Test ContentProvider Registration
    //================================================================================


    /**
     * This test checks to make sure that the content provider is registered correctly in the
     * AndroidManifest file. If it fails, you should check the AndroidManifest to see if you've
     * added a <provider/> tag and that you've properly specified the android:authorities attribute.
     */
    @Test
    public void testProviderRegistry() { //works

        /*
         * A ComponentName is an identifier for a specific application component, such as an
         * Activity, ContentProvider, BroadcastReceiver, or a Service.
         *
         * Two pieces of information are required to identify a component: the package (a String)
         * it exists in, and the class (a String) name inside of that package.
         *
         * We will use the ComponentName for our ContentProvider class to ask the system
         * information about the ContentProvider, specifically, the authority under which it is
         * registered.
         */
        String packageName = mContext.getPackageName();
        String favoritesProviderClassName = FavoritesProvider.class.getName();
        ComponentName componentName = new ComponentName(packageName, favoritesProviderClassName);

        try {

            /*
             * Get a reference to the package manager. The package manager allows us to access
             * information about packages installed on a particular device. In this case, we're
             * going to use it to get some information about our ContentProvider under test.
             */
            PackageManager pm = mContext.getPackageManager();

            /* The ProviderInfo will contain the authority, which is what we want to test */
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = packageName;

            /* Make sure that the registered authority matches the authority from the Contract */
            String incorrectAuthority =
                    "Error: FavoritesContentProvider registered with authority: " + actualAuthority +
                            " instead of expected authority: " + expectedAuthority;
            assertEquals(incorrectAuthority,
                    actualAuthority,
                    expectedAuthority);

        } catch (PackageManager.NameNotFoundException e) {
            String providerNotRegisteredAtAll =
                    "Error: FavoritesContentProvider not registered at " + mContext.getPackageName();
            /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            fail(providerNotRegisteredAtAll);
        }
    }


    //================================================================================
    // Test UriMatcher
    //================================================================================


    private static final Uri TEST_FAVORITES = FavoritesContract.FavoriteEntry.CONTENT_URI;
    // Content URI for a single task with id = 1
    private static final Uri TEST_FAVORITES_WITH_ID = TEST_FAVORITES.buildUpon().appendPath("1").build();


    /**
     * This function tests that the UriMatcher returns the correct integer value for
     * each of the Uri types that the ContentProvider can handle. Uncomment this when you are
     * ready to test your UriMatcher.
     */
    @Test
    public void testUriMatcher() { //works

        /* Create a URI matcher that the FavoritesContentProvider uses */
        UriMatcher testMatcher = FavoritesProvider.buildUriMatcher();

        /* Test that the code returned from our matcher matches the expected int */
        String favoritesUriDoesNotMatch = "Error: The FAVORITES URI was matched incorrectly.";
        int actualFavoritesMatchCode = testMatcher.match(TEST_FAVORITES);
        int expectedFavoritesMatchCode = FavoritesProvider.CODE_FAVORITES;

        assertEquals(favoritesUriDoesNotMatch,
                actualFavoritesMatchCode,
                expectedFavoritesMatchCode);

        /* Test that the code returned from our matcher matches the expected FAVORITES_WITH_ID */
        String favoritesWithIdDoesNotMatch =
                "Error: The FAVORITES_WITH_ID URI was matched incorrectly.";
        int actualFavoritesWithIdCode = testMatcher.match(TEST_FAVORITES_WITH_ID);
        int expectedFavoritesWithIdCode = FavoritesProvider.CODE_FAVORITE_WITH_MOVIE_ID;

        assertEquals(favoritesWithIdDoesNotMatch,
                actualFavoritesWithIdCode,
                expectedFavoritesWithIdCode);
    }


    //================================================================================
    // Test Insert
    //================================================================================
    //* Tests inserting a single row of data via a ContentResolver
     //
    @Test
    public void testInsert() { //works but really returning the base uri with the returned row id from db.insert() *FIXED*

        //* Create values to insert *//
        /* Obtain values from TestUtilities */
        ContentValues testValues = TestUtilities.createTestContentValues();

        //* TestContentObserver allows us to test if notifyChange was called appropriately *//*
        TestUtilities.TestContentObserver testObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        //* Register a content observer to be notified of changes to data at a given URI  *//*
        contentResolver.registerContentObserver(
                //* URI that we would like to observe changes to *//
                FavoritesContract.FavoriteEntry.CONTENT_URI,
                //* Whether or not to notify us if descendants of this URI change *//
                true,
                //* The observer to register (that will receive notifyChange callbacks) *//*
        testObserver);


        Uri retUri = contentResolver.insert(FavoritesContract.FavoriteEntry.CONTENT_URI, testValues);

        Uri expectedUri = ContentUris.withAppendedId(FavoritesContract.FavoriteEntry.CONTENT_URI, Long.parseLong("999999")); //*to do* remove hard coded string

        String insertProviderFailed = "Unable to insert item through Provider";

        //assertEquals(string message, expected, actual)
        assertEquals(insertProviderFailed, expectedUri, retUri);

        //If this fails, it's likely you didn't call notifyChange in your insert method from
        // your ContentProvider.
        testObserver.waitForNotificationOrFail();

        //waitForNotificationOrFail is synchronous, so after that call, we are done observing
        //changes to content and should therefore unregister this observer.
        contentResolver.unregisterContentObserver(testObserver);
    }




    //================================================================================
    // Test Query (for directory)
    //================================================================================
     //Inserts data, then tests if a query for the directory returns that data as a Cursor

    @Test
    public void testQuery() {

        //* Get access to a writable database *//*
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //* Create values to insert *//
        /* Obtain values from TestUtilities */
        ContentValues testValues = TestUtilities.createTestContentValues();

        //* Insert ContentValues into database and get a row ID back *//
        long taskRowId = database.insert(
                //* Table to insert values into *//
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                null,
                //* Values to insert into table *//*
                testValues);

        String insertFailed = "Unable to insert directly into the database";

        assertFalse(insertFailed, taskRowId == -1);

        //* We are done with the database, close it now. *//*
        database.close();

        //* Perform the ContentProvider query *//*
        Cursor taskCursor = mContext.getContentResolver().query(
                FavoritesContract.FavoriteEntry.CONTENT_URI,
                //* Columns; leaving this null returns every column in the table *//
                null,
                //* Optional specification for columns in the "where" clause above *//
                null,
                //* Values for "where" clause *//
                null,
                //* Sort order to return in Cursor *//
                null);


        String queryFailed = "Query failed to return a valid Cursor";

        assertFalse(queryFailed, taskCursor == null);

        /* We are done with the cursor, close it now. */
        taskCursor.close();
    }


    //================================================================================
    // Test Delete (for a single item)
    //================================================================================


    //Tests deleting a single row of data via a ContentResolver
    @Test
    public void testDelete() {
        //* Access writable database *//
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //* Create a new row of task data *//
        /* Obtain values from TestUtilities */
        ContentValues testValues = TestUtilities.createTestContentValues();

        //* Insert ContentValues into database and get a row ID back *//
        long taskRowId = database.insert(
                //* Table to insert values into *//
                FavoritesContract.FavoriteEntry.TABLE_NAME,
                null,
                //* Values to insert into table *//
                testValues);

        //* Always close the database when you're through with it *//
        database.close();

        String insertFailed = "Unable to insert into the database";

        assertFalse(insertFailed, taskRowId == -1);


        //* TestContentObserver allows us to test if notifyChange was called appropriately *//
        TestUtilities.TestContentObserver testObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        //* Register a content observer to be notified of changes to data at a given URI (tasks) *//
        contentResolver.registerContentObserver(
                //* URI that we would like to observe changes to *//
                FavoritesContract.FavoriteEntry.CONTENT_URI,
                //* Whether or not to notify us if descendants of this URI change *//
                true,
                //* The observer to register (that will receive notifyChange callbacks) *//
                testObserver);



        //* The delete method deletes the previously inserted row with COLUMN_MOVIE_ID = 999999 *//
        Uri uriToDelete = FavoritesContract.FavoriteEntry.CONTENT_URI.buildUpon().appendPath("999999").build(); //*to do* remove hard coded string
        int tasksDeleted = contentResolver.delete(uriToDelete, null, null);

        String deleteFailed = "Unable to delete item in the database";

        assertFalse(deleteFailed, tasksDeleted == 0);

        //If this fails, it's likely you didn't call notifyChange in your delete method from your ContentProvider.
        testObserver.waitForNotificationOrFail();

        //*waitForNotificationOrFail is synchronous, so after that call, we are done observing
        //changes to content and should therefore unregister this observer.
        contentResolver.unregisterContentObserver(testObserver);
    }

    /**
     * This method will clear all rows from the table in our database.
     *
     * Please note:
     *
     * - This does NOT delete the table itself. We call this method from our @Before annotated
     * method to clear all records from the database before each test on the ContentProvider.
     *
     * - We don't use the ContentProvider's delete functionality to perform this row deletion
     * because in this class, we are attempting to test the ContentProvider. We can't assume
     * that our ContentProvider's delete method works in our ContentProvider's test class.
     */
    private void deleteAllRecordsFromFavoritesTable() {
        /* Access writable database through WeatherDbHelper */
        FavoritesDbHelper helper = new FavoritesDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        /* The delete method deletes all of the desired rows from the table, not the table itself */
        database.delete(FavoritesContract.FavoriteEntry.TABLE_NAME, null, null);

        /* Always close the database when you're through with it */
        database.close();
    }

}
