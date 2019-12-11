package com.example.herd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Firebase variables
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    public FirebaseFirestore firestore;

    //Permission codes
    private final int ACCESS_FINE_LOCATION = 1;

    //Location related variables
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;
    private float latitude, longitude;
    private GeoPoint geoPoint;

    //Shared Preferences variables
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the SharedPreferences and the editor for them
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Initiallize the FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //Check if the user is already logged in
        if (mAuth.getCurrentUser() != null) {

            //If the user is already logged in move to the PostsViewerActivity
            Log.d("Herd",  "In onCreate reload if");
            Intent intent = new Intent(MainActivity.this,
                    PostsViewerActivity.class);
            startActivity(intent);

        } else {

            //If the user is not logged in yet initialize the firestore and location handlers
            firestore = FirebaseFirestore.getInstance();
            setUpLocationHandlers();

        }
    }

    //Initialized the locationListener and locationManager
    private void setUpLocationHandlers() {
        locationListener  = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) { }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    //On click method for "Follow the Herd" button
    public void getStarted(View view) {
        Log.d("Herd", "In get started method");
        Log.d("Herd", mAuth.toString());

        //If user navigated back to this activity while signed in, then clicked button again
        if (mAuth.getCurrentUser() != null) {
            Log.d("Herd",  "In getStarted reload if");
            Intent intent = new Intent(MainActivity.this,
                    PostsViewerActivity.class);
            startActivity(intent);
        } else {
            //The user is attempting to sign in for the first time
            signInUser(new CallbackInterface<AuthResult>() {

                @Override
                public void onStart() { }

                @Override
                public void onSuccess(AuthResult authResult) {
                    //The user was signed in successfully
                    //Get the current user and add the id to the Shared Preferences
                    firebaseUser = mAuth.getCurrentUser();
                    editor.putString("User ID", firebaseUser.getUid());
                    editor.commit();

                    //Notify the user that the authentication was successful
                    Log.d("Herd", "Authentication successful");
                    Toast.makeText(MainActivity.this, "Authentication Successful",
                            Toast.LENGTH_SHORT).show();

                    //Request the Access Fine Location permission to get the user's location
                    requestFineLocation();
                }

                @Override
                public void onFailure(Exception e) {
                    //Notify the user that authentication failed
                    Log.d("Herd", "Authentication Failed");
                    Toast.makeText(MainActivity.this, "Authentication Failed",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //Private handler method for signing in the user and handle CallbackInterface stages
    private void signInUser(final CallbackInterface callback) {
        callback.onStart();
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess(task.getResult());
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    //Handler method for requesting the Access Fine Location permission
    private void requestFineLocation() {
        //Check if user has granted location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                //Request the fine location permission so I can access their location
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
            }
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

                    //Request location updates so that the user's current location will not be null
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1000, 0, locationListener);

                    //Get the user's location
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    //Get the user's latitude and longitude and create the firestore Geopoint
                    latitude = (float) location.getLatitude();
                    longitude = (float) location.getLongitude();
                    geoPoint = new GeoPoint(latitude, longitude);
                    Log.d("longitude", Double.toString(location.getLongitude()));
                    Log.d("latitude", Double.toString(location.getLatitude()));

                    //Add the latitude and longitude to the Shared Preferences
                    editor.putFloat("latitude", latitude);
                    editor.putFloat("longitude", longitude);
                    editor.commit();

                    //Create the user data to be pushed to the firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("userId", firebaseUser.getUid());
                    user.put("location", geoPoint);

                    //Add the user to the to the database
                    addUser(user, new CallbackInterface<Void>() {
                        @Override
                        public void onStart() { }

                        @Override
                        public void onSuccess(Void v) {
                            //The user was added successfully, move to the PostsViewerActivity
                            Log.d("Herd", "User added to firestore successfully");
                            Intent intent = new Intent(MainActivity.this,
                                    PostsViewerActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.d("Herd", "Unable to add user to firestore " + e.getMessage());
                        }
                    });
                } else {
                    //Location permission has not been granted, notify user
                    Toast.makeText(this, "Location not granted, won't query for posts near you",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this,
                            PostsViewerActivity.class);
                    startActivity(intent);
                }
                return;
            }
        }
    }

    //Handler method for adding the user to the firestore
    private void addUser(Map<String, Object> user, final CallbackInterface<Void> callback) {
        callback.onStart();
        //Add the user to the user's collection in the firestore
        firestore.collection("users").document(firebaseUser.getUid())
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}