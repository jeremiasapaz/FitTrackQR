package com.example.fittrackapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeHelper {

    private static final String PREFS_NAME = "FitTrackPrefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final String MODE_SYSTEM = "system";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";

    private ThemeHelper() {
    }

    public static String getSavedMode(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_THEME_MODE, MODE_SYSTEM);
    }

    public static int modeToNightDelegate(String mode) {
        if (MODE_LIGHT.equals(mode)) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        if (MODE_DARK.equals(mode)) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    public static int indexForMode(String mode) {
        if (MODE_LIGHT.equals(mode)) {
            return 1;
        }
        if (MODE_DARK.equals(mode)) {
            return 2;
        }
        return 0;
    }

    public static String modeForIndex(int index) {
        switch (index) {
            case 1:
                return MODE_LIGHT;
            case 2:
                return MODE_DARK;
            default:
                return MODE_SYSTEM;
        }
    }

    /** Install saved appearance before activities inflate (see {@link FitTrackApplication}). */
    public static void applySavedTheme(Application application) {
        AppCompatDelegate.setDefaultNightMode(modeToNightDelegate(getSavedMode(application)));
    }

    public static void persistAndApply(Activity activity, String mode) {
        SharedPreferences prefs = activity.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String previous = prefs.getString(KEY_THEME_MODE, MODE_SYSTEM);

        prefs.edit().putString(KEY_THEME_MODE, mode).apply();

        int next = modeToNightDelegate(mode);
        int prev = modeToNightDelegate(previous);
        if (prev != next) {
            AppCompatDelegate.setDefaultNightMode(next);
            activity.recreate();
        }
    }
}
