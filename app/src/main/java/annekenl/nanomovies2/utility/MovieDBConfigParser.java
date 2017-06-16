package annekenl.nanomovies2.utility;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static annekenl.nanomovies2.NanoMoviesApplication.MOVIE_DB_API_KEY;

/**
 * Created by annekenl1
 */

public class MovieDBConfigParser extends AsyncTask<Void, Void, String>
{
    private MovieDBConfigItem moviedbconfigItem =  new MovieDBConfigItem();

    private final OnDownloadFinished listener;

    public interface  OnDownloadFinished
    {
        void onDownloadFinished(MovieDBConfigItem moviedbconfigItem);
    }

    public MovieDBConfigParser(OnDownloadFinished listener) {
        this.listener = listener;
    }

        @Override
        protected String doInBackground(Void... params)
        {
            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;

            String configJsonStr = null;

            try {
                // Construct the URL for The Movie Database API
                final String TMDB_BASE_URL = "https://api.themoviedb.org/3/configuration?";
                final String API_PARAM = "api_key";


                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
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
                configJsonStr = buffer.toString();
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
                        Log.e("Movies Config", "Error closing stream", e);
                    }
                }
            }

            Log.d(MovieDBConfigParser.class.getSimpleName(), configJsonStr);

            return configJsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                parseConfigJson(result);
            }
            else {
                result = "{\"images\":{}}";
            }
        }


    /*
    *
    * Example Result 1/18/17
    *
    * {
          "images": {
            "base_url": "http://image.tmdb.org/t/p/",
            "secure_base_url": "https://image.tmdb.org/t/p/",
            "backdrop_sizes": [
              "w300",
              "w780",
              "w1280",
              "original"
            ],
            "logo_sizes": [
              ...
            ],
            "poster_sizes": [
              "w92",
              "w154",
              "w185",
              "w342",
              "w500",
              "w780",
              "original"
            ],
            "profile_sizes": [
              ...
            ],
            "still_sizes": [
             ...
            ]
          },
          "change_keys": [
            "adult",
            "air_date",
            ...
            "videos"
          ]
        }
     */
    private void parseConfigJson(String json)
    {
        try
        {
            JSONObject mainImages = new JSONObject(json);

            if(mainImages.has("base_url")) {
                moviedbconfigItem.setTmdb_image_base_url(mainImages.getString("base_url"));
            }
            else {
                moviedbconfigItem.setTmdb_image_base_url("http://image.tmdb.org/t/p/"); //backup
            }

            if(mainImages.has("poster_sizes")) {
                JSONArray posterJsonArr = mainImages.getJSONArray("poster_sizes");

                String temp[] = new String[posterJsonArr.length()];
                for(int i = 0; i < temp.length; i++)
                {
                    temp[i] = posterJsonArr.getString(i);
                }

                moviedbconfigItem.setPoster_sizes(temp);
                moviedbconfigItem.setPoster_size(temp[temp.length-2]); //largest
            }
            else {
                String temp[] = new String[7];
                for(int i = 0; i < temp.length; i++)
                {
                    temp[0] = "w45";
                    temp[1] = "w92";
                    temp[2] = "w154";
                    temp[3] = "w185";
                    temp[4] = "w300";
                    temp[5] = "w500";
                    temp[6] = "original";
                }

                moviedbconfigItem.setPoster_sizes(temp);
                moviedbconfigItem.setPoster_size(temp[temp.length-2]); //largest
            }

            listener.onDownloadFinished(moviedbconfigItem);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
