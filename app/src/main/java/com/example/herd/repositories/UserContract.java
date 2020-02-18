package com.example.herd.repositories;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

public class UserContract {

    //Private constructor so class isn't instantiated accidentally
    private UserContract() {}

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_POSTS = "posts";
        public static final String COLUMN_NAME_COMMENTS = "comments";
        public static final String COLUMN_NAME_LIKED_POSTS = "liked_posts";
        public static final String COLUMN_NAME_DISLIKED_POSTS = "disliked_posts";
        public static final String COLUMN_NAME_LIKED_COMMENTS = "liked_comments";
        public static final String COLUMN_NAME_DISLIKED_COMMENTS = "disliked_comments";
    }

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + UserEntry.TABLE_NAME
                    + " (" + UserEntry.COLUMN_NAME_USER_ID + " TEXT,"
                    + UserEntry.COLUMN_NAME_POSTS + " TEXT,"
                    + UserEntry.COLUMN_NAME_COMMENTS + " TEXT,"
                    + UserEntry.COLUMN_NAME_LIKED_POSTS + " TEXT,"
                    + UserEntry.COLUMN_NAME_DISLIKED_POSTS + " TEXT,"
                    + UserEntry.COLUMN_NAME_LIKED_COMMENTS + " TEXT,"
                    + UserEntry.COLUMN_NAME_DISLIKED_COMMENTS + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;

    public static class UsersDBHelper extends SQLiteOpenHelper {

        // Database version and name
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "User.db";

        public UsersDBHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_USERS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
