package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.example.ciyashop.R;
import com.example.ciyashop.helper.DatabaseHelper;
import com.example.ciyashop.model.Home;
import com.example.ciyashop.model.NavigationList;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Config;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.RequestParamUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by UV on 29-Nov-16.
 */
public class NavigationDrawerAdapter extends BaseAdapter {
    private final Activity context;
    private List<Home.MainCategory> list = new ArrayList<>();
    private final LayoutInflater inflater;
    private int separator;
    private final DatabaseHelper databaseHelper;

    public NavigationDrawerAdapter(Activity context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        databaseHelper = new DatabaseHelper(context);
    }

    public void addAll(List<Home.MainCategory> list) {
        this.list = list;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).mainCatName.contains(context.getResources().getString(R.string.order))) {
                if (Config.IS_CATALOG_MODE_OPTION) {
                    list.remove(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    public List<Home.MainCategory> getDrawerList() {
        return list;
    }

    public void setSeparator(int separator) {
        this.separator = separator;
    }

    public int getSeparator() {
        return this.separator;
    }

    public List<Home.MainCategory> getList() {
        return this.list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        NavigationDrawerViewHolder listViewHolder;
        listViewHolder = new NavigationDrawerViewHolder();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_nav, viewGroup, false);

            listViewHolder.tvName = convertView.findViewById(R.id.tvName);
            listViewHolder.tvCart = convertView.findViewById(R.id.tvToolCart);
            listViewHolder.llMain = convertView.findViewById(R.id.llMain);
            listViewHolder.tvDivider = convertView.findViewById(R.id.tvDivider);
            listViewHolder.tvDividerGray = convertView.findViewById(R.id.tvDividerGray);
            listViewHolder.ivLeft = convertView.findViewById(R.id.ivLeft);
            convertView.setTag(listViewHolder);

        } else {
            listViewHolder = (NavigationDrawerViewHolder) convertView.getTag();
        }

        if (i != separator) {
            listViewHolder.tvDivider.setVisibility(View.VISIBLE);
            listViewHolder.tvDividerGray.setVisibility(View.GONE);
        } else {
            listViewHolder.tvDivider.setVisibility(View.GONE);
            listViewHolder.tvDividerGray.setVisibility(View.VISIBLE);
        }
        if (list.get(i).mainCatName != null && !list.get(i).mainCatName.equals("")) {
            listViewHolder.tvName.setText(list.get(i).mainCatName);
        }
        if (i == separator) {
            listViewHolder.ivLeft.setImageResource(R.drawable.ic_more_white);

        } else if (i < separator) {
            if (list.get(i).mainCatImage != null && !list.get(i).mainCatImage.equals("")) {
                Glide.with(context).load(list.get(i).mainCatImage).into(listViewHolder.ivLeft);
            }
            listViewHolder.ivLeft.getLayoutParams().width = ((BaseActivity) context).dpToPx(40);
            listViewHolder.ivLeft.getLayoutParams().height = ((BaseActivity) context).dpToPx(40);
        } else {
            listViewHolder.ivLeft.setColorFilter(Color.parseColor(((BaseActivity) context).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
            NavigationList.getInstance(context);
            listViewHolder.ivLeft.setImageResource(NavigationList.getImageList().get(Integer.parseInt(list.get(i).mainCatId)));
            listViewHolder.ivLeft.getLayoutParams().width = ((BaseActivity) context).dpToPx(22);
            listViewHolder.ivLeft.getLayoutParams().height = ((BaseActivity) context).dpToPx(22);
        }

        if (list.get(i).mainCatName.equals(RequestParamUtils.myCart)) {
            if (databaseHelper.getFromCart(0).size() > 0) {
                listViewHolder.tvCart.setText(String.valueOf(databaseHelper.getFromCart(0).size()));
                listViewHolder.tvCart.setVisibility(View.VISIBLE);
            } else {
                listViewHolder.tvCart.setVisibility(View.GONE);
            }
        } else {
            listViewHolder.tvCart.setVisibility(View.GONE);
        }

        Drawable unwrappedDrawable = listViewHolder.tvCart.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.parseColor(((BaseActivity) context).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));


        return convertView;
    }
}

class NavigationDrawerViewHolder {
    TextView tvName, tvDivider, tvDividerGray;
    ImageView ivLeft;
    LinearLayout llMain;
    TextView tvCart;
}