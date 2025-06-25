package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText etNewPassword, etConfirmPassword;
    private DBHelper dbHelper;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        dbHelper = new DBHelper(this);
        email = getIntent().getStringExtra("EMAIL");

        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        Button btnUpdate = findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(v -> updatePassword());
    }

    private void updatePassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update password in database
        if (dbHelper.updatePassword(email, newPassword)) {
            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, login.class));
            finish();
        } else {
            Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT).show();
        }
    }
}
