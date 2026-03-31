package com.collegealert.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * MainActivity.java - Home Screen / Dashboard
 *
 * This is the main hub of the app shown after login.
 * It displays category cards that users tap to see events.
 *
 * LAYOUT OVERVIEW:
 * ----------------
 * - Toolbar at top with app name and logout/admin buttons
 * - Welcome text showing the user's role
 * - Four large category cards:
 *   [Seminar]  [Exam]
 *   [Fest]     [Notice]
 * - "View All Events" button
 *
 * NAVIGATION:
 * -----------
 * - Tap any category card → EventListActivity (filtered by that category)
 * - Tap "All Events" → EventListActivity (show all events)
 * - Tap Admin button (admin only) → AdminActivity
 * - Tap Logout → LoginActivity
 */
public class MainActivity extends AppCompatActivity {

    // ===== UI COMPONENTS =====
    private Toolbar toolbar;
    private TextView tvWelcome;
    private CardView cardSeminar, cardExam, cardFest, cardNotice, cardAll;

    // ===== STATE =====
    private String userType;  // "student" or "admin"
    private String userEmail;

    // Permission request code for notification permission
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load user session data from SharedPreferences
        loadUserSession();

        // Initialize UI components
        initViews();

        // Set up the toolbar
        setupToolbar();

        // Set the welcome message
        setupWelcomeMessage();

        // Set up click listeners for category cards
        setupCardClickListeners();

        // Request notification permission on Android 13+ (API 33+)
        requestNotificationPermission();
    }

    /**
     * loadUserSession() - Reads saved login data from SharedPreferences.
     * Called in onCreate() to know what type of user is logged in.
     */
    private void loadUserSession() {
        SharedPreferences prefs = getSharedPreferences(
                SplashActivity.PREFS_NAME, MODE_PRIVATE);

        userType = prefs.getString(SplashActivity.KEY_USER_TYPE, "student");
        userEmail = prefs.getString(SplashActivity.KEY_USER_EMAIL, "User");
    }

    /**
     * initViews() - Find all views by their XML IDs.
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWelcome = findViewById(R.id.tvWelcome);
        cardSeminar = findViewById(R.id.cardSeminar);
        cardExam = findViewById(R.id.cardExam);
        cardFest = findViewById(R.id.cardFest);
        cardNotice = findViewById(R.id.cardNotice);
        cardAll = findViewById(R.id.cardAll);
    }

    /**
     * setupToolbar() - Configures the action bar.
     * setSupportActionBar() replaces the default title bar with our custom Toolbar.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("College Alert");
        }
    }

    /**
     * setupWelcomeMessage() - Shows a personalized greeting.
     * Displays different messages for students vs admins.
     */
    private void setupWelcomeMessage() {
        if ("admin".equals(userType)) {
            tvWelcome.setText("Welcome, Admin!\nManage and post campus alerts below.");
        } else {
            // Extract name from email (part before @)
            String name = userEmail.contains("@")
                    ? userEmail.substring(0, userEmail.indexOf('@'))
                    : userEmail;
            tvWelcome.setText("Welcome, " + name + "!\nTap a category to see alerts.");
        }
    }

    /**
     * setupCardClickListeners() - Attaches click listeners to all category cards.
     * Each card opens EventListActivity with the appropriate filter.
     */
    private void setupCardClickListeners() {

        // SEMINAR card
        cardSeminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventList("Seminar");
            }
        });

        // EXAM card
        cardExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventList("Exam");
            }
        });

        // FEST card
        cardFest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventList("Fest");
            }
        });

        // NOTICE card
        cardNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventList("Notice");
            }
        });

        // ALL EVENTS card
        cardAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventList("All"); // Special "All" filter shows everything
            }
        });
    }

    /**
     * openEventList() - Navigates to EventListActivity with a category filter.
     *
     * Intent Extras allow you to pass data between activities.
     * putExtra(key, value) stores data; getStringExtra(key) retrieves it.
     *
     * @param category The event category to filter by, or "All" for no filter
     */
    private void openEventList(String category) {
        Intent intent = new Intent(MainActivity.this, EventListActivity.class);
        // Pass the category as an "extra" to EventListActivity
        intent.putExtra("category", category);
        startActivity(intent);
    }

    /**
     * onCreateOptionsMenu() - Inflates the toolbar menu.
     * Called automatically by Android when the Activity is created.
     * The menu XML defines what buttons appear in the toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Show the Admin Panel button only if user is an admin
        MenuItem adminItem = menu.findItem(R.id.action_admin);
        if (adminItem != null) {
            adminItem.setVisible("admin".equals(userType));
        }

        return true;
    }

    /**
     * onOptionsItemSelected() - Handles toolbar menu button clicks.
     *
     * @param item The menu item that was clicked
     * @return true if handled, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_admin) {
            // Navigate to Admin panel
            startActivity(new Intent(MainActivity.this, AdminActivity.class));
            return true;

        } else if (id == R.id.action_logout) {
            // Perform logout
            performLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * performLogout() - Clears the login session and returns to LoginActivity.
     *
     * We clear SharedPreferences so the app knows the user is logged out.
     * Firebase Auth sign-out is also called to invalidate the auth token.
     */
    private void performLogout() {
        // Clear saved login session from SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences(
                SplashActivity.PREFS_NAME, MODE_PRIVATE).edit();
        editor.clear(); // Remove all saved preferences
        editor.apply();

        // Sign out from Firebase Authentication
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

        // Unsubscribe from FCM notifications
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .unsubscribeFromTopic("college_alerts");

        // Navigate back to Login screen
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Clear the back stack so user can't press Back to return to MainActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    /**
     * requestNotificationPermission() - Requests POST_NOTIFICATIONS permission.
     *
     * Android 13 (API 33) introduced a runtime permission for notifications.
     * On older Android versions, notifications are allowed by default.
     *
     * Runtime permissions must be requested while the app is running.
     * The user sees a dialog and can Allow or Deny.
     */
    private void requestNotificationPermission() {
        // Only needed on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            // Check if permission is already granted
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Request the permission - shows a system dialog
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    /**
     * onRequestPermissionsResult() - Called after the user responds to permission dialog.
     *
     * @param requestCode  The code we passed to requestPermissions()
     * @param permissions  Array of permissions we requested
     * @param grantResults Array of PERMISSION_GRANTED or PERMISSION_DENIED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Notifications enabled! You'll receive campus alerts.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Notifications denied. You can enable them in Settings.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
