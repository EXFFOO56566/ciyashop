package com.example.ciyashop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.customview.like.animation.SparkButton;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.javaclasses.AddToCartVariation;
import com.example.ciyashop.javaclasses.AddToWishList;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class RecentlyAddedAdapter extends RecyclerView.Adapter<RecentlyAddedAdapter.MyViewHolder> implements OnResponseListner {

    public static final String TAG = "ChangeLanguageItemAdapter";

    private final LayoutInflater inflater;
    List<Home.Product> list;
    private final Activity activity;
    private int width = 0, height = 0;
    ImageView imageView;

    public RecentlyAddedAdapter(Activity activity) {
        inflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    public void addAll(List<Home.Product> list) {
        this.list = list;
        getWidthAndHeight();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_product, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        imageView = holder.ivImage;
        holder.main.getLayoutParams().width = width;
        holder.llMain.getLayoutParams().width = width;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        if (Config.IS_RTL) {
            if (position == list.size() - 1) {
                params.setMargins(((BaseActivity) activity).dpToPx(10), 0, ((BaseActivity) activity).dpToPx(10), ((BaseActivity) activity).dpToPx(10));
            } else {
                params.setMargins(0, 0, ((BaseActivity) activity).dpToPx(10), ((BaseActivity) activity).dpToPx(10));
            }
            holder.main.setLayoutParams(params);
        } else {
            holder.main.getLayoutParams().width = width;
            holder.llMain.getLayoutParams().width = width;
        }

        if (!list.get(position).type.contains(RequestParamUtils.variable) && list.get(position).onSale) {
            ((BaseActivity) activity).showDiscount(holder.tvDiscount, list.get(position).salePrice, list.get(position).regularPrice);
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

        //Add product in cart if add to cart enable from admin panel
        new AddToCartVariation(activity).addToCart(holder.ivAddToCart, new Gson().toJson(list.get(position)));

        //Add product in wishlist and remove product from wishlist and check wishlist enable or not
        new AddToWishList(activity).addToWishList(holder.ivWishList, new Gson().toJson(list.get(position)), holder.tvPrice1);

        holder.ivWishList.setActivetint(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        holder.ivWishList.setColors(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)), Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));

        if (Constant.IS_ADD_TO_CART_ACTIVE) {
            holder.main.setOnClickListener(v -> {
                String productDetail = new Gson().toJson(list.get(position));
                CategoryList categoryListRider = new Gson().fromJson(
                        productDetail, new TypeToken<CategoryList>() {
                        }.getType());
                Constant.CATEGORYDETAIL = categoryListRider;
                if (categoryListRider.type.equals("external")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(categoryListRider.externalUrl));
                    activity.startActivity(browserIntent);
                } else {
                    Intent intent = new Intent(activity, ProductDetailActivity.class);
                    activity.startActivity(intent);
                }
            });
        } else {
            holder.main.setOnClickListener(view -> getProductDetail(String.valueOf(list.get(position).id)));
        }

        if (list.get(position).image != null) {
            //holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(activity.getBaseContext()).load(list.get(position).appThumbnail)
                    .fitCenter()
                    .error(R.drawable.no_image_available)
                    .transform(new RoundedCorners(5))
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.no_image_available);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvName.setText(Html.fromHtml(list.get(position).title + "", Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvName.setText(Html.fromHtml(list.get(position).title + ""));
        }

        holder.tvPrice.setTextSize(15);
        if (list.get(position).priceHtml != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml + "", Html.FROM_HTML_MODE_COMPACT));
            } else {
                holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml));
            }
        ((BaseActivity) activity).setPrice(holder.tvPrice, holder.tvPrice1, list.get(position).priceHtml);
        if (!list.get(position).rating.equals("") && list.get(position).rating != null) {
            holder.ratingBar.setRating(Float.parseFloat(list.get(position).rating));
        } else {
            holder.ratingBar.setRating(0);
        }
    }

    public void getWidthAndHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = (int) (displayMetrics.widthPixels / 2.5);
        height = width + width / 5;
    }

    public void getProductDetail(String groupId) {
        if (Utils.isInternetConnected(activity)) {
            ((BaseActivity) activity).showProgress("");
            PostApi postApi = new PostApi(activity, RequestParamUtils.getProductDetail, this, ((BaseActivity) activity).getlanuage());
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
    public int getItemCount() {
        return Math.min(list.size(), 8);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @SuppressLint("LongLogTag")
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

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain, ll_content, main;
        ImageView ivAddToCart, ivImage;
        TextViewRegular tvPrice, tvName, tvPrice1, tvDiscount;
        RatingBar ratingBar;
        SparkButton ivWishList;

        public MyViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            ll_content = view.findViewById(R.id.ll_content);
            main = view.findViewById(R.id.main);
            ivAddToCart = view.findViewById(R.id.ivAddToCart);
            ivImage = view.findViewById(R.id.ivImage);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvName = view.findViewById(R.id.tvName);
            tvPrice1 = view.findViewById(R.id.tvPrice1);
            tvDiscount = view.findViewById(R.id.tvDiscount);
            ratingBar = view.findViewById(R.id.ratingBar);
            ivWishList = view.findViewById(R.id.ivWishList);
        }
    }
}
