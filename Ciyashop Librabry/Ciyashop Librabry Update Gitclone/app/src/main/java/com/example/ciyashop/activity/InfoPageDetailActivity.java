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
import com.example.ciyashop.databinding.ActivityInfoPageDetailBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;

import org.json.JSONObject;

public class InfoPageDetailActivity extends BaseActivity implements OnResponseListner {

    private ActivityInfoPageDetailBinding binding;
    private String pageID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInfoPageDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pageID = getIntent().getExtras().getString(RequestParamUtils.ID);
        settvTitle(getIntent().getExtras().getString(RequestParamUtils.title));
        showBackButton();
        getPageData();
        setToolbarTheme();
    }

    public void getPageData() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.infoPages, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.pageId, pageID);
                postApi.callPostApi(new URLS().INFO_PAGES, jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.infoPages)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("status").equals("success")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            binding.tvDescription.setText(Html.fromHtml(jsonObject.getString("data"), Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            binding.tvDescription.setText(Html.fromHtml(jsonObject.getString("data")));
                        }
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
            }
        }
    }
}
