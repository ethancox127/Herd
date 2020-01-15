package com.example.herd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.local.QueryData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class PostCommentsActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //UI element variables
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView commentsRecyclerView;
    private FloatingActionButton floatingActionButton;

    //Comments variables
    private ArrayList<Post> commentList = new ArrayList<>();
    private ArrayList<String> commentID = new ArrayList<>();

    //Other variables
    private CommentsAdapter commentAdapter;
    private String docID;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    //Shared Preferences and editor
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Set<String> postLikes, postDislikes, commentLikes, commentDislikes;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comments);

        Log.d("PostCommentsActivity", "In onCreate");

        //Initialize UI variables
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.commentSwipeContainer);
        commentsRecyclerView = (RecyclerView) findViewById(R.id.commentRecyclerView);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.addCommentButton);

        //Set click and refresh listeners
        swipeRefreshLayout.setOnRefreshListener(this);
        floatingActionButton.setOnClickListener(this);

        //Initialize Shared Preferences and editor
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        postLikes = sharedPreferences.getStringSet("likes", new HashSet<String>());
        postDislikes = sharedPreferences.getStringSet("dislikes", new HashSet<String>());
        commentLikes = sharedPreferences.getStringSet("Comment Likes", new HashSet<String>());
        commentDislikes = sharedPreferences.getStringSet("Comment Dislikes", new HashSet<String>());
        userID = sharedPreferences.getString("User ID", "");

        //Get the post and post ID from the intent that started this activity
        Intent intent = getIntent();
        docID = intent.getStringExtra("Post ID");
        Post post = (Post) intent.getParcelableExtra("Post");

        //Add the post and postID to the comment lists
        commentList.add(post);
        commentID.add(docID);

        Log.d("Post", post.toString());
        Log.d("Post ID", docID);

        displayComments();
    }

    private void displayComments() {

        getComments(new CallbackInterface<QuerySnapshot>() {
            @Override
            public void onStart() { }

            @Override
            public void onSuccess(QuerySnapshot object) {
                for (DocumentSnapshot snapshot : object) {
                    commentList.add(snapshot.toObject(Post.class));
                    commentID.add(snapshot.getId());
                }

                commentAdapter = new CommentsAdapter(commentList, sharedPreferences, new OnItemClickListener() {
                    @Override
                    public void onRowClick(View view, int position) {

                        //Get the post id and associated holder
                        String id = commentID.get(position);
                        CommentsAdapter.PostViewHolder holder = commentAdapter.getViewByPosition(position);
                        int score =  Integer.valueOf(holder.score.getText().toString());
                        Set<String> likeSet, dislikeSet;

                        //Post was selected
                        if (position == 0) {

                            //Get the like and dislike sets for posts
                            likeSet = new HashSet<String>(sharedPreferences.getStringSet("Comment Likes",
                                    new HashSet<String>()));
                            dislikeSet = new HashSet<String>(sharedPreferences.getStringSet("Comment Dislikes",
                                    new HashSet<String>()));

                        } else {

                            //Get the like and dislike sets for comments
                            likeSet = new HashSet<String>(sharedPreferences.getStringSet("likes",
                                    new HashSet<String>()));
                            dislikeSet = new HashSet<String>(sharedPreferences.getStringSet("dislikes",
                                    new HashSet<String>()));

                        }

                        switch (view.getId()) {

                            case R.id.upvote:
                                //If upvote button is already selected notify user
                                if (likeSet != null && likeSet.contains(id)) {

                                    Toast.makeText(getApplicationContext(), "You already liked that post",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    //Add the liked post to shared preferences
                                    likeSet.add(id);
                                    holder.upvote.setImageResource(R.drawable.upvote_selected);
                                    score++;
                                    holder.score.setText(Integer.toString(score));

                                    if (position == 0) {
                                        editor.putStringSet("likes", likeSet);
                                        editor.apply();
                                        editor.commit();
                                        updateLikes(id);

                                        if (dislikeSet != null && dislikeSet.contains(id)) {
                                            dislikeSet.remove(id);
                                            editor.putStringSet("dislikes", dislikeSet);
                                            editor.apply();
                                            editor.commit();

                                            holder.downvote.setImageResource(R.drawable.downvote);

                                            removeDislike(id);
                                        }


                                    } else {
                                        editor.putStringSet("Comment Likes", likeSet);
                                        editor.apply();
                                        editor.commit();
                                        updateCommentLikes(id);

                                        if (dislikeSet != null && dislikeSet.contains(id)) {
                                            dislikeSet.remove(id);
                                            editor.putStringSet("Comment Dislikes", dislikeSet);
                                            editor.apply();
                                            editor.commit();

                                            holder.downvote.setImageResource(R.drawable.downvote);
                                            removeCommentDislike(id);
                                        }
                                    }
                                }
                                break;

                            case R.id.downvote:
                                //If downvote button is already selected notify user
                                if (dislikeSet != null && dislikeSet.contains(id)) {

                                    Toast.makeText(getApplicationContext(), "You already disliked that post",
                                            Toast.LENGTH_SHORT).show();

                                } else {

                                    //Add the disliked post to shared preferences
                                    dislikeSet.add(id);
                                    holder.downvote.setImageResource(R.drawable.downvote_selected);
                                    score--;
                                    holder.score.setText(Integer.toString(score));

                                    if (position == 0) {
                                        editor.putStringSet("dislikes", dislikeSet);
                                        editor.apply();
                                        editor.commit();
                                        updateDislikes(id);

                                        if (likeSet != null && likeSet.contains(id)) {
                                            likeSet.remove(id);
                                            editor.putStringSet("likes", likeSet);
                                            editor.apply();
                                            editor.commit();

                                            holder.upvote.setImageResource(R.drawable.upvote);

                                            removeLike(id);
                                        }


                                    } else {

                                        editor.putStringSet("Comment Dislikes", dislikeSet);
                                        editor.apply();
                                        editor.commit();
                                        updateCommentDislikes(id);

                                        if (likeSet != null && likeSet.contains(id)) {
                                            likeSet.remove(id);
                                            editor.putStringSet("Comment Likes", likeSet);
                                            editor.apply();
                                            editor.commit();

                                            holder.upvote.setImageResource(R.drawable.downvote);
                                            removeCommentLike(id);
                                        }
                                    }
                                }
                                break;

                        }

                    }
                });

                commentsRecyclerView.setAdapter(commentAdapter);
                commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                DividerItemDecoration decoration = new DividerItemDecoration(commentsRecyclerView.getContext(),
                        DividerItemDecoration.VERTICAL);
                commentsRecyclerView.addItemDecoration(decoration);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void getComments(final CallbackInterface<QuerySnapshot> callback) {
        //Query for the top comments on this post
        Query query = firestore.collection("posts")
                .document(docID).collection("comments")
                .orderBy("score", Query.Direction.DESCENDING);

        callback.onStart();
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess(task.getResult());
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });

        Log.d("Comments List", commentList.toString());
    }

    //Update the score for the appropriate post by value (1 or -1)
    private void updatePostScore(String postID, int value) {
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

    private void updateCommentScore(String commentID, int value) {
        firestore.collection("posts").document(docID)
                .collection("comments").document(commentID)
                .update("score", FieldValue.increment(value))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Comment score update successful");
                        } else {
                            Log.d("Herd", "Comment score update failed");
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void updateLikes(final String id) {
        firestore.collection("users")
                .document(userID)
                .update("likes", FieldValue.arrayUnion(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Updated user's liked posts");
                            updatePostScore(id, 1);
                        } else {
                            Log.d("Herd", "Unable to update user's liked posts");
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void removeLike(final String id) {
        firestore.collection("users")
                .document(userID)
                .update("likes", FieldValue.arrayRemove(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Updated user's liked posts");
                        } else {
                            Log.d("Herd", "Unable to update user's liked posts");
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void updateDislikes(final String id) {
        firestore.collection("users")
                .document(userID)
                .update("dislikes", FieldValue.arrayUnion(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Dislike added");
                            updatePostScore(id, -1);
                        } else {
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void removeDislike(final String id) {
        firestore.collection("users")
                .document(userID)
                .update("dislikes", FieldValue.arrayRemove(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Dislike removed");
                        } else {
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void updateCommentLikes(final String id) {
        firestore.collection("users")
                .document(userID)
                .update("Comment Likes", FieldValue.arrayUnion(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Updated user's liked posts");
                            updateCommentScore(id, 1);
                        } else {
                            Log.d("Herd", "Unable to update user's liked posts");
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void removeCommentLike(final String id) {
        firestore.collection("users")
                .document(userID)
                .update("Comment Likes", FieldValue.arrayRemove(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Comment like removed");
                        } else {
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void updateCommentDislikes(final String id) {
        firestore.collection("users")
                .document(userID)
                .update("Comment Disikes", FieldValue.arrayUnion(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Updated user's disliked posts");
                            updateCommentScore(id, -11);
                        } else {
                            Log.d("Herd", "Unable to update user's disliked posts");
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    private void removeCommentDislike(final String id) {
        firestore.collection("users")
                .document(userID)
                .update("Comment Dislikes", FieldValue.arrayRemove(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Comment dislike removed");
                        } else {
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        Log.d("PostComments", "In on start");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d("PostComments", "In on stop");
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), AddPostActivity.class);
        intent.putExtra("Doc ID", docID);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        //Refresh the adapter and remove the spinner when done
        swipeRefreshLayout.setRefreshing(false);
    }
}
