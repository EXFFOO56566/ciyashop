package com.example.ciyashop.activity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ciyashop.adapter.SellerReviewAdapter;
import com.example.ciyashop.databinding.ActivitySellerReviewBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.SellerData;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.RequestParamUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class SellerReviewActivity extends BaseActivity implements OnItemClickListener {

    private ActivitySellerReviewBinding binding;
    private SellerReviewAdapter sellerReviewAdapter;
    List<SellerData.SellerInfo.ReviewList> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySellerReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settvTitle(RequestParamUtils.vendorReview);
        showBackButton();
        setToolbarTheme();

        String listData = getIntent().getExtras().getString(RequestParamUtils.sellerInfo);
        SellerData sellerDataRider = new Gson().fromJson(
                listData, new TypeToken<SellerData>() {
                }.getType());

        if(!sellerDataRider.sellerInfo.reviewList.isEmpty()){
            list.addAll(sellerDataRider.sellerInfo.reviewList);
        }
        setReviewData();
    }

    public void setReviewData() {
        sellerReviewAdapter = new SellerReviewAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvReview.setLayoutManager(mLayoutManager);
        binding.rvReview.setAdapter(sellerReviewAdapter);
        binding.rvReview.setNestedScrollingEnabled(false);
        sellerReviewAdapter.addAll(list);
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }
}
