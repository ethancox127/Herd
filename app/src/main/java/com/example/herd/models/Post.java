package com.example.herd.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Post implements Parcelable {

    //Private class variables corresponding with the views in post_view.xml
    private String post;
    private int score;
    private int numComments;
    private Timestamp time;
    private Double latitude;
    private Double longitude;
    private String userID;

    public Post() {

    }

    public Post(String post, int score, int numComments, Timestamp time, Double latitude,
                Double longitude, String userId) {
        this.post = post;
        this.score = score;
        this.numComments = numComments;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userID = userId;
    }

    protected Post(Parcel in) {
        post = in.readString();
        score = in.readInt();
        numComments = in.readInt();
        time = in.readParcelable(Timestamp.class.getClassLoader());
        latitude = in.readDouble();
        longitude = in.readDouble();
        userID = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getPost() {
        return this.post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getNumComments() {
        return this.numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public Timestamp getTime() {
        return this.time;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getUserID() {
        if (this.userID == null) {
            return "";
        } else {
            return this.userID;
        }
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(post);
        dest.writeInt(score);
        dest.writeInt(numComments);
        dest.writeParcelable(time, flags);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(userID);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("post = " + this.post + " ");
        sb.append("score = " + this.score + " ");
        sb.append("numComments = " + this.numComments);
        return sb.toString();
    }
}
