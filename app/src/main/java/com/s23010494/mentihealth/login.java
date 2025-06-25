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
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(login.this, AskNameActivity.class);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}

