package com.collegealert.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SplashActivity.java - Splash Screen
 *
 * This is the FIRST screen users see when they open the app.
 * It displays the app logo/name for 2 seconds, then navigates to:
 * - LoginActivity: if the user is not logged in yet
 * - MainActivity: if the user is already logged in (remembered session)
 *
 * HOW SPLASH SCREENS WORK:
 * ------------------------
 * 1. App launches -> Android opens SplashActivity (declared as LAUNCHER in Manifest)
 * 2. We show a branded screen briefly
 * 3. We check SharedPreferences to see if user is already logged in
 * 4. Navigate to the appropriate next screen
 * 5. Call finish() so user can't press "Back" to return to splash
 *
 * SharedPreferences stores small key-value data locally on the device.
 * It persists even when the app is closed (unlike variables).
 */
public class SplashActivity extends AppCompatActivity {

    // How long to show the splash screen (in milliseconds)
    private static final int SPLASH_DURATION = 2500; // 2.5 seconds

    // SharedPreferences file name - used as a key to access preferences
    public static final String PREFS_NAME = "CollegeAlertPrefs";

    // Key names for SharedPreferences values
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USER_TYPE = "userType";    // "student" or "admin"
    public static final String KEY_USER_EMAIL = "userEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity
        setContentView(R.layout.activity_splash);

        // Use a Handler to delay navigation
        // Handler(Looper.getMainLooper()) ensures this runs on the UI thread
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // This code runs after SPLASH_DURATION milliseconds
                checkLoginAndNavigate();
            }
        }, SPLASH_DURATION);
    }

    /**
     * checkLoginAndNavigate() - Checks if user is logged in and navigates accordingly.
     *
     * SharedPreferences.getBoolean(key, defaultValue):
     * - Reads the stored boolean value for "key"
     * - Returns "defaultValue" if the key doesn't exist (first time launch)
     */
    private void checkLoginAndNavigate() {
        // Open SharedPreferences in MODE_PRIVATE (only this app can access it)
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user has previously logged in
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        Intent nextIntent;

        if (isLoggedIn) {
            // User is already logged in - go directly to the main screen
            nextIntent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // User is not logged in - show the login screen
            nextIntent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        // Start the next activity
        startActivity(nextIntent);

        // finish() removes SplashActivity from the back stack.
        // This means when user presses "Back" from Login/Main, they exit the app
        // instead of returning to the splash screen.
        finish();
    }
}
