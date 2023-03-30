package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CartActivity;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Cart;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.CustomToast;
import com.example.ciyashop.utils.RequestParamUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class GroupProductAdapter extends RecyclerView.Adapter<GroupProductAdapter.GroupProductViewHolder> {

    private List<CategoryList> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private final DatabaseHelper databaseHelper;
    private CustomToast toast;

    public GroupProductAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
        databaseHelper = new DatabaseHelper(activity);
        this.toast = new CustomToast(activity);
    }

    public void addAll(List<CategoryList> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public List<CategoryList> getList() {
        return list;
    }

    @NonNull
    @Override
    public GroupProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_product, parent, false);
        return new GroupProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GroupProductViewHolder holder, int position) {
        holder.ivCart.setColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        if (position == list.size() - 1) {
            holder.tvDivider.setVisibility(View.GONE);
        } else {
            holder.tvDivider.setVisibility(View.VISIBLE);
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

        holder.tvPrice.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        holder.tvPrice.setTextSize(15);

        ((BaseActivity) activity).setPrice(holder.tvPrice, holder.tvPrice1, list.get(position).priceHtml);

        holder.tvDetail.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));

        holder.llMain.setOnClickListener(view -> {
            Constant.CATEGORYDETAIL = list.get(position);
            Intent intent = new Intent(activity, ProductDetailActivity.class);
            intent.putExtra(RequestParamUtils.ID, list.get(position).id);
            activity.startActivity(intent);
        });

        if (list.get(position).images.size() > 0) {
            holder.ivImage.setVisibility(View.VISIBLE);
            Glide.with(activity.getBaseContext()).load(list.get(position).images.get(0).src).into(holder.ivImage);
        } else {
            holder.ivImage.setVisibility(View.INVISIBLE);
        }

        if (Config.IS_CATALOG_MODE_OPTION) {
            holder.ivCart.setVisibility(View.GONE);
        } else {
            holder.ivCart.setVisibility(View.VISIBLE);
        }

        if (!list.get(position).type.equals("variable")) {
            if (databaseHelper.getProductFromCartById(list.get(position).id + "") != null) {
                holder.ivCart.setColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            } else {
                holder.ivCart.setColorFilter(R.color.black);
            }
        } else if (list.get(position).type.equals("variable")) {
            holder.ivCart.setVisibility(View.GONE);
        }

        holder.ivCart.setOnClickListener(v -> {
            if (list.get(position).inStock) {
                if (list.get(position).type.equals("variable")) {
                    holder.ivCart.setVisibility(View.GONE);
                } else if (list.get(position).type.equals("simple")) {
                    holder.ivCart.setVisibility(View.VISIBLE);
                    holder.ivCart.setColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
                    Cart cart = new Cart();
                    cart.setQuantity(1);
                    cart.setVariation("{}");
                    cart.setProduct(new Gson().toJson(list.get(position)));
                    cart.setVariationid(0);
                    cart.setProductid(list.get(position).id + "");
                    cart.setBuyNow(0);
                    cart.setManageStock(list.get(position).manageStock);
                    if (databaseHelper.getProductFromCartById(list.get(position).id + "") != null) {
                        databaseHelper.addToCart(cart);
                        Intent intent = new Intent(activity, CartActivity.class);
                        intent.putExtra("buynow", 0);
                        activity.startActivity(intent);
                    } else {
                        databaseHelper.addToCart(cart);
                        ((BaseActivity) activity).showCart();
                        toast = new CustomToast(activity);
                        toast.showToast(activity.getString(R.string.item_added_to_your_cart));
                        toast.showBlackBg();
                    }
                    onItemClickListener.onItemClick(position, list.get(position).id + "", 11459060);
                }
            } else {
                toast = new CustomToast(activity);
                toast.showToast(activity.getString(R.string.out_of_stock));
                toast.showBlackBg();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class GroupProductViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage, ivCart;
        LinearLayout llMain;
        TextView tvName, tvDivider, tvDetail;
        TextView tvPrice, tvPrice1;

        public GroupProductViewHolder(View view) {
            super(view);
            ivImage = view.findViewById(R.id.ivImage);
            ivCart = view.findViewById(R.id.ivCart);
            llMain = view.findViewById(R.id.llMain);
            tvName = view.findViewById(R.id.tvName);
            tvDivider = view.findViewById(R.id.tvDivider);
            tvDetail = view.findViewById(R.id.tvDetail);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPrice1 = view.findViewById(R.id.tvPrice1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}