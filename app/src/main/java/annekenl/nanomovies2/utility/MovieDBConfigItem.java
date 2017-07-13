package annekenl.nanomovies2.utility;

/**  //https://developers.themoviedb.org/3/configuration/get-api-configuration
 * Get the system wide configuration information. Some elements of the API require some knowledge of this configuration data.
 * The purpose of this is to try and keep the actual API responses as light as possible. It is recommended you cache this data within
 * your application and check for updates every few days.

 This method currently holds the data relevant to building image URLs as well as the change key map.

 To build an image URL, you will need 3 pieces of data. The base_url, size and file_path. Simply combine them all and you will have
 a fully qualified URL. Here’s an example URL:

 https://image.tmdb.org/t/p/w500/8uO0gUM8aNqYLs1OsTBQiXu0fEv.jpg

 The configuration method also contains the list of change keys which can be useful if you are building an app that consumes data from the
 change feed.
 */

public class MovieDBConfigItem
{
    private String tmdb_image_base_url = "";
    //private String[] backdrop_sizes;
    private String[] poster_sizes = null;

    private String a_poster_size = "";

    public String getTmdb_image_base_url() {
        return tmdb_image_base_url;
    }

    public void setTmdb_image_base_url(String tmdb_image_base_url) {
        this.tmdb_image_base_url = tmdb_image_base_url;
    }

    public String[] getPoster_sizes() {
        return poster_sizes;
    }

    public void setPoster_sizes(String[] poster_sizes) {
        this.poster_sizes = poster_sizes;
    }

    public String getPoster_size() {
        return a_poster_size;
    }

    public void setPoster_size(String a_poster_size) {
        this.a_poster_size = a_poster_size;
    }

}
