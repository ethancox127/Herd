package com.example.herd.ui.login;

import android.app.Application;
import android.location.Location;

import com.example.herd.models.User;
import com.example.herd.repositories.AuthRepository;
import com.example.herd.repositories.LocationRepository;
import com.example.herd.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends AndroidViewModel {

    //Firebase variables
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    //Repositories
    private AuthRepository authRepository;
    private LocationRepository locationRepository;
    private UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);

        //Initialize the repositories
        authRepository = new AuthRepository();
        locationRepository = new LocationRepository(application);
        userRepository = new UserRepository();
    }

    //Sign in user and return true or false based on a successful authentication
    public LiveData<Boolean> signIn() {
        return authRepository.signInAnonymously();
    }


    //Check if user is signed in
    public LiveData<Boolean> checkSignedIn() {
        return authRepository.checkSignedIn();
    }

    //Return the locationRepository so the user can get location updates
    public LiveData<Location> getLocation() {
        return locationRepository;
    }

    //Add the user to the firestore and return whether it was successful
    public LiveData<Boolean> addUser(double latitude, double longitude) {
        User user = new User(mAuth.getCurrentUser().getUid(), latitude, longitude);
        return userRepository.addUser(user);
    }
}

