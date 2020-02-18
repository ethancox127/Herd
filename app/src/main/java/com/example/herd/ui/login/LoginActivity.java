package com.example.herd.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.herd.R;
import com.example.herd.models.Post;
import com.example.herd.ui.HomeActivity;
import com.example.herd.ui.posts_viewer.PostsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Activity level variables
    private final String TAG = "LoginActivity";
    private final int ACCESS_FINE_LOCATION = 1;
    private ImageButton signInButton;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize signInButton and set click listener
        signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(this);

        //Get ViewModel for Login Activity
        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
    }

    //Handler method for requesting the Access Fine Location permission
    private void requestFineLocation() {

        //Check if user has granted location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //Request the fine location permission so I can access their location
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);

        } else {
            getLocation();
        }
    }

    //Handle permission request results
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case ACCESS_FINE_LOCATION: {

                //Check that the fine location permission has been properly granted
                if (grantResults.length > 0 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    getLocation();

                } else {
                    signInButton.setClickable(true);
                }
                break;
            }
        }
    }

    //Helper method to get the users location
    private void getLocation() {

        //Call view model function to get location observable
        viewModel.getLocation().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {

                //If the location was retrieved successfully, add the user
                if (location != null) {

                    Log.d(TAG, "Location: " + location.toString());
                    addUser(location);

                } else {

                    signInButton.setClickable(true);
                    //Notify the user their location wasn't retrieved
                    Toast.makeText(getApplicationContext(), "Couldn't get location, can't load posts specific to your area",
                            Toast.LENGTH_LONG).show();
                    addUser(null);
                }
            }
        });

    }

    //Helper method for adding users to Firestore and local storage
    private void addUser(Location location) {

        //View Model observable for adding user to Firestore and local storage
        viewModel.addUser(location)
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {

                        //If the user was added successfully, go to Home Activity
                        if (aBoolean == true) {

                            Log.d(TAG, "User added successfully");
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intent);

                        } else {

                            signInButton.setClickable(true);
                            //Notify user that they couldn't be added to Firestore
                            Toast.makeText(getApplicationContext(), "Unable to add user, check your internet connection and try again",
                                    Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {

            case R.id.signInButton:

                //Will be true if authentication was successful but adding user to Firestore failed
                //or the user denied the location permission
                if (viewModel.checkSignedIn()) {

                    if (viewModel.checkLocation()) {
                        addUser(null);
                    } else {
                        requestFineLocation();
                    }

                } else {

                    //Call the view model function to authenticate users when signInButton selected
                    viewModel.signIn().observe(this, new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean aBoolean) {

                            //If user authentication is successful, request the fine location permission
                            if (aBoolean == true) {

                                Log.d(TAG, "Authentication successful");
                                viewModel.setUserName();
                                requestFineLocation();

                            } else {

                                //Notify the user that authentication failed
                                Toast.makeText(getApplicationContext(), "Unable to sign in, check your connection and try again",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                signInButton.setClickable(false);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        //If the user already signed in, go to Home Activity
        if (viewModel != null && viewModel.checkSignedIn()) {

            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.getLocation().removeObservers(this);
    }
}

