package com.example.fittrackapp;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.api.ApiClient;
import com.example.fittrackapp.api.ApiService;
import com.example.fittrackapp.models.Exercise;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExercisesActivity extends AppCompatActivity {

    private int exerciseTableDataRowIndex;

    EditText etExerciseName, etMuscleGroup, etAliasName;
    Button btnAddExercise;
    TextView tvExercises;
    TableLayout tableExercises;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        etExerciseName = findViewById(R.id.etExerciseName);
        etMuscleGroup = findViewById(R.id.etMuscleGroup);
        etAliasName = findViewById(R.id.etAliasName);

        btnAddExercise = findViewById(R.id.btnAddExercise);
        tvExercises = findViewById(R.id.tvExercises);
        tableExercises = findViewById(R.id.tableExercises);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnAddExercise.setOnClickListener(v -> addExercise());
        loadExercises();
    }

    private void addExercise() {
        String exerciseName = etExerciseName.getText().toString().trim();
        String muscleGroup = etMuscleGroup.getText().toString().trim();
        String aliasName = etAliasName.getText().toString().trim();

        if (exerciseName.isEmpty() || muscleGroup.isEmpty() || aliasName.isEmpty()) {
            showPopup("Missing Details", "Please fill exercise name, muscle group and alias name.");
            return;
        }

        String generatedQrCode = generateQrCodeValue(exerciseName, aliasName);

        Exercise exercise = new Exercise(
                exerciseName,
                muscleGroup,
                aliasName,
                generatedQrCode
        );

        apiService.addExercise(exercise).enqueue(new Callback<Exercise>() {
            @Override
            public void onResponse(Call<Exercise> call, Response<Exercise> response) {
                if (response.isSuccessful()) {
                    showPopup("Success", "Exercise added successfully.");

                    etExerciseName.setText("");
                    etMuscleGroup.setText("");
                    etAliasName.setText("");

                    loadExercises();
                } else {
                    showPopup("Failed", "Failed to add exercise. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Exercise> call, Throwable t) {
                showPopup("Error", t.getMessage());
            }
        });
    }

    private String generateQrCodeValue(String exerciseName, String aliasName) {
        String cleanExerciseName = exerciseName
                .replaceAll("\\s+", "_")
                .toLowerCase();

        String cleanAliasName = aliasName
                .replaceAll("\\s+", "_")
                .toLowerCase();

        return "FITTRACK_" + cleanExerciseName + "_" + cleanAliasName + "_" + System.currentTimeMillis();
    }

    private void loadExercises() {
        apiService.getExercises().enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body();

                    tableExercises.removeAllViews();
                    tvExercises.setText("");
                    exerciseTableDataRowIndex = 0;

                    if (exercises.isEmpty()) {
                        tvExercises.setText("No exercises found.");
                        return;
                    }

                    addExerciseTableHeader();

                    for (Exercise exercise : exercises) {
                        addExerciseTableRow(exercise);
                    }

                } else {
                    tvExercises.setText("No exercises found.");
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                tvExercises.setText("Error: " + t.getMessage());
            }
        });
    }

    private void addExerciseTableHeader() {
        TableRow headerRow = new TableRow(this);

        headerRow.addView(exerciseCell("Name", true));
        headerRow.addView(exerciseCell("Muscle", true));
        headerRow.addView(exerciseCell("Alias", true));
        headerRow.addView(exerciseCell("QR Code", true));

        tableExercises.addView(headerRow);
    }

    private void addExerciseTableRow(Exercise exercise) {
        TableRow row = new TableRow(this);

        int stripe = exerciseTableDataRowIndex++;

        row.addView(exerciseCell(exercise.exerciseName, false, stripe));
        row.addView(exerciseCell(exercise.muscleGroup, false, stripe));
        row.addView(exerciseCell(exercise.aliasName, false, stripe));

        Button qrButton = new Button(this);
        qrButton.setText("Show QR");
        TableUiHelper.applyCompactPillButtonStyle(this, qrButton);

        qrButton.setOnClickListener(v -> showQrCodeDialog(exercise));

        row.addView(qrButton, TableUiHelper.compactTableButtonParams());

        tableExercises.addView(row);
    }

    private TextView exerciseCell(String label, boolean isHeader) {
        return exerciseCell(label, isHeader, 0);
    }

    private TextView exerciseCell(String text, boolean isHeader, int stripeIndex) {
        if (!isHeader && (text == null || text.trim().isEmpty())) {
            text = "-";
        }
        return TableUiHelper.createTableCell(this, text, isHeader, stripeIndex);
    }

    private void showQrCodeDialog(Exercise exercise) {
        if (exercise.qrCode == null || exercise.qrCode.trim().isEmpty()) {
            showPopup("QR Code Missing", "No QR code value found for this exercise.");
            return;
        }

        Bitmap qrBitmap = generateQrBitmap(exercise.qrCode);

        if (qrBitmap == null) {
            showPopup("Error", "Failed to generate QR code.");
            return;
        }

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(qrBitmap);
        imageView.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(this)
                .setTitle(exercise.exerciseName + " QR Code")
                .setView(imageView)
                .setMessage("QR Text:\n" + exercise.qrCode)
                .setPositiveButton("OK", null)
                .show();
    }

    private Bitmap generateQrBitmap(String qrText) {
        try {
            int size = 600;

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    qrText,
                    BarcodeFormat.QR_CODE,
                    size,
                    size
            );

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;

        } catch (Exception e) {
            return null;
        }
    }

    private void showPopup(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}