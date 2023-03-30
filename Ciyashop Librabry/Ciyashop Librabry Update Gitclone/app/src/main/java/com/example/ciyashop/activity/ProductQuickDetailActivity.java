package com.example.ciyashop.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.ciyashop.databinding.ActivityProductQuickDetailBinding;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

public class ProductQuickDetailActivity extends BaseActivity {

    private ActivityProductQuickDetailBinding binding;

    public static String changedHeaderHtml(String htmlText) {
        String head = "<head><meta name=\"viewport\" content=\"width=device-width, user-scalable=yes\" /></head>";
        String closedTag = "</body></html>";
        return head + htmlText + closedTag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProductQuickDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbarTheme();
        hideSearchNotification();
        setScreenLayoutDirection();

        String description = getIntent().getExtras().getString(RequestParamUtils.description);
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //            tvDescription.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));
        //        } else {
        //            tvDescription.setText(Html.fromHtml(description));
        //        }
        binding.tvDescription.setHtml(description,
                new HtmlHttpImageGetter(binding.tvDescription));

        if (!description.equals("")) {
            binding.wvDetail.setInitialScale(1);

            binding.wvDetail.getSettings().setLoadsImagesAutomatically(true);
            binding.wvDetail.getSettings().setUseWideViewPort(true);
            binding.wvDetail.loadData(changedHeaderHtml(description), "text/html", "UTF-8");
            /* binding.wvDetail.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);*/
            binding.wvDetail.setInitialScale(1);
            binding.wvDetail.getSettings().setLoadWithOverviewMode(true);
            binding.wvDetail.getSettings().setUseWideViewPort(true);
            binding.wvDetail.getSettings().setBuiltInZoomControls(true);
        }
        String subTitle = getIntent().getExtras().getString(RequestParamUtils.title);
        String productImage = getIntent().getExtras().getString(RequestParamUtils.image);
        String productName = getIntent().getExtras().getString(RequestParamUtils.name);

        settvTitle(subTitle);
        binding.tvSubTitle.setText(subTitle);
        binding.tvProductName.setText(productName);
        binding.tvProductName.setTextColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));

        if (productImage.length() > 0) {
            binding.ivProduct.setVisibility(View.VISIBLE);
            Glide.with(this).load(productImage).into(binding.ivProduct);
        } else {
            binding.ivProduct.setVisibility(View.INVISIBLE);
        }
        showBackButton();
    }
}
