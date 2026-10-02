package com.example.clinico.Utils;

import android.util.Patterns;

public class ValidationHelper {

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isStrongPassword(String password) {
        // At least 6 characters as per Phase 1 requirement
        return password != null && password.length() >= 6;
    }

    public static String sanitizeInput(String input) {
        if (input == null) return "";
        // Basic sanitization: remove leading/trailing whitespace and potentially harmful scripts (though less relevant for mobile)
        return input.trim().replaceAll("<[^>]*>", "");
    }
}
