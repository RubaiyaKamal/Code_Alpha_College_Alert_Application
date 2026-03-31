# ProGuard / R8 Rules for College Alert App
# ==========================================
# These rules tell ProGuard/R8 which code to KEEP (not obfuscate/remove)
# during release builds. Required for libraries that use reflection (like Firebase).

# ===== FIREBASE RULES =====

# Keep Firebase model classes (Event.java) from being obfuscated.
# Firebase uses reflection to serialize/deserialize Java objects to/from JSON.
# If class names or field names are changed by ProGuard, Firebase can't map them.
-keep class com.collegealert.app.model.** { *; }

# Keep Firebase Auth classes
-keep class com.google.firebase.auth.** { *; }

# Keep Firebase Database classes
-keep class com.google.firebase.database.** { *; }

# Keep Firebase Messaging classes
-keep class com.google.firebase.messaging.** { *; }

# ===== GENERAL ANDROID RULES =====

# Keep Activity, Service, BroadcastReceiver classes (referenced in Manifest)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# Keep the RecyclerView adapter (uses reflection for ViewHolder)
-keep class androidx.recyclerview.widget.** { *; }

# Keep CardView
-keep class androidx.cardview.widget.** { *; }

# ===== DEBUGGING =====
# Uncomment these lines to keep line numbers in crash reports (useful for debugging)
# -keepattributes SourceFile,LineNumberTable
# -renamesourcefileattribute SourceFile
