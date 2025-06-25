package com.s23010494.mentihealth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDB.db";
    private static final int DATABASE_VERSION = 2; // Incremented for schema change

    // Table and column names
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name"; // Added name column

    // Create table query with name column
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EMAIL + " TEXT UNIQUE, "
            + COLUMN_PASSWORD + " TEXT, "
            + COLUMN_NAME + " TEXT)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Add new user to database
    public boolean addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Check if email exists
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?",
                new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Authenticate user
    public boolean authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{email, password});
        boolean authenticated = cursor.getCount() > 0;
        cursor.close();
        return authenticated;
    }

    // Update user password
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
        return rowsAffected > 0;
    }

    // Update user name
    public boolean updateName(String email, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);

        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
        return rowsAffected > 0;
    }

    // Get user name
    public String getName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_NAME + " FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?",
                new String[]{email});
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
            if (name == null) {
                name = "";
            }
        }
        cursor.close();
        return name;
    }

    // Check if user has set their name
    public boolean hasName(String email) {
        String name = getName(email);
        return !name.isEmpty();
    }
}

