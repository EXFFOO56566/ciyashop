package com.example.ciyashop.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.activity.FilterActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.FilterSelectedList;
import com.example.ciyashop.model.FilterColorOption;
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

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private List<FilterColorOption.Option> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;
    private final Map<Integer, Integer> selectList = new HashMap<>();

    public ColorAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<FilterColorOption.Option> list) {
        this.list = list;
        for (int i = 0; i < list.size(); i++) {
            selectList.put(i, 0);
        }
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    public List<FilterColorOption.Option> getList() {
        return list;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color, parent, false);
        return new ColorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        if (list.get(position).colorCode.equals("") && list.get(position).colorName.equals("")) {
            holder.tvColor.setText(list.get(position).colorName);
            GradientDrawable drawable = (GradientDrawable) holder.tvColor.getBackground();
            drawable.setColor(0x00000000);
        } else if (list.get(position).colorCode.equals("") && !list.get(position).colorName.equals("")) {
            holder.tvColor.setText("");
            GradientDrawable drawable = (GradientDrawable) holder.tvColor.getBackground();
            drawable.setColor(Color.BLACK);
        } else {
            holder.tvColor.setText("");
            GradientDrawable drawable = (GradientDrawable) holder.tvColor.getBackground();
            drawable.setColor(Color.parseColor(list.get(position).colorCode));
        }

        GradientDrawable drawable = (GradientDrawable) holder.tvSelect.getBackground();
        drawable.setColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        if (FilterSelectedList.selectedColorOptionList.get(0).options.size() > 0 &&
                FilterSelectedList.selectedColorOptionList.get(0).options.contains(list.get(position).colorName)
                && !FilterActivity.clearFilter) {
            holder.tvSelect.setVisibility(View.VISIBLE);
        } else {
            holder.tvSelect.setVisibility(View.GONE);
        }
        holder.tvColor.setOnClickListener(view -> {
            if (FilterActivity.clearFilter) {
                for (int k = 0; k < FilterSelectedList.selectedColorOptionList.get(0).options.size(); k++) {
                    FilterSelectedList.selectedColorOptionList.get(0).options.set(k, "");
                }
                for (int i = 0; i < list.size(); i++) {
                    selectList.put(i, 0);
                }
                FilterActivity.clearFilter = false;
            }
            if (selectList.get(position) == 1) {
                selectList.put(position, 0);
                holder.tvSelect.setVisibility(View.GONE);
                onItemClickListener.onItemClick(position, RequestParamUtils.strfalse, 0);
            } else {
                selectList.put(position, 1);
                holder.tvSelect.setVisibility(View.VISIBLE);
                onItemClickListener.onItemClick(position, RequestParamUtils.strtrue, 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ColorViewHolder extends RecyclerView.ViewHolder {

        TextView tvColor, tvSelect;

        public ColorViewHolder(View view) {
            super(view);
            tvColor = view.findViewById(R.id.tvColor);
            tvSelect = view.findViewById(R.id.tvSelect);
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
}