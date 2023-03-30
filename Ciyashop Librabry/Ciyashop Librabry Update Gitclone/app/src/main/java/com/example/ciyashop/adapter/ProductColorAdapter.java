package com.example.ciyashop.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.CheckIsVariationAvailable;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.Variation;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.CustomToast;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class ProductColorAdapter extends RecyclerView.Adapter<ProductColorAdapter.ProductColorViewHolder> {

    public static int selectedPos;
    AlertDialog alertDialog;
    ProductVariationAdapter productVariationAdapter;
    private List<String> list = new ArrayList<>();
    private List<CategoryList.Attribute> dialogList;
    private List<Variation> variationList = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0;
    private final int height = 0;
    private final CustomToast toast;
    private String type;

    public ProductColorAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
        toast = new CustomToast(activity);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addAll(List<String> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    public void addAllVariationList(List<Variation> variationList, ProductVariationAdapter productVariationAdapter) {
        this.variationList = variationList;
        this.productVariationAdapter = productVariationAdapter;
        showDialog(productVariationAdapter);
        notifyDataSetChanged();
    }

    public void getDialogList(List<CategoryList.Attribute> dialogList) {
        this.dialogList = dialogList;
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
        holder.llMain.getLayoutParams().height = width;
        holder.llMain.getLayoutParams().width = width;

        GradientDrawable gd = (GradientDrawable) holder.flTransparent.getBackground();
        gd.setColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_TRANSPARENT, Constant.PRIMARY_COLOR)));
        if (position == selectedPos) {
            holder.flTransparent.setVisibility(View.VISIBLE);
        } else {
            holder.flTransparent.setVisibility(View.GONE);
        }
        holder.llMain.setBackgroundColor(Color.WHITE);
        holder.llMain.setOnClickListener(view -> {
            if (alertDialog != null) {
                if (type.equals(RequestParamUtils.variable)) {
                    alertDialog.show();
                    productVariationAdapter.notifyDataSetChanged();
                }
            }
        });
        holder.tvName.setText(list.get(position));
        holder.tvName.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_TRANSPARENT, Constant.PRIMARY_COLOR)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void getWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels / 8;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void showDialog(final ProductVariationAdapter productVariationAdapter) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_product_variation, null);
        dialogBuilder.setView(dialogView);
        RecyclerView rvProductVariation = dialogView.findViewById(R.id.rvProductVariation);
        TextView tvDone = dialogView.findViewById(R.id.tvDone);
        TextView tvCancel = dialogView.findViewById(R.id.tvCancel);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        rvProductVariation.setLayoutManager(mLayoutManager);
        rvProductVariation.setAdapter(productVariationAdapter);
        rvProductVariation.setNestedScrollingEnabled(false);
        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
//        alertDialog.show();
        tvCancel.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        tvDone.setBackgroundColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        tvCancel.setOnClickListener(view -> {
            alertDialog.dismiss();
//                showDialog(productVariationAdapter);
//                selectedPos = pos;
            // notifyDataSetChanged();
        });
        tvDone.setOnClickListener(view -> {
            if (!new CheckIsVariationAvailable().isVariationAvailable(ProductDetailActivity.combination, variationList, dialogList)) {
                toast.showToast(activity.getResources().getString(R.string.combition));
            } else {
                onItemClickListener.onItemClick(new CheckIsVariationAvailable().getVariationId(variationList, list), "", 0);
                toast.cancelToast();
                alertDialog.dismiss();
                notifyDataSetChanged();
            }
        });
    }

    public static class ProductColorViewHolder extends RecyclerView.ViewHolder {

        FrameLayout llMain, flTransparent;
        TextView tvName;

        public ProductColorViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            flTransparent = view.findViewById(R.id.flTransparent);
            tvName = view.findViewById(R.id.tvName);
        }
    }
}