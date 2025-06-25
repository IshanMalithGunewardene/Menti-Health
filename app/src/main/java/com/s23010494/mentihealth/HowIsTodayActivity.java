package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HowIsTodayActivity extends AppCompatActivity {
    private EditText etExplain;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_is_today);

        email = getIntent().getStringExtra("EMAIL");
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

            // Get mood from previous activity (make sure you pass "MOOD" in the intent)
            String mood = getIntent().getStringExtra("MOOD");

            DBHelper dbHelper = new DBHelper(this);
            boolean saved = dbHelper.addJournalEntry(email, mood, explanation, currentDate);

            if (saved) {
                Toast.makeText(this, "Journal saved successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, JournalDashboardActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", getIntent().getStringExtra("NAME"));
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save journal", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

