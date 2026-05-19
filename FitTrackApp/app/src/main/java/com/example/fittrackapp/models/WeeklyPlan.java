package com.example.fittrackapp.models;

public class WeeklyPlan {
    public int planId;
    public int userId;
    public String dayOfWeek;
    public int exerciseId;

    public WeeklyPlan(int userId, String dayOfWeek, int exerciseId) {
        this.userId = userId;
        this.dayOfWeek = dayOfWeek;
        this.exerciseId = exerciseId;
    }
}