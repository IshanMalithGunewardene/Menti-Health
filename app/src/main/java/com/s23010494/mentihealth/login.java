package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        dbHelper = new DBHelper(this);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvSignup = findViewById(R.id.tv_signup);
        TextView tvReset = findViewById(R.id.tv_reset);

        btnLogin.setOnClickListener(v -> authenticateUser());

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(login.this, signup.class));
            finish();
        });

        tvReset.setOnClickListener(v -> {
            startActivity(new Intent(login.this, EnterEmailActivity.class));
        });
    }

    private void authenticateUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.authenticateUser(email, password)) {
            // Check if user already has a name stored
            if (dbHelper.hasName(email)) {
                // User already has a name - welcome them back and go to Dashboard
                String userName = dbHelper.getName(email);
                Toast.makeText(this, "Welcome back, " + userName + "!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(login.this, JournalDashboardActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", userName);
                // Clear the back stack so user can't go back to login
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // User doesn't have a name yet - go to AskNameActivity
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(login.this, AskNameActivity.class);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}

