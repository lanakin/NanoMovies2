package annekenl.nanomovies2.utility;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by annekenl1
 */
public class MovieItem implements Parcelable
{
    private String poster_path = "";
    private String overview = "";
    private String release_date = "";
    private String formattedDate = "";
    private String title;
    private double popularity = 0.0;
    private double vote_average = 0.0;

    private String id = "";
    private boolean isFavorite = false;
    private String[] trailers = null;
    private String[] reviews = null;


    public MovieItem() { }

    //getter/setter methods
    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getPoster_path() { return  poster_path; }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOverview() { return overview; }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;

        setFormattedDate();
    }

    public String getRelease_date() { return release_date; }

    public void setFormattedDate() {
        formattedDate = "";
        if(release_date != null) {
            String dateParts[] = release_date.split("-");
            if(dateParts.length == 3) {
                formattedDate += dateParts[1];
                formattedDate += "-" + dateParts[2];
                if(dateParts[0].length() == 4)
                    formattedDate += "-" + dateParts[0].substring(2,4);
            }
        }
    }

    public String getFormattedDate() { return formattedDate; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public double getVote_average() {
        return vote_average;
    }

    public void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFavorite() { return isFavorite; }

    public void toggleFavorite() { this.isFavorite = !this.isFavorite; }

    public void setFavorite(boolean fav) { isFavorite = fav; }

    public String[] getTrailers() {
        return trailers;
    }

    public void setTrailers(String[] trailers) {
        this.trailers = trailers;
    }

    public String[] getReviews() { return reviews; }

    public void setReviews(String[] reviews) { this.reviews = reviews; }


    /** BELOW is Parcel Implementation Code; pass an object into another Activity easily **/
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(poster_path);
        out.writeString(overview);
        out.writeString(release_date);
        out.writeString(formattedDate);
        out.writeString(title);
        out.writeDouble(popularity);
        out.writeDouble(vote_average);

        out.writeString(id);
        out.writeInt(isFavorite ? 1 : 0);
        out.writeStringArray(trailers);
        out.writeStringArray(reviews);
    }

    public static final Creator<MovieItem> CREATOR
            = new Creator<MovieItem>() {
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };

    private MovieItem(Parcel in)
    {
        poster_path = in.readString();
        overview = in.readString();
        release_date = in.readString();
        formattedDate = in.readString();
        title = in.readString();
        popularity = in.readDouble();
        vote_average = in.readDouble();

        id = in.readString();
        isFavorite = in.readInt()==1;
        in.readStringArray(trailers);
        in.readStringArray(reviews);
    }

}
