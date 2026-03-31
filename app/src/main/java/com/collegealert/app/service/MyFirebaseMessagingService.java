package com.collegealert.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.collegealert.app.MainActivity;
import com.collegealert.app.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * MyFirebaseMessagingService.java - FCM Service
 *
 * HOW Firebase Cloud Messaging (FCM) works (for beginners):
 * ---------------------------------------------------------
 * 1. Admin adds an event (or sends a notification via Firebase Console)
 * 2. Firebase sends a "push notification" to all subscribed devices
 * 3. This Service class wakes up IN THE BACKGROUND to handle it
 * 4. We then display the notification to the user
 *
 * This service runs even when the app is NOT open.
 * It's declared in AndroidManifest.xml with MESSAGING_EVENT filter.
 *
 * There are two types of FCM messages:
 * - Notification messages: Firebase handles display automatically (when app is background)
 * - Data messages: Your code handles them (more control, works in foreground too)
 *
 * This service handles BOTH types.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // Tag for Logcat - use Log.d(TAG, "message") to debug
    private static final String TAG = "FCMService";

    // The unique ID for our notification channel (required for Android 8.0+)
    private static final String CHANNEL_ID = "college_alerts_channel";

    // Human-readable name for the notification channel (shown in device settings)
    private static final String CHANNEL_NAME = "College Alerts";

    // Description shown in device notification settings
    private static final String CHANNEL_DESC = "Notifications for college events, exams, seminars, and notices";

    /**
     * onNewToken() - Called when FCM assigns a new registration token.
     *
     * WHAT IS A TOKEN?
     * Each device gets a unique "token" (like an address) from Firebase.
     * When you want to send a notification to one specific device, you use its token.
     * For "broadcast to all" notifications (like ours), you use Topics instead.
     *
     * This method is called:
     * - When the app is installed for the first time
     * - When the token is refreshed (security refresh)
     *
     * @param token The new FCM registration token for this device
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        // Log the token for debugging (don't log tokens in production apps)
        Log.d(TAG, "New FCM Token received: " + token);

        // TODO: If you want to send notifications to SPECIFIC users,
        // save this token to Firebase Database linked to the user's account.
        // For now, we use topic-based messaging (broadcasts to all subscribers).
        sendTokenToServer(token);
    }

    /**
     * sendTokenToServer() - Saves the FCM token to Firebase Database.
     * This is called when a new token is generated.
     *
     * For this beginner app, we just log it.
     * In a production app, you would save it to Firebase Realtime Database.
     *
     * @param token The FCM device token
     */
    private void sendTokenToServer(String token) {
        // Log the token - in production you would save to Firebase Database
        Log.d(TAG, "Saving token to server: " + token);

        // Example of how to save to Firebase (uncomment when ready):
        // FirebaseDatabase.getInstance().getReference("tokens")
        //     .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        //     .setValue(token);
    }

    /**
     * onMessageReceived() - The main method called when an FCM message arrives.
     *
     * This is triggered when:
     * - The app is in the FOREGROUND (always called)
     * - The app is in the BACKGROUND with a DATA message (not notification message)
     *
     * NOTE: If app is in background and message has a "notification" payload,
     * Android shows it automatically WITHOUT calling this method.
     *
     * @param remoteMessage The incoming FCM message containing title, body, data
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // ===== HANDLE NOTIFICATION PAYLOAD =====
        // "Notification" messages have a pre-built notification section
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);

            // Display the notification to the user
            showNotification(title, body);
        }

        // ===== HANDLE DATA PAYLOAD =====
        // "Data" messages carry custom key-value pairs
        // This gives us more control (e.g., pass event category for color-coding)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data payload: " + remoteMessage.getData());

            // Extract custom data fields
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String category = remoteMessage.getData().get("category");

            // If we have a title from data payload, show notification
            if (title != null) {
                // Add category info to notification body if available
                if (category != null && !category.isEmpty()) {
                    body = "[" + category + "] " + (body != null ? body : "");
                }
                showNotification(title, body);
            }
        }
    }

    /**
     * showNotification() - Creates and displays the actual notification.
     *
     * HOW ANDROID NOTIFICATIONS WORK:
     * --------------------------------
     * 1. Create a NotificationChannel (required for Android 8.0+)
     *    - Channel groups notifications by type in device settings
     * 2. Build the notification with NotificationCompat.Builder
     *    - Set icon, title, text, priority, etc.
     * 3. Create a PendingIntent
     *    - What happens when user TAPS the notification (open MainActivity)
     * 4. Show it via NotificationManager.notify()
     *
     * @param title   The notification title (bold text)
     * @param message The notification body (detail text)
     */
    private void showNotification(String title, String message) {
        // ===== STEP 1: Create notification channel (Android 8.0+ requirement) =====
        createNotificationChannel();

        // ===== STEP 2: Set up the tap action (PendingIntent) =====
        // When user taps the notification, open MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        // These flags clear any existing MainActivity instances and create a fresh one
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // PendingIntent wraps the Intent so the system can fire it later (when tapped)
        // FLAG_IMMUTABLE is required for Android 12+ security
        int requestCode = (int) System.currentTimeMillis(); // Unique code
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // ===== STEP 3: Get the default notification sound =====
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // ===== STEP 4: Build the notification =====
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)

                        // Small icon shown in status bar (must be a monochrome PNG)
                        .setSmallIcon(R.drawable.ic_notification)

                        // Title of the notification (displayed in bold)
                        .setContentTitle(title != null ? title : "College Alert")

                        // Body text of the notification
                        .setContentText(message != null ? message : "New update from your college")

                        // Expand text if it's long (BigTextStyle shows full text when expanded)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message != null ? message : "New update from your college"))

                        // Auto-dismiss notification when user taps it
                        .setAutoCancel(true)

                        // Play the default notification sound
                        .setSound(defaultSoundUri)

                        // HIGH priority makes it appear as a "heads-up" notification
                        // (pops up at top of screen even when phone is in use)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                        // What to do when notification is tapped
                        .setContentIntent(pendingIntent);

        // ===== STEP 5: Show the notification =====
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Each notification needs a unique ID
            // Using current time ensures each notification is shown separately
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notificationBuilder.build());

            Log.d(TAG, "Notification displayed with ID: " + notificationId);
        }
    }

    /**
     * createNotificationChannel() - Creates the notification channel.
     *
     * WHY CHANNELS ARE NEEDED (Android 8.0+ / API 26+):
     * Users can control notification behavior PER CHANNEL in Settings.
     * For example, they might want silent exam reminders but loud fest alerts.
     * We use one channel for all college alerts for simplicity.
     *
     * This method is safe to call multiple times - it only creates the channel
     * if it doesn't already exist.
     */
    private void createNotificationChannel() {
        // Channels are only available on Android 8.0 (Oreo) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // IMPORTANCE_HIGH makes notifications make sound and pop up
            // Other options: IMPORTANCE_LOW (silent), IMPORTANCE_DEFAULT (sound only)
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );

            // Set the description that appears in device Settings > Apps > Notifications
            channel.setDescription(CHANNEL_DESC);

            // Enable vibration for this channel
            channel.enableVibration(true);

            // Vibration pattern: [wait 0ms, vibrate 300ms, wait 200ms, vibrate 300ms]
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});

            // Enable notification light (LED) on devices that support it
            channel.enableLights(true);

            // Register the channel with the system
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
            }
        }
    }
}
