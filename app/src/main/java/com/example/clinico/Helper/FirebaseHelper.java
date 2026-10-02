package com.example.clinico.Helper;

import com.example.clinico.Utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseHelper {

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance();
    }

    public static FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance();
    }

    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    public static String getUid() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static DatabaseReference getUsersRef() {
        return getDatabase().getReference(Constants.NODE_USERS);
    }

    public static DatabaseReference getDoctorsRef() {
        return getDatabase().getReference(Constants.NODE_DOCTORS);
    }

    public static DatabaseReference getAppointmentsRef() {
        return getDatabase().getReference(Constants.NODE_APPOINTMENTS);
    }

    public static DatabaseReference getReviewsRef() {
        return getDatabase().getReference(Constants.NODE_REVIEWS);
    }

    public static DatabaseReference getFavoritesRef() {
        return getDatabase().getReference(Constants.NODE_FAVORITES);
    }
}
