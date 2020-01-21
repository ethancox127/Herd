package com.example.herd.repositories;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.lifecycle.LiveData;

public class LocationRepository extends LiveData<Location> {

    private final String TAG = "LocationRepository";
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    public LocationRepository(Context context) {
        locationClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    setValue(location);
                }
            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        Log.d(TAG, "onActive");
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        setValue(location);
                    }
                });
        startLocationUpdates();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        Log.d(TAG, "onInactive");
        locationClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
}
