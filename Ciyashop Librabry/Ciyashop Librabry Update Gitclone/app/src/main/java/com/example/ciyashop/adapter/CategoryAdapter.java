package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CategoryListActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Home.CategoryBanner> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;

    public CategoryAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Home.CategoryBanner> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {

        holder.llMain.getLayoutParams().width = width;
        holder.llMain.getLayoutParams().height = height;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                width,
                height
        );

        if (Config.IS_RTL) {
            if (position == list.size() - 1) {
                params.setMargins(((BaseActivity) activity).dpToPx(10), 0, ((BaseActivity) activity).dpToPx(10), 0);
            } else {
                params.setMargins(0, 0, ((BaseActivity) activity).dpToPx(10), 0);
            }
        }
        holder.llMain.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvName.setText(Html.fromHtml(list.get(position).catBannersTitle, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvName.setText(Html.fromHtml(list.get(position).catBannersTitle));
        }

        if (list.get(position).catBannersImageUrl != null) {
            Glide.with(activity.getBaseContext()).load(list.get(position).catBannersImageUrl)
                    .error(R.drawable.no_image_available)
                    .transform(new RoundedCorners(5))
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.no_image_available);
        }

        holder.llMain.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CategoryListActivity.class);
            intent.putExtra(RequestParamUtils.CATEGORY, list.get(position).catBannersCatId);
            intent.putExtra(RequestParamUtils.IS_WISHLIST_ACTIVE, Constant.IS_WISH_LIST_ACTIVE);
            activity.startActivity(intent);
        });

        Drawable unwrappedDrawable = holder.tvShopNow.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void getWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = (int) (displayMetrics.widthPixels / 2.8);
        height = (int) (width / 1.18);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvShopNow, tvName;
        ImageView ivImage;
        LinearLayout llMain;

        public CategoryViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            ivImage = view.findViewById(R.id.ivImage);
            tvName = view.findViewById(R.id.tvName);
            tvShopNow = view.findViewById(R.id.tvShopNow);
        }
    }
}