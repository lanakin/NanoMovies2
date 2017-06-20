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
package annekenl.nanomovies2.favdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import annekenl.nanomovies2.favdata.FavoritesContract.FavoriteEntry;

//modified from Sunshine example project - Udacity Android Nanodegree

/**
 * Manages a local database for favorites data.
 */
public class FavoritesDbHelper extends SQLiteOpenHelper {

    /*
     * This is the name of our database. Database names should be descriptive and end with the
     * .db extension.
     */
    public static final String DATABASE_NAME = "favorites.db";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     */
    private static final int DATABASE_VERSION = 1;

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * This String will contain a simple SQL statement that will create a table that will
         * cache our weather data.
         */
        final String SQL_CREATE_FAVORITES_TABLE =

                "CREATE TABLE " + FavoritesContract.FavoriteEntry.TABLE_NAME + " (" +

                /*
                 * FavoriteEntry did not explicitly declare a column called "_ID". However,
                 * FavoriteEntry implements the interface, "BaseColumns", which does have a field
                 * named "_ID". We use that here to designate our table's primary key.
                 */
                FavoriteEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                FavoriteEntry.COLUMN_TITLE       + " TEXT NOT NULL, "                 +

                FavoriteEntry.COLUMN_POSTER   + " TEXT NOT NULL, "                    +

                FavoriteEntry.COLUMN_OVERVIEW   + " TEXT NOT NULL, "                    +

                FavoriteEntry.COLUMN_RDATE   + " TEXT NOT NULL, "                    +

                FavoriteEntry.COLUMN_POPULARITY   + " REAL NOT NULL, "                    +

                FavoriteEntry.COLUMN_VOTE_AVG + " REAL NOT NULL, "                    +

                FavoriteEntry.COLUMN_TRAILERS    + " TEXT NOT NULL, "                    +

                FavoriteEntry.COLUMN_REVIEWS + " TEXT NOT NULL,"                  +

                /*
                 * To ensure this table can only contain one favorite entry per movie/(movie id record in 'TMDB'),
                 * we declare the movie id column to be unique. We also specify "ON CONFLICT REPLACE". This tells
                 * SQLite that if we have a favorite entry for a certain movie id and we attempt to
                 * insert another favorite entry with that movie id, we replace the old favorite entry.
                 */
                " UNIQUE (" + FavoriteEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        //onCreate(sqLiteDatabase);

        /* TO DO UPDATE and KEEP USER'S EXISTING FAVORITED MOVIES */
    }
}