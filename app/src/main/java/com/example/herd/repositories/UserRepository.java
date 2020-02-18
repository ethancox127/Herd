package com.example.herd.repositories;

import android.util.Log;

import com.example.herd.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UserRepository {

    private final String TAG = "UserRepository";
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public LiveData<Boolean> addUser(User user) {
        final MutableLiveData<Boolean> result = new MutableLiveData<>();
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUserID())
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Added user successfully with id " + user.getUserID());
                            result.setValue(true);
                        } else {
                            Log.d(TAG, "Unable to add user to firestore");
                            task.getException().printStackTrace();
                            result.setValue(false);
                        }
                    }
                });
        return result;
    }

    public void addToList(final String field, final String postID) {
        firestore.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .update(field, FieldValue.arrayUnion(postID))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Added " + postID + " to " + field);
                        } else {
                            Log.d(TAG, "Unable to add " + postID + " add " + field);
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

    public void removeFromList(final String field, final String postID) {
        firestore.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .update(field, FieldValue.arrayRemove(postID))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())  {
                            Log.d(TAG, "Removed " + postID + " from " + field);
                        } else {
                            Log.d(TAG, "Unable to remove " + postID + " from " + field);
                            task.getException().printStackTrace();
                        }
                    }
                });
    }

}
