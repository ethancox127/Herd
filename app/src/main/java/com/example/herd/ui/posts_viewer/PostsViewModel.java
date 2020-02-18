package com.example.herd.ui.posts_viewer;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import com.example.herd.models.DisplayObject;
import com.example.herd.models.Post;
import com.example.herd.repositories.LocalDBRepsoitory;
import com.example.herd.repositories.LocationRepository;
import com.example.herd.repositories.PreferencesRepository;
import com.example.herd.repositories.ReadRepository;
import com.example.herd.repositories.UserContract;
import com.example.herd.repositories.UserRepository;
import com.example.herd.repositories.WriteRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class PostsViewModel extends AndroidViewModel {

    private final String TAG = "PostsViewModel";

    //Firestore variables
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    //Top post query and new post query
    private Query topQuery = firestore.collection("posts")
            .orderBy("score", Query.Direction.DESCENDING).limit(10);

    private Query newQuery = firestore.collection("posts")
            .orderBy("time", Query.Direction.DESCENDING).limit(10);

    //Repository instance variables
    private ReadRepository topPostsRepository;
    private ReadRepository newPostsRepository;
    private WriteRepository writeRepository;
    private UserRepository userRepository;
    private LocalDBRepsoitory dbRepsoitory;
    private LocationRepository locationRepository;
    private PreferencesRepository preferencesRepository;

    //Other view model variables
    private ArrayList<String> likes, dislikes;
    private ArrayList<String> topPostID = new ArrayList<>(), newPostID = new ArrayList<>();
    private ArrayList<Post> topPostList = new ArrayList<>(), newPostList = new ArrayList<>();
    private Timestamp curTime = Timestamp.now();
    private String type = "top";
    private int distance = 30, time = 12;
    private MutableLiveData<Boolean> newPosts = new MutableLiveData();

    public PostsViewModel(@NonNull Application application) {
        super(application);

        //Initialize repository variables
        topPostsRepository = new ReadRepository(topQuery, "top");
        newPostsRepository = new ReadRepository(newQuery, "new");
        writeRepository = new WriteRepository();
        userRepository = new UserRepository();
        dbRepsoitory = new LocalDBRepsoitory(application);
        preferencesRepository = new PreferencesRepository(application);
        locationRepository = new LocationRepository(application, getLatitude(), getLongitude());

        //Get user's liked and disliked posts when view model and associated fragment are created
        likes = dbRepsoitory.getList(3);
        dislikes = dbRepsoitory.getList(4);
    }

    /* Methods to return  posts based on query */

    //Return top posts to fragment to be displayed in adapter
    public LiveData<ArrayList<Post>> getTopPosts() {
        Log.d(TAG, "getTopPosts");

        type = "top";
        curTime = Timestamp.now();
        return Transformations.map(topPostsRepository, new Deserializer());
    }

    //Return new posts to fragment to be displayed in adapter
    public LiveData<ArrayList<Post>> getNewPosts() {
        Log.d(TAG, "getNewPosts");

        type = "new";
        curTime = Timestamp.now();
        return Transformations.map(newPostsRepository, new Deserializer());
    }

    /* Methods for updating liked and disliked posts */

    //Adds a newly liked post to local db and firestore
    public void addLike(String id) {
        Log.d(TAG, "addLike");
        likes.add(id);
        dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_LIKED_POSTS, likes.toString());
        userRepository.addToList("likedPosts", id);
    }

    //Removes liked post from local db and firestore
    public void removeLike (String id) {
        Log.d(TAG, "removeLike");
        likes.remove(id);
        dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_LIKED_POSTS, likes.toString());
        userRepository.removeFromList("likedPosts", id);
    }

    //Adds newly disliked post to local db and firestore
    public void addDislike(String id) {
        Log.d(TAG, "addDislike");
        dislikes.add(id);
        dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_DISLIKED_POSTS, dislikes.toString());
        userRepository.addToList("dislikedPosts", id);
    }

    //Removes disliked post from local db and firestore
    public void removeDislike(String id) {
        Log.d(TAG, "removeDislike");
        dislikes.remove(id);
        dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_DISLIKED_POSTS, dislikes.toString());
        userRepository.removeFromList("dislikedPosts", id);
    }

    /* Methods for checking if a post has been liked or disliked */

    //Return if post at position has been liked by user to fragment
    public Boolean checkForLike(String id) {
        Log.d(TAG, "checkForLike");

        if (likes.contains(id)) {
            return true;
        } else {
            return false;
        }

    }

    //Return if post at position has been disliked by user to fragment
    public Boolean checkForDislike(String id) {
        Log.d(TAG, "checkForDislike");

        if (dislikes.contains(id)) {
            return true;
        } else {
            return false;
        }

    }

    //Updates post score in firestore
    public void updateScore(String id, int score) {
        Log.d(TAG, "updateScore");
        DocumentReference ref = firestore.collection("posts").document(id);
        writeRepository.updateScore(ref, score);
    }

    //Observable for if there are new posts to be displayed
    public LiveData<Boolean> newPosts() {
        newPosts.setValue(false);
        return newPosts;
    }

    //Observable for user's location
    public LiveData<Location> getLocation() {
        return locationRepository;
    }

    //Setter methods for variables
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public void setTime(int time) {
        this.time = time;
    }
    public void setLatitude(double latitude) {
        preferencesRepository.updateLatitude(latitude);
    }
    public void setLongitude(double longitude) {
        preferencesRepository.updateLongitude(longitude);
    }

    //Getter methods for variables
    public ArrayList<String> getPostID() {
        if (type == "top")
            return topPostID;
        else
            return newPostID;
    }

    public ArrayList<Post> getPostList() {
        if (type == "top")
            return topPostList;
        else
            return newPostList;
    }

    public ArrayList<String> getUserLikes() { return likes; }
    public ArrayList<String> getUserDislikes() { return dislikes; }
    public String getType() { return type; }
    public int getDistance() { return distance; }
    public int getTime() { return time; }
    public double getLatitude() { return preferencesRepository.getLatitude(); }
    public double getLongitude() { return preferencesRepository.getLongitude(); }

    //Maps query snapshots of posts to an ArrayList
    private class Deserializer implements androidx.arch.core.util.Function<DisplayObject, ArrayList<Post>> {

        @Override
        public ArrayList<Post> apply(DisplayObject object) {

            QuerySnapshot snapshots = object.getSnapshots();
            ArrayList<Post> postList;
            ArrayList<String> postID;
            if (object.getType() == "top") {
                postList = topPostList;
                postID = topPostID;
            } else {
                postList = newPostList;
                postID = newPostID;
            }

            Log.d(TAG, "apply");
            Log.d(TAG, Integer.toString(snapshots.size()));

            for (DocumentChange change : snapshots.getDocumentChanges()) {

                QueryDocumentSnapshot doc = change.getDocument();
                Log.d(TAG, Boolean.toString(doc.getMetadata().hasPendingWrites()));
                Log.d(TAG, doc.toObject(Post.class).toString());

                switch (change.getType()) {

                    case ADDED:
                        Log.d(TAG, "Added");

                        if (!postID.contains(doc.getId())) {

                            if (doc.getTimestamp("time").compareTo(curTime) < 0) {

                                if (newPosts.getValue()) {

                                    postID.add(0, doc.getId());
                                    postList.add(0, doc.toObject(Post.class));
                                    newPosts.setValue(false);

                                } else {

                                    postID.add(doc.getId());
                                    postList.add(doc.toObject(Post.class));

                                }

                            } else {

                                Log.d(TAG, "In else");

                                if (object.getType() == "new") {

                                    if (doc.get("userID").equals(userID) || type == "hot") {

                                        postID.add(0, doc.getId());
                                        postList.add(0, doc.toObject(Post.class));

                                    } else {
                                        newPosts.setValue(true);
                                    }

                                } else {

                                    if (type == "hot") {

                                        postID.add(doc.getId());
                                        postList.add(doc.toObject(Post.class));

                                    }

                                }
                            }
                        }
                        break;

                    case MODIFIED:
                        Log.d(TAG, "Modified");
                        if (doc.getMetadata().hasPendingWrites()) {
                            int index = postID.indexOf(doc.getId());
                            if (index != -1)
                                postList.set(index, doc.toObject(Post.class));
                        }
                        break;

                }
            }

            if (object.getType() == "top") {
                topPostID = postID;
                topPostList = postList;
                return topPostList;
            } else {
                newPostID = postID;
                newPostList = postList;
                return newPostList;
            }

        }

    }

}
