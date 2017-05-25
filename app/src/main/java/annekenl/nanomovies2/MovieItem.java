package annekenl.nanomovies2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by annekenl1 on 11/7/16.
 */
public class MovieItem implements Parcelable
{
    private String poster_path = "";
    //private boolean adult;
    private String overview = "";

    private String release_date = "";
    private String formattedDate = "";

   // private String[] genre_ids;
   // private String id;
    private String title; //skipping "original title"
  //  private String backdrop_path;
    private double popularity = 0.0;
    //private int vote_count;
   // private String video;
    private double vote_average = 0.0;

    public MovieItem() { }

    //getter/setter methods
    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getPoster_path() { return  poster_path; }

    //public void setAdult(boolean adult) { this.adult = adult; }

   // public boolean getAdult() { return adult; }

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


   // public void setGenre_ids(String[] genre_ids) {
    //    this.genre_ids = genre_ids;
    //}

    //public String[] getGenre_ids() { return genre_ids; }

    //public String getId() {
     //   return id;
    ///}

   // public void setId(String id) {
        //this.id = id; }
    //

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    //public String getBackdrop_path() {
      //  return backdrop_path;
    //}

   // public void setBackdrop_path(String backdrop_path) {
      //  this.backdrop_path = backdrop_path;
    //}

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    //public int getVote_count() {
       // return vote_count;
   // }

    //public void setVote_count(int vote_count) {
      //  this.vote_count = vote_count;
   // }

    //public String getVideo() {
     //   return video;
    //}

   // public void setVideo(String video) {
      //  this.video = video;
    //}

    public double getVote_average() {
        return vote_average;
    }

    public void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }


    /** BELOW is Parcel Implementation Code; pass an object into another Activity easily **/
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(poster_path);
       // out.writeValue(adult);
        out.writeString(overview);
        out.writeString(release_date);
        out.writeString(formattedDate);
       // out.writeArray(genre_ids);
       // out.writeString(id);
        out.writeString(title);
        //out.writeString(backdrop_path);
        out.writeDouble(popularity);
       // out.writeInt(vote_count);
       // out.writeString(video);
        out.writeDouble(vote_average);
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
       // adult = (Boolean) in.readValue(null);
        overview = in.readString();
        release_date = in.readString();
        formattedDate = in.readString();
       // in.readStringArray(genre_ids);
      //  id = in.readString();
        title = in.readString();
       // backdrop_path = in.readString();
        popularity = in.readDouble();
       // vote_count = in.readInt();
       // video = in.readString();
        vote_average = in.readDouble();
    }


}
