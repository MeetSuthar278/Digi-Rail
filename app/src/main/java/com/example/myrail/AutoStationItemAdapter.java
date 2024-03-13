package com.example.myrail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoStationItemAdapter extends ArrayAdapter<StationItem> {
    private List<StationItem> stationListFull;
    public AutoStationItemAdapter(@NonNull Context context, @NonNull List<StationItem> stationList) {
        super(context, 0, stationList);
        stationListFull = new ArrayList<>(stationList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return stationFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.sd_select, parent, false
            );
        }

        TextView textViewName = convertView.findViewById(R.id.sug_text);
        TextView textViewIcon = convertView.findViewById(R.id.sug_icon_text);
        ImageView imageViewFlag = convertView.findViewById(R.id.sug_station_icon);

        StationItem stationItem = getItem(position);

        if (stationItem != null) {
            textViewName.setText(stationItem.getStationName());
            textViewIcon.setText(stationItem.getStationCode());
            imageViewFlag.setImageResource(stationItem.getStationIcon());
        }

        return convertView;
    }

    private Filter stationFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<StationItem> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(stationListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (StationItem item : stationListFull) {
                    if (item.getStationName().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((StationItem) resultValue).getStationName();
        }
    };
}
