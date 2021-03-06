/*
 * Copyright (C) 2015 The Android Open Source Project
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
package annekenl.nanomovies2.favdata;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static annekenl.nanomovies2.favdata.FavoritesContract.FavoriteEntry.TABLE_NAME;


//modified from Sunshine example project - Udacity Android Nanodegree

/**
 * This class serves as the ContentProvider for all of a user's favorited movies data.
 * This class allows us to insert data, query data, and delete data.
 *
 */
public class FavoritesProvider extends ContentProvider {

    /*
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make that matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     *
     // Define final integer constants for the directory of tasks and a single item.
     // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
     */
    public static final int CODE_FAVORITES = 100;
    public static final int CODE_FAVORITE_WITH_MOVIE_ID = 101;

    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of FavoritesProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoritesDbHelper mOpenHelper;

    /**
     * Creates the UriMatcher that will match each URI to the CODE_FAVORITE and
     * CODE_WEATHER_WITH_DATE constants defined above.
     *
     * @return A UriMatcher that correctly matches the constants for CODE_FAVORITES and CODE_WEATHER_WITH_DATE
     */
    public static UriMatcher buildUriMatcher() {

        /*
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. The code passed into the constructor of UriMatcher here represents the code to
         * return for the root URI. It's common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoritesContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change. In Sunshine, we use CODE_WEATHER or CODE_WEATHER_WITH_DATE.
         */

        /* This URI is content://annekenl.nanomovies2/favorites/ */
        matcher.addURI(authority, FavoritesContract.PATH_FAVORITES, CODE_FAVORITES);

        /*
         * This URI would look something like content://annekenl.nanomovies2/favorites/395992
         * The "/#" signifies to the UriMatcher that if PATH_FAVORITES is followed by ANY number,
         * that it should return the CODE_FAVORITE_WITH_MOVIE_ID code
         */
        matcher.addURI(authority, FavoritesContract.PATH_FAVORITES + "/#", CODE_FAVORITE_WITH_MOVIE_ID);

        return matcher;
    }


    /**
     * In onCreate, we initialize our content provider on startup. This method is called for all
     * registered content providers on the application main thread at application launch time.
     * It must not perform lengthy operations, or application startup will be delayed.
     *
     * Nontrivial initialization (such as opening, upgrading, and scanning
     * databases) should be deferred until the content provider is used (via {@link #query},
     * {@link #bulkInsert(Uri, ContentValues[])}, etc).
     *
     * Deferred initialization keeps application startup fast, avoids unnecessary work if the
     * provider turns out not to be needed, and stops database errors (such as a full disk) from
     * halting application launch.
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {
        /*
         * As noted in the comment above, onCreate is run on the main thread, so performing any
         * lengthy operations will cause lag in your app. Since FavoriteDbHelper's constructor is
         * very lightweight, we are safe to perform that initialization here.
         */
        mOpenHelper = new FavoritesDbHelper(getContext());
        return true;
    }

    /**
     *
     * // Implement insert to handle requests to insert a single new row of data
     * @param uri    The URI of the insertion request. This must not be null.
     * @param values A set of column_name/value pairs to add to the database.
     *               This must not be null
     * @return the URI for the newly inserted item.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the favorites directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        switch (match) {
            case CODE_FAVORITES:
                // Insert new values into the database
                // Inserting values into tasks table
                long rowId = db.insert(TABLE_NAME, null, values);
                if ( rowId != -1) {
                    returnUri = ContentUris.withAppendedId(FavoritesContract.FavoriteEntry.CONTENT_URI, Long.parseLong(
                                                                 values.getAsString(FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID)) );
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }


    /**
     * Handles query requests from clients.
     *
     * @param uri           The URI to query
     * @param projection    The list of columns to put into the cursor. If null, all columns are
     *                      included.
     * @param selection     A selection criteria to apply when filtering rows. If null, then all
     *                      rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the
     *                      selection.
     * @param sortOrder     How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {

            /*
             * When sUriMatcher's match method is called with a URI that looks something like this
             *
             *      content://annekenl.nanomovies2/favorites/395992
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return one movie. The movie id is at the very end of the URI and can be accessed
             * programmatically using Uri's getLastPathSegment method.
             *
             * In this case, we want to return a cursor that contains one row of weather data for
             * a particular date.
             */
            case CODE_FAVORITE_WITH_MOVIE_ID: {

                /*
                 * In order to determine the movie id associated with this URI, we look at the last
                 * path segment. In the comment above, the last path segment is 395992.
                 */
                String movid = uri.getLastPathSegment();

                /*
                 * The query method accepts a string array of arguments, as there may be more
                 * than one "?" in the selection statement. Even though in our case, we only have
                 * one "?", we have to create a string array that only contains one element
                 * because this method signature accepts a string array.
                 */
                String[] selectionArguments = new String[]{movid};

                cursor = mOpenHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        FavoritesContract.FavoriteEntry.TABLE_NAME,
                        /*
                         * A projection designates the columns we want returned in our Cursor.
                         * Passing null will return all columns of data within the Cursor.
                         * However, if you don't need all the data from the table, it's best
                         * practice to limit the columns returned in the Cursor with a projection.
                         */
                        projection,
                        /*
                         * The URI that matches CODE_FAVORITE_WITH_MOVIE_ID contains a movie id at the end
                         * of it. We extract that movie id and use it with these next two lines to
                         * specify the row of favorites we want returned in the cursor. We use a
                         * question mark here and then designate selectionArguments as the next
                         * argument for performance reasons. Whatever Strings are contained
                         * within the selectionArguments array will be inserted into the
                         * selection statement by SQLite under the hood.
                         */
                        FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);

                break;
            }

            /*
             * When sUriMatcher's match method is called with a URI that looks EXACTLY like this
             *
             *      content://annekenl.nanomovies2/favorites/
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return all of the favorites in our favorites table.
             *
             * In this case, we want to return a cursor that contains every row of favorite data
             * in our favorites table.
             */
            case CODE_FAVORITES: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        FavoritesContract.FavoriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Deletes data at a given URI with optional arguments for more fine tuned deletions.
     *
     * @param uri           The full URI to query
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs Used in conjunction with the selection statement
     * @return The number of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {

            // Handle the single item case, recognized by the ID included in the URI path
            case CODE_FAVORITE_WITH_MOVIE_ID:
                /*
                 * In order to determine the movie id associated with this URI, we look at the last
                 * path segment. In the comment above, the last path segment is 395992.
                 */
                String movid = uri.getLastPathSegment();

                // Use selections/selectionArgs to filter for this ID
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(TABLE_NAME,
                        FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID + " = ? ", new String[]{movid});
                break;

            case CODE_FAVORITES:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        FavoritesContract.FavoriteEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    /**
     * getType is an abstract method in ContentProvider. Normally, this method handles requests
     * for the MIME type of the data at the given URI. For example, if your app provided images
     * at a particular URI, then you would return an image URI from this method.
     *
     * @param uri the URI to query.
     * @return normally a MIME type string, or null if there is no type.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Not yet implemented.");
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("Not yet implemented");
    }


    /**
     * You do not need to call this method. This is a method specifically to assist the testing
     * framework in running smoothly. You can read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

}
