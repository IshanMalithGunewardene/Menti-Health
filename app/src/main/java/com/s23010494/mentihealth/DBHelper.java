package com.s23010494.mentihealth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    // Database name and version
    private static final String DATABASE_NAME = "UserDB.db";
    private static final int DATABASE_VERSION = 4;

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static String COLUMN_NAME = "name";

    // Mood table (FIXED TYPO)
    private static final String TABLE_MOODS = "moods";
    private static final String COLUMN_MOOD_ID = "id";
    private static final String COLUMN_MOOD_EMAIL = "email";  // Fixed from COLUMN_M极OD_EMAIL
    private static final String COLUMN_MOOD = "mood";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    // Journal entries table
    private static final String TABLE_JOURNAL_ENTRIES = "journal_entries";
    private static final String COLUMN_ENTRY_ID = "id";
    private static final String COLUMN_ENTRY_EMAIL = "email";
    private static final String COLUMN_ENTRY_MOOD = "mood";
    private static final String COLUMN_ENTRY_TEXT = "entry_text";
    private static final String COLUMN_ENTRY_DATE = "entry_date";

    // Daily steps table
    private static final String TABLE_DAILY_STEPS = "daily_steps";
    private static final String COLUMN_STEPS_ID = "id";
    private static final String COLUMN_STEPS_EMAIL = "email";
    private static final String COLUMN_STEPS_DATE = "date";
    private static final String COLUMN_STEPS_COUNT = "steps_count";
    private static final String COLUMN_STEPS_BASELINE = "baseline_count";

    // Create table queries
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EMAIL + " TEXT UNIQUE, "
            + COLUMN_PASSWORD + " TEXT, "
            + COLUMN_NAME + " TEXT)";

    private static final String CREATE_TABLE_MOODS = "CREATE TABLE " + TABLE_MOODS + "("
            + COLUMN_MOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_MOOD_EMAIL + " TEXT, "  // Uses correct constant
            + COLUMN_MOOD + " TEXT, "
            + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    private static final String CREATE_TABLE_JOURNAL_ENTRIES = "CREATE TABLE " + TABLE_JOURNAL_ENTRIES + "("
            + COLUMN_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ENTRY_EMAIL + " TEXT, "
            + COLUMN_ENTRY_MOOD + " TEXT, "
            + COLUMN_ENTRY_TEXT + " TEXT, "
            + COLUMN_ENTRY_DATE + " TEXT)";

    private static final String CREATE_TABLE_DAILY_STEPS = "CREATE TABLE " + TABLE_DAILY_STEPS + "("
            + COLUMN_STEPS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_STEPS_EMAIL + " TEXT, "
            + COLUMN_STEPS_DATE + " TEXT, "
            + COLUMN_STEPS_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_STEPS_BASELINE + " INTEGER DEFAULT 0, "
            + "UNIQUE(" + COLUMN_STEPS_EMAIL + ", " + COLUMN_STEPS_DATE + "))";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DBHelper", "Creating tables...");
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_MOODS);
        db.execSQL(CREATE_TABLE_JOURNAL_ENTRIES);
        db.execSQL(CREATE_TABLE_DAILY_STEPS);
        Log.d("DBHelper", "Tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DBHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
        if (oldVersion < 4) {
            // Add daily steps table for version 4
            try {
                db.execSQL(CREATE_TABLE_DAILY_STEPS);
                Log.d("DBHelper", "Added daily steps table");
            } catch (SQLiteException e) {
                Log.e("DBHelper", "Error adding daily steps table: " + e.getMessage());
            }
        }
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

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_EMAIL + " = ?", new String[]{email});
        return rowsAffected > 0;
    }

    // Update user name
    public boolean updateName(String email, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_EMAIL + " = ?", new String[]{email});
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

    // Add user mood to moods table
    public boolean addUserMood(String email, String mood) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_MOOD_EMAIL, email);  // Uses correct constant
            values.put(COLUMN_MOOD, mood);

            Log.d("DBHelper", "Attempting to save mood: " + mood + " for email: " + email);
            long result = db.insert(TABLE_MOODS, null, values);

            if (result != -1) {
                Log.d("DBHelper", "Mood saved successfully with ID: " + result);
                return true;
            } else {
                Log.e("DBHelper", "Failed to insert mood - result was -1");
                return false;
            }
        } catch (SQLiteException e) {
            Log.e("DBHelper", "SQLite error when saving mood: " + e.getMessage());
            return false;
        }
    }

    // Add journal entry to database
    public boolean addJournalEntry(String email, String mood, String entryText, String date) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_ENTRY_EMAIL, email);
            values.put(COLUMN_ENTRY_MOOD, mood);
            values.put(COLUMN_ENTRY_TEXT, entryText);
            values.put(COLUMN_ENTRY_DATE, date);

            long result = db.insert(TABLE_JOURNAL_ENTRIES, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e("DBHelper", "Error saving journal entry: " + e.getMessage());
            return false;
        }
    }

    // Get all journal entries for a user
    public List<JournalEntry> getJournalEntries(String email) {
        List<JournalEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_JOURNAL_ENTRIES +
                        " WHERE " + COLUMN_ENTRY_EMAIL + " = ? ORDER BY " + COLUMN_ENTRY_DATE + " DESC",
                new String[]{email});

        if (cursor.moveToFirst()) {
            do {
                JournalEntry entry = new JournalEntry(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_MOOD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_DATE))
                );
                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return entries;
    }

    // Delete a journal entry by ID
    public boolean deleteJournalEntryById(int entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_JOURNAL_ENTRIES,
                    COLUMN_ENTRY_ID + " = ?",
                    new String[]{String.valueOf(entryId)});

            Log.d("DBHelper", "Deleted " + rowsDeleted + " journal entries with ID: " + entryId);
            return rowsDeleted > 0;
        } catch (SQLiteException e) {
            Log.e("DBHelper", "Error deleting journal entry by ID: " + e.getMessage());
            return false;
        }
    }

    // Update a journal entry
    public boolean updateJournalEntry(int entryId, String mood, String entryText, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ENTRY_MOOD, mood);
            values.put(COLUMN_ENTRY_TEXT, entryText);
            values.put(COLUMN_ENTRY_DATE, date);

            int rowsUpdated = db.update(TABLE_JOURNAL_ENTRIES, values,
                    COLUMN_ENTRY_ID + " = ?",
                    new String[]{String.valueOf(entryId)});

            Log.d("DBHelper", "Updated " + rowsUpdated + " journal entries with ID: " + entryId);
            return rowsUpdated > 0;
        } catch (SQLiteException e) {
            Log.e("DBHelper", "Error updating journal entry: " + e.getMessage());
            return false;
        }
    }

    // Get moods by date for calendar display
    public Map<String, String> getMoodsByDate(String email, String yearMonth) {
        Map<String, String> moodsByDate = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get the first mood entry for each date in the specified month
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_ENTRY_DATE + ", " + COLUMN_ENTRY_MOOD + 
                " FROM " + TABLE_JOURNAL_ENTRIES +
                " WHERE " + COLUMN_ENTRY_EMAIL + " = ? AND " + COLUMN_ENTRY_DATE + " LIKE ?" +
                " GROUP BY " + COLUMN_ENTRY_DATE +
                " ORDER BY " + COLUMN_ENTRY_DATE + " ASC",
                new String[]{email, yearMonth + "%"});

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_DATE));
                String mood = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTRY_MOOD));
                moodsByDate.put(date, mood);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return moodsByDate;
    }

    // Delete a journal entry (legacy method for backward compatibility)
    public boolean deleteJournalEntry(String email, String date, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_JOURNAL_ENTRIES,
                    COLUMN_ENTRY_EMAIL + " = ? AND " +
                            COLUMN_ENTRY_DATE + " = ? AND " +
                            COLUMN_ENTRY_TEXT + " = ?",
                    new String[]{email, date, text});

            Log.d("DBHelper", "Deleted " + rowsDeleted + " journal entries");
            return rowsDeleted > 0;
        } catch (SQLiteException e) {
            Log.e("DBHelper", "Error deleting journal entry: " + e.getMessage());
            return false;
        }
    }

    // Save or update daily steps for a user
    public boolean saveDailySteps(String email, String date, int currentSteps, int baselineSteps) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_STEPS_EMAIL, email);
            values.put(COLUMN_STEPS_DATE, date);
            values.put(COLUMN_STEPS_COUNT, currentSteps);
            values.put(COLUMN_STEPS_BASELINE, baselineSteps);

            // Try to insert first, if it fails due to unique constraint, update instead
            long result = db.insertWithOnConflict(TABLE_DAILY_STEPS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d("DBHelper", "Saved daily steps: " + (currentSteps - baselineSteps) + " for " + email + " on " + date);
            return result != -1;
        } catch (SQLiteException e) {
            Log.e("DBHelper", "Error saving daily steps: " + e.getMessage());
            return false;
        }
    }

    // Get daily steps for a user on a specific date
    public int getDailySteps(String email, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_STEPS_COUNT + ", " + COLUMN_STEPS_BASELINE + 
                " FROM " + TABLE_DAILY_STEPS + 
                " WHERE " + COLUMN_STEPS_EMAIL + " = ? AND " + COLUMN_STEPS_DATE + " = ?",
                new String[]{email, date});
        
        int dailySteps = 0;
        if (cursor.moveToFirst()) {
            int totalSteps = cursor.getInt(0);
            int baseline = cursor.getInt(1);
            dailySteps = totalSteps - baseline;
        }
        cursor.close();
        return Math.max(0, dailySteps); // Ensure non-negative
    }

    // Get baseline steps for a user on a specific date
    public int getBaselineSteps(String email, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_STEPS_BASELINE + 
                " FROM " + TABLE_DAILY_STEPS + 
                " WHERE " + COLUMN_STEPS_EMAIL + " = ? AND " + COLUMN_STEPS_DATE + " = ?",
                new String[]{email, date});
        
        int baseline = 0;
        if (cursor.moveToFirst()) {
            baseline = cursor.getInt(0);
        }
        cursor.close();
        return baseline;
    }
}
