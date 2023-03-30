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
import com.example.ciyashop.activity.WebDataActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 17-11-2017.
 */

public class WebPagesAdapter extends RecyclerView.Adapter<WebPagesAdapter.RecentViewHolder> {

    private List<Home.WebViewPages> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;

    public WebPagesAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Home.WebViewPages> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WebPagesAdapter.RecentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_info_pages, parent, false);
        return new RecentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvInfoPageTitle.setText(Html.fromHtml(list.get(position).webViewPagesPageTitle, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvInfoPageTitle.setText(Html.fromHtml(list.get(position).webViewPagesPageTitle));
        }

        holder.llMain.setOnClickListener(v -> {
            Intent intent = new Intent(activity, WebDataActivity.class);
            intent.putExtra(RequestParamUtils.WebData, list.get(position).webViewPagesPageId);
            intent.putExtra(RequestParamUtils.WebTitle, list.get(position).webViewPagesPageTitle);
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
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

    public static class RecentViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        TextView tvInfoPageTitle;

        public RecentViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            tvInfoPageTitle = view.findViewById(R.id.tvInfoPageTitle);
        }
    }
}
