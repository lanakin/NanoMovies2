package annekenl.nanomovies2;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import annekenl.nanomovies2.favdata.FavoritesContract;
import annekenl.nanomovies2.utility.MovieItem;
import annekenl.nanomovies2.utility.MovieItemDBHelper;

import static annekenl.nanomovies2.MoviesListFragment.MOVIEDB_POSTER_SIZE;
import static annekenl.nanomovies2.MoviesListFragment.MOVIE_ITEM_KEY;
import static annekenl.nanomovies2.MoviesListFragment.MOVIE_POSTER_BASE_URL;
import static annekenl.nanomovies2.NanoMoviesApplication.MOVIE_SETTINGS_PREFS;
import static annekenl.nanomovies2.utility.MovieItemDBHelper.ID_FAVORITES_LOADER;

/**
 * Created by annekenl
 *
 * To managed a list of Favorite Movies; otherwises functions similarly to the 'main' Movies List.
 * This fragment is utilized from users choosing "My Favorites" from the menu options
 */

public class FavMoviesListFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private GridView favPostersGrid;
    private ArrayList<MovieItem> mFavMovies = new ArrayList<MovieItem>();
    private MovieAdapter mFavMovieAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.favorite_movies_list));
        //setHasOptionsMenu(true);
            //it would make most sense for this to provide menu options that sort or interact with *Favorite (stored)* movies list
             //thus, a new menu that could operate similarly or different from the initial Popular Movies list
            //decided to leave this out for now and allow the user to use the back button to exit out of interacting with just "My Favorites" movies

        mFavMovieAdapter = new MovieAdapter(mFavMovies);

        getActivity().getSupportLoaderManager().initLoader(
                ID_FAVORITES_LOADER, null,
                new FavoriteMoviesParser());
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
        favPostersGrid = (GridView) rootView.findViewById(R.id.moviesPostersGrid);

        favPostersGrid.setAdapter(mFavMovieAdapter);

        favPostersGrid.setOnItemClickListener(this);

        return rootView;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        //MovieItem mMovieItem = mMovies.get(position);
        MovieItem selectedMovieItem = (MovieItem) parent.getItemAtPosition(position);

        //this should already be done the first time the movie is displayed in the Details Screen
            //from which the user can choose to add it to the Favorited Movies DB
        /*if(selectedMovieItem.getTrailers() == null
                || selectedMovieItem.getReviews() == null)
        {
            //get trailers
            new FetchTrailersTask().execute(selectedMovieItem); //notifydatasetchanged()

            //get reviews
            new FetchReviewsTask().execute(selectedMovieItem);  //" "; transitionToDetails()
        }
        else {
            transitionToDetails(selectedMovieItem);
        }*/

        transitionToDetails(selectedMovieItem);
    }

    private void transitionToDetails(MovieItem movieItem)
    {
        MoviesListDetailsFragment frag = new MoviesListDetailsFragment();

        Bundle mArgs = new Bundle();
        mArgs.putParcelable(MOVIE_ITEM_KEY,movieItem);
        frag.setArguments(mArgs);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, frag)
                .addToBackStack("fav_movie_list_details")
                .commit();
    }


    /* return to default movie list */


    private class MovieAdapter extends BaseAdapter
    {
        private ArrayList<MovieItem> moviesList;

        public MovieAdapter(ArrayList<MovieItem> moviesList)
        {
            super();
            this.moviesList = moviesList;
        }

        @Override
        public int getCount() {
            return moviesList.size();
        }

        @Override
        public MovieItem getItem(int position) {
            return moviesList.get(position);
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

            MovieItem currMovieItem = (MovieItem) moviesList.get(position);
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


    //modified from Sunshine example project - Udacity Android Nanodegree
    private class FavoriteMoviesParser implements LoaderManager.LoaderCallbacks<Cursor>
    {
        //private Uri mUri;

        // This connects our Activity into the loader lifecycle.
        // getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this); //on fav list frag. oncreate()


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
         * @param cursor   The cursor that is being returned.
         */
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
        {
            Log.d("onloadfin",cursor.getCount()+"");
            try {
                while (cursor.moveToNext())
                {
                    MovieItem movieItem = new MovieItem();
                    movieItem.setPosterPath(cursor.getString(MovieItemDBHelper.INDEX_POSTER));
                    movieItem.setOverview(cursor.getString(MovieItemDBHelper.INDEX_OVERVIEW));
                    movieItem.setRelease_date(cursor.getString(MovieItemDBHelper.INDEX_RDATE));
                    movieItem.setTitle(cursor.getString(MovieItemDBHelper.INDEX_TITLE));
                    movieItem.setPopularity(Double.parseDouble(cursor.getString(MovieItemDBHelper.INDEX_POPULARITY)));
                    movieItem.setVote_average(Double.parseDouble(cursor.getString(MovieItemDBHelper.INDEX_VOTE_AVG)));
                    movieItem.setId(cursor.getString(MovieItemDBHelper.INDEX_MOVIE_ID));

                    //trailers & reviews
                    String temp = cursor.getString(MovieItemDBHelper.INDEX_TRAILERS);
                    String[] tempArr = MovieItemDBHelper.convertStringToArray(temp);
                    movieItem.setTrailers(tempArr);

                    temp = cursor.getString(MovieItemDBHelper.INDEX_REVIEWS);
                    tempArr = MovieItemDBHelper.convertStringToArray(temp);
                    movieItem.setReviews(tempArr);

                    movieItem.setFavorite(true);

                    mFavMovies.add(movieItem);
                }//end of cursor
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            mFavMovieAdapter.notifyDataSetChanged();
            //favPostersGrid.invalidateViews();
            //favPostersGrid.setAdapter(mFavMovieAdapter);
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
