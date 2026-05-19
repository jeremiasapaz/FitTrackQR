package com.example.fittrackapp.models;

public class Exercise {
    public int exerciseId;
    public String exerciseName;
    public String muscleGroup;
    public String aliasName;
    public String qrCode;

    public Exercise(String exerciseName, String muscleGroup, String aliasName, String qrCode) {
        this.exerciseName = exerciseName;
        this.muscleGroup = muscleGroup;
        this.aliasName = aliasName;
        this.qrCode = qrCode;
    }
}