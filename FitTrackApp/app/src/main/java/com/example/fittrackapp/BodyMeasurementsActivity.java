package com.example.fittrackapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.api.ApiClient;
import com.example.fittrackapp.api.ApiService;
import com.example.fittrackapp.models.BodyMeasurement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.widget.TableLayout;
import android.widget.TableRow;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BodyMeasurementsActivity extends AppCompatActivity {

    private int measurementTableDataRowIndex;

    EditText etBodyWeight, etChest, etArms, etWaist, etLegs, etMeasurementDate;
    Button btnAddMeasurement, btnUpdateMeasurement;
    TextView tvMeasurements;
    TableLayout tableMeasurements;

    ApiService apiService;

    int loggedUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_measurements);

        etBodyWeight = findViewById(R.id.etBodyWeight);
        etChest = findViewById(R.id.etChest);
        etArms = findViewById(R.id.etArms);
        etWaist = findViewById(R.id.etWaist);
        etLegs = findViewById(R.id.etLegs);
        etMeasurementDate = findViewById(R.id.etMeasurementDate);


        btnAddMeasurement = findViewById(R.id.btnAddMeasurement);
        btnUpdateMeasurement = findViewById(R.id.btnUpdateMeasurement);
        tvMeasurements = findViewById(R.id.tvMeasurements);
        tableMeasurements = findViewById(R.id.tableMeasurements);

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

        etMeasurementDate.setOnClickListener(v -> showDatePicker());

        btnAddMeasurement.setOnClickListener(v -> addMeasurement());
        btnUpdateMeasurement.setOnClickListener(v -> updateMeasurement());
        loadMeasurements();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                BodyMeasurementsActivity.this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = year + "-" +
                            String.format(Locale.getDefault(), "%02d", month + 1) + "-" +
                            String.format(Locale.getDefault(), "%02d", dayOfMonth);

                    etMeasurementDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void addMeasurement() {
        if (!validateMeasurementFields()) {
            return;
        }

        BodyMeasurement measurement = createMeasurementFromFields();

        apiService.addBodyMeasurement(measurement).enqueue(new Callback<BodyMeasurement>() {
            @Override
            public void onResponse(Call<BodyMeasurement> call, Response<BodyMeasurement> response) {
                if (response.isSuccessful()) {
                    showPopup("Success", "Body measurement added successfully.");
                    clearFields();
                    loadMeasurements();
                } else {
                    showPopup("Failed", "Failed to add measurement. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BodyMeasurement> call, Throwable t) {
                showPopup("Error", t.getMessage());
            }
        });
    }

    private void updateMeasurement() {
        if (!validateMeasurementFields()) {
            return;
        }

        apiService.getBodyMeasurementsByUserId(loggedUserId).enqueue(new Callback<List<BodyMeasurement>>() {
            @Override
            public void onResponse(Call<List<BodyMeasurement>> call, Response<List<BodyMeasurement>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<BodyMeasurement> userMeasurements = response.body();

                    if (userMeasurements.isEmpty()) {
                        showPopup("No Record Found", "No body measurement found for this user. Please add one first.");
                        return;
                    }

                    BodyMeasurement latestMeasurement = userMeasurements.get(0);
                    int latestMeasurementId = latestMeasurement.measurementId;

                    BodyMeasurement updatedMeasurement = createMeasurementFromFields();
                    updatedMeasurement.measurementId = latestMeasurementId;

                    apiService.updateBodyMeasurement(latestMeasurementId, updatedMeasurement)
                            .enqueue(new Callback<BodyMeasurement>() {
                                @Override
                                public void onResponse(Call<BodyMeasurement> call, Response<BodyMeasurement> response) {
                                    if (response.isSuccessful()) {
                                        showPopup("Success", "Latest body measurement updated successfully.");
                                        clearFields();
                                        loadMeasurements();
                                    } else {
                                        showPopup("Failed", "Failed to update measurement. Code: " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<BodyMeasurement> call, Throwable t) {
                                    showPopup("Error", t.getMessage());
                                }
                            });

                } else {
                    showPopup("Failed", "Failed to find latest measurement. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<BodyMeasurement>> call, Throwable t) {
                showPopup("Error", t.getMessage());
            }
        });
    }

    private boolean validateMeasurementFields() {
        if (
                etBodyWeight.getText().toString().trim().isEmpty() ||
                        etChest.getText().toString().trim().isEmpty() ||
                        etArms.getText().toString().trim().isEmpty() ||
                        etWaist.getText().toString().trim().isEmpty() ||
                        etLegs.getText().toString().trim().isEmpty() ||
                        etMeasurementDate.getText().toString().trim().isEmpty()
        ) {
            showPopup("Missing Details", "Please fill all measurement fields.");
            return false;
        }

        return true;
    }

    private BodyMeasurement createMeasurementFromFields() {
        return new BodyMeasurement(
                loggedUserId,
                Double.parseDouble(etBodyWeight.getText().toString().trim()),
                Double.parseDouble(etChest.getText().toString().trim()),
                Double.parseDouble(etArms.getText().toString().trim()),
                Double.parseDouble(etWaist.getText().toString().trim()),
                Double.parseDouble(etLegs.getText().toString().trim()),
                etMeasurementDate.getText().toString().trim() + "T00:00:00Z"
        );
    }

    private void loadMeasurements() {
        apiService.getBodyMeasurementsByUserId(loggedUserId).enqueue(new Callback<List<BodyMeasurement>>() {
            @Override
            public void onResponse(Call<List<BodyMeasurement>> call, Response<List<BodyMeasurement>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<BodyMeasurement> measurements = response.body();

                    tableMeasurements.removeAllViews();
                    measurementTableDataRowIndex = 0;

                    if (measurements.isEmpty()) {
                        tvMeasurements.setText("No measurements found for this user.");
                        return;
                    }

                    tvMeasurements.setText("");

                    addTableHeader();

                    for (BodyMeasurement m : measurements) {
                        addMeasurementRow(m);
                    }

                } else {
                    tvMeasurements.setText("No measurements found.");
                }
            }

            @Override
            public void onFailure(Call<List<BodyMeasurement>> call, Throwable t) {
                tvMeasurements.setText("Error: " + t.getMessage());
            }
        });
    }

    private String formatDate(String rawDate) {
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

    private void clearFields() {
        etBodyWeight.setText("");
        etChest.setText("");
        etArms.setText("");
        etWaist.setText("");
        etLegs.setText("");
        etMeasurementDate.setText("");
    }

    private void showPopup(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void addTableHeader() {
        TableRow headerRow = new TableRow(this);

        headerRow.addView(measurementCell("Date", true));
        headerRow.addView(measurementCell("Weight", true));
        headerRow.addView(measurementCell("Chest", true));
        headerRow.addView(measurementCell("Arms", true));
        headerRow.addView(measurementCell("Waist", true));
        headerRow.addView(measurementCell("Legs", true));

        tableMeasurements.addView(headerRow);
    }

    private void addMeasurementRow(BodyMeasurement measurement) {
        TableRow row = new TableRow(this);

        int stripe = measurementTableDataRowIndex++;

        row.addView(measurementCell(formatDate(measurement.measurementDate), false, stripe));
        row.addView(measurementCell(String.valueOf(measurement.bodyWeight), false, stripe));
        row.addView(measurementCell(String.valueOf(measurement.chest), false, stripe));
        row.addView(measurementCell(String.valueOf(measurement.arms), false, stripe));
        row.addView(measurementCell(String.valueOf(measurement.waist), false, stripe));
        row.addView(measurementCell(String.valueOf(measurement.legs), false, stripe));

        tableMeasurements.addView(row);
    }

    private TextView measurementCell(String text, boolean isHeader) {
        return measurementCell(text, isHeader, 0);
    }

    private TextView measurementCell(String text, boolean isHeader, int stripeIndex) {
        return TableUiHelper.createTableCell(this, text, isHeader, stripeIndex);
    }
}