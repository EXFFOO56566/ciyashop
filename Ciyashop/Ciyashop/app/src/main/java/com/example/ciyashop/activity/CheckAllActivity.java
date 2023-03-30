package com.example.ciyashop.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.ciyashop.library.apicall.GetApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.ReviewAdapter;
import com.example.ciyashop.databinding.ActivityCheckAllBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.ProductReview;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class CheckAllActivity extends BaseActivity implements OnResponseListner, OnItemClickListener {

    private ReviewAdapter reviewAdapter;
    private final CategoryList categoryList = Constant.CATEGORYDETAIL;

    private ActivityCheckAllBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCheckAllBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbarTheme();
        hideSearchNotification();
        settvTitle(getResources().getString(R.string.review));
        showBackButton();
        getReview();
        setReviewData();
    }

    public void setReviewData() {
        reviewAdapter = new ReviewAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvAllReview.setLayoutManager(mLayoutManager);
        binding.rvAllReview.setAdapter(reviewAdapter);
        binding.rvAllReview.setNestedScrollingEnabled(false);
    }

    public void getReview() {
        if (Utils.isInternetConnected(this)) {
            GetApi getApi = new GetApi(this, RequestParamUtils.getReview, this, getlanuage());
            new URLS();
            getApi.callGetApi(URLS.WOO_MAIN_URL + new URLS().WOO_PRODUCT_URL + "/" + categoryList.id + "/" + new URLS().WOO_REVIEWS);
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {

        dismissProgress();
        if (methodName.equals(RequestParamUtils.getReview)) {
            if (response != null && response.length() > 0) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<ProductReview> reviewList = new ArrayList<>();
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String jsonResponse = jsonArray.get(i).toString();
                            ProductReview productReviewRider = new Gson().fromJson(
                                    jsonResponse, new TypeToken<ProductReview>() {
                                    }.getType());
                            reviewList.add(productReviewRider);
                        }
                    }
                    reviewAdapter.addAll(reviewList);

                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
            }
        }
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }
}
