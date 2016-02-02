package com.example.brandnewpeterson.projecttwo;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by brandnewpeterson on 1/26/16.
 */
public class CustomParcelable implements Parcelable {
    public String title;
    public String synopsis;
    public String rank;
    public String rating;
    public String year;
    public String id;
    public String movieObjToString;


    public CustomParcelable(String title, String synopsis, String rank, String rating, String year, String id, String movieObjToString) {
        this.title = title;
        this.synopsis = synopsis;
        this.rank = rank;
        this.rating = rating;
        this.year = year;
        this.id = id;
        this.movieObjToString = movieObjToString;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(synopsis);
        dest.writeString(rank);
        dest.writeString(rating);
        dest.writeString(year);
        dest.writeString(id);
        dest.writeString(movieObjToString);
    }

    // Creator
    public static final Creator<CustomParcelable> CREATOR
            = new Creator<CustomParcelable>() {

        @Override
        public CustomParcelable createFromParcel(Parcel in) {
            return new CustomParcelable(in);
        }

        @Override
        public CustomParcelable[] newArray(int size) {
            return new CustomParcelable[0];
        }
    };

    private CustomParcelable(Parcel in) {
        this.title = in.readString();
        this.synopsis = in.readString();
        this.rank = in.readString();
        this.rating = in.readString();
        this.year = in.readString();
        this.id = in.readString();
        this.movieObjToString = in.readString();

    }
}