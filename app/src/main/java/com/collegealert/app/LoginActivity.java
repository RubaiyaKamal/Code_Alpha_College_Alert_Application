package com.collegealert.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * LoginActivity.java - Login Screen
 *
 * This screen handles two types of login:
 * 1. STUDENT LOGIN: Any email/password (authenticated via Firebase Auth)
 * 2. ADMIN LOGIN: Fixed credentials (admin@college.edu / Admin@123)
 *
 * After successful login:
 * - User type is saved to SharedPreferences
 * - User is subscribed to FCM topic "college_alerts" for notifications
 * - Navigated to MainActivity
 *
 * Firebase Authentication handles:
 * - Creating new accounts (students can register)
 * - Verifying email/password combinations
 * - Secure password storage (Firebase never stores plain-text passwords)
 *
 * HOW THE TAB LAYOUT WORKS:
 * -------------------------
 * We use a TabLayout with two tabs: "Student" and "Admin"
 * When user selects a tab, we show/hide the relevant UI sections.
 */
public class LoginActivity extends AppCompatActivity {

    // ===== ADMIN CREDENTIALS =====
    // In a real app, admin credentials should be verified server-side.
    // For this beginner project, we use hardcoded credentials.
    // NEVER hardcode sensitive credentials in production apps!
    private static final String ADMIN_EMAIL = "admin@college.edu";
    private static final String ADMIN_PASSWORD = "Admin@123";

    // ===== UI COMPONENTS =====
    private TabLayout tabLayout;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private TextView tvForgotPassword, tvAdminHint;

    // ===== FIREBASE =====
    private FirebaseAuth mAuth; // Firebase Authentication instance

    // ===== STATE =====
    private boolean isAdminTab = false; // Track which tab is selected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Authentication
        // FirebaseAuth.getInstance() returns the shared Auth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize all UI components by finding them in the layout
        initViews();

        // Set up tab switching behavior
        setupTabs();

        // Set up button click listeners
        setupClickListeners();
    }

    /**
     * initViews() - Find all UI elements by their XML IDs.
     * Called once in onCreate() to cache view references.
     */
    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvAdminHint = findViewById(R.id.tvAdminHint);
    }

    /**
     * setupTabs() - Configures the Student/Admin tab switching behavior.
     */
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Check which tab was selected by its position
                if (tab.getPosition() == 0) {
                    // Student tab selected
                    isAdminTab = false;
                    btnRegister.setVisibility(View.VISIBLE);
                    tvAdminHint.setVisibility(View.GONE);
                    etEmail.setHint("Student Email");
                    etEmail.setText(""); // Clear previous input
                    etPassword.setText("");
                } else {
                    // Admin tab selected
                    isAdminTab = true;
                    btnRegister.setVisibility(View.GONE); // Admins can't self-register
                    tvAdminHint.setVisibility(View.VISIBLE);
                    etEmail.setHint("Admin Email");
                    etEmail.setText(""); // Clear previous input
                    etPassword.setText("");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed but must be implemented
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed but must be implemented
            }
        });
    }

    /**
     * setupClickListeners() - Attaches click handlers to all buttons.
     */
    private void setupClickListeners() {

        // LOGIN BUTTON
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdminTab) {
                    performAdminLogin();
                } else {
                    performStudentLogin();
                }
            }
        });

        // REGISTER BUTTON (only visible on Student tab)
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performStudentRegistration();
            }
        });

        // FORGOT PASSWORD LINK
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });
    }

    /**
     * performAdminLogin() - Validates admin credentials and logs in.
     *
     * Admin login checks against hardcoded credentials.
     * In production: verify credentials on the server (never client-side).
     */
    private void performAdminLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs are not empty
        if (!validateInputs(email, password)) return;

        // Check admin credentials
        if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
            // Admin credentials match!
            saveLoginSession("admin", email);
            subscribeToNotifications();
            navigateToMain();
            Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
        } else {
            // Wrong credentials
            Toast.makeText(this,
                    "Invalid admin credentials. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * performStudentLogin() - Authenticates student via Firebase Auth.
     *
     * signInWithEmailAndPassword() is an ASYNCHRONOUS operation.
     * It adds a listener to handle success/failure callbacks.
     * The app continues running while Firebase processes the request.
     */
    private void performStudentLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        // Show loading spinner
        showLoading(true);

        // Firebase Authentication: sign in
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Login successful!
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveLoginSession("student", email);
                        subscribeToNotifications();
                        navigateToMain();
                        Toast.makeText(LoginActivity.this,
                                "Welcome back, Student!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Login failed
                        String errorMsg = "Login failed.";
                        if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * performStudentRegistration() - Creates a new student account via Firebase Auth.
     *
     * createUserWithEmailAndPassword() creates a new account.
     * Firebase handles password hashing and secure storage.
     */
    private void performStudentRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        // Password must be at least 6 characters (Firebase requirement)
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        showLoading(true);

        // Firebase Authentication: create new account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Account created successfully!
                        saveLoginSession("student", email);
                        subscribeToNotifications();
                        navigateToMain();
                        Toast.makeText(LoginActivity.this,
                                "Account created! Welcome!", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMsg = "Registration failed.";
                        if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * sendPasswordResetEmail() - Sends a password reset link to the user's email.
     */
    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email address first");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Failed to send reset email. Check the address.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * subscribeToNotifications() - Subscribes this device to the "college_alerts" FCM topic.
     *
     * HOW FCM TOPICS WORK:
     * --------------------
     * Instead of sending to individual device tokens, topics let you
     * broadcast to ALL devices subscribed to that topic.
     *
     * When admin adds an event, you can send to topic "college_alerts"
     * and ALL subscribed student devices receive the notification!
     */
    private void subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("college_alerts")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("FCM", "Subscribed to college_alerts topic");
                    } else {
                        android.util.Log.e("FCM", "Failed to subscribe to topic");
                    }
                });
    }

    /**
     * saveLoginSession() - Saves login info to SharedPreferences.
     *
     * SharedPreferences.Editor is used to write values.
     * .apply() saves asynchronously (non-blocking, recommended for most cases)
     * .commit() saves synchronously (blocks until done, use sparingly)
     *
     * @param userType "student" or "admin"
     * @param email    The user's email address
     */
    private void saveLoginSession(String userType, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(
                SplashActivity.PREFS_NAME, MODE_PRIVATE).edit();

        editor.putBoolean(SplashActivity.KEY_IS_LOGGED_IN, true);
        editor.putString(SplashActivity.KEY_USER_TYPE, userType);
        editor.putString(SplashActivity.KEY_USER_EMAIL, email);
        editor.apply(); // Save asynchronously
    }

    /**
     * navigateToMain() - Goes to MainActivity and clears the back stack.
     *
     * FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK:
     * These flags ensure that pressing "Back" in MainActivity exits the app
     * instead of returning to the login screen.
     */
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * validateInputs() - Checks that email and password fields are not empty.
     *
     * @return true if inputs are valid, false otherwise
     */
    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * showLoading() - Shows/hides the progress spinner and enables/disables buttons.
     * Called before and after async Firebase operations.
     *
     * @param isLoading true to show spinner, false to hide it
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnRegister.setEnabled(true);
        }
    }
}
