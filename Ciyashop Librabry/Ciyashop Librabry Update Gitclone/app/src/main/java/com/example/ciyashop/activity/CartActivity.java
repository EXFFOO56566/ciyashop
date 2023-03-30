package com.example.ciyashop.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ciyashop.library.apicall.Ciyashop;
import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.CartAdapter;
import com.example.ciyashop.databinding.ActivityCartBinding;
import com.example.ciyashop.databinding.LayoutEmptyBinding;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.helper.RecyclerItemTouchHelper;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Cart;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class CartActivity extends BaseActivity implements OnItemClickListener, OnResponseListner, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String TAG = "CartActivity";
    private CartAdapter cartAdapter;
    private DatabaseHelper databaseHelper;
    private Bundle bundle;
    private String id;
    private int buyNow;
    private String checkOutUrl, THANKYOU, HOMEURL, THANKYOUMAIN;
    private boolean isLogin;
    private String customerId;

    List<Cart> cartList;

    private ActivityCartBinding binding;
    private LayoutEmptyBinding emptyBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCartBinding.inflate(getLayoutInflater());
        emptyBinding = LayoutEmptyBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        emptyBinding.llEmpty.setVisibility(View.GONE);

        setClickEvent();
        setToolbarTheme();
        setThemeColor();
        setScreenLayoutDirection();
        getIntentData();
        setCartAdapter();
        settvTitle(getString(R.string.cart));
        showBackButton();
        hideSearchNotification();
        getCartData();
        setBottomBar("cart", binding.svHome);
        customerId = getPreferences().getString(RequestParamUtils.ID, "");
    }

    public void setThemeColor() {
        Drawable unwrappedDrawable = binding.tvContinue.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        binding.tvNoOfItems.setTextColor((Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        setEmptyColor();
    }

    public void getIntentData() {
        bundle = getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getString(RequestParamUtils.ID);
            buyNow = bundle.getInt(RequestParamUtils.buynow);
        }
    }

    public void setCartAdapter() {
        Log.e(TAG, "CartAdapter: " + "Called");

        cartAdapter = new CartAdapter(this, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvCart.setLayoutManager(mLayoutManager);
        binding.rvCart.setAdapter(cartAdapter);
        cartAdapter.isFromBuyNow(buyNow);
        binding.rvCart.setNestedScrollingEnabled(false);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvCart);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int adapterPosition) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            cartAdapter.removeItem(position);
            Snackbar.make(binding.llCart, "Item Deleted", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Only if you need to restore open/close state when
        // the orientation is changed
        if (cartAdapter != null) {
            cartAdapter.saveStates(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Only if you need to restore open/close state when
        // the orientation is changed
        if (cartAdapter != null) {
            cartAdapter.restoreStates(savedInstanceState);
        }
    }

    /**
     * {@link OnItemClickListener#onItemClick(int position, String value, int OuterPos)) onClick} may be used on the
     * method.
     *
     * @see OnItemClickListener
     */

    @Override
    public void onItemClick(int position, String value, int outerPos) {
        Log.e(TAG, "onItemClick: " + "Called");
        switch (value) {
            case RequestParamUtils.delete:
                if (cartAdapter.getList().size() == 0) {
                    isEmptyLayout(true);
                } else {
                    setTotalCount();
                }

                TextView tvBottomCartCount = findViewById(R.id.tvBottomCartCount);
                if (tvBottomCartCount != null) {
                    if (new DatabaseHelper(this).getFromCart(0).size() > 0) {
                        tvBottomCartCount.setText(String.valueOf(new DatabaseHelper(this).getFromCart(0).size()));
                        tvBottomCartCount.setVisibility(View.VISIBLE);
                    } else {
                        tvBottomCartCount.setVisibility(View.GONE);
                    }
                }
                break;
            case RequestParamUtils.increment:
            case RequestParamUtils.decrement:
                setTotalCount();
                break;
            case RequestParamUtils.detail:
                databaseHelper.deleteFromBuyNow(outerPos + "");
                break;
        }
    }

    public void isEmptyLayout(boolean isEmpty) {
        Log.e(TAG, "isEmptyLayout: " + "Called");
        if (isEmpty) {
            Log.e(TAG, "isEmptyLayout: isEmpty:  " + "Called");
            emptyBinding.llEmpty.setVisibility(View.VISIBLE);
            binding.llMain.setVisibility(View.GONE);
            emptyBinding.tvEmptyTitle.setText(R.string.cart_empty);
            emptyBinding.tvEmptyDesc.setText(R.string.browse_item);
        } else {
            Log.e(TAG, "isEmptyLayout: IsCart:  " + "Called");
            emptyBinding.llEmpty.setVisibility(View.GONE);
            binding.llMain.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    public void getCartData() {
        Log.e(TAG, "getCartData: " + "Gotted Cart Data");
        cartList = databaseHelper.getFromCart(buyNow);
        if (cartList.size() > 0) {
            for (int i = 0; i < cartList.size(); i++) {
                String product = cartList.get(i).getProduct();
                try {
                    CategoryList categoryListRider = new Gson().fromJson(product, new TypeToken<CategoryList>() {
                    }.getType());
                    cartList.get(i).setCategoryList(categoryListRider);
                } catch (Exception e) {
                    Log.e("Gson Exception", "in Recent Product Get" + e.getMessage());
                }
            }
            cartAdapter.addAll(cartList);
            setTotalCount();
        } else {
            Log.e(TAG, "getCartData: Empty: " + "Called");
            isEmptyLayout(true);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setTotalCount() {
        Log.e(TAG, "setTotalCount: " + "Setted Total Count");
        binding.tvTotalItem.setText(cartAdapter.getList().size() + " " + getString(R.string.items));
        binding.tvNoOfItems.setText(cartAdapter.getList().size() + " " + getString(R.string.items));

        for (int i = 0; i < cartAdapter.getList().size(); i++) {
            if (cartAdapter.getList().get(i).getCategoryList().priceHtml != null) {
                String price = Html.fromHtml(cartAdapter.getList().get(i).getCategoryList().priceHtml).toString();
                if (Constant.CURRENCYSYMBOL == null && !price.equals("")) {
                    Constant.CURRENCYSYMBOL = price.charAt(i) + "";
                    break;
                }
            }
        }

        float amount = 0;
        for (int i = 0; i < cartAdapter.getList().size(); i++) {
            if (cartAdapter.getList().get(i).getCategoryList().taxPrice != null &&
                    cartAdapter.getList().get(i).getCategoryList().taxPrice.length() > 0 &&
                    !cartAdapter.getList().get(i).getCategoryList().taxPrice.equals("0.0")) {
                amount = amount + (Float.parseFloat(getPrice(cartAdapter.getList().get(i).getCategoryList().price)) * cartAdapter.getList().get(i).getQuantity());
            } else {
                try {
                    amount = amount + (Float.parseFloat(getPrice(cartAdapter.getList().get(i).getCategoryList().price)) * cartAdapter.getList().get(i).getQuantity());
                } catch (Exception e) {
                    Log.e("Exception = ", e.getMessage());
                }
            }
        }

        switch (Constant.CURRENCYSYMBOLPOSTION) {
            case "left":
                binding.tvAmount.setText(Constant.CURRENCYSYMBOL + Constant.setDecimal((double) amount) + "");
                binding.tvTotalAmount.setText(Constant.CURRENCYSYMBOL + Constant.setDecimal((double) amount) + "");
                break;
            case "right":
                binding.tvAmount.setText(Constant.setDecimal((double) amount) + Constant.CURRENCYSYMBOL + "");
                binding.tvTotalAmount.setText(Constant.setDecimal((double) amount) + Constant.CURRENCYSYMBOL + "");
                break;
            case "left_space":
                binding.tvAmount.setText(Constant.CURRENCYSYMBOL + " " + Constant.setDecimal((double) amount) + "");
                binding.tvTotalAmount.setText(Constant.CURRENCYSYMBOL + " " + Constant.setDecimal((double) amount) + "");
                break;
            case "right_space":
                binding.tvAmount.setText(Constant.setDecimal((double) amount) + " " + Constant.CURRENCYSYMBOL + "");
                binding.tvTotalAmount.setText(Constant.setDecimal((double) amount) + " " + Constant.CURRENCYSYMBOL + "");
                break;
        }
    }

    public String getPrice(String price) {
        price = price.replace("\\s+", "");
        if (!Constant.THOUSANDSSEPRETER.equals(".")) {
            price = price.replace(Constant.THOUSANDSSEPRETER, "");
        }
        return price;
    }

    public void setClickEvent() {
        binding.tvContinue.setOnClickListener(v -> {
            if (Constant.IS_GUEST_CHECKOUT_ACTIVE) {
                addToCartCheckOut();
            } else {
                if (customerId == null || customerId.equals("")) {
                    isLogin = true;
                    Intent i = new Intent(this, LogInActivity.class);
                    startActivity(i);
                } else {
                    addToCartCheckOut();
                }
            }
        });

        emptyBinding.tvContinueShopping.setOnClickListener(v -> {
            Log.e(TAG, "setClickEvent: " + "Called");
            Intent i = new Intent(CartActivity.this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    public JSONArray getCartDataForAPI() {
        Log.e(TAG, "getCartDataForAPI: " + "Called");
        List<Cart> cartList = databaseHelper.getFromCart(buyNow);
        if (cartList.size() > 0) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < cartList.size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put(RequestParamUtils.PRODUCT_ID, cartList.get(i).getProductid() + "");
                    if (new Ciyashop(CartActivity.this).getPreferences()) {
                        object.put(RequestParamUtils.quantity, new Ciyashop(CartActivity.this).getQuantity() + "");
                    } else {
                        object.put(RequestParamUtils.quantity, cartList.get(i).getQuantity() + "");
                    }
                    if (cartList.get(i).getVariation() != null) {
                        JSONObject ob1 = new JSONObject(cartList.get(i).getVariation());
                        object.put(RequestParamUtils.variation, ob1);
                    }
                    object.put(RequestParamUtils.variationId, cartList.get(i).getVariationid() + "");
                    jsonArray.put(object);
                }
                return jsonArray;
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        }
        return null;
    }

    public void addToCartCheckOut() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.addToCart, this, getlanuage());
            try {
                JSONObject jsonObject = new JSONObject();
                String customerId = getPreferences().getString(RequestParamUtils.ID, "");
                jsonObject.put(RequestParamUtils.user_id, customerId);
                jsonObject.put(RequestParamUtils.cartItems, getCartDataForAPI());
                jsonObject.put(RequestParamUtils.os, RequestParamUtils.android);
                jsonObject.put(RequestParamUtils.deviceToken, Constant.DEVICE_TOKEN);
                Log.e(TAG, "addToCartCheckOut: " + jsonObject);
                postApi.callPostApi(new URLS().ADD_TO_CART + getPreferences().getString(RequestParamUtils.CurrencyText, ""), jsonObject.toString());
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(final String response, String methodName) {
        if (methodName.equals(RequestParamUtils.addToCart)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                Log.e("Response " + methodName, response);
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {

                        THANKYOUMAIN = jsonObj.getString(RequestParamUtils.THANKYOU);
                        THANKYOU = jsonObj.getString(RequestParamUtils.THANKYOUEND);
                        checkOutUrl = jsonObj.getString(RequestParamUtils.CHECKOUT_URL);
                        HOMEURL = jsonObj.getString(RequestParamUtils.HOME_URL);

                        if (!THANKYOUMAIN.isEmpty()) {
                            Constant.CheckoutURL.add(THANKYOUMAIN);
                        }
                        if (!THANKYOU.isEmpty()) {
                            Constant.CheckoutURL.add(THANKYOU);
                        }

                        Intent intent = new Intent(this, WebViewActivity.class);
                        intent.putExtra(RequestParamUtils.buynow, buyNow);
                        intent.putExtra(RequestParamUtils.THANKYOU, THANKYOU);
                        intent.putExtra(RequestParamUtils.THANKYOUExtra, THANKYOUMAIN);
                        intent.putExtra(RequestParamUtils.CHECKOUT_URL, checkOutUrl);
                        intent.putExtra(RequestParamUtils.HOME_URL, jsonObj.getString(RequestParamUtils.HOME_URL));
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }

        else if (methodName.equals(RequestParamUtils.IsUserExists)){
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
//                        {
//                            "is_user_exists": "true"
//                        }
//
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("is_user_exists");
                    Log.e("TAG", "onResponse:+++++ "+""+status );
                    if (!status.equals("true")) {
                        setLogoutFornonExistUserDialog();
                     //   Toast.makeText(this, "No Exist", Toast.LENGTH_LONG).show();
                       
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                    Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        databaseHelper.deleteFromBuyNow(id);
        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("Cart Activity", "On Restart Called");
        customerId = getPreferences().getString(RequestParamUtils.ID, "");
        getCartData();
        if (isLogin) {
            isLogin = false;
            if (!customerId.equals("")) {
                addToCartCheckOut();
            }
        }
    }

}
