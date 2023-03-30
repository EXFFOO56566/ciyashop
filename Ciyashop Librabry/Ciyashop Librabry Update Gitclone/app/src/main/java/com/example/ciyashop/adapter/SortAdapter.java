package com.example.ciyashop.adapter;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;

import java.util.List;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class SortAdapter extends RecyclerView.Adapter<SortAdapter.SortViewHolder> {

    private List<String> list;
    private final Activity activity;
    private int selectedPosition = -1;

    public SortAdapter(Activity activity) {
        this.activity = activity;
    }

    public void addAll(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SortViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sort, parent, false);
        return new SortViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SortViewHolder holder, int position) {
        //check the radio button if both position and selectedPosition matches
        holder.rdSort.setChecked(position == selectedPosition);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.rdSort.setButtonTintList(ColorStateList.valueOf(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR))));
        } else {
            ColorStateList sl = new ColorStateList(new int[][]{
                    new int[]{-android.R.attr.state_checked},
                    new int[]{android.R.attr.state_checked}
            }, new int[]{
                    Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)) //disabled
                    , Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)) //enabled
            }
            );
            Drawable d = DrawableCompat.wrap(ContextCompat.getDrawable(holder.rdSort.getContext(), R.drawable.abc_btn_radio_material));
            DrawableCompat.setTintList(d, sl);
            holder.rdSort.setButtonDrawable(d);
        }

        //Set the position tag to both radio button and label
        holder.rdSort.setTag(position);
        holder.tvTitle.setTag(position);
        holder.rdSort.setOnClickListener(this::itemCheckChanged);
        holder.tvTitle.setText(list.get(position));
        holder.tvTitle.setOnClickListener(this::itemCheckChanged);
    }

    public static class SortViewHolder extends RecyclerView.ViewHolder {

        RadioButton rdSort;
        TextView tvTitle;

        public SortViewHolder(View view) {
            super(view);
            rdSort = view.findViewById(R.id.rdSort);
            tvTitle = view.findViewById(R.id.tvTitle);
        }
    }

    @Override
    public int getItemCount() {
        return (null != list ? list.size() : 0);
    }

    //Return the selectedPosition item
    public String getSelectedItem() {
        if (selectedPosition != -1) {
            Toast.makeText(activity, "Selected Item : " + list.get(selectedPosition), Toast.LENGTH_SHORT).show();
            return list.get(selectedPosition);
        }
        return "";
    }

    public int getSelectedPosition() {
        if (selectedPosition != -1) {
            return selectedPosition;
        }
        return 0;
    }

    //Delete the selected position from the list
    public void deleteSelectedPosition() {
        if (selectedPosition != -1) {
            list.remove(selectedPosition);
            selectedPosition = -1;//after removing selectedPosition set it back to -1
            notifyDataSetChanged();
        }
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;//after removing selectedPosition set it back to -1
        notifyDataSetChanged();
    }

    private void itemCheckChanged(View v) {
        selectedPosition = (Integer) v.getTag();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

}