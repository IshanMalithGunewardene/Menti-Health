package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AskNameActivity extends AppCompatActivity {
    private EditText etName;
    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_name);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);
        etName = findViewById(R.id.et_name);
        Button btnStart = findViewById(R.id.btn_start);

        // Get email from intent
        email = getIntent().getStringExtra("EMAIL");

        btnStart.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.updateName(email, name)) {
                // Save login session now that we have complete user info
                sessionManager.createLoginSession(email, name);
                
                Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                
                // Launch Terms and Conditions page instead of MainActivity
                Intent intent = new Intent(AskNameActivity.this, TermsConditionsActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                // Clear the back stack so user can't go back to login/signup
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                
            } else {
                Toast.makeText(this, "Error saving name", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

