package com.example.myrail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import java.util.ArrayList;

public class MonitoringTrain extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String URL = "https://monitoring-c4nh.onrender.com/detect_persons";
    private static final int DEFAULT_TOTAL = 20;

    private CoachListAdapter coachListAdapter;
    private ArrayList<CoachItem> coacharray;
    private RecyclerView coach_list;
    private TextView mtr_data,mtr_dsp,waitTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_train);

        mtr_data = findViewById(R.id.mtr_data);
        mtr_dsp = findViewById(R.id.mtr_des);
        waitTextView = findViewById(R.id.waitTextView);

        // Show "Please wait..." message
        waitTextView.setVisibility(View.VISIBLE);

        coach_list = findViewById(R.id.coach_list);
        coacharray = new ArrayList<>();
        for (int i=0; i<15; i++){
            coacharray.add(new CoachItem(R.drawable.coachview, "GN",Integer.toString(i)));
        }


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        coach_list.setLayoutManager(layoutManager);

        coachListAdapter = new CoachListAdapter(coacharray);
        coach_list.setAdapter(coachListAdapter);

        coachListAdapter.setOnItemClickListener(new CoachListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                CoachItem clickedItem = coacharray.get(position);
                int per = position+80;
                //int ppl = position*5 + 100;
                String cch = "GN";
                String strper = Integer.toString(per);

               // mtr_dsp.setText("Approximately "+ppl+" people are present in "+cch+" coach");

                int imageResourceId = R.drawable.test_3;

                // Convert drawable to byte array
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageResourceId);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] byteArray = outputStream.toByteArray();

                // Create OkHttpClient instance
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS) // Increase connect timeout
                        .readTimeout(30, TimeUnit.SECONDS) // Increase read timeout
                        .writeTimeout(30, TimeUnit.SECONDS) // Increase write timeout
                        .build();

                // Create request body
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "test_3.jpeg", RequestBody.create(MediaType.parse("image/jpeg"), byteArray,0, byteArray.length))
                        .build();

                // Create POST request
                Request request = new Request.Builder()
                        .url(URL)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            Log.d(TAG, "Response: " + responseBody);
                            try {
                                // Parse JSON response
                                JSONObject jsonObject = new JSONObject(responseBody);
                                int count = jsonObject.getInt("person_count");

                                // Calculate percentage
                                double percentage = (double) count / DEFAULT_TOTAL * 100;

                                runOnUiThread(() -> {
                                    // Display the percentage on the TextView
                                    waitTextView.setVisibility(View.GONE);
                                    mtr_data.setText(percentage + "%");
                                    mtr_dsp.setText("Approximately "+count+" people are present in "+cch+" coach");
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

        });

    }
}