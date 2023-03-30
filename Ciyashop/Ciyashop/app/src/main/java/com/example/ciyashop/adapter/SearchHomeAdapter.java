package com.example.ciyashop.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.SearchLive;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class SearchHomeAdapter extends RecyclerView.Adapter<SearchHomeAdapter.SearchHomeViewHolder> {

    private List<SearchLive> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;

    public SearchHomeAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<SearchLive> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    public void updateList(List<SearchLive> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public List<SearchLive> getList() {
        return list;
    }

    @NonNull
    @Override
    public SearchHomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search, parent, false);
        return new SearchHomeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHomeViewHolder holder, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvTitle.setText(Html.fromHtml(list.get(position).name, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvTitle.setText(Html.fromHtml(list.get(position).name));
        }
        holder.llMain.setOnClickListener(v -> onItemClickListener.onItemClick(position, list.get(position).name, list.get(position).id));
        holder.tvTitle.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        holder.line.setBackgroundColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_TRANSPARENT_VERY_LIGHT, Constant.SECONDARY_COLOR)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SearchHomeViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView line;
        LinearLayout llMain;

        public SearchHomeViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            line = view.findViewById(R.id.line);
            llMain = view.findViewById(R.id.llMain);
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