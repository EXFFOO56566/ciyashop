package com.example.ciyashop.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.example.ciyashop.R;
import com.example.ciyashop.customview.pulltozoom.PullToZoomScrollViewEx;
import com.example.ciyashop.databinding.ActivityBlogDiscriptionBinding;
import com.example.ciyashop.databinding.BlogContentBinding;
import com.example.ciyashop.databinding.ProfileZoomViewBinding;
import com.example.ciyashop.utils.BaseActivity;

public class BlogDescriptionActivity extends BaseActivity {

    private ActivityBlogDiscriptionBinding binding;
    private ProfileZoomViewBinding profileZoomViewBinding;
    private BlogContentBinding blogContentBinding;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBlogDiscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();
        loadViewForCode();
//        showBackButton();
        setScreenLayoutDirection();
        getIntentData();
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        int mScreenWidth = localDisplayMetrics.widthPixels;
        LinearLayout.LayoutParams localObject = new LinearLayout.LayoutParams(mScreenWidth, (int) (9.0F * (mScreenWidth / 13.0F)));
        binding.scrollView.setHeaderLayoutParams(localObject);
    }

    private void loadViewForCode() {
        PullToZoomScrollViewEx scrollView = findViewById(R.id.scroll_view);
        profileZoomViewBinding = ProfileZoomViewBinding.inflate(getLayoutInflater());
        View zoomView = profileZoomViewBinding.getRoot();
        blogContentBinding = BlogContentBinding.inflate(getLayoutInflater());
        View contentView = blogContentBinding.getRoot();
        scrollView.setZoomView(zoomView);
        scrollView.setScrollContentView(contentView);
    }

    public void setClickEvent() {
        blogContentBinding.ivShare.setOnClickListener(v -> {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                String shareMessage = "\n" + blogContentBinding.tvBlogTitle.getText().toString() + "\n\n";
                shareMessage = shareMessage + bundle.getString("link") + "\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            } catch (Exception e) {
                Log.e("Exception is ", e.getMessage());
            }
        });
    }

    private void getIntentData() {
        bundle = getIntent().getExtras();
        if (bundle == null) return;
        blogContentBinding.tvBlogDate.setText(bundle.getString("date"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            blogContentBinding.tvBlogContent.setText(Html.fromHtml(bundle.getString("description"), Html.FROM_HTML_MODE_COMPACT));
        } else {
            blogContentBinding.tvBlogTitle.setText(Html.fromHtml(bundle.getString("name")));
            blogContentBinding.tvBlogContent.setText(Html.fromHtml(bundle.getString("description")));
        }
        if (bundle.getString("image") != null)
            Glide.with(this)
                    .asBitmap().format(DecodeFormat.PREFER_ARGB_8888)
                    .error(R.drawable.no_image_available)
                    .load(bundle.getString("image"))
                    .into(profileZoomViewBinding.ivZoom);
    }
}
