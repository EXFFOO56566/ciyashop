package com.example.ciyashop.fcm;

/**
 * Created by User on 06-12-2017.
 */

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;


public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN", s);
        sendRegistrationToServer(s);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }
}



