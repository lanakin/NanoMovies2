package annekenl.nanomovies2;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import javax.net.ssl.HttpsURLConnection;

import annekenl.nanomovies2.favdata.FavoritesContract;
import annekenl.nanomovies2.utility.MovieDBConfigItem;
import annekenl.nanomovies2.utility.MovieDBConfigParser;
import annekenl.nanomovies2.utility.MovieItem;
import annekenl.nanomovies2.utility.MovieItemToDBHelper;

import static annekenl.nanomovies2.NanoMoviesApplication.MOVIEDB_CONFIG_CHECK;
import static annekenl.nanomovies2.NanoMoviesApplication.MOVIE_DB_API_KEY;
import static annekenl.nanomovies2.NanoMoviesApplication.MOVIE_SETTINGS_PREFS;
import static annekenl.nanomovies2.utility.MovieItemToDBHelper.ID_FAVORITES_LOADER;


public class MoviesListFragment extends Fragment implements MovieDBConfigParser.OnDownloadFinished, AdapterView.OnItemClickListener
{
    private GridView postersGrid;
    private ArrayList<MovieItem> mMovies = new ArrayList<MovieItem>();

    public static final String MOVIE_POSTER_BASE_URL = "MOVIE_POSTER_BASE_URL";
    public static final String MOVIEDB_POSTER_SIZE= "MOVIE_POSTER_SIZE";
    public static final String MOVIE_ITEM_KEY= "MOVIE_ITEM";

    private MovieAdapter mMovieAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(shouldCheckMoviePosterConfig())
        {
            MovieDBConfigParser configObj = new MovieDBConfigParser(this);
            configObj.execute();
        }
        else
        {
            //will used last stored config info
            updateMoviesList();
        }
    }

    @Override
    public void onDownloadFinished(MovieDBConfigItem moviedbconfigItem)
    {
        SharedPreferences prefs =  getActivity().getSharedPreferences(MOVIE_SETTINGS_PREFS, 0);
        SharedPreferences.Editor edit = prefs.edit();

        //save downloaded config. info.
        edit.putString(MOVIE_POSTER_BASE_URL,moviedbconfigItem.getTmdb_image_base_url());
        edit.putString(MOVIEDB_POSTER_SIZE,moviedbconfigItem.getPoster_size()); //poster size for grid
        edit.commit();

        updateMoviesList(); //then download all popular movie info, display the posters in a grid.
    }


    private boolean shouldCheckMoviePosterConfig()
    {
        SharedPreferences prefs =  getActivity().getSharedPreferences(MOVIE_SETTINGS_PREFS, 0);

        Long checkTime = prefs.getLong(MOVIEDB_CONFIG_CHECK,0);

        Calendar now = Calendar.getInstance();

        if(now.getTimeInMillis() >= checkTime)
        {
            //do configuration download and update task
            return true;

        } else {
            return false;
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       int id = item.getItemId();

        if (id == R.id.most_popular_menu)
        {
            sortMoviesByMostPopular();
            mMovieAdapter.notifyDataSetChanged();
            return true;
        }
        else if (id == R.id.highest_rated_menu)
        {
            sortMoviesByHighestRated();
            mMovieAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.my_favorites_menu)
        {
            getActivity().getSupportLoaderManager().initLoader(
                    MovieItemToDBHelper.ID_FAVORITES_LOADER, null,
                    new FavoriteMoviesParser());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void updateMoviesList()
    {
        new FetchMoviesTask().execute();
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_movies_list, container, false);
        postersGrid = (GridView) rootView.findViewById(R.id.moviesPostersGrid);

        mMovieAdapter = new MovieAdapter();
        postersGrid.setAdapter(mMovieAdapter);

        postersGrid.setOnItemClickListener(this);

        return rootView;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        MovieItem mMovieItem = mMovies.get(position);

        if(mMovieItem.getTrailers() == null
                || mMovieItem.getReviews() == null)
        {
            //get trailers
            new FetchTrailersTask().execute(mMovieItem); //notifydatasetchanged()

            //get reviews
            new FetchReviewsTask().execute(mMovieItem);  //" "; transitionToDetails()
        }
        else {
            transitionToDetails(mMovieItem);
        }
    }

    private void transitionToDetails(MovieItem movieItem)
    {
        MoviesListDetailsFragment frag = new MoviesListDetailsFragment();

        Bundle mArgs = new Bundle();
        mArgs.putParcelable(MOVIE_ITEM_KEY,movieItem);
        frag.setArguments(mArgs);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, frag)
                .addToBackStack("movie_list_details")
                .commit();
    }


    private void sortMoviesByMostPopular()
    {
        Collections.sort(mMovies, new Comparator<MovieItem>() {
            public int compare(MovieItem o1, MovieItem o2) {
                return Double.compare(o1.getPopularity(),o2.getPopularity());
            }
        });

        Collections.reverse(mMovies); //greatest to least
    }

    private void sortMoviesByHighestRated()
    {
        Collections.sort(mMovies, new Comparator<MovieItem>() {
            public int compare(MovieItem o1, MovieItem o2) {
                return Double.compare(o1.getVote_average(),o2.getVote_average());
            }
        });

        Collections.reverse(mMovies); //greatest to least
    }


    private class MovieAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMovies.size();
        }

        @Override
        public MovieItem getItem(int position) {
            return mMovies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container)
        {
            if (convertView == null) { // if it's not recycled, initialize some attributes
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.fragment_movies_list_item,
                        container, false);
            }

            MovieItem currMovieItem = (MovieItem) mMovies.get(position);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.moviePostersImageView);

            //form complete poster path url
            SharedPreferences prefs =  getActivity().getSharedPreferences(MOVIE_SETTINGS_PREFS, 0);
            String baseUrl = prefs.getString(MOVIE_POSTER_BASE_URL,"");
            String posterSize = prefs.getString(MOVIEDB_POSTER_SIZE, "");
            String posterPath = currMovieItem.getPoster_path();

            String posterUrl = baseUrl + posterSize + posterPath;

            Picasso.with(getActivity()).load(posterUrl).noFade().placeholder(android.R.drawable.progress_indeterminate_horizontal).into(imageView);

            return convertView;
        }
    }


    //Typical AsyncTask for network query. Modified from example Sunshine app in Udacity Android Nanodegree
    private class FetchMoviesTask extends AsyncTask<Void, Void, String>
    {
        @Override
        protected String doInBackground(Void... params)
        {
            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            //if (params.length == 0)
            //return null;

            try {
                // Construct the URL for The Movie Database API
                final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
                final String API_PARAM = "api_key";
                final String PAGE_PARAM = "page";


                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendQueryParameter(API_PARAM, MOVIE_DB_API_KEY)
                        //optional
                        .appendQueryParameter(PAGE_PARAM, "1")
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Popular Movies", "Error closing stream", e);
                    }
                }
            }

            //Log.d(FetchMoviesTask.class.getSimpleName(), moviesJsonStr);

            return moviesJsonStr;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if(result != null) {

                parseMovieJson(result);
            }
        }

    }

    private void parseMovieJson(String json)
    {
        try
        {
            JSONObject mainObj = new JSONObject(json);

            if(mainObj.has("results"))
            {
                JSONArray results = mainObj.getJSONArray("results");

                //int total = 0;  //start of a possible feature to allow additional 'pages' of items

                //if(mainObj.has("total_results"))
                //{
                   // int overallTotal = mainObj.getInt("total_results")

                //}

                for(int i = 0; i < results.length(); i++)  //page 1 about 20 items ~
                {
                    JSONObject currMovie = results.getJSONObject(i);

                    MovieItem movieItem = new MovieItem();
                    movieItem.setPosterPath(currMovie.getString("poster_path"));
                    movieItem.setOverview(currMovie.getString("overview"));
                    movieItem.setRelease_date(currMovie.getString("release_date"));
                    movieItem.setTitle(currMovie.getString("title"));
                    movieItem.setPopularity(currMovie.getDouble("popularity"));
                    movieItem.setVote_average(currMovie.getDouble("vote_average"));

                    movieItem.setId(currMovie.getInt("id")+"");
                    //movieItem.setIsVideos(currMovie.getBoolean("video")); //all results have this as false...

                    //get trailers
                    //new FetchTrailersTask().execute(movieItem); //notifydatasetchanged

                    //get reviews
                    //new FetchReviewsTask().execute(movieItem); //this is about 40 asynctasks total here, it works but could be
                                                   //problematic and unnecessary until user wants additional details for a movie
                    mMovies.add(movieItem);
                }

                sortMoviesByMostPopular();
                mMovieAdapter.notifyDataSetChanged();
               //postersGrid.setAdapter(mMovieAdapter);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Typical AsyncTask for network query. Modified from example Sunshine app in Udacity Android Nanodegree
    private class FetchTrailersTask extends AsyncTask<MovieItem, Void, String>
    {
        MovieItem currMovie = null;

        @Override
        protected String doInBackground(MovieItem... params)
        {
            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailersJsonStr = null;

            if (params.length == 0)
                return null;

            currMovie = params[0];

            try {
                // Construct the URL for The Movie Database API
                final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String API_PARAM = "api_key";

                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendPath(currMovie.getId())
                        .appendPath("videos")
                        .appendQueryParameter(API_PARAM, MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                trailersJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Trailers", "Error closing stream", e);
                    }
                }
            }

            //Log.d(FetchTrailersTask.class.getSimpleName(), trailersJsonStr);

            return trailersJsonStr;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if(result != null) {

                parseTrailersJson(result,currMovie);
            }
        }

    }

    private void parseTrailersJson(String json, MovieItem movieItem)
    {
        try
        {
            JSONObject mainObj = new JSONObject(json);

            if(mainObj.has("results"))
            {
                JSONArray results = mainObj.getJSONArray("results");

                String[] tempArr = new String[results.length()];

                for(int i = 0; i < results.length(); i++)
                {
                    JSONObject currTrailerInfo = results.getJSONObject(i);

                    //getting just the info. needed to create youtube trailer
                    tempArr[i] = currTrailerInfo.getString("key");
                }

                movieItem.setTrailers(tempArr);

                mMovieAdapter.notifyDataSetChanged();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Typical AsyncTask for network query. Modified from example Sunshine app in Udacity Android Nanodegree
    private class FetchReviewsTask extends AsyncTask<MovieItem, Void, String>
    {
        MovieItem currMovie = null;

        @Override
        protected String doInBackground(MovieItem... params)
        {
            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String reviewsJsonStr = null;

            if (params.length == 0)
                return null;

            currMovie = params[0];

            try {
                // Construct the URL for The Movie Database API
                final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String API_PARAM = "api_key";
                final String PAGE_PARAM = "page";


                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendPath(currMovie.getId())
                        .appendPath("reviews")
                        .appendQueryParameter(API_PARAM, MOVIE_DB_API_KEY)
                        //optional
                        .appendQueryParameter(PAGE_PARAM, "1")
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                reviewsJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Trailers", "Error closing stream", e);
                    }
                }
            }

            //Log.d(FetchReviewsTask.class.getSimpleName(), reviewsJsonStr);

            return reviewsJsonStr;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if(result != null) {

                parseReviewsJson(result,currMovie);
            }
        }

    }

    private void parseReviewsJson(String json, MovieItem movieItem)
    {
        try
        {
            JSONObject mainObj = new JSONObject(json);

            if(mainObj.has("results"))
            {
                JSONArray results = mainObj.getJSONArray("results");

                String[] tempArr = new String[results.length()];

                for(int i = 0; i < results.length(); i++)
                {
                    JSONObject currTrailerInfo = results.getJSONObject(i);

                    //getting just the url
                    tempArr[i] = currTrailerInfo.getString("url");
                }

                movieItem.setReviews(tempArr);
                Log.d("reviews parse",movieItem.getTitle());

                mMovieAdapter.notifyDataSetChanged();

                transitionToDetails(movieItem); //this method and getReviews async task only called from onItemClick()
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    //modified from Sunshine example project - Udacity Android Nanodegree
    private class FavoriteMoviesParser implements LoaderManager.LoaderCallbacks<Cursor>
    {
        //private Uri mUri;

        // This connects our Activity into the loader lifecycle.
        // getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);


        /* *
          * Creates and returns a CursorLoader that loads the data for our URI and stores it in a Cursor.
          *
          * @param loaderId The loader ID for which we need to create a loader
          * @param loaderArgs Any arguments supplied by the caller
          *
          * @return A new Loader instance that is ready to start loading.*/
        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

            switch (loaderId) {

                case ID_FAVORITES_LOADER:

                    return new CursorLoader(getActivity(),
                            FavoritesContract.FavoriteEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                default:
                    throw new RuntimeException("Loader Not Implemented: " + loaderId);
            }
        }

        /**
         * Runs on the main thread when a load is complete. If initLoader is called (we call it from
         * onCreate in DetailActivity) and the LoaderManager already has completed a previous load
         * for this Loader, onLoadFinished will be called immediately. Within onLoadFinished, we bind
         * the data to our views so the user can see the details of the weather on the date they
         * selected from the forecast.
         *
         * @param loader The cursor loader that finished.
         * @param data   The cursor that is being returned.
         */
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {
        /* * Before we bind the data to the UI that will display that data, we need to check the
         * cursor to make sure we have the results that we are expecting. In order to do that, we
         * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
         * Although it may not seem obvious at first, moveToFirst will return true if it contains
         * a valid first row of data.
         *
         * If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.*/

            boolean cursorHasValidData = false;
            if (data != null && data.moveToFirst()) {
                //We have valid data, continue on to bind the data to the UI
                        cursorHasValidData = true;
            }

            if (!cursorHasValidData) {
               // No data to display, simply return and do nothing
                return;
            }

            //parseFavoritesDBData() - create and return an ArrayList of MovieItems for MoviesListFragment to use
            for(int i = 0; i < results.length(); i++)  //page 1 about 20 items ~
            {
                JSONObject currMovie = results.getJSONObject(i);

                MovieItem movieItem = new MovieItem();
                movieItem.setPosterPath(currMovie.getString("poster_path"));
                movieItem.setOverview(currMovie.getString("overview"));
                movieItem.setRelease_date(currMovie.getString("release_date"));
                movieItem.setTitle(currMovie.getString("title"));
                movieItem.setPopularity(currMovie.getDouble("popularity"));
                movieItem.setVote_average(currMovie.getDouble("vote_average"));

                movieItem.setId(currMovie.getInt("id")+"");
                //movieItem.setIsVideos(currMovie.getBoolean("video")); //all results have this as false...

                //get trailers
                //new FetchTrailersTask().execute(movieItem); //notifydatasetchanged

                //get reviews
                //new FetchReviewsTask().execute(movieItem); //this is about 40 asynctasks total here, it works but could be
                //problematic and unnecessary until user wants additional details for a movie
                mMovies.add(movieItem);
            }

            //sortMoviesByMostPopular();
            mMovieAdapter.notifyDataSetChanged();
            //postersGrid.setAdapter(mMovieAdapter)
        }

        /* Called when a previously created loader is being reset, thus making its data unavailable.
         * The application should at this point remove any references it has to the Loader's data.
         * Since we don't store any of this cursor's data, there are no references we need to remove.
         *
         * @param loader The Loader that is being reset.
        */
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }


}
