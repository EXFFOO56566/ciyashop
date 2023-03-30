package com.example.ciyashop.adapter;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Download;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 16-11-2017.
 */

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.RecentViewHolder> {
    private final List<Download> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;

    public DownloadAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Download> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public List<Download> getList() {
        return this.list;
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download, parent, false);
        return new RecentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecentViewHolder holder, int position) {
        holder.llMain.setOnClickListener(view -> {
            //full view click
        });

        holder.ivDownload.setOnClickListener(view -> {
            //open url in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(position).downloadUrl));
            activity.startActivity(browserIntent);
//                new DownloadFileFromURL().execute(list.get(position).downloadUrl);

            String urlString = list.get(position).downloadUrl;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(RequestParamUtils.chromePackage);
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                // Chrome browser presumably not installed so allow user to choose instead
                intent.setPackage(null);
                activity.startActivity(intent);
            }
        });
//        holder.ivDownload.getDrawable().setColorFilter(new
//                PorterDuffColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)), PorterDuff.Mode.OVERLAY));
        holder.ivDownload.setBackgroundColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        String strExpire;
        try {
            String[] date = list.get(position).accessExpires.split("T");
            strExpire = " " + date[0] + "";
            holder.tvExpiration.setText(strExpire);
        } catch (Exception e) {
            Log.e("Exception is ", e.getMessage());
            strExpire = " " + list.get(position).accessExpires + "";
            holder.tvExpiration.setText(strExpire);
        }

        holder.tvFileName.setText(list.get(position).downloadName);
        holder.tvTitle.setText(list.get(position).productName);
        String strRemains = " " + list.get(position).downloadsRemaining;
        holder.tvRemain.setText(strRemains);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RecentViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        ImageView ivDownload;
        TextView tvBottom, tvTitle, tvRemain, tvExpiration, tvFileName;

        public RecentViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            ivDownload = view.findViewById(R.id.ivDownload);
            tvBottom = view.findViewById(R.id.tvBottom);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvRemain = view.findViewById(R.id.tvRemain);
            tvExpiration = view.findViewById(R.id.tvExpiration);
            tvFileName = view.findViewById(R.id.tvFileName);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

}
