package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HowIsTodayActivity extends AppCompatActivity {
    private static final String TAG = "HowIsTodayActivity";
    private EditText etExplain;
    private String email;
    private String name;
    private String mood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_is_today);

        // Get extras with null checks
        email = getIntent().getStringExtra("EMAIL");
        name = getIntent().getStringExtra("NAME");
        mood = getIntent().getStringExtra("MOOD");

        // Log received values for debugging
        Log.d(TAG, "Received EMAIL: " + (email != null ? email : "null"));
        Log.d(TAG, "Received NAME: " + (name != null ? name : "null"));
        Log.d(TAG, "Received MOOD: " + (mood != null ? mood : "null"));

        etExplain = findViewById(R.id.et_explain);
        Button btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> {
            String explanation = etExplain.getText().toString().trim();
            if (explanation.isEmpty()) {
                Toast.makeText(this, "Please explain how you feel", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current date
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Validate mood
            if (mood == null || mood.isEmpty()) {
                Toast.makeText(this, "Mood not found. Please select your mood first.", Toast.LENGTH_SHORT).show();
                return;
            }

            DBHelper dbHelper = new DBHelper(this);
            boolean saved = dbHelper.addJournalEntry(email, mood, explanation, currentDate);

            if (saved) {
                Toast.makeText(this, "Journal saved successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, JournalDashboardActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save journal", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to save journal entry to database");
            }
        });
    }
}

