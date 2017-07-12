package annekenl.nanomovies2;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainMovieActivity extends AppCompatActivity
{
    //private String abTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        /*Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);*/
       // if(!abTitle.isEmpty())
          //  this.setTitle(abTitle);

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MoviesListFragment())
                    //.addToBackStack("main_movies_list") //1st fragment
                    .commit();
        } else {
            String abTitle = savedInstanceState.getString("title");
            this.setTitle(abTitle);
        }

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

  //  public void setActionBarTitle(String abTitle)
    //{
      //  this.abTitle = abTitle;
        //this.setTitle(abTitle); //works as a simple way to change the action bar title
   // }
    //apparently can also do getActivity().setTitle("");

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Log.d("main activity","onBackPressed");

        FragmentManager fm = getSupportFragmentManager();
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
