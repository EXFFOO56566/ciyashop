package com.example.ciyashop.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.WalletTransactionAdapter;
import com.example.ciyashop.customview.textview.TextViewRegular;

import com.example.ciyashop.databinding.ActivityWalletTeansectionBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.databinding.LayoutShimmerWalletBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.WalletTransaction;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WalletTransactionActivity extends BaseActivity implements OnResponseListner, OnItemClickListener {

     ActivityWalletTeansectionBinding binding;
     LayoutShimmerWalletBinding walletBinding;
    // LayoutEmptyBinding emptyBinding;

    //Todo : global variable
    private String customerId;
    WalletTransactionAdapter walletTransactionAdapter;
    List<WalletTransaction.Transaction> list = new ArrayList<>();
    private String THANKYOU, THANKYOUMAIN;
    String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWalletTeansectionBinding.inflate(getLayoutInflater());
        walletBinding = LayoutShimmerWalletBinding.bind(binding.getRoot());
       // emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        setToolbarTheme();
     //   setEmptyColor();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.my_wallet));
        hideSearchNotification();
        showBackButton();
        setClickEvent();
        customerId = getPreferences().getString(RequestParamUtils.ID, "");
    }

    public void setToolbarTheme() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getPreferences().getString(Constant.HEADER_COLOR, Constant.HEAD_COLOR)));
        }
    }

    public void setEmptyColor() {
        TextView tvContinueShopping = findViewById(R.id.tvContinueShopping);
        //   ImageView ivGo = findViewById(R.id.ivGo);
        tvContinueShopping.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setStroke(5, Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvContinueShopping.setBackground(gradientDrawable);
        //  ivGo.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    public void getWalletTransactionData() {
        if (Utils.isInternetConnected(this)) {
            //showProgress("");
            if (Config.SHIMMER_VIEW) {
                walletBinding.shimmerviewwallet.startShimmer();
                walletBinding.shimmerviewwallet.setVisibility(View.VISIBLE);
            } else {
                walletBinding.shimmerviewwallet.setVisibility(View.GONE);
                showProgress("");
            }
            try {
                PostApi postApi = new PostApi(this, RequestParamUtils.Wallet, this, getlanuage());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.USER_ID, customerId);
                postApi.callPostApi(new URLS().WALLET + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("walletTransaction", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    public void setWalletTransactionData() {
        walletTransactionAdapter = new WalletTransactionAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvWalletTransection.setLayoutManager(mLayoutManager);
        binding.rvWalletTransection.setAdapter(walletTransactionAdapter);
        binding.rvWalletTransection.setNestedScrollingEnabled(false);
    }

    public void showEmpty() {
        binding.llEmptyWallet.setVisibility(View.VISIBLE);
      //  binding.tvEmptyTitle.setText(R.string.no_notification_yet);
    }

    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.Wallet)) {
            if (response != null && response.length() > 0) {
                if (Config.SHIMMER_VIEW) {
                    walletBinding.shimmerviewwallet.stopShimmer();
                    walletBinding.shimmerviewwallet.setVisibility(View.GONE);
                } else {
                    dismissProgress();
                }
                try {
                    final WalletTransaction walletTransaction = new Gson().fromJson(
                            response, new TypeToken<WalletTransaction>() {
                            }.getType());
                    if (walletTransaction.status.equals("success")) {
                        binding.llEmptyWallet.setVisibility(View.GONE);
                        list = new ArrayList<>();
                        Log.e("---------------", "onResponse: " + walletTransaction.getTransactions().size());
                        list.addAll(walletTransaction.getTransactions());
                        if (walletTransaction.getTransactions().size() > 0) {
                            binding.llEmptyWallet.setVisibility(View.GONE);

                        } else {
                            binding.llEmptyWallet.setVisibility(View.VISIBLE);
                        }

                        walletTransactionAdapter.addAll(list);
                        URL = walletTransaction.getTopupPage();
                        THANKYOUMAIN = walletTransaction.getThankyou();
                        THANKYOU = walletTransaction.getThankyouEndpoint();
                        if (!THANKYOUMAIN.isEmpty()) {
                            Constant.CheckoutURL.add(THANKYOUMAIN);
                        }
                        if (!THANKYOU.isEmpty()) {
                            Constant.CheckoutURL.add(THANKYOU);
                        }
                    } else {
                        showEmpty();
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    public void setClickEvent() {
        binding.icAddTransection.setOnClickListener(v -> {
            Intent i = new Intent(WalletTransactionActivity.this, WebViewWalletAddActivity.class);
            i.putExtra(RequestParamUtils.TRANSECTION_URL, URL);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWalletTransactionData();
        setWalletTransactionData();
    }
}
