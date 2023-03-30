package com.example.ciyashop.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.ChangeLanguageItemAdapter;
import com.example.ciyashop.adapter.InfoPageAdapter;
import com.example.ciyashop.adapter.WebPagesAdapter;
import com.example.ciyashop.customview.bounceview.BounceView;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.databinding.ActivityAccountBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.SyncWishList;
import com.example.ciyashop.model.Customer;
import com.example.ciyashop.model.InfoPages;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class AccountActivity extends BaseActivity implements OnResponseListner, OnItemClickListener {

    private ActivityAccountBinding binding;

    private InfoPageAdapter infoPageAdapter;
    private WebPagesAdapter webPageAdapter;
    private Customer customer = new Customer();
    private String customerId = "";
    private Boolean isWallet;
    public static AlertDialog alert;

    ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    ActivityResultLauncher<Intent> galleryActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prepareCameraLauncher();
        prepareGalleryLauncher();
        setClickEvent();
        setToolbarTheme();
        showDownload();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.account));
        hideSearchNotification();
        showBackButton();
        setBottomBar("account", binding.svHome);
        customerId = getPreferences().getString(RequestParamUtils.ID, "");
        isWallet = getPreferences().getBoolean(RequestParamUtils.IS_WALLET, false);
        infoPages();
        if (Config.IS_CATALOG_MODE_OPTION) {
            binding.tvMyOrder.setVisibility(View.GONE);
        } else {
            binding.tvMyOrder.setVisibility(View.VISIBLE);
        }

        binding.swNotification.setChecked(getPreferences().getBoolean(RequestParamUtils.NOTIFICATIONSTATUS, true));
        setToolBarTheme();
        setTheme();
        if (Constant.IS_CURRENCY_SWITCHER_ACTIVE && Constant.CurrencyList.size() > 1) {
            binding.tvCurrancy.setVisibility(View.VISIBLE);
        } else {
            binding.tvCurrancy.setVisibility(View.GONE);
        }
        if (Constant.IS_WPML_ACTIVE && Constant.LANGUAGELIST.size() > 1) {
            binding.tvLanguage.setVisibility(View.VISIBLE);
        } else {
            binding.tvLanguage.setVisibility(View.GONE);
        }

        if (Constant.WEBVIEWPAGES != null && Constant.WEBVIEWPAGES.size() > 0) {
            webPageAdapter = new WebPagesAdapter(this, this);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.rvwebViewPages.setLayoutManager(mLayoutManager);
            binding.rvwebViewPages.setAdapter(webPageAdapter);
            binding.rvwebViewPages.setNestedScrollingEnabled(false);
            webPageAdapter.addAll(Constant.WEBVIEWPAGES);
        }

        binding.swNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                isNotificationSend(1);
            } else {
                isNotificationSend(2);
            }
        });
    }

    private void prepareCameraLauncher() {
        cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result != null && result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Bitmap photo;
                if (data != null) {
                    photo = (Bitmap) data.getExtras().get("data");
                    uploadUserImage(null, photo);
                } else {
                    Toast.makeText(AccountActivity.this, "Error Capturing Image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void prepareGalleryLauncher() {
        galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result != null && result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Uri selectedImage;
                if (data != null) {
                    selectedImage = data.getData();
                    if (selectedImage != null) {
                        uploadUserImage(selectedImage, null);
                    } else {
                        Toast.makeText(AccountActivity.this, "Error Getting Image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AccountActivity.this, "Error Getting Image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showDownload() {
        if (Config.IS_DOWNLOAD_SHOW) {
            binding.llDownload.setVisibility(View.VISIBLE);
        } else {
            binding.llDownload.setVisibility(View.GONE);
        }
    }

    public void setToolBarTheme() {
        try {
            Drawable mDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.account_bg, null);
            if (mDrawable != null) {
                mDrawable.setColorFilter(new
                        PorterDuffColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.OVERLAY));
            }
            if (!Constant.IS_REWARD_POINT_ACTIVE) {
                binding.tvMyPoint.setVisibility(View.GONE);
                binding.RewardPointLine.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("Exception is ", e.getMessage());
        }

        Drawable unwrappedDrawable = binding.ivEdit.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvCustomerName.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvCustomerPhone.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvCustomerEmail.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvBalance.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setTheme() {
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked},
        };

        int[] thumbColors = new int[]{
                Color.GRAY,
                Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)),
        };

        int[] trackColors = new int[]{
                Color.GRAY,
                Color.parseColor(getPreferences().getString(Constant.APP_TRANSPARENT, Constant.PRIMARY_COLOR)),
        };

        DrawableCompat.setTintList(DrawableCompat.wrap(binding.swNotification.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(binding.swNotification.getTrackDrawable()), new ColorStateList(states, trackColors));
        Typeface tf = Typeface.createFromAsset(getAssets(), "font/RobotoCondensed-Light.ttf");
        binding.swNotification.setTypeface(tf, Typeface.BOLD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("OnResume call", "called OnResume");
    }

    public void setCustomerData() {
        if (customer.id == 0) {
            binding.profileImage.setVisibility(View.VISIBLE);
            binding.llProfileInfo.setVisibility(View.GONE);
            binding.ivEdit.setVisibility(View.INVISIBLE);
            Glide.with(getApplicationContext()).load(R.drawable.man)
                    .placeholder(R.drawable.man)
                    .error(R.drawable.man)
                    .into(binding.profileImage);
            binding.tvCustomerName.setText("");
            binding.tvCustomerPhone.setText("");
            binding.tvCustomerEmail.setText("");
            binding.tvLogIn.setText(R.string.login);
        } else {
            if (customer.pgsProfileImage == null) {
                binding.profileImage.setVisibility(View.VISIBLE);
            } else {
                binding.profileImage.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext()).load(customer.pgsProfileImage).placeholder(R.drawable.man)
                        .error(R.drawable.man).into(binding.profileImage);
            }
            binding.ivEdit.setVisibility(View.VISIBLE);
            binding.llProfileInfo.setVisibility(View.VISIBLE);

            if (!customer.firstName.isEmpty() || !customer.lastName.isEmpty()) {
                Log.e("TAG", "setCustomerData:firstName ");
                binding.tvCustomerName.setVisibility(View.VISIBLE);
                binding.tvCustomerName.setText(String.format("%s %s", customer.firstName, customer.lastName));
            } else {
                binding.tvCustomerName.setVisibility(View.GONE);
            }

            if (!customer.billing.phone.isEmpty()) {
                binding.tvCustomerPhone.setVisibility(View.VISIBLE);
                binding.tvCustomerPhone.setText(customer.billing.phone);
            } else {
                binding.tvCustomerPhone.setVisibility(View.GONE);
            }

            try {
                JSONObject jsonObject = new JSONObject(getPreferences().getString(RequestParamUtils.CUSTOMER, ""));
                if (String.valueOf(jsonObject).equals("")) {
                    binding.tvCustomerPhone.setText("");
                } else {
                    JSONArray jsonArray = jsonObject.getJSONArray(RequestParamUtils.metaData);
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jb = jsonArray.getJSONObject(i);
                        if (jb.getString(RequestParamUtils.key).equalsIgnoreCase(RequestParamUtils.mobile)) {
                            //mobile
                            binding.tvCustomerPhone.setText(jb.getString(RequestParamUtils.value));
                            binding.tvCustomerPhone.setVisibility(View.VISIBLE);
                        }else{
                            binding.tvCustomerPhone.setVisibility(View.GONE);

                        }
                    }
                }
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }

            if (!customer.email.isEmpty()) {
                binding.tvCustomerEmail.setVisibility(View.VISIBLE);
                binding.tvCustomerEmail.setText(customer.email);
            } else {
                binding.tvCustomerEmail.setVisibility(View.GONE);
            }
            binding.tvLogIn.setText(R.string.sign_out);
        }
    }

    public void showLanguageDialog() {
        TextView title = new TextViewRegular(this);
        title.setText(getString(R.string.language));
        title.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        title.setPadding(10, 25, 10, 25);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ChangeLanguageItemAdapter mAdapter;
        builder.setCustomTitle(title);
        final View alertView = getLayoutInflater().inflate(R.layout.language_dialog, null, false);

        RecyclerView rvDisplayitems = alertView.findViewById(R.id.rvDisplayitems);
        mAdapter = new ChangeLanguageItemAdapter(AccountActivity.this, Constant.LANGUAGELIST, AccountActivity.this);
        rvDisplayitems.setHasFixedSize(true);
        rvDisplayitems.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvDisplayitems.setLayoutManager(mLayoutManager);
        rvDisplayitems.setAdapter(mAdapter);
        builder.setView(alertView);
        alert = builder.create();
        alert.show();
        BounceView.addAnimTo(alert);        //Call before showing the dialog
    }

    private void selectImage() {
        final CharSequence[] items = {getString(R.string.take_photo), getString(R.string.choose_from_library),
                getString(R.string.cancel)};

        TextView title = new TextViewRegular(this);
        title.setText(R.string.add_photo);
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 25, 10, 25);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                AccountActivity.this);
        builder.setCustomTitle(title);

        // builder.setTitle("Add Photo!");
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals(getString(R.string.take_photo))) {
                //Camera
                if (mayRequestPermission()) {
                    captureImageIntent();
                }
            } else if (items[item].equals(getString(R.string.choose_from_library))) {
                //Gallery
                pickImageIntent();
            } else if (items[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void captureImageIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraActivityResultLauncher.launch(cameraIntent);
    }

    private void pickImageIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(pickPhoto);
    }

    private boolean mayRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1212);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1212) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImageIntent();
            } else {
                Snackbar.make(binding.tvCustomerName, R.string.permission_need, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, v -> {
                            final Intent i = new Intent();
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

    public void setLogin() {
        Intent intent = new Intent(AccountActivity.this, LogInActivity.class);
        startActivity(intent);
    }

    public void setLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.do_you_really_want_to_signout));
        builder.setTitle(getResources().getString(R.string.sign_out));

        builder.setCancelable(false);
        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.setPositiveButton(getResources().getString(R.string.sure), (dialog, which) -> {
            Toast.makeText(AccountActivity.this, R.string.log_out_success,
                    Toast.LENGTH_LONG).show();
            SharedPreferences.Editor pre = getPreferences().edit();
            pre.putString(RequestParamUtils.CUSTOMER, "");
            pre.putString(RequestParamUtils.ID, "");
            binding.llWallet.setVisibility(View.GONE);
            binding.tvBalance.setVisibility(View.GONE);
            pre.apply();
            LoginManager.getInstance().logOut();
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if (accessToken != null) {
                LoginManager.getInstance().logOut();
            }
            customer = new Customer();
            checkLoggedin();

            if (getPreferences().getBoolean(RequestParamUtils.LOGIN_SHOW_IN_APP_START, true)) {
                Intent intent = new Intent(AccountActivity.this, SplashScreenActivity.class);
                intent.putExtra("is_splash", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

        BounceView.addAnimTo(alert);        //Call before showing the dialog

        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }



    private String refreshedToken;

    public void isNotificationSend(int status) {
        if (Constant.DEVICE_TOKEN == null || Constant.DEVICE_TOKEN.equals("")) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.isComplete()) {
                    refreshedToken = task.getResult();
                    Constant.DEVICE_TOKEN = refreshedToken;
                }
            });
        }

        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.isNotificationSend, this, getPreferences().getString(RequestParamUtils.LANGUAGE, ""));
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.deviceToken, Constant.DEVICE_TOKEN);
                jsonObject.put(RequestParamUtils.deviceType, "2");
                jsonObject.put(RequestParamUtils.status, "" + status);
                postApi.callPostApi(new URLS().NOTIFICATIONSTATUS, jsonObject.toString());
            } catch (JSONException e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void customerAccount() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.customer, this, getPreferences().getString(RequestParamUtils.LANGUAGE, ""));
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.user_id, customerId);
                postApi.callPostApi(new URLS().CUSTOMER, jsonObject.toString());
            } catch (JSONException e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void infoPages() {
        if (Utils.isInternetConnected(this)) {
//            if (Constant.INFO_PAGE_DATA.equals("")) {
            showProgress("");
            JSONObject jsonObject = new JSONObject();
            try {
                if (!customerId.isEmpty()) {
                    jsonObject.put(RequestParamUtils.user_id, customerId);
                } else {
                    jsonObject.put(RequestParamUtils.user_id, "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PostApi postApi = new PostApi(this, RequestParamUtils.infoPages, this, getlanuage());
            postApi.callPostApi(new URLS().INFO_PAGES, jsonObject.toString());
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void uploadUserImage(Uri image, Bitmap bitmap) {
        PostApi postApi = new PostApi(this, RequestParamUtils.updateUserImage, this, getlanuage());
        try {
            Glide.with(getApplicationContext()).load(image)
                    .placeholder(R.drawable.man)
                    .error(R.drawable.man)
                    .into(binding.profileImage);
            if (bitmap == null) {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();

            String encoded = Base64.encodeToString(b, Base64.DEFAULT);

            JSONObject jsonObject = new JSONObject();
            JSONObject object = new JSONObject();

            object.put(RequestParamUtils.data, encoded);
            object.put(RequestParamUtils.name, "image.jpg");

            jsonObject.put(RequestParamUtils.userImage, object);
            jsonObject.put(RequestParamUtils.user_id, customerId);
            postApi.callPostApi(new URLS().UPDATE_USER_IMAGE, jsonObject.toString());
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        switch (methodName) {
            case RequestParamUtils.customer:
                if (response != null && response.length() > 0) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        String status = jsonObj.getString("status");
                        if (status.equals("error")) {
                            String msg = jsonObj.getString("message");
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        customer = new Gson().fromJson(
                                response, new TypeToken<Customer>() {
                                }.getType());
                        SharedPreferences.Editor pre = getPreferences().edit();
                        pre.putString(RequestParamUtils.CUSTOMER, response);
                        pre.apply();
                        setCustomerData();
                    }
                }
                dismissProgress();
                new SyncWishList(AccountActivity.this).syncWishList(getPreferences().getString(RequestParamUtils.ID, ""), false);

                break;
            case RequestParamUtils.infoPages:
                dismissProgress();
                if (response != null && response.length() > 0) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        String status = jsonObj.getString("status");
                        String balance = jsonObj.getString("balance") + " " + Constant.CURRENCYSYMBOL;
                        binding.tvBalance.setText(balance);
                        if (isWallet) {
                            if (customerId == null || customerId.equals("")) {
                                binding.tvBalance.setVisibility(View.GONE);
                                binding.llWallet.setVisibility(View.GONE);
                            } else {
                                binding.tvBalance.setVisibility(View.VISIBLE);
                                binding.llWallet.setVisibility(View.VISIBLE);
                            }
                        } else {
                            binding.tvBalance.setVisibility(View.GONE);
                            binding.llWallet.setVisibility(View.GONE);
                        }
                        if (status.equals("success")) {
                            //get data and show it
                            Constant.INFO_PAGE_DATA = response;
                            setInfoPages();
                        } else {
                            String msg = jsonObj.getString("message");
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }


                    } catch (Exception e) {
                        Log.e("error", e.getMessage());
                    }
                }
                checkLoggedin();

                break;
            case RequestParamUtils.updateUserImage:
                dismissProgress();
                if (response != null && response.length() > 0) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        String status = jsonObj.getString("status");
                        if (status.equals("success")) {
                            customer = new Gson().fromJson(
                                    response, new TypeToken<Customer>() {
                                    }.getType());

                            SharedPreferences.Editor pre = getPreferences().edit();
                            pre.putString(RequestParamUtils.CUSTOMER, response);
                            pre.apply();
                            setCustomerData();
                        }
                    } catch (Exception e) {
                        Log.e("error", e.getMessage());
                    }
                }
                break;
            case RequestParamUtils.isNotificationSend:
                dismissProgress();
                if (response != null && response.length() > 0) {
                    Log.e("Response is ", response + "");
                    try {
                        JSONObject jsonObj = new JSONObject(response);

                        String status = jsonObj.getString("status");
                        if (status.equals("success")) {
                            SharedPreferences.Editor pre = getPreferences().edit();
                            pre.putBoolean(RequestParamUtils.NOTIFICATIONSTATUS, binding.swNotification.isChecked());
                            pre.apply();
                        }
                    } catch (Exception e) {
                        Log.e("error", e.getMessage());
                    }
                }
                break;
            case RequestParamUtils.IsUserExists:


                dismissProgress();
            if (response != null && response.length() > 0) {
                try {
//                        {
//                            "is_user_exists": "true"
//                        }
//
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("is_user_exists");
                    Log.e("TAG", "onResponse:+++++ "+""+status );
                    if (!status.equals("true")) {
                        setLogoutFornonExistUserDialog();
                        //   Toast.makeText(this, "No Exist", Toast.LENGTH_LONG).show();

                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                    Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
                }
            }

            }



    }

    public void setInfoPages() {
        final InfoPages infoPageRider = new Gson().fromJson(
                Constant.INFO_PAGE_DATA, new TypeToken<InfoPages>() {
                }.getType());
        infoPageAdapter = new InfoPageAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvInfoPages.setLayoutManager(mLayoutManager);
        binding.rvInfoPages.setAdapter(infoPageAdapter);
        binding.rvInfoPages.setNestedScrollingEnabled(false);
        infoPageAdapter.addAll(infoPageRider.data);
    }

    public void checkLoggedin() {
        customerId = getPreferences().getString(RequestParamUtils.ID, "");
        String cust = getPreferences().getString(RequestParamUtils.CUSTOMER, "");
        if (cust.equals("")) {
            if (!customerId.isEmpty()) {
                customerAccount();
            }
        } else {
            AccessToken.setCurrentAccessToken(null);
            customer = new Gson().fromJson(
                    cust, new TypeToken<Customer>() {
                    }.getType());
        }
        setCustomerData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkLoggedin();
        infoPages();
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
        alert.dismiss();
        cleardatabasedata();
        getPreferences().edit().putBoolean(RequestParamUtils.iSSITELANGUAGECALLED, false).apply();
        getPreferences().edit().putString(RequestParamUtils.DEFAULTLANGUAGE, "").apply();
        setLocaleByLanguageChange(getPreferences().getString(RequestParamUtils.LANGUAGE, ""));
    }

    private void cleardatabasedata() {
        DatabaseHelper databaseHelper = new DatabaseHelper(AccountActivity.this);
        databaseHelper.clearCart();
        setCount();
        databaseHelper.clearRecentItem();
        databaseHelper.clearWhishlist();
        databaseHelper.clearSearch();
    }

    public void setClickEvent() {
        binding.tvDownload.setOnClickListener(v -> {
            Intent intent;
            if (customerId != null && !customerId.equals("")) {
                intent = new Intent(this, DownloadActivity.class);
            } else {
                intent = new Intent(this, LogInActivity.class);
            }
            startActivity(intent);
        });

        binding.tvRateUs.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
            }
        });

        binding.tvCurrancy.setOnClickListener(v -> {
            List<String> listItems = new ArrayList<>();
            for (int i = 0; i < Constant.CurrencyList.size(); i++) {
                String Name = Constant.CurrencyList.get(i);
                try {
                    JSONObject obj = new JSONObject(Name);
                    String htmlText = "<html><font color='#8E8E8E'>" + " " + obj.get(RequestParamUtils.SYMBOL) + "</font></html>";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        listItems.add(obj.get(RequestParamUtils.NAME) + " (" + (Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)) + " )");
                    } else {
                        listItems.add(obj.get(RequestParamUtils.NAME) + " (" + (Html.fromHtml(htmlText)) + " )");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            final CharSequence[] charSequenceItems = listItems.toArray(new CharSequence[listItems.size()]);

            TextView title = new TextViewRegular(this);
            title.setText(getString(R.string.currency));
            title.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            title.setPadding(10, 25, 10, 25);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.WHITE);
            title.setTextSize(22);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCustomTitle(title);
            builder.setItems(charSequenceItems, (dialog, item) -> {
                Constant.IS_CURRENCY_SET = true;
                SharedPreferences.Editor pre = getPreferences().edit();

                for (int i = 0; i < Constant.CurrencyList.size(); i++) {
                    if (i == item) {
                        String Name = Constant.CurrencyList.get(i);
                        try {
                            JSONObject obj = new JSONObject(Name);
                            String htmlText = obj.get(RequestParamUtils.NAME).toString();
                            pre.putString(RequestParamUtils.CurrencyText, "/?currency=" + htmlText);
                            DatabaseHelper databaseHelper = new DatabaseHelper(AccountActivity.this);
                            databaseHelper.clearCart();
                            databaseHelper.clearRecentItem();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setCount();
                pre.apply();
            });
            AlertDialog alert = builder.create();
            BounceView.addAnimTo(alert);        //Call before showing the dialog
            alert.show();
        });

        binding.tvLanguage.setOnClickListener(v -> showLanguageDialog());

        binding.profileImage.setOnClickListener(v -> {
            if (!customerId.equals("")) {
                selectImage();
            }
        });

        binding.tvAddress.setOnClickListener(v -> {
            if (customerId.equals("")) {
                setLogin();
            } else {
                Intent intent = new Intent(AccountActivity.this, MyAddressActivity.class);
                startActivity(intent);
            }
        });

        binding.tvMyPoint.setOnClickListener(v -> {
            if (customerId.equals("")) {
                setLogin();
            } else {
                Intent intent = new Intent(AccountActivity.this, MyPointActivity.class);
                intent.putExtra(RequestParamUtils.USER_ID, customerId);
                startActivity(intent);
            }
        });

        binding.tvAccountSetting.setOnClickListener(v -> {
            if (customerId.equals("")) {
                setLogin();
            } else {
                Intent intent = new Intent(AccountActivity.this, AccountSettingActivity.class);
                startActivity(intent);
            }
        });

        binding.tvLogIn.setOnClickListener(v -> {
            if (binding.tvLogIn.getText().toString().equals(getResources().getString(R.string.login))) {
                setLogin();
            } else {
                setLogoutDialog();
            }
        });

        binding.tvClearHistory.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.this_permentantly_clear_you_history));
            builder.setTitle(getResources().getString(R.string.clear_history));
            builder.setCancelable(false);
            builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());

            builder.setPositiveButton(getResources().getString(R.string.claer), (dialog, which) -> {
                DatabaseHelper databaseHelper = new DatabaseHelper(AccountActivity.this);
                databaseHelper.clearCart();
                databaseHelper.clearRecentItem();
                databaseHelper.clearWhishlist();
                databaseHelper.clearSearch();
                getPreferences().edit().putString(RequestParamUtils.LANGUAGE, "").apply();
                getPreferences().edit().putBoolean(RequestParamUtils.iSSITELANGUAGECALLED, false).apply();
                getPreferences().edit().putString(RequestParamUtils.DEFAULTLANGUAGE, "").apply();
                getPreferences().edit().putBoolean(Constant.RTL, false).apply();

                Config.IS_RTL = false;

                Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
            AlertDialog alert = builder.create();
            alert.show();
            BounceView.addAnimTo(alert);        //Call before showing the dialog

            Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        });

        binding.tvMyOrder.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, MyOrderActivity.class);
            startActivity(intent);
        });

        binding.tvAboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, AboutUsActivity.class);
            startActivity(intent);
        });

        binding.llWallet.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, WalletTransactionActivity.class);
            startActivity(intent);
        });

        binding.tvContactUs.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ContactUsActivity.class);
            startActivity(intent);
        });

        binding.tvMyRewars.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, RewardsActivity.class);
            startActivity(intent);
        });
    }
}