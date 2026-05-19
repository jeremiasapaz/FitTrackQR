package com.example.fittrackapp;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.api.ApiClient;
import com.example.fittrackapp.api.ApiService;
import com.example.fittrackapp.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (
                fullName.isEmpty() ||
                        email.isEmpty() ||
                        password.isEmpty() ||
                        confirmPassword.isEmpty()
        ) {
            showPopup("Missing Details", "Please fill all fields.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showPopup("Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            showPopup("Weak Password", "Password must contain at least 6 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showPopup("Password Mismatch", "Passwords do not match.");
            return;
        }

        User user = new User(fullName, email, password);

        apiService.register(user).enqueue(new Callback<Object>() {

            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if (response.isSuccessful()) {

                    showPopup("Success", "Registration successful.");

                    finish();

                } else {

                    showPopup("Registration Failed", "Could not register user.");
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {

                showPopup("Connection Error", t.getMessage());
            }
        });
    }

    private void showPopup(String title, String message) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}