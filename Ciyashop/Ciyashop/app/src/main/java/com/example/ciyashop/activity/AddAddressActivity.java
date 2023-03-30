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
import com.example.ciyashop.databinding.ActivityAddAddressBinding;
import com.example.ciyashop.databinding.ToolbarBinding;
import com.example.ciyashop.model.Customer;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

public class AddAddressActivity extends BaseActivity implements OnResponseListner {

    private ActivityAddAddressBinding binding;
    private ToolbarBinding toolbarbinding;
    private Customer customer = new Customer();
    private String cust;
    private Bundle bundle;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        toolbarbinding = ToolbarBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        setClickEvent();
        setScreenLayoutDirection();
        setToolbarTheme();
        setThemeColor();
        bundle = getIntent().getExtras();
        if (bundle != null) {
            type = bundle.getInt(RequestParamUtils.type);
        }
        if (type == 0) {
            binding.tvActivityTitle.setText(getResources().getText(R.string.add_billing_address));
        } else {
            binding.tvActivityTitle.setText(getResources().getText(R.string.add_shipping_address));
            binding.tilPhone.setVisibility(View.GONE);
        }
        settvTitle(getResources().getString(R.string.add_new_addresses));
        showBackButton();
    }



    public void setThemeColor() {

        toolbarbinding.ivSearch.setVisibility(View.GONE);

        //tvSave
        Drawable tvSaveAddressDrawable = binding.tvSave.getBackground();
        Drawable rappedDrawable = DrawableCompat.wrap(tvSaveAddressDrawable);
        DrawableCompat.setTint(rappedDrawable, Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        binding.tvActivityTitle.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));


      //  binding.tvCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray_round_button));
        Drawable tvCancelAddressDrawable = binding.tvCancel.getBackground();
        Drawable rappedDrawable1 = DrawableCompat.wrap(tvCancelAddressDrawable);
        DrawableCompat.setTint(rappedDrawable1, getResources().getColor(R.color.gray));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cust = getPreferences().getString(RequestParamUtils.CUSTOMER, "");
        customer = new Gson().fromJson(
                cust, new TypeToken<Customer>() {
                }.getType());
        setAddress();
    }

    public void setAddress() {
        if (type == 0) {
            //billing Address
            binding.etFirstName.setText(customer.billing.firstName);
            binding.etLastName.setText(customer.billing.lastName);
            binding.etPincode.setText(customer.billing.postcode);
            binding.etAddress1.setText(customer.billing.address1);
            binding.etAddress2.setText(customer.billing.address2);
            binding.etCity.setText(customer.billing.city);
            binding.etPhoneNumber.setText(customer.billing.phone);
            binding.etCompany.setText(customer.billing.company);
        } else {
            //Shipping Address
            binding.etFirstName.setText(customer.shipping.firstName);
            binding.etLastName.setText(customer.shipping.lastName);
            binding.etPincode.setText(customer.shipping.postcode);
            binding.etAddress1.setText(customer.shipping.address1);
            binding.etAddress2.setText(customer.shipping.address2);
            binding.etCity.setText(customer.shipping.city);
            binding.etCompany.setText(customer.shipping.company);
        }
    }

    public void setClickEvent() {
        binding.tvCancel.setOnClickListener(v -> finish());
        binding.tvSave.setOnClickListener(v -> updateAddress());
    }

    public void updateAddress() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.updateCustomer, this, getlanuage());
            try {
                String strFirstName = binding.etFirstName.getText().toString();
                String strLastName = binding.etLastName.getText().toString();
                String strPinCode = binding.etPincode.getText().toString();
                String strAddress1 = binding.etAddress1.getText().toString();
                String strAddress2 = binding.etAddress2.getText().toString();
                String strCity = binding.etCity.getText().toString();
                String strCompany = binding.etCompany.getText().toString();
                String strPhoneNumber = binding.etPhoneNumber.getText().toString();
                if (type == 0) {
                    //Billing Address
                    customer.billing.firstName = strFirstName;
                    customer.billing.lastName = strLastName;
                    customer.billing.postcode = strPinCode;
                    customer.billing.address1 = strAddress1;
                    customer.billing.address2 = strAddress2;
                    customer.billing.city = strCity;
                    customer.billing.company = strCompany;
                    customer.billing.phone = strPhoneNumber;
                } else {
                    //Shipping Address
                    customer.shipping.firstName = strFirstName;
                    customer.shipping.lastName = strLastName;
                    customer.shipping.postcode = strPinCode;
                    customer.shipping.address1 = strAddress1;
                    customer.shipping.address2 = strAddress2;
                    customer.shipping.city = strCity;
                    customer.shipping.company = strCompany;
                }
                String data = new Gson().toJson(customer);
                JSONObject jsonObject = new JSONObject(data);
                String id = getPreferences().getString(RequestParamUtils.ID, "");

                jsonObject.put(RequestParamUtils.user_id, id);
                postApi.callPostApi(new URLS().UPDATE_CUSTOMER, jsonObject.toString());
            } catch (JSONException e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.updateCustomer)) {
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
                        Toast.makeText(this, R.string.address_updated_successfully, Toast.LENGTH_SHORT).show();
                        finish();
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