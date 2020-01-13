package com.example.herd;

import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

/*
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    //Local variables
    private ArrayList<Post> postList;
    private ArrayList<String> postID;
    private OnItemClickListener clickListener;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public PostAdapter(ArrayList<Post> postList, ArrayList<String> postID, OnItemClickListener clickListener) {
        this.postList = postList;
        this.postID = postID;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom post view layout
        View postView = inflater.inflate(R.layout.post_view, parent, false);

        //Return a new holder for the custom post view
        PostViewHolder postViewHolder = new PostViewHolder(postView);
        return postViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        TextView postView  = holder.postView;
        postView.setText(post.getPost());

        TextView score = holder.score;
        score.setText(Integer.toString(post.getScore()));

        TextView numComments  = holder.numComments;
        numComments.setText(Integer.toString(post.getNumComments()) + " comments");

        TextView timeFromPost = holder.timeFromPost;
        timeFromPost.setText("5m");

        updateFields(holder, position);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView postView, score, numComments, timeFromPost;
        private ImageButton upvote, downvote;
        private LinearLayout post;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            postView = (TextView) itemView.findViewById(R.id.PostText);
            score = (TextView) itemView.findViewById(R.id.score);
            numComments = (TextView) itemView.findViewById(R.id.numComments);
            timeFromPost = (TextView) itemView.findViewById(R.id.timeFromPost);
            upvote = (ImageButton) itemView.findViewById(R.id.upvote);
            downvote = (ImageButton) itemView.findViewById(R.id.downvote);
            post = (LinearLayout) itemView.findViewById(R.id.postView);

            post.setOnClickListener(this);
            upvote.setOnClickListener(this);
            downvote.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    private void updateFields(PostViewHolder holder, int position) {
        final TextView score = holder.score;
        final TextView numComments = holder.numComments;
        firestore.collection("posts").document(postID.get(position))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Log.d("Herd", "Current data: " + snapshot.getData());
                            if (snapshot.getLong("score") != Long.parseLong(score.getText().toString())) {
                                score.setText(snapshot.getLong("score").toString());
                            }
                            Log.d("Before split", numComments.getText().toString());
                            String curNumComments = numComments.getText().toString().split(" ")[0];
                            Log.d("After split", curNumComments);
                            if (snapshot.getLong("numComments") != Long.parseLong(curNumComments)) {
                                numComments.setText(snapshot.getLong("numComments").toString() + " comments");
                            }
                        } else {
                            Log.d("Herd", "Current data: null");
                        }
                    }
                });
    }
*/

public class PostAdapter extends FirestorePagingAdapter<Post, PostAdapter.PostViewHolder> {

    //Firestore and other class variables
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<String> postID;
    private OnItemClickListener clickListener;
    private String userID;
    private int time, distance;
    private Float userLat, userLon;
    private HashMap<Integer, PostViewHolder> holderList;
    private SharedPreferences sharedPreferences;

    //Constructor for Firestore Paging Adapter
    public PostAdapter(@NonNull FirestorePagingOptions<Post> options, ArrayList<String> postID,
                       SharedPreferences preferences, int time, int distance, OnItemClickListener clickListener) {
        super(options);

        //Initialize class variables
        this.postID = postID;
        this.sharedPreferences = preferences;
        userID = sharedPreferences.getString("User ID", "");
        this.time = time;
        this.distance = distance;
        this.userLat = sharedPreferences.getFloat("latitude", 0);
        this.userLon = sharedPreferences.getFloat("longitude", 0);
        this.clickListener = clickListener;
        this.holderList = new HashMap<>();
    }

    public PostAdapter(@NonNull FirestorePagingOptions<Post> options, ArrayList<String> postID) {
        super(options);
        this.postID = postID;
    }

    public void setPostID(ArrayList<String> postID) {
        this.postID = postID;
    }

    @Override
    protected void onBindViewHolder(final PostViewHolder holder, final int position, Post post) {
        Log.d("Herd", "In onBindViewHolder");

        int postDist = calcDistance(post.getLatitude(), post.getLongitude());
        int postTime = calcTime(post.getTime());
        if (postDist <= distance && postTime <= time) {
            //Bind data to view holder
            holder.postView.setText(post.getPost());
            holder.score.setText(Integer.toString(post.getScore()));
            holder.numComments.setText(Integer.toString(post.getNumComments()) + " comments");
            holder.timeFromPost.setText(calcTimeAgo(post.getTime()));
            holder.distance.setText(postDist + " miles");

            Set<String> likes = new HashSet<String>(sharedPreferences.getStringSet("likes",
                    new HashSet<String>()));
            Set<String> dislikes = new HashSet<String>(sharedPreferences.getStringSet("dislikes",
                    new HashSet<String>()));
            //Log.d("Liked posts", likes.toString());
            //Log.d("Dislikes posts", dislikes.toString());

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

            if (!holderList.containsKey(position)) {
                holderList.put(position, holder);
            }
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            if (position == 0)
                params.height = 1;
            else
                params.height = 0;
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            holder.itemView.setVisibility(View.GONE);
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

    private int calcTime(Timestamp postTime) {
        Date now = new Date();
        Date postDate = postTime.toDate();
        long diffInMillies = now.getTime() - postDate.getTime();
        long weeks = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)/7;
        return (int) weeks;
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
    public void refresh() {
        super.refresh();
        holderList.clear();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        //Inflate the custom post view layout
        View postView = inflater.inflate(R.layout.post_view, parent, false);

        //Return a new holder for the custom post view
        PostViewHolder postViewHolder = new PostViewHolder(postView);
        return postViewHolder;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //UI element variables for post view
        public TextView postView, score, numComments, timeFromPost, distance;
        public ImageButton upvote, downvote;
        public LinearLayout post;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            //Initialize UI elements
            postView = itemView.findViewById(R.id.PostText);
            score = itemView.findViewById(R.id.score);
            numComments = itemView.findViewById(R.id.numComments);
            timeFromPost = itemView.findViewById(R.id.timeFromPost);
            distance = itemView.findViewById(R.id.distance);
            upvote = itemView.findViewById(R.id.upvote);
            downvote = itemView.findViewById(R.id.downvote);
            post = itemView.findViewById(R.id.postView);

            post.setOnClickListener(this);
            upvote.setOnClickListener(this);
            downvote.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onRowClick(v, getAdapterPosition());
        }
    }
}
