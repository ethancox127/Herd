package com.example.herd.interfaces;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public interface CallbackInterface<T> {
    void onStart();
    void onSuccess(T object);
    void onFailure(Exception e);
}
