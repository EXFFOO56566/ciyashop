package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.customview.like.animation.SparkButton;
import com.example.ciyashop.customview.textview.TextViewLight;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.AddToCartVariation;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.WishList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class SellerProductAdapter extends RecyclerView.Adapter<SellerProductAdapter.CategoryGridHolder> {

    private static final String TAG = "SellerProductAdapter";
    AlertDialog alertDialog;

    private List<CategoryList> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private final DatabaseHelper databaseHelper;

    public SellerProductAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
        databaseHelper = new DatabaseHelper(activity);
    }

    public void addAll(List<CategoryList> list) {
        for (CategoryList item : list) {
            add(item);
        }
//        this.list = list;
//        notifyDataSetChanged();
    }

    public void add(CategoryList item) {
        this.list.add(item);
        if (list.size() > 1) {
            notifyItemInserted(list.size() - 1);
        } else {
            notifyDataSetChanged();
        }
    }

    public void newList() {
        this.list = new ArrayList<>();
    }

    @NonNull
    @Override
    public CategoryGridHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grid_category, parent, false);
        return new CategoryGridHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryGridHolder holder, int position) {
        holder.llSale.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(view -> ClickProduct(position));
        if (!list.get(position).type.contains(RequestParamUtils.variable) && list.get(position).onSale) {
            ((BaseActivity) activity).showDiscount(holder.tvDiscount, list.get(position).salePrice, list.get(position).regularPrice);
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

        //Add product in cart if add to cart enable from admin panel
        new AddToCartVariation(activity).addToCart(holder.ivAddToCart, new Gson().toJson(list.get(position)));

        if (!list.get(position).averageRating.equals("")) {
            holder.ratingBar.setRating(Float.parseFloat(list.get(position).averageRating));
        } else {
            holder.ratingBar.setRating(0);
        }
        if (list.get(position).appthumbnail != null) {
            Glide.with(activity.getBaseContext())
                    .asBitmap().format(DecodeFormat.PREFER_ARGB_8888)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.no_image_available)
                    .load(list.get(position).appthumbnail)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.no_image_available);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvName.setText(Html.fromHtml(list.get(position).name, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvName.setText(Html.fromHtml(list.get(position).name));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml));
        }
        holder.tvPrice.setTextSize(15);
        ((BaseActivity) activity).setPrice(holder.tvPrice, holder.tvPrice1, list.get(position).priceHtml);

        holder.ivWishList.setActivetint(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        holder.ivWishList.setColors(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)), Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));

        if (Constant.IS_WISH_LIST_ACTIVE) {
            holder.ivWishList.setVisibility(View.VISIBLE);
            holder.ivWishList.setChecked(databaseHelper.getWishlistProduct(list.get(position).id + ""));
        } else {
            holder.ivWishList.setVisibility(View.GONE);
        }

        holder.ivWishList.setOnClickListener(v -> {
            if (databaseHelper.getWishlistProduct(list.get(position).id + "")) {
                holder.ivWishList.setChecked(false);
                onItemClickListener.onItemClick(list.get(position).id, RequestParamUtils.delete, 0);
                databaseHelper.deleteFromWishList(list.get(position).id + "");
            } else {
                holder.ivWishList.setChecked(true);
                holder.ivWishList.playAnimation();
                WishList wishList = new WishList();
                wishList.setProduct(new Gson().toJson(list.get(position)));
                wishList.setProductid(list.get(position).id + "");
                databaseHelper.addToWishList(wishList);
                onItemClickListener.onItemClick(list.get(position).id, RequestParamUtils.insert, 0);

                String value = holder.tvPrice1.getText().toString();
                if (value.contains(Constant.CURRENCYSYMBOL)) {
                    value = value.replaceAll(Constant.CURRENCYSYMBOL, "");
                }
                if (value.contains(Constant.CURRENCYSYMBOL)) {
                    value = value.replace(Constant.CURRENCYSYMBOL, "");
                }
                value = value.replaceAll("\\s", "");
                value = value.replaceAll(",", "");
//                try {
//                    ((BaseActivity) activity).logAddedToWishlistEvent(String.valueOf(list.get(position).id), list.get(position).name, Constant.CURRENCYSYMBOL, Double.parseDouble(value));
//                } catch (Exception e) {
//                    Log.e("TAG", "Exception: " + e.getMessage());
//                }
            }
        });

        holder.llMain.setOnClickListener(v -> ClickProduct(position));
        ViewTreeObserver vto = holder.ivImage.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                holder.ivImage.getViewTreeObserver().removeOnPreDrawListener(this);
                Log.e("Height: " + holder.ivImage.getMeasuredHeight(), " Width: " + holder.ivImage.getMeasuredWidth());
                return true;
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull CategoryGridHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(activity.getBaseContext())
                .clear(holder.ivImage);
    }

    public void ClickProduct(int position) {
        if (list.get(position).type.equals(RequestParamUtils.external)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(position).externalUrl));
            activity.startActivity(browserIntent);
        } else {
            Constant.CATEGORYDETAIL = list.get(position);
            Intent intent = new Intent(activity, ProductDetailActivity.class);
            activity.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public static class CategoryGridHolder extends RecyclerView.ViewHolder {

        ImageView ivImage, ivAddToCart;
        TextView tvDiscount, tvName, tvPrice, tvPrice1;
        SparkButton ivWishList;
        LinearLayout llMain, llContent, main;
        RatingBar ratingBar;
        FrameLayout flImage, llSale;
        TextView tvSale;

        public CategoryGridHolder(View view) {
            super(view);
            ivImage = view.findViewById(R.id.ivImage);
            ivAddToCart = view.findViewById(R.id.ivAddToCart);
            tvDiscount = view.findViewById(R.id.tvDiscount);
            tvName = view.findViewById(R.id.tvName);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPrice1 = view.findViewById(R.id.tvPrice1);
            ivWishList = view.findViewById(R.id.ivWishList);
            llMain = view.findViewById(R.id.llMain);
            llContent = view.findViewById(R.id.ll_content);
            main = view.findViewById(R.id.main);
            ratingBar = view.findViewById(R.id.ratingBar);
            flImage = view.findViewById(R.id.flImage);
            llSale = view.findViewById(R.id.llSale);
            tvSale = view.findViewById(R.id.tvSale);
        }
    }
}