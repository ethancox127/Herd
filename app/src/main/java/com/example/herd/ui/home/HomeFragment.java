package com.example.herd.ui.home;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.herd.AddPostActivity;
import com.example.herd.CallbackInterface;
import com.example.herd.MainActivity;
import com.example.herd.OnItemClickListener;
import com.example.herd.Post;
import com.example.herd.PostAdapter;
import com.example.herd.PostCommentsActivity;
import com.example.herd.PostsViewerActivity;
import com.example.herd.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.DefaultSnapshotDiffCallback;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //UI element variables
    private ProgressBar progressBar;
    private FloatingActionButton addPostButton;
    private ExtendedFloatingActionButton newPostsButton;
    private SwipeRefreshLayout swipeContainer;
    private MaterialButton hotButton, newButton;
    private PopupWindow popupWindow;

    //Firebase variables
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    //Recycler view and associated variables
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private LinearLayoutManager postsLayoutManager;

    //Timestamp variable for differentiating between posts added after last load
    public static Timestamp curTime;

    //SharedPreferences instance and editor
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //Posts and related variables variables
    private ArrayList<String> postID = new ArrayList<>();
    private ArrayList<Post> postList = new ArrayList<>();
    private ArrayList<String> newPostID = new ArrayList<>();
    private ArrayList<Post> newPostList = new ArrayList<>();
    private Set<String> likes;
    private Set<String> dislikes;
    private int numNewPosts = 0;
    private String userID;

    //Private query flags
    private String type = "hot";
    private int distance = 10;
    private int time = 12;
    private Query newPosts;
    private ListenerRegistration registration;

    //Location variables
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;
    private float latitude, longitude, prevLat, prevLon;

    //Permission codes
    private final int ACCESS_FINE_LOCATION = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d("Herd", "Home fragment onCreateView");

        //Inflate the fragment within
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        //Initialize the UI elements
        postsRecyclerView = root.findViewById(R.id.recyclerView);
        progressBar = root.findViewById(R.id.progressBar);
        addPostButton = root.findViewById(R.id.addPostButton);
        newPostsButton = root.findViewById(R.id.newPostsButton);
        swipeContainer = root.findViewById(R.id.swipeContainer);
        hotButton = root.findViewById(R.id.hotButton);
        newButton = root.findViewById(R.id.newButton);

        //Initialize the SharedPreferences
        preferences = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = preferences.edit();

        //Get the userID and number of liked and disliked posts
        likes = preferences.getStringSet("likes", new HashSet<String>());
        dislikes = preferences.getStringSet("dislikes", new HashSet<String>());
        Log.d("Likes posts", likes.toString());
        Log.d("Disliked posts", dislikes.toString());
        userID = preferences.getString("User ID", "");
        prevLat = preferences.getFloat("latitude", 0);
        prevLon = preferences.getFloat("longitude", 0);

        //Get curTime to separate between posts already in firestore and those received after
        curTime = Timestamp.now();

        //Initialize the linear layout manager for the recycler view
        postsLayoutManager = new LinearLayoutManager(getContext());

        //Notify fragment there is a menu
        setHasOptionsMenu(true);

        //Add click and swipe listeners
        addPostButton.setOnClickListener(this);
        hotButton.setOnClickListener(this);
        newButton.setOnClickListener(this);
        swipeContainer.setOnRefreshListener(this);

        setUpLocationHandlers();
        requestFineLocation();

        //Load existing posts and listen for new posts
        displayPosts();

        return root;
    }

    //Initialized the locationListener and locationManager
    private void setUpLocationHandlers() {
        locationListener  = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    //Handler method for requesting the Access Fine Location permission
    private void requestFineLocation() {
        System.out.println("In requestFineLocation");
        //Check if user has granted location permissions
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //Request the fine location permission so I can access their location
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        } else {

            //Request location updates so that the user's current location will not be null
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 0, locationListener);

            //Get the user's location
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            //Get the user's latitude and longitude and create the firestore Geopoint
            latitude = (float) location.getLatitude();
            longitude = (float) location.getLongitude();

            if (prevLat != latitude || prevLon != longitude) {
                Log.d("longitude", Double.toString(location.getLongitude()));
                Log.d("latitude", Double.toString(location.getLatitude()));

                //Add the latitude and longitude to the Shared Preferences
                editor.putFloat("latitude", latitude);
                editor.putFloat("longitude", longitude);
                editor.commit();
            }
        }
    }

    //Handle permission request results
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION: {
                //Check that the fine location permission has been properly granted
                if (grantResults.length > 0 && getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    //Request location updates so that the user's current location will not be null
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1000, 0, locationListener);

                    //Get the user's location
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    //Get the user's latitude and longitude and create the firestore Geopoint
                    latitude = (float) location.getLatitude();
                    longitude = (float) location.getLongitude();

                    if (prevLat != latitude || prevLon != longitude) {
                        Log.d("longitude", Double.toString(location.getLongitude()));
                        Log.d("latitude", Double.toString(location.getLatitude()));

                        //Add the latitude and longitude to the Shared Preferences
                        editor.putFloat("latitude", latitude);
                        editor.putFloat("longitude", longitude);
                        editor.commit();
                    }
                } else {
                    //Location permission has not been granted, notify user
                    Toast.makeText(getContext(), "Location not granted, won't query for posts near you",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    //Helper function for displaying posts
    private void displayPosts() {

        //Get query given flags
        final Query query = updateQuery();

        //Create paged list configurations
        final PagedList.Config config = new PagedList.Config.Builder()
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
                        Log.d("Herd", "In parseSnapshot");
                        Log.d("Herd", snapshot.getData().toString());
                        int index = postID.indexOf(snapshot.getId());
                        Post post = snapshot.toObject(Post.class);
                        Log.d("Post", post.toString());
                        Log.d("Index", Integer.toString(index));
                        if (index != -1) {
                            postList.set(index, post);
                        } else {
                            int newIndex = newPostID.indexOf(snapshot.getId());
                            Log.d("New Index", Integer.toString(newIndex));
                            Log.d("New posts ID's", newPostID.toString());
                            if (newIndex != -1) {
                                Log.d("Parse snapshot", "in second if");
                                Post post1 = newPostList.remove(newIndex);
                                Log.d("New post", post1.toString());
                                postList.add(post1);
                                postID.add(newPostID.remove(newIndex));
                                return post1;
                            }
                            postList.add(post);
                            postID.add(snapshot.getId());
                        }
                        return post;
                    }
                })
                .build();

        //Create Post adapter
        postAdapter = new PostAdapter(options, postID, preferences, time, distance, new OnItemClickListener() {
            @Override
            //Called when a post is clicked
            //Get post and post ID at that position and start activity to display post and comments
            public void onRowClick(View view, int position) {

                Log.d("Post id", postID.toString());
                String id = postID.get(position);
                PostAdapter.PostViewHolder holder = postAdapter.getViewByPosition(position);
                Set<String> likeSet = new HashSet<String>(preferences.getStringSet("likes",
                        new HashSet<String>()));
                Set<String> dislikeSet = new HashSet<String>(preferences.getStringSet("dislikes",
                        new HashSet<String>()));
                int score =  Integer.valueOf(holder.score.getText().toString());
                Log.d("Herd", "position: " + position);
                Log.d("score", Integer.toString(score));

                switch (view.getId()) {
                    //Post selected
                    case R.id.postView:
                        Intent intent = new Intent(getContext(), PostCommentsActivity.class);
                        intent.putExtra("Post", postList.get(position));
                        intent.putExtra("Post ID", id);
                        startActivity(intent);
                        break;

                    //Upvote button selected
                    case R.id.upvote:

                        //If upvote button is already selected notify user
                        if (likeSet != null && likeSet.contains(id)) {

                            Toast.makeText(getContext(), "You already liked that post",
                                    Toast.LENGTH_SHORT).show();

                        } else {

                            likeSet.add(id);
                            editor.putStringSet("likes", new HashSet<String>(likeSet));
                            editor.apply();
                            editor.commit();
                            holder.upvote.setImageResource(R.drawable.upvote_selected);
                            score++;
                            holder.score.setText(Integer.toString(score));

                            if (dislikeSet != null && dislikeSet.contains(id)) {
                                dislikeSet.remove(id);
                                editor.putStringSet("dislikes", new HashSet<String>(dislikeSet));
                                editor.apply();
                                editor.commit();
                                holder.downvote.setImageResource(R.drawable.downvote);
                            }

                            //Update user's liked posts
                            updateLikes(id);
                        }
                        break;

                    //Downvote button selected
                    case R.id.downvote:

                        if (dislikeSet != null && dislikeSet.contains(id)) {
                            Toast.makeText(getContext(), "You already disliked that post",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            dislikeSet.add(id);
                            editor.putStringSet("dislikes", new HashSet<String>(dislikeSet));
                            editor.apply();
                            editor.commit();
                            holder.downvote.setImageResource(R.drawable.downvote_selected);
                            score--;
                            holder.score.setText(Integer.toString(score));

                            if (likeSet != null && likeSet.contains(id)) {
                                likeSet.remove(id);
                                editor.putStringSet("likes", new HashSet<String>(likeSet));
                                editor.apply();
                                editor.commit();
                                holder.upvote.setImageResource(R.drawable.upvote);
                            }

                            updateDislikes(id);
                        }
                        break;
                }
            }
        });

        //Set the adapter for the recycler view and add the decorations and layout manager
        postsRecyclerView.setAdapter(postAdapter);
        postsRecyclerView.setLayoutManager(postsLayoutManager);
        DividerItemDecoration decoration = new DividerItemDecoration(postsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        postsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), postsLayoutManager.getOrientation()){
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (view.getVisibility() == View.GONE) {
                    outRect.setEmpty();
                } else {
                    super.getItemOffsets(outRect, view, parent, state);
                }
            }
        });

        if (registration != null) {
            registration.remove();
        }
        if (type == "new") {
            listenForNewPosts();
        }
    }

    private void listenForNewPosts() {
        newPosts = firestore.collection("posts")
                .orderBy("time", Query.Direction.DESCENDING);

        registration = newPosts.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            e.printStackTrace();
                        }

                        if (queryDocumentSnapshots != null)  {
                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges())  {
                                Post post = dc.getDocument().toObject(Post.class);
                                switch (dc.getType()) {
                                    case ADDED:
                                        if (!postID.contains(dc.getDocument().getId())) {
                                            newPostList.add(post);
                                            newPostID.add(dc.getDocument().getId());
                                        }
                                        if (post.getTime().toDate().after(curTime.toDate())) {
                                            if (numNewPosts == 0 && post.getUserID() != userID) {
                                                addButton();
                                            }
                                            numNewPosts++;
                                        }
                                        break;
                                    case MODIFIED:
                                        Log.d("Herd", "post has been modified");
                                        int position = postID.indexOf(dc.getDocument().getId());
                                        int newPos = newPostID.indexOf(dc.getDocument().getId());
                                        if (position != -1) {
                                            PostAdapter.PostViewHolder holder = postAdapter.getViewByPosition(position);
                                            if (dc.getDocument().getLong("score") !=
                                                    Long.parseLong(holder.score.getText().toString())) {
                                                holder.score.setText(dc.getDocument().getLong("score").toString());
                                            }
                                            Log.d("Before split", holder.numComments.getText().toString());
                                            String curNumComments = holder.numComments.getText().toString()
                                                    .split(" ")[0];
                                            Log.d("After split", curNumComments);
                                            if (dc.getDocument().getLong("numComments") !=
                                                    Long.parseLong(curNumComments)) {
                                                holder.numComments.setText(dc.getDocument().getLong("numComments")
                                                        .toString() + " comments");
                                            }
                                        } else if (newPos != -1) {
                                            newPostList.set(newPos, dc.getDocument().toObject(Post.class));
                                        }
                                        break;
                                }
                            }
                        } else {
                            Log.d("Herd", "Could not get new data");
                        }
                    }
                });
    }

    //Helper function managing the query for the adapter
    private Query updateQuery() {
        if (type == "hot") {
            return firestore.collection("posts")
                    .orderBy("score", Query.Direction.DESCENDING);
        } else {
            return firestore.collection("posts")
                    .whereLessThan("time", curTime)
                    .orderBy("time", Query.Direction.DESCENDING);
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

    private void updateLikes(final String id) {
        firestore.collection("users")
                .document(userID)
                .update("likes", FieldValue.arrayUnion(id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Herd", "Updated user's liked posts");
                            if (dislikes != null && dislikes.contains(id)) {
                                removeDislike(id);
                            }
                            updateScore(id, 1);
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
                            if (likes != null && likes.contains(id)) {
                                removeLike(id);
                            }
                            updateScore(id, -1);
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

    //Add the new posts button to the layout and set it's click listener
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
                    distance = progress;
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
                    time = progress;
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
                    Log.d("Time", Integer.toString(time));
                    Log.d("Distance", Integer.toString(distance));
                    postList.clear();
                    postID.clear();
                    curTime = Timestamp.now();
                    displayPosts();
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
                postList.clear();
                postID.clear();
                newPostsButton.setVisibility(View.GONE);
                //Update the time to listen for new posts after
                curTime = Timestamp.now();
                postAdapter.refresh();
                registration.remove();
                listenForNewPosts();

            case R.id.newButton:
                if (!newButton.isChecked()) {
                    newButton.setChecked(true);
                } else {
                    postList.clear();
                    postID.clear();
                    type = "new";
                    curTime = Timestamp.now();
                    displayPosts();
                }
                break;

            case R.id.hotButton:
                if (!hotButton.isChecked())  {
                    hotButton.setChecked(true);
                } else {
                    postList.clear();
                    postID.clear();
                    type = "hot";
                    curTime = Timestamp.now();
                    displayPosts();
                }
                break;
        }
    }

    @Override
    public void onStart() {
        Log.d("Home fragment", "In on start");
        super.onStart();
        //Activity starting, listen for new posts
        postAdapter.startListening();
        if (numNewPosts > 0) {
            postList.clear();
            postID.clear();
            curTime = Timestamp.now();
            postAdapter.refresh();
        }
    }

    @Override
    public void onStop() {
        Log.d("Home fragment","In on stop");
        super.onStop();
        //Activity stopping, stop listening for new posts
        postAdapter.stopListening();
        newPostsButton.setVisibility(View.GONE);
        editor.commit();
    }

    @Override
    public void onRefresh() {
        //Refresh the adapter and remove the spinner when done
        Log.d("Herd", "In onRefresh");
        postList.clear();
        postID.clear();
        curTime = Timestamp.now();
        postAdapter.refresh();
        swipeContainer.setRefreshing(false);
    }
}
