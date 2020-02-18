package com.example.herd.ui.login;

import android.app.Application;
import android.location.Location;

import com.example.herd.models.User;
import com.example.herd.repositories.AuthRepository;
import com.example.herd.repositories.LocalDBRepsoitory;
import com.example.herd.repositories.LocationRepository;
import com.example.herd.repositories.PreferencesRepository;
import com.example.herd.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends AndroidViewModel {

    //Firebase variables
    private FirebaseAuth mAuth;
    public FirebaseFirestore firestore;

    //Repositories
    private AuthRepository authRepository;
    private LocationRepository locationRepository;
    private UserRepository userRepository;
    private LocalDBRepsoitory dbRepsoitory;
    private PreferencesRepository preferencesRepository;

    //User variable to be added to firestore and local storage
    User user;
    String userID;

    // View model constructor
    public LoginViewModel(@NonNull Application application) {
        super(application);

        //Initialize the repositories
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        dbRepsoitory = new LocalDBRepsoitory(application);
        preferencesRepository = new PreferencesRepository(application);
        locationRepository = new LocationRepository(application, preferencesRepository.getLatitude(),
                preferencesRepository.getLongitude());

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null)
            userID = mAuth.getCurrentUser().getUid();
    }

    //Sign in user and return true or false based on a successful authentication
    public LiveData<Boolean> signIn() {
        return authRepository.signInAnonymously();
    }

    public void setUserName() {
        userID = mAuth.getCurrentUser().getUid();
    }

    //Check if user is signed in
    public boolean checkSignedIn() {
        return authRepository.checkSignedIn();
    }

    //Return the locationRepository to get the user's location
    public LiveData<Location> getLocation() {
        return locationRepository;
    }

    public Boolean checkLocation() {
        if (preferencesRepository.getLatitude() != 0 && preferencesRepository.getLongitude() != 0) {
            return true;
        } else {
            return false;
        }
    }

    //Adds user info to firestore and local storage
    public LiveData<Boolean> addUser(Location location) {

        double latitude = 0, longitude = 0;

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        user = new User(userID, latitude, longitude);
        MediatorLiveData liveData = new MediatorLiveData();

        liveData.addSource(addUserToPrefs(latitude, longitude), value -> liveData.setValue(value));
        liveData.addSource(addUserToLocalDB(), value -> liveData.setValue(value));
        liveData.addSource(addUserToFirestore(), value -> liveData.setValue(value));

        return liveData;
    }

    //Helper methods
    private LiveData<Boolean> addUserToFirestore() {
        return userRepository.addUser(user);
    }

    private LiveData<Boolean> addUserToLocalDB() {
        return dbRepsoitory.addUser(user);
    }

    private LiveData<Boolean> addUserToPrefs(double latitude, double longitude) {
        return preferencesRepository.addUser(userID, latitude, longitude);
    }


}

