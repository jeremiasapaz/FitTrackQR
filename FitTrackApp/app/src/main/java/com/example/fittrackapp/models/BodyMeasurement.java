package com.example.fittrackapp.models;

public class BodyMeasurement {
    public int measurementId;
    public int userId;
    public double bodyWeight;
    public double chest;
    public double arms;
    public double waist;
    public double legs;
    public String measurementDate;

    public BodyMeasurement(int userId, double bodyWeight, double chest, double arms, double waist, double legs, String measurementDate) {
        this.userId = userId;
        this.bodyWeight = bodyWeight;
        this.chest = chest;
        this.arms = arms;
        this.waist = waist;
        this.legs = legs;
        this.measurementDate = measurementDate;
    }
}