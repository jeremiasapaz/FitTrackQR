package com.example.fittrackapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.api.ApiClient;
import com.example.fittrackapp.api.ApiService;
import com.example.fittrackapp.models.Exercise;
import com.example.fittrackapp.models.Workout;
import com.example.fittrackapp.models.WorkoutLog;

import java.util.ArrayList;
import java.util.List;
import android.widget.TableLayout;
import android.widget.TableRow;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutLogsActivity extends AppCompatActivity {

    private int workoutLogTableDataRowIndex;

    Spinner spWorkout, spExercise;
    EditText etSets, etReps, etWeight;
    Button btnAddWorkoutLog;
    TextView tvWorkoutLogs;
    TableLayout tableWorkoutLogs;

    ApiService apiService;

    List<Workout> workoutList = new ArrayList<>();
    List<Exercise> exerciseList = new ArrayList<>();

    ArrayAdapter<String> workoutAdapter;
    ArrayAdapter<String> exerciseAdapter;

    int selectedExerciseIdFromQr = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_logs);

        spWorkout = findViewById(R.id.spWorkout);
        spExercise = findViewById(R.id.spExercise);

        etSets = findViewById(R.id.etSets);
        etReps = findViewById(R.id.etReps);
        etWeight = findViewById(R.id.etWeight);

        btnAddWorkoutLog = findViewById(R.id.btnAddWorkoutLog);
        tvWorkoutLogs = findViewById(R.id.tvWorkoutLogs);
        tableWorkoutLogs = findViewById(R.id.tableWorkoutLogs);

        apiService = ApiClient.getClient().create(ApiService.class);
        selectedExerciseIdFromQr = getIntent().getIntExtra("SELECTED_EXERCISE_ID", -1);

        loadWorkouts();
        loadExercises();

        btnAddWorkoutLog.setOnClickListener(v -> addWorkoutLog());
        loadWorkoutLogs();
    }

    private void loadWorkouts() {
        int loggedUserId = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getInt("userId", -1);

        if (loggedUserId == -1) {
            showPopup("Error", "Logged-in user not found. Please login again.");
            return;
        }

        apiService.getWorkouts().enqueue(new Callback<List<Workout>>() {
            @Override
            public void onResponse(Call<List<Workout>> call, Response<List<Workout>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Workout> allWorkouts = response.body();
                    workoutList.clear();

                    for (Workout workout : allWorkouts) {
                        if (workout.userId == loggedUserId) {
                            workoutList.add(workout);
                        }
                    }

                    List<String> workoutNames = new ArrayList<>();

                    for (Workout workout : workoutList) {
                        workoutNames.add(formatWorkoutDate(workout.workoutDate));
                    }

                    if (workoutNames.isEmpty()) {
                        workoutNames.add("No workouts found for this user");
                    }

                    workoutAdapter = new ArrayAdapter<>(
                            WorkoutLogsActivity.this,
                            android.R.layout.simple_spinner_item,
                            workoutNames
                    );

                    workoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spWorkout.setAdapter(workoutAdapter);

                } else {
                    showPopup("Error", "Failed to load workouts.");
                }
            }

            @Override
            public void onFailure(Call<List<Workout>> call, Throwable t) {
                showPopup("Error", "Workout loading failed: " + t.getMessage());
            }
        });
    }

    private void loadExercises() {
        apiService.getExercises().enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    exerciseList.clear();
                    exerciseList.addAll(response.body());

                    List<String> exerciseNames = new ArrayList<>();

                    for (Exercise exercise : exerciseList) {
                        exerciseNames.add(exercise.exerciseName);
                    }

                    if (exerciseNames.isEmpty()) {
                        exerciseNames.add("No exercises found");
                    }

                    exerciseAdapter = new ArrayAdapter<>(
                            WorkoutLogsActivity.this,
                            android.R.layout.simple_spinner_item,
                            exerciseNames
                    );

                    exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spExercise.setAdapter(exerciseAdapter);
                    if (selectedExerciseIdFromQr != -1) {
                        for (int i = 0; i < exerciseList.size(); i++) {
                            if (exerciseList.get(i).exerciseId == selectedExerciseIdFromQr) {
                                spExercise.setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    showPopup("Error", "Failed to load exercises.");
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                showPopup("Error", "Exercise loading failed: " + t.getMessage());
            }
        });
    }

    private void addWorkoutLog() {
        String setsText = etSets.getText().toString().trim();
        String repsText = etReps.getText().toString().trim();
        String weightText = etWeight.getText().toString().trim();

        if (workoutList.isEmpty()) {
            showPopup("Missing Workout", "No workouts found for this logged-in user.");
            return;
        }

        if (exerciseList.isEmpty()) {
            showPopup("Missing Exercise", "No exercises found.");
            return;
        }

        if (setsText.isEmpty() || repsText.isEmpty() || weightText.isEmpty()) {
            showPopup("Missing Details", "Please fill sets, reps and weight.");
            return;
        }

        int selectedWorkoutPosition = spWorkout.getSelectedItemPosition();
        int selectedExercisePosition = spExercise.getSelectedItemPosition();

        if (selectedWorkoutPosition < 0 || selectedExercisePosition < 0) {
            showPopup("Missing Details", "Please select workout and exercise.");
            return;
        }

        Workout selectedWorkout = workoutList.get(selectedWorkoutPosition);
        Exercise selectedExercise = exerciseList.get(selectedExercisePosition);

        WorkoutLog log = new WorkoutLog(
                selectedWorkout.workoutId,
                selectedExercise.exerciseId,
                Integer.parseInt(setsText),
                Integer.parseInt(repsText),
                Double.parseDouble(weightText)
        );

        apiService.addWorkoutLog(log).enqueue(new Callback<WorkoutLog>() {
            @Override
            public void onResponse(Call<WorkoutLog> call, Response<WorkoutLog> response) {
                if (response.isSuccessful()) {
                    showPopup("Success", "Workout log added successfully.");
                    tvWorkoutLogs.setText("Workout log added successfully.");

                    etSets.setText("");
                    etReps.setText("");
                    etWeight.setText("");

                    loadWorkoutLogs();
                } else {
                    showPopup("Failed", "Failed to add workout log. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WorkoutLog> call, Throwable t) {
                showPopup("Error", t.getMessage());
            }
        });
    }

    private void loadWorkoutLogs() {
        apiService.getWorkoutLogs().enqueue(new Callback<List<WorkoutLog>>() {
            @Override
            public void onResponse(Call<List<WorkoutLog>> call, Response<List<WorkoutLog>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    tableWorkoutLogs.removeAllViews();
                    tvWorkoutLogs.setText("");
                    workoutLogTableDataRowIndex = 0;

                    addWorkoutLogTableHeader();

                    boolean hasLogs = false;

                    for (WorkoutLog log : response.body()) {
                        Workout workout = getWorkoutById(log.workoutId);

                        if (workout != null) {
                            hasLogs = true;
                            addWorkoutLogTableRow(log, workout);
                        }
                    }

                    if (!hasLogs) {
                        tableWorkoutLogs.removeAllViews();
                        tvWorkoutLogs.setText("No workout logs found for this user.");
                    }

                } else {
                    tvWorkoutLogs.setText("No workout logs found.");
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutLog>> call, Throwable t) {
                tvWorkoutLogs.setText("Error: " + t.getMessage());
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

    private void addWorkoutLogTableHeader() {
        TableRow headerRow = new TableRow(this);

        headerRow.addView(workoutLogCell("Date", true));
        headerRow.addView(workoutLogCell("Exercise", true));
        headerRow.addView(workoutLogCell("Sets", true));
        headerRow.addView(workoutLogCell("Reps", true));
        headerRow.addView(workoutLogCell("Weight", true));
        headerRow.addView(workoutLogCell("Delete", true));

        tableWorkoutLogs.addView(headerRow);
    }

    private void addWorkoutLogTableRow(WorkoutLog log, Workout workout) {
        TableRow row = new TableRow(this);

        int stripe = workoutLogTableDataRowIndex++;

        row.addView(workoutLogCell(formatWorkoutDate(workout.workoutDate), false, stripe));
        row.addView(workoutLogCell(getExerciseNameById(log.exerciseId), false, stripe));
        row.addView(workoutLogCell(String.valueOf(log.sets), false, stripe));
        row.addView(workoutLogCell(String.valueOf(log.reps), false, stripe));
        row.addView(workoutLogCell(String.valueOf(log.weight), false, stripe));

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        TableUiHelper.applyCompactPillButtonStyle(this, deleteButton);

        deleteButton.setOnClickListener(v -> confirmDeleteWorkoutLog(log.logId));

        row.addView(deleteButton, TableUiHelper.compactTableButtonParams());

        tableWorkoutLogs.addView(row);
    }

    private void confirmDeleteWorkoutLog(int logId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Workout Log")
                .setMessage("Are you sure you want to delete this workout log?")
                .setPositiveButton("Delete", (dialog, which) -> deleteWorkoutLog(logId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWorkoutLog(int logId) {
        apiService.deleteWorkoutLog(logId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    showPopup("Success", "Workout log deleted successfully.");
                    loadWorkoutLogs();
                } else {
                    showPopup("Failed", "Failed to delete workout log. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                showPopup("Error", t.getMessage());
            }
        });
    }

    private TextView workoutLogCell(String text, boolean isHeader) {
        return workoutLogCell(text, isHeader, 0);
    }

    private TextView workoutLogCell(String text, boolean isHeader, int stripeIndex) {
        return TableUiHelper.createTableCell(this, text, isHeader, stripeIndex);
    }

    private Workout getWorkoutById(int workoutId) {
        for (Workout workout : workoutList) {
            if (workout.workoutId == workoutId) {
                return workout;
            }
        }

        return null;
    }

    private String getExerciseNameById(int exerciseId) {
        for (Exercise exercise : exerciseList) {
            if (exercise.exerciseId == exerciseId) {
                return exercise.exerciseName;
            }
        }

        return "Unknown Exercise";
    }
}