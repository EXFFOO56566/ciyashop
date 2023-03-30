package com.example.ciyashop.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.activity.FilterActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.FilterSelectedList;
import com.example.ciyashop.model.FilterOtherOption;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class FilterTypeAdapter extends RecyclerView.Adapter<FilterTypeAdapter.FilterTypeViewHolder> implements OnItemClickListener {

    private final List<FilterOtherOption> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;
    private final Map<Integer, Integer> filterTypeList = new HashMap<>();
    FilterAdapter filterTypeAdapter;
    private boolean isRatingEnable;

    public FilterTypeAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void setRatingEnableOrNot(boolean isRatingEnable) {
        this.isRatingEnable = isRatingEnable;
    }

    public void addAll(List<FilterOtherOption> list) {
        for (FilterOtherOption result : list) {
            if (result.name.equalsIgnoreCase("rating")) {
                if (!isRatingEnable || result.options.size() == 0) {
                } else this.list.add(result);
            } else this.list.add(result);
        }
//        this.list = list;
        getWidthAndHeight();
        filterTypeList.put(0, 1);
        for (int i = 0; i < list.size(); i++) {
            filterTypeList.put(i, 0);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilterTypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_type, parent, false);
        return new FilterTypeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FilterTypeViewHolder holder, int position) {
        holder.tvTitle.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        holder.tvTitle.setText(list.get(position).name);

        ((BaseActivity) activity).setTextViewDrawableColors(holder.tvTitle, (Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        setFilterTypeAdapter(holder.rvFilter, position);
//        if (list.get(position).name.toLowerCase().equals("rating")) {
//            if (!isRatingEnable || list.get(position).options.size() == 0) {
//                list.remove(position);
//                return;
//            }
//        }

        holder.tvTitle.setOnClickListener(view -> {
            if (filterTypeList.get(position) == 1) {
                filterTypeList.put(position, 0);
                holder.rvFilter.setVisibility(View.GONE);
            } else {
                filterTypeList.put(position, 1);
                holder.rvFilter.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {

        if (FilterActivity.clearFilter) {
            clearFilter();
            FilterActivity.clearFilter = false;
        }
        FilterOtherOption filterOtherOption = FilterSelectedList.selectedOtherOptionList.get(outerPos);
        if (value.equals(RequestParamUtils.strtrue)) {
            filterOtherOption.options.set(position, list.get(outerPos).options.get(position));
        } else {
            filterOtherOption.options.set(position, "");
        }
    }

    public void clearFilter() {
        for (int i = 0; i < FilterSelectedList.selectedOtherOptionList.size(); i++) {
            FilterOtherOption filterOtherOption = FilterSelectedList.selectedOtherOptionList.get(i);
            List<String> option = filterOtherOption.options;
            for (int j = 0; j < option.size(); j++) {
                option.set(j, "");
            }
        }
    }

    public static class FilterTypeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        RecyclerView rvFilter;

        public FilterTypeViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            rvFilter = view.findViewById(R.id.rvFilter);
        }
    }

    public void getWidthAndHeight() {
        int height_value = activity.getResources().getInteger(R.integer.height);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels / 2 - 20;
        height = width - height_value;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setFilterTypeAdapter(RecyclerView recyclerView, int pos) {
        if (filterTypeList.get(pos) == 1) {
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
        }
        filterTypeAdapter = new FilterAdapter(activity, this, list.get(pos).name);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(filterTypeAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        filterTypeAdapter.addAll(list.get(pos).options);
        filterTypeAdapter.setOuterListPosition(pos);
    }
}