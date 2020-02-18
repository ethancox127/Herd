package com.example.herd.repositories;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.herd.models.User;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LocalDBRepsoitory {

    UserContract.UsersDBHelper dbHelper;
    SQLiteDatabase writeableDB, readableDB;

    public LocalDBRepsoitory(Context context) {
        dbHelper = new UserContract.UsersDBHelper(context);
        writeableDB = dbHelper.getWritableDatabase();
        readableDB = dbHelper.getReadableDatabase();
    }

    public void createTable() {
        dbHelper.onCreate(writeableDB);
    }

    public LiveData<Boolean> addUser(User user) {
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_NAME_USER_ID, user.getUserID());
        values.put(UserContract.UserEntry.COLUMN_NAME_POSTS, String.valueOf(user.getPosts()));
        values.put(UserContract.UserEntry.COLUMN_NAME_COMMENTS, String.valueOf(user.getComments()));
        values.put(UserContract.UserEntry.COLUMN_NAME_LIKED_POSTS, String.valueOf(user.getLikedPosts()));
        values.put(UserContract.UserEntry.COLUMN_NAME_DISLIKED_POSTS, String.valueOf(user.getDislikedPosts()));
        values.put(UserContract.UserEntry.COLUMN_NAME_LIKED_COMMENTS, String.valueOf(user.getLikedComments()));
        values.put(UserContract.UserEntry.COLUMN_NAME_DISLIKED_COMMENTS, String.valueOf(user.getDislikedComments()));

        long newRowId = writeableDB.insert(UserContract.UserEntry.TABLE_NAME, null, values);

        MutableLiveData<Boolean> result = new MutableLiveData<>();
        if (newRowId != -1) {
            Log.d("LocalDBRepository", "Row inserted at " + newRowId);
            result.setValue(true);
        } else {
            result.setValue(false);
        }

        return result;
    }

    public ArrayList<String> getList(int columnIndex) {
        String selectQuery = "SELECT * FROM " + UserContract.UserEntry.TABLE_NAME;
        Cursor cursor = readableDB.rawQuery(selectQuery, null);
        String result = "";

        if (cursor != null  &&  cursor.moveToFirst()) {
            result = cursor.getString(columnIndex);
            result = result.replaceAll("\\p{P}", "");
        }

        return new ArrayList<String>(Arrays.asList(result.split(" ")));
    }

    public LiveData<Boolean> updateColumn(String column, String value) {
        ContentValues values = new ContentValues();
        values.put(column, value);

        Log.d("LocalDBRepository", "Column " + column);

        long row = writeableDB.update(UserContract.UserEntry.TABLE_NAME, values, null,
                null);

        MutableLiveData<Boolean> result = new MutableLiveData<>();
        if (row != -1) {
            Log.d("LocalDBRepository", "Row updated at " + row);
            result.setValue(true);
        } else {
            result.setValue(false);
        }

        return result;
    }

}
