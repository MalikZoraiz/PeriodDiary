# Period Diary - Android App

Period Diary is a comprehensive and user-friendly Android application designed to help users track their menstrual cycles, predict future periods, and understand their reproductive health. The app offers a secure, cloud-backed experience, ensuring user data is never lost.

## ✨ Core Features

*   **Menstrual Cycle Tracking:** Easily log period start and end dates on an intuitive calendar interface.
*   **Automated Predictions:** The app automatically calculates and displays predictions for future periods, fertile windows, and ovulation days based on user data.
*   **Data Visualization:** A clear and interactive calendar visualizes past, current, and predicted cycle days.
*   **Personalized Dashboard:** A dynamic dashboard provides at-a-glance information, including a countdown to the next period and the current day of the cycle.
*   **Secure Cloud Backup & Restore:** Seamlessly back up and restore all user data (period entries and user name) using Firebase Authentication and Firestore. Data is automatically synced across devices upon login.
*   **Onboarding Flow:** A guided onboarding process helps new users set up their initial data with ease.
*   **Data Management:** Users have full control over their data, with options to edit past entries or permanently delete their account and all associated cloud data.

## 🛠️ Tech Stack & Architecture

*   **Language:** [Kotlin](https://kotlinlang.org/) (100% Kotlin-first)
*   **Architecture:** Follows modern Android app architecture principles.
    *   **UI Layer:** Activities and Fragments using ViewBinding.
    *   **Data Layer:** Repository pattern for abstracting data sources.
*   **Asynchronous Programming:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/flow.html) are used extensively for background tasks and reactive data updates.
*   **Local Database:** [Android Room](https://developer.android.com/training/data-storage/room) for persistent local storage, providing a single source of truth for the UI.
*   **Cloud Backend:** [Firebase](https://firebase.google.com/)
    *   **Authentication:** For secure email/password user registration and login.
    *   **Firestore:** As a NoSQL cloud database for reliable data backup and synchronization.
*   **UI Components:**
    *   [Android Material Components](https://material.io/develop/android) for modern UI elements.
    *   `RecyclerView` for efficient and dynamic calendar displays.
    *   `ViewPager2` for the cycle information carousel on the dashboard.

## 📂 Project Structure

The project is organized into logical packages to promote scalability and maintainability.

```
app/src/main/java/com/nexadev/perioddiary/
│
├── activities/       # Core screens like Dashboard, Settings, etc.
│
├── adapter/          # RecyclerView adapters for calendar and cycle info.
│
├── data/
│   └── database/     # Room database (AppDatabase, DAOs, and @Entity classes).
│
├── onboarding/       # Activities related to the initial user setup.
│
└── ... (other utility classes)
```

## 🧠 Key Logic Areas

To quickly understand the app's core functionality, focus on these files:

*   **`DashboardActivity.kt`**: The central hub of the app. It observes the local database using Kotlin Flow (`observePeriodData()`) and updates the entire UI reactively (`updateUiWithData()`). All cycle calculation and prediction logic resides here.
*   **`LoginActivity.kt`**: Contains the critical `syncDataOnLogin()` function. This function intelligently merges local data (from onboarding) with cloud data from Firestore, ensuring a seamless experience for both new and returning users.
*   **`LastPeriodActivity.kt` / `LogMorePeriodsActivity.kt`**: These files show how initial user data is collected and saved to the local Room database during the onboarding flow.
*   **`EditPeriodActivity.kt`**: Demonstrates how to handle data editing, ensuring that changes are propagated to both the local database and synced with Firebase.
*   **`AppDatabase.kt` / `PeriodEntry.kt`**: The foundation of the local data layer, defining the database schema and structure.

## 🚀 Setup & Installation

To build and run this project, you will need to:

1.  **Clone the repository:**
    ```sh
    git clone <repository-url>
    ```

2.  **Set up Firebase:**
    *   Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    *   Add an Android app to your Firebase project with the package name `com.nexadev.perioddiary`.
    *   Download the `google-services.json` file provided during the setup process.
    *   Place the `google-services.json` file in the `app/` directory of this project.
    *   In the Firebase Console, enable **Authentication** (with the Email/Password sign-in method) and **Firestore**.

3.  **Build and Run:**
    *   Open the project in Android Studio.
    *   Let Gradle sync the dependencies.
    *   Build and run the app on an emulator or a physical device.
