package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CategoryListActivity;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/9/2017.
 */

public class BannerViewPagerAdapter extends PagerAdapter {
    private List<Home.MainSlider> list = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private final Activity activity;
    private int length;

    public BannerViewPagerAdapter(Activity activity) {
        this.activity = activity;
    }

    public void addAll(List<Home.MainSlider> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_home_top_banner, container, false);
        final ImageView imageView = view.findViewById(R.id.ivHomeTopBanner);
        final ProgressBar progress_bar = view.findViewById(R.id.progress_bar);
//        imageView.setImageResource(R.drawable.banner);
        container.addView(view);
        progress_bar.getIndeterminateDrawable().setColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)), android.graphics.PorterDuff.Mode.MULTIPLY);
        imageView.setOnClickListener(view1 -> {
            Intent intent = new Intent(activity, CategoryListActivity.class);
            intent.putExtra(RequestParamUtils.CATEGORY, list.get(position).sliderCatId);
            intent.putExtra(RequestParamUtils.IS_WISHLIST_ACTIVE, Constant.IS_WISH_LIST_ACTIVE);
            activity.startActivity(intent);
        });

        if (list.get(position).uploadImageUrl == null || list.get(position).uploadImageUrl.equals("")) {
            progress_bar.setVisibility(View.GONE);
        }

        if (list.get(position).uploadImageUrl != null && !list.get(position).uploadImageUrl.equals("")) {
            Glide.with(activity.getBaseContext())
                    .load(list.get(position).uploadImageUrl)
                    .error(R.drawable.no_image_available)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progress_bar.setVisibility(View.GONE);
                            Glide.with(activity.getBaseContext())
                                    .load(R.drawable.no_image_available)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .error(R.drawable.no_image_available)
                                    .into(imageView);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progress_bar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.no_image_available);
        }

        ViewTreeObserver vto = imageView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
//                Log.e("Banner Height: " + imageView.getMeasuredHeight(), "Banner  Width: " +imageView.getMeasuredWidth());
                return true;
            }
        });
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
}
