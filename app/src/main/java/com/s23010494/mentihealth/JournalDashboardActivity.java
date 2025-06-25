package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JournalDashboardActivity extends AppCompatActivity {
    private static final String TAG = "JournalDashboard";
    private RecyclerView rvJournalEntries;
    private JournalEntriesAdapter adapter;
    private DBHelper dbHelper;
    private String email;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_dashboard);
        Log.d(TAG, "Activity created");

        // Initialize DBHelper
        dbHelper = new DBHelper(this);
        
        // Get intent extras with null checks
        email = getIntent().getStringExtra("EMAIL");
        name = getIntent().getStringExtra("NAME");
        
        if (email == null) {
            Log.e(TAG, "Email not received - finishing activity");
            finish();
            return;
        }

        // Initialize RecyclerView
        rvJournalEntries = findViewById(R.id.rv_journal_entries);
        if (rvJournalEntries == null) {
            Log.e(TAG, "RecyclerView not found in layout");
            return;
        }
        rvJournalEntries.setLayoutManager(new LinearLayoutManager(this));

        // Load journal entries
        loadJournalEntries();
        
        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void loadJournalEntries() {
        List<JournalEntry> entries = dbHelper.getJournalEntries(email);
        if (entries == null) {
            Log.e(TAG, "Failed to load journal entries");
            return;
        }
        
        adapter = new JournalEntriesAdapter(entries);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - refreshing entries");
        loadJournalEntries();
    }
}

