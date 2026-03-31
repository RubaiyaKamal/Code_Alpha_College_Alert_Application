package com.collegealert.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.collegealert.app.model.Event;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * AdminActivity.java - Admin Panel
 *
 * This screen allows the admin to add new campus events.
 * It is only accessible when logged in as an admin.
 *
 * FEATURES:
 * ---------
 * - Form to enter event details (title, description, date/time, location)
 * - Spinner (dropdown) to select event category
 * - "Add Event" button saves the event to Firebase Realtime Database
 * - Real-time validation of form inputs
 *
 * HOW ADDING TO FIREBASE WORKS:
 * -----------------------------
 * 1. Create an Event object with the form data
 * 2. Get a reference to the "events" node: databaseRef.child("events")
 * 3. Call .push() to generate a unique ID for the new event
 * 4. Call .setValue(event) to save the Event object as JSON
 * 5. Add a listener to handle success/failure
 *
 * Firebase automatically converts our Event Java object to JSON like:
 * {
 *   "title": "Math Exam",
 *   "description": "Final semester exam",
 *   "category": "Exam",
 *   "dateTime": "2024-03-15 10:00 AM",
 *   "location": "Hall B",
 *   "timestamp": 1710500400000
 * }
 *
 * SENDING NOTIFICATIONS:
 * ----------------------
 * After adding the event, you can send FCM notifications via:
 * 1. Firebase Console → Cloud Messaging → Send message
 * 2. Firebase Admin SDK (server-side)
 * 3. FCM REST API with your server key
 * This app focuses on #1 (manual via Firebase Console).
 */
public class AdminActivity extends AppCompatActivity {

    // ===== UI COMPONENTS =====
    private Toolbar toolbar;
    private EditText etTitle, etDescription, etDateTime, etLocation;
    private Spinner spinnerCategory;
    private Button btnAddEvent;
    private ProgressBar progressBar;

    // ===== FIREBASE =====
    private DatabaseReference databaseRef;

    // ===== DATA =====
    // Available categories for the spinner dropdown
    private String[] categories = {"Seminar", "Exam", "Fest", "Notice"};
    private String selectedCategory = "Seminar"; // Default selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase Realtime Database reference
        // "events" is the top-level node where all events are stored
        databaseRef = FirebaseDatabase.getInstance().getReference("events");

        // Initialize UI
        initViews();
        setupToolbar();
        setupCategorySpinner();
        setupClickListeners();
    }

    /**
     * initViews() - Find all views by their IDs.
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etEventTitle);
        etDescription = findViewById(R.id.etEventDescription);
        etDateTime = findViewById(R.id.etEventDateTime);
        etLocation = findViewById(R.id.etEventLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * setupToolbar() - Configures the action bar with back navigation.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Panel - Add Event");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * setupCategorySpinner() - Populates and configures the category dropdown.
     *
     * Spinner = Dropdown selector widget in Android
     * ArrayAdapter = Adapter that works with a simple String array
     * android.R.layout.simple_spinner_item = Built-in Android layout for each item
     */
    private void setupCategorySpinner() {
        // Create an ArrayAdapter using the categories array
        // android.R.layout.simple_spinner_item is the default spinner item layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        // Set the layout for the dropdown list view
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach adapter to spinner
        spinnerCategory.setAdapter(adapter);

        // Listen for selection changes
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update selectedCategory when user picks from dropdown
                selectedCategory = categories[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "Seminar"; // Default
            }
        });
    }

    /**
     * setupClickListeners() - Sets up the Add Event button listener.
     */
    private void setupClickListeners() {
        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventToFirebase();
            }
        });
    }

    /**
     * addEventToFirebase() - Validates the form and saves the event to Firebase.
     *
     * STEPS:
     * 1. Validate all required fields are filled
     * 2. Create an Event object from the form data
     * 3. Use Firebase push() to generate a unique key
     * 4. Save the event with setValue()
     * 5. Handle success/failure callbacks
     */
    private void addEventToFirebase() {
        // ===== STEP 1: Read and validate form inputs =====
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dateTime = etDateTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Check required fields
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Event title is required");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Event description is required");
            etDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(dateTime)) {
            etDateTime.setError("Date and time is required");
            etDateTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return;
        }

        // ===== STEP 2: Create the Event object =====
        // System.currentTimeMillis() = current time as milliseconds since Jan 1, 1970
        // Used for sorting events chronologically
        long timestamp = System.currentTimeMillis();

        Event event = new Event(
                title,
                description,
                selectedCategory,
                dateTime,
                location,
                timestamp
        );

        // ===== STEP 3: Show loading state =====
        showLoading(true);

        // ===== STEP 4: Save to Firebase =====
        // push() generates a unique child key (like "-Abc123XYZ")
        // This ensures each event has a unique identifier
        // setValue(event) converts the Event object to JSON and saves it
        databaseRef.push()
                .setValue(event)
                .addOnSuccessListener(aVoid -> {
                    // ===== STEP 5a: Handle success =====
                    showLoading(false);
                    Toast.makeText(AdminActivity.this,
                            "Event added successfully! Students will be notified.",
                            Toast.LENGTH_LONG).show();

                    // Clear the form for the next entry
                    clearForm();

                    // Show notification reminder
                    showNotificationReminder(title, selectedCategory);
                })
                .addOnFailureListener(e -> {
                    // ===== STEP 5b: Handle failure =====
                    showLoading(false);
                    Toast.makeText(AdminActivity.this,
                            "Failed to add event: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * showNotificationReminder() - Reminds admin to send FCM notification.
     *
     * Saving to database doesn't automatically trigger FCM notifications.
     * Admin needs to send a notification manually via Firebase Console
     * OR the server should send it automatically (advanced setup).
     *
     * This dialog guides the admin on how to send notifications.
     */
    private void showNotificationReminder(String title, String category) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Event Added!")
                .setMessage("Event '" + title + "' has been added under " + category + ".\n\n"
                        + "To notify students:\n"
                        + "1. Open Firebase Console\n"
                        + "2. Go to Cloud Messaging\n"
                        + "3. Create a new notification\n"
                        + "4. Set topic: college_alerts\n"
                        + "5. Enter the event details\n"
                        + "6. Send!")
                .setPositiveButton("Got it", null)
                .show();
    }

    /**
     * clearForm() - Resets all form fields after successful submission.
     */
    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etDateTime.setText("");
        etLocation.setText("");
        spinnerCategory.setSelection(0); // Reset to first category
    }

    /**
     * showLoading() - Shows/hides the loading spinner.
     *
     * @param isLoading true to show loading state, false to restore normal state
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnAddEvent.setEnabled(false);
            btnAddEvent.setText("Adding Event...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnAddEvent.setEnabled(true);
            btnAddEvent.setText("Add Event");
        }
    }
}
