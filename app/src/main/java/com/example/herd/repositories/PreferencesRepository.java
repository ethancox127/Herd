package com.example.herd.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PreferencesRepository {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public PreferencesRepository(Context context) {
        preferences = context.getSharedPreferences("My preferences", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public LiveData<Boolean> addUser(String userID, double latitude, double longitude) {
        editor.putString("User ID", userID);
        editor.putLong("latitude", Double.doubleToLongBits(latitude));
        editor.putLong("longitude", Double.doubleToLongBits(longitude));

        MutableLiveData<Boolean> result = new MutableLiveData<>();
        result.setValue(editor.commit());
        return result;
    }

    public boolean updateLatitude(double latitude) {
        editor.putLong("latitude", Double.doubleToLongBits(latitude));
        return editor.commit();
    }

    public boolean updateLongitude(double longitude) {

        editor.putLong("longitude", Double.doubleToLongBits(longitude));
        return editor.commit();
    }

    public String getUserID() {
        return preferences.getString("User ID", "");
    }

    public double getLatitude() {
        return Double.longBitsToDouble(preferences.getLong("latitude", 0L));
    }

    public double getLongitude() {
        return Double.longBitsToDouble(preferences.getLong("longitude", 0L));
    }

}
