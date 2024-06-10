## SyncPlus Application

### Overview
SyncPlus is a comprehensive time-tracking and project management application designed to offer seamless user experience even in areas with unreliable internet connections. The application is inspired by Toggl and incorporates features like offline mode and reminder notifications to enhance productivity and accuracy in timekeeping.

### Key Features

#### Offline Mode
The offline mode ensures that users can continue to track their time and manage projects without an active internet connection. This is particularly beneficial for users in areas with unreliable internet connections. Here's how it works:

1. **Time Tracking Offline**: Users can start and stop the time tracker even when they are offline. The time logs are stored locally on the device.
2. **Local Data Storage**: All data related to time logs is stored locally while offline.
3. **Syncing Data**: Once the device is back online, the application automatically syncs the locally stored data with Firestore. A toast message informs the user that their data has been successfully synced.
4. **Error Handling and Logging**: Robust error handling ensures that any issues encountered during the offline mode or syncing process are logged for quick resolution.

#### Reminder Notifications
Inspired by Toggl's reminder notifications, SyncPlus includes a notification bell icon that prompts users to track their time and provides essential project information. This feature minimizes the risk of missed entries and ensures accurate timekeeping. Here’s how it works:

1. **Notification Bell Icon**: Located at the top right corner of the landing page, the bell icon serves as the entry point for notifications.
2. **Project Time Left**: Upon clicking the bell icon, a dialog displays the remaining time for each project until the due date, using visually appealing representations.
3. **Daily Goals**: The dialog also shows the minimum and maximum hours of daily goals for each project, helping users to stay on track with their time management.
4. **Notification Sound**: A notification sound plays when the notification page is opened, ensuring that users are immediately aware of any pending reminders.
5. **User Interface**: The notification dialog uses a consistent design scheme, ensuring a seamless user experience.

### Detailed Feature Description

#### Offline Mode

1. **Initialization**: When the user opens the application, the `LogActivity` initializes the necessary handlers and checks for internet connectivity.
2. **Time Tracking**: Users can start and stop the time tracker using the `startButton` and `saveButton`. If offline, the data is saved locally.
3. **Local Storage**: The time logs, including start time, elapsed time, and project ID, are stored locally in the device's shared preferences.
4. **Syncing Data**: When the device reconnects to the internet, a `NetworkReceiver` listens for the connectivity change and triggers the syncing process. The locally stored time logs are then uploaded to Firestore.
5. **User Notification**: A toast message informs the user that the data has been successfully synced to Firestore.

#### Reminder Notifications

1. **Notification Bell Icon**: The bell icon on the landing page acts as an alert for users to check their time logs and project status.
2. **Dialog Display**: When clicked, the `NotificationDialog` displays a list of projects with details about the remaining time and daily goals.
3. **Data Fetching**: The application fetches the project data from Firestore and calculates the remaining time until the project’s due date.
4. **Visual Representation**: The dialog presents this information in a visually appealing manner, ensuring that users can easily understand their project status and goals.
5. **Notification Sound**: A sound plays when the dialog is displayed, grabbing the user’s attention to check the reminders.

### Implementation Files

1. **LogActivity.kt**: Manages the time tracking functionality, including starting, stopping, and saving time logs, and handles offline storage and syncing.
2. **MainActivity.kt**: Handles user authentication and registers the network receiver for syncing data when the device goes back online.
3. **NotificationDialog.kt**: Displays the notification dialog with project details and daily goals.
4. **activity_log.xml**: Layout file for the `LogActivity`, including the chronometer and buttons for starting and stopping time tracking.
5. **activity_main.xml**: Layout file for the main activity, including the bell icon and cards for navigating to different activities.
6. **dialog_notification.xml**: Layout file for the notification dialog, displaying project details and daily goals.
7. **item_project.xml**: Layout file for each project item in the notification dialog.

### Conclusion

The SyncPlus application ensures that users can track their time and manage their projects efficiently, regardless of their internet connectivity. The offline mode guarantees uninterrupted functionality, while the reminder notifications help users stay on top of their tasks and goals. These features are designed to enhance user productivity and ensure accurate timekeeping, following the best practices inspired by Toggl.