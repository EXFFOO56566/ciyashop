package com.example.ciyashop.fcm;

import android.content.Context;
import android.util.Log;

import com.example.ciyashop.utils.RequestParamUtils;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class MyNotificationReceivedHandler implements OneSignal.OSRemoteNotificationReceivedHandler {


    @Override
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent osNotificationReceivedEvent) {
        JSONObject data = osNotificationReceivedEvent.getNotification().toJSONObject();
        String customKey;

        if (data != null) {
            //While sending a Push notification from OneSignal dashboard
            // you can send an addtional data named "customkey" and retrieve the value of it and do necessary operation
            customKey = data.optString(RequestParamUtils.customkey, null);
            Log.e("OneSignalExample", "customkey set with value: " + customKey);
        }
    }
}