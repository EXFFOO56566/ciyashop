package com.example.ciyashop.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivitySplashScreenBinding;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.Notify;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.ShaKeyHelper;
import com.example.ciyashop.utils.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends BaseActivity {

    private static final String TAG = "SplashScreenActivity";
    private ActivitySplashScreenBinding binding;
    ShaKeyHelper keyHelper;
    private String refreshedToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCurrentYear();
        verifyPurchase();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.isComplete()) {
                refreshedToken = task.getResult();
                Constant.DEVICE_TOKEN = refreshedToken;
                Log.e(TAG, "onComplete: " + refreshedToken);
            } else {
                Log.e(TAG, "onFailed: ");
            }
        });

        int systemTime = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0);
        Log.e("System time is ", systemTime + " ");

        if (Constant.DEVICE_TOKEN != null && !Constant.DEVICE_TOKEN.equals("")) {
            if (getPreferences().getString(RequestParamUtils.DEVICE_TOKEN, "").equals("") ||
                    !getPreferences().getString(RequestParamUtils.DEVICE_TOKEN, "").equals(Constant.DEVICE_TOKEN)) {
                addDeviceToken();
            }
        }
        infiniteCall();
        Log.e("token", "Refreshed token: " + refreshedToken);
        Utils.printHashKey(this);
        clearCustomer();
        keyHelper = new ShaKeyHelper();
        keyHelper.get(this, "SHA1");
        keyHelper.get(this, "SHA256");

    }

    public void setCurrentYear(){

        binding.tvSplashText.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if(year != 0){
            binding.tvSplashText.setText(getString(R.string.footer).replace("2021",""+year));
        }


    }



    public void infiniteCall() {
        if (Config.IS_MANAGE_FROM_SERVER) {
            checkIsInfiniteScroll();
        } else {
            SharedPreferences.Editor editor = getPreferences().edit();
            editor.putBoolean(RequestParamUtils.INFINITESCROLL, Config.IS_INFINITE_LAYOUT);
            editor.putBoolean(RequestParamUtils.LOGIN_SHOW_IN_APP_START, Config.IS_LOGIN_SHOW);

            int introvalue = getPreferences().getInt(RequestParamUtils.IS_SLIDER_SHOW, -1);
            if (Config.IS_SLIDER_SHOW) {
                if (introvalue == -1) {
                    editor.putInt(RequestParamUtils.IS_SLIDER_SHOW, 0);
                } else if (introvalue == 1) {
                    introvalue = introvalue + 1;
                    editor.putInt(RequestParamUtils.IS_SLIDER_SHOW, introvalue);
                }
            } else {
                if (introvalue == 0) {
                    editor.putInt(RequestParamUtils.IS_SLIDER_SHOW, -1);
                }
            }
            editor.apply();
            setLocale(Config.LANGUAGE);
            if (mayRequestPermission()) {
                setData();
            }
        }
    }

    public void getNotification() {
        if (getIntent() != null) {
            Intent intent = new Intent(this, HomeActivity.class);
            if (getIntent().getExtras() != null) {
                String noteCode = getIntent().getExtras().getString("not_code");
                if (noteCode != null) {
                    if (Integer.parseInt(noteCode) == 1) {
                        intent = new Intent(this, RewardsActivity.class);
                        intent.putExtra(RequestParamUtils.Splashscreen, true);
                    } else if (Integer.parseInt(noteCode) == 2) {
                        intent = new Intent(this, MyOrderActivity.class);
                        intent.putExtra(RequestParamUtils.Splashscreen, true);
                    } else if (Integer.parseInt(noteCode) == 3) {
                        intent = new Intent(this, HomeActivity.class);
                        intent.putExtra(RequestParamUtils.Splashscreen, true);
                    }
                }
                startActivityIntent(intent);
            } else {
                startActivityIntent(new Intent(SplashScreenActivity.this, HomeActivity.class));
            }
        } else {
            startActivityIntent(new Intent(SplashScreenActivity.this, HomeActivity.class));
        }
    }

    public void startActivityIntent(final Intent intent) {
        new Handler().postDelayed(() -> {
            startActivity(intent);
            finish();
        }, 1000);
    }

    @Override
    public Uri getReferrer() {
        // There is a built in function available from SDK>=22
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return super.getReferrer();
        }

        Intent intent = getIntent();
        Uri referrer = (Uri) intent.getExtras().get("android.intent.extra.REFERRER");
        if (referrer != null) {
            return referrer;
        }

        String referrerName = intent.getStringExtra("android.intent.extra.REFERRER_NAME");
        if (referrerName != null) {
            try {
                Log.e("Exception is ", referrerName);
                return Uri.parse(referrerName);
            } catch (ParseException e) {
                Log.e("Exception is ", e.getMessage());
            }
        }
        return null;
    }

    private boolean mayRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1212);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1212) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setData();
            } else {
                Snackbar.make(findViewById(R.id.crMain), "Permission must be need", Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, v -> {
                            final Intent i = new Intent();
                            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            i.addCategory(Intent.CATEGORY_DEFAULT);
                            i.setData(Uri.parse("package:" + getPackageName()));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            startActivity(i);
                        }).show();
            }
        }
    }

    public void setData() {
        buildGoogleApiClient();

        /*Added For Deep Linking By Nirav Shah 28-08-2018*/

        final Intent intent = getIntent();
//        String action = intent.getAction();
        Uri data = intent.getData();
        Log.e("Data From deeplink", data + " null");

        if (data != null) {
            Log.e(TAG, "setData: " + data);
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnSuccessListener(this, pendingDynamicLinkData -> {
                        //Get dynamic link from result (may be null if no link is found)
                        Uri deepLink;
                        deepLink = pendingDynamicLinkData.getLink();
                        if (deepLink != null) {
                            Log.e("onSuccessDeepLink: ", "" + deepLink);

                            String[] separated = deepLink.toString().split("#");

                            if (separated.length > 1) {
                                String path1 = separated[0];
                                String path2 = separated[1];

                                Log.e("lastPathSegment: ", "" + path2);

                                if (path2 != null && path2.length() > 0) {
                                    getProductDetails(path2);
                                } else {
                                }
                            }
                        } else {
                            Log.e("", "DeepLink No Link ");
                            String data1 = intent.getDataString();
                            String action = intent.getAction();
                            if (Intent.ACTION_VIEW.equals(action) && data1 != null) {

                                String[] separated = data1.split("#");
                                if (separated.length > 1) {
                                    String path1 = separated[0];
                                    String path2 = separated[1];

                                    if (path2 != null && path2.length() > 0) {
                                        getProductDetails(path2);
                                    } else {
                                    }
                                } else {
                                    if (separated.length > 0) {
                                        String path1 = separated[0];
                                        String[] array = path1.split("/");
                                        if (array.length > 0) {
                                            String searchValue = array[array.length - 1];
                                            Intent intent1 = new Intent(SplashScreenActivity.this, CategoryListActivity.class);
                                            intent1.putExtra(RequestParamUtils.SEARCH, searchValue);
                                            startActivity(intent1);
                                            finish();
                                        }
                                    }
                                }
                            } else {
                                homeOrIntroActivity();
                            }
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Log.e("", "getDynamicLink : onFailure" + e.getMessage());
                        Log.e("", "getDynamicLink : onFailure" + e);
                    });

            /*Deeplink Over */
        } else {
            if (!getPreferences().getString(RequestParamUtils.ID, "").equals("")) {
                getNotification();
            } else {
                homeOrIntroActivity();
            }
        }
    }

    public void homeOrIntroActivity() {
        int slider = getPreferences().getInt(RequestParamUtils.IS_SLIDER_SHOW, -1);
        boolean login = getPreferences().getBoolean(RequestParamUtils.LOGIN_SHOW_IN_APP_START, true);
        if (slider == 0 && login) {
            startActivityIntent(new Intent(this, IntroSliderActivity.class));
        } else if (slider == 0) {
            startActivityIntent(new Intent(this, IntroSliderActivity.class));
        } else if (login) {
            if (!getPreferences().getString(RequestParamUtils.ID, "").equals("")) {
                startActivityIntent(new Intent(SplashScreenActivity.this, HomeActivity.class));
            } else {
                Intent intent = new Intent(SplashScreenActivity.this, LogInActivity.class);
                intent.putExtra("is_splash", true);
                startActivityIntent(intent);
            }
        } else {
            startActivityIntent(new Intent(this, HomeActivity.class));
        }
    }

    private void getProductDetails(String lastPathSegment) {
        if (Utils.isInternetConnected(SplashScreenActivity.this)) {
            showProgress("");
            PostApi postApi = new PostApi(SplashScreenActivity.this, RequestParamUtils.getProductDetail, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.INCLUDE, lastPathSegment);
                postApi.callPostApi(new URLS().PRODUCT_URL + (SplashScreenActivity.this).getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(SplashScreenActivity.this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    private void checkIsInfiniteScroll() {
        if (Utils.isInternetConnected(SplashScreenActivity.this)) {
            showProgress("");
            JSONObject jsonObject = new JSONObject();
            PostApi postApi = new PostApi(SplashScreenActivity.this, RequestParamUtils.INFINITESCROLL, this, getlanuage());
            try {
                postApi.callPostApi(new URLS().INFINITE_SCROLL, jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(SplashScreenActivity.this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void addDeviceToken() {
        if (Utils.isInternetConnected(this)) {
            PostApi postApi = new PostApi(SplashScreenActivity.this, RequestParamUtils.addDeviceToken, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.DEVICE_TOKEN, Constant.DEVICE_TOKEN);
                jsonObject.put(RequestParamUtils.DEVICE_TYPE, RequestParamUtils.android);
                postApi.callPostApi(new URLS().ADDNOTIFICATION, jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        Log.e("Splash", "called");
        if (methodName.equals(RequestParamUtils.addDeviceToken)) {
            if (response != null && response.length() > 0) {
                try {
                    Log.e("Response is ", response);
                    getPreferences().edit().putString(RequestParamUtils.DEVICE_TOKEN, Constant.DEVICE_TOKEN).apply();
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
            }
        } else if (methodName.contains("post")) {
            if (response != null && response.length() > 0) {
                try {
                    Notify notifyRider = new Gson().fromJson(
                            response, new TypeToken<Notify>() {
                            }.getType());
                    Log.e("Response ==> ", response + notifyRider.status);
                    if (notifyRider.status == 1) {
//                        getPreferences().edit().putString("notified", "yes").commit();
                        callAPI("yes");
                    } else {
//                        getPreferences().edit().putString("notified", "no").commit();
                        callAPI("no");
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
                dismissProgress();
            }
        } else if (methodName.equals(RequestParamUtils.getProductDetail)) {
            if (response != null && response.length() > 0) {
                try {
                    finish();
                    JSONArray jsonArray = new JSONArray(response);
                    CategoryList categoryListRider = new Gson().fromJson(
                            jsonArray.get(0).toString(), new TypeToken<CategoryList>() {
                            }.getType());
                    Constant.CATEGORYDETAIL = categoryListRider;

                    if (categoryListRider.type.equals(RequestParamUtils.external)) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(categoryListRider.externalUrl));
                        startActivity(browserIntent);
                    } else {
                        Intent intent = new Intent(SplashScreenActivity.this, ProductDetailActivity.class);
                        intent.putExtra(RequestParamUtils.fromdeeplink, true);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
                dismissProgress();
            }
        } else if (methodName.contains(RequestParamUtils.INFINITESCROLL)) {
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("home_layout")) {
                        String value = jsonObject.getString("home_layout");
                        SharedPreferences.Editor editor = getPreferences().edit();
                        editor.putBoolean(RequestParamUtils.INFINITESCROLL, value.contains("scroll"));
                        editor.apply();
                    }

                    if (jsonObject.has("is_login")) {
                        boolean value = jsonObject.getBoolean("is_login");
                        SharedPreferences.Editor editor = getPreferences().edit();
                        editor.putBoolean(RequestParamUtils.LOGIN_SHOW_IN_APP_START, value);
                        editor.apply();
                    }

                    if (jsonObject.has("is_slider")) {
                        boolean value = jsonObject.getBoolean("is_slider");
                        SharedPreferences.Editor editor = getPreferences().edit();
                        int introvalue = getPreferences().getInt(RequestParamUtils.IS_SLIDER_SHOW, -1);
                        if (value) {
                            if (introvalue == -1) {
                                editor.putInt(RequestParamUtils.IS_SLIDER_SHOW, 0);
                            } else if (introvalue == 1) {
                                introvalue = introvalue + 1;
                                editor.putInt(RequestParamUtils.IS_SLIDER_SHOW, introvalue);
                            }
                        } else {
                            if (introvalue == 0) {
                                editor.putInt(RequestParamUtils.IS_SLIDER_SHOW, -1);
                            }
                        }
                        editor.apply();
                    }

                    if (jsonObject.has("site_language") && !jsonObject.getString("site_language").equals("")) {
                        setLocale(jsonObject.getString("site_language"));
                    }

                    if (jsonObject.has("is_rtl")) {
                        if (getPreferences().getString(RequestParamUtils.LANGUAGE, "").equals("")) {
                            Config.IS_RTL = jsonObject.getBoolean("is_rtl");
                            getPreferences().edit().putBoolean(Constant.RTL, Config.IS_RTL).apply();
                        } else {
                            Config.IS_RTL = getPreferences().getBoolean(Constant.RTL, false);
                        }
                    }

                    if (jsonObject.has("is_terawallet_active")) {
                        boolean value = jsonObject.getBoolean("is_terawallet_active");
                        SharedPreferences.Editor editor = getPreferences().edit();
                        editor.putBoolean(RequestParamUtils.IS_WALLET, value);
                        editor.apply();
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
                dismissProgress();
            }

            if (mayRequestPermission()) {
                setData();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgress();
    }
}
