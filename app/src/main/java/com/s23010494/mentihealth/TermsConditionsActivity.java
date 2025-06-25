package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TermsConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);

        // Get name and email from intent
        String userName = getIntent().getStringExtra("NAME");
        String userEmail = getIntent().getStringExtra("EMAIL");
        if (userName == null || userName.isEmpty()) {
            userName = "User";
        }

        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("We are happy to help you, " + userName);

        Button btnStart = findViewById(R.id.btn_start);
        String finalName = userName;
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(TermsConditionsActivity.this, MoodTrackerActivity.class);
            intent.putExtra("NAME", finalName);
            intent.putExtra("EMAIL", userEmail);
            startActivity(intent);
            finish();
        });
    }
}

