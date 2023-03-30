package com.example.ciyashop.activity;

import android.content.Intent;
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
import com.example.ciyashop.adapter.MyOrderAdapter;
import com.example.ciyashop.databinding.ActivityMyOrderBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.databinding.LayoutOrderPlaceholderBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Orders;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyOrderActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {

    private ActivityMyOrderBinding binding;
    private LayoutEmptyBinding emptyBinding;
    private LayoutOrderPlaceholderBinding orderPlaceholderBinding;

    List<Orders> list = new ArrayList<>();
    int pastVisibleItems, visibleItemCount, totalItemCount;
    Boolean setNoItemFound = false;
    private MyOrderAdapter myOrderAdapter;
    private int page = 1;
    private boolean loading = true;
    private boolean Splashscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMyOrderBinding.inflate(getLayoutInflater());
        emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        orderPlaceholderBinding = LayoutOrderPlaceholderBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());
        emptyBinding.tvEmptyTitle.setText(R.string.no_order_found);
        setToolbarTheme();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.my_orders));

        if (getPreferences().getString(RequestParamUtils.ID, "").equals("")) {
            Intent myOrderIntent = new Intent(this, LogInActivity.class);
            startActivity(myOrderIntent);
        }

        showBackButton();
        setEmptyColor();
        hideSearchNotification();
        setMyOrderAdapter();
        myOrder(true);
        Intent intent = getIntent();
        if (intent.hasExtra(RequestParamUtils.Splashscreen)) {
            Splashscreen = intent.getBooleanExtra(RequestParamUtils.Splashscreen, true);
        } else {
            Splashscreen = false;
        }
        ivBack.setOnClickListener(view -> backPressed());
    }

    public void setMyOrderAdapter() {
        myOrderAdapter = new MyOrderAdapter(this, this);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvMyOrders.setLayoutManager(mLayoutManager);
        binding.rvMyOrders.setAdapter(myOrderAdapter);
        binding.rvMyOrders.setNestedScrollingEnabled(false);
        binding.rvMyOrders.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                myOrder(false);
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

    public void myOrder(boolean loaderShow) {
        if (Utils.isInternetConnected(this)) {
            if (loaderShow) {
                //showProgress("");
                if (Config.SHIMMER_VIEW) {
                    orderPlaceholderBinding.shimmerViewContainer.startShimmer();
                    orderPlaceholderBinding.shimmerViewContainer.setVisibility(View.VISIBLE);
                } else {
                    orderPlaceholderBinding.shimmerViewContainer.setVisibility(View.GONE);
                    showProgress("");
                }
            }
            PostApi postApi = new PostApi(this, RequestParamUtils.orders, this, getlanuage());
            JSONObject object = new JSONObject();
            try {
                object.put(RequestParamUtils.PAGE, page);
                String customerId = getPreferences().getString(RequestParamUtils.ID, "");
                object.put(RequestParamUtils.customer, customerId);
                postApi.callPostApi(new URLS().ORDERS + getPreferences().getString(RequestParamUtils.CurrencyText, ""), object.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.orders)) {
            // dismissProgress();
            if (Config.SHIMMER_VIEW) {
                orderPlaceholderBinding.shimmerViewContainer.stopShimmer();
                orderPlaceholderBinding.shimmerViewContainer.setVisibility(View.GONE);
            } else {
                dismissProgress();
            }
            if (response != null && response.length() > 0) {
                try {
                    //set call here
                    loading = true;
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String jsonResponse = jsonArray.get(i).toString();
                        Orders categoryListRider = new Gson().fromJson(
                                jsonResponse, new TypeToken<Orders>() {
                                }.getType());
                        list.add(categoryListRider);
                    }
                    myOrderAdapter.addAll(list);
                    if (list.size() > 0) {
                        emptyBinding.llEmpty.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getString("status").equals("error")) {
                            setNoItemFound = true;
                            if (myOrderAdapter.getItemCount() == 0) {
                                emptyBinding.llEmpty.setVisibility(View.VISIBLE);
                                emptyBinding.tvEmptyTitle.setText(R.string.no_order_found);
                                emptyBinding.tvContinueShopping.setOnClickListener(view -> finish());
                            }
                        }
                    } catch (JSONException e1) {
                        Log.e("noProductJSONException", e1.getMessage());
                    }
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
            } else {
                emptyBinding.llEmpty.setVisibility(View.VISIBLE);
                emptyBinding.tvEmptyTitle.setText(R.string.no_order_found);
                emptyBinding.tvContinueShopping.setOnClickListener(view -> finish());
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (getPreferences().getString(RequestParamUtils.ID, "").equals("")) {
            finish();
        }
        page = 0;
        list = new ArrayList<>();
        myOrder(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed();
    }

    public void backPressed() {
        if (Splashscreen) {
            Intent intent = new Intent(MyOrderActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
