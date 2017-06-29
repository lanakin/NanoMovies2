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

import android.net.Uri;
import android.provider.BaseColumns;


//modified from Sunshine example project - Udacity Android Nanodegree

/**
 * Defines table and column names for the weather database. This class is not necessary, but keeps
 * the code organized.
 */
public class FavoritesContract {

    /*
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * Play Store.
     */
    public static final String CONTENT_AUTHORITY = "annekenl.nanomovies2";

    /*
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
     * Possible paths that can be appended to BASE_CONTENT_URI to form valid URI's that Sunshine
     * can handle. For instance,
     *
     *     content://annekenl.nanomovies2/favorites/
     *     [           BASE_CONTENT_URI         ][ PATH_FAVORITES ]
     *
     * is a valid path for looking at favorties data.
     */
    public static final String PATH_FAVORITES = "favorites";

    /* Inner class that defines the table contents of the favorites table */
    public static final class FavoriteEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Favorites table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES)
                .build();

        /* Used internally as the name of our weather table. */
        public static final String TABLE_NAME = "favorites";


        public static final String COLUMN_TITLE = "title";

        /* Movie ID as returned by API, used to identify the particular movie from "The Movie Database" */
        public static final String COLUMN_MOVIE_ID = "movie_id";

        /* the last part of the url to the poster image in "The Movie Database" */
        public static final String COLUMN_POSTER = "poster_path";

        public static final String COLUMN_OVERVIEW= "overiew";

        /* release date yyyy-mm-dd */
        public static final String COLUMN_RDATE = "rdate";

        /* popularity is stored as a double representing popularity score on "The Movie Database" */
        public static final String COLUMN_POPULARITY = "popularity";

        /* vote_avg is stored as a double representing the voting average score on "The Movie Database" */
        public static final String COLUMN_VOTE_AVG = "vote_avg";

        /* a string array stored as a single string with items separated by comma. Items are the youtube key needed
           to form the youtube url of the video trailer.
         */
        public static final String COLUMN_TRAILERS = "trailers";

        /* a string array stored as a single string with items separated by comma. Items are the links
          to the reviews on www.themoviedb.org */
        public static final String COLUMN_REVIEWS = "reviews";

        /* boolean stored as 0 or 1 for if the movie is part of the favorites collection (useful for the UI - favorite button) */
        public static final String COLUMN_FAV_BOOL = "fav_bool";

    }
}