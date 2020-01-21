package com.example.herd.repositories;

import android.util.Log;

import com.example.herd.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UserRepository {

    private final String TAG = "UserRepository";

    public LiveData<Boolean> addUser(User user)  {
        final MutableLiveData<Boolean> result = new MutableLiveData<>();
        FirebaseFirestore.getInstance().collection("users")
                .add(user)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Added user successfully with id " + task.getResult().getId());
                            result.setValue(true);
                        } else {
                            Log.w(TAG, "Unable to add user");
                            task.getException().printStackTrace();
                            result.setValue(false);
                        }
                    }
                });
        return result;
    }

}
