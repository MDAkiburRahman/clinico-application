# Clinico Project Documentation

## 1. Overview
Clinico is a comprehensive healthcare application developed for Android. It allows users to browse medical professionals, book appointments, and maintain their health records in one place.

## 2. Architecture
The project follows the **MVVM (Model-View-ViewModel)** architectural pattern:
- **Model (Domain)**: POJO classes representing data entities (e.g., `DoctorsModel`, `AppointmentModel`).
- **View (Activity/Fragment)**: Responsible for UI display and user interaction. All activities extend `BaseActivity` for shared functionality.
- **ViewModel**: Handles data logic and communicates with Firebase.

## 3. Core Components
- **BaseActivity**: Provides global services like Internet connectivity checks, Firebase instances, and Session management.
- **FirebaseHelper**: A centralized utility for accessing Firebase Authentication and Database references.
- **NetworkUtils**: Monitors network status to ensure the app handles offline states gracefully.
- **SessionManager**: Manages local user preferences and session data using SharedPreferences.

## 4. Key Features
- **Authentication**: Email/Password login with "Remember Me" functionality.
- **Home Screen**: Features banners, medical categories, and prioritized lists of top/featured doctors.
- **Doctor Details**: Displays biography, education, languages, and a photo gallery.
- **Booking System**: Interactive date and time selection with real-time availability checking.
- **Favorites**: Personalized list of preferred doctors.
- **Reviews**: User-contributed ratings and feedback for doctors.

## 5. UI/UX Enhancements (Production Ready)
- **Shimmer Loading**: Modern skeleton screens for content loading.
- **Material Dialogs**: Consistent and accessible dialogs for errors and confirmations.
- **Empty States**: Clear messaging when no data is available.
- **Responsive Layouts**: Scrollable containers and flexible UI elements for different screen sizes.
- **Edge-to-Edge**: Full-screen experience with proper system bar handling.

## 6. Setup and Configuration
- **Firebase**: Requires `google-services.json` in the `app` folder.
- **API Keys**: Cloudinary and other 3rd party keys should be placed in `local.properties` or a secure constants file.
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## 7. Development Guidelines
- Always use `ViewBinding` for layout interactions.
- Extend `BaseActivity` for any new Activity.
- Use `FirebaseHelper` for database operations to ensure consistency.
- Implement internet checks (`checkInternet()`) before performing network-heavy operations.
