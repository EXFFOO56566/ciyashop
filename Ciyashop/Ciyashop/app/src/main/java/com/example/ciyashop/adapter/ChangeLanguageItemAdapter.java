package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

public class ChangeLanguageItemAdapter extends RecyclerView.Adapter<ChangeLanguageItemAdapter.MyViewHolder> {

    public static final String TAG = "ChangeLanguageItemAdapter";
    private final LayoutInflater inflater;
    List<Home.WpmlLanguage> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;

    public ChangeLanguageItemAdapter(Activity activity, List<Home.WpmlLanguage> list, OnItemClickListener onItemClickListener) {
        inflater = LayoutInflater.from(activity);
        this.activity = activity;
        if (list != null) {
            this.list = list;
        }
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_custom_dialog, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (list.get(position).dispLanguage != null && list.get(position).dispLanguage.length() > 0) {
            String str = list.get(position).dispLanguage + " ( " + list.get(position).code + " )";
            holder.tvDisplayItems.setText(str);
        } else {
            holder.tvDisplayItems.setText("");
        }

        holder.ll_main.setOnClickListener(v -> {
            SharedPreferences.Editor pre = ((BaseActivity) activity).getPreferences().edit();
            ((BaseActivity) activity).getPreferences().edit().putString(RequestParamUtils.LANGUAGE, "").apply();
            pre.putString(RequestParamUtils.LANGUAGE, list.get(position).code);
            Config.IS_RTL = list.get(position).isRtl;
            pre.putBoolean(Constant.RTL, list.get(position).isRtl);
            ((BaseActivity) activity).getPreferences().edit().putBoolean(RequestParamUtils.iSSITELANGUAGECALLED, false).apply();
            //pre.putBoolean(RequestParamUtils.iSSITELANGUAGECALLED, false);
            pre.apply();
            onItemClickListener.onItemClick(position, list.get(position).code, position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDisplayItems;
        private final LinearLayout ll_main;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvDisplayItems = itemView.findViewById(R.id.tvDisplayitems);
            ll_main = itemView.findViewById(R.id.ll_main);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}