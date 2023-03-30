package com.example.ciyashop.activity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.ciyashop.library.apicall.GetApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.DownloadAdapter;
import com.example.ciyashop.databinding.ActivityDownloadBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Download;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    private ActivityDownloadBinding binding;
    private LayoutEmptyBinding emptyBinding;
    DownloadAdapter downloadAdapter;
    List<Download> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadBinding.inflate(getLayoutInflater());
        emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());
        setDownloadAdapter();
        getDownloadProducts();
        settvTitle(getString(R.string.download));
        setToolbarTheme();
        setScreenLayoutDirection();
        showBackButton();
        hideSearchNotification();
        setThemeColor();
    }

    void setDownloadAdapter() {
        downloadAdapter = new DownloadAdapter(this, this);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvDownload.setLayoutManager(mLayoutManager);
        binding.rvDownload.setAdapter(downloadAdapter);
        binding.rvDownload.setNestedScrollingEnabled(false);
    }

    public void setThemeColor() {

        TextView tvContinueShopping = findViewById(R.id.tvContinueShopping);
       // ImageView ivGo = findViewById(R.id.ivGo);
        tvContinueShopping.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setStroke(5, Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvContinueShopping.setBackground(gradientDrawable);
      //  ivGo.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    void getDownloadProducts() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            GetApi getApi = new GetApi(this, RequestParamUtils.getDownloads, this, getlanuage());
            String customerId = getPreferences().getString(RequestParamUtils.ID, "");
            getApi.callGetApi(URLS.WOO_MAIN_URL + new URLS().WOO_CUSTOMERS + "/" + customerId + "/" + new URLS().WOO_DOWNLOADS_URL);
            Log.e("TAG", "getDownloadcall: "+URLS.WOO_MAIN_URL + new URLS().WOO_CUSTOMERS + "/" + customerId + "/" + new URLS().WOO_DOWNLOADS_URL );
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        dismissProgress();
        if (methodName.endsWith(RequestParamUtils.getDownloads)) {
            Log.e("getDownload", response);
            try {
                JSONArray jsonArray = new JSONArray(response);
                list = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String jsonResponse = jsonArray.get(i).toString();
                    Download downloadListRider = new Gson().fromJson(
                            jsonResponse, new TypeToken<Download>() {
                            }.getType());
                    list.add(downloadListRider);
                    Log.e("getDownload",new Gson().toJson(downloadListRider));
                }
                downloadAdapter.addAll(list);
                if (list.size() == 0) {
                    showEmptyLayout();
                } else {
                    emptyBinding.llEmpty.setVisibility(View.GONE);
                }
                dismissProgress();
            } catch (Exception e) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getString("message").equals("No product found")) {
//                        setNoItemFound = true;
                        if (downloadAdapter.getItemCount() == 0) {
                            showEmptyLayout();
                        }
                    }
                } catch (JSONException e1) {
                    Log.e("noProductJSONException", e1.getMessage());
                }
                Log.e(methodName + "Gson Exception is ", e.getMessage());
                dismissProgress();
            }
        }
    }

    public void showEmptyLayout() {
        emptyBinding.llEmpty.setVisibility(View.VISIBLE);
        emptyBinding.tvEmptyTitle.setText(getString(R.string.no_product_found));
        emptyBinding.tvContinueShopping.setOnClickListener(view -> finish());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getDownloadProducts();
    }
}
