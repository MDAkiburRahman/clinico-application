package com.example.clinico.Helper;

import android.content.Context;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationHelper {

    public static void updateFCMToken(Context context) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                String token = task.getResult();
                String uid = FirebaseHelper.getUid();
                if (uid != null && token != null) {
                    FirebaseHelper.getUsersRef()
                            .child(uid)
                            .child("fcmToken")
                            .setValue(token);
                }
            });
        } catch (Exception e) {
            Log.e("FCM", "Error getting FCM token: " + e.getMessage());
        }
    }

    public static void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }
}
