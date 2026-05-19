package com.example.fittrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.api.ApiClient;
import com.example.fittrackapp.api.ApiService;
import com.example.fittrackapp.models.Exercise;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrScannerActivity extends AppCompatActivity {

    ApiService apiService;

    TextView tvScannerStatus, tvExerciseTitle, tvExerciseDetails;
    Button btnAddWorkoutLog;

    Exercise selectedExercise;
    int loggedUserId;

    private final ActivityResultLauncher<ScanOptions> qrLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedQrText = result.getContents();
                    findExerciseByQrCode(scannedQrText);
                } else {
                    tvScannerStatus.setText("QR scan cancelled.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        tvScannerStatus = findViewById(R.id.tvScannerStatus);
        tvExerciseTitle = findViewById(R.id.tvExerciseTitle);
        tvExerciseDetails = findViewById(R.id.tvExerciseDetails);
        btnAddWorkoutLog = findViewById(R.id.btnAddWorkoutLog);

        apiService = ApiClient.getClient().create(ApiService.class);

        loggedUserId = getIntent().getIntExtra("USER_ID", -1);

        if (loggedUserId == -1) {
            loggedUserId = getSharedPreferences("UserSession", MODE_PRIVATE)
                    .getInt("userId", -1);
        }

        btnAddWorkoutLog.setOnClickListener(v -> {
            if (selectedExercise != null) {
                Intent intent = new Intent(QrScannerActivity.this, WorkoutLogsActivity.class);
                intent.putExtra("USER_ID", loggedUserId);
                intent.putExtra("SELECTED_EXERCISE_ID", selectedExercise.exerciseId);
                startActivity(intent);
            }
        });

        startQrScanner();
    }

    private void startQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan exercise QR code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);

        qrLauncher.launch(options);
    }

    private void findExerciseByQrCode(String scannedQrText) {
        tvScannerStatus.setText("Searching exercise details...");

        apiService.getExercises().enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    selectedExercise = null;

                    for (Exercise exercise : response.body()) {
                        if (exercise.qrCode != null && exercise.qrCode.equals(scannedQrText)) {
                            selectedExercise = exercise;
                            break;
                        }
                    }

                    if (selectedExercise != null) {
                        showExerciseDetailsOnPage(selectedExercise);
                    } else {
                        tvScannerStatus.setText("No exercise found for this QR code.");
                        tvExerciseTitle.setVisibility(View.GONE);
                        tvExerciseDetails.setVisibility(View.GONE);
                        btnAddWorkoutLog.setVisibility(View.GONE);
                    }
                } else {
                    tvScannerStatus.setText("Failed to load exercises. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                tvScannerStatus.setText("Error: " + t.getMessage());
            }
        });
    }

    private void showExerciseDetailsOnPage(Exercise exercise) {
        tvScannerStatus.setText("Exercise found successfully.");

        tvExerciseTitle.setVisibility(View.VISIBLE);
        tvExerciseDetails.setVisibility(View.VISIBLE);
        btnAddWorkoutLog.setVisibility(View.VISIBLE);

        String details =
                "Exercise Name: " + getSafeText(exercise.exerciseName) + "\n\n" +
                        "Muscle Group: " + getSafeText(exercise.muscleGroup) + "\n\n" +
                        "Alias Name: " + getSafeText(exercise.aliasName);

        tvExerciseDetails.setText(details);
    }

    private String getSafeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }

        return text;
    }
}