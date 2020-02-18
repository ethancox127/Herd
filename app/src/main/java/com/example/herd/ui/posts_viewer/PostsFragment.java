package com.example.herd.ui.posts_viewer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.herd.R;
import com.example.herd.adapters.PostAdapter;
import com.example.herd.interfaces.OnItemClickListener;
import com.example.herd.models.Post;
import com.example.herd.ui.add_post.AddPostActivity;
import com.example.herd.ui.comments_viewer.CommentsActivity;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PostsFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private final String TAG = "PostsFragment";

    //UI element variables
    private ProgressBar progressBar;
    private FloatingActionButton addPostButton;
    private ExtendedFloatingActionButton newPostsButton;
    private SwipeRefreshLayout swipeContainer;
    private MaterialButton hotButton, newButton;

    //Recycler view and associated variables
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;

    private PostsViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the fragment within
        final View root = inflater.inflate(R.layout.fragment_posts, container, false);

        //Initialize the UI elements
        postsRecyclerView = root.findViewById(R.id.recyclerView);
        progressBar = root.findViewById(R.id.progressBar);
        addPostButton = root.findViewById(R.id.addPostButton);
        newPostsButton = root.findViewById(R.id.newPostsButton);
        swipeContainer = root.findViewById(R.id.swipeContainer);
        hotButton = root.findViewById(R.id.hotButton);
        newButton = root.findViewById(R.id.newButton);

        //Notify fragment there is a menu
        setHasOptionsMenu(true);

        //Add click and swipe listeners
        addPostButton.setOnClickListener(this);
        hotButton.setOnClickListener(this);
        newButton.setOnClickListener(this);
        swipeContainer.setOnRefreshListener(this);

        return root;
    }

    private void displayPosts(LiveData<ArrayList<Post>> posts) {

        posts.observe(this, new Observer<ArrayList<Post>>() {
            @Override
            public void onChanged(ArrayList<Post> posts) {

                if (postAdapter == null) {
                    postAdapter = new PostAdapter(viewModel, new OnItemClickListener() {
                        @Override
                        public void onRowClick(View view, int position) {

                            String postID = viewModel.getPostID().get(position);

                            switch (view.getId()) {

                                case R.id.postView:

                                    Intent intent = new Intent(getContext(), CommentsActivity.class);
                                    intent.putExtra("Post ID", postID);
                                    intent.putExtra("Post", viewModel.getPostList().get(position));
                                    startActivity(intent);
                                    break;

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

                    postsRecyclerView.setAdapter(postAdapter);
                    postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    postsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                            DividerItemDecoration.VERTICAL));

                } else {

                    postAdapter.updateItems();
                    postAdapter.updateLocation();
                    postAdapter.notifyDataSetChanged();

                }
            }
        });

    }

    private void listenForNewPosts() {
        viewModel.newPosts().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == true)
                    addButton();
            }
        });
    }

    private void listenForLocationChanges() {
        viewModel.getLocation().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                Log.d(TAG, "onChanged");

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Your location has changed, would you like to load posts near it?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                viewModel.setLatitude(location.getLatitude());
                                viewModel.setLongitude(location.getLongitude());
                                postAdapter.updateLocation();
                                reloadPosts();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            }
        });
    }

    //Add the new posts button to the layout and set it's click listener
    private void addButton() {
        newPostsButton.setVisibility(View.VISIBLE);
        newPostsButton.setOnClickListener(this);
    }

    //Set the filter menu for the fragment
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.filter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.filter) {

            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.filter_options);
            SeekBar distanceBar = dialog.findViewById(R.id.distanceSeekBar);
            SeekBar timeBar = dialog.findViewById(R.id.timeSeekBar);

            distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    viewModel.setDistance(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    viewModel.setTime(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            dialog.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, Integer.toString(viewModel.getDistance()));
                    Log.d(TAG, Integer.toString(viewModel.getTime()));

                    reloadPosts();

                    dialog.dismiss();
                }
            });

            dialog.show();
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
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

             //If the new posts button is clicked refresh the adapter and remove the button
            case R.id.newPostsButton:
                newPostsButton.setVisibility(View.GONE);
                displayPosts(viewModel.getNewPosts());

            case R.id.newButton:
                if (!newButton.isChecked()) {
                    newButton.setChecked(true);
                } else {
                    displayPosts(viewModel.getNewPosts());
                }
                break;

            case R.id.hotButton:
                if (!hotButton.isChecked())  {
                    hotButton.setChecked(true);
                } else {
                    displayPosts(viewModel.getTopPosts());
                }
                break;
        }
    }

    private void reloadPosts() {
        if (viewModel.getType() == "top") {
            displayPosts(viewModel.getTopPosts());
        } else {
            displayPosts(viewModel.getNewPosts());
        }
    }

    @Override
    public void onStart() {
        Log.d("Home fragment", "In on start");
        super.onStart();
        viewModel = ViewModelProviders.of(this).get(PostsViewModel.class);
        reloadPosts();
        listenForNewPosts();
        listenForLocationChanges();
    }

    @Override
    public void onStop() {
        Log.d("Home fragment","In on stop");
        super.onStop();
        newPostsButton.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        //Refresh the adapter and remove the spinner when done
        Log.d("Herd", "In onRefresh");
        reloadPosts();
        swipeContainer.setRefreshing(false);
    }
}
