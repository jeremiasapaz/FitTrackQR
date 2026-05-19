package com.example.fittrackapp.api;

import com.example.fittrackapp.models.User;
import com.example.fittrackapp.models.Exercise;
import com.example.fittrackapp.models.Workout;
import com.example.fittrackapp.models.WorkoutLog;
import com.example.fittrackapp.models.WeeklyPlan;
import com.example.fittrackapp.models.BodyMeasurement;
import com.example.fittrackapp.models.LoginResponse;
import com.example.fittrackapp.models.CreateWeeklyPlanRequest;

import retrofit2.Call;
import java.util.List;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.DELETE;

public interface ApiService {

    @POST("api/Auth/register")
    Call<Object> register(@Body User user);

    @POST("api/Auth/login")
    Call<LoginResponse> login(@Body User user);

    @GET("api/Exercises")
    Call<List<Exercise>> getExercises();

    @POST("api/Exercises")
    Call<Exercise> addExercise(@Body Exercise exercise);

    @GET("api/Workouts")
    Call<List<Workout>> getWorkouts();

    @POST("api/Workouts")
    Call<Workout> addWorkout(@Body Workout workout);

    @DELETE("api/Workouts/{id}")
    Call<Object> deleteWorkout(@Path("id") int id);

    @GET("api/WorkoutLogs")
    Call<List<WorkoutLog>> getWorkoutLogs();

    @POST("api/WorkoutLogs")
    Call<WorkoutLog> addWorkoutLog(@Body WorkoutLog workoutLog);

    @DELETE("api/WorkoutLogs/{id}")
    Call<Object> deleteWorkoutLog(@Path("id") int id);

    @GET("api/WeeklyPlans")
    Call<List<WeeklyPlan>> getWeeklyPlans();

    @POST("api/WeeklyPlans")
    Call<Object> addWeeklyPlan(@Body CreateWeeklyPlanRequest request);

    @DELETE("api/WeeklyPlans/{id}")
    Call<Object> deleteWeeklyPlan(@Path("id") int id);

    @GET("api/BodyMeasurements")
    Call<List<BodyMeasurement>> getBodyMeasurements();

    @POST("api/BodyMeasurements")
    Call<BodyMeasurement> addBodyMeasurement(@Body BodyMeasurement measurement);

    @PUT("api/BodyMeasurements/{id}")
    Call<BodyMeasurement> updateBodyMeasurement(
            @Path("id") int id,
            @Body BodyMeasurement bodyMeasurement
    );

    @GET("api/BodyMeasurements/user/{userId}")
    Call<List<BodyMeasurement>> getBodyMeasurementsByUserId(@Path("userId") int userId);

}