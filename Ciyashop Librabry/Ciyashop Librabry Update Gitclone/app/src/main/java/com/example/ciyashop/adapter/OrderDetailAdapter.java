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
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Orders;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by User on 01-12-2017.
 */

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.RecentViewHolder> {
    private List<Orders.LineItem> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private final String currencySymbol;
    private int width = 0, height = 0;

    public OrderDetailAdapter(Activity activity, OnItemClickListener onItemClickListener, String currencySymbol) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
        this.currencySymbol = currencySymbol;
    }

    public void addAll(List<Orders.LineItem> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ordered_product, parent, false);
        return new RecentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecentViewHolder holder, int position) {
        holder.llMain.setOnClickListener(view -> {
//                Constant.ORDERDETAIL = list.get(position);
//                Intent intent = new Intent(activity, OrderDetailActivity.class);
//                intent.putExtra(RequestParamUtils.ID, list.get(position).id);
//                activity.startActivity(intent);
        });

        holder.tvProductName.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        holder.tvProductName.setText(list.get(position).name);
        String strPrice;
        try {
            strPrice = currencySymbol + " " + Constant.setDecimal((double) list.get(position).price);
            holder.tvProductPrice.setText(strPrice);
        } catch (Exception e) {
            strPrice = currencySymbol + " " + list.get(position).price;
            holder.tvProductPrice.setText(strPrice);
        }
        holder.tvProductPrice.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        holder.tvQuantity.setText(String.valueOf(list.get(position).quantity));
        holder.tvQuantity.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
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
        TextView tvProductName, tvQuantity;
        TextView tvProductPrice;

        public RecentViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            tvProductName = view.findViewById(R.id.tvProductName);
            tvProductPrice = view.findViewById(R.id.tvProductPrice);
            tvQuantity = view.findViewById(R.id.tvQuantity);
        }
    }
}
