package com.example.fittrackapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.api.ApiClient;
import com.example.fittrackapp.api.ApiService;
import com.example.fittrackapp.models.Workout;

import java.util.List;
import android.app.DatePickerDialog;
import java.util.Calendar;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutsActivity extends AppCompatActivity {

    private int workoutTableDataRowIndex;

    EditText etWorkoutDate, etNotes;
    Button btnAddWorkout;
    TextView tvWorkouts;
    TableLayout tableWorkouts;
    int userId;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        userId = getIntent().getIntExtra("USER_ID", 0);
        etWorkoutDate = findViewById(R.id.etWorkoutDate);
        etWorkoutDate.setOnClickListener(v -> showDatePicker());
        etNotes = findViewById(R.id.etNotes);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        tvWorkouts = findViewById(R.id.tvWorkouts);
        tableWorkouts = findViewById(R.id.tableWorkouts);


        apiService = ApiClient.getClient().create(ApiService.class);

        btnAddWorkout.setOnClickListener(v -> addWorkout());
        loadWorkouts();
    }

    private void addWorkout() {
        String date = etWorkoutDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (userId == 0) {
            showPopup("User Error", "Logged-in user was not detected. Please login again.");
            return;
        }

        if (date.isEmpty()) {
            showPopup("Missing Details", "Please enter workout date.");
            return;
        }

        Workout workout = new Workout(userId, date + "T00:00:00Z", notes);

        apiService.addWorkout(workout).enqueue(new Callback<Workout>() {
            @Override
            public void onResponse(Call<Workout> call, Response<Workout> response) {
                if (response.isSuccessful()) {
                    showPopup("Success", "Workout added successfully.");
                    tvWorkouts.setText("Workout added successfully.");
                    loadWorkouts();
                } else {
                    showPopup("Failed", "Failed to add workout. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Workout> call, Throwable t) {
                showPopup("Error", t.getMessage());
            }
        });
    }

    private void loadWorkouts() {
        apiService.getWorkouts().enqueue(new Callback<List<Workout>>() {
            @Override
            public void onResponse(Call<List<Workout>> call, Response<List<Workout>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    tableWorkouts.removeAllViews();
                    tvWorkouts.setText("");
                    workoutTableDataRowIndex = 0;

                    addWorkoutTableHeader();

                    boolean hasWorkouts = false;

                    for (Workout w : response.body()) {
                        if (w.userId == userId) {
                            hasWorkouts = true;
                            addWorkoutTableRow(w);
                        }
                    }

                    if (!hasWorkouts) {
                        tableWorkouts.removeAllViews();
                        tvWorkouts.setText("No workouts found for this user.");
                    }

                } else {
                    tvWorkouts.setText("No workouts found.");
                }
            }

            @Override
            public void onFailure(Call<List<Workout>> call, Throwable t) {
                tvWorkouts.setText("Error: " + t.getMessage());
            }
        });
    }

    private void addWorkoutTableHeader() {
        TableRow headerRow = new TableRow(this);

        headerRow.addView(workoutCell("Date", true));
        headerRow.addView(workoutCell("Notes", true));
        headerRow.addView(workoutCell("Delete", true));

        tableWorkouts.addView(headerRow);
    }

    private void addWorkoutTableRow(Workout workout) {
        TableRow row = new TableRow(this);

        int stripe = workoutTableDataRowIndex++;

        row.addView(workoutCell(formatWorkoutDate(workout.workoutDate), false, stripe));

        String notes = workout.notes;

        if (notes == null || notes.trim().isEmpty()) {
            notes = "-";
        }

        row.addView(workoutCell(notes, false, stripe));

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        TableUiHelper.applyCompactPillButtonStyle(this, deleteButton);

        deleteButton.setOnClickListener(v -> confirmDeleteWorkout(workout.workoutId));

        row.addView(deleteButton, TableUiHelper.compactTableButtonParams());

        tableWorkouts.addView(row);
    }

    private void confirmDeleteWorkout(int workoutId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Workout")
                .setMessage("Are you sure you want to delete this workout?")
                .setPositiveButton("Delete", (dialog, which) -> deleteWorkout(workoutId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWorkout(int workoutId) {
        apiService.deleteWorkout(workoutId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    showPopup("Success", "Workout deleted successfully.");
                    loadWorkouts();
                } else {
                    showPopup("Failed", "Failed to delete workout. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                showPopup("Error", t.getMessage());
            }
        });
    }

    private TextView workoutCell(String text, boolean isHeader) {
        return workoutCell(text, isHeader, 0);
    }

    private TextView workoutCell(String text, boolean isHeader, int stripeIndex) {
        return TableUiHelper.createTableCell(this, text, isHeader, stripeIndex);
    }

    private String formatWorkoutDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "Unknown date";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    Locale.getDefault()
            );

            SimpleDateFormat outputFormat = new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            );

            return outputFormat.format(inputFormat.parse(rawDate));

        } catch (ParseException e) {
            if (rawDate.contains("T")) {
                return rawDate.substring(0, rawDate.indexOf("T"));
            }

            return rawDate;
        }
    }

    private void showPopup(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = selectedYear + "-" +
                            String.format("%02d", selectedMonth + 1) + "-" +
                            String.format("%02d", selectedDay);

                    etWorkoutDate.setText(formattedDate);
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }
}