package com.example.herd.adapters;

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
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.example.herd.R;
import com.example.herd.interfaces.OnItemClickListener;
import com.example.herd.models.Post;
import com.example.herd.ui.comments_viewer.CommentsViewModel;
import com.example.herd.ui.posts_viewer.PostsViewModel;
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

    private final String TAG = "CommentsAdapter";

    //Local variables
    private ArrayList<Post> postList;
    private OnItemClickListener clickListener;
    private CommentsViewModel viewModel;
    private ArrayList<String> likes, dislikes, postID;
    private double userLat, userLon;

    public CommentsAdapter(CommentsViewModel viewModel, OnItemClickListener clickListener) {
        this.postList = viewModel.getPostList();
        this.clickListener = clickListener;
        this.viewModel = viewModel;
        this.likes = viewModel.getUserLikes();
        this.dislikes = viewModel.getUserDislikes();
        this.postID = viewModel.getPostIDList();
        this.userLat = viewModel.getLatitude();
        this.userLon = viewModel.getLongitude();
    }

    @NonNull
    @Override
    public CommentsAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom post view layout
        View postView = inflater.inflate(R.layout.post_view, parent, false);

        //Return a new holder for the custom post view
        CommentsAdapter.PostViewHolder postViewHolder = new CommentsAdapter.PostViewHolder(postView);
        return postViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.PostViewHolder holder, int position) {

        Post post = postList.get(position);
        int postDist = calcDistance(post.getLatitude(), post.getLongitude());

        holder.postView.setText(post.getPost());
        holder.score.setText(Integer.toString(post.getScore()));
        holder.numComments.setText(Integer.toString(post.getNumComments()) + " comments");
        holder.timeFromPost.setText(calcTimeAgo(post.getTime()));
        holder.distance.setText(postDist + " miles");

        if (likes != null && likes.contains(postID.get(position))) {
            holder.upvote.setImageResource(R.drawable.upvote_selected);
        } else {
            holder.upvote.setImageResource(R.drawable.upvote);
        }

        if (dislikes != null && dislikes.contains(postID.get(position))) {
            holder.downvote.setImageResource(R.drawable.downvote_selected);
        } else {
            holder.downvote.setImageResource(R.drawable.downvote);
        }
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

    public void updateItems() {
        this.postList = viewModel.getPostList();
        this.postID = viewModel.getPostIDList();
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView postView, score, numComments, timeFromPost, distance;
        private ImageButton upvote, downvote;
        private LinearLayout post;

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

            post.setOnClickListener(this);
            upvote.setOnClickListener(this);
            downvote.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onRowClick(view, getAdapterPosition());
        }
    }

}
