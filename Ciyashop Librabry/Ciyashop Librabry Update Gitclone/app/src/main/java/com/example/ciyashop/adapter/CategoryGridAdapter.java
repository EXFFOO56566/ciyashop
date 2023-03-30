package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.customview.like.animation.SparkButton;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.javaclasses.AddToCartVariation;
import com.example.ciyashop.javaclasses.AddToWishList;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridAdapter.CategoryGridHolder> {

    private static final String TAG = "CategoryGridAdapter";
    List<CategoryList> list = new ArrayList<>();
    private final Activity activity;
    RequestManager manager;

    public CategoryGridAdapter(Activity activity) {
        this.activity = activity;

         manager = Glide.with(activity.getBaseContext());
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

        //Add product in cart if add to cart enable from admin panel
        new AddToCartVariation(activity).addToCart(holder.ivAddToCart, new Gson().toJson(list.get(position)));
//
        //Add product in wishlist and remove product from wishlist and check wishlist enable or not
        new AddToWishList(activity).addToWishList(holder.ivWishList, new Gson().toJson(list.get(position)), holder.tvPrice1);

        if (!list.get(position).averageRating.equals("")) {
            holder.ratingBar.setRating(Float.parseFloat(list.get(position).averageRating));
        } else {
            holder.ratingBar.setRating(0);
        }
        if (list.get(position).appthumbnail != null) {
            // holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);


            holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            RequestOptions myOptions = new RequestOptions()
                    .override(holder.ivImage.getWidth(), holder.ivImage.getHeight())
                    .centerCrop();
//            Glide.with(activity)
//                    .asBitmap().format(DecodeFormat.PREFER_ARGB_8888)
//                    .load(list.get(position).appthumbnail)
//                    .apply(myOptions)
//                    .error(R.drawable.placeholder)
//                    .transform(new RoundedCorners(5))
//                    .placeholder(R.drawable.placeholder)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(holder.ivImage);


            manager.load(list.get(position).appthumbnail)
                    .apply(myOptions)
                    .error(R.drawable.placeholder)
                    .transform(new RoundedCorners(5))
                    .placeholder(R.drawable.placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
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

        holder.ll_content.setOnClickListener(v -> ClickProduct(position));

        ViewTreeObserver vto = holder.ivImage.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                holder.ivImage.getViewTreeObserver().removeOnPreDrawListener(this);
//                Log.e("Height: " + holder.ivImage.getMeasuredHeight(), " Width: " + holder.ivImage.getMeasuredWidth());
                return true;
            }
        });

        if (!list.get(position).type.contains(RequestParamUtils.variable) && list.get(position).onSale) {
            ((BaseActivity) activity).showDiscount(holder.tvDiscount, list.get(position).salePrice, list.get(position).regularPrice);
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }
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
            Log.e(TAG, "ClickProduct: " + new Gson().toJson(list.get(position)));
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

    @Override
    public long getItemId(int position) {
        return list.get(position).id;
//        return super.getItemId(position);
    }

    public static class CategoryGridHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain, ll_content, main;
        RatingBar ratingBar;
        ImageView ivImage, ivAddToCart;
        SparkButton ivWishList;
        TextView tvName;
        TextViewRegular tvPrice, tvPrice1, tvDiscount;
        FrameLayout llSale;

        public CategoryGridHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            ll_content = view.findViewById(R.id.ll_content);
            main = view.findViewById(R.id.main);
            ratingBar = view.findViewById(R.id.ratingBar);
            ivImage = view.findViewById(R.id.ivImage);
            ivAddToCart = view.findViewById(R.id.ivAddToCart);
            ivWishList = view.findViewById(R.id.ivWishList);
            tvName = view.findViewById(R.id.tvName);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPrice1 = view.findViewById(R.id.tvPrice1);
            tvDiscount = view.findViewById(R.id.tvDiscount);
            llSale = view.findViewById(R.id.llSale);
        }
    }
}
