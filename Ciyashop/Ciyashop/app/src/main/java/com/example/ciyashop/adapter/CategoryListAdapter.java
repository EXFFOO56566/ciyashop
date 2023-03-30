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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ciyashop.library.apicall.GetApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CartActivity;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.customview.like.animation.SparkButton;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.AddToCartVariation;
import com.example.ciyashop.javaclasses.AddToWishList;
import com.example.ciyashop.javaclasses.CheckIsVariationAvailable;
import com.example.ciyashop.model.Cart;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.Variation;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.CustomToast;
import com.example.ciyashop.utils.RequestParamUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.CategoryListHolder> implements OnResponseListner, OnItemClickListener {

    private static final String TAG = "CategoryListAdapter";
    AlertDialog alertDialog;
    private List<CategoryList> list = new ArrayList<>();
    private final Activity activity;
    private final DatabaseHelper databaseHelper;
    private CustomToast toast;
    private final boolean isDialogOpen = false;
    private int VariationPage = 1;
    private List<Variation> variationList = new ArrayList<>();
    private int defaultVariationId;

    public CategoryListAdapter(Activity activity) {
        this.activity = activity;
        toast = new CustomToast(activity);
        databaseHelper = new DatabaseHelper(activity);
    }

    public void addAll(List<CategoryList> list) {
        for (CategoryList item : list) {
            add(item);
        }
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
    public CategoryListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_category, parent, false);
        return new CategoryListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryListHolder holder, int position) {

        holder.itemView.setOnClickListener(view -> ClickProduct(position));

        //Add product in cart if add to cart enable from admin panel
        Log.e(TAG, "onBindViewHolder:kushal " + new Gson().toJson(list.get(position)));
        new AddToCartVariation(activity).addToCart(holder.ivCart, new Gson().toJson(list.get(position)));
//
        //Add product in wishlist and remove product from wishlist and check wishlist enable or noot
        new AddToWishList(activity).addToWishList(holder.ivWishList, new Gson().toJson(list.get(position)), holder.tvPrice1);

        holder.ivWishList.setActivetint(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));
        holder.ivWishList.setColors(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)), Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECOND_COLOR)));

        if (!list.get(position).averageRating.equals("")) {
            holder.ratingBar.setRating(Float.parseFloat(list.get(position).averageRating));
        } else {
            holder.ratingBar.setRating(0);
        }
        if (list.get(position).appthumbnail != null) {
            holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);


                RequestOptions myOptions = new RequestOptions()
                        .centerCrop() // or centerCrop
                        .override(800, 500);

            Glide.with(activity.getBaseContext())
                        .applyDefaultRequestOptions(myOptions)
                        .load(list.get(position).appthumbnail)
                        .error(R.drawable.no_image_available)

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

    public void callApi(int position) {
        ((BaseActivity) activity).showProgress("");
        if (VariationPage == 1) {
            variationList = new ArrayList<>();
        }
        GetApi getApi = new GetApi(activity, "getVariation_" + position, this, ((BaseActivity) activity).getlanuage());
        new URLS();
        getApi.callGetApi(URLS.WOO_MAIN_URL + new URLS().WOO_PRODUCT_URL + "/" + list.get(position).id + "/" + new URLS().WOO_VARIATION + "?page=" + VariationPage);

    }

    @Override
    public void onResponse(String response, String methodName) {
        ((BaseActivity) activity).dismissProgress();
        String[] separated = methodName.split("_");
//        String str = separated[0];
        String strPosition = separated[1];
        int positions = Integer.parseInt(strPosition);

        Log.e(TAG, "onResponse: " + positions);
        if (methodName.contains("getVariation_")) {
            JSONArray jsonArray = null;
            if (response != null && response.length() > 0) {
                try {
                    jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String jsonResponse = jsonArray.get(i).toString();
                        Variation variationRider = new Gson().fromJson(
                                jsonResponse, new TypeToken<Variation>() {
                                }.getType());
                        variationList.add(variationRider);
                    }
                    if (jsonArray.length() == 10) {
                        //more product available
                        VariationPage++;
                        callApi(positions);
                    } else {
                        showDialog(positions);
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
                if (jsonArray == null || jsonArray.length() != 10) {
                    getDefaultVariationId();
                }
            }
        }
    }

    public void showDialog(final int position) {
        RecyclerView rvProductVariation;
        ProductVariationAdapter productVariationAdapter;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_product_variation, null);
        dialogBuilder.setView(dialogView);

        rvProductVariation = dialogView.findViewById(R.id.rvProductVariation);
        TextViewRegular tvDone = dialogView.findViewById(R.id.tvDone);
        TextViewRegular tvCancel = dialogView.findViewById(R.id.tvCancel);

        productVariationAdapter = new ProductVariationAdapter(activity, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        rvProductVariation.setLayoutManager(mLayoutManager);
        rvProductVariation.setAdapter(productVariationAdapter);
        rvProductVariation.setNestedScrollingEnabled(false);
        productVariationAdapter.addAll(list.get(position).attributes);
        productVariationAdapter.addAllVariationList(variationList);

        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
//        alertDialog.show();
        tvCancel.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        tvDone.setBackgroundColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvCancel.setOnClickListener(view -> alertDialog.dismiss());
        tvDone.setOnClickListener(view -> {
            if (alertDialog != null) {
                alertDialog.show();
            }
            if (!new CheckIsVariationAvailable().isVariationAvailable(ProductDetailActivity.combination, variationList, list.get(position).attributes)) {
                toast.showToast(activity.getString(R.string.combition_doesnt_exist));
            } else {
                toast.cancelToast();
                alertDialog.dismiss();
                if (databaseHelper.getVariationProductFromCart(getCartVariationProduct(position))) {
                    //tvCart.setText(getResources().getString(R.string.go_to_cart));
                } else {
                    //tvCart.setText(getResources().getString(R.string.add_to_Cart));
                }
                //changePrice();

                if (!new CheckIsVariationAvailable().isVariationAvailable(ProductDetailActivity.combination, variationList, list.get(position).attributes)) {
                    toast.showToast(activity.getString(R.string.variation_not_available));
                    toast.showRedBg();
                } else {
                    if (getCartVariationProduct(position) != null) {
                        Cart cart = getCartVariationProduct(position);
                        if (databaseHelper.getVariationProductFromCart(cart)) {
                            Intent intent = new Intent(activity, CartActivity.class);
                            intent.putExtra("buynow", 0);
                            activity.startActivity(intent);
                        } else {
                            databaseHelper.addVariationProductToCart(getCartVariationProduct(position));
                            ((BaseActivity) activity).showCart();
                            toast = new CustomToast(activity);
//                                toast.showBlackbg();
                            toast.showToast(activity.getString(R.string.item_added_to_your_cart));
                        }
                    } else {
                        toast = new CustomToast(activity);
                        toast.showRedBg();
                        toast.showToast(activity.getString(R.string.variation_not_available));
                    }
                }
            }
        });
        alertDialog.show();
    }

    public void getDefaultVariationId() {
        Log.e("default variation id ", "called");
        List<String> list = new ArrayList<>();
        JSONObject object = new JSONObject();
        try {
            for (int i = 0; i < ProductDetailActivity.combination.size(); i++) {
                String value = ProductDetailActivity.combination.get(i);
                String[] valuearray = new String[0];
                if (value != null && value.contains("->")) {
                    valuearray = value.split("->");
                }
                if (valuearray.length > 0) {
                    object.put(valuearray[0], valuearray[1]);
                }
                list.add(ProductDetailActivity.combination.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        defaultVariationId = new CheckIsVariationAvailable().getVariationId(variationList, list);
        CategoryList.Image image = new CategoryList().getImageInstance();
        image.src = CheckIsVariationAvailable.imageSrc;
    }

    public Cart getCartVariationProduct(int position) {
        Log.e("getCartVariation", "called");
        List<String> lists = new ArrayList<>();
        JSONObject object = new JSONObject();
        try {
            for (int i = 0; i < ProductDetailActivity.combination.size(); i++) {
                String value = ProductDetailActivity.combination.get(i);
                String[] valuearray = new String[0];
                if (value != null && value.contains("->")) {
                    valuearray = value.split("->");
                }
                if (valuearray.length > 0) {
                    object.put(valuearray[0], valuearray[1]);
                }
                lists.add(ProductDetailActivity.combination.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Cart cart = new Cart();
        cart.setQuantity(1);
        cart.setVariation(object.toString());
        list.get(position).priceHtml = CheckIsVariationAvailable.priceHtml;
        list.get(position).price = CheckIsVariationAvailable.price + "";

        if (CheckIsVariationAvailable.imageSrc != null && !CheckIsVariationAvailable.imageSrc.contains(RequestParamUtils.placeholder)) {
            list.get(position).appthumbnail = CheckIsVariationAvailable.imageSrc;
        }
        if (!list.get(position).manageStock) {
            list.get(position).manageStock = CheckIsVariationAvailable.isManageStock;
        }

        //list.get(position).images.set(0,"")
        list.get(position).images.clear();
        cart.setVariationid(new CheckIsVariationAvailable().getVariationId(variationList, lists));
        cart.setProductid(list.get(position).id + "");
        cart.setBuyNow(0);
        cart.setManageStock(list.get(position).manageStock);
        cart.setStockQuantity(CheckIsVariationAvailable.stockQuantity);
        cart.setProduct(new Gson().toJson(list.get(position)));

        if (cart.getVariationid() != defaultVariationId) {
            CategoryList.Image image = new CategoryList().getImageInstance();
            image.src = CheckIsVariationAvailable.imageSrc;
            list.get(position).images.add(image);
        } else {
            CategoryList.Image image = new CategoryList().getImageInstance();
            image.src = CheckIsVariationAvailable.imageSrc;
            list.get(position).images.add(image);
        }
        cart.setProduct(new Gson().toJson(list.get(position)));
        return cart;
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
    public void onItemClick(int position, String value, int outerPos) {
    }

    public static class CategoryListHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        RatingBar ratingBar;
        ImageView ivImage, ivCart;
        SparkButton ivWishList;
        TextView tvName;
        TextViewRegular tvPrice, tvPrice1, tvDiscount;

        public CategoryListHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            ratingBar = view.findViewById(R.id.ratingBar);
            ivImage = view.findViewById(R.id.ivImage);
            ivCart = view.findViewById(R.id.ivCart);
            ivWishList = view.findViewById(R.id.ivWishList);
            tvName = view.findViewById(R.id.tvName);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPrice1 = view.findViewById(R.id.tvPrice1);
            tvDiscount = view.findViewById(R.id.tvDiscount);
        }
    }
}