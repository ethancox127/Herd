package com.example.herd.ui.comments_viewer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
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

import com.example.herd.R;
import com.example.herd.adapters.CommentsAdapter;
import com.example.herd.adapters.PostAdapter;
import com.example.herd.interfaces.CallbackInterface;
import com.example.herd.interfaces.OnItemClickListener;
import com.example.herd.models.Post;
import com.example.herd.ui.add_post.AddPostActivity;
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

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private final String TAG = "CommentsActivity";

    //UI element variables
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView commentsRecyclerView;
    private FloatingActionButton floatingActionButton;
    private PostView postView;

    //Other variables
    private CommentsAdapter postAdapter;
    private String docID;
    private Post post;
    private CommentsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comments);

        Log.d(TAG, "In onCreate");

        //Initialize UI variables
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.commentSwipeContainer);
        commentsRecyclerView = (RecyclerView) findViewById(R.id.commentRecyclerView);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.addCommentButton);
        postView = findViewById(R.id.post);

        //Set click and refresh listeners
        swipeRefreshLayout.setOnRefreshListener(this);
        floatingActionButton.setOnClickListener(this);

        //Get the post and post ID from the intent that started this activity
        Intent intent = getIntent();
        docID = intent.getStringExtra("Post ID");
        post = (Post) intent.getParcelableExtra("Post");

        Log.d(TAG, post.toString());
        Log.d(TAG, docID);

        viewModel = ViewModelProviders.of(this).get(CommentsViewModel.class);
        viewModel.setQuery(docID);
        displayComments(viewModel.getComments());
    }

    private void displayComments(LiveData<ArrayList<Post>> comments) {
        comments.observe(this, new Observer<ArrayList<Post>>() {
            @Override
            public void onChanged(ArrayList<Post> posts) {
                if (postAdapter == null) {
                    postAdapter = new CommentsAdapter(viewModel, new OnItemClickListener() {
                        @Override
                        public void onRowClick(View view, int position) {

                            String postID = viewModel.getPostIDList().get(position);

                            switch (view.getId()) {

                                case R.id.upvote:

                                    if (viewModel.checkForLike(postID)) {

                                        viewModel.updateScore(postID, -1);
                                        viewModel.removeLike(postID);

                                    } else {

                                        viewModel.updateScore(postID, 1);
                                        viewModel.addLike(postID);

                                        if (viewModel.checkForDislike(postID)) {
                                            viewModel.removeDislike(postID);
                                        }
                                    }
                                    break;

                                case R.id.downvote:

                                    if (viewModel.checkForDislike(postID)) {

                                        viewModel.updateScore(postID, 1);
                                        viewModel.removeDislike(postID);

                                    } else {

                                        viewModel.updateScore(postID, -1);
                                        viewModel.addDislike(postID);

                                        if (viewModel.checkForLike(postID)) {
                                            viewModel.removeLike(postID);
                                        }
                                    }
                                    break;

                            }
                        }
                    });
                } else {
                    postAdapter.updateItems();
                    postAdapter.notifyDataSetChanged();
                }

                commentsRecyclerView.setAdapter(postAdapter);
                commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                commentsRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                        DividerItemDecoration.VERTICAL));
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "In on start");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "In on stop");
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), AddPostActivity.class);
        intent.putExtra("Post ID", docID);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        //Refresh the adapter and remove the spinner when done
        displayComments(viewModel.getComments());
        swipeRefreshLayout.setRefreshing(false);
    }
}
