package com.example.herd.ui.add_post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.herd.R;
import com.example.herd.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddPostActivity extends AppCompatActivity {

    private EditText postText;
    private Button sendButton;
    private FirebaseFirestore firestore;
    private String userID, postID;
    private double latitude, longitude;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        Intent intent = getIntent();
        if (intent.getStringExtra("Doc ID") != null) {
            postID = intent.getStringExtra("Doc ID");
        }

        postText = (EditText) findViewById(R.id.postText);
        sendButton = (Button) findViewById(R.id.sendPostButton);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        userID = sharedPreferences.getString("User ID", "");
        latitude = (double) sharedPreferences.getFloat("latitude", 0);
        longitude = (double) sharedPreferences.getFloat("longitude", 0);
    }

    public void sendPost(View view) {
        Log.d("Post text", postText.getText().toString());
        final Post post = new Post(postText.getText().toString(),0,0,
                Timestamp.now(), latitude, longitude, userID);
        if (postID == null) {
            firestore.collection("posts").add(post)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Log.d("Herd", "Post added successfully");
                                finish();
                            } else {
                                task.getException().printStackTrace();
                            }
                        }
                    });
        } else {
            firestore.collection("posts").document(postID)
                    .collection("comments").add(post)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Log.d("Herd", "Comment added successfully to post with id " + postID);
                                firestore.collection("posts").document(postID)
                                        .update("numComments",  FieldValue.increment(1))
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("Herd", "Num comments updated");
                                                    finish();
                                                } else {
                                                    task.getException().printStackTrace();
                                                }
                                            }
                                        });
                            } else {
                                task.getException().printStackTrace();
                            }
                        }
                    });
        }
    }
}

