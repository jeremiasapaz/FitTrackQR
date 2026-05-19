package com.example.fittrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.api.ApiClient;
import com.example.fittrackapp.api.ApiService;
import com.example.fittrackapp.models.User;
import com.example.fittrackapp.models.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoRegister;
    TextView tvResult;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        tvResult = findViewById(R.id.tvResult);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showPopup("Missing Details", "Please enter email and password.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showPopup("Invalid Email", "Please enter a valid email address.");
            return;
        }

        User user = new User("", email, password);

        apiService.login(user).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse loginResponse = response.body();

                    showPopup("Success", "Login successful.");

                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);

                    intent.putExtra("USER_ID", loginResponse.userId);
                    intent.putExtra("FULL_NAME", loginResponse.fullName);

                    startActivity(intent);

                } else {

                    showPopup("Login Failed", "Invalid login credentials.");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

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