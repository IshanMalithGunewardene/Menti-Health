package com.s23010494.mentihealth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JournalDashboardActivity extends AppCompatActivity {
    private static final String TAG = "JournalDashboard";
    private RecyclerView rvJournalEntries;
    private JournalEntriesAdapter adapter;
    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private String email;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_dashboard);
        Log.d(TAG, "Activity created");

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        email = getIntent().getStringExtra("EMAIL");
        name = getIntent().getStringExtra("NAME");

        if (email == null) {
            Log.e(TAG, "Email not received - finishing activity");
            finish();
            return;
        }

        rvJournalEntries = findViewById(R.id.rv_journal_entries);
        if (rvJournalEntries == null) {
            Log.e(TAG, "RecyclerView not found in layout");
            return;
        }
        rvJournalEntries.setLayoutManager(new LinearLayoutManager(this));

        loadJournalEntries();
        setupBottomNavigation();
    }

    private void loadJournalEntries() {
        List<JournalEntry> entries = dbHelper.getJournalEntries(email);
        adapter = new JournalEntriesAdapter(entries);

        // Set up edit/delete actions
        adapter.setOnEntryActionListener(new JournalEntriesAdapter.OnEntryActionListener() {
            @Override
            public void onEdit(JournalEntry entry, int position) {
                // Launch edit activity with entry data
                Intent editIntent = new Intent(JournalDashboardActivity.this, EditJournalActivity.class);
                editIntent.putExtra("ENTRY_ID", entry.getId());
                editIntent.putExtra("ENTRY_MOOD", entry.getMood());
                editIntent.putExtra("ENTRY_TEXT", entry.getText());
                editIntent.putExtra("ENTRY_DATE", entry.getDate());
                startActivity(editIntent);
            }

            @Override
            public void onDelete(JournalEntry entry, int position) {
                // Show confirmation dialog before deleting
                showDeleteConfirmationDialog(entry, position);
            }
        });

        rvJournalEntries.setAdapter(adapter);
        Log.d(TAG, "Loaded " + entries.size() + " journal entries");
    }

    private void setupBottomNavigation() {
        ImageButton btnPlus = findViewById(R.id.btn_plus);
        if (btnPlus == null) {
            Log.e(TAG, "Plus button not found");
            return;
        }
        btnPlus.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodTrackerActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
        });

        ImageButton btnJournal = findViewById(R.id.btn_journal);
        if (btnJournal != null) {
            btnJournal.setOnClickListener(v -> loadJournalEntries());
        }

        // Add Stats button navigation
        ImageButton btnStats = findViewById(R.id.btn_stats);
        if (btnStats != null) {
            btnStats.setOnClickListener(v -> {
                Intent intent = new Intent(this, StatsActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
            });
        }

        // Add Calendar button navigation
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);
        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v -> {
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
            });
        }
        
        // Add Menu button with popup menu
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> showPopupMenu(v));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - refreshing entries");
        loadJournalEntries();
    }

    private void showDeleteConfirmationDialog(JournalEntry entry, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this journal entry? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteJournalEntryById(entry.getId());
                    if (deleted) {
                        Toast.makeText(this, "Entry deleted successfully", Toast.LENGTH_SHORT).show();
                        loadJournalEntries(); // Refresh the list
                    } else {
                        Toast.makeText(this, "Failed to delete entry", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showMenuDialog() {
        String[] menuOptions = {"Profile", "Settings", "Logout"};
        
        new AlertDialog.Builder(this)
                .setTitle("Menu")
                .setItems(menuOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Profile
                            Toast.makeText(this, "Profile: " + name + " (" + email + ")", Toast.LENGTH_LONG).show();
                            break;
                        case 1: // Settings
                            Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // Logout
                            showLogoutConfirmationDialog();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_faq) {
                Intent intent = new Intent(this, FAQActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
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

