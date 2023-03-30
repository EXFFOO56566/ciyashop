package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CategoryListActivity;
import com.example.ciyashop.activity.SearchCategoryListActivity;
import com.example.ciyashop.customview.textview.TextViewLight;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class SearchInnerCategoryAdapter extends RecyclerView.Adapter<SearchInnerCategoryAdapter.CategoryViewHolder> implements OnItemClickListener {

    private List<Home.AllCategory> list = new ArrayList<>();
    private Map<Integer, List<Home.AllCategory>> childList = new HashMap<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;
    private final Map<Integer, CategoryViewHolder> expandList = new HashMap<>();
    private SearchInnerInnerCategoryAdapter searchInnerInnerCategoryAdapter;
    private int previousPosition = -1;
    private Animation rotate;
    LinearLayout llMain;

    public SearchInnerCategoryAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Home.AllCategory> list, Map<Integer, List<Home.AllCategory>> childList) {
        this.list = list;
        this.childList = childList;
        for (int i = 0; i < list.size(); i++) {
            expandList.put(i, null);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_catgory, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        llMain = holder.llMain;
        setInnerAdapter(holder.rvInnerRecycleView, list.get(holder.getAdapterPosition()).id);

        holder.llMain.setOnClickListener(view -> {
            if (childList.get(list.get(holder.getAdapterPosition()).id).size() == 0) {
                Intent intent = new Intent(activity, CategoryListActivity.class);
                intent.putExtra(RequestParamUtils.CATEGORY, list.get(holder.getAdapterPosition()).id + "");
                intent.putExtra(RequestParamUtils.SEARCH, SearchCategoryListActivity.search);
                intent.putExtra(RequestParamUtils.ORDER_BY, SearchCategoryListActivity.sortBy);
                intent.putExtra(RequestParamUtils.POSITION, SearchCategoryListActivity.sortPosition);
                activity.startActivity(intent);
            } else {
                if (expandList.get(holder.getAdapterPosition()) != null) {
                    holder.rvInnerRecycleView.setVisibility(View.GONE);
                    rotate = AnimationUtils.loadAnimation(activity,
                            R.anim.anti_click_rotate);
                    holder.ivGo.startAnimation(rotate);
                    expandList.put(holder.getAdapterPosition(), null);
                } else {
                    holder.rvInnerRecycleView.setVisibility(View.VISIBLE);
                    rotate = AnimationUtils.loadAnimation(activity,
                            R.anim.click_rotate);
                    holder.ivGo.startAnimation(rotate);
                    expandList.put(holder.getAdapterPosition(), holder);
                    if (expandList.get(previousPosition) != null && previousPosition != -1 && previousPosition != holder.getAdapterPosition()) {
                        expandList.get(previousPosition).rvInnerRecycleView.setVisibility(View.GONE);
                        rotate = AnimationUtils.loadAnimation(activity,
                                R.anim.anti_click_rotate);
                        expandList.get(previousPosition).ivGo.startAnimation(rotate);
                        expandList.put(previousPosition, null);
                    }
                    previousPosition = holder.getAdapterPosition();
                }
            }
        });
        holder.tvName.setText(list.get(holder.getAdapterPosition()).name);
        if (!list.get(holder.getAdapterPosition()).image.src.equals("")) {
            Glide.with(activity.getBaseContext()).load(list.get(holder.getAdapterPosition()).image.src + "").into(holder.ivImage);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        ImageView ivGo, ivImage;
        TextView tvName;
        RecyclerView rvInnerRecycleView;

        public CategoryViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            ivGo = view.findViewById(R.id.ivGo);
            ivImage = view.findViewById(R.id.ivImage);
            tvName = view.findViewById(R.id.tvName);
            rvInnerRecycleView = view.findViewById(R.id.rvInnerRecycleView);
        }
    }

    public void getWidthAndHeight() {
        int height_value = activity.getResources().getInteger(R.integer.height);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels / 2 - height_value;
        height = width - height_value;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setInnerAdapter(RecyclerView recyclerView, int id) {
        searchInnerInnerCategoryAdapter = new SearchInnerInnerCategoryAdapter(activity, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(searchInnerInnerCategoryAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        searchInnerInnerCategoryAdapter.addAll(childList.get(id));
    }
}