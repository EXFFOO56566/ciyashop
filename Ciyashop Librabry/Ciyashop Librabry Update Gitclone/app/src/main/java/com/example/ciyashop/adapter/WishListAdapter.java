package com.example.ciyashop.adapter;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ciyashop.library.apicall.GetApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CartActivity;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.customview.textview.TextViewRegular;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.CheckIsVariationAvailable;
import com.example.ciyashop.model.Cart;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.Variation;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
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
 * Created by User on 17-11-2017.
 */

public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.RecentViewHolder> implements OnResponseListner, OnItemClickListener {

    private List<CategoryList> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private final DatabaseHelper databaseHelper;

    AlertDialog alertDialog;
    private CustomToast toast;
    private boolean isDialogOpen = false;
    private int VariationPage = 1;
    private List<Variation> variationList = new ArrayList<>();
    private int defaultVariationId;

    public WishListAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
        databaseHelper = new DatabaseHelper(activity);
        this.toast = new CustomToast(activity);
    }

    public void addAll(List<CategoryList> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_row_wishlist, parent, false);
        return new RecentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecentViewHolder holder, int position) {
        holder.llDelete.setVisibility(View.VISIBLE);
        holder.tvCart.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        holder.ivCart.setColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        holder.llMain.setOnClickListener(view -> {
            if (list.get(position).type.equals(RequestParamUtils.external)) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(position).externalUrl));
                activity.startActivity(browserIntent);
            } else {
                Constant.CATEGORYDETAIL = list.get(position);
                Intent intent = new Intent(activity, ProductDetailActivity.class);
                intent.putExtra(RequestParamUtils.ID, list.get(position).id);
                activity.startActivity(intent);
            }
        });

        //Add product in cart if add to cart enable from admin panel

        if (!list.get(position).averageRating.equals("")) {
            holder.ratingBar.setRating(Float.parseFloat(list.get(position).averageRating));
        } else {
            holder.ratingBar.setRating(0);
        }

        Glide.with(activity.getBaseContext()).load(list.get(position).appthumbnail).into(holder.ivImage);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            tvProductName.setText(categoryList.name + "");
            holder.tvName.setText(Html.fromHtml(list.get(position).name + "", Html.FROM_HTML_MODE_LEGACY));
        } else {
//            tvProductName.setText(categoryList.name + "");
            holder.tvName.setText(Html.fromHtml(list.get(position).name + ""));
        }

        holder.tvPrice.setTextSize(15);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.tvPrice.setText(Html.fromHtml(list.get(position).priceHtml));
        }

        ((BaseActivity) activity).setPrice(holder.tvPrice, holder.tvPrice1, list.get(position).priceHtml);

        holder.llDelete.setOnClickListener(v -> {
            onItemClickListener.onItemClick(list.get(position).id, RequestParamUtils.delete, list.size());
            databaseHelper.deleteFromWishList(list.get(position).id + "");
            list.remove(position);
            notifyDataSetChanged();
        });

        if (Constant.IS_ADD_TO_CART_ACTIVE) {
            if (Config.IS_CATALOG_MODE_OPTION) {
                holder.llAddCart.setVisibility(View.GONE);
            } else {
                holder.llDelete.setVisibility(View.VISIBLE);
                if (!list.get(position).inStock) {
                    holder.tvCart.setText(activity.getString(R.string.out_of_stock));
                }
                if (!list.get(position).type.equals("variable")) {
                    if (databaseHelper.getProductFromCartById(list.get(position).id + "") != null) {
                        holder.tvCart.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
                        holder.ivCart.setColorFilter(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
                    } else {
                        holder.ivCart.setColorFilter(R.color.black);
                        holder.tvCart.setTextColor(activity.getResources().getColor(R.color.black));
                    }
                }
                holder.llAddCart.setOnClickListener(v -> {
                    if (list.get(position).inStock) {
                        if (list.get(position).type.equals("variable")) {
                            isDialogOpen = true;
                            callApi(position);
                        } else if (list.get(position).type.equals("simple")) {
                            holder.tvCart.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
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
                                onItemClickListener.onItemClick(list.get(position).id, RequestParamUtils.delete, list.size());
                                databaseHelper.deleteFromWishList(list.get(position).id + "");
                                list.remove(position);
                                notifyDataSetChanged();
                            }
                        }
                    } else {
                        toast = new CustomToast(activity);
                        toast.showToast(activity.getString(R.string.out_of_stock));
                        toast.showBlackBg();
                    }
                });
            }
        } else {
            holder.llAddCart.setVisibility(View.GONE);
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
                toast = new CustomToast(activity);
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
                    toast = new CustomToast(activity);
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
//                                toast.showBlackbg();
                            toast = new CustomToast(activity);
                            toast.showToast(activity.getString(R.string.item_added_to_your_cart));
                            onItemClickListener.onItemClick(list.get(position).id, RequestParamUtils.delete, list.size());
                            databaseHelper.deleteFromWishList(list.get(position).id + "");
                            list.remove(position);
                            notifyDataSetChanged();
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

        CategoryList.Image image = new CategoryList().getImageInstance();
        image.src = CheckIsVariationAvailable.imageSrc;
        list.get(position).images.add(image);
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

    public static class RecentViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain, llAddCart, llDelete;
        ImageView ivCart, ivImage, ivDelete;
        TextViewRegular tvPrice, tvName, tvPrice1, tvCart;
        RatingBar ratingBar;

        public RecentViewHolder(View view) {
            super(view);
            llMain = view.findViewById(R.id.llMain);
            llAddCart = view.findViewById(R.id.ll_AddCart);
            llDelete = view.findViewById(R.id.ll_Delete);
            ivCart = view.findViewById(R.id.iv_cart);
            ivImage = view.findViewById(R.id.ivImage);
            ivDelete = view.findViewById(R.id.iv_delete);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPrice1 = view.findViewById(R.id.tvPrice1);
            tvCart = view.findViewById(R.id.tvCart);
            ratingBar = view.findViewById(R.id.ratingBar);
            tvName = view.findViewById(R.id.tvName);
        }
    }


    @SuppressLint("LongLogTag")
    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.contains("getVariation_")) {
            ((BaseActivity) activity).dismissProgress();
            String[] separated = methodName.split("_");
//            String str = separated[0];
            String strPosition = separated[1];
            int positions = Integer.parseInt(strPosition);

            Log.e("onResponse: ", "position=" + positions);

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
}
