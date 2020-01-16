package com.example.herd;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.PostViewHolder> {

    //Local variables
    private ArrayList<Post> commentList;
    private ArrayList<String> commentID;
    private OnItemClickListener clickListener;
    private Float userLat, userLon;
    private HashMap<Integer, CommentsAdapter.PostViewHolder> holderList;
    private SharedPreferences sharedPreferences;

    public CommentsAdapter(ArrayList<Post> commentList, ArrayList<String> commentID,
                           SharedPreferences preferences, OnItemClickListener clickListener) {
        this.commentList = commentList;
        this.commentID = commentID;
        this.clickListener = clickListener;
        this.holderList = new HashMap<>();
        this.sharedPreferences = preferences;
        this.userLat = preferences.getFloat("latitude", 0);
        this.userLon = preferences.getFloat("longitude", 0);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        Post post = commentList.get(position);

        //Bind data to view holder
        holder.postView.setText(post.getPost());
        holder.score.setText(Integer.toString(post.getScore()));
        holder.numComments.setText(Integer.toString(post.getNumComments()) + " comments");
        holder.timeFromPost.setText(calcTimeAgo(post.getTime()));
        holder.distance.setText(calcDistance(post.getLatitude(), post.getLongitude()) + " miles");

        Set<String> likes, dislikes;

        if (position == 0) {
            likes = sharedPreferences.getStringSet("likes", new HashSet<String>());
            dislikes = sharedPreferences.getStringSet("dislikes", new HashSet<String>());
        } else {
            likes = sharedPreferences.getStringSet("Comment Likes", new HashSet<String>());
            dislikes = sharedPreferences.getStringSet("Comment Dislikes", new HashSet<String>());
        }

        if (likes != null && likes.contains(commentID.get(position))) {
            holder.upvote.setImageResource(R.drawable.upvote_selected);
        } else {
            holder.upvote.setImageResource(R.drawable.upvote);
        }

        if (dislikes != null && dislikes.contains(commentID.get(position))) {
            holder.downvote.setImageResource(R.drawable.downvote_selected);
        } else {
            holder.downvote.setImageResource(R.drawable.downvote);
        }

        if (position == 0) {
            holder.postView.setTextSize(36);
        }

        if (!holderList.containsKey(position)) {
            holderList.put(position, holder);
        }

    }

    public PostViewHolder getViewByPosition(int position) {
        return holderList.get(position);
    }

    public static String calcTimeAgo(Timestamp postTime) {
        Date now = new Date();
        Date postDate = postTime.toDate();
        long diffInMillies = now.getTime() - postDate.getTime();
        long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        long hours = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        long minutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
        long seconds = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        long weeks = days/7;
        if (weeks > 0) {
            return weeks + (weeks > 1 ? " weeks ago" : " week ago");
        } else if (days > 0) {
            return days + (days > 1 ? " days ago" : " day ago");
        } else if (hours > 0) {
            return hours + (hours > 1 ? " hours ago" : " hour ago");
        } else if (minutes > 0){
            return minutes + (minutes > 1 ? " minutes ago" : " minute ago");
        } else {
            return seconds + (seconds > 1 ? " seconds ago" : " second ago");
        }
    }

    private int calcDistance(Double latitude, Double longitude) {
        Location postLoc = new Location("");
        postLoc.setLatitude(latitude);
        postLoc.setLongitude(longitude);

        Location userLoc = new Location("");
        userLoc.setLatitude(userLat);
        userLoc.setLongitude(userLon);

        float distanceInMeters = userLoc.distanceTo(postLoc);
        int miles = (int) ((int) distanceInMeters*0.000621371192f);
        return miles;
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.post_view, parent, false);

        //Return a new holder for the custom post view
        PostViewHolder postViewHolder = new PostViewHolder(postView);
        return postViewHolder;
    }

    public class PostViewHolder extends ViewHolder implements View.OnClickListener {
        public TextView postView, score, numComments, timeFromPost, distance;
        public ImageButton upvote, downvote;
        public LinearLayout post;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            postView = (TextView) itemView.findViewById(R.id.PostText);
            score = (TextView) itemView.findViewById(R.id.score);
            numComments = (TextView) itemView.findViewById(R.id.numComments);
            timeFromPost = (TextView) itemView.findViewById(R.id.timeFromPost);
            distance = (TextView) itemView.findViewById(R.id.distance);
            upvote = (ImageButton) itemView.findViewById(R.id.upvote);
            downvote = (ImageButton) itemView.findViewById(R.id.downvote);
            post = (LinearLayout) itemView.findViewById(R.id.postView);

            upvote.setOnClickListener(this);
            downvote.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onRowClick(view, getAdapterPosition());
        }
    }
}
