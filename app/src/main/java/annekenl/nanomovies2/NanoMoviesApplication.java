package annekenl.nanomovies2;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by annekenl1
 */

public class NanoMoviesApplication extends Application
{
    public static final String MOVIE_SETTINGS_PREFS = "MoviePosterPrefs";
    public static final String MOVIEDB_CONFIG_CHECK = "CONFIG_CHECK";
    public static final String MOVIE_DB_API_KEY = "25d9aac1cf6aabbef22617a8bc417dfd";

    @Override
    public void onCreate()
    {

        super.onCreate();

        Log.d("application", "oncreate");

        saveDateForPeriodicUpdate();
    }

    private void saveDateForPeriodicUpdate()
    {
        SharedPreferences prefs =  getSharedPreferences(MOVIE_SETTINGS_PREFS, 0);
        SharedPreferences.Editor edit = prefs.edit();

        //Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE,4); //days to wait

        edit.putLong(MOVIEDB_CONFIG_CHECK, now.getTimeInMillis());
        edit.commit();
    }
}
