package com.example.ciyashop.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.activity.FilterActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.FilterSelectedList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private List<String> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;
    private int outerPosition;
    private final String name;

    public FilterAdapter(Activity activity, OnItemClickListener onItemClickListener, String name) {
        this.activity = activity;
        this.name = name;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setOuterListPosition(int outerPosition) {
        this.outerPosition = outerPosition;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false);
        return new FilterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        if (name.equalsIgnoreCase("rating")) {
            try {
                if (!list.get(position).equals("")) {
                    holder.ratingBar.setRating(Float.parseFloat(list.get(position)));
                } else {
                    holder.ratingBar.setRating(0);
                }
            } catch (Exception e) {
                holder.ratingBar.setRating(0);
            }
            holder.tvName.setVisibility(View.GONE);
            holder.llRating.setVisibility(View.VISIBLE);
        } else {
            holder.tvName.setVisibility(View.VISIBLE);
            holder.llRating.setVisibility(View.GONE);
            holder.tvName.setText(list.get(position));
        }

        if (FilterSelectedList.selectedOtherOptionList.get(outerPosition).options.size() > 0 && !FilterActivity.clearFilter) {
            if (FilterSelectedList.selectedOtherOptionList.get(outerPosition).options.contains(list.get(position))) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.ckSelect.getButtonDrawable().setColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_IN);
                }
                holder.ckSelect.setChecked(true);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.ckSelect.getButtonDrawable().setColorFilter(activity.getColor(R.color.gray_light), PorterDuff.Mode.SRC_IN);
                }
                holder.ckSelect.setChecked(false);
            }
        } else {
            holder.ckSelect.setChecked(false);
        }
        holder.llMain.setOnClickListener(v -> {
            if (!holder.ckSelect.isChecked()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.ckSelect.getButtonDrawable().setColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_IN);
                }
                holder.ckSelect.setChecked(true);
                onItemClickListener.onItemClick(position, RequestParamUtils.strtrue, outerPosition);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.ckSelect.getButtonDrawable().setColorFilter(activity.getColor(R.color.gray_light), PorterDuff.Mode.SRC_IN);
                }
                holder.ckSelect.setChecked(false);
                onItemClickListener.onItemClick(position, RequestParamUtils.strfalse, outerPosition);
            }
        });

        holder.ckSelect.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.ckSelect.getButtonDrawable().setColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)), PorterDuff.Mode.SRC_IN);
                }
                onItemClickListener.onItemClick(position, RequestParamUtils.strtrue, outerPosition);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.ckSelect.getButtonDrawable().setColorFilter(activity.getColor(R.color.gray_light), PorterDuff.Mode.SRC_IN);
                }
                onItemClickListener.onItemClick(position, RequestParamUtils.strfalse, outerPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class FilterViewHolder extends RecyclerView.ViewHolder {

        RatingBar ratingBar;
        LinearLayout llMain, llRating;
        TextView tvName;
        CheckBox ckSelect;

        public FilterViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            llRating = view.findViewById(R.id.llRating);
            tvName = view.findViewById(R.id.tvName);
            ckSelect = view.findViewById(R.id.ckSelect);
            ratingBar = view.findViewById(R.id.ratingBar);
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

}