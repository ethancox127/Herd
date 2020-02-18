package com.example.herd.ui.add_post;

import android.app.Application;

import com.example.herd.models.Post;
import com.example.herd.repositories.LocalDBRepsoitory;
import com.example.herd.repositories.PreferencesRepository;
import com.example.herd.repositories.UserContract;
import com.example.herd.repositories.UserRepository;
import com.example.herd.repositories.WriteRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class AddPostViewModel extends AndroidViewModel {

    private String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private double latitude, longitude;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference ref;
    private ArrayList<String> posts, comments;
    private String type = "";

    private LocalDBRepsoitory dbRepsoitory;
    private PreferencesRepository preferencesRepository;
    private WriteRepository writeRepository;
    private UserRepository userRepository;

    public AddPostViewModel(@NonNull Application application) {
        super(application);
        dbRepsoitory = new LocalDBRepsoitory(application);
        preferencesRepository = new PreferencesRepository(application);
        writeRepository = new WriteRepository();
        userRepository = new UserRepository();

        latitude = preferencesRepository.getLatitude();
        longitude = preferencesRepository.getLongitude();
        posts = dbRepsoitory.getList(1);
        comments = dbRepsoitory.getList(2);
    }

    public LiveData<String> addPostToFirestore(String postID, String postText) {

        if (postID != null) {
            ref = firestore.collection("posts").document(postID)
                    .collection("comments");
            type = "comment";
        } else {
            ref = firestore.collection("posts");
            type = "post";
        }

        final Post post = new Post(postText,0,0, Timestamp.now(), latitude,
                longitude, userID);

        return writeRepository.addPost(ref, post);
    }

    public void updateUser(String postID) {
        if (type == "post") {
            userRepository.addToList(UserContract.UserEntry.COLUMN_NAME_POSTS, postID);
        } else {
            userRepository.addToList(UserContract.UserEntry.COLUMN_NAME_COMMENTS, postID);
        }
    }

    public void addPostToDB(String postID) {
        if (type == "post") {
            posts.add(postID);
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_POSTS, posts.toString());
        } else {
            comments.add(postID);
            dbRepsoitory.updateColumn(UserContract.UserEntry.COLUMN_NAME_COMMENTS, comments.toString());
        }
    }

}
