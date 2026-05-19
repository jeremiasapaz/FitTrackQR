package com.example.fittrackapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.api.ApiClient;
import com.example.fittrackapp.api.ApiService;
import com.example.fittrackapp.models.Exercise;
import com.example.fittrackapp.models.WeeklyPlan;
import com.example.fittrackapp.models.CreateWeeklyPlanRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import android.widget.TableLayout;
import android.widget.TableRow;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeeklyPlansActivity extends AppCompatActivity {

    private int weeklyPlanTableDataRowIndex;

    Spinner spDayOfWeek;
    Button btnSelectExercises, btnAddWeeklyPlan;
    TextView tvSelectedExercises, tvWeeklyPlans;
    TableLayout tableWeeklyPlans;

    ApiService apiService;

    int loggedUserId;

    List<Exercise> exerciseList = new ArrayList<>();
    List<Integer> selectedExercisePositions = new ArrayList<>();

    boolean[] selectedExercises;

    String[] days = {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_plans);

        spDayOfWeek = findViewById(R.id.spDayOfWeek);
        btnSelectExercises = findViewById(R.id.btnSelectExercises);
        btnAddWeeklyPlan = findViewById(R.id.btnAddWeeklyPlan);
        tvSelectedExercises = findViewById(R.id.tvSelectedExercises);
        tvWeeklyPlans = findViewById(R.id.tvWeeklyPlans);
        tableWeeklyPlans = findViewById(R.id.tableWeeklyPlans);

        apiService = ApiClient.getClient().create(ApiService.class);

        loggedUserId = getIntent().getIntExtra("USER_ID", -1);

        if (loggedUserId == -1) {
            loggedUserId = getSharedPreferences("UserSession", MODE_PRIVATE)
                    .getInt("userId", -1);
        }

        if (loggedUserId == -1) {
            showPopup("Error", "Logged-in user not found. Please login again.");
            return;
        }

        setupDayDropdown();
        loadExercises();

        btnSelectExercises.setOnClickListener(v -> showExerciseMultiSelectDialog());
        btnAddWeeklyPlan.setOnClickListener(v -> addWeeklyPlan());
        loadWeeklyPlans();
    }

    private void setupDayDropdown() {
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(
                WeeklyPlansActivity.this,
                android.R.layout.simple_spinner_item,
                days
        );

        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDayOfWeek.setAdapter(dayAdapter);
    }

    private void loadExercises() {
        apiService.getExercises().enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    exerciseList.clear();
                    exerciseList.addAll(response.body());

                    selectedExercises = new boolean[exerciseList.size()];
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

    private void showExerciseMultiSelectDialog() {
        if (exerciseList.isEmpty()) {
            showPopup("Missing Exercise", "No exercises found.");
            return;
        }

        String[] exerciseNames = new String[exerciseList.size()];

        for (int i = 0; i < exerciseList.size(); i++) {
            exerciseNames[i] = exerciseList.get(i).exerciseName;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Exercises")
                .setMultiChoiceItems(exerciseNames, selectedExercises, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!selectedExercisePositions.contains(which)) {
                            selectedExercisePositions.add(which);
                        }
                    } else {
                        selectedExercisePositions.remove(Integer.valueOf(which));
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> updateSelectedExercisesText())
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Clear All", (dialog, which) -> {
                    selectedExercisePositions.clear();

                    for (int i = 0; i < selectedExercises.length; i++) {
                        selectedExercises[i] = false;
                    }

                    tvSelectedExercises.setText("No exercises selected");
                })
                .show();
    }

    private void updateSelectedExercisesText() {
        if (selectedExercisePositions.isEmpty()) {
            tvSelectedExercises.setText("No exercises selected");
            return;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < selectedExercisePositions.size(); i++) {
            int position = selectedExercisePositions.get(i);
            builder.append(exerciseList.get(position).exerciseName);

            if (i < selectedExercisePositions.size() - 1) {
                builder.append(", ");
            }
        }

        tvSelectedExercises.setText(builder.toString());
    }

    private void addWeeklyPlan() {
        if (selectedExercisePositions.isEmpty()) {
            showPopup("Missing Exercises", "Please select at least one exercise.");
            return;
        }

        int selectedDayPosition = spDayOfWeek.getSelectedItemPosition();

        if (selectedDayPosition < 0) {
            showPopup("Missing Day", "Please select a day.");
            return;
        }

        String selectedDay = days[selectedDayPosition];

        addSelectedExercises(selectedDay);
    }

    private void clearSelectedExercises() {
        selectedExercisePositions.clear();

        if (selectedExercises != null) {
            for (int i = 0; i < selectedExercises.length; i++) {
                selectedExercises[i] = false;
            }
        }

        tvSelectedExercises.setText("No exercises selected");
    }

    private void loadWeeklyPlans() {
        apiService.getWeeklyPlans().enqueue(new Callback<List<WeeklyPlan>>() {
            @Override
            public void onResponse(Call<List<WeeklyPlan>> call, Response<List<WeeklyPlan>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    tableWeeklyPlans.removeAllViews();
                    tvWeeklyPlans.setText("");
                    weeklyPlanTableDataRowIndex = 0;

                    addWeeklyPlanTableHeader();

                    boolean hasPlans = false;

                    for (WeeklyPlan plan : response.body()) {
                        if (plan.userId == loggedUserId) {
                            hasPlans = true;
                            addWeeklyPlanTableRow(plan);
                        }
                    }

                    if (!hasPlans) {
                        tableWeeklyPlans.removeAllViews();
                        tvWeeklyPlans.setText("No weekly plans found for this user.");
                    }

                } else {
                    tvWeeklyPlans.setText("No weekly plans found.");
                }
            }

            @Override
            public void onFailure(Call<List<WeeklyPlan>> call, Throwable t) {
                tvWeeklyPlans.setText("Error: " + t.getMessage());
            }
        });
    }

    private String getExerciseNameById(int exerciseId) {
        for (Exercise exercise : exerciseList) {
            if (exercise.exerciseId == exerciseId) {
                return exercise.exerciseName;
            }
        }

        return "Exercise ID: " + exerciseId;
    }

    private String joinExerciseNames(List<String> exercises) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < exercises.size(); i++) {
            builder.append(exercises.get(i));

            if (i < exercises.size() - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    private void showPopup(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void addSelectedExercises(String selectedDay) {
        List<Integer> selectedExerciseIds = new ArrayList<>();

        for (int position : selectedExercisePositions) {
            selectedExerciseIds.add(exerciseList.get(position).exerciseId);
        }

        CreateWeeklyPlanRequest request = new CreateWeeklyPlanRequest(
                loggedUserId,
                selectedDay,
                selectedExerciseIds
        );

        apiService.addWeeklyPlan(request).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    showPopup("Success", "Weekly plan added successfully.");
                    loadWeeklyPlans();
                    clearSelectedExercises();
                } else {
                    showPopup("Failed", "Failed to add weekly plan. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                showPopup("Error", t.getMessage());
            }
        });
    }

    private void addWeeklyPlanTableHeader() {
        TableRow headerRow = new TableRow(this);

        headerRow.addView(weeklyPlanCell("Day", true));
        headerRow.addView(weeklyPlanCell("Exercise", true));
        headerRow.addView(weeklyPlanCell("Delete", true));

        tableWeeklyPlans.addView(headerRow);
    }

    private void addWeeklyPlanTableRow(WeeklyPlan plan) {
        TableRow row = new TableRow(this);

        int stripe = weeklyPlanTableDataRowIndex++;

        row.addView(weeklyPlanCell(plan.dayOfWeek, false, stripe));
        row.addView(weeklyPlanCell(getExerciseNameById(plan.exerciseId), false, stripe));

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        TableUiHelper.applyCompactPillButtonStyle(this, deleteButton);

        deleteButton.setOnClickListener(v -> confirmDeleteWeeklyPlan(plan.planId));

        row.addView(deleteButton, TableUiHelper.compactTableButtonParams());

        tableWeeklyPlans.addView(row);
    }

    private TextView weeklyPlanCell(String text, boolean isHeader) {
        return weeklyPlanCell(text, isHeader, 0);
    }

    private TextView weeklyPlanCell(String text, boolean isHeader, int stripeIndex) {
        return TableUiHelper.createTableCell(this, text, isHeader, stripeIndex);
    }

    private void confirmDeleteWeeklyPlan(int planId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Planned Workout")
                .setMessage("Are you sure you want to delete this planned workout?")
                .setPositiveButton("Delete", (dialog, which) -> deleteWeeklyPlan(planId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWeeklyPlan(int planId) {
        apiService.deleteWeeklyPlan(planId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    showPopup("Success", "Planned workout deleted successfully.");
                    loadWeeklyPlans();
                } else {
                    showPopup("Failed", "Failed to delete planned workout. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                showPopup("Error", t.getMessage());
            }
        });
    }
}