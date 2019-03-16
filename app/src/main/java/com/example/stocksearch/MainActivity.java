package com.example.stocksearch;

import android.Manifest;
import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.Daily;
import org.patriques.output.timeseries.IntraDay;
import org.patriques.output.timeseries.Monthly;
import org.patriques.output.timeseries.Weekly;
import org.patriques.output.timeseries.data.StockData;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LifecycleObserver, StockSelectFragment.OnDataPass {
    DrawerLayout drawer;
    Boolean dataReceived;
    File stockDoc;
    Context mContext = this;
    stockListAdapter adapter;
    portfolioListAdapter padapter;
    ArrayList<stockItem> portfolioData;
    ListView searchListView;
    ListView portfolioListView;
    SearchView searchView;
    SearchView portfolioSearchView;
    Vibrator vibrator;
    Bundle bundle;
    AlphaVantageConnector apiConntector = apiConntector = new AlphaVantageConnector("6ERTTBIWEAQ4CCN7", 3000);
    NavigationView navigationView;
    ExecutorService aSyncThread = Executors.newSingleThreadExecutor();
    String nasdaqResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        //Checking for storage permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // aSyncThread.execute(new Runnable() {
        //@Override
        //public void run() {

        nasdaqResponse = nasdaqResponse;
        //Getting traded stocks data
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.setStrictReplyParsing(false);
            ftpClient.connect("ftp.nasdaqtrader.com");
            ftpClient.login("anonymous", "anonymous");
            ftpClient.changeWorkingDirectory("/SymbolDirectory");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            InputStream is = new BufferedInputStream(ftpClient.retrieveFileStream("nasdaqtraded.txt"));
            nasdaqResponse = convertStreamToString(is);

            ftpClient.logout();
            ftpClient.disconnect();

        } catch (SocketException e) {

        } catch (IOException e) {

        }

        // }
        //  });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            (this).getSupportActionBar().setTitle("Portfolio");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PortfolioFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_portfolio);
        }

        if (nasdaqResponse != "") {
            dataReceived = true;
            //Formatting Nasdaq data to be workable
            String[] nasdaqData = nasdaqResponse.split("\\n");

            ArrayList<String> allData = new ArrayList<String>();
            for (int i = 1; i < nasdaqData.length; ++i) {
                allData.add(nasdaqData[i]);
            }

            ArrayList<stockItem> listData = new ArrayList<stockItem>();
            String[] row = new String[12];

            for (int i = 0; i < allData.size(); ++i) {
                row = allData.get(i).split("\\|");
                if (row.length >= 2) {
                    String[] cleanName = row[2].split("-|,|\\.");
                    listData.add(new stockItem(row[1], cleanName[0]));
                }
            }


            //Initializing all data search list
            searchListView = (ListView) findViewById(R.id.stocklist);

            if (searchListView != null) {
                adapter = new stockListAdapter(mContext, listData);
                searchListView.setAdapter(adapter);
            }

            searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    vibrator.vibrate(25);

                    stockItem selectedItem = listData.get(position);

                    //Running on new thread - cannot have network operations on main
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            StockSelectFragment ssfrag = new StockSelectFragment();
                            ssfrag.setArguments(apiGetData(selectedItem.getTicker(), selectedItem.getSecurityName()));
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, ssfrag).commit();
                        }
                    });
                    t.start();

                    //Hiding views in stock select fragment
                    searchView.setVisibility(View.GONE);
                    searchListView.setVisibility(View.GONE);
                    getSupportActionBar().setTitle(selectedItem.getSecurityName());
                }
            });

            searchView = (SearchView) findViewById(R.id.searchStocks);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (TextUtils.isEmpty(s)) {
                        adapter.filter("");
                        searchListView.clearTextFilter();
                    } else {
                        adapter.filter(s);
                    }
                    return true;
                }
            });
            searchView.setVisibility(View.GONE);


            //Initializing user created portfolio list
            portfolioData = new ArrayList<stockItem>();
            portfolioListView = (ListView) findViewById(R.id.portfoliolist);

            if (portfolioListView != null) {
                padapter = new portfolioListAdapter(mContext, portfolioData);
                portfolioListView.setAdapter(padapter);
            }

            portfolioListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    vibrator.vibrate(25);

                    stockItem selectedItem = portfolioData.get(position);

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            StockSelectFragment ssfrag = new StockSelectFragment();
                            ssfrag.setArguments(apiGetData(selectedItem.getTicker(), selectedItem.getSecurityName()));
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, ssfrag).commit();
                        }
                    });
                    t.start();

                    //Hiding views in stock select fragment
                    portfolioSearchView.setVisibility(View.GONE);
                    portfolioListView.setVisibility(View.GONE);
                    getSupportActionBar().setTitle(selectedItem.getSecurityName());
                }
            });
            portfolioSearchView = (SearchView) findViewById(R.id.searchPortfolio);
            portfolioSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (TextUtils.isEmpty(s)) {
                        padapter.filter("");
                        portfolioListView.clearTextFilter();
                    } else {
                        padapter.filter(s);
                    }
                    return true;
                }
            });
            portfolioListView.setVisibility(View.VISIBLE);

        } else {

        }
    }

    public Bundle apiGetData(String ticker, String securityName) {

        //Alphavantage api calls to get data for selected stock

        TimeSeries stockTimeSeries = new TimeSeries(apiConntector);

        String securityname = securityName;
        String currentprice = "Unavailable";
        String open = "Unavailable";
        String close = "Unavailable";
        String low = "Unavailable";
        String high = "Unavailable";
        String volume = "Unavailable";
        String dividend = "Unavailable";
        String monthlyhigh = "Unavailable";
        String monthlylow = "Unavailable";
        ArrayList<Float> priceArrayDaily = new ArrayList<>();
        ArrayList<String> timeArrayDaily = new ArrayList<>();
        ArrayList<Float> priceArrayWeekly = new ArrayList<>();
        ArrayList<String> timeArrayWeekly = new ArrayList<>();
        ArrayList<Float> priceArrayMonthly = new ArrayList<>();
        ArrayList<String> timeArrayMonthly = new ArrayList<>();

        try {
            int currentDay;
            StockData data;

            //intra day info for chart and text views
            IntraDay intraDayChartResponse = stockTimeSeries.intraDay(ticker, Interval.ONE_MIN, OutputSize.FULL);
            List<StockData> stockDataIntraDayChart = intraDayChartResponse.getStockData();
            data = stockDataIntraDayChart.get(0);
            currentprice = String.format("%.2f", data.getOpen());
            volume = Long.toString(data.getVolume());

            currentDay = stockDataIntraDayChart.get(0).getDateTime().getDayOfMonth();
            for (int i=0; i < stockDataIntraDayChart.size(); ++i){
                StockData stock = stockDataIntraDayChart.get(i);
                if (stock.getDateTime().getDayOfMonth() == currentDay){
                    timeArrayDaily.add(Integer.toString(stock.getDateTime().getHour()) + ":" +
                            (stock.getDateTime().getMinute() < 10 ? "0" + Integer.toString(stock.getDateTime().getMinute()) : Integer.toString(stock.getDateTime().getMinute())));
                    priceArrayDaily.add((float) stock.getOpen());

                    Log.d("test", timeArrayDaily.get(i));
                }
                else{
                    break;
                }
            }

            //weekly info for chart
            IntraDay weeklyChartResponse = stockTimeSeries.intraDay(ticker, Interval.FIVE_MIN, OutputSize.FULL);

            List<StockData> stockDataWeeklyChart = weeklyChartResponse.getStockData();
            currentDay = stockDataWeeklyChart.get(0).getDateTime().getDayOfMonth();
            for (int i=0; i < stockDataWeeklyChart.size(); ++i){
                StockData stock = stockDataWeeklyChart.get(i);
                if (stock.getDateTime().getDayOfMonth() != currentDay){
                    if (stock.getDateTime().getDayOfMonth() + 1 != currentDay){
                        break;
                    }
                    else {
                        currentDay = stock.getDateTime().getDayOfMonth();
                    }
                }
                priceArrayWeekly.add((float) stock.getOpen());
            }

            //monthly info for chart and text views
            Daily monthlyChartResponse = stockTimeSeries.daily(ticker);
            List<StockData> stockDataMonthlyChart = monthlyChartResponse.getStockData();

            data = stockDataMonthlyChart.get(0);
            open = String.format("%.2f", data.getOpen());
            close = String.format("%.2f", data.getClose());
            low = String.format("%.2f", data.getLow());
            high = String.format("%.2f", data.getHigh());
            dividend = Double.toString(data.getDividendAmount());

            for (int i=0; i < stockDataMonthlyChart.size() && i < 30; ++i){
                StockData stock = stockDataMonthlyChart.get(i);
                priceArrayMonthly.add((float) stock.getOpen());
            }

            //52 week info
            Monthly monthlyResponse = stockTimeSeries.monthly(ticker);
            List<StockData> stockDataMonthly = monthlyResponse.getStockData();
            StockData stockMonthly = stockDataMonthly.get(0);
            monthlyhigh = String.format("%.2f", stockMonthly.getHigh());
            monthlylow = String.format("%.2f", stockMonthly.getLow());



        } catch (AlphaVantageException e) {

        }

        Boolean isInPortfolio = false;
        for (int i = 0; i < portfolioData.size(); ++i) {
            if (portfolioData.get(i).getTicker() == ticker) {
                isInPortfolio = true;
                break;
            }
        }

        bundle = new Bundle();
        bundle.putBoolean("isinportfolio", isInPortfolio);
        bundle.putString("securityname", securityname);
        bundle.putString("currentprice", "$" + currentprice);
        bundle.putString("volume", volume);
        bundle.putString("ticker", ticker);
        bundle.putString("open", "$" + open);
        bundle.putString("close", "$" + close);
        bundle.putString("low", "$" + low);
        bundle.putString("high", "$" + high);
        bundle.putString("dividend", dividend + "%");
        bundle.putString("monthlylow", "$" + monthlylow);
        bundle.putString("monthlyhigh", "$" + monthlyhigh);

        bundle.putStringArrayList("intraDailyTimes", timeArrayDaily);


        bundle.putSerializable("intraDailyPrices", priceArrayDaily);
        bundle.putSerializable("weeklyPrices", priceArrayWeekly);
        bundle.putSerializable("monthlyPrices", priceArrayMonthly);

        return bundle;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        searchView.setQuery("", false);
        searchView.clearFocus();
        portfolioSearchView.setQuery("", false);
        portfolioSearchView.clearFocus();

        vibrator.vibrate(25);

        switch (menuItem.getItemId()) {
            case R.id.nav_search:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchFragment()).commit();
                (this).getSupportActionBar().setTitle("Search");
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                portfolioListView.setVisibility(View.GONE);
                portfolioSearchView.setVisibility(View.GONE);
                searchListView.setVisibility(View.VISIBLE);
                searchView.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_portfolio:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PortfolioFragment()).commit();
                (this).getSupportActionBar().setTitle("Portfolio");
                if (!portfolioSearchView.isIconified()) {
                    portfolioSearchView.setIconified(true);
                }
                searchListView.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                portfolioListView.setVisibility(View.VISIBLE);
                portfolioSearchView.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_notifications:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationsFragment()).commit();
                (this).getSupportActionBar().setTitle("Notifications");
                searchListView.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                portfolioListView.setVisibility(View.GONE);
                portfolioSearchView.setVisibility(View.GONE);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onDataPass(String portfolioTicker, String securityName) {
        Boolean isInPortfolio = false;

        //Checking if the stock already exists in the portfolio list
        for (int i = 0; i < portfolioData.size(); ++i) {
            if (portfolioData.get(i).getTicker() == portfolioTicker) {
                isInPortfolio = true;
                portfolioData.remove(i);
                break;
            }
        }

        //Removing from portfolio list
        if (isInPortfolio) {
            padapter = null;
            padapter = new portfolioListAdapter(mContext, portfolioData);
            portfolioListView.setAdapter(padapter);

            vibrator.vibrate(25);
            Toast.makeText(mContext, portfolioTicker + " removed from portfolio", Toast.LENGTH_SHORT).show();
        }

        //Adding to portfolio list
        else {
            portfolioData.add(new stockItem(portfolioTicker, securityName));
            padapter = null;
            padapter = new portfolioListAdapter(mContext, portfolioData);
            portfolioListView.setAdapter(padapter);

            vibrator.vibrate(25);
            Toast.makeText(mContext, portfolioTicker + " added to portfolio", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {

        //General UI cleanup on back pressed
        if (searchView.getQuery() != "") {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        if (portfolioSearchView.getQuery() != "") {
            portfolioSearchView.setQuery("", false);
            portfolioSearchView.clearFocus();
        }
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        }
        if (!portfolioSearchView.isIconified()) {
            portfolioSearchView.setIconified(true);
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (navigationView.getMenu().findItem(R.id.nav_search).isChecked()) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchFragment()).commit();
                (this).getSupportActionBar().setTitle("Search");
                searchListView.setVisibility(View.VISIBLE);
                searchView.setVisibility(View.VISIBLE);
                portfolioListView.setVisibility(View.GONE);
                portfolioSearchView.setVisibility(View.GONE);
            } else if (navigationView.getMenu().findItem(R.id.nav_portfolio).isChecked()) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PortfolioFragment()).commit();
                (this).getSupportActionBar().setTitle("Portfolio");
                portfolioListView.setVisibility(View.VISIBLE);
                portfolioSearchView.setVisibility(View.VISIBLE);
                searchListView.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
            } else if (navigationView.getMenu().findItem(R.id.nav_notifications).isChecked()) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationsFragment()).commit();
                (this).getSupportActionBar().setTitle("Notifications");
                searchListView.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                portfolioListView.setVisibility(View.GONE);
                portfolioSearchView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onStop() {

        super.onStop();
        if (stockDoc.exists()) {
            stockDoc.delete();
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}

