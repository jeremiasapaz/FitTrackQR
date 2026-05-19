package com.example.fittrackapp.models;

import java.util.List;

public class CreateWeeklyPlanRequest {
    public int userId;
    public String dayOfWeek;
    public List<Integer> exerciseIds;

    public CreateWeeklyPlanRequest(int userId, String dayOfWeek, List<Integer> exerciseIds) {
        this.userId = userId;
        this.dayOfWeek = dayOfWeek;
        this.exerciseIds = exerciseIds;
    }
}