package com.example.ciyashop.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;

import com.example.ciyashop.R;
import com.example.ciyashop.databinding.ActivitySellerMoreInfoBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.RequestParamUtils;

public class SellerMoreInfoActivity extends BaseActivity {
    private ActivitySellerMoreInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySellerMoreInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String dealerName = getIntent().getExtras().getString(RequestParamUtils.Dealer);
        if (dealerName != null) {
            settvTitle(dealerName);
        } else {
            settvTitle(RequestParamUtils.Dealer);
        }
        setToolbarTheme();
        showBackButton();
        String data = getIntent().getExtras().getString(RequestParamUtils.data);
        if (data == null || data.equals("")) {
            binding.tvContentDesc.setText(R.string.no_data_found);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvContentDesc.setText(Html.fromHtml(data, Html.FROM_HTML_MODE_COMPACT));
            } else {
                binding.tvContentDesc.setText(Html.fromHtml(data));
            }
        }
    }
}
