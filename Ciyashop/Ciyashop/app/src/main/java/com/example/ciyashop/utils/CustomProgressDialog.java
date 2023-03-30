package com.example.ciyashop.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.ciyashop.R;


/**
 * Created by Bhumi Shah on 11/8/2017.
 */

public class CustomProgressDialog {

    public static CustomProgressDialog customProgressDialog;
    //TODO : VAriable Declaration
    public Context context;
    public Dialog dialogView;
    ImageView iv1;
    AnimationDrawable Anim;

    public CustomProgressDialog(Context context) {
        this.context = context;
    }

    public static CustomProgressDialog getCustomProgressDialog(Context context) {
        if (customProgressDialog == null) {
            customProgressDialog = new CustomProgressDialog(context);
        }
        return customProgressDialog;
    }



    public void showCustomDialog(Activity context) {
        dialogView = new Dialog(context);
        dialogView.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogView.setContentView(R.layout.layout_progress_dialog);

        iv1 = dialogView.findViewById(R.id.iv1);
       // iv1 = dialogView.findViewById(R.id.iv1);
        if (!context.isDestroyed()) {
//            GlideDrawableImageViewTarget ivSmily = new GlideDrawableImageViewTarget(iv1);
            Glide.with(context.getBaseContext()).load(R.raw.loader).into(iv1);



        }

        dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogView.show();
    }

    public Boolean isShowing() {

        try {
            if (dialogView.isShowing()) {
                return true;
            }
        } catch (Exception e) {
            Log.e("Exception is ", e.getMessage());
            return false;
        }
        return false;
    }


    //TODO : Dissmiss Dialog
    public void dissmissDialog() {
        try {
            if (dialogView.isShowing() && dialogView != null) {
                dialogView.dismiss();
            }
        } catch (Exception e) {
            Log.e("Exception is ", e.getMessage());
        }
    }
}
