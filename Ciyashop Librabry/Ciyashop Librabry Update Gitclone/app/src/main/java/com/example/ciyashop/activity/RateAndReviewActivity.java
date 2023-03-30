package com.example.ciyashop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityRateAndReviewBinding;
import com.example.ciyashop.model.Customer;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;


public class RateAndReviewActivity extends BaseActivity implements OnResponseListner {

    String image, pid;
    private String customerId;
    private Customer customer = new Customer();

    private ActivityRateAndReviewBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRateAndReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();
        setToolbarTheme();
        settvTitle(getResources().getString(R.string.review));
        showBackButton();
        setColorTheme();
        setScreenLayoutDirection();
        hideSearchNotification();
        Intent i = getIntent();
        binding.tvProductName.setText(i.getStringExtra(RequestParamUtils.PRODUCT_NAME));
        image = i.getStringExtra(RequestParamUtils.IMAGEURL);
        Glide.with(this).load(image).into(binding.ivProductImage);
//        try {
//            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(imge).getContent());
//            ivProductImage.setImageBitmap(bitmap);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }

        pid = i.getStringExtra(RequestParamUtils.PRODUCT_ID);
        checkLoggedIn();
    }


    public void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                1.1f, 1f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        v.startAnimation(anim);
    }


    public void setClickEvent() {
        binding.tvSubmit.setOnClickListener(v -> {
            if (customerId.equals("")) {
                if (binding.etUserName.getText().toString().length() == 0) {
                    Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show();
                } else {
                    if (binding.etEmail.getText().toString().length() == 0) {
                        Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_SHORT).show();
                    } else {
                        if (binding.etComment.getText().length() == 0) {
                            Toast.makeText(this, R.string.enter_message, Toast.LENGTH_SHORT).show();
                        } else {
                            if (binding.rating.getRating() == 0) {
                                Toast.makeText(this, getResources().getString(R.string.please_apply_rating), Toast.LENGTH_SHORT).show();
                            } else {
                                submitRate();
                            }
                        }
                    }
                }
            } else {
                if (binding.etComment.getText().length() == 0) {
                    Toast.makeText(this, R.string.enter_message, Toast.LENGTH_SHORT).show();
                } else {
                    if (binding.rating.getRating() == 0) {
                        Toast.makeText(this, getResources().getString(R.string.please_apply_rating), Toast.LENGTH_SHORT).show();
                    } else {
                        submitRate();
                    }
                }
            }
        });


        binding.rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                scaleView(binding.rating, 1.1f, 1f);
            }
        });

    }

    private void setColorTheme() {
        // tvContactSeller.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        Drawable unwrappedDrawable = binding.tvSubmit.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
    }

    public void submitRate() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.submitRate, this, getlanuage());
            JSONObject object = new JSONObject();
            try {
                object.put(RequestParamUtils.emailcustomer, binding.etEmail.getText().toString());
                object.put(RequestParamUtils.namecustomer, binding.etUserName.getText().toString());
                object.put(RequestParamUtils.product, pid);
                object.put(RequestParamUtils.comment, binding.etComment.getText().toString());
                object.put(RequestParamUtils.ratestar, binding.rating.getRating());
                if (!(customerId.equals(""))) {
                    object.put(RequestParamUtils.USER_ID, customerId);
                }
                postApi.callPostApi(new URLS().RATING, object.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void checkLoggedIn() {
        customerId = getPreferences().getString(RequestParamUtils.ID, "");
        String cust = getPreferences().getString(RequestParamUtils.CUSTOMER, "");
        if (cust.equals("")) {
            if (!customerId.isEmpty()) {
                binding.etEmail.setEnabled(true);
                binding.etUserName.setEnabled(true);
            }
        } else {
            customer = new Gson().fromJson(
                    cust, new TypeToken<Customer>() {
                    }.getType());

            setCustomerData();
        }
    }

    public void setCustomerData() {
        String name = customer.firstName + " " + customer.lastName;
        binding.etUserName.setText(name);
        binding.etEmail.setText(customer.email);
        binding.etUserName.setEnabled(binding.etUserName.getText().toString().contains("null"));
        binding.etEmail.setEnabled(false);
    }

    @Override
    public void onResponse(String response, String methodName) {
        Log.e("Rating api", response);
        if (methodName.equals(RequestParamUtils.submitRate)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {
                        Toast.makeText(this, getString(R.string.your_review_is_waiting_for_approval), Toast.LENGTH_SHORT).show();
                        finish();
                        //  onBackPressed();
                    } else {
                        Toast.makeText(this, jsonObj.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }
    }
}
