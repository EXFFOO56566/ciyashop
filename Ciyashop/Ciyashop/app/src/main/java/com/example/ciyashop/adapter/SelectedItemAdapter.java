package com.example.ciyashop.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import com.bumptech.glide.request.RequestOptions;
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
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectedItemAdapter extends RecyclerView.Adapter<SelectedItemAdapter.MyViewHolder> implements OnResponseListner {

    public static final String TAG = "ChangeLanguageItemAdapter";
    private final LayoutInflater inflater;
    List<Home.Product> list = new ArrayList<>();
    private final Activity activity;
    private int width = 0, height = 0;

    public SelectedItemAdapter(Activity activity) {
        inflater = LayoutInflater.from(activity);
        this.activity = activity;
        Log.e("TAG", "addAll: " + list.size());
    }

    public void addAll(List<Home.Product> list) {
        this.list = list;
        Log.e("TAG", "addAll: " + list.size());
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
//        holder.llMain.getLayoutParams().width = width;
        holder.llMain.getLayoutParams().height = height;
        Log.e("TAG", "AddCustomSectionon BindViewHolder ");
        if (!list.get(position).type.contains(RequestParamUtils.variable) && list.get(position).onSale) {
            ((BaseActivity) activity).showDiscount(holder.tvDiscount, list.get(position).salePrice, list.get(position).regularPrice);
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

        //Add product in cart if add to cart enable from admin panel
        new AddToCartVariation(activity).addToCart(holder.ivAddToCart, new Gson().toJson(list.get(position)));

        //Add product in wishlist and remove product from wishlist and check wishlist enable or not
        new AddToWishList(activity).addToWishList(holder.ivWishList, new Gson().toJson(list.get(position)), holder.tvPrice1);

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
            holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            RequestOptions myOptions = new RequestOptions()
                    .fitCenter() // or centerCrop
                    .override(holder.ivImage.getWidth(), holder.ivImage.getHeight());
            Glide.with(activity.getBaseContext())
                    .applyDefaultRequestOptions(myOptions)
                    .load(list.get(position).image)
                    .error(R.drawable.no_image_available)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.no_image_available);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
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
        int height_value = activity.getResources().getInteger(R.integer.height);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels / 2 - 10;
        height = width;
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
        return list.size();
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

        LinearLayout llMain, main, ll_content;
        ImageView ivAddToCart, ivImage;
        TextViewRegular tvPrice, tvName, tvPrice1, tvDiscount;
        RatingBar ratingBar;
        SparkButton ivWishList;

        public MyViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            main = view.findViewById(R.id.main);
            ll_content = view.findViewById(R.id.ll_content);
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
