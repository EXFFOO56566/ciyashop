package com.example.ciyashop.activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityContactSellerBinding;
import com.example.ciyashop.model.Customer;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;


public class ContactSellerActivity extends BaseActivity implements OnResponseListner {

    private ActivityContactSellerBinding binding;
    private String sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityContactSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setScreenLayoutDirection();
        setCustomerData();
        setToolbarTheme();
        setThemeColor();
        settvTitle(getResources().getString(R.string.contact_seller));
        showBackButton();
        sellerId = getIntent().getExtras().getString(RequestParamUtils.ID);
        setClickEvent();
    }

    private void setThemeColor() {
        Drawable unwrappedDrawable = binding.tvSend.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor((getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)))));
    }

    public void setCustomerData() {
        String cust = getPreferences().getString(RequestParamUtils.CUSTOMER, "");
        if (!cust.equals("")) {
            Customer customer = new Gson().fromJson(
                    cust, new TypeToken<Customer>() {
                    }.getType());
            String name = customer.firstName + " " + customer.lastName;
            binding.etName.setText(name);
            binding.etEmail.setText(customer.email);
        }
    }

    public void setClickEvent() {
        binding.tvSend.setOnClickListener(v -> {
            if (binding.etName.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show();
            } else {
                if (binding.etEmail.getText().toString().isEmpty()) {
                    Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_SHORT).show();
                } else {
                    if (Utils.isValidEmail(binding.etEmail.getText().toString())) {
                        if (binding.etMessage.getText().toString().isEmpty()) {
                            Toast.makeText(this, R.string.enter_message, Toast.LENGTH_SHORT).show();
                        } else {
                            contactSeller();
                        }
                    } else {
                        Toast.makeText(this, R.string.enter_valid_email_address, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void contactSeller() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(ContactSellerActivity.this, RequestParamUtils.contactSeller, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.name, binding.etName.getText().toString());
                jsonObject.put(RequestParamUtils.email, binding.etEmail.getText().toString());
                jsonObject.put(RequestParamUtils.message, binding.etMessage.getText().toString());
                jsonObject.put(RequestParamUtils.sellerId, sellerId);
                postApi.callPostApi(new URLS().CONTACT_SELLER, jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.contactSeller)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    //set call here
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {
                        Toast.makeText(this, R.string.message_sent_successfully, Toast.LENGTH_SHORT).show();
                        binding.etName.getText().clear();
                        binding.etEmail.getText().clear();
                        binding.etMessage.getText().clear();
                        finish();
                    } else {
                        Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                    Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
            }
        }
    }
}
