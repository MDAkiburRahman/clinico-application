# Implementation Plan - Daily Medication Tracker Activity

This plan replaces the "Records" bottom navigation item with a dedicated **Daily Medication Tracker** activity, allowing patients to fully manage their medications and adherence.

## User Review Required

> [!NOTE]
> - **Navigation Change**: The "Records" item in the bottom bar will be replaced by "Meds". Patients can still access their medical records via the Sidebar Drawer or the "Medical Records" quick action on the Home screen.
> - **Full Management**: Unlike the Home screen summary, the new **MedicationActivity** will allow patients to **Add**, **Edit**, and **Delete** their medications.

## Proposed Changes

### 1. UI Enhancements

#### [MODIFY] [activity_main.xml](file:///G:/Clinico/app/src/main/res/layout/activity_main.xml)
- Change `navRecords` block to `navMeds`.
- Update icon to `ic_medical_plus` (more pill/medication oriented).
- Update text label to **"Meds"**.

#### [NEW] [activity_medication.xml](file:///G:/Clinico/app/src/main/res/layout/activity_medication.xml)
- A full-screen list of medications.
- Floating Action Button (FAB) to add a new medication.
- Empty state: "No medications added yet."

#### [NEW] [dialog_add_medication.xml](file:///G:/Clinico/app/src/main/res/layout/dialog_add_medication.xml)
- A dialog form to enter:
    - Medicine Name.
    - Dosage (e.g., 500mg).
    - Time of day (Spinner: Morning, Afternoon, Evening, Night).

### 2. Logic Implementation

#### [NEW] [MedicationActivity.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/Activity/MedicationActivity.java)
- Fetch all medications for the current patient from Firebase `Medications/` node.
- Implement the "Add Medication" dialog logic.
- Reuse `MedicationAdapter` for the list, adding "Delete" and "Edit" capabilities.

#### [MODIFY] [MedicationAdapter.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/Adapter/MedicationAdapter.java)
- Add a "Delete" icon to the card.
- Implement a listener for delete actions.

#### [MODIFY] [MainActivity.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/Activity/MainActivity.java)
- Update the bottom navigation listener for the 5th item to launch `MedicationActivity`.
- Update `updateDrawerSelection` to handle the new ID.

## Verification Plan

### Manual Verification
1. **Navigation**: Click the "Meds" button in the bottom bar and verify it opens the full tracker activity.
2. **Add Medication**:
   - Tap the FAB in `MedicationActivity`.
   - Enter a medicine name and dosage.
   - Save and verify it appears in the list.
3. **Delete Medication**: Delete a medication and verify it disappears from both the Activity and the Home Screen summary.
4. **Adherence Sync**: Mark a medication as "Taken" in the activity and verify the state is reflected on the Home Screen.
