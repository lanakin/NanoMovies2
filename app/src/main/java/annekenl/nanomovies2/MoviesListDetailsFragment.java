package annekenl.nanomovies2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static annekenl.nanomovies2.NanoMoviesApplication.MOVIE_SETTINGS_PREFS;

/**
 * Created by annekenl1
 */

public class MoviesListDetailsFragment extends Fragment
{
    private MovieItem mMovieItem = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);

        mMovieItem = getArguments().getParcelable(MoviesListFragment.MOVIE_ITEM_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_movie_list_details_v3, container, false);

        ImageView poster = (ImageView) rootView.findViewById(R.id.movieDetailsImageView);

        //form complete poster path url
        SharedPreferences prefs =  getActivity().getSharedPreferences(MOVIE_SETTINGS_PREFS, 0);
        String baseUrl = prefs.getString(MoviesListFragment.MOVIE_POSTER_BASE_URL,"");
        String posterSize = prefs.getString(MoviesListFragment.MOVIEDB_POSTER_SIZE, "");
        String posterPath = mMovieItem.getPoster_path();

        String posterUrl = baseUrl + posterSize + posterPath;

        Picasso.with(getActivity()).load(posterUrl).noFade()
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .into(poster);

        TextView title = (TextView) rootView.findViewById(R.id.movieDetailsTitle);
        title.setText(mMovieItem.getTitle());

        TextView userRating = (TextView) rootView.findViewById(R.id.movieDetailsRatings);
        userRating.setText(mMovieItem.getVote_average()+"");

       TextView releaseDate = (TextView) rootView.findViewById(R.id.movieDetailsReleaseDate);

        String finalDateStr = "";
        if(mMovieItem.getRelease_date() != null) {
            String dateParts[] = mMovieItem.getRelease_date().split("-");
            if(dateParts.length == 3) {
                finalDateStr += dateParts[1];
                finalDateStr += "-" + dateParts[2];
                if(dateParts[0].length() == 4)
                    finalDateStr += "-" + dateParts[0].substring(2,4);
            }
        }

        releaseDate.setText(finalDateStr+"");

        TextView popularity = (TextView) rootView.findViewById(R.id.movieDetailsPopularity);
        String temp = String.format("%.2f",mMovieItem.getPopularity());
        popularity.setText(temp);

        TextView plot = (TextView) rootView.findViewById(R.id.movieDetailsOverview);
        plot.setText(mMovieItem.getOverview());

        return rootView;
    }

}