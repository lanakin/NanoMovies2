package annekenl.nanomovies2;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import annekenl.nanomovies2.databinding.FragmentMovieDetailsBinding;
import annekenl.nanomovies2.utility.MovieItem;
import annekenl.nanomovies2.utility.MovieItemDBHelper;

import static annekenl.nanomovies2.NanoMoviesApplication.MOVIE_SETTINGS_PREFS;

/**
 * Created by annekenl1
 */

public class MoviesListDetailsFragment extends Fragment
{
    private MovieItem mMovieItem = null;
    private FragmentMovieDetailsBinding binding;
    //private MovieItemDBHelper movItem2DBHelper;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);

        mMovieItem = getArguments().getParcelable(MoviesListFragment.MOVIE_ITEM_KEY);

        String[] testArr = mMovieItem.getTrailers();
        Log.e("trailers",testArr.length+"");

        String[] testArr2 = mMovieItem.getReviews();
        Log.e("reviews",testArr2.length+"");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

        //data binding
        binding = FragmentMovieDetailsBinding.bind(rootView);
        binding.setMovieInfo(mMovieItem);

        setupTrailers();
        setupReviews();

        setFavButtonLabel();
        binding.favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAsFavorite(v);
            }
        });

        return rootView;
    }

    //Favorite Button - on details view load
    private void setFavButtonLabel()
    {
       /* if(!mMovieItem.isFavorite()) {
            binding.favButton.setText(getResources().getString(R.string.favorite_label_add));
        } else {
            binding.favButton.setText(getResources().getString(R.string.favorite_label_remove));
        }*/

        mMovieItem.setFavorite(MovieItemDBHelper
                .query(mMovieItem,getActivity().getContentResolver(),getActivity()));

        if(mMovieItem.isFavorite())
        {
            binding.favButton.setText(getResources().getString(R.string.favorite_label_remove));
        }
        else {
            binding.favButton.setText(getResources().getString(R.string.favorite_label_add));
        }
    }

    private void markAsFavorite(View v)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set title
        alertDialogBuilder.setTitle("Favorite Movies Collection");

        // *set dialog message*
       if(!mMovieItem.isFavorite()) {
           alertDialogBuilder
                   .setMessage("Add to your Favorites")
                   .setCancelable(false)
                   .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           //Toast t = makeText(getActivity(), "FAVORITED!", Toast.LENGTH_SHORT);
                          // t.show();

                           MovieItemDBHelper.insert(mMovieItem,getActivity().getContentResolver(),getActivity());

                           binding.favButton.setText(getResources().getString(R.string.favorite_label_remove));
                           mMovieItem.toggleFavorite();

                           dialog.dismiss();
                       }
                   })
                   .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           dialog.dismiss();
                       }
                   });
       }
       else {
        alertDialogBuilder
                   .setMessage("Remove from your Favorites")
                   .setCancelable(false)
                   .setPositiveButton("REMOVE",new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog,int id) {
                           //Toast t = makeText(getActivity(),"Removed from Favorites", Toast.LENGTH_SHORT);
                           //t.show();

                           MovieItemDBHelper.delete(mMovieItem,getActivity().getContentResolver(),getActivity());

                           binding.favButton.setText(getResources().getString(R.string.favorite_label_add));
                           mMovieItem.toggleFavorite();

                           dialog.dismiss();
                       }
                   })
                   .setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog,int id) {
                           dialog.dismiss();
                       }
                   });
       }
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    private void setupTrailers()
    {
        final Spinner trailersChoices;

        binding.movieDetailsTrailerChoices.detailsChoicesHeader.setText("Trailers:");

        trailersChoices = binding.movieDetailsTrailerChoices.itemSpinner;
        if(!(mMovieItem.getTrailers().length==0))
        {
            final String[] trailers = mMovieItem.getTrailers();
            trailersChoices.setAdapter(new ArrayItemAdapter(trailers,"Trailer"));

            //didn't like that spinner behavior is such that i couldn't play the same selected item twice/multiple times in a row
            /*trailersChoices.post(new Runnable() { //queued to not run on initial load
                @Override
                public void run() {
                    trailersChoices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                //String youtubeBaseUrl = "https://www.youtube.com/watch?v=";
                                //String youtubeKey = trailers[position];

                                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeBaseUrl + youtubeKey)));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            //do nothing - this is not however called if don't change the item...
                        }
                    });
                }
            });*/

            ImageView img = binding.movieDetailsTrailerChoices.itemIcon;
            img.setImageResource(R.drawable.ic_play_arrow_black_24px);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String youtubeKey = (String) trailersChoices.getSelectedItem();
                    String youtubeBaseUrl = "https://www.youtube.com/watch?v=";

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeBaseUrl + youtubeKey)));
                }
            });
        }
        else {
            binding.movieDetailsTrailerChoices.zeroItems.setVisibility(View.VISIBLE);
            binding.movieDetailsTrailerChoices.detailsChoicesRow.setVisibility(View.GONE);
        }
    }

    private void setupReviews()
    {
        final Spinner reviewsChoices;

        binding.movieDetailsReviewChoices.detailsChoicesHeader.setText("Reviews:");

        reviewsChoices = binding.movieDetailsReviewChoices.itemSpinner;
        if(!(mMovieItem.getReviews().length==0))
        {
            final String[] reviews = mMovieItem.getReviews();
            reviewsChoices.setAdapter(new ArrayItemAdapter(reviews,"Review"));

            ImageView img = binding.movieDetailsReviewChoices.itemIcon;
            img.setImageResource(R.drawable.ic_description_black_24px);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String article = (String) reviewsChoices.getSelectedItem();

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(article)));
                }
            });
        }
        else {
            binding.movieDetailsReviewChoices.zeroItems.setVisibility(View.VISIBLE);
            binding.movieDetailsReviewChoices.detailsChoicesRow.setVisibility(View.GONE);
        }
    }


    //DATA VIEW BINDING CUSTOM
    @BindingAdapter("nanomovie:poster_path")
    public static void loadImage(ImageView view, String posterPath)
    {
        //form complete poster path url
        SharedPreferences prefs =  view.getContext().getSharedPreferences(MOVIE_SETTINGS_PREFS, 0);
        String baseUrl = prefs.getString(MoviesListFragment.MOVIE_POSTER_BASE_URL,"");
        String posterSize = prefs.getString(MoviesListFragment.MOVIEDB_POSTER_SIZE, "");

        String posterUrl = baseUrl + posterSize + posterPath;

        Picasso.with(view.getContext()).load(posterUrl).noFade()
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .into(view);
    }


    @BindingAdapter("android:text")
    public static void setNumberText(TextView tv, Double num)
    {
        String temp = String.format("%.2f",num);
        tv.setText(temp);
    }


    private class ArrayItemAdapter extends BaseAdapter
    {
        private String[] items;
        private String type;

        public ArrayItemAdapter(String[] items, String type) {
            this.items = items;
            this.type = type;
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //String item = (String) getItem(position);

            if (convertView == null) { // if it's not recycled, initialize some attributes
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.my_spinner,
                        parent, false);
            }

            TextView txt = (TextView) convertView.findViewById(R.id.mySpinnerTV);
            txt.setText(type+" "+ (position+1));

            return convertView;
        }
    }

}
