package com.example.fittrackapp;

import android.app.Application;

public class FitTrackApplication extends Application {

    @Override
    public void onCreate() {
        ThemeHelper.applySavedTheme(this);
        super.onCreate();
    }
}
