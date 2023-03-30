package com.example.ciyashop.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.customview.pref.BuildConfig;
import com.example.ciyashop.databinding.ActivityAboutUsBinding;
import com.example.ciyashop.databinding.ToolbarBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

import java.util.Calendar;

public class AboutUsActivity extends BaseActivity implements OnResponseListner {

    private static final String TAG = "AboutUsActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private ActivityAboutUsBinding binding;
    private ToolbarBinding toolbarbinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAboutUsBinding.inflate(getLayoutInflater());
        toolbarbinding = ToolbarBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());
        setCurrentYear();
        setClickEvent();
        setToolbarTheme();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.about_us));
        showBackButton();
        aboutUs();
        hideSearchNotification();
        String versionCode = getString(R.string.version) + BuildConfig.VERSION_CODE;
        binding.tvVersion.setText(versionCode);
        if (Constant.SOCIALLINK != null) {
            if (Constant.SOCIALLINK.pinterest == null || Constant.SOCIALLINK.pinterest.length() == 0) {
                binding.ivPinterest.setVisibility(View.GONE);
            }
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
            if (Constant.SOCIALLINK.instagram == null || Constant.SOCIALLINK.instagram.length() == 0) {
                binding.ivInstagram.setVisibility(View.GONE);
            }
        } else {
            binding.ivPinterest.setVisibility(View.GONE);
            binding.ivTwitter.setVisibility(View.GONE);
            binding.ivFacebook.setVisibility(View.GONE);
            binding.ivLinkedin.setVisibility(View.GONE);
            binding.ivGooglePlus.setVisibility(View.GONE);
            binding.ivInstagram.setVisibility(View.GONE);
        }
        if (Constant.APPLOGO != null && !Constant.APPLOGO.equals("")) {
            Glide.with(this).load(Constant.APPLOGO).error(R.drawable.logo).into(binding.Logo);
        }
        setColorTheme();

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = getString(R.string.version) + pInfo.versionName;
            binding.tvVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }



    public void setCurrentYear(){
        Log.e(TAG, "setCurrentYear: gdfgdssgdfgdfg" );
        binding.tvCopyRight.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Log.e(TAG, "setCurrentYear: gdfgdssgdfgdfg"+year );
        if(year != 0){
            binding.tvCopyRight.setText(getString(R.string.footer).replace("2021",""+year));
        }else{
            //Toast.makeText(this, "dkjds", Toast.LENGTH_SHORT).show();

        }


    }
    public void setClickEvent() {
        binding.ivPinterest.setOnClickListener(v -> {
            String url = Constant.SOCIALLINK.pinterest;
            if (!url.startsWith(RequestParamUtils.UrlStartWith) && !url.startsWith(RequestParamUtils.UrlStartWithsecure)) {
                url = RequestParamUtils.UrlStartWith + url;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        binding.ivInstagram.setOnClickListener(v -> {
            String url = Constant.SOCIALLINK.instagram;
            if (!url.startsWith(RequestParamUtils.UrlStartWith) && !url.startsWith(RequestParamUtils.UrlStartWithsecure)) {
                url = RequestParamUtils.UrlStartWith + url;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

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

        binding.tvTermsAndCondition.setOnClickListener(v -> {
            Intent intent = new Intent(AboutUsActivity.this, TermsAndPrivacyActivity.class);
            intent.putExtra(RequestParamUtils.PassingData, RequestParamUtils.termsOfUse);
            startActivity(intent);
        });

        binding.tvPrivacyPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(AboutUsActivity.this, TermsAndPrivacyActivity.class);
            intent.putExtra(RequestParamUtils.PassingData, RequestParamUtils.privacyPolicy);
            startActivity(intent);
        });
    }

    public void setColorTheme() {
        binding.tvTermsAndCondition.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvPrivacyPolicy.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivFacebook.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivGooglePlus.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivLinkedin.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivTwitter.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivPinterest.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivInstagram.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvCopyRight.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvVersion.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        binding.tvFollowUs.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvMoreAboutUs.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void aboutUs() {
        PostApi postApi = new PostApi(this, RequestParamUtils.staticPage, this, getlanuage());
        JSONObject object = new JSONObject();
        try {
            object.put(RequestParamUtils.page, RequestParamUtils.about_us);
            showProgress("");
            postApi.callPostApi(new URLS().STATIC_PAGES, object.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.staticPage)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    //set call here
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {
                        String content = jsonObj.getString("data");
                        if (content.equals("")) {
                            binding.tvMoreAboutUs.setVisibility(View.GONE);
                            binding.tvAboutusContent.setVisibility(View.GONE);
                        } else {
                            binding.tvMoreAboutUs.setVisibility(View.VISIBLE);
                            binding.tvAboutusContent.setVisibility(View.VISIBLE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvAboutusContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
                            } else {
                                binding.tvAboutusContent.setText(Html.fromHtml(content));
                            }
                            binding.tvAboutusContent.setHtml(content,
                                    new HtmlHttpImageGetter(binding.tvAboutusContent));
                        }
                    } else {
                        Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
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
