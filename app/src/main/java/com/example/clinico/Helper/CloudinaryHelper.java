package com.example.clinico.Helper;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {

    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dbriptsv");
            config.put("api_key", "324233441943851");
            config.put("api_secret", "VqVPJm_Qp89fZWqGiNfBM6zzPbw");
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }

    public interface UploadListener {
        void onSuccess(String url);
        void onError(String error);
    }

    public static void uploadFile(Uri fileUri, UploadListener listener) {
        MediaManager.get().upload(fileUri)
                .option("resource_type", "auto") // Automatically detect if it's an image or PDF/raw
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        listener.onSuccess((String) resultData.get("secure_url"));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        listener.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }
}
