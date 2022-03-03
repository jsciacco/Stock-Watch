package com.example.stock_watch;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class MyViewHolder extends RecyclerView.ViewHolder {

    TextView stockSymbol;
    TextView companyName;
    TextView price;
    TextView priceChange;
    TextView changePct;

    MyViewHolder(View view) {
        super(view);
        stockSymbol = view.findViewById(R.id.stocksymbol);
        companyName = view.findViewById(R.id.stockname);
        price = view.findViewById(R.id.price);
        priceChange = view.findViewById(R.id.change);
        changePct = view.findViewById(R.id.percent);
    }

}
