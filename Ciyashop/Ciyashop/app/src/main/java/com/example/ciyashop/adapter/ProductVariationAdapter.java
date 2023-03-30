package com.example.ciyashop.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.activity.ProductDetailActivity;
import com.example.ciyashop.customview.textview.TextViewMedium;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.javaclasses.CheckIsVariationAvailable;
import com.example.ciyashop.model.CategoryList;
import com.example.ciyashop.model.Variation;
import com.example.ciyashop.utils.BaseActivity;
import com.example.ciyashop.utils.Constant;
import com.example.ciyashop.utils.CustomToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bhumi Shah on 11/7/2017.
 */

public class ProductVariationAdapter extends RecyclerView.Adapter<ProductVariationAdapter.ProductVariationViewHolder> implements OnItemClickListener {

    ProductVariationInnerAdapter productVariationInnerAdapter;
    HashMap<Integer, String> combination = new HashMap<>();

    private List<CategoryList.Attribute> list = new ArrayList<>();
    private List<CategoryList.Attribute> selectedCombinationList = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;
    private List<Variation> variationList = new ArrayList<>();
    private int isFirstLoad;
    private final CustomToast toast;

    public ProductVariationAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
        toast = new CustomToast(activity);
    }

    public void addAll(List<CategoryList.Attribute> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void addAllVariationList(List<Variation> variationList) {
        this.variationList = variationList;
        isFirstLoad = 0;
        onItemClick(0, list.get(0).name + "->" + list.get(0).options.get(0), 0);
    }

    @NonNull
    @Override
    public ProductVariationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_detail_variation, parent, false);
        return new ProductVariationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProductVariationViewHolder holder, int position) {
        setVariationAdapter(holder.rvProductVariation, position);
        holder.tvTitle.setText(list.get(position).name);
        holder.tvTitle.setTextColor(Color.parseColor(((BaseActivity) activity).getPreferences().getString(Constant.SECOND_COLOR, Constant.SECONDARY_COLOR)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onItemClick(int position, String value, int outerPos) {
//        if (outerpos == 0) {
//            ProductColorAdapter.selectedpos = position;
//        }
        isFirstLoad = isFirstLoad + 1;
        String comb = "";
        combination.put(outerPos, value);
        ProductDetailActivity.combination = combination;
        Map<Integer, String> tempCombinationList = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            if (i > outerPos) {
                tempCombinationList.put(i, "");
            } else if (i == outerPos) {
                tempCombinationList.put(i, combination.get(i));
            } else {
                tempCombinationList.put(i, combination.get(i));
            }
        }
        list.get(outerPos).position = position;
        for (int i = 0; i < tempCombinationList.size(); i++) {
            if (comb.equals("")) {
                comb = String.format("%s%s", comb, tempCombinationList.get(i));
            } else {
                if (tempCombinationList.get(i) != null) {
                    if (!tempCombinationList.get(i).equals(""))
                        comb = String.format("%s!%s", comb, tempCombinationList.get(i));
                }
            }
        }

        if (outerPos > 0) {
            List<CategoryList.Attribute> temp = new CheckIsVariationAvailable().getVariationList(variationList, comb, list);
            if (temp.size() > 0) {
                List<CategoryList.Attribute> oldList = selectedCombinationList;
                selectedCombinationList = new ArrayList<>();
                for (int j = 0; j < list.size(); j++) {
                    if (j > outerPos && temp.size() > 0) {
                        try {
                            selectedCombinationList.add(temp.get(j));
                        } catch (IndexOutOfBoundsException e) {
                            selectedCombinationList.add(list.get(j));
                        }
                    } else {
                        selectedCombinationList.add(oldList.get(j));
                    }
                }
            } else {
                for (int j = 0; j < list.size(); j++) {
                    if (j > outerPos) {
                        selectedCombinationList.set(j, new CategoryList().getAttributeInstance());
                    }
                }
            }
        } else {
            if (new CheckIsVariationAvailable().getVariationList(variationList, comb, list).size() > 0) {
                selectedCombinationList = new ArrayList<>();
                selectedCombinationList.addAll(new CheckIsVariationAvailable().getVariationList(variationList, comb, list));
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (i > outerPos) {
                        if (tempCombinationList.size() > i && selectedCombinationList.size() >= i) {
                            selectedCombinationList.set(i, new CategoryList().getAttributeInstance());
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();

        if (isFirstLoad != 1) {
            if (!new CheckIsVariationAvailable().isVariationAvailable(combination, variationList, list)) {
                toast.showToast(activity.getResources().getString(R.string.combition));
            } else {
                toast.cancelToast();
            }
        } else {
            setCombination();
        }
        notifyDataSetChanged();
//        if (!new CheckIsVariationAvailable().isVariationAvailbale(combination, variationList, list)) {
        onItemClickListener.onItemClick(position, value, outerPos);
//        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setVariationAdapter(RecyclerView recyclerView, int pos) {
        productVariationInnerAdapter = new ProductVariationInnerAdapter(activity, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(productVariationInnerAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        productVariationInnerAdapter.setOutListId(list.get(pos).name);
        productVariationInnerAdapter.setOuterPosition(pos);
        if (selectedCombinationList.size() > 0) {
            try {
                if (selectedCombinationList.get(pos) != null) {
                    productVariationInnerAdapter.addAllVariationList(selectedCombinationList.get(pos).options);
                }
                productVariationInnerAdapter.previousSelectionPosition = list.get(pos).position;
            } catch (IndexOutOfBoundsException e) {
                productVariationInnerAdapter.addAllVariationList(list.get(pos).options);
                productVariationInnerAdapter.previousSelectionPosition = list.get(pos).position;
            }
        } else {
            productVariationInnerAdapter.previousSelectionPosition = list.get(pos).position;
        }
        productVariationInnerAdapter.addAll(list.get(pos).options, list.get(pos).newOptions);
    }

    public void setCombination() {
        if (list.size() == selectedCombinationList.size()) {
            for (int i = 0; i < list.size(); i++) {
                String value = selectedCombinationList.get(i).name + "->" + selectedCombinationList.get(i).options.get(0);
                combination.put(i, value);
                list.get(i).position = list.get(i).options.indexOf(selectedCombinationList.get(i).options.get(0));
            }
        }
    }

    public static class ProductVariationViewHolder extends RecyclerView.ViewHolder {

        RecyclerView rvProductVariation;
        TextViewMedium tvTitle;

        public ProductVariationViewHolder(View view) {
            super(view);
            rvProductVariation = view.findViewById(R.id.rvProductVariation);
            tvTitle = view.findViewById(R.id.tvTitle);
        }
    }
}