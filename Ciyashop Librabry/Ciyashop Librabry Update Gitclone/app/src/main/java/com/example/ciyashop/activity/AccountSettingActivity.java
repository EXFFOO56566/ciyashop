package com.example.ciyashop.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityAccountSettingBinding;
import com.example.ciyashop.model.Customer;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class AccountSettingActivity extends BaseActivity implements OnResponseListner {

    private ActivityAccountSettingBinding binding;

    private Customer customer = new Customer();

    DatePickerDialog datePickerDialog;
    private boolean allowClose = false;
    String customerId;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAccountSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        customerId = getPreferences().getString(RequestParamUtils.ID, "");
        setClickEvent();
        setToolbarTheme();
        setThemeColor();
        hideSearchNotification();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.account_setting));
        showBackButton();
        setData();
    }

    public void setThemeColor() {
        binding.icGoOne.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivGo.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvDeactivateAccount.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvChangePassword.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        Drawable unwrappedDrawable = binding.tvSave.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor((getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)))));
    }

    public void setData() {
        String cust = getPreferences().getString(RequestParamUtils.CUSTOMER, "");
        customer = new Gson().fromJson(
                cust, new TypeToken<Customer>() {
                }.getType());

        String socialLogin = getPreferences().getString(RequestParamUtils.SOCIAL_SIGNIN, "");

        if (socialLogin.equals("1")) {
            binding.llPassword.setVisibility(View.GONE);
        } else {
            binding.llPassword.setVisibility(View.VISIBLE);
        }
        if (customer.id != 0) {
            //setData
            binding.etFirstName.setText(customer.firstName);
            binding.etLastName.setText(customer.lastName);
            binding.tvEmail.setText(customer.email);
            try {
                JSONObject jsonObject = new JSONObject(cust);
                JSONArray jsonArray = jsonObject.getJSONArray("meta_data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jb = jsonArray.getJSONObject(i);
                    if (jb.getString("key").equalsIgnoreCase("mobile")) {
                        //mobile
                        binding.etMobileNumber.setText(jb.getString("value"));
                    } else if (jb.getString("key").equalsIgnoreCase("gender")) {
                        //gender

                        if (jb.getString("value").equalsIgnoreCase("male")) {
                            GradientDrawable gradientDrawable = (GradientDrawable) binding.flMale.getBackground();
                            gradientDrawable.setColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
                            gradientDrawable = (GradientDrawable) binding.flFemale.getBackground();
                            gradientDrawable.setColor(Color.parseColor("#c5c5c5"));

                            //flMale.setBackgroundResource(R.drawable.primary_round_button);
                            binding.ivRightFemale.setVisibility(View.GONE);
                            binding.ivRightMale.setVisibility(View.VISIBLE);
                        } else if (jb.getString("value").equalsIgnoreCase("female")) {
                            GradientDrawable gradientDrawable = (GradientDrawable) binding.flFemale.getBackground();
                            gradientDrawable.setColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
                            gradientDrawable = (GradientDrawable) binding.flMale.getBackground();
                            gradientDrawable.setColor(Color.parseColor("#c5c5c5"));

//                            flMale.setBackgroundResource(R.drawable.gray_round_corner_button);
                            binding.ivRightFemale.setVisibility(View.VISIBLE);
                            binding.ivRightMale.setVisibility(View.GONE);
                        } else {
                            binding.flFemale.setBackgroundResource(R.drawable.gray_round_corner_button);
                            binding.flMale.setBackgroundResource(R.drawable.gray_round_corner_button);
                            binding.ivRightFemale.setVisibility(View.GONE);
                            binding.ivRightMale.setVisibility(View.GONE);
                        }
                    } else if (jb.getString("key").equalsIgnoreCase("dob")) {
                        //DOB
                        binding.etDOB.setText(jb.getString("value"));
                    }
                }
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        } else {
            //no data
        }
    }

    public void setClickEvent() {
        binding.flMale.setOnClickListener(v -> {
            GradientDrawable gradientDrawable = (GradientDrawable) binding.flMale.getBackground();
            gradientDrawable.setColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
            gradientDrawable = (GradientDrawable) binding.flFemale.getBackground();
            gradientDrawable.setColor(Color.parseColor("#c5c5c5"));

            binding.ivRightFemale.setVisibility(View.GONE);
            binding.ivRightMale.setVisibility(View.VISIBLE);
        });

        binding.flFemale.setOnClickListener(v -> {
            GradientDrawable gradientDrawable = (GradientDrawable) binding.flFemale.getBackground();
            gradientDrawable.setColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
            gradientDrawable = (GradientDrawable) binding.flMale.getBackground();
            gradientDrawable.setColor(Color.parseColor("#c5c5c5"));
            binding.ivRightFemale.setVisibility(View.VISIBLE);
            binding.ivRightMale.setVisibility(View.GONE);
        });

        binding.tvDeactivateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(AccountSettingActivity.this, DeactiveAccountActivity.class);
            startActivity(intent);
        });

        binding.tvChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(AccountSettingActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        binding.etDOB.setOnClickListener(v -> {
            //select date
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            final int minYear = mYear - 8;
            final int minMonth = mMonth;
            final int minDay = mDay;

            if (String.valueOf(binding.etDOB.getText()).isEmpty()) {
                mYear = minYear;
                mMonth = minMonth;
                mDay = minDay;
            } else {
                String selectedDate = binding.etDOB.getText().toString();
                String[] dateParts = selectedDate.split("/");
                String day = dateParts[0];
                String month = dateParts[1];
                String year = dateParts[2];

                mYear = Integer.parseInt(year);
                mMonth = Integer.parseInt(month) - 1;
                mDay = Integer.parseInt(day);
            }
            DatePickerDialog.OnDateSetListener datePickerListener = (view, selectedYear, selectedMonth, selectedDay) -> {
            };

            datePickerDialog = new DatePickerDialog(
                    AccountSettingActivity.this, datePickerListener,
                    mYear, mMonth, mDay) {
                @Override
                public void onBackPressed() {
                    allowClose = true;
                    super.onBackPressed();
                }

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {

                        DatePicker datePicker = datePickerDialog
                                .getDatePicker();

                        if (datePicker.getYear() < minYear || datePicker.getMonth() < minMonth && datePicker.getYear() == minYear ||
                                datePicker.getDayOfMonth() <= minDay && datePicker.getYear() == minYear && datePicker.getMonth() == minMonth) {

                            datePicker.updateDate(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                            String dob = datePicker.getDayOfMonth() + "/" + (datePicker.getMonth() + 1) + "/" + datePicker.getYear();
                            binding.etDOB.setText(dob);
                            allowClose = true;
                        } else {
                            allowClose = false;
                            Toast.makeText(AccountSettingActivity.this, R.string.enter_proper_detail, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            allowClose = true;
                        }
                    }
                    super.onClick(dialog, which);
                }

                @Override
                public void dismiss() {
                    if (allowClose) {
                        super.dismiss();
                    }
                }
            };

            datePickerDialog.setCancelable(false);
            datePickerDialog.show();
        });
        binding.tvSave.setOnClickListener(v -> saveData());

        binding.llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AccountSettingActivity.this, DeleteAccountActivity.class);
                startActivity(intent);


            }
        });


    }

    public void saveData() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.updateCustomer, this, getlanuage());
            try {
                customer.firstName = String.valueOf(binding.etFirstName.getText());
                customer.lastName = String.valueOf(binding.etLastName.getText());
                customer.dob = String.valueOf(binding.etDOB.getText());

                String data = new Gson().toJson(customer);
                JSONObject jsonObject = new JSONObject(data);

                String id = getPreferences().getString(RequestParamUtils.ID, "");
                String phone = String.valueOf(binding.etMobileNumber.getText());
                jsonObject.put(RequestParamUtils.user_id, id);
                jsonObject.put(RequestParamUtils.mobiles, phone);

                if (binding.ivRightMale.getVisibility() == View.VISIBLE) {
                    jsonObject.put(RequestParamUtils.gender, "Male");
                } else if (binding.ivRightFemale.getVisibility() == View.VISIBLE) {
                    jsonObject.put(RequestParamUtils.gender, "Female");
                }
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
        Log.e("TAG", "onResponse: Response 1==>"+response);
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
                        Toast.makeText(this, R.string.information_updated_successfully, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }
        else if (methodName.equals(RequestParamUtils.IsUserExists)){

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

}