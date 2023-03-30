package com.example.ciyashop.fcm;

/**
 * Created by User on 06-12-2017.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.ciyashop.R;
import com.example.ciyashop.activity.HomeActivity;
import com.example.ciyashop.activity.MyOrderActivity;
import com.example.ciyashop.activity.RewardsActivity;
import com.example.ciyashop.customview.pref.Prefs;
import com.example.ciyashop.utils.RequestParamUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    Context context;
    // [START receive_message]

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();

        Log.e("Notification ", data.toString());
        Prefs.putString("notification", data.toString());
        sendNotification(data);

        context = MyFirebaseMessagingService.this;
    }

    private void sendNotification(Map<String, String> data) {
        Bitmap icon;
        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Intent intent = null;
        if (data.size() > 0) {
            if (data.get(RequestParamUtils.not_code) != null) {
                if (Integer.parseInt(data.get(RequestParamUtils.not_code)) == 1) {
                    intent = new Intent(this, RewardsActivity.class);
                } else if (Integer.parseInt(data.get(RequestParamUtils.not_code)) == 2) {
                    intent = new Intent(this, MyOrderActivity.class);
                } else if (Integer.parseInt(data.get(RequestParamUtils.not_code)) == 3) {
                    intent = new Intent(this, HomeActivity.class);
                }
            } else {
                intent = new Intent(this, HomeActivity.class);
            }
        }

        String channelId = getString(R.string.default_notification_channel_id);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        notificationBuilder.setContentTitle(data.get("title"));
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setContentInfo("");
        notificationBuilder.setLargeIcon(icon);
        notificationBuilder.setColor(Color.GRAY);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentText(data.get("message"));
        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        notificationBuilder.setLights(Color.YELLOW, 1000, 300);
        // Since android Oreo notification channel is needed..
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }
}
