package com.example.herd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private PostAdapter commentAdapter;
    private String docID;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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

        //Get the post and post ID from the intent that started this activity
        Intent intent = getIntent();
        docID = intent.getStringExtra("Post ID");
        Post post = (Post) intent.getParcelableExtra("Post");

        //Add the post and postID to
        //commentList.add(post);
        //commentID.add(docID);

        Log.d("Herd", post.toString());
        Log.d("Herd", docID);

        getComments();
    }

    private void getComments() {
        //Query for the top comments on this post
        Query query = firestore.collection("posts")
                .document(docID).collection("comments")
                .orderBy("score", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (Post post : task.getResult().toObjects(Post.class)) {
                        Log.d("Comment", post.toString());
                    }
                } else {
                    task.getException().printStackTrace();
                }
            }
        });

        //Create paged list configurations
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

        //Create Firestore Paging Options and parse posts in postsList and PostID's for other use
        FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(query, config, new SnapshotParser<Post>() {
                    @NonNull
                    @Override
                    public Post parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Post post = snapshot.toObject(Post.class);
                        commentList.add(post);
                        commentID.add(snapshot.getId());
                        return post;
                    }
                })
                .build();

        commentAdapter = new PostAdapter(options, commentID);

        commentsRecyclerView.setAdapter(commentAdapter);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration decoration = new DividerItemDecoration(commentsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        commentsRecyclerView.addItemDecoration(decoration);
    }

    @Override
    protected void onStart() {
        Log.d("PostComments", "In on start");
        super.onStart();
        commentAdapter.startListening();
    }

    @Override
    protected void onStop() {
        Log.d("PostComments", "In on stop");
        super.onStop();
        commentAdapter.stopListening();
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
