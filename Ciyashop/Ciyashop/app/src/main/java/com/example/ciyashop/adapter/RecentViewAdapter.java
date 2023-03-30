package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class RecentViewAdapter extends RecyclerView.Adapter<RecentViewAdapter.RecentViewHolder> {

    private List<CategoryList> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0;
    private final int height = 0;
    ImageView ivImage;

    public RecentViewAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<CategoryList> list) {
        this.list = list;
        if (this.list == null) {
            this.list = new ArrayList<>();
        }
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_special_offer, parent, false);
        return new RecentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecentViewHolder holder, int position) {
        ivImage = holder.ivImage;
        holder.llMain.getLayoutParams().width = width - (width / 3);
        holder.ivAddToCart.setVisibility(View.GONE);
        ((BaseActivity) activity).showDiscount(holder.tvOff, list.get(position).salePrice, list.get(position).regularPrice);

        holder.llMain.setOnClickListener(view -> {
            if (list.get(position).type.equals(RequestParamUtils.external)) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(position).externalUrl));
                activity.startActivity(browserIntent);
            } else {
                Constant.CATEGORYDETAIL = list.get(position);
                Intent intent = new Intent(activity, ProductDetailActivity.class);
                intent.putExtra(RequestParamUtils.ID, list.get(position).id);
                activity.startActivity(intent);
            }
        });

        if (list.get(position).appthumbnail != null) {
            //holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(activity.getBaseContext()).load(list.get(position).appthumbnail)
                    .fitCenter()
                    .error(R.drawable.no_image_available)
                    .transform(new RoundedCorners(5))
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.no_image_available);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.
                os.Build.VERSION_CODES.N) {
            holder.tvName.setText(Html.fromHtml(list.get(position).name + "", Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvName.setText(Html.fromHtml(list.get(position).name + ""));
        }

        holder.tvPrice.setTextSize(15);
        if (list.get(position).priceHtml != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml + "", Html.FROM_HTML_MODE_COMPACT));
            } else {
                holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml));
            }
        holder.tvPrice.setTextSize(15);

        ((BaseActivity) activity).setPrice(holder.tvPrice, holder.tvPrice1, list.get(position).priceHtml);

        if (!list.get(position).averageRating.equals("") && list.get(position).averageRating != null) {
            holder.ratingBar.setRating(Float.parseFloat(list.get(position).averageRating));
        } else {
            holder.ratingBar.setRating(0);
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(list.size(), 5);
    }

    public void getWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
    }

    public static class RecentViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        ImageView ivImage, ivAddToCart;
        TextView tvName;
        TextView tvOff, tvPrice, tvPrice1;
        RatingBar ratingBar;

        public RecentViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            ivImage = view.findViewById(R.id.ivImage);
            ivAddToCart = view.findViewById(R.id.ivAddToCart);
            tvName = view.findViewById(R.id.tvName);
            tvOff = view.findViewById(R.id.tvOff);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPrice1 = view.findViewById(R.id.tvPrice1);
            ratingBar = view.findViewById(R.id.ratingBar);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}