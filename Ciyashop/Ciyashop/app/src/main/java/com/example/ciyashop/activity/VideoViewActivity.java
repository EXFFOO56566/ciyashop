package com.example.ciyashop.activity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.ciyashop.databinding.ActivityVideoViewBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.RequestParamUtils;

public class VideoViewActivity extends BaseActivity {

    private String url;
    private ActivityVideoViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settvImage();
        hideSearchNotification();
        setToolbarTheme();
        showBackButton();
        url = getIntent().getExtras().getString(RequestParamUtils.VIDEO_URL);

        binding.wvVideoView.getSettings().setJavaScriptEnabled(true);
        binding.wvVideoView.getSettings().setDomStorageEnabled(true);
        binding.wvVideoView.getSettings().setJavaScriptEnabled(true);
        binding.wvVideoView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        binding.wvVideoView.getSettings().setSupportMultipleWindows(false);
        binding.wvVideoView.getSettings().setSupportZoom(false);
        binding.wvVideoView.setVerticalScrollBarEnabled(false);
        binding.wvVideoView.setHorizontalScrollBarEnabled(false);
        binding.wvVideoView.loadUrl(url);
        binding.wvVideoView.setWebViewClient(new loadWebView());
    }

    private static class loadWebView extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}
