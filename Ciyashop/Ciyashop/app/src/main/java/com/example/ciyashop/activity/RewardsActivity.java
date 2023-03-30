package com.example.ciyashop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.MyRewardsAdapter;
import com.example.ciyashop.databinding.ActivityRewardsBinding;
import com.example.ciyashop.databinding.ItemRewardPlaceholderBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.FilterSelectedList;
import com.example.ciyashop.model.Rewards;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RewardsActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    int pastVisibleItems, visibleItemCount, totalItemCount;
    Boolean setNoItemFound = false;
    private MyRewardsAdapter myRewardsAdapter;
    private int page = 1;
    private final List<Rewards> list = new ArrayList<>();
    private boolean loading = true;
    private boolean Splashscreen = false;

    private String refreshedToken;
    private ActivityRewardsBinding binding;
    private LayoutEmptyBinding emptyBinding;
    private ItemRewardPlaceholderBinding rewardPlaceholderBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRewardsBinding.inflate(getLayoutInflater());
        emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        rewardPlaceholderBinding = ItemRewardPlaceholderBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        setToolbarTheme();
        hideSearchNotification();
        settvTitle(getResources().getString(R.string.my_reward));
        showBackButton();
        myRewards(true);
        seMyRewardAdapter();
        setScreenLayoutDirection();
        setEmptyColor();

        Intent intent = getIntent();
        if (intent.hasExtra(RequestParamUtils.Splashscreen)) {
            Splashscreen = intent.getBooleanExtra(RequestParamUtils.Splashscreen, true);
        } else {
            Splashscreen = false;
        }
        ivBack.setOnClickListener(view -> backPressed());
    }

    public void setEmptyColor() {
//        TextViewRegular tvContinueShopping = findViewById(R.id.tvContinueShopping);
//       // ImageView ivGo = findViewById(R.id.ivGo);
//        tvContinueShopping.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
//        GradientDrawable gradientDrawable = new GradientDrawable();
//        gradientDrawable.setStroke(5, Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
//        tvContinueShopping.setBackground(gradientDrawable);
        // ivGo.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        TextView tvContinueShopping = findViewById(R.id.tvContinueShopping);
        TextView tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        TextView tvEmptyDesc = findViewById(R.id.tvEmptyDesc);
        //ImageView ivGo = findViewById(R.id.ivGo);
        Drawable unwrappedDrawable = tvContinueShopping.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        tvEmptyTitle.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvEmptyDesc.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void seMyRewardAdapter() {
        myRewardsAdapter = new MyRewardsAdapter(this, this);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvMyRewards.setLayoutManager(mLayoutManager);
        binding.rvMyRewards.setAdapter(myRewardsAdapter);
        binding.rvMyRewards.setNestedScrollingEnabled(false);
        binding.rvMyRewards.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {   //check for scroll down
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            if (!setNoItemFound) {
                                loading = false;
                                page = page + 1;
                                Log.e("End ", "Last Item Wow  and page no:- " + page);
                                myRewards(false);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    public void myRewards(Boolean dialog) {
        if (Utils.isInternetConnected(this)) {
            if (dialog) {
                //showProgress("");
                if (Config.SHIMMER_VIEW) {
                    rewardPlaceholderBinding.shimmerViewContainer.startShimmer();
                    rewardPlaceholderBinding.shimmerViewContainer.setVisibility(View.VISIBLE);
                } else {
                    rewardPlaceholderBinding.shimmerViewContainer.setVisibility(View.GONE);
                    showProgress("");
                }
            }

            PostApi postApi = new PostApi(this, RequestParamUtils.coupons, this, getlanuage());
            try {
                JSONObject jsonObject;
                if (FilterSelectedList.filterJson.equals("")) {
                    jsonObject = new JSONObject();
                } else {
                    jsonObject = new JSONObject(FilterSelectedList.filterJson);
                }
                if (Constant.DEVICE_TOKEN == null || Constant.DEVICE_TOKEN.equals("")) {
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.isComplete()) {
                            refreshedToken = task.getResult();
                            Constant.DEVICE_TOKEN = refreshedToken;
                        }
                    });
                }
                jsonObject.put(RequestParamUtils.PAGE, page);
                jsonObject.put(RequestParamUtils.DEVICE_TOKEN, Constant.DEVICE_TOKEN);
                jsonObject.put(RequestParamUtils.USER_ID, getPreferences().getString(RequestParamUtils.ID, ""));
                postApi.callPostApi(new URLS().REWARDS, jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.coupons)) {
            //dismissProgress();
            if (Config.SHIMMER_VIEW) {
                rewardPlaceholderBinding.shimmerViewContainer.stopShimmer();
                rewardPlaceholderBinding.shimmerViewContainer.setVisibility(View.GONE);
            } else {
                dismissProgress();
            }
            if (response != null && response.length() > 0) {
                try {
                    //set call here
                    loading = true;
                    Rewards rewardsRider = new Gson().fromJson(
                            response, new TypeToken<Rewards>() {
                            }.getType());
                    myRewardsAdapter.addAll(rewardsRider.data);
                    Log.e("TAG", "onResponse: " + new Gson().toJson(rewardsRider.data));
                    if (myRewardsAdapter.getList().size() == 0) {
                        setNoItemFound = true;
                        if (myRewardsAdapter.getItemCount() == 0) {
                            noCouponFound();
                        }
                    } else {
                        emptyBinding.llEmpty.setVisibility(View.GONE);
                        emptyBinding.tvEmptyTitle.setText(R.string.no_coupon_found);
                        binding.rvMyRewards.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    try {
                        JSONObject object = new JSONObject(response);
//                        if (object.getString("message").equals("No Coupons found")) {
//
//                        }
                        noCouponFound();
                    } catch (JSONException e1) {
                        Log.e("noProductJSONException", e1.getMessage());
                    }
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
            }
        }
    }

    public void noCouponFound() {
        setNoItemFound = true;
        if (myRewardsAdapter.getItemCount() == 0) {
            emptyBinding.llEmpty.setVisibility(View.VISIBLE);
            emptyBinding.tvEmptyTitle.setText(R.string.no_coupon_found);
            emptyBinding.tvContinueShopping.setOnClickListener(view -> finish());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed();
    }

    public void backPressed() {
        if (Splashscreen) {
            Intent intent = new Intent(RewardsActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
