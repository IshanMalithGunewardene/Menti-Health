package com.s23010494.mentihealth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "MentiHealthSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    
    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    /**
     * Create login session
     */
    public void createLoginSession(String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NAME, name);
        editor.apply();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Get stored email
     */
    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }
    
    /**
     * Get stored name
     */
    public String getName() {
        return pref.getString(KEY_NAME, null);
    }
    
    /**
     * Clear session (logout)
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Check if session data is complete
     */
    public boolean isSessionComplete() {
        return isLoggedIn() && getEmail() != null && getName() != null;
    }
}
