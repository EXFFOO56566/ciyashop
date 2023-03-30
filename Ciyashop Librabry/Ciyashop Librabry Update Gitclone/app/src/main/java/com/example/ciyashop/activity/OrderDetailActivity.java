package com.example.ciyashop.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.URLS;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.adapter.OrderDetailAdapter;
import com.example.ciyashop.databinding.ActivityOrderDetailBinding;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Orders;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends BaseActivity implements OnItemClickListener, OnResponseListner {
    private static final String TAG = OrderDetailActivity.class.getSimpleName();
    private final Orders orderData = Constant.ORDERDETAIL;
    private OrderDetailAdapter orderDetailAdapter;
    private final List<Orders.OrderTrackingData> list = new ArrayList<>();
    String trackUrl,upperString;
    private ActivityOrderDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setClickEvent();
        setScreenLayoutDirection();
        settvTitle(getResources().getString(R.string.order_detail));
        showBackButton();
        setToolbarTheme();
        setColorTheme();
        setData();
    }

    public void setColorTheme() {
        binding.tvOrderIdlabel.setTextColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        binding.tvOrderId.setTextColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        binding.tvTrackID.setTextColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));
        binding.tvOrderDateAndStatus.setTextColor(Color.parseColor(getPreferences().getString(Constant.APP_COLOR, Constant.PRIMARY_COLOR)));

        Drawable unwrappedDrawable = binding.tvCancelOrder.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        unwrappedDrawable = binding.tvOrderStatus.getBackground();
        wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

        //set background and corner
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(8);
        shape.setColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        // now find your view and add background to it
        View view = findViewById(R.id.llorder);
        view.setBackground(shape);

        //tvTotalAmountlabel.setTextColor(Color.parseColor(getPreferences().getString(tvTrackMessage2Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvTotal.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvTotalAmountlabel.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        // llorder.setBackgroundColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        binding.tvBillingAddresslabel.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.tvShippingAddresslabel.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));

        binding.ivBillingname.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivBillingname.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivBillingphoneno.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivBillingemail.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivShippingaddress.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivShippingname.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivShippingphoneno.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        binding.ivShippingemail.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    @SuppressLint("SetTextI18n")
    public void setData() {

        String orderId = "#" + orderData.id;

        try{

            if(orderData.status != null && !orderData.status.isEmpty()){
                upperString = orderData.status.substring(0, 1).toUpperCase() + orderData.status.substring(1);
            }

        }catch (Exception e){
            Log.e(TAG, "setData: "+e.getMessage() );
        }

//        if (Constant.IS_ORDER_TRACKING_ACTIVE && orderData.orderTrackingData.size() != 0) {
//
//            for (int i = 0; i < orderData.orderTrackingData.size(); i++) {
//                binding.tvTrackMessage1.setVisibility(View.VISIBLE);
//                binding.tvTrackMessage2.setVisibility(View.VISIBLE);
//                binding.tvTrackMessage1.setText(orderData.orderTrackingData.get(i).trackmessage1);
//                binding.tvTrackMessage2.setText(RequestParamUtils.Tracking);
//                trackUrl = orderData.orderTrackingData.get(i).ordertrackinglink;
//                binding.tvTrackID.setText(orderData.orderTrackingData.get(i).trackmessage2);
//                binding.tvTrackID.setClickable(orderData.orderTrackingData.get(i).usetrackbutton);
//            }
//
//
//
//
//        }
        if (Constant.IS_ORDER_TRACKING_ACTIVE && orderData.orderTrackingData.size() != 0) {
            Log.e("TAG", "setData:if "+Constant.IS_ORDER_TRACKING_ACTIVE);
            for (int i = 0; i < orderData.orderTrackingData.size(); i++) {
                binding.tvTrackMessage1.setVisibility(View.VISIBLE);
                binding.tvTrackMessage2.setVisibility(View.VISIBLE);
                binding.tvTrackID.setVisibility(View.VISIBLE);
                binding.tvTrackMessage1.setText(orderData.orderTrackingData.get(i).trackmessage1 );
                binding.tvTrackMessage2.setText(RequestParamUtils.Tracking);
                trackUrl = orderData.orderTrackingData.get(i).ordertrackinglink;
                Log.e("TAG", "setDataTrackurl: "+trackUrl );
                binding.tvTrackID.setPaintFlags( binding.tvTrackID.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
                binding.tvTrackID.setText(orderData.orderTrackingData.get(0).trackmessage2);


                binding.tvTrackID.setClickable(orderData.orderTrackingData.get(i).usetrackbutton == true);
            }

        } else {
            binding.tvTrackID.setVisibility(View.GONE);
            binding.tvTrackMessage1.setVisibility(View.GONE);
            binding.tvTrackMessage2.setVisibility(View.GONE);
        }

//       Locale uk = new Locale("en", "GB");
        Currency pound = Currency.getInstance(orderData.currency);
        String currencySymbol = pound.getSymbol();

        binding.tvOrderDateAndStatus.setText(getString(R.string.order) + " " + orderId + " " + getString(R.string.was_place_on) + " " + Constant.setDate(orderData.dateCreated) + " " + getString(R.string.and_currently) + " " + upperString);
        setProductList(currencySymbol);
        binding.tvOrderStatus.setText(upperString);

        if (orderData.status.equalsIgnoreCase("processing")) {
            Log.e("check", "onenter: ");
            Drawable unwrappedDrawable = binding.tvOrderStatus.getBackground();
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.green));
        } else if (orderData.status.equalsIgnoreCase("cancelled")) {
            Drawable unwrappedDrawable = binding.tvOrderStatus.getBackground();
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, Color.RED);
        } else if (orderData.status.equalsIgnoreCase("completed")) {
            Drawable unwrappedDrawable = binding.tvOrderStatus.getBackground();
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.green));
        } else if (orderData.status.equalsIgnoreCase("on-hold")) {
            Drawable unwrappedDrawable = binding.tvOrderStatus.getBackground();
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.green));
        }else{

        }

        float subtotalPrice = 0;
        for (int i = 0; i < orderData.lineItems.size(); i++) {
            subtotalPrice = subtotalPrice + Float.parseFloat(orderData.lineItems.get(i).total);
        }
        binding.tvSubTotal.setText(currencySymbol + " " + Constant.setDecimal((double) subtotalPrice));
        try {
            binding.tvShippingCharges.setText(currencySymbol + " " + Constant.setDecimal(Double.valueOf((orderData.shippingTotal))));
        } catch (Exception e) {
            binding.tvShippingCharges.setText(currencySymbol + " " + (orderData.shippingTotal));
        }

        try {
            binding.tvTotal.setText(currencySymbol + " " + Constant.setDecimal(Double.valueOf(orderData.total)));
        } catch (Exception e) {
            binding.tvTotal.setText(currencySymbol + " " + orderData.total);
        }
        binding.tvPaymentMethodTitle.setText(orderData.paymentMethodTitle + "");

        binding.tvBillingEmail.setText(orderData.billing.email + "");
        binding.tvShippingEmail.setText(orderData.billing.email + "");

        binding.tvBillingPhone.setText(orderData.billing.phone + "");
        binding.tvShippingPhone.setText(orderData.billing.phone + "");

        //tvBillingCompanyName.setText(orderData.billing.company + "");
        binding.tvBillingName.setText(orderData.billing.firstName + " " + orderData.billing.lastName);
        binding.tvBillingAddress.setText(orderData.billing.address1 + " " + orderData.billing.address2 + "," + orderData.billing.city + " " + orderData.billing.postcode + "");
        // tvBillingAddress2.setText(orderData.billing.address2 + "");
        // tvBillingCityPin.setText(orderData.billing.city + " " + orderData.billing.postcode);

        Locale lCountry = new Locale("", orderData.billing.country);
        String billingCountry = lCountry.getDisplayCountry();
        // tvBillingCountryState.setText(orderData.billing.state + ", " + billingCountry);

        // tvShippingCompanyName.setText(orderData.shipping.company + "");

        binding.tvShippingName.setText(orderData.shipping.firstName + " " + orderData.shipping.lastName);
        binding.tvShippingAddress.setText(orderData.shipping.address1 + " " + orderData.shipping.address2 + "" + orderData.shipping.city + " " + orderData.shipping.postcode + "");

        /* tvShippingAddress2.setText(orderData.shipping.address2 + "");
        tvShippingCityPin.setText(orderData.shipping.city + " " + orderData.shipping.postcode);*/

        Locale lCountryShip = new Locale("", orderData.shipping.country);
        String shippingCountryShip = lCountryShip.getDisplayCountry();
        // tvShippingCountryState.setText(orderData.shipping.state + ", " + shippingCountryShip);

        if (orderData.status.equalsIgnoreCase(RequestParamUtils.onHold) || orderData.status.equalsIgnoreCase(RequestParamUtils.pending)) {
            binding.tvCancelOrder.setClickable(true);
            binding.tvCancelOrder.setAlpha((float) 1);
        } else {
            binding.tvCancelOrder.setClickable(false);
            binding.tvCancelOrder.setAlpha((float) 0.3);
        }
    }

    public void setProductList(String currencySymbol) {
        orderDetailAdapter = new OrderDetailAdapter(this, this, currencySymbol);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvOrderedContent.setLayoutManager(mLayoutManager);
        binding.rvOrderedContent.setAdapter(orderDetailAdapter);
        binding.rvOrderedContent.setNestedScrollingEnabled(false);
        orderDetailAdapter.addAll(orderData.lineItems);
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
    }

    public void cancelOrder() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(this, RequestParamUtils.cancelOrder, this, getlanuage());
            JSONObject object = new JSONObject();
            try {
                object.put(RequestParamUtils.order, orderData.id + "");
                postApi.callPostApi(new URLS().CANCEL_ORDER, object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {
        if (methodName.equals(RequestParamUtils.cancelOrder)) {
            dismissProgress();
            if (response != null && response.length() > 0) {
                try {
                    //set call here
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.getString("result");
                    if (status.equals("success")) {
                        Toast.makeText(this, R.string.order_is_cancelled, Toast.LENGTH_SHORT).show();
                        binding.tvCancelOrder.setClickable(false);
                        binding.tvCancelOrder.setAlpha((float) 0.3);
                        finish();
                        //TODO:code here for cancelled order
                    } else {
                        Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(methodName + "Gson Exception is ", e.getMessage());
                    Toast.makeText(this, R.string.something_went_wrong_try_after_somtime, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
            }
        }
    }

    public void setClickEvent() {
        binding.tvCancelOrder.setOnClickListener(v -> cancelOrder());

        binding.tvTrackID.setOnClickListener(v -> {
            Log.e(TAG, "setClickEvent: "+trackUrl );
            if (!trackUrl.startsWith(RequestParamUtils.UrlStartWith) && !trackUrl.startsWith(RequestParamUtils.UrlStartWithsecure)) {
                trackUrl = RequestParamUtils.UrlStartWith + trackUrl;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trackUrl));
            startActivity(browserIntent);
        });
    }
}