package annekenl.nanomovies2;

import android.os.Bundle;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainMovieActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MoviesListFragment())
                    //.addToBackStack("main_movies_list") //1st fragment
                    .commit();
        } else {
            String abTitle = savedInstanceState.getString("title");
            this.setTitle(abTitle);
        }

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Log.d("main activity","onBackPressed");

        FragmentManager fm = getFragmentManager();
        int backstackCount = fm.getBackStackEntryCount();
        if (backstackCount >= 1) {
            FragmentManager.BackStackEntry backstack = fm
                    .getBackStackEntryAt(backstackCount - 1);

            String currBackItemName = backstack.getName();
            Log.d("BACKSTACK", "" + currBackItemName);

            if(currBackItemName.equals("fav_movie_list")) {
                this.setTitle(getResources().getString(R.string.favorite_movies_list));
            }
            else if(currBackItemName.equals("movie_list_details")) {
                // stays the same
            }
        } else {
            this.setTitle(MoviesListFragment.mainMoviesTitle);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("title", (String) this.getTitle());
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }
}
