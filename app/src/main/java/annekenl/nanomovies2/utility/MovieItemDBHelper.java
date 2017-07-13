package annekenl.nanomovies2.utility;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import annekenl.nanomovies2.favdata.FavoritesContract;

/**
 * Created by annekenl
 */

public class MovieItemDBHelper
{

    /** The columns of data that we are interested in displaying  (ALL)
     */
    public static final String[] FAVORITE_DETAIL_PROJECTION =  {
            FavoritesContract.FavoriteEntry.COLUMN_TITLE,
            FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID,
            FavoritesContract.FavoriteEntry.COLUMN_POSTER,
            FavoritesContract.FavoriteEntry.COLUMN_OVERVIEW,
            FavoritesContract.FavoriteEntry.COLUMN_RDATE,
            FavoritesContract.FavoriteEntry.COLUMN_POPULARITY,
            FavoritesContract.FavoriteEntry.COLUMN_VOTE_AVG,
            FavoritesContract.FavoriteEntry.COLUMN_TRAILERS,
            FavoritesContract.FavoriteEntry.COLUMN_REVIEWS,
            FavoritesContract.FavoriteEntry.COLUMN_FAV_BOOL
    };


    /** We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.*/
    //0 = _id
    public static final int INDEX_TITLE = 1;
    public static final int INDEX_MOVIE_ID = 2;
    public static final int INDEX_POSTER = 3;
    public static final int INDEX_OVERVIEW = 4;
    public static final int INDEX_RDATE = 5;
    public static final int INDEX_POPULARITY = 6;
    public static final int INDEX_VOTE_AVG = 7;
    public static final int INDEX_TRAILERS = 8;
    public static final int INDEX_REVIEWS = 9;
    public static final int INDEX_FAV_BOOL = 10;

    public static String strSeparator = "__,__";


    public static void insert(MovieItem movItem, ContentResolver contentResolver, Context context)
    {
        ContentValues values = new ContentValues();

        values.put(FavoritesContract.FavoriteEntry.COLUMN_TITLE, movItem.getTitle());
        values.put(FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID, movItem.getId());
        values.put(FavoritesContract.FavoriteEntry.COLUMN_POSTER, movItem.getPoster_path());
        values.put(FavoritesContract.FavoriteEntry.COLUMN_OVERVIEW, movItem.getOverview());
        values.put(FavoritesContract.FavoriteEntry.COLUMN_RDATE, movItem.getRelease_date());
        values.put(FavoritesContract.FavoriteEntry.COLUMN_POPULARITY, movItem.getPopularity());
        values.put(FavoritesContract.FavoriteEntry.COLUMN_VOTE_AVG, movItem.getVote_average());

        //convert string array to string
        String trailersStr = convertStrArrayToStr(movItem.getTrailers());
        values.put(FavoritesContract.FavoriteEntry.COLUMN_TRAILERS, trailersStr);
        String reviewsStr = convertStrArrayToStr(movItem.getReviews());
        values.put(FavoritesContract.FavoriteEntry.COLUMN_REVIEWS, reviewsStr);

        values.put(FavoritesContract.FavoriteEntry.COLUMN_FAV_BOOL, movItem.isFavorite() ? 1 : 0);

        // Insert the content values via a ContentResolver
        Uri uri = contentResolver.insert(FavoritesContract.FavoriteEntry.CONTENT_URI, values);

        // Display the URI that's returned with a Toast
        if(uri != null) {
          //  Toast.makeText(context, uri.toString(), Toast.LENGTH_LONG).show();
        } else {
            Log.e("SINGLE INSERT FAIL",uri.toString());
        }
    }

    //https://stackoverflow.com/questions/9053685/android-sqlite-saving-string-array
    public static String convertStrArrayToStr(String[] array)
    {
        String str = "";
        for (int i = 0;i<array.length; i++) {
            str = str+array[i];
            // Do not append comma at the end of last element
            if(i<array.length-1){
                str = str+strSeparator;
            }
        }
        return str;
    }

    public static String[] convertStringToArray(String str)
    {
        String[] arr = str.split(strSeparator);

        return arr;
    }


    public static void delete(MovieItem movItem, ContentResolver contentResolver, Context context)
    {
        try {

            Uri uriToDelete = FavoritesContract.FavoriteEntry.CONTENT_URI.buildUpon().appendPath(movItem.getId()).build();

            // delete via a ContentResolver
            int deleted = contentResolver.delete(uriToDelete,null,null);

            // Display the URI that's returned with a Toast
            if (deleted == 1) {
                //Toast.makeText(context, uriToDelete.toString() + " deleted", Toast.LENGTH_LONG).show();
            } else {
                Log.e("SINGLE DELETE FAIL", deleted + " ITEMS");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean query(MovieItem movItem, ContentResolver contentResolver, Context context)
    {
        boolean isFound = false;

        try {

            Uri uri = FavoritesContract.FavoriteEntry.CONTENT_URI.buildUpon().appendPath(movItem.getId()).build();

            // query via a ContentResolver
            Cursor found = contentResolver.query(
                    uri,
                    //* Columns; leaving this null returns every column in the table *//
                    null,
                    //* Optional specification for columns in the "where" clause above *//
                    null,
                    //* Values for "where" clause *//
                    null,
                    //* Sort order to return in Cursor *//
                    null);

            // Display the URI that's returned with a Toast
            if (found!=null && found.moveToFirst()) {
                //Toast.makeText(context, uri.toString() + " found in db", Toast.LENGTH_LONG).show();
                isFound = true;
                found.close();
            } else {
                Log.e("SINGLE QUERY FAIL", uri.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isFound;
    }
}
