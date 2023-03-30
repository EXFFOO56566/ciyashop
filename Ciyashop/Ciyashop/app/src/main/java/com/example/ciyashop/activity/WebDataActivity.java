package com.example.ciyashop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.ciyashop.databinding.ActivityWebDataBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.RequestParamUtils;

public class WebDataActivity extends BaseActivity {

    private static final String TAG = "WebDataActivity";
    private ActivityWebDataBinding binding;
    private String webData, webTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        hideSearchNotification();
        setToolbarTheme();
        showBackButton();
        getIntentData();
        setScreenLayoutDirection();
        WebSettings settings = binding.webview.getSettings();
        settings.setJavaScriptEnabled(true);
        binding.webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        showProgress("");

        binding.webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e(TAG, "Processing webview url click...");
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                Log.e(TAG, "Finished loading URL: " + url);
                dismissProgress();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(WebDataActivity.this, "Error :" + description, Toast.LENGTH_SHORT).show();
                dismissProgress();
            }
        });
        binding.webview.loadUrl(webData);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent.hasExtra(RequestParamUtils.WebData)) {
            webData = intent.getStringExtra(RequestParamUtils.WebData);
            webTitle = intent.getStringExtra(RequestParamUtils.WebTitle);
            settvTitle(webTitle);
        } else {
            webData = "";
        }
    }
}
