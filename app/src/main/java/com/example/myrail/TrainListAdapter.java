package com.example.myrail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
public class TrainListAdapter extends ArrayAdapter<TrainListPage>{
    private Context mContext;
    private int mResource;

    public TrainListAdapter(Context context, int resource, ArrayList<TrainListPage> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String tNum = getItem(position).getTrainNumber();
        String tName = getItem(position).getTrainName();
        String tTime = getItem(position).getTrainTime();
        boolean[] days = getItem(position).getDays();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tNumTextView = convertView.findViewById(R.id.trainlist_tnum);
        TextView tNameTextView = convertView.findViewById(R.id.trainlist_tname);
        TextView tTimeTextView = convertView.findViewById(R.id.trainlist_time);
        TextView m,tu,w,th,f,sa,su;
        m = convertView.findViewById(R.id.m);
        tu = convertView.findViewById(R.id.tu);
        w = convertView.findViewById(R.id.w);
        th = convertView.findViewById(R.id.th);
        f = convertView.findViewById(R.id.f);
        sa = convertView.findViewById(R.id.sa);
        su = convertView.findViewById(R.id.su);

        tNumTextView.setText(tNum);
        tNameTextView.setText(tName);
        tTimeTextView.setText(tTime);
        Drawable drawable = ContextCompat.getDrawable(mContext,R.drawable.nodays_bg);

        if(days[0] == false){
            m.setBackground(drawable);
        }
        if(days[1] == false){
            tu.setBackground(drawable);
        }
        if(days[2] == false){
            w.setBackground(drawable);
        }
        if(days[3] == false){
            th.setBackground(drawable);
        }
        if(days[4] == false){
            f.setBackground(drawable);
        }
        if(days[5] == false){
            sa.setBackground(drawable);
        }
        if(days[6] == false){
            su.setBackground(drawable);
        }

        return convertView;
    }
}