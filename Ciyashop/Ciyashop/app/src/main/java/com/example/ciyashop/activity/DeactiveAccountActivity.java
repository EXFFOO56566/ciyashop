package com.example.ciyashop.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityDeactiveAccountBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class DeactiveAccountActivity extends BaseActivity implements OnResponseListner {

    public String socialLogin;
    private ActivityDeactiveAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeactiveAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();
        setScreenLayoutDirection();
        setToolbarTheme();
        setThemeColor();
        hideSearchNotification();
        settvTitle(getResources().getString(R.string.account_setting));
        showBackButton();

        socialLogin = getPreferences().getString(RequestParamUtils.SOCIAL_SIGNIN, "");
        if (socialLogin.equals("1")) {
            binding.tilPassword.setVisibility(View.GONE);
        } else {
            binding.tilPassword.setVisibility(View.VISIBLE);
        }
    }

    public void setThemeColor() {
        binding.tvTitles.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        Drawable unwrappedDrawable = binding.llButton.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor((getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)))));
    }

    public void setClickEvent() {
        binding.tvConfirmDeactivation.setOnClickListener(v -> {
            if (binding.etEmail.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_SHORT).show();
            } else {
                if (Utils.isValidEmail(binding.etEmail.getText().toString())) {
                    if (socialLogin.equals("1")) {
                        saveData();
                    } else {
                        if (binding.etPassword.getText().toString().isEmpty()) {
                            Toast.makeText(this, R.string.enter_password, Toast.LENGTH_SHORT).show();
                        } else {
                            saveData();
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.enter_valid_email_address, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void saveData() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.deactivateUser, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                String id = getPreferences().getString(RequestParamUtils.ID, "");
                jsonObject.put(RequestParamUtils.user_id, id);
                jsonObject.put(RequestParamUtils.disable_user, "1");
                jsonObject.put(RequestParamUtils.email, binding.etEmail.getText().toString());

                if (socialLogin.equals("1")) {
                    jsonObject.put(RequestParamUtils.PASSWORD, "");
                    jsonObject.put(RequestParamUtils.socialUser, RequestParamUtils.yes);
                } else {
                    jsonObject.put(RequestParamUtils.PASSWORD, binding.etPassword.getText().toString());
                }
                postApi.callPostApi(new URLS().DEACTIVATE_USER, jsonObject.toString());
            } catch (JSONException e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.deactivateUser)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {
                        SharedPreferences.Editor pre = getPreferences().edit();
                        pre.putString(RequestParamUtils.CUSTOMER, "");
                        pre.putString(RequestParamUtils.ID, "");
                        pre.apply();
                        Toast.makeText(this, R.string.account_deactivated, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                    Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
