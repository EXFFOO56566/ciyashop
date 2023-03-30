package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.activity.CategoryListActivity;
import com.example.ciyashop.customview.textview.TextViewLight;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class SearchInnerInnerCategoryAdapter extends RecyclerView.Adapter<SearchInnerInnerCategoryAdapter.CategoryViewHolder> {

    private List<Home.AllCategory> list;
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private final Map<Integer, SearchCategoryAdapter.CategoryViewHolder> expandList = new HashMap<>();

    public SearchInnerInnerCategoryAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Home.AllCategory> list) {
        this.list = list;
        for (int i = 0; i < list.size(); i++) {
            expandList.put(i, null);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sinner_earch_catgory, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        holder.tvName.setText(list.get(position).name);
        holder.llMain.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CategoryListActivity.class);
            intent.putExtra(RequestParamUtils.CATEGORY, list.get(position).id + "");
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        TextView tvName;

        public CategoryViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            tvName = view.findViewById(R.id.tvName);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}