package com.example.stock_watch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// J.C. Sciaccotta

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private final List<Stock> stockList = new ArrayList<>();  // Main content is here
    private RecyclerView recyclerView; // Layout's recyclerview
    private SwipeRefreshLayout swiper;
    private StockAdapter mAdapter; // Data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);

        mAdapter = new StockAdapter(stockList, this);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(this::doRefresh);

        List<Stock> tempList = new ArrayList<>();
        tempList.addAll(loadFile());

        boolean trueFalse = doNetCheck();
        if (trueFalse) {
            NameDownloaderRunnable nameLoaderRunnable = new NameDownloaderRunnable(this);
            new Thread(nameLoaderRunnable).start();
            doRefresh();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Updated Without a Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            for (Stock tempStock : tempList){
                stockList.add(new Stock (tempStock.getStockSymbol(),tempStock.getCompanyName(), 0.00, "0.00", 0.00));
            }
            Collections.sort(stockList);
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onPause() {
        saveStock();
        super.onPause();
    }

    @Override
    public void onClick(View v) {  // click listener called by ViewHolder clicks
        Toast.makeText(MainActivity.this, "You clicked!", Toast.LENGTH_SHORT).show();
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock s = stockList.get(pos);

        String symbol = s.getStockSymbol();
        if (symbol.trim().isEmpty()) {
            return;
        }

        String url = "https://www.marketwatch.com/investing/stock/" + symbol;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View v) {  // long click listener called by ViewHolder long clicks
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock stock = stockList.get(pos);
        String stockSymbol = stock.getStockSymbol();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol " + stockSymbol + "?");
        builder.setPositiveButton("Delete", (dialog, id) -> {
            mAdapter.notifyItemRemoved(pos);
            stockList.remove(stock);
            saveStock();
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
            Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Toast.makeText(v.getContext(), "LONG " + stock.toString(), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void doRefresh(){
        stockList.clear();
        mAdapter.notifyDataSetChanged();
        List<Stock> tempList = new ArrayList<>();
        tempList.addAll(loadFile());
        List<String> stringList = new ArrayList<>();
        for (Stock tempStock: tempList){
            String n = tempStock.getStockSymbol();
            stringList.add(n);
        }
        boolean trueFalse = doNetCheck();
        if (trueFalse){
            for (String s : stringList) {
                doDownload(s);
            }
        }
        else{
            stockList.addAll(tempList);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Updated Without a Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        swiper.setRefreshing(false);

        Toast.makeText(this, "List content refreshed", Toast.LENGTH_SHORT).show();
    }

    public void downloadFailed() {
        Toast.makeText(this, "Stock Name Information Did Not Download", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opt_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.addStock) {
            Toast.makeText(this, "You want to add a Stock", Toast.LENGTH_SHORT).show();
            boolean trueFalse = doNetCheck();

            if (trueFalse){
                addStock();
                return true;
            }
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("No Network Connection");
                builder.setMessage("Stock Cannot Be Added Without a Network Connection");
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    private List<Stock> loadFile() {

        Log.d(TAG, "loadFile: Loading JSON File");
        List<Stock> loadList = new ArrayList<>();
        try {
            InputStream is = getApplicationContext().openFileInput(getString(R.string.file_name));
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String stockSymbol = jsonObject.getString("symbol");
                String companyName = jsonObject.getString("companyName");
                double price = jsonObject.getDouble("latestPrice");
                String priceChange = jsonObject.getString("change");
                double changePct = jsonObject.getDouble("changePercent");
                Stock stock = new Stock(stockSymbol, companyName, price, priceChange, changePct);
                loadList.add(stock);
            }

        } catch (FileNotFoundException e) {
            Toast.makeText(this, getString(R.string.no_file), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loadList;
    }

    private void saveStock() {

        Log.d(TAG, "saveStock: Saving JSON File");

        try {
            FileOutputStream fos = getApplicationContext().
                    openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);

            PrintWriter printWriter = new PrintWriter(fos);
            printWriter.print(stockList);
            printWriter.close();
            fos.close();

            Log.d(TAG, "saveStock: JSON:\n" + stockList.toString());

            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private boolean doNetCheck() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Toast.makeText(MainActivity.this, getString(R.string.connected), Toast.LENGTH_SHORT).show();
            return true;

        } else {
            Toast.makeText(MainActivity.this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void addStock(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String cityName = et.getText().toString().trim().replaceAll(", ", ",");
                getStockSymbols(cityName);
                Toast.makeText(MainActivity.this, "Search for this Stock!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setMessage("Please enter a Stock Symbol:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void getStockSymbols(String s){

        List<String>symbolList = new ArrayList<>();
        for (String key : NameDownloaderRunnable.stockMap.keySet()){
            if (key.contains(s)){
                symbolList.add(key);
                Collections.sort(symbolList);
            }
        }
        final CharSequence[] sArray = new CharSequence[symbolList.size()];
        for (int i = 0; i < symbolList.size(); i++) {
            sArray[i] = symbolList.get(i);
        }

        if (sArray.length == 1){
            doDownload(sArray[0].toString());
        }
        else if (sArray.length == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Symbol Not Found: " + s);
            builder.setMessage("Data for stock symbol");
            AlertDialog dialog = builder.create();
            dialog.show();
            Toast.makeText(MainActivity.this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Make a selection");

            builder.setItems(sArray, (dialog, which) -> doDownload(sArray[which].toString()));

            builder.setNegativeButton("Nevermind", (dialog, id) -> {
                Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
                return;
            });
            AlertDialog dialog = builder.create();

            dialog.show();
        }
    }
    public void doDownload(String s){
        StockDownloaderRunnable loaderTaskRunnable = new StockDownloaderRunnable(this, s);
        new Thread(loaderTaskRunnable).start();
    }

    public void updateData(Stock stock) {

        if (stock == null) {
            Toast.makeText(this, "Please Enter a Valid City Name", Toast.LENGTH_SHORT).show();
            return;
        }

        String arrow = (Double.parseDouble(stock.getPriceChange()) < 0) ? "▼" : "▲";

        String stockSymbol = stock.getStockSymbol();
        String companyName = stock.getCompanyName();
        double price = stock.getPrice();
        String priceChange = arrow + stock.getPriceChange();
        double changePct = stock.getChangePct();

        for (Stock newStock : stockList){
            String newStockSymbol = newStock.getStockSymbol();
            if (stockSymbol.equals(newStockSymbol)){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Duplicate Stock");
                builder.setIcon(R.drawable.img);
                builder.setMessage("Stock symbol " + stockSymbol + " is already displayed." );
                AlertDialog dialog = builder.create();
                dialog.show();
                return;
            }
        }
        stockList.add(
                new Stock(stockSymbol, companyName, price, priceChange, changePct));

                Collections.sort(stockList);

                mAdapter.notifyItemRangeChanged(0, stockList.size());

                saveStock();


    }
}