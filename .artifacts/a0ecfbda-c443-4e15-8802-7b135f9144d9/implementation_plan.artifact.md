# Implementation Plan - Fully Functional Health and Activity Overview

Transform the static "Health Overview" and "Activity Overview" sections in `activity_main.xml` into dynamic components that load real data from Firebase.

## User Review Required

> [!IMPORTANT]
> - I will be introducing two new nodes in your Firebase database: `HealthData` and `ActivityOverview`.
> - I will add logic to `MainActivity` to populate these sections dynamically from Firebase for the logged-in user.
> - I will update the `ActivityChartView` to accept and render dynamic data points instead of hardcoded values.

## Proposed Changes

### 1. Data Layer & Models

#### [NEW] [HealthDataModel.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/Domain/HealthDataModel.java)
- Define structure for Heart Rate, Steps, BP, and Sleep data.

#### [MODIFY] [Constants.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/Utils/Constants.java)
- Add `NODE_HEALTH_DATA = "HealthData"` and `NODE_ACTIVITY_OVERVIEW = "ActivityOverview"`.

#### [MODIFY] [MainRepository.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/Repository/MainRepository.java)
- Add `loadHealthData(String userId)`: Returns LiveData of `HealthDataModel`.
- Add `loadActivityOverview(String userId)`: Returns LiveData of `List<ActivityDataModel>`.

---

### 2. View Model

#### [MODIFY] [MainViewModel.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/ViewModel/MainViewModel.java)
- Expose methods to fetch health and activity data using the repository.

---

### 3. UI Components

#### [MODIFY] [ActivityChartView.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/View/ActivityChartView.java)
- Add a `setData(List<ActivityDataModel> data)` method.
- Update `onDraw` to calculate scaling based on the dynamic data range.
- Call `invalidate()` to trigger a redraw when data is set.

---

### 4. Integration in MainActivity

#### [MODIFY] [MainActivity.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/Activity/MainActivity.java)
- In `initApp()`, start observing `loadHealthData` and `loadActivityOverview`.
- Update the included `item_health_stat` views (Heart Rate, Steps, BP, Sleep) with data from Firebase.
- Pass the activity data list to the `ActivityChartView`.
- Implement a `seedMockHealthData()` utility to ensure the user has initial data to see if the nodes don't exist yet.

## Verification Plan

### Automated Tests
- Run `:app:assembleDebug` to ensure all new bindings and repository methods are valid.

### Manual Verification
- Verify that changing values in the Firebase `HealthData` node reflects instantly on the Home Page.
- Confirm the `ActivityChartView` renders different line segments based on the step counts provided in the `ActivityOverview` node.
- Ensure the progress bar in the "Steps" card updates correctly based on the current/target ratio.
