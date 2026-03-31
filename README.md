
# College Alert App 🔔

An Android application that sends push notifications to students about campus events including Seminars, Exams, Fests, and Important Notices. Built with Java and Firebase.

---

## Features

- **Push Notifications** via Firebase Cloud Messaging (FCM) for real-time alerts
- **Event Categories**: Seminar (Blue), Exam (Red), Fest (Green), Notice (Orange)
- **Real-time Event List** pulled from Firebase Realtime Database
- **Student Login** via Firebase Authentication (Email/Password)
- **Admin Panel** to add new campus events
- **Color-coded Cards** for easy category identification
- **Splash Screen** with auto login detection
- **Material Design UI** with CardView and RecyclerView

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Android (Java) | App development |
| Firebase Authentication | Student/Admin login |
| Firebase Realtime Database | Store and sync events |
| Firebase Cloud Messaging (FCM) | Push notifications |
| RecyclerView + CardView | Event list display |
| Material Design Components | UI framework |

---

## Project Structure

```
app/src/main/
├── java/com/collegealert/app/
│   ├── SplashActivity.java          # Splash screen (entry point)
│   ├── LoginActivity.java           # Student & Admin login
│   ├── MainActivity.java            # Home dashboard with category cards
│   ├── EventListActivity.java       # Scrollable list of events
│   ├── AdminActivity.java           # Admin panel to add events
│   ├── model/
│   │   └── Event.java               # Data model (POJO for Firebase)
│   ├── adapter/
│   │   └── EventAdapter.java        # RecyclerView adapter for event cards
│   └── service/
│       └── MyFirebaseMessagingService.java  # Handles FCM push notifications
│
├── res/
│   ├── layout/
│   │   ├── activity_splash.xml      # Splash screen UI
│   │   ├── activity_login.xml       # Login screen UI
│   │   ├── activity_main.xml        # Main dashboard UI
│   │   ├── activity_event_list.xml  # Event list UI
│   │   ├── activity_admin.xml       # Admin panel UI
│   │   └── item_event.xml           # Single event card UI
│   ├── values/
│   │   ├── strings.xml              # All text strings
│   │   ├── colors.xml               # Color palette
│   │   └── themes.xml               # App themes/styles
│   ├── drawable/
│   │   ├── ic_notification.xml      # Notification bell icon
│   │   ├── badge_background.xml     # Category badge shape
│   │   ├── input_background.xml     # Text field background
│   │   └── circle_*.xml             # Circle backgrounds for icons
│   └── menu/
│       └── main_menu.xml            # Toolbar menu (Admin, Logout)
│
└── AndroidManifest.xml              # App configuration & permissions
```

---

## Setup Instructions

### Step 1: Clone / Download the Project

```bash
git clone <your-repo-url>
cd Code_Alpha_College_Alert_Application
```

### Step 2: Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Name it: `College Alert App`
4. Follow the wizard (Analytics is optional)
5. Click **"Add app"** → select **Android** icon

### Step 3: Register Your Android App on Firebase

1. **Package name**: `com.collegealert.app`
2. **App nickname**: College Alert *(optional)*
3. Click **"Register app"**
4. **Download `google-services.json`**
5. **Replace** the placeholder `app/google-services.json` with the downloaded file

### Step 4: Enable Firebase Services

#### Authentication
1. Firebase Console → **Authentication** → **Sign-in method**
2. Enable **Email/Password** provider
3. Click **Save**

#### Realtime Database
1. Firebase Console → **Realtime Database** → **Create database**
2. Select **"Start in test mode"** for development
3. Set database rules (Firebase Console → Realtime Database → Rules):

```json
{
  "rules": {
    "events": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

#### Cloud Messaging (FCM)
- FCM is automatically enabled when you add Firebase to your project
- No extra setup needed for basic use

### Step 5: Build and Run

1. Open the project in **Android Studio**
2. Wait for Gradle sync to complete
3. Connect an Android device or start an emulator
4. Click **Run** (green play button)

---

## Usage Guide

### For Students

| Action | Steps |
|---|---|
| Login | Open app → Student tab → Enter email & password → LOGIN |
| Register | Open app → Student tab → Enter email & password → CREATE ACCOUNT |
| View Events | After login → Tap any category card |
| View All Events | After login → Tap "View All Campus Events" |
| Logout | Main screen → 3-dot menu → Logout |

### For Admins

| Action | Steps |
|---|---|
| Login | Open app → Admin tab → `admin@college.edu` / `Admin@123` |
| Add Event | After login → 3-dot menu → Admin Panel → Fill form → ADD EVENT |
| Send Notification | Firebase Console → Cloud Messaging → New notification → Topic: `college_alerts` |

### Admin Demo Credentials
```
Email:    admin@college.edu
Password: Admin@123
```

---

## How Push Notifications Work

```
Admin adds event in app
        ↓
Event saved to Firebase Realtime Database
        ↓
Admin sends notification in Firebase Console
(Cloud Messaging → New Notification → Topic: college_alerts)
        ↓
Firebase sends push to all subscribed devices
        ↓
MyFirebaseMessagingService.onMessageReceived() called
        ↓
Notification displayed on student device
```

### Sending a Test Notification

1. Open **Firebase Console**
2. Go to **Cloud Messaging** → **Send your first message**
3. Enter **Notification title**: "New Exam Alert"
4. Enter **Notification text**: "Mathematics exam on March 15"
5. Click **Next** → Under **Target**, select **Topic**
6. Topic name: `college_alerts`
7. Click **Next** → **Now** → **Review** → **Publish**

---

## Firebase Database Structure

```json
{
  "events": {
    "-AutoGeneratedKey1": {
      "title": "Mathematics Final Exam",
      "description": "Covers all chapters from the semester",
      "category": "Exam",
      "dateTime": "2024-03-15 at 10:00 AM",
      "location": "Examination Hall Block B",
      "timestamp": 1710500400000
    },
    "-AutoGeneratedKey2": {
      "title": "Annual Tech Fest",
      "description": "Join us for a day of innovation and fun!",
      "category": "Fest",
      "dateTime": "2024-03-20 at 9:00 AM",
      "location": "College Auditorium",
      "timestamp": 1710936000000
    }
  }
}
```

---

## Color Theme Reference

| Category | Color | Hex Code |
|---|---|---|
| Seminar | Blue | `#1565C0` |
| Exam | Red | `#C62828` |
| Fest | Green | `#2E7D32` |
| Notice | Orange | `#E65100` |
| App Primary | Indigo | `#3F51B5` |

---

## Requirements

- **Android Studio** Arctic Fox (2020.3.1) or newer
- **Android SDK** API 21+ (Android 5.0 Lollipop)
- **Java** 8 or higher
- **Firebase** account (free Spark plan works)
- **Internet connection** (for Firebase features)

---

## Known Limitations & Future Improvements

- Admin credentials are hardcoded (not production-ready)
- No date picker for event creation (manual text input)
- FCM notifications must be sent manually via Firebase Console
- No image upload for events
- No event detail screen (only list view)

### Suggested Future Features
- [ ] Date/Time picker dialog for admin
- [ ] Server-side auto-notification when event is added
- [ ] Event detail screen with full description
- [ ] Search and filter functionality
- [ ] Push notification for specific categories
- [ ] Dark mode support

---

## Troubleshooting

| Problem | Solution |
|---|---|
| Build fails: "google-services.json is missing" | Replace the placeholder `app/google-services.json` with your real Firebase file |
| "Permission denied" on notifications | Go to Settings → Apps → College Alert → Notifications → Allow |
| Events not loading | Check Firebase Database rules allow reads for authenticated users |
| Login fails | Ensure Email/Password auth is enabled in Firebase Console |
| Notifications not received | Ensure you subscribed to `college_alerts` topic (happens on login) |

---

## Project Information

- **Package**: `com.collegealert.app`
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Java
- **Architecture**: Single-module Android app

---

*Built as part of Code Alpha Android Development Internship*
