package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
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
import com.example.ciyashop.activity.InfoPageDetailActivity;
import com.example.ciyashop.customview.textview.TextViewLight;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.InfoPages;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 17-11-2017.
 */

public class InfoPageAdapter extends RecyclerView.Adapter<InfoPageAdapter.RecentViewHolder> {

    private List<InfoPages.Datum> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;

    public InfoPageAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<InfoPages.Datum> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InfoPageAdapter.RecentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_info_pages, parent, false);
        return new RecentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        final String title;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            title = String.valueOf(Html.fromHtml(list.get(position).title, Html.FROM_HTML_MODE_COMPACT));
        } else {
            title = String.valueOf(Html.fromHtml(list.get(position).title));
        }

        holder.llMain.setOnClickListener(view -> {
            Intent intent = new Intent(activity, InfoPageDetailActivity.class);
            intent.putExtra(RequestParamUtils.ID, list.get(position).pageId + "");
            intent.putExtra("title", title);
            activity.startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvInfoPageTitle.setText(Html.fromHtml(list.get(position).title, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvInfoPageTitle.setText(Html.fromHtml(list.get(position).title));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RecentViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        TextView tvInfoPageTitle;

        public RecentViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            tvInfoPageTitle = view.findViewById(R.id.tvInfoPageTitle);
        }
    }

    public void getWidthAndHeight() {
        int height_value = activity.getResources().getInteger(R.integer.height);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels / 2 - height_value * 2;
        height = width / 2 + height_value;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
