package com.example.herd.repositories;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AuthRepository {

    private final String TAG = "AuthRepository";
    private FirebaseAuth mAuth;

    public AuthRepository() {

        mAuth = FirebaseAuth.getInstance();

    }

    public LiveData<Boolean> signInAnonymously() {
        final MutableLiveData<Boolean> result = new MutableLiveData();
        mAuth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Anonymous login successful");
                            result.setValue(true);
                        } else {
                            Log.e(TAG, "Unable to add anonymous user");
                            task.getException().printStackTrace();
                            result.setValue(false);
                        }
                    }
                });
        return result;
    }

    public boolean checkSignedIn() {

        if (mAuth != null && mAuth.getCurrentUser() != null) {
            return true;
        } else {
            return false;
        }
    }

}
