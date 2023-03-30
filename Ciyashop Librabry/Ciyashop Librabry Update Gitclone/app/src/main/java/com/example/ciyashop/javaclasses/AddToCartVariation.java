package com.example.ciyashop.javaclasses;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ciyashop.library.apicall.GetApi;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.activity.CartActivity;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.adapter.GroupProductAdapter;
import com.example.ciyashop.adapter.ProductVariationAdapter;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Cart;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.Variation;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.CustomToast;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddToCartVariation implements OnResponseListner, OnItemClickListener {

    private static final String TAG = "AddToCartVariation";
    private final Activity activity;
    private int VariationPage = 1;
    private List<Variation> variationList = new ArrayList<>();
    private int defaultVariationId;
    private final DatabaseHelper databaseHelper;
    private CustomToast toast;
    private final int page = 1;
    AlertDialog alertDialog;
    private CategoryList list;
    TextView tvDone;
    ImageView tvAddToCart;

    public AddToCartVariation(Activity activity) {
        this.activity = activity;
        this.databaseHelper = new DatabaseHelper(activity);
        this.toast = new CustomToast(activity);
    }

    public void addToCart(final ImageView tvAddToCart, String detail) {

        this.tvAddToCart = tvAddToCart;
        Drawable unwrappedDrawable = tvAddToCart.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        this.list = new Gson().fromJson(detail, new TypeToken<CategoryList>() {
        }.getType());
        String htmlPrice;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            htmlPrice = String.valueOf(Html.fromHtml(list.priceHtml + "", Html.FROM_HTML_MODE_COMPACT));
        } else {
            htmlPrice = (Html.fromHtml(list.priceHtml) + "");
        }

        if (Constant.IS_ADD_TO_CART_ACTIVE) {
            if (Config.IS_CATALOG_MODE_OPTION) {
                tvAddToCart.setVisibility(View.GONE);
            } else if (htmlPrice.equals("") && list.price.equals("")) {
                tvAddToCart.setVisibility(View.GONE);
            } else {
                tvAddToCart.setVisibility(View.VISIBLE);
                DrawableCompat.setTint(wrappedDrawable, Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
                if (list.type.equals(RequestParamUtils.grouped)) {
                    if (list.groupedProducts != null) {
                        for (int i = 0; i < list.groupedProducts.size(); i++) {
                            if (databaseHelper.getProductFromCartById(list.groupedProducts.get(i) + "") != null) {
                                tvAddToCart.setImageResource(R.drawable.ic_check);
                            } else {
                                tvAddToCart.setImageResource(R.drawable.ic_cart_white);
                                break;
                            }
                        }
                    }
                } else if (list.type.equals(RequestParamUtils.simple)) {
                    if (databaseHelper.getProductFromCartById(list.id + "") != null) {
                        tvAddToCart.setImageResource(R.drawable.ic_check);
                    } else {
                        tvAddToCart.setImageResource(R.drawable.ic_cart_white);
                    }
                } else {
//                    tvAddToCart.setText(activity.getResources().getString(R.string.add_to_Cart));
                }
                if (!list.inStock) {
                    tvAddToCart.setClickable(false);
                    unwrappedDrawable = tvAddToCart.getBackground();
                    wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                    DrawableCompat.setTint(wrappedDrawable, Color.RED);
                } else {
                    unwrappedDrawable = tvAddToCart.getBackground();
                    wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                    DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
                }
                tvAddToCart.setOnClickListener(v -> {
                    if (list.inStock) {
                        switch (list.type) {
                            case "variable":
                                callApi();
                                break;
                            case "simple":
                                tvAddToCart.setImageResource(R.drawable.ic_check);
                                Cart cart = new Cart();
                                cart.setQuantity(1);
                                cart.setVariation("{}");
                                cart.setProduct(new Gson().toJson(list));
                                cart.setVariationid(0);
                                cart.setProductid(list.id + "");
                                cart.setBuyNow(0);
                                cart.setManageStock(list.manageStock);
                                if (databaseHelper.getProductFromCartById(list.id + "") != null) {
                                    databaseHelper.addToCart(cart);
                                    Intent intent = new Intent(activity, CartActivity.class);
                                    intent.putExtra("buynow", 0);
                                    activity.startActivity(intent);
                                } else {
                                    databaseHelper.addToCart(cart);
                                    ((BaseActivity) activity).showCart();
                                    ((BaseActivity) activity).showCartAnimation();
                                    ((BaseActivity) activity).vibrateononadd();
                                    toast = new CustomToast(activity);
                                    toast.showToast(activity.getString(R.string.item_added_to_your_cart));
                                    toast.showBlackBg();
                                }
                                break;
                            case RequestParamUtils.grouped:
                                if (databaseHelper.getProductFromCartById(list.id + "") != null) {
                                    Intent intent = new Intent(activity, CartActivity.class);
                                    intent.putExtra(RequestParamUtils.buynow, 0);
                                    activity.startActivity(intent);
                                } else {
                                    StringBuilder groupId = new StringBuilder();
                                    for (int i = 0; i < list.groupedProducts.size(); i++) {
                                        if (groupId.toString().equals("")) {
                                            groupId.append(list.groupedProducts.get(i));
                                        } else {
                                            groupId.append(",").append(list.groupedProducts.get(i));
                                        }
                                    }
                                    getGroupProducts(groupId.toString());
                                }
                                break;
                        }
                    } else {
                        toast = new CustomToast(activity);
                        toast.showToast(activity.getString(R.string.out_of_stock));
                        toast.showBlackBg();
                    }
                });
            }
        } else {
            tvAddToCart.setVisibility(View.GONE);
        }
    }

    public void showGroupProduct(List<CategoryList> groupProductList) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.group_product_view, null);
        dialogBuilder.setView(dialogView);

        RecyclerView rvGroupProduct = dialogView.findViewById(R.id.rvGroupProduct);
        GroupProductAdapter groupProductAdapter = new GroupProductAdapter(activity, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        rvGroupProduct.setLayoutManager(mLayoutManager);
        rvGroupProduct.setAdapter(groupProductAdapter);
        groupProductAdapter.addAll(groupProductList);
        rvGroupProduct.setNestedScrollingEnabled(false);

        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        alertDialog.show();
    }

    public void callApi() {
        ((BaseActivity) activity).showProgress("");
        if (VariationPage == 1) {
            variationList = new ArrayList<>();
        }

        String strURl = new URLS().WOO_MAIN_URL + new URLS().WOO_PRODUCT_URL + "/" + list.id + "/" + new URLS().WOO_VARIATION;
        if (((BaseActivity) activity).getPreferences().getString(RequestParamUtils.CurrencyText, "").equals("")) {
            strURl = strURl + "?page=" + VariationPage;
        } else {
            strURl = strURl +  ((BaseActivity) activity).getPreferences().getString(RequestParamUtils.CurrencyText, "") + "&page=" + VariationPage;
        }

        GetApi getApi = new GetApi(activity, "getVariation_", this, ((BaseActivity) activity).getlanuage());
        new URLS();
        getApi.callGetApi(strURl);
    }

    public void showDialog() {
        RecyclerView rvProductVariation;
        ProductVariationAdapter productVariationAdapter;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_product_variation, null);
        dialogBuilder.setView(dialogView);

        rvProductVariation = dialogView.findViewById(R.id.rvProductVariation);
        tvDone = dialogView.findViewById(R.id.tvDone);
        TextView tvCancel = dialogView.findViewById(R.id.tvCancel);

        productVariationAdapter = new ProductVariationAdapter(activity, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        rvProductVariation.setLayoutManager(mLayoutManager);
        rvProductVariation.setAdapter(productVariationAdapter);
        rvProductVariation.setNestedScrollingEnabled(false);
        productVariationAdapter.addAll(list.attributes);
        productVariationAdapter.addAllVariationList(variationList);

        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
//        alertDialog.show();
        tvCancel.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvDone.setBackgroundColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        tvCancel.setOnClickListener(view -> alertDialog.dismiss());
        tvDone.setOnClickListener(view -> {

            if (alertDialog != null) {
                alertDialog.show();
            }
            Log.e(TAG, "onClick: " + new CheckIsVariationAvailable().isVariationAvailable(ProductDetailActivity.combination, variationList, list.attributes));
            if (!new CheckIsVariationAvailable().isVariationAvailable(ProductDetailActivity.combination, variationList, list.attributes)) {
                toast = new CustomToast(activity);
                toast.showToast(activity.getString(R.string.combition_doesnt_exist));
            } else {

                if (databaseHelper.getVariationProductFromCart(getCartVariationProduct())) {
                    //tvCart.setText(getResources().getString(R.string.go_to_cart));
                } else {
                    //tvCart.setText(getResources().getString(R.string.add_to_Cart));
                }
                Log.e(TAG, "onClick:list.inStock  " + list.attributes.size() + variationList.size());
                //changePrice();
                if (!new CheckIsVariationAvailable().isVariationAvailable(ProductDetailActivity.combination, variationList, list.attributes)) {
                    toast = new CustomToast(activity);
                    toast.showToast(activity.getString(R.string.variation_not_available));
                    toast.showRedBg();
                } else {
                    if (getCartVariationProduct() != null) {
                        Cart cart = getCartVariationProduct();
                        if (databaseHelper.getVariationProductFromCart(cart)) {
                            Intent intent = new Intent(activity, CartActivity.class);
                            intent.putExtra("buynow", 0);
                            activity.startActivity(intent);
                        } else {
                            CheckIsVariationAvailable checkIsVariationAvailable = new CheckIsVariationAvailable();
                            Log.e(TAG, "onClick:list.inStock varation  " + CheckIsVariationAvailable.inStock);
                            if (CheckIsVariationAvailable.inStock) {
                                Log.e(TAG, "onClick: " + getCartVariationProduct().getStockQuantity());
                                databaseHelper.addVariationProductToCart(getCartVariationProduct());
                                ((BaseActivity) activity).showCart();
                                ((BaseActivity) activity).showCartAnimation();
                                ((BaseActivity) activity).vibrateononadd();
//                                toast.showBlackbg();
                                toast = new CustomToast(activity);
                                toast.showToast(activity.getString(R.string.item_added_to_your_cart));
                                //toast.cancelToast();
                                alertDialog.dismiss();
                            } else {
                                toast = new CustomToast(activity);
                                toast.showToast(activity.getString(R.string.out_of_stock));
                            }
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

    public Cart getCartVariationProduct() {
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
        list.priceHtml = CheckIsVariationAvailable.priceHtml;
        list.price = CheckIsVariationAvailable.price + "";


        //list.images.set(0,"")
        if (CheckIsVariationAvailable.imageSrc != null && !CheckIsVariationAvailable.imageSrc.contains(RequestParamUtils.placeholder)) {
            list.appthumbnail = CheckIsVariationAvailable.imageSrc;
        }
        if (!list.manageStock) {
            list.manageStock = CheckIsVariationAvailable.isManageStock;
        }
        list.images.clear();

        cart.setVariationid(new CheckIsVariationAvailable().getVariationId(variationList, lists));
        cart.setProductid(list.id + "");
        cart.setBuyNow(0);
        cart.setManageStock(list.manageStock);
        cart.setStockQuantity(CheckIsVariationAvailable.stockQuantity);
        cart.setProduct(new Gson().toJson(list));

        CategoryList.Image image = new CategoryList().getImageInstance();
        image.src = CheckIsVariationAvailable.imageSrc;
        list.images.add(image);
        cart.setProduct(new Gson().toJson(list));
        return cart;
    }

    public void getGroupProducts(String groupId) {
        if (Utils.isInternetConnected(activity)) {
            ((BaseActivity) activity).showProgress("");
            PostApi postApi = new PostApi(activity, RequestParamUtils.getGroupProducts, this, ((BaseActivity) activity).getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(RequestParamUtils.INCLUDE, groupId);
                jsonObject.put(RequestParamUtils.PAGE, page);
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
        if (methodName.contains("getVariation_")) {
            ((BaseActivity) activity).dismissProgress();
            Log.e(TAG, "onResponse: " + response);

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
                        callApi();
                    } else {
                        showDialog();
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
                if (jsonArray == null || jsonArray.length() != 10) {
                    getDefaultVariationId();
                }
            }
        } else if (methodName.equals(RequestParamUtils.getGroupProducts)) {
            ((BaseActivity) activity).dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<CategoryList> categoryLists = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String jsonResponse = jsonArray.get(i).toString();
                        CategoryList categoryListRider = new Gson().fromJson(
                                jsonResponse, new TypeToken<CategoryList>() {
                                }.getType());
                        categoryLists.add(categoryListRider);
                    }
                    showGroupProduct(categoryLists);
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                }
            }
        }
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
        Log.e(TAG, "On Item Click");
        Drawable unwrappedDrawable = tvAddToCart.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        if (outerPos == 11459060) {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            for (int i = 0; i < list.groupedProducts.size(); i++) {
                if (databaseHelper.getProductFromCartById(list.groupedProducts.get(i) + "") != null) {
                    DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
                } else {
                    DrawableCompat.setTint(wrappedDrawable, (Color.parseColor("#333333")));
                    break;
                }
            }
        } else {
            if (getCartVariationProduct() != null) {
                Cart cart = getCartVariationProduct();

                if (databaseHelper.getVariationProductFromCart(cart)) {
                    tvDone.setText(activity.getString(R.string.go_to_cart));
                } else {
                    tvDone.setText(activity.getString(R.string.done));
                }
            } else {
                tvDone.setText(activity.getString(R.string.done));
            }
        }


    }

}

