package com.example.ciyashop.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.customview.textview.TextViewLight;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.MyPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ankita on 5/10/2018.
 */

public class MyPointsAdapter extends RecyclerView.Adapter<MyPointsAdapter.MyPointHolder> {

    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private final int width = 0;
    private final int height = 0;
    private List<MyPoint.Event> list = new ArrayList<>();

    public MyPointsAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<MyPoint.Event> list) {
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public List<MyPoint.Event> getList() {
        if (this.list == null) {
            this.list = new ArrayList<>();
        }
        return this.list;
    }

    @NonNull
    @Override
    public MyPointsAdapter.MyPointHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_points, parent, false);
        return new MyPointHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyPointHolder holder, int position) {
        holder.tvDate.setText(list.get(position).dateDisplayHuman);
        holder.tvDescription.setText(list.get(position).description);
        holder.tvPoint.setText(list.get(position).points);
        if (position == list.size() - 1) {
            holder.line1.setVisibility(View.GONE);
        } else {
            holder.line1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyPointHolder extends RecyclerView.ViewHolder {

        View line1;
        TextView tvDescription, tvDate, tvPoint;

        public MyPointHolder(View view) {
            super(view);
            line1 = view.findViewById(R.id.line1);
            tvDescription = view.findViewById(R.id.tvDescription);
            tvDate = view.findViewById(R.id.tvDate);
            tvPoint = view.findViewById(R.id.tvPoint);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
