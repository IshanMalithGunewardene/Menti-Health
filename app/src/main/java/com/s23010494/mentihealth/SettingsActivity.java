package com.s23010494.mentihealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private String email, name;
    private Switch switchNotifications;
    private Switch switchDarkMode;
    private TextView tvUserEmail;
    private TextView tvUserName;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get user data from intent
        email = getIntent().getStringExtra("EMAIL");
        name = getIntent().getStringExtra("NAME");

        sharedPreferences = getSharedPreferences("MentiHealthPrefs", MODE_PRIVATE);

        initViews();
        setupUserInfo();
        setupPreferences();
        setupBottomNavigation();
        setupBackButton();
    }

    private void initViews() {
        switchNotifications = findViewById(R.id.switch_notifications);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserName = findViewById(R.id.tv_user_name);
        
        // Setup logout button
        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void setupUserInfo() {
        if (name != null) {
            tvUserName.setText(name);
        }
        if (email != null) {
            tvUserEmail.setText(email);
        }
    }

    private void setupPreferences() {
        // Load saved preferences
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode_enabled", false);

        switchNotifications.setChecked(notificationsEnabled);
        switchDarkMode.setChecked(darkModeEnabled);

        // Set up listeners
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode_enabled", isChecked);
            editor.apply();
            // Note: You might want to restart the activity or apply theme changes here
        });
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupBottomNavigation() {
        ImageButton btnJournal = findViewById(R.id.btn_journal);
        ImageButton btnStats = findViewById(R.id.btn_stats);
        ImageButton btnPlus = findViewById(R.id.btn_plus);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);

        btnJournal.setOnClickListener(v -> {
            Intent intent = new Intent(this, JournalDashboardActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
            finish();
        });

        btnStats.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatsActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
            finish();
        });

        btnPlus.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodTrackerActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
        });

        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
            finish();
        });

        ImageButton btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add(0, 1, 0, "FAQ");
        popupMenu.getMenu().add(0, 2, 0, "Logout");
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == 1) { // FAQ
                Intent intent = new Intent(this, FAQActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
                return true;
            } else if (itemId == 2) { // Logout
                showLogoutConfirmationDialog();
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear session data
                    SessionManager sessionManager = new SessionManager(this);
                    sessionManager.logoutUser();
                    
                    // Show logout message
                    Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Redirect to welcome screen
                    Intent intent = new Intent(this, welcome.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
