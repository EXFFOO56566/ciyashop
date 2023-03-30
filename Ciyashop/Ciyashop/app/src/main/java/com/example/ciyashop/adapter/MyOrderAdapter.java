package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.OrderDetailActivity;
import com.example.ciyashop.activity.RepaymentActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Orders;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */
public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.RecentViewHolder> {
    private List<Orders> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;

    public MyOrderAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<Orders> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_order, parent, false);
        return new RecentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecentViewHolder holder, int position) {

//        holder.llMain.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Constant.ORDERDETAIL = list.get(position);
//                Intent intent = new Intent(activity, OrderDetailActivity.class);
//                intent.putExtra(RequestParamUtils.ID, list.get(position).id);
//                activity.startActivity(intent);
//            }
//        });

        Drawable unwrappedDrawable = holder.tvView.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR))));

        holder.tvOrderDateAndId.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        holder.tvStatus.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));

//        if (list.get(position).orderRepaymentUrl != null && list.get(position).orderRepaymentUrl.equals("")) {
//            holder.tvRepayment.setVisibility(View.GONE);
//        } else {
//            holder.tvRepayment.setVisibility(View.VISIBLE);
//        }

        holder.tvQuantity.setText(String.valueOf(list.get(position).lineItems.size()));
        String strAmount = list.get(position).currency + " " + list.get(position).total;
        holder.tvTotalAmount.setText(strAmount);
        holder.tvView.setOnClickListener(view -> {
            Constant.ORDERDETAIL = list.get(position);
            Intent intent = new Intent(activity, OrderDetailActivity.class);
            intent.putExtra(RequestParamUtils.ID, list.get(position).id);
            activity.startActivity(intent);
        });

        holder.tvRepayment.setOnClickListener(view -> {
            Constant.ORDERDETAIL = list.get(position);
            Intent intent = new Intent(activity, RepaymentActivity.class);
            intent.putExtra(RequestParamUtils.ID, list.get(position).id);
            intent.putExtra(RequestParamUtils.RepaymentURL, list.get(position).orderRepaymentUrl);
            intent.putExtra(RequestParamUtils.THANKYOU, list.get(position).Thankyou);
            activity.startActivity(intent);
        });

        if (list.get(position).lineItems.get(0).productImage.equals("")) {
            holder.ivImage.setVisibility(View.INVISIBLE);
        } else {
            holder.ivImage.setVisibility(View.VISIBLE);
            Glide.with(activity.getBaseContext())
                    .load(list.get(position).lineItems.get(0).productImage)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .transform(new RoundedCorners(5))
                    .into(holder.ivImage);

        }

        String title = "";
        for (int i = 0; i < list.get(position).lineItems.size(); i++) {
            if (i == 0) {
                title = list.get(position).lineItems.get(i).name;
            } else {
                title = String.format("%s & %s", title, list.get(position).lineItems.get(i).name);
            }
        }
//        holder.tvTitle.setText(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            tvProductName.setText(categoryList.name + "");
            holder.tvTitle.setText(Html.fromHtml(title + "", Html.FROM_HTML_MODE_LEGACY));
        } else {
//            tvProductName.setText(categoryList.name + "");
            holder.tvTitle.setText(Html.fromHtml(title + ""));
        }

        String upperString = list.get(position).status.substring(0, 1).toUpperCase() + list.get(position).status.substring(1);
        holder.tvStatus.setText(upperString);

        String idAndDate = list.get(position).id + "";

        String orderDate = " " + Constant.setDate(list.get(position).dateCreated);
        holder.tvOrderDate.setText(orderDate);
        holder.tvOrderDateAndId.setText(idAndDate);
//        String statusDesc = "";
        if (list.get(position).status.equalsIgnoreCase(RequestParamUtils.any)) {
            holder.tvStatusDesc.setText(R.string.delivered_soon);
            holder.tvStatus.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        } else if (list.get(position).status.equalsIgnoreCase(RequestParamUtils.pending)) {
            holder.tvStatusDesc.setText(R.string.order_is_in_pending_state);
            holder.tvStatus.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        } else if (list.get(position).status.equalsIgnoreCase(RequestParamUtils.processing)) {
            holder.tvStatusDesc.setText(R.string.order_is_under_processing);
            holder.tvStatus.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        } else if (list.get(position).status.equalsIgnoreCase(RequestParamUtils.onHold)) {
            holder.tvStatusDesc.setText(R.string.order_is_on_hold);
            holder.tvStatus.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        } else if (list.get(position).status.equalsIgnoreCase(RequestParamUtils.completed)) {
            holder.tvStatusDesc.setText(R.string.delivered);
            holder.tvStatus.setTextColor(activity.getResources().getColor(R.color.green));
        } else if (list.get(position).status.equalsIgnoreCase(RequestParamUtils.cancelled)) {
            holder.tvStatusDesc.setText(R.string.order_is_cancelled);
            holder.tvStatus.setTextColor(Color.RED);
        } else if (list.get(position).status.equalsIgnoreCase(RequestParamUtils.refunded)) {
            holder.tvStatusDesc.setText(R.string.you_are_refunded_for_this_order);
            holder.tvStatus.setTextColor(Color.RED);
        } else if (list.get(position).status.equalsIgnoreCase(RequestParamUtils.failed)) {
            holder.tvStatusDesc.setText(R.string.order_is_failed);
            holder.tvStatus.setTextColor(Color.RED);
        } else if (list.get(position).status.equalsIgnoreCase(RequestParamUtils.shipping)) {
            holder.tvStatusDesc.setText(R.string.delivered_soon);
            holder.tvStatus.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        }
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
        ImageView ivImage;
        TextView tvOrderDateAndId, tvOrderDate, tvTitle, tvTotalAmount, tvQuantity, tvView, tvRepayment, tvStatusDesc, tvStatus;

        public RecentViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            ivImage = view.findViewById(R.id.ivImage);
            tvOrderDateAndId = view.findViewById(R.id.tvOrderDateAndId);
            tvOrderDate = view.findViewById(R.id.tvOrderDate);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
            tvQuantity = view.findViewById(R.id.tvQuantity);
            tvView = view.findViewById(R.id.tvView);
            tvRepayment = view.findViewById(R.id.tvRepayment);
            tvStatusDesc = view.findViewById(R.id.tvStatusDesc);
            tvStatus = view.findViewById(R.id.tvStatus);
        }
    }
}