package com.example.stock_watch;

import android.util.JsonWriter;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

public class Stock implements Comparable<Stock>, Serializable {

    private final String stockSymbol;
    private final String companyName;
    private final double price;
    private final String priceChange;
    private final double changePct;


    public Stock(String stockSymbol, String companyName, double price, String priceChange, double changePct) {
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.price = price;
        this.priceChange = priceChange;
        this.changePct = changePct;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }
    public String getCompanyName() {
        return companyName;
    }
    public double getPrice(){ return price;}
    public String getPriceChange(){ return priceChange;}
    public double getChangePct(){ return changePct;}


    @Override
    public int compareTo(Stock stock) {
        return stockSymbol.compareTo(stock.stockSymbol);
    }

    @NonNull
    @Override
    public String toString() {

        try {
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(sw);
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject();
            jsonWriter.name("symbol").value(getStockSymbol());
            jsonWriter.name("companyName").value(getCompanyName());
            jsonWriter.name("latestPrice").value(getPrice());
            jsonWriter.name("change").value(getPriceChange());
            jsonWriter.name("changePercent").value(getChangePct());
            jsonWriter.endObject();
            jsonWriter.close();
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
