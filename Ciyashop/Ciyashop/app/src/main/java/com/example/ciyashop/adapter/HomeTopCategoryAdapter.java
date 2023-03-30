package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CategoryListActivity;
import com.example.ciyashop.activity.SearchCategoryInnerListActivity;
import com.example.ciyashop.activity.SearchCategoryListActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class HomeTopCategoryAdapter extends RecyclerView.Adapter<HomeTopCategoryAdapter.CategoryViewHolder> {

    private List<Home.MainCategory> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0;
    private final int height = 0;

    ImageView imageView;

    public HomeTopCategoryAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Home.MainCategory> list) {
        this.list.clear();
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_top_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        imageView = holder.ivImage;
        Home.MainCategory mainCategory = list.get(position);

        holder.llMain.getLayoutParams().width = width;
        if (holder.flImage.getLayoutParams().width > width - 10) {
            holder.flImage.getLayoutParams().width = width - 10;
            holder.flImage.getLayoutParams().height = width - 10;
        }

        if (position == list.size() - 1) {
            holder.ivImage.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_more_white, null));
        } else {
            if (mainCategory.mainCatImage != null && !mainCategory.mainCatImage.equals("")) {
                Glide.with(activity.getBaseContext()).load(mainCategory.mainCatImage).error(R.drawable.ic_more_white).into(holder.ivImage);
            } else {
                holder.ivImage.setImageResource(R.drawable.blackround);
            }
        }

        if (list.get(position).mainCatName != null && !list.get(position).mainCatName.equals("")) {
            holder.tvName.setText(list.get(position).mainCatName);
        }
        holder.tvName.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        if (position == list.size() - 1) {
            holder.llMain.setOnClickListener(view -> {
                Intent intent = new Intent(activity, SearchCategoryListActivity.class);
                intent.putExtra(RequestParamUtils.from, RequestParamUtils.SEARCH);
                activity.startActivity(intent);
            });
        } else {
            holder.llMain.setOnClickListener(view -> {
                try {
                    Intent intent = new Intent(activity, SearchCategoryInnerListActivity.class);
                    intent.putExtra(RequestParamUtils.CATEGORY, Integer.parseInt(list.get(position).mainCatId));
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Log.e("Category exception", e.getMessage());
                    Intent intent = new Intent(activity, CategoryListActivity.class);
                    intent.putExtra(RequestParamUtils.CATEGORY, list.get(position).mainCatId);
                    activity.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        FrameLayout flImage;
        ImageView ivImage;
        TextView tvName;

        public CategoryViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            flImage = view.findViewById(R.id.flImage);
            ivImage = view.findViewById(R.id.ivImage);
            tvName = view.findViewById(R.id.tvName);
        }
    }

    public void getWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels / 5;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}