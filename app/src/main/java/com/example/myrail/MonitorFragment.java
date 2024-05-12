package com.example.myrail;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MonitorFragment extends Fragment {

    private ImageButton mon_searchbtn;
    private AutoCompleteTextView mon_train;
    private ListView train_name_dialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context context = getContext();
        View v = inflater.inflate(R.layout.fragment_monitor, container, false);
        mon_searchbtn = v.findViewById(R.id.mon_searchbtn);
        mon_train = v.findViewById(R.id.mon_train);
        mon_searchbtn.setOnClickListener(view -> {
            String searchText = mon_train.getText().toString();
            if(searchText.isEmpty()){
                Toast.makeText(context,"Enter Train Name or Number.",Toast.LENGTH_SHORT).show();
            }
            else {
                makeApiRequest(searchText);
            }
        });
        return v;
    }

    private void makeApiRequest(String searchText) {
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
                                            Intent intent = new Intent(getActivity(), MonitoringTrain.class);
                                            intent.putExtra("train_name",trains.get(i).getTName());
                                            intent.putExtra("train_no",trains.get(i).getTNumber());
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
}