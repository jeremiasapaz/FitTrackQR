package com.example.fittrackapp.models;

public class Workout {
    public int workoutId;
    public int userId;
    public String workoutDate;
    public String notes;

    public Workout(int userId, String workoutDate, String notes) {
        this.userId = userId;
        this.workoutDate = workoutDate;
        this.notes = notes;
    }
}