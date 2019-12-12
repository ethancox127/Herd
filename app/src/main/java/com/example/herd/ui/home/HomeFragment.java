package com.example.herd.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.herd.AddPostActivity;
import com.example.herd.CallbackInterface;
import com.example.herd.OnItemClickListener;
import com.example.herd.Post;
import com.example.herd.PostAdapter;
import com.example.herd.PostCommentsActivity;
import com.example.herd.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //UI element variables
    private ProgressBar progressBar;
    private FloatingActionButton addPostButton;
    private ExtendedFloatingActionButton newPostsButton;
    private SwipeRefreshLayout swipeContainer;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    //Firebase variables
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    //Recycler view and associated variables
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private LinearLayoutManager postsLayoutManager;
    private ArrayList<String> postID = new ArrayList<>();
    private ArrayList<Post> postList = new ArrayList<>();

    //Timestamp variable for differentiating between posts added after last load
    public static Timestamp curTime;

    private SharedPreferences preferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d("Home fragment", "In on create view");

        //Inflate the fragment within
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        //Initialize the UI elements
        postsRecyclerView = root.findViewById(R.id.recyclerView);
        progressBar = root.findViewById(R.id.progressBar);
        addPostButton = root.findViewById(R.id.addPostButton);
        newPostsButton = root.findViewById(R.id.newPostsButton);
        swipeContainer = root.findViewById(R.id.swipeContainer);

        preferences = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        //Initialize Timestamp when user enters activity
        curTime = Timestamp.now();

        //Initialize the linear layout manager for the recycler view
        postsLayoutManager = new LinearLayoutManager(getContext());

        //Set the filter menu on the toolbar
        setHasOptionsMenu(true);

        //Create an onClickListener for the add posts floating button
        addPostButton.setOnClickListener(this);

        swipeContainer.setOnRefreshListener(this);

        //Load and listen for new posts
        listenForNewPosts();

        return root;
    }

    private void listenForNewPosts() {

        Query query = firestore.collection("posts")
                .orderBy("score", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(query, config, new SnapshotParser<Post>() {
                    @NonNull
                    @Override
                    public Post parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Post post = snapshot.toObject(Post.class);
                        postList.add(post);
                        postID.add(snapshot.getId());
                        return post;
                    }
                })
                .build();

        postAdapter = new PostAdapter(options, postID, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d("Herd", view.toString());
                switch (view.getId()) {
                    case R.id.postView:
                        Intent intent = new Intent(getContext(), PostCommentsActivity.class);
                        intent.putExtra("Post", postList.get(position));
                        intent.putExtra("Post ID", postID.get(position));
                        startActivity(intent);
                        break;
                    case R.id.upvote:
                        updateScore(postID.get(position), 1);
                        break;
                    case R.id.downvote:
                        updateScore(postID.get(position), -1);
                        break;
                }
            }
        });

        postsRecyclerView.setAdapter(postAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(postsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        postsRecyclerView.setLayoutManager(postsLayoutManager);
        postsRecyclerView.addItemDecoration(decoration);

        firestore.collection("posts")
                .orderBy("time", Query.Direction.DESCENDING)
                .whereGreaterThan("time", curTime)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            e.printStackTrace();
                        }

                        if (snapshots != null && snapshots.size() > 0) {
                            DocumentSnapshot doc = snapshots.getDocuments().get(0);
                            String userID = preferences.getString("User ID", "");
                            if (doc.getData().get("userID") != userID) {
                                addButton();
                            }
                        }
                    }
                });
    }

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

    private void addButton() {
        newPostsButton.setVisibility(View.VISIBLE);
        newPostsButton.setOnClickListener(this);
    }

    //Set the filter menu for the fragment
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d("Herd", "In onCreateOptionsMenu");
        inflater.inflate(R.menu.filter_menu, menu);
    }

    //On Click implementation for fragment buttons
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            //If the add post button is clicked go to activity where user can add a post
            case R.id.addPostButton:
                Intent intent = new Intent(view.getContext(), AddPostActivity.class);
                startActivity(intent);
                //startActivityForResult(intent, 1);
                break;
            case R.id.newPostsButton:
                postAdapter.refresh();
                newPostsButton.setVisibility(View.GONE);
                curTime = Timestamp.now();
        }
    }

    @Override
    public void onStart() {
        Log.d("Home fragment", "In on start");
        super.onStart();
        postAdapter.startListening();
        postAdapter.refresh();
    }

    @Override
    public void onStop() {
        Log.d("Home fragment","In on stop");
        super.onStop();
        postAdapter.stopListening();
        newPostsButton.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        postAdapter.refresh();
        swipeContainer.setRefreshing(false);
    }
}
