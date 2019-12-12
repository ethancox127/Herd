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

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<String> postID;
    private OnItemClickListener clickListener;

    public PostAdapter(@NonNull FirestorePagingOptions<Post> options, ArrayList<String> postID,
                       OnItemClickListener clickListener) {
        super(options);
        this.postID = postID;
        this.clickListener = clickListener;
    }

    @Override
    protected void onBindViewHolder(PostViewHolder holder, int position, Post post) {
        holder.postView.setText(post.getPost());

        holder.score.setText(Integer.toString(post.getScore()));

        holder.numComments.setText(Integer.toString(post.getNumComments()) + " comments");

        holder.timeFromPost.setText("5m");

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
        private TextView postView, score, numComments, timeFromPost;
        private ImageButton upvote, downvote;
        private LinearLayout post;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            postView = itemView.findViewById(R.id.PostText);
            score = itemView.findViewById(R.id.score);
            numComments = itemView.findViewById(R.id.numComments);
            timeFromPost = itemView.findViewById(R.id.timeFromPost);
            upvote = itemView.findViewById(R.id.upvote);
            downvote = itemView.findViewById(R.id.downvote);
            post = itemView.findViewById(R.id.postView);

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
}
