package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JournalDashboardActivity extends AppCompatActivity {
    private RecyclerView rvJournalEntries;
    private JournalEntriesAdapter adapter;
    private DBHelper dbHelper;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_dashboard);

        dbHelper = new DBHelper(this);
        email = getIntent().getStringExtra("EMAIL");

        rvJournalEntries = findViewById(R.id.rv_journal_entries);
        rvJournalEntries.setLayoutManager(new LinearLayoutManager(this));

        loadJournalEntries();
        setupBottomNavigation();
    }

    private void loadJournalEntries() {
        List<JournalEntry> entries = dbHelper.getJournalEntries(email);
        adapter = new JournalEntriesAdapter(entries);
        rvJournalEntries.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        ImageButton btnPlus = findViewById(R.id.btn_plus);
        btnPlus.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodTrackerActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", getIntent().getStringExtra("NAME"));
            startActivity(intent);
        });

        ImageButton btnJournal = findViewById(R.id.btn_journal);
        btnJournal.setOnClickListener(v -> loadJournalEntries());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJournalEntries(); // Refresh entries when returning to this activity
    }
}

