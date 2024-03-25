package com.example.myrail;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TrackingFragment extends Fragment {

    private TextView source_code,destination_code,textViewIcon,label;
    private AutoCompleteTextView source,destination,train_name_no;
    private ImageButton train_search, exchange;
    private Button submit;
    private  String st;
    private ArrayList<StationItem> StationList;
    private ListView train_name_dialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        InputStream inputStream = context.getResources().openRawResource(R.raw.stations);
        fillStationList(inputStream);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Context context = getContext();
            View v = inflater.inflate(R.layout.fragment_tracking,container,false);
            String[] station = {"Anand","Vadodara","Ahmedabad","Ajmer","Abu","Nadiad","Ankleshwar","America"};
            source = v.findViewById(R.id.source);
            destination = v.findViewById(R.id.destination);
            train_name_no = v.findViewById(R.id.train_name_no);
            train_search = v.findViewById(R.id.train_Search);
            exchange = v.findViewById(R.id.exchange);
            submit = v.findViewById(R.id.submit);
            source_code = v.findViewById(R.id.source_code);
            destination_code = v.findViewById(R.id.destination_code);
            label = v.findViewById(R.id.label);
            boolean liveapi = false;


        Bundle args = getArguments();
        if (args != null) {
            liveapi = args.getBoolean("liveapi", false);
            Log.d("lapi", "onCreateView: "+liveapi);
        } else {
            liveapi = false;
        }


        boolean finalLiveapi = liveapi;
        train_search.setOnClickListener(view -> {
                String searchText = train_name_no.getText().toString();
                if(searchText.isEmpty()){
                    Toast.makeText(context,"Enter Train Name or Number.",Toast.LENGTH_SHORT).show();
                }
                else {
                    makeApiRequest(searchText, finalLiveapi);
                }
            });






            source.setOnItemClickListener((adapterView, view, i, l) -> {
                    textViewIcon = view.findViewById(R.id.sug_icon_text);
                    source_code.setText(textViewIcon.getText());

            });

            source.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if(charSequence.length() == 0){
                            source_code.setText("SRC");
                        }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            destination.setOnItemClickListener((adapterView, view, i, l) -> {
                textViewIcon = view.findViewById(R.id.sug_icon_text);
                    destination_code.setText(textViewIcon.getText());
                if(destination.getText().equals("")){
                    destination_code.setText("DST");
                }
            });

        destination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    destination_code.setText("DST");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

            exchange.setOnClickListener(view -> {
                Animation rotate = AnimationUtils.loadAnimation(context,R.anim.xchnge_rotate);
                String s = source.getText().toString();
                String d = destination.getText().toString();
                String sc,dc;
                sc = source_code.getText().toString();
                dc = destination_code.getText().toString();
                if(!s.isEmpty() || !d.isEmpty()){
                    exchange.startAnimation(rotate);
                    destination.setText(s);
                    source.setText(d);
                    if(sc!="SRC" || dc!="DST"){
                        Log.d("xchange", "onCreateView:  SRC ="+sc+", DST : "+dc);
                        if(sc.equals("SRC")){
                          source_code.setText(dc);
                          destination_code.setText("DST");
                        } else if(dc.equals("DST")) {
                            source_code.setText("SRC");
                            destination_code.setText(sc);
                        } else {
                            source_code.setText(dc);
                            destination_code.setText(sc);
                        }
                    }
                }
            });

            submit.setOnClickListener(view -> {
                String s=source.getText().toString(), d=destination.getText().toString();
                if(!s.isEmpty() && !d.isEmpty() && !s.equals(d)){
                    Intent intent = new Intent(getActivity(), TrainList.class);
                    intent.putExtra("Src",s);
                    intent.putExtra("Dst",d);
                    intent.putExtra("sc",source_code.getText().toString());
                    intent.putExtra("dc", destination_code.getText().toString());
                    intent.putExtra("liveapi",finalLiveapi);
                    Log.d("intent", "onCreateView: "+s+" and "+d);
                    startActivity(intent);
                }
                else if(s.isEmpty() && d.isEmpty()){
                    Toast.makeText(context,"Enter Source and Destination.", Toast.LENGTH_SHORT).show();
                }
                else if(s.isEmpty()){
                    Toast.makeText(context,"Enter the Source.", Toast.LENGTH_SHORT).show();
                } else if (d.isEmpty()) {
                    Toast.makeText(context,"Enter the Destination.", Toast.LENGTH_SHORT).show();
                }else if (s.equals(d)) {
                    Toast.makeText(context,"Arrival & Destination cannot be same.", Toast.LENGTH_SHORT).show();
                } else {
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


    private void makeApiRequest(String searchText,boolean liveapi) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\"search\": \"" + searchText + "\"}", mediaType);
        Request request = new Request.Builder()
                .url("https://trains.p.rapidapi.com/")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", "34fb2eabf9msh6af036478913763p13a2e1jsnc15f990dfbf6")
                .addHeader("X-RapidAPI-Host", "trains.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Api", "IOException: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseBody);
                        // Assuming jsonArray contains suggestions for train names
                        final List<String> trainNames = new ArrayList<>();
                        final List<String> trainNo = new ArrayList<>();
                        final List<TrainDialogList> trains = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject trainObject = jsonArray.getJSONObject(i);
                            String name = trainObject.getString("name");
                            String tnum = trainObject.getString("train_num");
                            trains.add(new TrainDialogList(name,tnum));
                        }
                        // Update UI with the suggestions
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if(trains.isEmpty()){
                                    Toast.makeText(getContext(),"Please enter valid Train Details",Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    final Dialog dialog = new Dialog(getContext());
                                    dialog.setContentView(R.layout.train_dialog);
                                    dialog.setTitle("Available Trains");
                                    TrainDialogAdapter adapter = new TrainDialogAdapter(getContext(), trains);
                                    train_name_dialog = dialog.findViewById(R.id.train_name_dialog);
                                    train_name_dialog.setAdapter(adapter);
                                    dialog.show();
                                    train_name_dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            Intent intent = new Intent(getActivity(), LiveTrain.class);
                                            intent.putExtra("train_name",trains.get(i).getTName());
                                            intent.putExtra("train_no",trains.get(i).getTNumber());
                                            intent.putExtra("liveapi",liveapi);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("Api", "JSONException: " + e.getMessage());
                    }
                } else {
                    Log.e("Api", "Error: " + response.code() + " " + response.message());
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("tf", "onDestroyView: destroyed view--------------");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("tf", "onDetach: Detached------------------");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("tf", "onDestroy: destroyed-----------------");
    }

}