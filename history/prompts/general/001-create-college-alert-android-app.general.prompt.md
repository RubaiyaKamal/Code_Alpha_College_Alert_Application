---
id: "001"
title: "Create College Alert Android Application"
stage: general
date: "2026-03-31"
surface: agent
model: claude-sonnet-4-6
feature: none
branch: main
user: unknown
command: "Create complete Android College Alert Application"
labels: ["android", "java", "firebase", "fcm", "realtime-database", "notifications", "material-design"]
links:
  spec: null
  ticket: null
  adr: null
  pr: null
files:
  - "build.gradle"
  - "settings.gradle"
  - "gradle.properties"
  - "gradle/wrapper/gradle-wrapper.properties"
  - "app/build.gradle"
  - "app/proguard-rules.pro"
  - "app/google-services.json"
  - "app/src/main/AndroidManifest.xml"
  - "app/src/main/java/com/collegealert/app/SplashActivity.java"
  - "app/src/main/java/com/collegealert/app/LoginActivity.java"
  - "app/src/main/java/com/collegealert/app/MainActivity.java"
  - "app/src/main/java/com/collegealert/app/EventListActivity.java"
  - "app/src/main/java/com/collegealert/app/AdminActivity.java"
  - "app/src/main/java/com/collegealert/app/model/Event.java"
  - "app/src/main/java/com/collegealert/app/adapter/EventAdapter.java"
  - "app/src/main/java/com/collegealert/app/service/MyFirebaseMessagingService.java"
  - "app/src/main/res/layout/activity_splash.xml"
  - "app/src/main/res/layout/activity_login.xml"
  - "app/src/main/res/layout/activity_main.xml"
  - "app/src/main/res/layout/activity_event_list.xml"
  - "app/src/main/res/layout/activity_admin.xml"
  - "app/src/main/res/layout/item_event.xml"
  - "app/src/main/res/values/strings.xml"
  - "app/src/main/res/values/colors.xml"
  - "app/src/main/res/values/themes.xml"
  - "app/src/main/res/menu/main_menu.xml"
  - "app/src/main/res/drawable/ic_notification.xml"
  - "app/src/main/res/drawable/badge_background.xml"
  - "app/src/main/res/drawable/input_background.xml"
  - "app/src/main/res/drawable/circle_white_bg.xml"
  - "app/src/main/res/drawable/circle_blue_bg.xml"
  - "app/src/main/res/drawable/circle_red_bg.xml"
  - "app/src/main/res/drawable/circle_green_bg.xml"
  - "app/src/main/res/drawable/circle_orange_bg.xml"
  - "app/src/main/res/xml/backup_rules.xml"
  - "app/src/main/res/xml/data_extraction_rules.xml"
  - ".gitignore"
  - "README.md"
tests: []
---

## Prompt

Create a complete Android College Alert Application project. The project directory is: C:\Users\Lap Zone\CODE_ALPHA\Code_Alpha_College_Alert_Application\

**Objective:** A beginner-friendly Android app that sends notifications about campus events to students.

**Tech Stack:** Android (Java), Firebase Cloud Messaging (FCM) for notifications, Firebase Realtime Database for event storage.

**Features:**
- Students receive push notifications for: Seminars, Exams, Fests, Important Notices
- View list of all campus events/alerts
- Events categorized by type (Seminar, Exam, Fest, Notice)
- Basic admin functionality to add events (can be a separate screen or hardcoded admin credentials)
- Firebase integration for real-time updates

[Full project structure and implementation details as specified by user - 49 files total]

## Response

Created a complete Android College Alert Application with 49 files across all required directories:

**Gradle Configuration (5 files):**
- Project-level build.gradle with Google Services plugin
- App-level build.gradle with Firebase BOM 32.7.2, RecyclerView, CardView, Material Design dependencies
- settings.gradle, gradle.properties, gradle-wrapper.properties

**Java Source Files (7 files):**
- SplashActivity: Entry point with 2.5s splash, auto-login detection via SharedPreferences
- LoginActivity: Tabbed Student/Admin login, Firebase Auth, FCM topic subscription
- MainActivity: Dashboard with 2x2 category grid, notification permission request, toolbar menu
- EventListActivity: Real-time Firebase listener with RecyclerView, category filtering, empty state
- AdminActivity: Form-based event creation with Firebase push(), AlertDialog notification reminder
- Event.java: POJO model with no-arg constructor (Firebase requirement)
- EventAdapter.java: RecyclerView adapter with dynamic color-coding per category (Blue/Red/Green/Orange)
- MyFirebaseMessagingService: Handles both notification and data payloads, creates NotificationChannel for Android 8+

**Layout XML Files (6 files):**
- All activity layouts + item_event.xml for RecyclerView cards
- Material Design with CardView, TabLayout, Spinner, ProgressBar

**Resource Files (15+ files):**
- strings.xml, colors.xml, themes.xml, main_menu.xml
- Shape drawables for badges, circle icons, input fields
- Vector ic_notification.xml for status bar icon
- Adaptive launcher icons for all mipmap densities

**Firebase Integration:**
- google-services.json placeholder with full setup instructions
- Database structure documented
- FCM topic "college_alerts" for broadcast notifications

**Outcome:** All 49 files created. App is ready to build after user replaces google-services.json with their real Firebase credentials and enables Auth + Realtime Database in Firebase Console.
