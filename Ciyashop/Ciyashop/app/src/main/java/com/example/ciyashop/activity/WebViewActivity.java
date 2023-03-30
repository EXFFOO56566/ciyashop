package com.example.ciyashop.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.ciyashop.library.apicall.Ciyashop;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivityWebviewBinding;
import com.example.ciyashop.databinding.ToolbarBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.model.Cart;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class WebViewActivity extends BaseActivity implements OnResponseListner {

    private static final String TAG = "WebViewActivity";
    String url, thank_you_url, home_url, track_url, thank_you_again;
    private DatabaseHelper databaseHelper;
    private boolean isFirstLoad = false;
    private int buyNow;
    String orderId;

    private ActivityWebviewBinding binding;
    private ToolbarBinding toolbarBinding;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWebviewBinding.inflate(getLayoutInflater());
        toolbarBinding = ToolbarBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        settvImage();
        hideSearchNotification();
        setToolbarTheme();
        showBackButton();
        setScreenLayoutDirection();
        url = getIntent().getExtras().getString(RequestParamUtils.CHECKOUT_URL);
        thank_you_again = getIntent().getExtras().getString(RequestParamUtils.THANKYOUExtra);
        thank_you_url = getIntent().getExtras().getString(RequestParamUtils.THANKYOU);
        home_url = getIntent().getExtras().getString(RequestParamUtils.HOME_URL);
        buyNow = getIntent().getExtras().getInt(RequestParamUtils.buynow);
        binding.wvCheckOut.getSettings().setLoadsImagesAutomatically(true);
        binding.wvCheckOut.getSettings().setJavaScriptEnabled(true);
        binding.wvCheckOut.getSettings().setDomStorageEnabled(true);
        binding.wvCheckOut.setWebViewClient(new WebViewClient());
        binding.wvCheckOut.setWebChromeClient(new WebChromeClient());
        binding.wvCheckOut.addJavascriptInterface(new WebAppInterface(this), "Android");
        binding.wvCheckOut.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(binding.wvCheckOut, true);
        }
        WebView.setWebContentsDebuggingEnabled(true);

        binding.wvCheckOut.setWebViewClient(new myWebClient());
        CookieManager.getInstance().setAcceptCookie(true);

        try {
            JSONObject jsonObject = new JSONObject();
            String customerId = getPreferences().getString(RequestParamUtils.ID, "");
            jsonObject.put(RequestParamUtils.user_id, customerId);
            jsonObject.put(RequestParamUtils.cartItems, getCartDataForAPI());
            jsonObject.put(RequestParamUtils.os, RequestParamUtils.android);
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
            binding.wvCheckOut.postUrl(url, postData.getBytes());
            showProgress("");
            setToolbarTheme();
            binding.wvCheckOut.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }

        //Initiated Checkout Facebook

        List<Cart> cartList = databaseHelper.getFromCart(buyNow);
        for (int i = 0; i < cartList.size(); i++) {
            String product = cartList.get(i).getProduct();
            // get product detail via Gson from string
            CategoryList categoryListRider = new Gson().fromJson(product, new TypeToken<CategoryList>() {
            }.getType());

            //Initiated Checkout Facebook
//            if (categoryListRider.price != null && !categoryListRider.price.equals("")) {
//                logInitiatedCheckoutEvent(cartList.get(i).getProductid(), categoryListRider.name, cartList.get(i).getQuantity(), false, Constant.CURRENCYSYMBOL, Double.parseDouble(categoryListRider.price));
//            }
        }

        // Get system current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String formattedDate = df.format(calendar.getTime());

        // Save ABANDONED card details for facebook pixel
        SharedPreferences.Editor pre = getPreferences().edit();
        Gson gson = new Gson();
        String json = gson.toJson(cartList);
        //save Array list value in list
        pre.putString(RequestParamUtils.ABANDONED, json);
        pre.putString(RequestParamUtils.ABANDONEDTIME, formattedDate);
        pre.apply();

        //get value from string to array list
        String abandoned = getPreferences().getString(RequestParamUtils.ABANDONED, "");
        Type type = new TypeToken<List<Cart>>() {
        }.getType();
        List<Cart> connections = new Gson().fromJson(abandoned, type);

        Log.e(TAG, "formattedDate: " + getPreferences().getString(RequestParamUtils.ABANDONEDTIME, ""));
        Log.e(TAG, "shouldOverrideUrlLoading: " + connections.size());
    }

    public JSONArray getCartDataForAPI() {
        List<Cart> cartList = databaseHelper.getFromCart(buyNow);
        if (cartList.size() > 0) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < cartList.size(); i++) {
//                    String product = cartList.get(i).getProduct();

                    JSONObject object = new JSONObject();
                    object.put(RequestParamUtils.PRODUCT_ID, cartList.get(i).getProductid() + "");
                    if (new Ciyashop(WebViewActivity.this).getPreferences()) {
                        object.put(RequestParamUtils.quantity, new Ciyashop(WebViewActivity.this).getQuantity() + "");
                    } else {
                        object.put(RequestParamUtils.quantity, cartList.get(i).getQuantity() + "");
                    }

                    if (cartList.get(i).getVariation() != null) {
                        JSONObject ob1 = new JSONObject(cartList.get(i).getVariation());
                        object.put(RequestParamUtils.variation, ob1);
                    }
                    object.put(RequestParamUtils.variationId, cartList.get(i).getVariationid() + "");
                    jsonArray.put(object);
                }
                return jsonArray;
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        }
        return null;
    }

    public void setClickEvent() {
        toolbarBinding.ivBack.setOnClickListener(v -> {
            if (binding.wvCheckOut.canGoBack()) {
                binding.wvCheckOut.goBack();
            } else {
                CookieManager.getInstance().removeAllCookie();
                binding.wvCheckOut.clearCache(true);
                binding.wvCheckOut.clearHistory();
                clearCookies(WebViewActivity.this);
                logout();
            }
        });
    }

    //Custom WebViewClient
    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            Log.e("Response", "" + url);
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
                if (isFirstLoad) {
                    List<Cart> cartList = databaseHelper.getFromCart(buyNow);
                    for (int i = 0; i < cartList.size(); i++) {
                        String product = cartList.get(i).getProduct();
                        CategoryList categoryListRider = new Gson().fromJson(product, new TypeToken<CategoryList>() {
                        }.getType());

//                        try {
//                            logPurchasedEvent(cartList.get(i).getQuantity(), categoryListRider.name, cartList.get(i).getProductid(), Constant.CURRENCYCODE, Double.parseDouble(categoryListRider.price));
//                        } catch (Exception e) {
//                            Log.e("Exception =", e.getMessage());
//                        }
                    }

                    SharedPreferences.Editor pre = getPreferences().edit();
                    pre.putString(RequestParamUtils.ABANDONED, "");
                    pre.putString(RequestParamUtils.ABANDONEDTIME, "");
                    pre.apply();

                    if (url.contains("cancel_order=true")) {
                        Intent intent = new Intent(WebViewActivity.this, OrderCancelActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        if (url.contains("app_checkout_order_id")) {
                            String[] items = url.split("app_checkout_order_id=");
                            Log.e(TAG, "onPageStarted: " + items[1]);
                            orderId = items[0];
                            LatLong();
                        } else {
                        }
                    }

                    binding.wvCheckOut.clearCache(true);
                    binding.wvCheckOut.clearHistory();
                    clearCookies(WebViewActivity.this);
                    isFirstLoad = false;
                } else {
                    Log.e("Else Condition ", "Called");
                }
            } else if (home_url != null && url.contains(home_url)) {
//                Toast.makeText(WebviewActivity.this,"Something went wrong ...try after Somnetime or Contact Admin",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
//            view.loadUrl(url);

            if (url.contains("http")) {
                view.loadUrl(url);
            }
            Log.e("URL GET = ", url);
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
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && binding.wvCheckOut.canGoBack()) {
            binding.wvCheckOut.goBack();
            return true;
        } else {
            CookieManager.getInstance().removeAllCookie();
            binding.wvCheckOut.clearCache(true);
            binding.wvCheckOut.clearHistory();
            clearCookies(WebViewActivity.this);
            logout();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void logout() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, "logout", this, getlanuage());
            postApi.callPostApi(new URLS().LOGOUT, "");
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void LatLong() {
        if (Utils.isInternetConnected(this)) {
            try {
//                JSONObject jsonObject = new JSONObject();
                JSONObject metadata = new JSONObject();
                JSONObject lat = new JSONObject();
                lat.put("key", "pgsdb_order_destination_lat");
                lat.put("value", "21.2085796");
                JSONObject log = new JSONObject();
                log.put("key", "pgsdb_order_destination_long");
                log.put("value", "72.7734316");
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(lat);
                jsonArray.put(log);
                metadata.put("meta_data", jsonArray);
                PostApi postApi = new PostApi(this, RequestParamUtils.ORDERID, this, getlanuage());
                postApi.callPostApi(new URLS().ORDERS + orderId, metadata.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            showProgress("");
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
                    } else {
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        } else if (methodName.equals(RequestParamUtils.ORDERID)) {
            Intent intent = new Intent(WebViewActivity.this, ThankYouActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (response != null && response.length() > 0) {
                Log.e(TAG, "onResponse: *****************");
            }
        }
    }

    public void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.e("log", "Using clearCookies code for API >=" + Build.VERSION_CODES.LOLLIPOP_MR1);
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.e("log", "Using clearCookies code for API <" + Build.VERSION_CODES.LOLLIPOP_MR1);
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(final String toast) {
            Log.e("Title is ", toast);

            runOnUiThread(() -> {
                dismissProgress();
                if (toast != null) {
                    CookieManager.getInstance().setAcceptCookie(true);
                    binding.wvCheckOut.loadUrl(toast);
                    binding.wvCheckOut.setVisibility(View.VISIBLE);
                    isFirstLoad = true;
                    dismissProgress();
                }
            });
        }
    }
}
