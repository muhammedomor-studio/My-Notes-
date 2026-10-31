# My Notes — Native Offline Android Application

**My Notes** is a native, offline-first personal notes, daily journal, and task management application designed for Android using Kotlin and Jetpack Compose. Built with privacy and local security at its core, all data is stored securely on the device using Room Database and DataStore preferences with zero network dependencies or analytics.

---

## Key Features

- **Notes Management**:
  - Full CRUD support with formatted text editing.
  - Pin, Favorite, Duplicate, and Archive notes.
  - Categorization, custom tags, and pastel background colors.
  - Instant offline search and multi-criteria sorting (Recently Updated, Recently Created, Alphabetical, Oldest).
  - List and Grid view layouts.
  - Export individual notes as formatted **TXT** or **PDF** documents.

- **Daily Journal & Diary**:
  - Daily reflections with mood tracking (Happy, Excited, Calm, Neutral, Sad, Anxious, Tired, Grateful).
  - Date navigation, calendar picker, and timeline browsing.
  - Optional daily writing notification reminders.
  - Export journal entries as TXT or PDF.

- **To-Do & Task Manager**:
  - Priority levels (Low, Medium, High) with visual indicators.
  - Due dates, time, and overdue alerts.
  - Filter by All, Today, Upcoming, Overdue, and Completed.
  - Visual completion progress bar.
  - Export task lists to TXT.

- **Security & Privacy (PIN Protection)**:
  - 4–6 digit security PIN lock.
  - Multi-round salted cryptographic hashing (PBKDF/SHA-256) with unique per-device salt.
  - Configurable auto-lock timeout upon backgrounding.
  - Complete data privacy: 100% offline, zero trackers, zero cloud connections.

- **Backup & Restore**:
  - Full structured JSON export of all notes, categories, diaries, and tasks.
  - Strict schema validation and confirmation dialogs to prevent data loss.

---

## Project Structure & Architecture

```
MyNotes/
├── .github/
│   └── workflows/
│       └── build-apk.yml       # Automated GitHub Actions APK compilation
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MyNotesApplication.kt
│   │   │   ├── data/
│   │   │   │   ├── backup/        # ExportUtils, BackupManager (JSON/PDF/TXT)
│   │   │   │   ├── local/         # Room Database, Entities, DAOs
│   │   │   │   ├── preferences/   # DataStore preferences
│   │   │   │   └── repository/    # Notes, Journal, Task repositories
│   │   │   ├── notifications/     # NotificationHelper & ReminderReceiver
│   │   │   ├── security/          # SecurityManager (PIN hashing)
│   │   │   ├── navigation/        # MainNavigation & BottomBar
│   │   │   └── ui/
│   │   │       ├── components/    # GlassCard, FormatToolbar, ColorPicker
│   │   │       ├── home/          # HomeDashboardScreen & ViewModel
│   │   │       ├── notes/         # NotesScreen, Editor & ViewModel
│   │   │       ├── journal/       # JournalScreen & ViewModel
│   │   │       ├── tasks/         # TasksScreen & ViewModel
│   │   │       ├── settings/      # SettingsScreen & ViewModel
│   │   │       ├── lock/          # LockScreen & ViewModel
│   │   │       └── theme/         # Color, Theme, Typography
│   │   └── res/                   # Drawables, mipmaps, strings, file_paths
└── build.gradle.kts
```

---

## GitHub Repository Upload Instructions

To push this project to a new GitHub repository:

1. Initialize git in the root folder:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of My Notes Android application"
   ```
2. Create a new repository on [GitHub](https://github.com/new).
3. Set the remote and push to the main branch:
   ```bash
   git branch -M main
   git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPOSITORY>.git
   git push -u origin main
   ```

---

## Automated APK Build with GitHub Actions

The repository includes a ready-to-run GitHub Actions workflow (`.github/workflows/build-apk.yml`) that automatically compiles the debug APK on every push or pull request to the `main` branch.

### How to Download the APK from GitHub:
1. Open your repository on GitHub in your browser.
2. Click on the **Actions** tab at the top.
3. Click on the latest workflow run (e.g., "Build My Notes APK").
4. Scroll down to the **Artifacts** section at the bottom of the summary page.
5. Click **My-Notes-Android-APK** to download the ZIP archive containing `app-debug.apk`.

---

## Installing the APK on an Android Device

1. Extract the downloaded `app-debug.apk` file.
2. Transfer `app-debug.apk` to your Android device via USB, Google Drive, or by downloading it directly on the device.
3. Open the file on your device using a file manager.
4. If prompted, enable **"Install unknown apps"** in your Android device settings for your browser/file manager.
5. Tap **Install** and launch **My Notes**.
