package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EnterEmailActivity extends AppCompatActivity {
    private EditText etEmail;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_enter_email);

        dbHelper = new DBHelper(this);
        etEmail = findViewById(R.id.et_email);

        // Reset Password Button
        Button btnReset = findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(v -> validateEmail());

        // Back to Login
        TextView tvBackLogin = findViewById(R.id.tv_back_login);
        tvBackLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, login.class));
            finish();
        });
    }

    private void validateEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dbHelper.checkEmailExists(email)) {
            Toast.makeText(this, "Email not registered", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed to password reset screen
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("EMAIL", email);
        startActivity(intent);
        finish();
    }
}
