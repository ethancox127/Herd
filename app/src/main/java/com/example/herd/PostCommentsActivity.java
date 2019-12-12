package com.example.herd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.local.QueryData;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class PostCommentsActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView commentsRecyclerView;
    private TextView textView;
    private FloatingActionButton floatingActionButton;
    private ProgressBar progressBar;
    private ArrayList<Post> commentList;
    private ArrayList<String> commentID;
    private PostAdapter commentAdapter;
    private String docID;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comments);

        Log.d("PostCommentsActivity", "In onCreate");

        commentsRecyclerView = (RecyclerView) findViewById(R.id.commentsRecyclerView);
        textView = (TextView) findViewById(R.id.noCommentsText);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.addCommentButton);
        floatingActionButton.setOnClickListener(this);

        commentList = new ArrayList<>();
        commentID = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        docID = intent.getStringExtra("Post ID");
        Post post = (Post) intent.getParcelableExtra("Post");
        commentList.add(post);
        commentID.add(docID);

        Log.d("Herd", post.toString());
        Log.d("Herd", docID);

        commentsRecyclerView.setAdapter(commentAdapter);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        /*commentAdapter = new PostAdapter(commentList, commentID, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (view.getId()) {
                    case R.id.upvote:
                        Log.d("Herd", "Upvote button selected");
                        firestore.collection("posts")
                                .document(docID)
                                .collection("comments")
                                .document(commentID.get(position))
                                .update("score", FieldValue.increment(1))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("Herd", "Post score updated.");
                                        } else {
                                            task.getException().printStackTrace();
                                        }
                                    }
                                });
                        break;
                    case R.id.downvote:
                        Log.d("Herd", "Downvote button selected");
                        firestore.collection("posts")
                                .document(docID)
                                .collection("comments")
                                .document(commentID.get(position))
                                .update("score", FieldValue.increment(-1))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("Herd", "Post score updated.");
                                        } else {
                                            task.getException().printStackTrace();
                                        }
                                    }
                                });
                        break;
                }
            }
        });
        commentsRecyclerView.setAdapter(commentAdapter);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (post.getNumComments() == 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            getComments(new CallbackInterface<QuerySnapshot>() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess(QuerySnapshot query) {
                    for (QueryDocumentSnapshot data : query) {
                        commentID.add(data.getId());
                        String post = data.getString("post");
                        int score = (int) ((long) data.get("score"));
                        int numComments = (int) ((long) data.get("numComments"));
                        Timestamp timestamp = data.getTimestamp("time");
                        commentList.add(new Post(post, score, numComments, timestamp));
                    }

                    commentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            });
        }*/
    }

    private void getComments(final CallbackInterface<QuerySnapshot> callback) {
        callback.onStart();
        firestore.collection("posts").document(docID).collection("comments")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), AddPostActivity.class);
        intent.putExtra("Doc ID", docID);
        startActivity(intent);
    }
}
