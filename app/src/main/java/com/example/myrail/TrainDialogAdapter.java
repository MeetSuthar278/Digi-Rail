package com.example.myrail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class TrainDialogAdapter extends ArrayAdapter<TrainDialogList> {
    public TrainDialogAdapter(@NonNull Context context, @NonNull List<TrainDialogList> train) {
        super(context, 0, train);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TrainDialogList train = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_list_item, parent, false);
        }

        TextView dialog_tname = convertView.findViewById(R.id.dialog_tname);
        TextView dialog_tnum = convertView.findViewById(R.id.dialog_tnum);

        // Set data to the views
        if (train != null) {
            dialog_tname.setText(train.getTName());
            dialog_tnum.setText(train.getTNumber());
        }

        return convertView;
    }
}
