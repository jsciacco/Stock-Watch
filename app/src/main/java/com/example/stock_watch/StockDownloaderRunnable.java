package com.example.stock_watch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

    public class StockDownloaderRunnable implements Runnable {

        private static final String TAG = "StockDownloaderRunnable";

        private final MainActivity mainActivity;
        private final String stockName;

        private static final String stockURLpart1 = "https://cloud.iexapis.com/stable/stock/";
        private static final String stockURLpart2 = "/quote";

        private static final String yourAPIKey = "pk_239eb0cbc5844f36811490fa0938fffa";


        StockDownloaderRunnable(MainActivity mainActivity, String stockName) {
            this.mainActivity = mainActivity;
            this.stockName = stockName;
        }


        @Override
        public void run() {

            String finalURL = stockURLpart1+stockName+stockURLpart2;

            Uri.Builder buildURL = Uri.parse(finalURL).buildUpon();
            buildURL.appendQueryParameter("token", yourAPIKey);
            String urlToUse = buildURL.build().toString();
            Log.d(TAG, "doInBackground: " + urlToUse);

            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(urlToUse);

                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();

                if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                    handleResults(null);
                    return;
                }

                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }

                Log.d(TAG, "doInBackground: " + sb.toString());

            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ", e);
                handleResults(null);
                return;
            }
            handleResults(sb.toString());
        }

        public void handleResults(final String jsonString) {

            final Stock s = parseJSON(jsonString);
            mainActivity.runOnUiThread(() -> mainActivity.updateData(s));
        }

        private Stock parseJSON(String s) {

            try {
                JSONObject jObjMain = new JSONObject(s);

                String stockSymbol = jObjMain.getString("symbol");
                String companyName = jObjMain.getString("companyName");
                double price = jObjMain.getDouble("latestPrice");
                String priceChange = jObjMain.getString("change");
                double changePct = jObjMain.getDouble("changePercent");

                return new Stock(stockSymbol, companyName, price, priceChange, changePct);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
