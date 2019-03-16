package com.example.stocksearch;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.w3c.dom.Text;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class StockSelectFragment extends Fragment {
    View view;
    Boolean stockIsInPortfolio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stockselect, container, false);
        stockIsInPortfolio = getArguments().getBoolean("isinportfolio");
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        ArrayList<Float>tempPriceArrayList;
        ArrayList<String> tempTimeArrayList;
        ArrayList<Entry> yAXES;
        XAxis xAxis;


        FloatingActionButton addFab = (FloatingActionButton)view.findViewById(R.id.fab_add);
        ToggleButton dailyButton = (ToggleButton)view.findViewById(R.id.daily_button);
        ToggleButton weeklyButton = (ToggleButton)view.findViewById(R.id.weekly_button);
        ToggleButton monthlyButton = (ToggleButton)view.findViewById(R.id.monthly_button);
        TextView tickerTextView = (TextView)view.findViewById(R.id.stockSelectTicker);
        TextView currentPriceTextView = (TextView)view.findViewById(R.id.stockSelectCurrentPrice);
        TextView openTextView = (TextView)view.findViewById(R.id.stockSelectOpen);
        TextView closeTextView = (TextView)view.findViewById(R.id.stockSelectClose);
        TextView lowTextView = (TextView)view.findViewById(R.id.stockSelectLow);
        TextView highTextView = (TextView)view.findViewById(R.id.stockSelectHigh);
        TextView volumeTextView = (TextView)view.findViewById(R.id.stockSelectVolume);
        TextView dividendTextView = (TextView)view.findViewById(R.id.stockSelectDividend);
        TextView monthlyLowTextView = (TextView)view.findViewById(R.id.stockSelectMonthlyLow);
        TextView monthlyHighTextView = (TextView)view.findViewById(R.id.stockSelectMonthlyHigh);

        tickerTextView.setText(getArguments().getString("ticker"));
        currentPriceTextView.setText(getArguments().getString("currentprice"));
        openTextView.setText(getArguments().getString("open"));
        closeTextView.setText(getArguments().getString("close"));
        lowTextView.setText(getArguments().getString("low"));
        highTextView.setText(getArguments().getString("high"));
        volumeTextView.setText(getArguments().getString("volume"));
        dividendTextView.setText(getArguments().getString("dividend"));
        monthlyLowTextView.setText(getArguments().getString("monthlylow"));
        monthlyHighTextView.setText(getArguments().getString("monthlyhigh"));

        //Creating daily chart
        LineChart dailyChart = (LineChart) view.findViewById(R.id.dailyStockChart);
        dailyChart.setExtraLeftOffset(30);
        dailyChart.setExtraRightOffset(30);
        dailyChart.setExtraBottomOffset(90);
        dailyChart.setScaleYEnabled(false);
        dailyChart.setHighlightPerDragEnabled(false);
        dailyChart.setHighlightPerTapEnabled(false);
        dailyChart.getDescription().setEnabled(false);
        dailyChart.getLegend().setEnabled(false);
        dailyChart.setNoDataText("Unavailable");
        //dailyChart.getXAxis().setDrawLabels(false);
        dailyChart.getAxisRight().setDrawLabels(false);

        tempPriceArrayList = (ArrayList)getArguments().getSerializable("intraDailyPrices");
        Collections.reverse(tempPriceArrayList);
        tempTimeArrayList = getArguments().getStringArrayList("intraDailyTimes");
        xAxis = dailyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);

        yAXES = new ArrayList<>();

        for (int i=0; i < tempPriceArrayList.size() && i < tempPriceArrayList.size(); ++i){

            float price = tempPriceArrayList.get(i);

            yAXES.add(new Entry(i, price));
        }

        ArrayList<ILineDataSet> lineDataSetDaily = new ArrayList<>();

        LineDataSet dailySet = new LineDataSet(yAXES, "daily");
        dailySet.setDrawCircles(false);
        dailySet.setColor(0xFF008577);
        dailySet.setLineWidth(2);
        dailySet.setDrawValues(false);
        lineDataSetDaily.add(dailySet);
        dailyChart.setData(new LineData(lineDataSetDaily));
        dailyButton.setChecked(true);


        //Creating weekly chart
        LineChart weeklyChart = (LineChart) view.findViewById(R.id.weeklyStockChart);
        weeklyChart.setExtraLeftOffset(30);
        weeklyChart.setExtraRightOffset(30);
        weeklyChart.setExtraBottomOffset(90);
        weeklyChart.setScaleYEnabled(false);
        weeklyChart.setHighlightPerDragEnabled(false);
        weeklyChart.setHighlightPerTapEnabled(false);
        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.getLegend().setEnabled(false);
        weeklyChart.setNoDataText("Unavailable");
        weeklyChart.getXAxis().setDrawLabels(false);
        weeklyChart.getAxisRight().setDrawLabels(false);

        tempPriceArrayList = (ArrayList)getArguments().getSerializable("weeklyPrices"); //change key
        Collections.reverse(tempPriceArrayList);
        tempTimeArrayList = getArguments().getStringArrayList("intraDailyTimes");

        yAXES = new ArrayList<>();

        for (int i=0; i < tempPriceArrayList.size() && i < tempPriceArrayList.size(); ++i){

            float price = tempPriceArrayList.get(i);

            yAXES.add(new Entry(i, price));
        }

        ArrayList<ILineDataSet> lineDataSetWeekly = new ArrayList<>();

        LineDataSet weeklySet = new LineDataSet(yAXES, "weekly");
        weeklySet.setDrawCircles(false);
        weeklySet.setColor(0xFF008577);
        weeklySet.setLineWidth(2);
        weeklySet.setDrawValues(false);
        lineDataSetWeekly.add(weeklySet);
        weeklyChart.setData(new LineData(lineDataSetWeekly));

        weeklyChart.setVisibility(View.GONE);


        //Creating monthly chart
        LineChart monthlyChart = (LineChart) view.findViewById(R.id.monthlyStockChart);
        monthlyChart.setExtraLeftOffset(30);
        monthlyChart.setExtraRightOffset(30);
        monthlyChart.setExtraBottomOffset(90);
        monthlyChart.setScaleYEnabled(false);
        monthlyChart.setHighlightPerDragEnabled(false);
        monthlyChart.setHighlightPerTapEnabled(false);
        monthlyChart.getDescription().setEnabled(false);
        monthlyChart.getLegend().setEnabled(false);
        monthlyChart.setNoDataText("Unavailable");
        monthlyChart.getXAxis().setDrawLabels(false);
        monthlyChart.getAxisRight().setDrawLabels(false);

        tempPriceArrayList = (ArrayList)getArguments().getSerializable("monthlyPrices"); //change key
        Collections.reverse(tempPriceArrayList);
        tempTimeArrayList = getArguments().getStringArrayList("intraDailyTimes");

        yAXES = new ArrayList<>();

        for (int i=0; i < tempPriceArrayList.size() && i < tempPriceArrayList.size(); ++i){

            float price = tempPriceArrayList.get(i);

            yAXES.add(new Entry(i, price));
        }

        ArrayList<ILineDataSet> lineDataSetMonthly = new ArrayList<>();

        LineDataSet monthlySet = new LineDataSet(yAXES, "yearly");
        monthlySet.setDrawCircles(false);
        monthlySet.setColor(0xFF008577);
        monthlySet.setLineWidth(2);
        monthlySet.setDrawValues(false);
        lineDataSetMonthly.add(monthlySet);
        monthlyChart.setData(new LineData(lineDataSetMonthly));

        monthlyChart.setVisibility(View.GONE);


        //Setting "+" or "-" picture on button for action to be taken
        if (stockIsInPortfolio){
            addFab.setImageResource(R.drawable.ic_remove);
        }
        else{
            addFab.setImageResource(R.drawable.ic_add);
        }

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                passData(getArguments().getString("ticker"), getArguments().getString("securityname"));

                if (stockIsInPortfolio){
                    addFab.setImageResource(R.drawable.ic_add);
                    stockIsInPortfolio = false;
                }
                else{
                    addFab.setImageResource(R.drawable.ic_remove);
                    stockIsInPortfolio = true;
                }
            }
        });

        dailyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                vibrator.vibrate(25);
                dailyChart.setVisibility(View.VISIBLE);
                weeklyChart.setVisibility(View.GONE);
                monthlyChart.setVisibility(View.GONE);
                dailyButton.setChecked(true);
                weeklyButton.setChecked(false);
                monthlyButton.setChecked(false);
            }
        });

        weeklyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                vibrator.vibrate(25);
                dailyChart.setVisibility(View.GONE);
                weeklyChart.setVisibility(View.VISIBLE);
                monthlyChart.setVisibility(View.GONE);
                dailyButton.setChecked(false);
                weeklyButton.setChecked(true);
                monthlyButton.setChecked(false);
            }
        });

        monthlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                vibrator.vibrate(25);
                dailyChart.setVisibility(View.GONE);
                weeklyChart.setVisibility(View.GONE);
                monthlyChart.setVisibility(View.VISIBLE);
                dailyButton.setChecked(false);
                weeklyButton.setChecked(false);
                monthlyButton.setChecked(true);
            }
        });

        return view;
    }

    public interface OnDataPass {
        void onDataPass(String portfolioTicker, String securityname);
    }

    OnDataPass dataPasser;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public void passData(String portfolioTicker, String securityname){
        dataPasser.onDataPass(portfolioTicker, securityname);
    }

}
