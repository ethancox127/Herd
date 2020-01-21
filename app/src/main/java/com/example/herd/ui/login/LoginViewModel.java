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
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends AndroidViewModel {

    //Firebase variables
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser;
    public FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    //Repositories
    private AuthRepository authRepository;
    private LocationRepository locationRepository;
    private UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
        locationRepository = new LocationRepository(application);
        userRepository = new UserRepository();
    }

    public LiveData<Boolean> signIn() {
        authRepository.signInAnonymously();
        return authRepository;
    }

    public LiveData<Location> getLocation() {
        return locationRepository;
    }

    public LiveData<Boolean> addUser(double latitude, double longitude) {
        firebaseUser = mAuth.getCurrentUser();
        User user = new User(firebaseUser.getUid(), latitude, longitude);
        return userRepository.addUser(user);
    }
}

