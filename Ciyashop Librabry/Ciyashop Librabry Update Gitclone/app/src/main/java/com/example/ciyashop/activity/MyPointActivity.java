package com.example.ciyashop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.MyPointsAdapter;
import com.example.ciyashop.customview.CustomLinearLayoutManager;
import com.example.ciyashop.databinding.ActivityMyPointBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.MyPoint;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

public class MyPointActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    private MyPointsAdapter myPointSAdapter;
    private Bundle bundle;
    private String userId;
    private boolean loading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    Boolean setNoItemFound = false;
    private int page = 1;
    CustomLinearLayoutManager mLayoutManager;

    private ActivityMyPointBinding binding;
    private LayoutEmptyBinding emptyBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMyPointBinding.inflate(getLayoutInflater());
        emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        settvTitle(getString(R.string.my_point));
        hideSearchNotification();
        showBackButton();
        setToolbarTheme();
        setPointAdapter();
        getIntentData();
        getMyPoint(page, true);
        binding.tvMyPoint.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void getIntentData() {
        bundle = getIntent().getExtras();
        if (bundle != null) {
            userId = bundle.getString(RequestParamUtils.USER_ID, "");
        }
    }

    public void setPointAdapter() {
        myPointSAdapter = new MyPointsAdapter(this, this);
        mLayoutManager = new CustomLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvMyPoints.setLayoutManager(mLayoutManager);
        binding.rvMyPoints.setAdapter(myPointSAdapter);
        binding.rvMyPoints.setNestedScrollingEnabled(false);
        binding.rvMyPoints.setHasFixedSize(true);
        binding.rvMyPoints.setItemViewCacheSize(20);
        binding.rvMyPoints.setDrawingCacheEnabled(true);
        binding.rvMyPoints.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.rvMyPoints.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                getMyPoint(page, true);
                                //Do pagination.. i.e. fetch new data
                            }
                        }
                    }
                }
            }
        });
    }

    public void getMyPoint(int page, boolean isDialogShow) {
        if (Utils.isInternetConnected(this)) {
            if (isDialogShow) {
                showProgress("");
            }
            PostApi postApi = new PostApi(this, RequestParamUtils.getMyPoint, this, getlanuage());
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(RequestParamUtils.USER_ID, userId);
                jsonObject.put(RequestParamUtils.PAGE, page);
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
            postApi.callPostApi(new URLS().REWARDSPOINT, jsonObject.toString());
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    @Override
    public void onResponse(final String response, String methodName) {
        dismissProgress();
        if (methodName.equals(RequestParamUtils.getMyPoint)) {
            if (response != null && response.length() > 0) {
                try {
                    MyPoint myPointRider = new Gson().fromJson(
                            response, new TypeToken<MyPoint>() {
                            }.getType());
                    showData();
                    if (myPointRider.status.equals("success")) {
                        loading = true;
                        binding.tvMyPoint.setText(String.valueOf(myPointRider.data.pointsBalance));
                        myPointSAdapter.addAll(myPointRider.data.events);
                        if (myPointRider.data.events.size() > 0) {
                            if (Integer.parseInt(myPointRider.data.totalRows) > myPointSAdapter.getList().size() &&
                                    myPointSAdapter.getList().size() < 20) {
                                page = page + 1;
                                getMyPoint(page, false);
                            }
                        } else {
                            setNoItemFound = true;
                        }
                    } else {
                        setNoItemFound = true;
                    }

                } catch (Exception e) {
                    dismissProgress();
                    showNoData();
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
            }
        }
    }

    public void showNoData() {
        emptyBinding.llEmpty.setVisibility(View.VISIBLE);
        binding.llMyPoint.setVisibility(View.GONE);
        emptyBinding.tvEmptyTitle.setText(R.string.no_points_earned);
        emptyBinding.tvEmptyDesc.setText(R.string.purchase_product_and_erned_pointa);
        emptyBinding.tvContinueShopping.setOnClickListener(view -> {
            finish();
            Intent i = new Intent(MyPointActivity.this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    public void showData() {
        emptyBinding.llEmpty.setVisibility(View.GONE);
        binding.llMyPoint.setVisibility(View.VISIBLE);
    }

}
