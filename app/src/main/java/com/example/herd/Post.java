package com.example.herd;

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
    private GeoPoint point;
    private String userID;

    public Post() {

    }

    public Post(String post, int score, int numComments, Timestamp time, GeoPoint point, String userId) {
        this.post = post;
        this.score = score;
        this.numComments = numComments;
        this.time = time;
        this.point = point;
        this.userID = userId;
    }

    protected Post(Parcel in) {
        post = in.readString();
        score = in.readInt();
        numComments = in.readInt();
        time = in.readParcelable(Timestamp.class.getClassLoader());
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

    private static String calcTimeAgo(Long postTime) {
        if (postTime != 0) {
            //Get current timestamp
            long curTime = System.currentTimeMillis()/1000;
            long diffTime = curTime - postTime;
            String difference = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(diffTime),
                    TimeUnit.MILLISECONDS.toMinutes(diffTime) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(diffTime) % TimeUnit.MINUTES.toSeconds(1));
            return difference;
        } else {
            return "0 sec";
        }
    }

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

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public GeoPoint getPoint() {
        if (this.point == null) {
            return new GeoPoint(0, 0);
        } else {
            return this.point;
        }
    }

    public void setPoint(GeoPoint point) {
        this.point = point;
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
        dest.writeString(userID);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("post = " + this.post + "\n");
        return sb.toString();
    }
}
