package com.example.ciyashop.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ciyashop.R;
import com.example.ciyashop.interfaces.OnItemClickListener;
import com.example.ciyashop.model.WalletTransaction;
import com.example.ciyashop.utils.Constant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WalletTransactionAdapter extends RecyclerView.Adapter<WalletTransactionAdapter.WalletTransectionViewHolder> {

    private List<WalletTransaction.Transaction> list = new ArrayList<>();
    private final Activity activity;
    private final OnItemClickListener onItemClickListener;

    public WalletTransactionAdapter(Activity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    public void addAll(List<WalletTransaction.Transaction> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WalletTransectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_transection, parent, false);
        return new WalletTransectionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletTransectionViewHolder holder, int position) {

        WalletTransaction.Transaction transaction = list.get(position);

        holder.tvTitle.setText(transaction.getDetails());
        if (transaction.getType().equals("credit")) {
            holder.tvAmmount.setText("+ "+ transaction.getAmount()+" "+Constant.CURRENCYSYMBOL);
            holder.tvAmmount.setTextColor(activity.getResources().getColor(R.color.green));
        } else {
            holder.tvAmmount.setText("- " +transaction.getAmount()+" "+Constant.CURRENCYSYMBOL);
            holder.tvAmmount.setTextColor(activity.getResources().getColor(R.color.red));
        }
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm,dd/MM/yyyy");

        Date d = null;
        try {
            d = input.parse(transaction.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDate.setText(output.format(d));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class WalletTransectionViewHolder extends RecyclerView.ViewHolder {


        TextView tvTitle,tvAmmount;
        TextView tvDate;

        public WalletTransectionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAmmount = itemView.findViewById(R.id.tvAmmount);
            tvDate = itemView.findViewById(R.id.tvDate);


        }
    }
}
