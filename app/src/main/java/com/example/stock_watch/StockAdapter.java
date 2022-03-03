package com.example.stock_watch;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private static final String TAG = "StockAdapter";
    private final List<Stock> stockList;
    private final MainActivity mainAct;

    StockAdapter(List<Stock> empList, MainActivity ma) {
        this.stockList = empList;
        mainAct = ma;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_row, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.stockSymbol.setTextColor(Color.parseColor("#31F82C"));
        holder.companyName.setTextColor(Color.parseColor("#31F82C"));
        holder.price.setTextColor(Color.parseColor("#31F82C"));
        holder.priceChange.setTextColor(Color.parseColor("#31F82C"));
        holder.changePct.setTextColor(Color.parseColor("#31F82C"));
        Stock stock = stockList.get(position);
        String posNeg = stock.getPriceChange();
        holder.stockSymbol.setText(stock.getStockSymbol());
        holder.companyName.setText(stock.getCompanyName());
        holder.price.setText(String.format("%.2f", stock.getPrice()));
        holder.priceChange.setText(String.format("%.6s",stock.getPriceChange()));
        holder.changePct.setText(String.format("(%.2f%%)", stock.getChangePct()));

        if (posNeg.contains("▼")){
            holder.stockSymbol.setTextColor(Color.parseColor("#FF0000"));
            holder.companyName.setTextColor(Color.parseColor("#FF0000"));
            holder.price.setTextColor(Color.parseColor("#FF0000"));
            holder.priceChange.setTextColor(Color.parseColor("#FF0000"));
            holder.changePct.setTextColor(Color.parseColor("#FF0000"));
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

}