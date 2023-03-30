package com.example.ciyashop.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.drawable.DrawableCompat;

import com.ciyashop.library.apicall.PostApi;
import com.ciyashop.library.apicall.interfaces.OnResponseListner;
import com.example.ciyashop.R;
import com.example.ciyashop.customview.bounceview.BounceView;
import com.example.ciyashop.databinding.ActivityDeleteAccountBinding;
import com.example.ciyashop.databinding.ToolbarBinding;
import com.example.ciyashop.utils.APIS;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;
import com.example.ciyashop.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class DeleteAccountActivity extends BaseActivity implements OnResponseListner {

    ActivityDeleteAccountBinding binding;
    ToolbarBinding toolbarbinding;
    private String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        toolbarbinding = ToolbarBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());
        settvTitle(getResources().getString(R.string.delete_account));
        setTheme();
        customerId = getPreferences().getString(RequestParamUtils.ID, "");
        Log.e("TAG", "onCreate: "+customerId +" "+getPreferences().getString(RequestParamUtils.email, ""));
        Clickevent();
    }

    private void setTheme() {

        toolbarbinding.ivBack.setColorFilter(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        toolbarbinding.ivSearch.setVisibility(View.INVISIBLE);
        binding.tvDeleleteAccount.setText(R.string.delete_account);
        binding.tvDelete.setText(R.string.delete);
        Drawable unwrappedDrawable =    binding.tvDelete.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, (Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));

    }

    private void Clickevent() {

        binding.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Utils.isValidEmail(binding.etEmail.getText().toString())) {

                    setWariningDialog();

                } else {
                    Toast.makeText(DeleteAccountActivity.this, R.string.enter_valid_email_address, Toast.LENGTH_SHORT).show();
                }


            }
        });

    }
    public void setWariningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Constant.delete_account_alert_message);
        builder.setTitle(Constant.delete_account_alert_title);
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.sure), (dialog, which) -> {

            deleteaccount();


        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> {

            dialog.cancel();


        });

        AlertDialog alert = builder.create();
        alert.show();

        BounceView.addAnimTo(alert);        //Call before showing the dialog

        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.parseColor(getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }
    public void deleteaccount() {
        if (Utils.isInternetConnected(this)) {
            showProgress("");
            PostApi postApi = new PostApi(DeleteAccountActivity.this, RequestParamUtils.deleteaccount, this, getlanuage());
            JSONObject object = new JSONObject();
            try {
                customerId = getPreferences().getString(RequestParamUtils.ID, "");
                object.put(RequestParamUtils.user_email, binding.etEmail.getText().toString());
                object.put(RequestParamUtils.user_id, customerId);
                object.put(RequestParamUtils.appVersions, new APIS().version);
                //if not value will be passed then bydefault it will be yes
                object.put(RequestParamUtils.sendconfirmemail, "yes");
                String token = getPreferences().getString(RequestParamUtils.NOTIFICATION_TOKEN, "");
                object.put(RequestParamUtils.deviceToken, token);
                Log.e("TAG", "deleteaccount: " + new APIS().MAIN_URL + RequestParamUtils.delete_account);


                postApi.callPostApi(new APIS().MAIN_URL + RequestParamUtils.delete_account, object.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.internet_not_working, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response, String methodName) {

        switch (methodName) {

            case RequestParamUtils.deleteaccount:
                dismissProgress();
                if (response != null && response.length() > 0) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        String status = jsonObj.getString("status");
                        String msg = jsonObj.getString("message");
                        if (status.equals("true")) {
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Log.e("TAG", "onResponse:false "+msg );
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(methodName + "Gson Exception is ", e.getMessage());
                        Toast.makeText(getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show(); //display in long period of time
                    }
                }
                break;

        }


    }
}