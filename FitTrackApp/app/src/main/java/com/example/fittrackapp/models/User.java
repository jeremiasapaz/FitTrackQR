package com.example.fittrackapp.models;

public class User {

    public int userId;
    public String fullName;
    public String email;
    public String passwordHash;

    public User(String fullName, String email, String passwordHash) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}