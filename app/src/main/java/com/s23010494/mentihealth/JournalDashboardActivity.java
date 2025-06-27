package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
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
    private String email;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_dashboard);
        Log.d(TAG, "Activity created");

        dbHelper = new DBHelper(this);

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
                Toast.makeText(JournalDashboardActivity.this, "Edit: " + entry.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(JournalEntry entry, int position) {
                Toast.makeText(JournalDashboardActivity.this, "Delete: " + entry.getText(), Toast.LENGTH_SHORT).show();
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
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - refreshing entries");
        loadJournalEntries();
    }
}

