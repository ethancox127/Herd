package com.example.herd;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    //Constructor for Firestore Paging Adapter
    public PostAdapter(@NonNull FirestorePagingOptions<Post> options, ArrayList<String> postID,
                       OnItemClickListener clickListener) {
        super(options);

        //Initialize class variables
        this.postID = postID;
        this.clickListener = clickListener;
    }

    @Override
    protected void onBindViewHolder(PostViewHolder holder, int position, Post post) {
        //Bind data to view holder
        holder.postView.setText(post.getPost());
        holder.score.setText(Integer.toString(post.getScore()));
        holder.numComments.setText(Integer.toString(post.getNumComments()) + " comments");
        holder.timeFromPost.setText("5m");

        //Listen for score and numComments updates
        updateFields(holder, position);
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
        private TextView postView, score, numComments, timeFromPost;
        private ImageButton upvote, downvote;
        private LinearLayout post;

        //Variables for tracking if buttons have been clicked
        private boolean upvoteClicked = false, downvoteClicked = false;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            //Initialize UI elements
            postView = itemView.findViewById(R.id.PostText);
            score = itemView.findViewById(R.id.score);
            numComments = itemView.findViewById(R.id.numComments);
            timeFromPost = itemView.findViewById(R.id.timeFromPost);
            upvote = itemView.findViewById(R.id.upvote);
            downvote = itemView.findViewById(R.id.downvote);
            post = itemView.findViewById(R.id.postView);

            //Set click listeners for appropriate variables
            post.setOnClickListener(this);
            upvote.setOnClickListener(this);
            downvote.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //Determine which view was selected
            switch (view.getId()) {

                //A post was selected, return position to calling activity
                case R.id.postView:
                    clickListener.onRowClick(getAdapterPosition());
                    break;

                //Upvote button selected
                case R.id.upvote:
                    //If upvote button wasn't clicked, update the score and change the button
                    //images as necessary
                    if (upvoteClicked == false) {
                        upvoteClicked = true;
                        updateScore(postID.get(getAdapterPosition()), 1);
                        ((ImageButton) view).setImageResource(R.drawable.upvote_selected);
                        if (downvoteClicked == true) {
                            downvoteClicked = false;
                            (downvote).setImageResource(R.drawable.downvote);
                        }
                    }
                    break;

                //Downvote button selected
                case R.id.downvote:
                    //If downvote button wasn't clicked, update the score and change the button
                    //images as necessary
                    if (downvoteClicked == false) {
                        downvoteClicked = true;
                        updateScore(postID.get(getAdapterPosition()), -1);
                        ((ImageButton) view).setImageResource(R.drawable.downvote_selected);
                        if (upvoteClicked ==  true) {
                            upvoteClicked = false;
                            (upvote).setImageResource(R.drawable.upvote);
                        }
                    }
                    break;
            }
        }

        //Update the score for the appropriate post by value (1 or -1)
        private void updateScore(String postID, int value) {
            firestore.collection("posts").document(postID)
                    .update("score", FieldValue.increment(value))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Herd", "Post score update successful");
                            } else {
                                Log.d("Herd", "Post score update failed");
                                task.getException().printStackTrace();
                            }
                        }
                    });
        }
    }

    //Listen for updates to the post score and number of comments
    private void updateFields(PostViewHolder holder, int position) {
        //UI variables
        final TextView score = holder.score;
        final TextView numComments = holder.numComments;

        //Firestore listener for document with postID at given position
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

                            //If the snapshot score has changed, update the score text
                            if (snapshot.getLong("score") != Long.parseLong(score.getText().toString())) {
                                score.setText(snapshot.getLong("score").toString());
                            }

                            //Parse the number of comments to get int value
                            String curNumComments = numComments.getText().toString().split(" ")[0];
                            //If the number of comments has changed, update the view text
                            if (snapshot.getLong("numComments") != Long.parseLong(curNumComments)) {
                                numComments.setText(snapshot.getLong("numComments").toString() + " comments");
                            }
                        } else {
                            Log.d("Herd", "Current data: null");
                        }
                    }
                });
    }
}
