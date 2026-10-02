# Implementation Plan: Custom Side Menu Redesign

Redesign the side menu (Navigation Drawer) to match the provided modern healthcare design, featuring a graphical header, user profile section, and updated menu items.

## Proposed Changes

### [Component Name] UI Resources

#### [NEW] [ic_dropdown.xml](file:///G:/Clinico/app/src/main/res/drawable/ic_dropdown.xml)
Vector drawable for the dropdown arrow in the profile section.

#### [MODIFY] [drawer_menu.xml](file:///G:/Clinico/app/src/main/res/menu/drawer_menu.xml)
Update menu items to match the labels and icons in the design:
- Home
- My Appointments
- Find Doctors
- Health Records
- Notifications
- Messages
- Settings
- Log Out (separate group at bottom)

#### [MODIFY] [nav_header_main.xml](file:///G:/Clinico/app/src/main/res/layout/nav_header_main.xml)
Complete redesign of the header:
- **Graphic Section**: snoopy-doctor icon, "Hello, Take care!" text, and description.
- **Profile Section**: Circular user image, Name, "Patient" role, and dropdown arrow.

#### [NEW] [nav_item_bg.xml](file:///G:/Clinico/app/src/main/res/drawable/nav_item_bg.xml)
Selector for menu items to provide the rounded blue background for the selected state.

#### [NEW] [nav_drawer_bg.xml](file:///G:/Clinico/app/src/main/res/drawable/nav_drawer_bg.xml)
A drawable for the `NavigationView` background to provide the large rounded corners on the right side.

### [Component Name] MainActivity Logic

#### [MODIFY] [MainActivity.java](file:///G:/Clinico/app/src/main/java/com/example/clinico/Activity/MainActivity.java)
- Ensure the header user data is correctly bound to the new UI elements.
- Handle any new menu item clicks (Find Doctors, Notifications, etc.).

## User Review Required

> [!IMPORTANT]
> The design features specific icons for "Find Doctors", "Health Records", "Notifications", etc. I will use the most appropriate Material icons available in the project or create standard ones to match the style.

## Verification Plan

### Manual Verification
- Deploy the app and verify the visual alignment of the new header.
- Check that the "Home" item has the blue rounded background by default.
- Verify the navigation drawer has the curved right-side edge as shown in the image.
- Ensure user data (name) is correctly displayed in the profile section of the drawer.
