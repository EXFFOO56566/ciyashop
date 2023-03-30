package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CategoryListActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;


public class VerticalBannerAdapter extends PagerAdapter {
    private List<Home.BannerAd> list = new ArrayList<>();

    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;

    public VerticalBannerAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Home.BannerAd> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_vertical_banner, container, false);
        container.addView(view);
        LinearLayout llMain = view.findViewById(R.id.llMain);

        ImageView ivBanner = view.findViewById(R.id.ivBanner);
//        llMain.getLayoutParams().height = height;
        llMain.setOnClickListener(view1 -> {
            Intent intent = new Intent(activity, CategoryListActivity.class);
            intent.putExtra(RequestParamUtils.CATEGORY, list.get(position).bannerAdCatId);
            intent.putExtra(RequestParamUtils.IS_WISHLIST_ACTIVE, Constant.IS_WISH_LIST_ACTIVE);
            activity.startActivity(intent);
        });

        if (list.get(position).bannerAdImageUrl != null && !list.get(position).bannerAdImageUrl.equals("")) {
            //ivBanner.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(activity.getBaseContext())
                    .load(list.get(position).bannerAdImageUrl)
                    //.override(510,650)
                    .fitCenter()
                    .error(R.drawable.no_image_available)
                    .transform(new RoundedCorners(5))
                    .into(ivBanner);
        } else {
            ivBanner.setBackgroundResource(R.drawable.no_image_available);
        }
        return view;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view == obj;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        container.removeView(view);
    }

    public void getWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = width / 2;
    }
}

