package com.example.ciyashop.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.example.ciyashop.R;
import com.example.ciyashop.databinding.LayoutDetailTwoBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.google.android.material.appbar.AppBarLayout;

public class TestActivity extends BaseActivity {

    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    private SellerInfoActivity.State mCurrentState = SellerInfoActivity.State.IDLE;
    private LayoutDetailTwoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = LayoutDetailTwoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initCollapsingToolbar();
    }

    private void initCollapsingToolbar() {
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, i) -> {
            if (i == 0) {
                if (mCurrentState != SellerInfoActivity.State.EXPANDED) {
                    binding.toolbar.setBackgroundColor(Color.TRANSPARENT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.TRANSPARENT);
                    }
                }
                mCurrentState = SellerInfoActivity.State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout1.getTotalScrollRange()) {
                if (mCurrentState != SellerInfoActivity.State.COLLAPSED) {
                    binding.toolbar.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
                    }
                }
                mCurrentState = SellerInfoActivity.State.COLLAPSED;
            } else {
                if (mCurrentState != SellerInfoActivity.State.IDLE) {
                    binding.toolbar.setBackgroundColor(Color.TRANSPARENT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.TRANSPARENT);
                    }
                }
                mCurrentState = SellerInfoActivity.State.IDLE;
            }
        });
    }
}