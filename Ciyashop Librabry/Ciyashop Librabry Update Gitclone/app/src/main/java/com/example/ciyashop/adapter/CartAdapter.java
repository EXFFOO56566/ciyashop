package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.customview.swipeview.ViewBinderHelper;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Cart;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Cart> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0, height = 0;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    private final DatabaseHelper databaseHelper;
    String value;
    private int isBuyNow = 0;

    public CartAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
        databaseHelper = new DatabaseHelper(activity);
        binderHelper.setOpenOnlyOne(true);
    }

    public void addAll(List<Cart> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    public void isFromBuyNow(int isBuyNow) {
        this.isBuyNow = isBuyNow;
    }

    public List<Cart> getList() {
        return list;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        if (list != null && 0 <= position && position < list.size()) {
            // bindView start
//            final String data = list.get(position).getCartId() + "";

            Drawable tvIncrement = holder.tvIncrement.getBackground();
            Drawable rappedDrawable = DrawableCompat.wrap(tvIncrement);
            DrawableCompat.setTint(rappedDrawable, Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

            Drawable tvDecrement = holder.tvDecrement.getBackground();
            Drawable rappedDrawables = DrawableCompat.wrap(tvDecrement);
            DrawableCompat.setTint(rappedDrawables, Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

//            Drawable ivDeleteCart = holder.ivDeleteRight.getBackground();
//            Drawable rappedDrawableRight = DrawableCompat.wrap(ivDeleteCart);
//            DrawableCompat.setTint(rappedDrawableRight, Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

            //    holder.ivDelete.setColorFilter(activity.getResources().getColor(R.color.red));

            holder.ivDeleteRight.setColorFilter(activity.getResources().getColor(R.color.remove));

            // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
            // put an unique string id as value, can be any string which uniquely define the data
            holder.tvPrice.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            holder.tvPrice1.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            holder.txtVariation.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            holder.tvQuantity.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            //holder.llDeleteBackground.setBackgroundColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            holder.tvDeleteCartBG.setTextColor(activity.getResources().getColor(R.color.remove));

            binderHelper.closeLayout(position + "");
            if (!list.get(position).getCategoryList().averageRating.equals("")) {
                holder.ratingBar.setRating(Float.parseFloat(list.get(position).getCategoryList().averageRating));
            } else {
                holder.ratingBar.setRating(0);
            }
            if (list.get(position).getCategoryList().images.size() > 0) {
                holder.ivImage.setVisibility(View.VISIBLE);
                holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(activity.getBaseContext()).load(list.get(position).getCategoryList().appthumbnail)
                        .error(R.drawable.no_image_available)
//                        .fit()
                        .transform(new RoundedCorners(5))
                        .into(holder.ivImage);
            } else {
                holder.ivImage.setVisibility(View.INVISIBLE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            tvProductName.setText(categoryList.name + "");
                holder.tvName.setText(Html.fromHtml(list.get(position).getCategoryList().name + "", Html.FROM_HTML_MODE_LEGACY));
            } else {
//            tvProductName.setText(categoryList.name + "");
                holder.tvName.setText(Html.fromHtml(list.get(position).getCategoryList().name + ""));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.tvPrice.setText(Html.fromHtml(list.get(position).getCategoryList().priceHtml, Html.FROM_HTML_MODE_COMPACT));
            } else {
                holder.tvPrice.setText(Html.fromHtml(list.get(position).getCategoryList().priceHtml));
            }
            holder.tvPrice.setTextSize(15);

            ((BaseActivity) activity).setPrice(holder.tvPrice, holder.tvPrice1, list.get(position).getCategoryList().priceHtml);

            // holder.tvQuantity.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
            holder.tvQuantity.setText(String.valueOf(list.get(position).getQuantity()));

            holder.tvIncrement.setOnClickListener(v -> {
                int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
                quantity = quantity + 1;

                if (list.get(position).isManageStock()) {
                    if (quantity > list.get(position).getCategoryList().stockQuantity) {
                        Toast.makeText(activity, activity.getString(R.string.only) + " " + list.get(position).getCategoryList().stockQuantity + " " + activity.getString(R.string.quntity_is_avilable), Toast.LENGTH_SHORT).show();
                    } else {
                        holder.tvQuantity.setText(String.valueOf(quantity));
                        databaseHelper.updateQuantity(quantity, list.get(position).getProductid(), list.get(position).getVariationid() + "");
                        list.get(position).setQuantity(quantity);
                        onItemClickListener.onItemClick(position, RequestParamUtils.increment, quantity);
                    }
                } else {
                    holder.tvQuantity.setText(String.valueOf(quantity));
                    databaseHelper.updateQuantity(quantity, list.get(position).getProductid(), list.get(position).getVariationid() + "");
                    list.get(position).setQuantity(quantity);
                    onItemClickListener.onItemClick(position, RequestParamUtils.increment, quantity);
                }
            });

            holder.tvDecrement.setOnClickListener(v -> {
                int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
                quantity = quantity - 1;
                if (quantity < 1) {
                    quantity = 1;
                }
                holder.tvQuantity.setText(String.valueOf(quantity));
                databaseHelper.updateQuantity(quantity, list.get(position).getProductid(), list.get(position).getVariationid() + "");
                list.get(position).setQuantity(quantity);
                onItemClickListener.onItemClick(position, RequestParamUtils.decrement, quantity);
            });

            holder.llMain.setOnClickListener(view -> {
                if (isBuyNow == 0) {
                    if (list.get(position).getCategoryList().type.equals(RequestParamUtils.external)) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(position).getCategoryList().externalUrl));
                        activity.startActivity(browserIntent);
                    } else {
                        Constant.CATEGORYDETAIL = list.get(position).getCategoryList();
                        Intent intent = new Intent(activity, ProductDetailActivity.class);
                        intent.putExtra(RequestParamUtils.ID, list.get(position).getCategoryList().id);
                        activity.startActivity(intent);
                    }
                }
            });

            try {
                JSONObject jObject = new JSONObject(list.get(position).getVariation());
                Iterator<String> iterator = jObject.keys();
                value = "";
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    if (value.length() == 0) {
                        value = String.format("%s%s : %s", value, key, jObject.getString(key));
                    } else {
                        value = String.format("%s, %s : %s", value, key, jObject.getString(key));
                    }
                }
            } catch (Exception e) {
                Log.e("exception is ", e.getMessage());
            }

            if (value != null && !value.isEmpty()) {
                holder.txtVariation.setVisibility(View.VISIBLE);
                holder.txtVariation.setText(value);
            } else {
                holder.txtVariation.setVisibility(View.GONE);
            }

            holder.llDelete.setOnClickListener(v -> {
                if (list.get(position).getCategoryList().type.equals(RequestParamUtils.variable)) {
                    databaseHelper.deleteVariationProductFromCart(list.get(position).getProductid(), list.get(position).getVariationid() + "");
                } else {
                    databaseHelper.deleteFromCart(list.get(position).getProductid());
                }
                list.remove(position);
                onItemClickListener.onItemClick(position, RequestParamUtils.delete, 0);
                notifyDataSetChanged();
            });
            //bind view over
        }
    }

    public void removeItem(int position) {
        if (list.get(position).getCategoryList().type.equals(RequestParamUtils.variable)) {
            databaseHelper.deleteVariationProductFromCart(list.get(position).getProductid(), list.get(position).getVariationid() + "");
        } else {
            databaseHelper.deleteFromCart(list.get(position).getProductid());
        }
        list.remove(position);
        onItemClickListener.onItemClick(position, RequestParamUtils.delete, 0);
        notifyDataSetChanged();
    }

    public void saveStates(Bundle outState) {
        binderHelper.saveStates(outState);
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link Activity #onRestoreInstanceState(Bundle)}
     */
    public void restoreStates(Bundle inState) {
        binderHelper.restoreStates(inState);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage, tvDecrement, tvIncrement, ivDeleteRight;
        LinearLayout llDelete;
        public RelativeLayout llMain, llDeleteBackground;
        TextView tvQuantity, txtVariation, tvName;
        TextViewRegular tvPrice, tvPrice1;
        RatingBar ratingBar;
        TextViewRegular tvDeleteCartBG;

        public CartViewHolder(View view) {
            super(view);
            ivDeleteRight = view.findViewById(R.id.ivDeleteRight);
            tvDeleteCartBG = view.findViewById(R.id.tvDeleteCartBG);
            ivImage = view.findViewById(R.id.ivImage);
            tvDecrement = view.findViewById(R.id.tvDecrement);
            tvIncrement = view.findViewById(R.id.tvIncrement);
            llDelete = view.findViewById(R.id.ll_Delete);
            llMain = view.findViewById(R.id.llMain);
            tvQuantity = view.findViewById(R.id.tvQuantity);
            txtVariation = view.findViewById(R.id.txtVariation);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPrice1 = view.findViewById(R.id.tvPrice1);
            tvName = view.findViewById(R.id.tvName);
            ratingBar = view.findViewById(R.id.ratingBar);
            llDeleteBackground = view.findViewById(R.id.llDeleteBackground);
        }
    }

    public void getWidthAndHeight() {
        int height_value = activity.getResources().getInteger(R.integer.height);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels / 2 - 20;
        height = width - height_value;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}