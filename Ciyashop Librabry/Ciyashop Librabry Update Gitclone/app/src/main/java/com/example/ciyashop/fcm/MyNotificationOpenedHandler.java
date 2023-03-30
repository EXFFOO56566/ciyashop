package com.example.ciyashop.fcm;

import android.util.Log;
import android.widget.Toast;

import com.example.ciyashop.utils.MyApplication;
import com.example.ciyashop.utils.RequestParamUtils;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class MyNotificationOpenedHandler implements OneSignal.OSNotificationOpenedHandler {
    // This fires when a notification is opened by tapping on it.

    @Override
    public void notificationOpened(OSNotificationOpenedResult osNotificationOpenedResult) {
        OSNotificationAction.ActionType actionType = osNotificationOpenedResult.getAction().getType();
        JSONObject data = osNotificationOpenedResult.getNotification().getAdditionalData();
        String activityToBeOpened;


        //If we send notification with action buttons we need to specidy the button id's and retrieve it to
        //do the necessary operation.
        if (actionType == OSNotificationAction.ActionType.ActionTaken) {
            Log.e("OneSignalExample", "Button pressed with id: " + osNotificationOpenedResult.getAction().getActionId());
            if (osNotificationOpenedResult.getAction().getActionId().equals(RequestParamUtils.actionOne)) {
                Toast.makeText(MyApplication.getContext(), "ActionOne Button was pressed", Toast.LENGTH_LONG).show();
            } else if (osNotificationOpenedResult.getAction().getActionId().equals(RequestParamUtils.actionTwo)) {
                Toast.makeText(MyApplication.getContext(), "ActionTwo Button was pressed", Toast.LENGTH_LONG).show();
            }
        }
    }
}