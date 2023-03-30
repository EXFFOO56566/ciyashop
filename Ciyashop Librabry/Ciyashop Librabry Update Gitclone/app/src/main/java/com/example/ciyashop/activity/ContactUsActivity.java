package com.example.ciyashop.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityContactUsBinding;
import com.example.ciyashop.utils.APIS;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactUsActivity extends BaseActivity implements OnResponseListner {
    private ActivityContactUsBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityContactUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();
        setToolbarTheme();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.contact_us));
        hideSearchNotification();
        showBackButton();

        if (Constant.WHATSAPP.length() == 0) {
            // tvEmail.setVisibility(View.GONE);
            //ivEmail.setVisibility(View.GONE);
        } else {
            // tvEmail.setText(Constant.WHATSAPP);
        }

        if (Constant.PHONE.length() == 0) {
            binding.tvPhone.setVisibility(View.GONE);
            binding.ivPhone.setVisibility(View.GONE);
        } else {
            binding.tvPhone.setText(Constant.PHONE);
        }

        binding.tvWebsite.setText(new APIS().APP_URL);

        if (Constant.EMAIL.length() == 0) {
            binding.tvEmail.setVisibility(View.GONE);
            binding.tvEmail.setVisibility(View.GONE);
        } else {
            binding.tvEmail.setText(Constant.EMAIL);
        }

        //binding.tvEmail.setText(URLS.APP_URL);

        if (Constant.ADDRESS_LINE1.length() == 0 && Constant.ADDRESS_LINE2.length() == 0) {
            binding.ivLocation.setVisibility(View.GONE);
            binding.tvLocation1.setVisibility(View.GONE);
            binding.tvLocation2.setVisibility(View.GONE);
        } else {
            if (Constant.ADDRESS_LINE1.length() == 0) {
                binding.tvLocation1.setVisibility(View.GONE);
            } else {
                binding.tvLocation1.setText(Constant.ADDRESS_LINE1 + " " + Constant.ADDRESS_LINE2);
            }
            if (Constant.ADDRESS_LINE2.length() == 0) {
                binding.tvLocation2.setVisibility(View.GONE);
            } else {
                binding.tvLocation2.setText(Constant.ADDRESS_LINE2);
            }
        }
        if (Constant.SOCIALLINK != null) {
            /*if (Constant.SOCIALLINK.pinterest == null || Constant.SOCIALLINK.pinterest.length() == 0) {
                ivPinterest.setVisibility(View.GONE);
            }*/
            if (Constant.SOCIALLINK.twitter == null || Constant.SOCIALLINK.twitter.length() == 0) {
                binding.ivTwitter.setVisibility(View.GONE);
            }
            if (Constant.SOCIALLINK.facebook == null || Constant.SOCIALLINK.facebook.length() == 0) {
                binding.ivFacebook.setVisibility(View.GONE);
            }
            if (Constant.SOCIALLINK.linkedin == null || Constant.SOCIALLINK.linkedin.length() == 0) {
                binding.ivLinkedin.setVisibility(View.GONE);
            }
            if (Constant.SOCIALLINK.googlePlus == null || Constant.SOCIALLINK.googlePlus.length() == 0) {
                binding.ivGooglePlus.setVisibility(View.GONE);
            }


        } else {
            //ivPinterest.setVisibility(View.GONE);
            binding.ivTwitter.setVisibility(View.GONE);
            binding.ivFacebook.setVisibility(View.GONE);
            binding.ivLinkedin.setVisibility(View.GONE);
            binding.ivGooglePlus.setVisibility(View.GONE);
            // ivInstagram.setVisibility(View.GONE);
        }
        if (Constant.APPLOGO != null && !Constant.APPLOGO.equals("")) {
            Glide.with(this).load(Constant.APPLOGO).error(R.drawable.logo).into(binding.Logo);
        }
        setColorTheme();
    }

    public void email() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto: " + binding.tvEmail.getText().toString()));
        startActivity(Intent.createChooser(emailIntent, "Send feedback"));
    }

    public void phone() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + binding.tvPhone.getText().toString()));
        startActivity(intent);
    }

    public void website() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(binding.tvWebsite.getText().toString()));
        startActivity(browserIntent);
    }

    public void setColorTheme() {
        binding.ivLocation.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        binding.ivPhone.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivWebsite.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivEmail.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivEmail.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        //tvSend.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        Drawable unwrappedDrawable = binding.tvSend.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        binding.tvPhone.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvWebsite.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvEmail.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvEmail.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvLocation1.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvLocation2.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvFollowTitle.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void contactUs() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.contactus, this, getlanuage());
            JSONObject object = new JSONObject();
            try {
                object.put(RequestParamUtils.name, binding.etName.getText().toString());
                object.put(RequestParamUtils.email, binding.etEmail.getText().toString());
                object.put(RequestParamUtils.contactNo, binding.etContactNumber.getText().toString());
                object.put(RequestParamUtils.subject, binding.etSubject.getText().toString());
                object.put(RequestParamUtils.message, binding.etMessage.getText().toString());
                postApi.callPostApi(new URLS().CONTACTUS, object.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.contactus)) {
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
                        binding.etContactNumber.getText().clear();
                        binding.etSubject.getText().clear();
                        binding.etMessage.getText().clear();
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

    public void setClickEvent() {
        binding.tvPhone.setOnClickListener(v -> phone());
        binding.ivPhone.setOnClickListener(v -> phone());
        binding.ivWebsite.setOnClickListener(v -> website());
        binding.tvWebsite.setOnClickListener(v -> website());
        binding.ivEmail.setOnClickListener(v -> email());
        binding.tvEmail.setOnClickListener(v -> email());

        binding.ivTwitter.setOnClickListener(v -> {
            String url = Constant.SOCIALLINK.twitter;
            if (!url.startsWith(RequestParamUtils.UrlStartWith) && !url.startsWith(RequestParamUtils.UrlStartWithsecure)) {
                url = RequestParamUtils.UrlStartWith + url;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        binding.ivFacebook.setOnClickListener(v -> {
            String url = Constant.SOCIALLINK.facebook;
            if (!url.startsWith(RequestParamUtils.UrlStartWith) && !url.startsWith(RequestParamUtils.UrlStartWithsecure)) {
                url = RequestParamUtils.UrlStartWith + url;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        binding.ivLinkedin.setOnClickListener(v -> {
            String url = Constant.SOCIALLINK.linkedin;
            if (!url.startsWith(RequestParamUtils.UrlStartWith) && !url.startsWith(RequestParamUtils.UrlStartWithsecure)) {
                url = RequestParamUtils.UrlStartWith + url;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        binding.ivGooglePlus.setOnClickListener(v -> {
            String url = Constant.SOCIALLINK.googlePlus;
            if (!url.startsWith(RequestParamUtils.UrlStartWith) && !url.startsWith(RequestParamUtils.UrlStartWithsecure)) {
                url = RequestParamUtils.UrlStartWith + url;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        binding.tvSend.setOnClickListener(v -> {
            if (binding.etName.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show();
            } else {
                if (binding.etEmail.getText().toString().isEmpty()) {
                    Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_SHORT).show();
                } else {
                    if (Utils.isValidEmail(binding.etEmail.getText().toString())) {
                        if (binding.etSubject.getText().toString().isEmpty()) {
                            Toast.makeText(this, R.string.enter_subject, Toast.LENGTH_SHORT).show();
                        } else {
                            if (binding.etMessage.getText().toString().isEmpty()) {
                                Toast.makeText(this, R.string.enter_message, Toast.LENGTH_SHORT).show();
                            } else {
                                contactUs();
                            }
                        }
                    } else {
                        Toast.makeText(this, R.string.enter_valid_email_address, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

}
