package annekenl.nanomovies2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainMovieActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MoviesListFragment())
                    .addToBackStack("main_movies_list")
                    .commit();
        }
    }

}
