package com.example.herd.repositories;

import android.util.Log;

import com.example.herd.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class WriteRepository {

    public LiveData<String> addPost(CollectionReference ref, Post post) {
        MutableLiveData<String> result = new MutableLiveData<>();
        ref.add(post).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Log.d("Herd", "Post added successfully");
                    result.setValue(task.getResult().getId());
                } else {
                    task.getException().printStackTrace();
                    result.setValue("Failure");
                }
            }
        });
        return result;
    }

    public void updateScore(DocumentReference ref, int score) {
        ref.update("score", FieldValue.increment(score))
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

}
