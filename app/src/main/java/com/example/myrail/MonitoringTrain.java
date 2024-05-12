package com.example.myrail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MonitoringTrain extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String URL = "https://monitoring-c4nh.onrender.com/detect_persons";
    private static final int DEFAULT_TOTAL = 20;

    private CoachListAdapter coachListAdapter;
    private ArrayList<CoachItem> coacharray;
    private RecyclerView coach_list;
    private TextView mtr_data, mtr_dsp, waitTextView,selectCoachLabel,mlive_trainno,mlive_trainname,mlive_traindate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_train);

        mtr_data = findViewById(R.id.mtr_data);
        mtr_dsp = findViewById(R.id.mtr_des);
        waitTextView = findViewById(R.id.waitTextView);
        selectCoachLabel = findViewById(R.id.selectCoachLabel);
        mlive_trainno = findViewById(R.id.mlive_trainno);
        mlive_trainname = findViewById(R.id.mlive_trainname);
        mlive_traindate = findViewById(R.id.mlive_traindate);


        Intent i1 = getIntent();
        String train_name = i1.getStringExtra("train_name");
        String train_no = i1.getStringExtra("train_no");

        mlive_trainno.setText(train_no);
        mlive_trainname.setText(train_name);


        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
        String formattedDate = sdf.format(today);
        mlive_traindate.setText(formattedDate);


        String[] coachid = {"GN","GN","GN","A1","A2","A3","B1","B2","B3","C1","C2","C3","GN","GN","GN"};
        int[] imageResourceId = {R.drawable.test_3,R.drawable.t2,R.drawable.t3,R.drawable.t4,R.drawable.t5,R.drawable.test_3,R.drawable.t2,R.drawable.t3,R.drawable.t4,R.drawable.t5,R.drawable.test_3,R.drawable.t2,R.drawable.t3,R.drawable.t4,R.drawable.t5};

        coach_list = findViewById(R.id.coach_list);
        coacharray = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            coacharray.add(new CoachItem(R.drawable.coachview, coachid[i], Integer.toString(i+1)));
        }


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        coach_list.setLayoutManager(layoutManager);

        coachListAdapter = new CoachListAdapter(coacharray);
        coach_list.setAdapter(coachListAdapter);

        coachListAdapter.setOnItemClickListener(new CoachListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                mtr_data.setVisibility(View.GONE);
                mtr_dsp.setVisibility(View.GONE);
                waitTextView.setVisibility(View.VISIBLE);
                selectCoachLabel.setVisibility(View.GONE);
                String cch = "GN";

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageResourceId[position]);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] byteArray = outputStream.toByteArray();

                makeapirequest(byteArray,cch);
            }

        });


    }

    private void makeapirequest(byte[] byteArray,String cch){
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Increase connect timeout
            .readTimeout(30, TimeUnit.SECONDS) // Increase read timeout
            .writeTimeout(30, TimeUnit.SECONDS) // Increase write timeout
            .build();

    // Create request body
    RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "test_3.jpeg", RequestBody.create(MediaType.parse("image/jpeg"), byteArray, 0, byteArray.length))
            .build();

    // Create POST request
    Request request = new Request.Builder()
            .url(URL)
            .post(requestBody)
            .build();

                client.newCall(request).

    enqueue(new Callback() {
        @Override
        public void onFailure (Call call, IOException e){
            e.printStackTrace();
        }

        @Override
        public void onResponse (Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Log.d(TAG, "Response: " + responseBody);
                try {
                    // Parse JSON response
                    JSONObject jsonObject = new JSONObject(responseBody);
                    int count = jsonObject.getInt("person_count");

                    // Calculate percentage
                    double percentage = (double) count / DEFAULT_TOTAL * 100;
                    String crowdDensity = getCrowdDensity(percentage);
                    Log.d("percentage", "onResponse: " + percentage);
                    int per = (int) percentage;
                    Log.d("percentage", "onResponse: " + per);
                    runOnUiThread(() -> {

                        // Display the percentage on the TextView
                        waitTextView.setVisibility(View.GONE);
                        mtr_data.setVisibility(View.VISIBLE);
                        mtr_dsp.setVisibility(View.VISIBLE);
                        mtr_data.setText(crowdDensity);
                        mtr_dsp.setText("Approximately " + percentage + "% of coach is accupied");
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Handle unsuccessful response here
                Log.d(TAG, "Request failed: " + response.code() + " - " + response.message());
            }
        }
    });
}

    private String getCrowdDensity(double percentage) {
        if (percentage <=35) {
            return "Very less crowded";
        } else if (percentage >= 36 && percentage <=55) {
            return "Less crowded";
        } else if (percentage >= 56 && percentage <=70) {
            return "Moderately crowded";
        } else if (percentage >= 71 && percentage <=85) {
            return "Crowded";
        } else {
            return "Very crowded";
        }
    }
}