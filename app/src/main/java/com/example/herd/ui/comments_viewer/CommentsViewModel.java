package com.example.herd.ui.comments_viewer;

import android.app.Application;
import android.util.Log;

import com.example.herd.models.DisplayObject;
import com.example.herd.models.Post;
import com.example.herd.repositories.LocalDBRepsoitory;
import com.example.herd.repositories.PreferencesRepository;
import com.example.herd.repositories.ReadRepository;
import com.example.herd.repositories.UserContract;
import com.example.herd.repositories.UserRepository;
import com.example.herd.repositories.WriteRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

public class CommentsViewModel extends AndroidViewModel {

    private final String TAG = "CommentsViewModel";

    //Firestore variables
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    //Top post query and new post query
    private Query commentsQuery;
    private CollectionReference commentsRef;

    //Repositories
    private ReadRepository readRepository;
    private WriteRepository writeRepository;
    private LocalDBRepsoitory dbRepsoitory;
    private PreferencesRepository preferencesRepository;
    private UserRepository userRepository;

    String postID;
    private ArrayList<String> likes, dislikes;
    private ArrayList<Post> posts = new ArrayList<>();
    private ArrayList<String> postIDList = new ArrayList<>();

    public CommentsViewModel(Application application) {
        super(application);

        readRepository = new ReadRepository(commentsQuery, "comments");
        writeRepository = new WriteRepository();
        dbRepsoitory = new LocalDBRepsoitory(application);
        preferencesRepository = new PreferencesRepository(application);
        userRepository = new UserRepository();

        likes = dbRepsoitory.getList(3);
        dislikes = dbRepsoitory.getList(4);
    }

    public void setQuery(String postID) {
        this.postID = postID;
        likes.add(0, postID);
        commentsQuery = firestore.collection("posts")
                .document(postID)
                .collection("comments");
        commentsRef = firestore.collection("posts")
                .document(postID)
                .collection("comments");
        readRepository.setQuery(commentsQuery);
        Log.d(TAG, commentsQuery.toString());
    }

    public LiveData<ArrayList<Post>> getComments() {
        return Transformations.map(readRepository, new Deserializer());
    }

    /* Methods for updating liked and disliked comments */

    //Adds a newly liked post to local db and firestore
    public void addLike(String id) {
        Log.d(TAG, "addLike");
        likes.add(id);
        if (id == postID) {
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_LIKED_POSTS, likes.toString());
            userRepository.addToList("likedPosts", userID);
        } else {
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_LIKED_COMMENTS, likes.toString());
            userRepository.addToList("likedComments", userID);
        }
    }

    //Removes liked post from local db and firestore
    public void removeLike (String id) {
        Log.d(TAG, "removeLike");
        likes.remove(id);
        if (id == postID) {
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_LIKED_POSTS, likes.toString());
            userRepository.removeFromList("likedPosts", userID);
        } else {
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_LIKED_COMMENTS, likes.toString());
            userRepository.removeFromList("likedComments", userID);
        }
    }

    //Adds newly disliked post to local db and firestore
    public void addDislike(String id) {
        Log.d(TAG, "addDislike");
        dislikes.add(id);
        if (id == postID) {
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_DISLIKED_POSTS, dislikes.toString());
            userRepository.addToList("dislikedPosts", userID);
        } else {
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_DISLIKED_COMMENTS, dislikes.toString());
            userRepository.addToList("dislikedComments", userID);
        }
    }

    //Removes disliked post from local db and firestore
    public void removeDislike(String id) {
        Log.d(TAG, "removeDislike");
        dislikes.remove(id);
        if (id == postID) {
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_DISLIKED_POSTS, dislikes.toString());
            userRepository.addToList("dislikedPosts", userID);
        } else {
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_DISLIKED_COMMENTS, dislikes.toString());
            userRepository.addToList("dislikedComments", userID);
        }
    }

    //Updates post score in firestore
    public void updateScore(String id, int score) {
        Log.d(TAG, "updateScore");
        DocumentReference ref;
        if (id == postID) {
            ref = firestore.collection("posts").document(id);
        } else {
            ref = commentsRef.document(id);
        }
        writeRepository.updateScore(ref, score);
    }

    public boolean checkForLike(String id) {
        return likes.contains(id);
    }

    public boolean checkForDislike(String id) {
        return dislikes.contains(id);
    }

    public void addToLists(Post post, String id) {
        posts.add(0, post);
        postIDList.add(0, id);
    }

    public ArrayList<Post> getPostList() { return posts; }
    public ArrayList<String> getPostIDList() { return postIDList; }
    public ArrayList<String> getUserLikes() { return likes; }
    public ArrayList<String> getUserDislikes() { return dislikes; }
    public double getLatitude() { return preferencesRepository.getLatitude(); }
    public double getLongitude() { return preferencesRepository.getLongitude(); }

    private class Deserializer implements androidx.arch.core.util.Function<DisplayObject, ArrayList<Post>> {

        @Override
        public ArrayList<Post> apply(DisplayObject object) {
            Log.d(TAG, "apply");
            QuerySnapshot snapshots = object.getSnapshots();

            for (DocumentChange change : snapshots.getDocumentChanges()) {

                QueryDocumentSnapshot doc = change.getDocument();

                switch (change.getType()) {

                    case ADDED:

                        if (!postIDList.contains(doc.getId())) {

                            postIDList.add(doc.getId());
                            posts.add(doc.toObject(Post.class));

                        }
                        break;

                    case MODIFIED:
                        Log.d(TAG, "Modified");
                        if (doc.getMetadata().hasPendingWrites()) {
                            int index = postID.indexOf(doc.getId());
                            if (index != -1)
                                posts.set(index, doc.toObject(Post.class));
                        }
                }
            }

            return posts;
        }
    }
}
