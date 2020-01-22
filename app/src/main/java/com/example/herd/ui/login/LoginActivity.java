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

    //Permission codes
    private final int ACCESS_FINE_LOCATION = 1;

    private ImageButton signInButton;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("Herd", "Main onCreate");

        signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(this);

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

                    viewModel.getLocation().observe(this, new Observer<Location>() {
                        @Override
                        public void onChanged(Location location) {
                            if (location != null) {
                                addUser(location);
                            } else {
                                Toast.makeText(getApplicationContext(), "Couldn't get location",  Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Herd", "Main onStart");
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, PostsFragment.class);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Herd", "Main onResume");
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.signInButton:
                viewModel.signIn().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == true) {
                            requestFineLocation();
                        } else {
                            Toast.makeText(getApplicationContext(), "Unable to sign in", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
        }
    }

    private void addUser(Location location) {
        viewModel.addUser(location.getLatitude(), location.getLongitude())
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean == true) {
                            Intent intent = new Intent(getApplicationContext(), PostsFragment.class);
                        } else {
                            Toast.makeText(getApplicationContext(), "Unable to add user", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}

