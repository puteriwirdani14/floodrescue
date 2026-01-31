package com.example.floodrescue.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.floodrescue.databinding.ActivityRegisterBinding;
import com.example.floodrescue.utils.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        // Register button click
        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.editTextName.getText().toString().trim();
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();
            String confirmPassword = binding.editTextConfirmPassword.getText().toString().trim();
            String phone = binding.editTextPhone.getText().toString().trim();

            if (validateInput(name, email, password, confirmPassword, phone)) {
                performRegistration(name, email, password, phone);
            }
        });

        // Back to login links
        binding.textViewLogin.setOnClickListener(v -> finish());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private boolean validateInput(String name,
                                  String email,
                                  String password,
                                  String confirmPassword,
                                  String phone) {

        if (name.isEmpty()) {
            binding.editTextName.setError("Name required");
            return false;
        }

        if (email.isEmpty()) {
            binding.editTextEmail.setError("Email required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Invalid email format");
            return false;
        }

        if (password.isEmpty()) {
            binding.editTextPassword.setError("Password required");
            return false;
        }

        if (password.length() < 6) {
            binding.editTextPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.editTextConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void performRegistration(String name, String email, String password, String phone) {
        boolean success = dbHelper.addUser(email, password, name, phone);

        if (success) {
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Email may already exist.", Toast.LENGTH_SHORT).show();
        }
    }
}
