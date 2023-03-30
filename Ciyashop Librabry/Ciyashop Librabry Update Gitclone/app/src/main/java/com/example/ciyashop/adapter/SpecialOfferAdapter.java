package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.AddToCartVariation;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class SpecialOfferAdapter extends RecyclerView.Adapter<SpecialOfferAdapter.SpecialOfferViewHolder> implements OnResponseListner {

    private List<Home.Product> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private int width = 0;
    private final int height = 0;
    ImageView imageView;

    public SpecialOfferAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
        notifyDataSetChanged();
    }

    public void addAll(List<Home.Product> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    public List<Home.Product> getList() {
        return list;
    }

    @NonNull
    @Override
    public SpecialOfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_special_offer, parent, false);
        return new SpecialOfferViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SpecialOfferViewHolder holder, int position) {
        imageView = holder.ivImage;
        holder.llMain.getLayoutParams().width = width - (width / 3);
        //Add product in cart if add to cart enable from admin panel
        new AddToCartVariation(activity).addToCart(holder.ivAddToCart, new Gson().toJson(list.get(position)));
        holder.llMain.setOnClickListener(view -> getProductDetail(list.get(position).id));
        if (list.get(position).image != null) {
            //holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(activity.getBaseContext()).load(list.get(position).image)
                    .fitCenter()
                    .error(R.drawable.no_image_available)
                    .transform(new RoundedCorners(5))
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.no_image_available);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.tvName.setText(Html.fromHtml(list.get(position).title + "", Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvName.setText(Html.fromHtml(list.get(position).title + ""));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml));
        }
        String per = list.get(position).percentage + "% off";
        holder.tvOff.setText(per);
        holder.tvPrice.setTextSize(15);

        Drawable unwrappedDrawable = holder.tvOff.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        ((BaseActivity) activity).setPrice(holder.tvPrice, holder.tvPrice1, list.get(position).priceHtml);
    }

    @Override
    public int getItemCount() {
        return Math.min(list.size(), 4);
    }

    public void getWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void getProductDetail(String groupId) {
        if (Utils.isInternetConnected(activity)) {
            ((BaseActivity) activity).showProgress("");
            PostApi postApi = new PostApi(activity, RequestParamUtils.getProductDetail, this, ((BaseActivity) activity).getPreferences().getString(RequestParamUtils.LANGUAGE, ""));
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.INCLUDE, groupId);
                postApi.callPostApi(new URLS().PRODUCT_URL + ((BaseActivity) activity).getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("Json Exception", e.getMessage());
            }
        } else {
            Toast.makeText(activity, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.getProductDetail)) {
            if (response != null && response.length() > 0) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    CategoryList categoryListRider = new Gson().fromJson(
                            jsonArray.get(0).toString(), new TypeToken<CategoryList>() {
                            }.getType());
                    Constant.CATEGORYDETAIL = categoryListRider;
                    if (categoryListRider.type.equals(RequestParamUtils.external)) {

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(categoryListRider.externalUrl));
                        activity.startActivity(browserIntent);
                    } else {
                        Intent intent = new Intent(activity, ProductDetailActivity.class);
                        activity.startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
                ((BaseActivity) activity).dismissProgress();
            }
        }
    }

    public static class SpecialOfferViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        ImageView ivImage, ivAddToCart;
        TextView tvName;
        TextView tvOff, tvPrice, tvPrice1;

        public SpecialOfferViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            ivImage = view.findViewById(R.id.ivImage);
            ivAddToCart = view.findViewById(R.id.ivAddToCart);
            tvName = view.findViewById(R.id.tvName);
            tvOff = view.findViewById(R.id.tvOff);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPrice1 = view.findViewById(R.id.tvPrice1);
        }
    }
}