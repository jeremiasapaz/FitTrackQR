package com.example.fittrackapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

public class DashboardActivity extends AppCompatActivity {

    Button btnExercises;
    Button btnWorkouts;
    Button btnWorkoutLogs;
    Button btnWeeklyPlans;
    Button btnBodyMeasurements;
    Button btnScanQrCode;

    AppCompatImageButton btnThemeLight;
    AppCompatImageButton btnThemeDark;

    int userId;
    String fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        userId = getIntent().getIntExtra("USER_ID", -1);
        fullName = getIntent().getStringExtra("FULL_NAME");

        if (userId == -1) {
            userId = getSharedPreferences("UserSession", MODE_PRIVATE)
                    .getInt("userId", -1);
        }

        if (userId == -1) {
            showPopup("Error", "User ID not found. Please login again.");
            return;
        }

        getSharedPreferences("UserSession", MODE_PRIVATE)
                .edit()
                .putInt("userId", userId)
                .putString("fullName", fullName)
                .apply();

        btnExercises = findViewById(R.id.btnExercises);
        btnWorkouts = findViewById(R.id.btnWorkouts);
        btnWorkoutLogs = findViewById(R.id.btnWorkoutLogs);
        btnWeeklyPlans = findViewById(R.id.btnWeeklyPlans);
        btnBodyMeasurements = findViewById(R.id.btnBodyMeasurements);
        btnScanQrCode = findViewById(R.id.btnScanQrCode);

        btnThemeLight = findViewById(R.id.btnThemeLight);
        btnThemeDark = findViewById(R.id.btnThemeDark);

        setupThemeIcons();

        btnExercises.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExercisesActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        btnWorkouts.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkoutsActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        btnWorkoutLogs.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkoutLogsActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        btnWeeklyPlans.setOnClickListener(v -> {
            Intent intent = new Intent(this, WeeklyPlansActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        btnBodyMeasurements.setOnClickListener(v -> {
            Intent intent = new Intent(this, BodyMeasurementsActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        btnScanQrCode.setOnClickListener(v -> {
            Intent intent = new Intent(this, QrScannerActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btnThemeLight != null && btnThemeDark != null) {
            refreshThemeIconHighlight();
        }
    }

    private void setupThemeIcons() {
        btnThemeLight.setOnClickListener(v ->
                ThemeHelper.persistAndApply(this, ThemeHelper.MODE_LIGHT));

        btnThemeDark.setOnClickListener(v ->
                ThemeHelper.persistAndApply(this, ThemeHelper.MODE_DARK));

        refreshThemeIconHighlight();
    }

    /** Highlights sun vs moon based on the UI mode users actually see (includes “system”). */
    private void refreshThemeIconHighlight() {
        boolean effectiveDark = isEffectiveNightMode();
        btnThemeLight.setAlpha(effectiveDark ? 0.38f : 1f);
        btnThemeDark.setAlpha(effectiveDark ? 1f : 0.38f);
    }

    private boolean isEffectiveNightMode() {
        String saved = ThemeHelper.getSavedMode(this);
        if (ThemeHelper.MODE_DARK.equals(saved)) {
            return true;
        }
        if (ThemeHelper.MODE_LIGHT.equals(saved)) {
            return false;
        }
        int nightFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void showPopup(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
