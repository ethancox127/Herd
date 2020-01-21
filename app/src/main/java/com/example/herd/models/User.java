package com.example.herd.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {

    private String userID;
    private double latitude, longitude;
    private List<String> posts, comments, likedPosts, dislikedPosts, likedComments, dislikedComments;

    public User() { }

    public User(String userID, double latitude, double longitude, List<String> posts, List<String> comments,
                List<String> likedPosts, List<String> dislikedPosts, List<String> likedComments, List<String> dislikedComments) {
        this.userID = userID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.posts = posts;
        this.comments = comments;
        this.likedPosts = likedPosts;
        this.dislikedPosts = dislikedPosts;
        this.likedComments = likedComments;
        this.dislikedComments = dislikedComments;
    }

    public User(String userID, double latitude, double longitude) {
        this.userID = userID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.posts = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.likedPosts = new ArrayList<>();
        this.dislikedPosts = new ArrayList<>();
        this.likedComments = new ArrayList<>();
        this.dislikedComments = new ArrayList<>();
    }

    public User(Parcel in) {
        this.userID = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.posts = in.readArrayList(String.class.getClassLoader());
        this.comments = in.readArrayList(String.class.getClassLoader());
        this.likedPosts = in.readArrayList(String.class.getClassLoader());
        this.dislikedPosts = in.readArrayList(String.class.getClassLoader());
        this.likedComments = in.readArrayList(String.class.getClassLoader());
        this.dislikedComments = in.readArrayList(String.class.getClassLoader());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeList(posts);
        dest.writeList(comments);
        dest.writeList(likedPosts);
        dest.writeList(dislikedPosts);
        dest.writeList(likedComments);
        dest.writeList(dislikedComments);
    }

    //Getters
    public String getUserID() { return userID; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public List<String> getPosts() { return posts; }
    public List<String> getComments() { return comments; }
    public List<String> getLikedPosts() { return likedPosts; }
    public List<String> getDislikedPosts() { return dislikedPosts; }
    public List<String> getLikedComments() { return likedComments; }
    public List<String> getDislikedComments() { return dislikedComments; }

    //Setters
    public void setUserID(String userID) { this.userID = userID; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setPosts(List<String> posts) { this.posts = posts; }
    public void setComments(List<String> comments) { this.comments = comments; }
    public void setLikedPosts(List<String> likedPosts) { this.likedPosts = likedPosts; }
    public void setDislikedPosts(List<String> dislikedPosts) { this.dislikedPosts = dislikedPosts; }
    public void setLikedComments(List<String> likedComments) { this.likedComments = likedComments; }
    public void setDislikedComments(List<String> dislikedComments) { this.dislikedComments = dislikedComments; }
}
