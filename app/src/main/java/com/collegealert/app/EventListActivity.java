package com.collegealert.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.collegealert.app.adapter.EventAdapter;
import com.collegealert.app.model.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * EventListActivity.java - Event List Screen
 *
 * This screen displays a list of campus events filtered by category.
 * It receives the category from MainActivity via Intent extras.
 *
 * HOW FIREBASE REALTIME DATABASE WORKS (for beginners):
 * -----------------------------------------------------
 * Firebase Realtime Database stores data as a JSON tree (like a big nested object).
 * Our database structure looks like this:
 *
 * college-alert-app/
 * └── events/
 *     ├── -ABC123/              ← Auto-generated key
 *     │   ├── title: "Math Exam"
 *     │   ├── description: "..."
 *     │   ├── category: "Exam"
 *     │   ├── dateTime: "2024-03-15 10:00 AM"
 *     │   ├── location: "Hall B"
 *     │   └── timestamp: 1710500400000
 *     └── -DEF456/
 *         └── ...
 *
 * We read data using:
 * 1. addValueEventListener() - Listens for ALL changes in real-time (auto-updates)
 * 2. addListenerForSingleValueEvent() - Reads data ONCE (no auto-update)
 *
 * For this app, we use addValueEventListener() so the list updates
 * automatically when admin adds a new event!
 */
public class EventListActivity extends AppCompatActivity {

    // ===== UI COMPONENTS =====
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView tvEmpty;  // Shown when no events are found

    // ===== ADAPTER & DATA =====
    private EventAdapter eventAdapter;
    private List<Event> eventList;

    // ===== FIREBASE =====
    private DatabaseReference databaseRef;  // Reference to Firebase Database
    private ValueEventListener eventListener;  // Listens for data changes

    // ===== STATE =====
    private String category;  // The category filter passed from MainActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        // Get the category filter from Intent (passed by MainActivity)
        // getStringExtra() retrieves a String that was passed with putExtra()
        category = getIntent().getStringExtra("category");
        if (category == null) category = "All"; // Default to showing all

        // Initialize UI components
        initViews();

        // Set up toolbar with category name as title
        setupToolbar();

        // Set up RecyclerView
        setupRecyclerView();

        // Connect to Firebase and start listening for events
        loadEventsFromFirebase();
    }

    /**
     * initViews() - Find all views by their XML IDs.
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    /**
     * setupToolbar() - Configures the toolbar with back button and category title.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            // Show a back arrow button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Set title based on category
            if ("All".equals(category)) {
                getSupportActionBar().setTitle("All Campus Events");
            } else {
                getSupportActionBar().setTitle(category + " Alerts");
            }
        }
    }

    /**
     * onSupportNavigateUp() - Handles the back arrow press in the toolbar.
     * Called when user presses the "<" back button in the action bar.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go back to previous activity
        return true;
    }

    /**
     * setupRecyclerView() - Configures the RecyclerView with layout manager and adapter.
     *
     * RecyclerView needs two things:
     * 1. LayoutManager - Controls HOW items are arranged (list, grid, etc.)
     * 2. Adapter - Controls WHAT data is shown and HOW each item looks
     *
     * LinearLayoutManager arranges items in a vertical scrolling list.
     */
    private void setupRecyclerView() {
        eventList = new ArrayList<>(); // Start with empty list

        // Create the adapter with empty list (will be filled by Firebase)
        eventAdapter = new EventAdapter(this, eventList);

        // LinearLayoutManager: items stacked vertically (like a standard list)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Connect the adapter to the RecyclerView
        recyclerView.setAdapter(eventAdapter);
    }

    /**
     * loadEventsFromFirebase() - Connects to Firebase and loads events in real-time.
     *
     * DATABASE PATH: "events" node in Firebase Realtime Database
     *
     * QUERY EXPLANATION:
     * - For "All": Read all events, sorted by timestamp (newest first)
     * - For specific category: Filter events where category == selected category
     *
     * orderByChild("timestamp") - Sort results by the "timestamp" field
     * equalTo(value) - Only return items where the field equals this value
     */
    private void loadEventsFromFirebase() {
        // Get a reference to the "events" node in Firebase Database
        // FirebaseDatabase.getInstance() returns the default database instance
        // .getReference("events") points to the "events" branch of the JSON tree
        databaseRef = FirebaseDatabase.getInstance().getReference("events");

        // Build the appropriate query
        Query query;

        if ("All".equals(category)) {
            // Show all events, ordered by timestamp (ascending)
            // We'll reverse the list later to show newest first
            query = databaseRef.orderByChild("timestamp");
        } else {
            // Filter by category using orderByChild + equalTo
            // Firebase requires an index on "category" for this to work efficiently
            // (Add ".indexOn": ["category", "timestamp"] in Firebase Database Rules)
            query = databaseRef.orderByChild("category").equalTo(category);
        }

        // Create a listener that updates the UI whenever data changes in Firebase
        // This enables REAL-TIME updates - no need to refresh!
        eventListener = new ValueEventListener() {

            /**
             * onDataChange() - Called whenever data at the queried location changes.
             * This includes the initial load AND any future additions/modifications.
             *
             * @param snapshot The current state of all data at the queried location
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the current list before adding fresh data
                eventList.clear();

                // snapshot.getChildren() iterates over each child node
                // Each "child" is one event in the database
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    // Convert the Firebase JSON node into an Event Java object
                    // getValue(Event.class) uses our Event model's getters/setters
                    Event event = eventSnapshot.getValue(Event.class);

                    if (event != null) {
                        // Set the event ID from the Firebase-generated key
                        // The key is like "-ABC123DEF456"
                        event.setEventId(eventSnapshot.getKey());
                        eventList.add(event);
                    }
                }

                // Reverse the list so newest events appear at the top
                // (Firebase returns them in ascending timestamp order)
                java.util.Collections.reverse(eventList);

                // Update the adapter with new data
                eventAdapter.updateEvents(eventList);

                // Show empty state message if no events found
                updateEmptyState();
            }

            /**
             * onCancelled() - Called when the database read fails.
             * Reasons: no internet, Firebase rules deny access, etc.
             *
             * @param error The DatabaseError with error code and message
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EventListActivity.this,
                        "Failed to load events: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Show empty state since we have no data
                updateEmptyState();
            }
        };

        // Attach the listener to the query
        // This starts listening - onDataChange() will be called immediately
        // with existing data, then again whenever data changes
        query.addValueEventListener(eventListener);
    }

    /**
     * updateEmptyState() - Shows or hides the "no events" message.
     * Called after every data refresh.
     */
    private void updateEmptyState() {
        if (eventList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No " + ("All".equals(category) ? "" : category + " ")
                    + "events found.\nCheck back later!");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * onDestroy() - Called when the Activity is being destroyed.
     *
     * IMPORTANT: Always remove Firebase listeners when the activity is destroyed!
     * If you don't, the listener keeps running in the background, wasting
     * memory and battery, and can cause crashes.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove the Firebase listener to prevent memory leaks
        if (databaseRef != null && eventListener != null) {
            databaseRef.removeEventListener(eventListener);
        }
    }
}
