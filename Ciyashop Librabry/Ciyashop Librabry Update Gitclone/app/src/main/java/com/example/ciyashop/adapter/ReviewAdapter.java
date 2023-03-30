package com.example.ciyashop.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.ProductReview;
import com.example.ciyashop.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {

    private List<ProductReview> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;

    public ReviewAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<ProductReview> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {

        Drawable unwrappedDrawable = holder.tvRatting.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        if (list.get(position).rating != 0 && list.get(position).rating > 2) {
            DrawableCompat.setTint(wrappedDrawable, activity.getResources().getColor(R.color.green));
        } else {
            DrawableCompat.setTint(wrappedDrawable, Color.RED);
        }
        holder.tvRatting.setText(Constant.setDecimalTwo((double) list.get(position).rating));
        holder.tvName.setText(list.get(position).name);
        holder.tvReview.setText(list.get(position).review);
        holder.tvTime.setText(Constant.setDate(list.get(position).dateCreated));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ReviewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRatting, tvReview;
        TextView tvTime;

        public ReviewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvName);
            tvRatting = view.findViewById(R.id.tvRatting);
            tvReview = view.findViewById(R.id.tvReview);
            tvTime = view.findViewById(R.id.tvTime);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}