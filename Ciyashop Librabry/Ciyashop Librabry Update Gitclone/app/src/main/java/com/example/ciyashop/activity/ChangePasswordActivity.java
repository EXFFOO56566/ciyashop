package com.example.ciyashop.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityChangePasswordBinding;
import com.example.ciyashop.model.Customer;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePasswordActivity extends BaseActivity implements OnResponseListner {

    private ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();
        setToolbarTheme();
        hideSearchNotification();
        setThemeColor();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.account_setting));
        showBackButton();
    }

    public void setThemeColor() {
        binding.llButton.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvTitles.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void setClickEvent() {
        binding.tvCancel.setOnClickListener(v -> {
            finish();
        });

        binding.tvSave.setOnClickListener(v -> {
            if (binding.etEmail.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_SHORT).show();
            } else {
                if (Utils.isValidEmail(binding.etEmail.getText().toString())) {

                    String cust = getPreferences().getString(RequestParamUtils.CUSTOMER, "");
                    Customer customer = new Gson().fromJson(
                            cust, new TypeToken<Customer>() {
                            }.getType());
                    if (binding.etEmail.getText().toString().equalsIgnoreCase(customer.email.toLowerCase())) {
                        if (binding.etOldPassword.getText().toString().isEmpty()) {
                            Toast.makeText(this, R.string.enter_password, Toast.LENGTH_SHORT).show();
                        } else {
                            if (binding.etOldPassword.getText().toString().equals(getPreferences().getString(RequestParamUtils.PASSWORD, ""))) {
                                if (binding.etNewPassword.getText().toString().isEmpty()) {
                                    Toast.makeText(this, R.string.enter_new_password, Toast.LENGTH_SHORT).show();
                                } else {
                                    if (binding.etConfirrmNewPassword.getText().toString().isEmpty()) {
                                        Toast.makeText(this, R.string.enter_confirm_password, Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (binding.etNewPassword.getText().toString().equals(binding.etConfirrmNewPassword.getText().toString())) {
                                            //change Password
                                            changePassword();
                                        } else {
                                            Toast.makeText(this, R.string.password_and_confirm_password_not_matched, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(this, R.string.enter_proper_detail, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, R.string.enter_proper_detail, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.enter_valid_email_address, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void changePassword() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.resetPassword, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                String id = getPreferences().getString(RequestParamUtils.ID, "");
                jsonObject.put(RequestParamUtils.user_id, id);
                jsonObject.put(RequestParamUtils.PASSWORD, binding.etNewPassword.getText().toString());
                postApi.callPostApi(new URLS().RESET_PASSWORD, jsonObject.toString());
            } catch (JSONException e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.resetPassword)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {
                        SharedPreferences.Editor pre = getPreferences().edit();
                        pre.putString(RequestParamUtils.PASSWORD, binding.etNewPassword.getText().toString());
                        pre.apply();
                        binding.etEmail.setText("");
                        binding.etOldPassword.setText("");
                        binding.etNewPassword.setText("");
                        binding.etConfirrmNewPassword.setText("");
                        Toast.makeText(this, R.string.information_updated_successfully, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }
    }
}
