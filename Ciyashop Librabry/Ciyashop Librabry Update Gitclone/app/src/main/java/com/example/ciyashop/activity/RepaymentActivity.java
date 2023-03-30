package com.example.ciyashop.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.multidex.BuildConfig;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityRepaymentBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;

import org.json.JSONObject;

public class RepaymentActivity extends BaseActivity implements OnResponseListner {

    private static final String TAG = "RepaymentActivity";

    private ActivityRepaymentBinding binding;

    String url, thank_you_url, home_url, track_url, thank_you_again;
    private DatabaseHelper databaseHelper;
    private final boolean isFirstLoad = false;
    private int buyNow;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRepaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();
        databaseHelper = new DatabaseHelper(this);
        settvImage();
        hideSearchNotification();
        setToolbarTheme();
        showBackButton();
        setScreenLayoutDirection();
        url = getIntent().getExtras().getString(RequestParamUtils.RepaymentURL);
        thank_you_again = getIntent().getExtras().getString(RequestParamUtils.THANKYOUExtra);
        thank_you_url = getIntent().getExtras().getString(RequestParamUtils.THANKYOU);

        buyNow = getIntent().getExtras().getInt(RequestParamUtils.buynow);
        binding.wvRepayment.getSettings().setLoadsImagesAutomatically(true);
        binding.wvRepayment.getSettings().setJavaScriptEnabled(true);
        binding.wvRepayment.getSettings().setDomStorageEnabled(true);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        binding.wvRepayment.setWebViewClient(new WebViewClient());
        binding.wvRepayment.setWebChromeClient(new WebChromeClient());
        binding.wvRepayment.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        binding.wvRepayment.setWebViewClient(new RepaymentActivity.myWebClient());
        try {
            JSONObject jsonObject = new JSONObject();
            String customerId = getPreferences().getString(RequestParamUtils.ID, "");
            jsonObject.put(RequestParamUtils.REPAY, "yes");
            jsonObject.put(RequestParamUtils.FROMAPP, "yes");
            jsonObject.put(RequestParamUtils.user_id, customerId);
            String postData = jsonObject.toString();
            binding.wvRepayment.postUrl(url, postData.getBytes());
            showProgress("");
            setToolbarTheme();
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
        //Initiated Checkout Facebook
    }

    public void setClickEvent() {
        if (binding.wvRepayment.canGoBack()) {
            binding.wvRepayment.goBack();
        } else {
            binding.wvRepayment.clearCache(true);
            binding.wvRepayment.clearHistory();
            logout();
        }
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            Log.e("Responce", "----------------" + url);
            if (url.contains(thank_you_url)) {
                SharedPreferences.Editor pre = getPreferences().edit();
                pre.putString(RequestParamUtils.ABANDONED, "");
                pre.putString(RequestParamUtils.ABANDONEDTIME, "");
                pre.apply();
                Intent intent = new Intent(RepaymentActivity.this, ThankYouActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                binding.wvRepayment.clearCache(true);
                binding.wvRepayment.clearHistory();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            // progress.setVisibility(View.GONE);
            super.onPageFinished(view, url);
            dismissProgress();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && binding.wvRepayment.canGoBack()) {
            binding.wvRepayment.goBack();
            return true;
        } else {
            binding.wvRepayment.clearCache(true);
            binding.wvRepayment.clearHistory();
            logout();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void logout() {
        if (Utils.isInternetConnected(this)) {
//            showProgress("");
            PostApi postApi = new PostApi(this, "logout", this, getlanuage());
            postApi.callPostApi(new URLS().LOGOUT, "");
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        dismissProgress();
        if (methodName.equals(RequestParamUtils.logout)) {
            if (response != null && response.length() > 0) {
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {
                        finish();
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }
    }
}
