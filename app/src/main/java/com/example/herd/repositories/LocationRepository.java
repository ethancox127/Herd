package com.example.herd.repositories;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LocationRepository extends LiveData<Location> {

    private final String TAG = "LocationRepository";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Context context;
    private Location lastKnown;

    public LocationRepository(Context context, double latitude, double longitude) {

        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.lastKnown = new Location(locationManager.GPS_PROVIDER);
        lastKnown.setLatitude(latitude);
        lastKnown.setLongitude(longitude);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "New location: " + location.toString());

                if (lastKnown.getLatitude() != location.getLatitude() ||
                        lastKnown.getLongitude() != location.getLongitude()) {

                    setValue(location);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        Log.d(TAG, "onActive");

        requestUpdates();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        Log.d(TAG, "onInactive");

        stopUpdates();
    }

    private void requestUpdates() {

        // if device is running SDK < 23
        if (Build.VERSION.SDK_INT < 23) {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 3000, locationListener);

            }

        } else {

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 3000, locationListener);

            }
        }
    }

    private void stopUpdates() {
        locationManager.removeUpdates(locationListener);
    }
}
