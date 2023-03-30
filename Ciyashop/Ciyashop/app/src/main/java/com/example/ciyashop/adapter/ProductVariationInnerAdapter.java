package com.example.ciyashop.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ciyashop.R;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class ProductVariationInnerAdapter extends RecyclerView.Adapter<ProductVariationInnerAdapter.ProductColorViewHolder> {

    public int previousSelectionPosition;
    public int outerPosition;
    private String outListId;
    private List<String> list = new ArrayList<>();
    private List<CategoryList.NewOption> newOptionList = new ArrayList<>();
    private List<String> variationList = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0;
    private final int height = 0;

    public ProductVariationInnerAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void setOutListId(String outListId) {
        this.outListId = outListId;
    }

    public void setOuterPosition(int outerPosition) {
        this.outerPosition = outerPosition;
    }

    public void addAll(List<String> list, List<CategoryList.NewOption> newOptionList) {
        this.list = list;
        this.newOptionList = newOptionList;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    public void addAllVariationList(List<String> variationList) {
        this.variationList = variationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_color, parent, false);
        return new ProductColorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProductColorViewHolder holder, int position) {
        GradientDrawable gd = (GradientDrawable) holder.flTransparent.getBackground();
        gd.setColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_TRANSPARENT, Constant.PRIMARY_COLOR)));

        if (variationList != null && variationList.size() > 0) {
//            if (!variationList.contains(list.get(holder.getAdapterPosition())) && outerPosition != 0) {
//                gd = (GradientDrawable) holder.llMain.getBackground();
//                gd.setStroke(5, Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_TRANSPARENT_VERY_LIGHT, Constant.PRIMARY_COLOR)));
//
//                holder.tvNa me.setTextColor(activity.getResources().getColor(R.color.gray_table));
//            } else {
            gd = (GradientDrawable) holder.llMain.getBackground();
            gd.setStroke(5, Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            holder.tvName.setTextColor(activity.getResources().getColor(R.color.blackTransperent));
//            }
        } else {
            if (outerPosition == 0) {
                holder.llMain.setBackgroundResource(R.drawable.primary_strok_button);
                holder.tvName.setTextColor(activity.getResources().getColor(R.color.blackTransperent));
            }
        }
        if (previousSelectionPosition == holder.getAdapterPosition()) {
            holder.flTransparent.setVisibility(View.VISIBLE);
        } else {
            holder.flTransparent.setVisibility(View.GONE);
        }

        holder.llMain.getLayoutParams().height = width;
        holder.llMain.getLayoutParams().width = width;

        holder.llMain.setOnClickListener(view -> {
            previousSelectionPosition = holder.getAdapterPosition();
            onItemClickListener.onItemClick(previousSelectionPosition, outListId + "->" + list.get(holder.getAdapterPosition()), outerPosition);
            notifyDataSetChanged();
        });

        if (newOptionList != null) {
            if (newOptionList.get(holder.getAdapterPosition()).image != null && !newOptionList.get(holder.getAdapterPosition()).image.equals("")) {
                holder.ivImg.setVisibility(View.VISIBLE);
                Glide.with(activity.getBaseContext()).load(newOptionList.get(holder.getAdapterPosition()).image)
                        .error(R.drawable.no_image_available)
                        .into(holder.ivImg);
                // holder.tvName.setText(newOptionList.get(holder.getAdapterPosition()).image);
            } else if (newOptionList.get(holder.getAdapterPosition()).color != null && !newOptionList.get(holder.getAdapterPosition()).color.equals("")) {
                holder.tvName.setText("");
                holder.tvName.setBackgroundColor(Color.parseColor(newOptionList.get(holder.getAdapterPosition()).color));
            } else {
                holder.tvName.setText(list.get(holder.getAdapterPosition()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void getWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels / 7;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public static class ProductColorViewHolder extends RecyclerView.ViewHolder {

        FrameLayout llMain, flTransparent;
        TextView tvName;
        ImageView ivImg;

        public ProductColorViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            flTransparent = view.findViewById(R.id.flTransparent);
            tvName = view.findViewById(R.id.tvName);
            ivImg = view.findViewById(R.id.ivImg);
        }
    }
}