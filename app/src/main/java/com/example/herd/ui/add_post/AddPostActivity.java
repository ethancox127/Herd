package com.example.herd.ui.add_post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.herd.R;
import com.example.herd.models.Post;
import com.example.herd.ui.posts_viewer.PostsViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddPostActivity extends AppCompatActivity {

    private EditText postText;
    private Button sendButton;
    private String postID;
    private AddPostViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        Intent intent = getIntent();
        if (intent.getStringExtra("Post ID") != null) {
            postID = intent.getStringExtra("Post ID");
        }

        postText = (EditText) findViewById(R.id.postText);
        sendButton = (Button) findViewById(R.id.sendPostButton);
        viewModel = ViewModelProviders.of(this).get(AddPostViewModel.class);
    }

    public void sendPost(View view) {
        Log.d("Post text", postText.getText().toString());
        viewModel.addPostToFirestore(postID, postText.getText().toString())
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(String postID) {
                        if (postID != "Failure") {
                            Log.d("AddPostActivity", "Post added successfully");
                            viewModel.updateUser(postID);
                            viewModel.addPostToDB(postID);
                            finish();
                        } else {
                            Log.d("AddPostActivity", "Unable to add post");
                            finish();
                        }
                    }
                });
    }
}

