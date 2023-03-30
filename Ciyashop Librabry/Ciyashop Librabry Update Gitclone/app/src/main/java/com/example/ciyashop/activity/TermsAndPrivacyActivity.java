package com.example.ciyashop.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityTermsAndPrivacyBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class TermsAndPrivacyActivity extends BaseActivity implements OnResponseListner {

    String data;
    private ActivityTermsAndPrivacyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTermsAndPrivacyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showBackButton();
        setToolbarTheme();
        hideSearchNotification();
        data = getIntent().getStringExtra(RequestParamUtils.PassingData);
        if (data.equals(RequestParamUtils.privacyPolicy)) {
            settvTitle(getResources().getString(R.string.privacy_policy));
        } else if (data.equals(RequestParamUtils.termsOfUse)) {
            settvTitle(getResources().getString(R.string.terms_condition));
        }
        getPages();
    }

    public void getPages() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.staticPage, this, getlanuage());
            JSONObject object = new JSONObject();
            try {
                object.put(RequestParamUtils.page, data);
                postApi.callPostApi(new URLS().STATIC_PAGES, object.toString());
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
        if (methodName.equals(RequestParamUtils.staticPage)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    //set call here
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {
                        String content = jsonObj.getString("data");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            binding.tvContentDesc.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            binding.tvContentDesc.setText(Html.fromHtml(content));
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
