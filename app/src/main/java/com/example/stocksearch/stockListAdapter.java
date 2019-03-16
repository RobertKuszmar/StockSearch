package com.example.stocksearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class stockListAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    int mResource;
    List<stockItem> stockList;
    ArrayList<stockItem> arrayList;

    public stockListAdapter(Context context, List<stockItem> stockList){
        mContext = context;
        this.stockList = stockList;
        inflater = LayoutInflater.from(mContext);
        this.arrayList = new ArrayList<stockItem>();
        this.arrayList.addAll(stockList);
        this.stockList.clear();
    }

    public class ViewHolder{
        TextView tvTicker;
        TextView tvSecurityName;
    }

    @Override
    public int getCount() {
        return stockList.size();
    }

    @Override
    public Object getItem(int position) {
        return stockList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.stock_list, null);

            holder.tvTicker = convertView.findViewById(R.id.tickerText);
            holder.tvSecurityName = convertView.findViewById(R.id.securityNameText);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        holder.tvTicker.setText(stockList.get(position).getTicker());
        holder.tvSecurityName.setText(stockList.get(position).getSecurityName());

        return convertView;
    }

    public void filter(String charText){
        charText = charText.toLowerCase(Locale.getDefault()).trim();
        stockList.clear();
        if (charText.length() == 0){
            //stockList.addAll(arrayList);
            stockList.clear();
        }
        else{
            for (stockItem item : arrayList){
                if (item.getTicker().toLowerCase(Locale.getDefault()).trim().startsWith(charText) || item.getSecurityName().toLowerCase(Locale.getDefault()).trim().startsWith(charText)){
                    stockList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
}
