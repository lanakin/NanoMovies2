package annekenl.nanomovies2;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
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
import static annekenl.nanomovies2.R.id.container;

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

        mFavMovieAdapter = new MovieAdapter(mFavMovies);

        new FavoriteMoviesTask().execute();

        setRetainInstance(true);
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
    public void onStart()
    {
        super.onStart();

        //Log.d("favs","onstart");
        //new FavoriteMoviesTask().execute();
            // favorites in db might have change if user removes a favorite in the details screen
            // removing this in favor of requirement that user be able to return to same position
            // in list.
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
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

        getActivity().getFragmentManager().beginTransaction()
                .replace(container, frag)
                .addToBackStack("fav_movie_list_details")
                .commit();
    }

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
    public class FavoriteMoviesTask extends AsyncTask<Void, Void, Cursor>
    {
        @Override
        protected Cursor doInBackground(Void... params)
        {
           Cursor c = getContext().getContentResolver()
                   .query(FavoritesContract.FavoriteEntry.CONTENT_URI,null,null,null,null);

            return c;
        }

        @Override
        protected void onPostExecute(Cursor c)
        {
            if(c != null) {

                //Log.d("onpostexec",c.getCount()+"");

                try {
                    // favorites in db might have change if user removes a favorite in the details screen
                    // removing this in favor of requirement that user be able to return to same position
                    // in list.
                        //mFavMovies = new ArrayList<MovieItem>();
                        //mFavMovieAdapter = new MovieAdapter(mFavMovies);

                    while (c.moveToNext())
                    {
                        MovieItem movieItem = new MovieItem();
                        movieItem.setPosterPath(c.getString(MovieItemDBHelper.INDEX_POSTER));
                        movieItem.setOverview(c.getString(MovieItemDBHelper.INDEX_OVERVIEW));
                        movieItem.setRelease_date(c.getString(MovieItemDBHelper.INDEX_RDATE));
                        movieItem.setTitle(c.getString(MovieItemDBHelper.INDEX_TITLE));
                        movieItem.setPopularity(Double.parseDouble(c.getString(MovieItemDBHelper.INDEX_POPULARITY)));
                        movieItem.setVote_average(Double.parseDouble(c.getString(MovieItemDBHelper.INDEX_VOTE_AVG)));
                        movieItem.setId(c.getString(MovieItemDBHelper.INDEX_MOVIE_ID));

                        //trailers & reviews
                        String temp = c.getString(MovieItemDBHelper.INDEX_TRAILERS);
                        String[] tempArr = MovieItemDBHelper.convertStringToArray(temp);
                        movieItem.setTrailers(tempArr);

                        temp = c.getString(MovieItemDBHelper.INDEX_REVIEWS);
                        tempArr = MovieItemDBHelper.convertStringToArray(temp);
                        movieItem.setReviews(tempArr);

                        movieItem.setFavorite(true);

                        mFavMovies.add(movieItem);
                    }//end of c

                    c.close();
                    mFavMovieAdapter.notifyDataSetChanged();
                    //favPostersGrid.setAdapter(mFavMovieAdapter);

                    if(mFavMovies.isEmpty()) {
                       favPostersGrid.setVisibility(View.GONE);  //reveals a textview with 'empty message'
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    } //favmoviestask
}
