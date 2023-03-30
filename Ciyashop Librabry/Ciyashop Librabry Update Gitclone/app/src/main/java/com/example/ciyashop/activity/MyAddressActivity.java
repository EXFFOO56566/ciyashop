package com.example.ciyashop.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityMyAddressBinding;
import com.example.ciyashop.model.Customer;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

public class MyAddressActivity extends BaseActivity implements OnResponseListner {

    private ActivityMyAddressBinding binding;
    private Customer customer = new Customer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setClickEvent();
        setThemeColor();
        setToolbarTheme();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.my_address));
        showBackButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("here", "in on resume");
        setAddress();
    }

    public void setThemeColor() {
        binding.tvNoShippingAddressAdd.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvNoBillingAddressAdd.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvBillingAddresslabel.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvShippingAddresslabel.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvBillingEdit.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvShippingEdit.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        //setting icon theme color
        /*setColorFilter(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));*/
        binding.ivBillingaddress.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivBillingname.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivBillingphoneno.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));


        binding.ivShippingaddress.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivShippingname.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivShippingphoneno.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivShippingemail.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void setAddress() {
        //set Address data
        String cust = getPreferences().getString(RequestParamUtils.CUSTOMER, "");
        customer = new Gson().fromJson(
                cust, new TypeToken<Customer>() {
                }.getType());

        if (customer.billing.phone.equals("") &&
                customer.billing.firstName.equals("") &&
                customer.billing.lastName.equals("") &&
                customer.billing.address1.equals("") &&
                customer.billing.address2.equals("") &&
                customer.billing.company.equals("") &&
                customer.billing.city.equals("") &&
                customer.billing.state.equals("") &&
                customer.billing.postcode.equals("")) {

            binding.llBillingAddress.setVisibility(View.GONE);
            binding.llNoBillingAddress.setVisibility(View.VISIBLE);

        } else {
            binding.llBillingAddress.setVisibility(View.VISIBLE);
            binding.llNoBillingAddress.setVisibility(View.GONE);

            binding.tvBillingPhoneNumber.setText(customer.billing.phone);

            String address1empty = customer.billing.address1.equals("") ? "" : ", ";
            String address2empty = customer.billing.address2.equals("") ? "" : ", ";
            String cityEmpty = customer.billing.city.equals("") ? "" : ", ";
            String stateEmpty = customer.billing.city.equals("") ? "" : ", ";
            String countryEmpty = customer.billing.country.equals("") ? "" : ", ";

            String address = customer.billing.address1 + address1empty + customer.billing.address2 + address2empty + customer.billing.city + cityEmpty + customer.billing.state + stateEmpty + customer.billing.country + countryEmpty + customer.billing.postcode;
            binding.tvBillingAddress.setText(address);
            String name = customer.billing.firstName + " " + customer.billing.lastName;
            binding.tvBillingName.setText(name);
        }

        if (customer.shipping.firstName.equals("") && customer.shipping.lastName.equals("") && customer.shipping.address1.equals("") && customer.shipping.address2.equals("") && customer.shipping.company.equals("") && customer.shipping.city.equals("") && customer.shipping.state.equals("") && customer.shipping.postcode.equals("")) {
            binding.llShippingAddress.setVisibility(View.GONE);
            binding.llNoShippingAddress.setVisibility(View.VISIBLE);
        } else {
            binding.llShippingAddress.setVisibility(View.VISIBLE);
            binding.llNoShippingAddress.setVisibility(View.GONE);

            String address1empty = customer.shipping.address1.equals("") ? "" : ", ";
            String address2empty = customer.shipping.address2.equals("") ? "" : ", ";
            String cityEmpty = customer.shipping.city.equals("") ? "" : ", ";
            String stateEmpty = customer.shipping.city.equals("") ? "" : ", ";
            String countryEmpty = customer.shipping.country.equals("") ? "" : ", ";

            String address = customer.shipping.address1 + address1empty + customer.shipping.address2 + address2empty + customer.shipping.city + cityEmpty + customer.shipping.state + stateEmpty + customer.shipping.country + countryEmpty + customer.shipping.postcode;
            binding.tvShippingAddress.setText(address);
            String name = customer.shipping.firstName + " " + customer.shipping.lastName;
            binding.tvShippingName.setText(name);
        }
    }

    public void addBilling() {
        Intent intent = new Intent(MyAddressActivity.this, AddAddressActivity.class);
        intent.putExtra(RequestParamUtils.type, 0);
        startActivity(intent);
    }

    public void addShipping() {
        Intent intent = new Intent(MyAddressActivity.this, AddAddressActivity.class);
        intent.putExtra(RequestParamUtils.type, 1);
        startActivity(intent);
    }

    public void setClickEvent() {
        binding.tvBillingEdit.setOnClickListener(v -> addBilling());

        binding.tvShippingEdit.setOnClickListener(v -> addShipping());

        binding.tvNoBillingAddressAdd.setOnClickListener(v -> addBilling());

        binding.tvNoShippingAddressAdd.setOnClickListener(v -> addShipping());

        binding.tvBillingRemove.setOnClickListener(v -> {
            try {
                JSONObject main = new JSONObject();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.address1, "");
                jsonObject.put(RequestParamUtils.address2, "");
                jsonObject.put(RequestParamUtils.city, "");
                jsonObject.put(RequestParamUtils.company, "");
                jsonObject.put(RequestParamUtils.country, "");
                jsonObject.put(RequestParamUtils.firstName, "");
                jsonObject.put(RequestParamUtils.lastName, "");
                jsonObject.put(RequestParamUtils.phone, "");
                jsonObject.put(RequestParamUtils.postcode, "");
                jsonObject.put(RequestParamUtils.state, "");
                main.put(RequestParamUtils.billing, jsonObject);
                removeAddress(main);
            } catch (JSONException e) {
                Log.e("error", e.getMessage());
            }
        });

        binding.tvShippingRemove.setOnClickListener(v -> {
            try {
                JSONObject main = new JSONObject();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.address1, "");
                jsonObject.put(RequestParamUtils.address2, "");
                jsonObject.put(RequestParamUtils.city, "");
                jsonObject.put(RequestParamUtils.company, "");
                jsonObject.put(RequestParamUtils.country, "");
                jsonObject.put(RequestParamUtils.firstName, "");
                jsonObject.put(RequestParamUtils.lastName, "");
                jsonObject.put(RequestParamUtils.postcode, "");
                jsonObject.put(RequestParamUtils.state, "");
                main.put(RequestParamUtils.shipping, jsonObject);
                removeAddress(main);
            } catch (JSONException e) {
                Log.e("error", e.getMessage());
            }
        });
    }

    public void removeAddress(JSONObject object) {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.removeAddress, this, getlanuage());
            String customerId = getPreferences().getString(RequestParamUtils.ID, "");
            new URLS();
            postApi.callPostApi(URLS.WOO_MAIN_URL + new URLS().WOO_CUSTOMERS + "/" + customerId, object.toString());
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.removeAddress)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("error")) {
                        String msg = jsonObj.getString("message");
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    try {
                        customer = new Gson().fromJson(response, new TypeToken<Customer>() {
                        }.getType());
                        SharedPreferences.Editor pre = getPreferences().edit();
                        pre.putString(RequestParamUtils.CUSTOMER, response);
                        pre.apply();
                        setAddress();
                    } catch (Exception e1) {
                        Log.e("Exception is ", e1.getMessage());
                    }
                }
            }
        }
        else if (methodName.equals(RequestParamUtils.IsUserExists)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("is_user_exists");
                    Log.e("TAG", "onResponse:+++++ " + "" + status);
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
}
