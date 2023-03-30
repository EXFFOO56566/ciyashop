package com.example.ciyashop.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.customview.textview.TextViewLight;
import com.example.ciyashop.customview.textview.TextViewMedium;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Notification;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 16-11-2017.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.RecentViewHolder> {

    private List<Notification.Datum> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;

    public NotificationAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Notification.Datum> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new RecentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecentViewHolder holder, int position) {
        holder.llMain.setOnClickListener(view -> {
            onItemClickListener.onItemClick(position, list.get(position).pushMetaId, list.size());
            list.remove(position);
            notifyDataSetChanged();
        });
        holder.tvNotificationTitle.setText(list.get(position).msg);
        holder.tvNotificationDesc.setText(list.get(position).customMsg);
        holder.viewLine.setBackgroundColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RecentViewHolder extends RecyclerView.ViewHolder {

        View viewLine;
        LinearLayout llMain;
        TextView tvNotificationTitle;
        TextView tvNotificationDesc;

        public RecentViewHolder(View view) {
            super(view);
            viewLine = view.findViewById(R.id.view_line);
            llMain = view.findViewById(R.id.llMain);
            tvNotificationTitle = view.findViewById(R.id.tvNotificationTitle);
            tvNotificationDesc = view.findViewById(R.id.tvNotificationDesc);
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
