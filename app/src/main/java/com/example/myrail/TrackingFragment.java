package com.example.myrail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TrackingFragment extends Fragment {

    private TextView source_code,destination_code,textViewIcon;
    private AutoCompleteTextView source,destination,train_name_no;
    private ImageButton train_search, exchange;
    private Button submit;
    private  String st;
    private List<StationItem> StationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Context context = getContext();
            View v = inflater.inflate(R.layout.fragment_tracking,container,false);
            View v1 = inflater.inflate(R.layout.sd_select,container,false);
            String[] station = {"Anand","Vadodara","Ahmedabad","Ajmer","Abu","Nadiad","Ankleshwar","America"};
            source = v.findViewById(R.id.source);
            destination = v.findViewById(R.id.destination);
            train_name_no = v.findViewById(R.id.train_name_no);
            train_search = v.findViewById(R.id.train_Search);
            exchange = v.findViewById(R.id.exchange);
            submit = v.findViewById(R.id.submit);
            source_code = v.findViewById(R.id.source_code);
            destination_code = v.findViewById(R.id.destination_code);

            InputStream inputStream = context.getResources().openRawResource(R.raw.stations);
            fillStationList(inputStream);

            source.setOnItemClickListener((adapterView, view, i, l) -> {
                    textViewIcon = view.findViewById(R.id.sug_icon_text);
                    source_code.setText(textViewIcon.getText());
            });

            destination.setOnItemClickListener((adapterView, view, i, l) -> {
                textViewIcon = view.findViewById(R.id.sug_icon_text);
                    destination_code.setText(textViewIcon.getText());
            });

            exchange.setOnClickListener(view -> {
                String s = source.getText().toString();
                String d = destination.getText().toString();
                String sc,dc;
                sc = source_code.getText().toString();
                dc = destination_code.getText().toString();
                if(!s.isEmpty() || !d.isEmpty()){
                    destination.setText(s);
                    source.setText(d);
                    if(sc!="SRC" || dc!="DST"){
                    source_code.setText(dc);
                    destination_code.setText(sc);
                    }
                }
            });

            submit.setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), TrainList.class);
                String s=source.getText().toString(), d=destination.getText().toString();
                if(!s.isEmpty() && !d.isEmpty()){
                    startActivity(intent);
                }
                else if(s.isEmpty() && d.isEmpty()){
                    Toast.makeText(context,"Enter Source and Destination.", Toast.LENGTH_SHORT).show();
                }
                else if(s.isEmpty()){
                    Toast.makeText(context,"Enter the Source.", Toast.LENGTH_SHORT).show();
                } else if (d.isEmpty()) {
                    Toast.makeText(context,"Enter the Destination.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context,"Enter Valid Station.", Toast.LENGTH_SHORT).show();
                }
            });
            AutoStationItemAdapter adapter = new AutoStationItemAdapter(context, StationList);
            source.setAdapter(adapter);
            destination.setAdapter(adapter);
            return v;
    }

    private void fillStationList(InputStream inputStream){
        StationList = new ArrayList<>();
        ArrayList<String> al = new ArrayList<>();

        try {
            byte[] buffer = new byte[0];
            buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String jsonString = new String(buffer, "UTF-8");

            Log.d("Json", "onCreateView: "+jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);

            // Get the "features" array from the JSONObject
            JSONArray featuresArray = jsonObject.getJSONArray("features");
            for (int i = 0; i < featuresArray.length(); i++) {
                JSONObject feature = featuresArray.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");
                String name = properties.optString("name");
                String code = properties.optString("code");
                StationList.add(new StationItem(name.toUpperCase(), code, R.drawable.station_icon));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}