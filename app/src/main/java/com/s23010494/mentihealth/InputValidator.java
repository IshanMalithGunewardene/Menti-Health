package com.s23010494.mentihealth;

import android.util.Patterns;

public class InputValidator {
    // Email validation
    public static boolean isValidEmail(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Password validation (min 8 chars, at least 1 digit, 1 letter)
    public static boolean isValidPassword(String password) {
        return password.length() >= 8 && 
               password.matches(".*[0-9].*") && 
               password.matches(".*[a-zA-Z].*");
    }
}

