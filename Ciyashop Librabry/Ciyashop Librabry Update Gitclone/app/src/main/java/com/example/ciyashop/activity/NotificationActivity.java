package com.example.ciyashop.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.NotificationAdapter;
import com.example.ciyashop.databinding.ActivityNotificationBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.databinding.LayoutNotificationPlaceholderBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Notification;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    List<Notification.Datum> list = new ArrayList<>();
    boolean fromDeleteAll = false;
    private NotificationAdapter notificationAdapter;
    private ActivityNotificationBinding binding;
    private LayoutEmptyBinding emptyBinding;
    private LayoutNotificationPlaceholderBinding notificationPlaceholderBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        notificationPlaceholderBinding = LayoutNotificationPlaceholderBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        setClickEvent();
        setToolbarTheme();
        setEmptyColor();
        setScreenLayoutDirection();
        showBackButton();
        settvTitle(getResources().getString(R.string.notification));
        hideSearchNotification();
        getNotification();
        setNotificationAdapter();
    }

    public void setNotificationAdapter() {
        notificationAdapter = new NotificationAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvNotification.setLayoutManager(mLayoutManager);
        binding.rvNotification.setAdapter(notificationAdapter);
        binding.rvNotification.setNestedScrollingEnabled(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemClick(int position, String value, int outerPos) {
//        String userid = getPreferences().getString(RequestParamUtils.ID, "");
        try {
            JSONObject jsonObject = new JSONObject();
            int[] arr = new int[1];
            int str = Integer.parseInt(value);

            arr[0] = str;
            jsonObject.put(RequestParamUtils.push_meta_id, new JSONArray(arr));
            deleteNotification(jsonObject);
            if (outerPos - 1 == 0) {
                showEmpty();
            }
            if (list.get(position).notCode.equals("1")) {
                //Coupon
                Intent intent = new Intent(this, RewardsActivity.class);
                startActivity(intent);
            } else if (list.get(position).notCode.equals("2")) {
                //order
                Intent intent = new Intent(this, MyOrderActivity.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
    }

    public void getNotification() {
        if (Utils.isInternetConnected(this)) {
            //showProgress("");
            if (Config.SHIMMER_VIEW) {
                notificationPlaceholderBinding.shimmerViewContainer.startShimmer();
                notificationPlaceholderBinding.shimmerViewContainer.setVisibility(View.VISIBLE);
            } else {
                notificationPlaceholderBinding.shimmerViewContainer.setVisibility(View.GONE);
                showProgress("");
            }
            PostApi postApi = new PostApi(this, RequestParamUtils.getNotification, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
//                String token = getPreferences().getString(RequestParamUtils.NOTIFICATION_TOKEN, "");
                jsonObject.put(RequestParamUtils.deviceToken, FirebaseMessaging.getInstance().getToken());
                jsonObject.put(RequestParamUtils.deviceType, Constant.DEVICE_TYPE);
                postApi.callPostApi(new URLS().PUSH_NOTIFICATION, jsonObject.toString());
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void deleteNotification(JSONObject jsonObject) {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.deleteNotifications, this, getlanuage());
            postApi.callPostApi(new URLS().DELETE_NOTIFICATIONS, jsonObject.toString());
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.getNotification)) {
            //dismissProgress();
            if (Config.SHIMMER_VIEW) {
                notificationPlaceholderBinding.shimmerViewContainer.stopShimmer();
                notificationPlaceholderBinding.shimmerViewContainer.setVisibility(View.GONE);
            } else {
                dismissProgress();
            }
            if (response != null && response.length() > 0) {
                try {
                    final Notification notificationRider = new Gson().fromJson(response, new TypeToken<Notification>() {
                    }.getType());
                    if (notificationRider.status.equals("success")) {
                        emptyBinding.llEmpty.setVisibility(View.GONE);
                        list.addAll(notificationRider.data);
                        notificationAdapter.addAll(list);
                    } else {
                        showEmpty();
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            } else {
                showEmpty();
            }
        } else if (methodName.equals(RequestParamUtils.deleteNotifications)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("success")) {
                        if (fromDeleteAll) {
                            list.clear();
                            notificationAdapter.notifyDataSetChanged();
                        }
                        fromDeleteAll = false;
                        if (notificationAdapter.getItemCount() == 0) {
                            showEmpty();
                        }
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }
    }

    public void showEmpty() {
        emptyBinding.llEmpty.setVisibility(View.VISIBLE);
        emptyBinding.tvEmptyTitle.setText(R.string.no_notification_yet);
    }

    public void setClickEvent() {
        emptyBinding.tvContinueShopping.setOnClickListener(v -> {
            Intent i = new Intent(NotificationActivity.this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        binding.tvDeleteAll.setOnClickListener(v -> {
            try {
                JSONObject jsonObject = new JSONObject();
                int[] arr = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    int str = Integer.parseInt(list.get(i).pushMetaId);
                    arr[i] = str;
                }
                fromDeleteAll = true;
                jsonObject.put(RequestParamUtils.push_meta_id, new JSONArray(arr));
                deleteNotification(jsonObject);
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        });
    }
}