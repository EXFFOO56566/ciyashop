package com.example.ciyashop.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityWebviewWalletAddBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.Date;


public class WebViewWalletAddActivity extends BaseActivity {

    private String webData, webTitle;
    private static final String TAG = "WebViewWalletAdd";
    private ActivityWebviewWalletAddBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearCache(this, 1);

        binding = ActivityWebviewWalletAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        hideSearchNotification();
        setToolbarTheme();
        showBackButton();
        getIntentData();
        setScreenLayoutDirection();
        showProgress("");
        binding.webview.getSettings().setLoadsImagesAutomatically(true);
        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.getSettings().setDomStorageEnabled(true);
        binding.webview.setWebViewClient(new WebViewClient());
        binding.webview.setWebChromeClient(new WebChromeClient());
        binding.webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        binding.webview.setWebViewClient(new myWebClient());
        try {
            JSONObject jsonObject = new JSONObject();
            String customerId = getPreferences().getString(RequestParamUtils.ID, "");
            jsonObject.put(RequestParamUtils.user_id, customerId);
            if (Constant.IS_WPML_ACTIVE) {
                if (getPreferences().getString(RequestParamUtils.LANGUAGE, "").isEmpty()) {
                    if (!getPreferences().getString(RequestParamUtils.DEFAULTLANGUAGE, "").isEmpty()) {
                        jsonObject.put(RequestParamUtils.Languages, getPreferences().getString(RequestParamUtils.DEFAULTLANGUAGE, ""));
                    }
                } else {
                    jsonObject.put(RequestParamUtils.Languages, getPreferences().getString(RequestParamUtils.LANGUAGE, ""));
                }
            }
            Log.e("jsonObject ", jsonObject.toString());
            String postData = jsonObject.toString();
            binding.webview.postUrl(webData, postData.getBytes());
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent.hasExtra(RequestParamUtils.TRANSECTION_URL)) {
            webData = intent.getStringExtra(RequestParamUtils.TRANSECTION_URL);
            Log.e("Logged URL ==>", webData + "");
            settvTitle(getResources().getString(R.string.addtransection));
        } else {
            webData = "";
        }
    }

    //Custom WebViewClient
    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            Log.e("Responce", "----------------" + url);
            String text = "";

            String[] separated = url.split("\\?");
            String checkUrl = separated[0]; // this will contain "Fruit"
            for (int i = 0; i < Constant.CheckoutURL.size(); i++) {
                String value = Constant.CheckoutURL.get(i);
                if ((value.charAt(0) + "").contains("/")) {
                    Log.e(TAG, "fullURL: " + value);
                    StringBuilder sb = new StringBuilder(value);
                    sb.deleteCharAt(0);
                    value = sb.toString();
                    Log.e(TAG, "Deleted url      : " + value);
                }
                if ((value.charAt(value.length() - 1) + "").contains("/")) {
                    StringBuilder sb = new StringBuilder(value);
                    sb.deleteCharAt(value.length() - 1);
                    value = sb.toString();
                }
                if (!value.equals("")) {
                    if (checkUrl.contains(value)) {
                        //text = Constant.CheckoutURL.get(i);
                        text = value;
                        break;
                    }
                }
            }
            Log.e(TAG, "shouldOverrideUrlLoading: " + text);
            if (!text.isEmpty() && checkUrl.contains(text)) {
//                    List<Cart> cartList = databaseHelper.getFromCart(buyNow);
//                    for (int i = 0; i < cartList.size(); i++) {
//                        String product = cartList.get(i).getProduct();
//
//                        CategoryList categoryListRider = new Gson().fromJson(product, new TypeToken<CategoryList>() {
//                        }.getType());
//                        logPurchasedEvent(cartList.get(i).getQuantity(), categoryListRider.name, cartList.get(i).getProductid(), Constant.CURRENCYCODE, Double.parseDouble(categoryListRider.price));
//                    }
                SharedPreferences.Editor pre = getPreferences().edit();
                pre.putString(RequestParamUtils.ABANDONED, "");
                pre.putString(RequestParamUtils.ABANDONEDTIME, "");
                pre.apply();
                Intent intent = new Intent(WebViewWalletAddActivity.this, ThankYouActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                binding.webview.clearCache(true);
                binding.webview.clearHistory();
//                    isfirstLoad = false;
//                } else {
//                    Log.e("Else Condition ", "Called");
//                }
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

    static int clearCacheFolder(final File dir, final int numDays) {

        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    /*
     * Delete the files older than numDays days from the application cache
     * 0 means all files.
     *
     */

    public static void clearCache(final Context context, final int numDays) {
        Log.e(TAG, String.format("Starting cache prune, deleting files older than %d days", numDays));
        int numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays);
        Log.e(TAG, String.format("Cache pruning completed, %d files deleted", numDeletedFiles));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}
