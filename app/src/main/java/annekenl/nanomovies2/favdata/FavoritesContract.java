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
     *     content://com.example.android.sunshine/weather/
     *     [           BASE_CONTENT_URI         ][ PATH_WEATHER ]
     *
     * is a valid path for looking at weather data.
     *
     *      content://com.example.android.sunshine/givemeroot/
     *
     * will fail, as the ContentProvider hasn't been given any information on what to do with
     * "givemeroot". At least, let's hope not. Don't be that dev, reader. Don't be that dev.
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



        /**
         * Builds a URI that adds the weather date to the end of the forecast content URI path.
         * This is used to query details about a single weather entry by date. This is what we
         * use for the detail view query. We assume a normalized date is passed to this method.
         *
         * @param date Normalized date in milliseconds
         * @return Uri to query details about a single weather entry
         */
        /*public static Uri buildWeatherUriWithDate(long date) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(date))
                    .build();
        }*/

        /**
         * Returns just the selection part of the weather query from a normalized today value.
         * This is used to get a weather forecast from today's date. To make this easy to use
         * in compound selection, we embed today's date as an argument in the query.
         *
         * @return The selection part of the weather query for today onwards
         */
       // public static String getSqlSelectForTodayOnwards() {
            //long normalizedUtcNow = SunshineDateUtils.normalizeDate(System.currentTimeMillis());
            //return WeatherEntry.COLUMN_DATE + " >= " + normalizedUtcNow;
        //}
    }
}