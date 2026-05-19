package com.example.fittrackapp.models;

public class WorkoutLog {
    public int logId;
    public int workoutId;
    public int exerciseId;
    public int sets;
    public int reps;
    public double weight;

    public WorkoutLog(int workoutId, int exerciseId, int sets, int reps, double weight) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
    }
}