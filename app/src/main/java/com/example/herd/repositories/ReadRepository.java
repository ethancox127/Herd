package com.example.herd.repositories;

import android.os.Handler;
import android.util.Log;

import com.example.herd.models.DisplayObject;
import com.example.herd.models.Post;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

public class ReadRepository extends LiveData<DisplayObject> {

    private String TAG;

    private Query query;
    private final MyValueEventListener listener = new MyValueEventListener();
    private ListenerRegistration registration;

    private boolean listenerRemovePending = false;
    private final Handler handler = new Handler();
    private final Runnable removeListener = new Runnable() {
        @Override
        public void run() {
            registration.remove();
            listenerRemovePending = false;
            isActive = false;
        }
    };

    private String type;
    private boolean isActive = false;

    public ReadRepository(Query query, String type) {
        Log.d(TAG, "ReadRepository");
        this.query = query;
        this.type = type;
        if (type == "new") {
            TAG = "NewReadRepository";
        } else {
            TAG = "TopReadRepository";
        }
    }

    public void setQuery(Query query)  {
        this.query = query;
    }

    @Override
    protected void onActive() {
        Log.d(TAG, "onActive");
        Log.d(TAG, query.toString());
        if (listenerRemovePending) {
            handler.removeCallbacks(removeListener);
        } else {
            registration = query.addSnapshotListener(listener);
        }
        listenerRemovePending = false;
        isActive = true;
    }

    @Override
    protected void onInactive() {
        Log.d(TAG, "onInactive");
        handler.postDelayed(removeListener, 2000);
        listenerRemovePending = true;
    }

    public void startListening() {
        Log.d(TAG, "startListening");
        registration = query.addSnapshotListener(listener);
    }

    public void removeListener() {
        Log.d(TAG, "removeListener");
        registration.remove();
        isActive = false;
    }

    public boolean checkIsActive() {
        return isActive;
    }

    private class MyValueEventListener implements EventListener<QuerySnapshot> {

        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                            @Nullable FirebaseFirestoreException e) {

            Log.d(TAG, "onEvent");

            if (e != null) {
                Log.e(TAG, "Can't listen to query " + query);
                e.printStackTrace();
            }

            setValue(new DisplayObject(type, queryDocumentSnapshots));

        }

    }

}
