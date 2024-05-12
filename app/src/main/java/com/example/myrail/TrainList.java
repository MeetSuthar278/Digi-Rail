package com.example.myrail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TrainList extends AppCompatActivity {

    TextView src_label, dst_label;
    ListView train_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_list);

        src_label = findViewById(R.id.src_label);
        dst_label = findViewById(R.id.dst_label);
        train_list = findViewById(R.id.train_list);
        Intent intent = getIntent();
        String src = intent.getStringExtra("Src");
        String dst = intent.getStringExtra("Dst");
        String sc = intent.getStringExtra("sc");
        String dc = intent.getStringExtra("dc");
        boolean liveapi = intent.getBooleanExtra("liveapi",false);
        Log.d("lapi", "onCreateView: "+liveapi);
        Log.d("intent", "onCreate: " + sc + " and " + dc);

        src_label.setText(src);
        dst_label.setText(dst);

        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(today);

        ArrayList<TrainListPage> trainlistpage = new ArrayList<>();


        if(liveapi == true){
            try {
                makeapirequest(trainlistpage,sc,dc,formattedDate,liveapi);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            src_label.setText("True");
//            try {
//                makejsonrequest(trainlistpage);
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//
//            train_list.setOnItemClickListener((adapterView, view, i, l) -> {
//                Intent intent1 = new Intent(this, LiveTrain.class);
//                intent1.putExtra("train_name", trainlistpage.get(i).getTrainName());
//                intent1.putExtra("train_no", trainlistpage.get(i).getTrainNumber());
//                intent1.putExtra("liveapi",liveapi);
//                startActivity(intent1);
//            });
        } else {
            try {
                makejsonrequest(trainlistpage);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            train_list.setOnItemClickListener((adapterView, view, i, l) -> {
                Intent intent1 = new Intent(this, LiveTrain.class);
                intent1.putExtra("train_name", trainlistpage.get(i).getTrainName());
                intent1.putExtra("train_no", trainlistpage.get(i).getTrainNumber());
                intent1.putExtra("liveapi",liveapi);
                startActivity(intent1);
            });
        }
    }

    private void makeapirequest(ArrayList<TrainListPage> trainlistpage, String sc, String dc,String formattedDate, boolean liveapi) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://irctc1.p.rapidapi.com/api/v3/trainBetweenStations?fromStationCode=" + sc + "&toStationCode=" + dc + "&dateOfJourney="+formattedDate)
                .get()
                .addHeader("X-RapidAPI-Key", "34fb2eabf9msh6af036478913763p13a2e1jsnc15f990dfbf6")
                .addHeader("X-RapidAPI-Host", "irctc1.p.rapidapi.com")
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
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject trainObject = dataArray.getJSONObject(i);
                            String trainNumber = trainObject.getString("train_number");
                            String trainName = trainObject.getString("train_name");
                            String fromStd = trainObject.getString("from_std");
                            String toStd = trainObject.getString("to_std");
                            String timeStr = fromStd + " - " + toStd;
                            boolean m=false,tu=false,w=false,th=false,f=false,sa=false,su=false;

                            JSONArray runDaysArray = trainObject.getJSONArray("run_days");
                            boolean[] runDays = new boolean[7];
                            for (int j = 0; j < runDaysArray.length(); j++) {
                                String day = runDaysArray.get(j).toString();
                                Log.d("day1", "makejsonrequest: "+day);
                                if(day.equals("Mon") && m==false){
                                    m=true;
                                    continue;
                                } else if (day.equals("Tue") && tu!=true) {
                                    tu=true;
                                    continue;
                                }else if (day.equals("Wed") && w!=true) {
                                    w=true;
                                    continue;
                                }else if (day.equals("Thu") && th!=true) {
                                    th=true;
                                    continue;
                                }else if (day.equals("Fri") && f!=true) {
                                    f=true;
                                    continue;
                                }else if (day.equals("Sat") && sa!=true) {
                                    sa=true;
                                    continue;
                                }else if (day.equals("Sun") && su!=true) {
                                    su=true;
                                    continue;
                                }
                            }
                            runDays[0] = m;
                            runDays[1] = tu;
                            runDays[2] = w;
                            runDays[3] = th;
                            runDays[4] = f;
                            runDays[5] = sa;
                            runDays[6] = su;
                            trainlistpage.add(new TrainListPage(trainNumber, trainName, timeStr,runDays));
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TrainListAdapter adapter = new TrainListAdapter(TrainList.this, R.layout.train_list_adapter, trainlistpage);
                                Log.d("trainlist", "run: " + trainlistpage);
                                train_list.setAdapter(adapter);

                                train_list.setOnItemClickListener((adapterView, view, i, l) -> {
                                    Intent intent1 = new Intent(TrainList.this,LiveTrain.class);
                                    intent1.putExtra("train_name",trainlistpage.get(i).getTrainName());
                                    intent1.putExtra("train_no",trainlistpage.get(i).getTrainNumber());
                                    intent1.putExtra("liveapi",liveapi);
                                    startActivity(intent1);
                                });
                            }
                        });
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e("Api", "Error: " + response.code() + " " + response.message());
                }
            }
        });
    }


    private void makejsonrequest(ArrayList<TrainListPage> trainlistpage) throws JSONException {

        String s = "{\"status\":true,\"message\":\"Success\",\"timestamp\":1710593908187,\"data\":[{\"train_number\":\"12655\",\"train_name\":\"NavjeevanSFExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"MAS\",\"from_std\":\"22:20\",\"from_sta\":\"22:18\",\"local_train_from_sta\":1338,\"to_sta\":\"22:52\",\"to_std\":\"22:57\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:32\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":25,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"1A\",\"3E\"]},{\"train_number\":\"12902\",\"train_name\":\"GujaratMail\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"DDR\",\"from_std\":\"23:48\",\"from_sta\":\"23:46\",\"local_train_from_sta\":1426,\"to_sta\":\"00:20\",\"to_std\":\"00:25\",\"from_day\":0,\"to_day\":1,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:32\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":25,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"SL\",\"3A\",\"2A\",\"1A\",\"3E\"]},{\"train_number\":\"11465\",\"train_name\":\"Somnath-JabalpurExpress\",\"run_days\":[\"Mon\",\"Sat\"],\"train_src\":\"SMNH\",\"train_dstn\":\"JBP\",\"from_std\":\"19:52\",\"from_sta\":\"19:50\",\"local_train_from_sta\":1190,\"to_sta\":\"20:26\",\"to_std\":\"20:31\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"CYI\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":32,\"to_station_name\":\"CHHAYAPURI\",\"duration\":\"0:34\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":23,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"8KmsfromBRC\",\"duration_rating\":1,\"score_duration\":8,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"1A\",\"3E\"]},{\"train_number\":\"12844\",\"train_name\":\"Ahmedabad-PuriSFExpress\",\"run_days\":[\"Mon\",\"Thu\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"PURI\",\"from_std\":\"20:04\",\"from_sta\":\"20:02\",\"local_train_from_sta\":1202,\"to_sta\":\"20:38\",\"to_std\":\"20:43\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:34\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":23,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":8,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"20960\",\"train_name\":\"IntercitySFExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"VDG\",\"train_dstn\":\"BL\",\"from_std\":\"20:40\",\"from_sta\":\"20:38\",\"local_train_from_sta\":1238,\"to_sta\":\"21:15\",\"to_std\":\"21:20\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":35,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:35\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":23,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":8,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"CC\"]},{\"train_number\":\"19218\",\"train_name\":\"SaurashtraJantaExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"VRL\",\"train_dstn\":\"BDTS\",\"from_std\":\"21:32\",\"from_sta\":\"21:30\",\"local_train_from_sta\":1290,\"to_sta\":\"22:08\",\"to_std\":\"22:13\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:36\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":21,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"1A\"]},{\"train_number\":\"22946\",\"train_name\":\"SaurashtraMail\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"OKHA\",\"train_dstn\":\"MMCT\",\"from_std\":\"21:39\",\"from_sta\":\"21:37\",\"local_train_from_sta\":1297,\"to_sta\":\"22:15\",\"to_std\":\"22:20\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:36\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":21,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"1A\",\"3E\"]},{\"train_number\":\"22928\",\"train_name\":\"LokshaktiSFExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"BDTS\",\"from_std\":\"21:52\",\"from_sta\":\"21:50\",\"local_train_from_sta\":1310,\"to_sta\":\"22:28\",\"to_std\":\"22:33\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:36\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":21,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"SL\",\"3A\",\"2A\",\"1A\",\"3E\"]},{\"train_number\":\"14701\",\"train_name\":\"AmrapurAravaliExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"SGNR\",\"train_dstn\":\"BDTS\",\"from_std\":\"23:20\",\"from_sta\":\"23:18\",\"local_train_from_sta\":2838,\"to_sta\":\"00:01\",\"to_std\":\"00:06\",\"from_day\":1,\"to_day\":2,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:41\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":21,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"14707\",\"train_name\":\"RANAKPUREXP\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"BKN\",\"train_dstn\":\"DDR\",\"from_std\":\"23:59\",\"from_sta\":\"23:57\",\"local_train_from_sta\":1437,\"to_sta\":\"00:40\",\"to_std\":\"00:45\",\"from_day\":0,\"to_day\":1,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":35,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:41\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":21,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"19034\",\"train_name\":\"GujaratQueen\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"BL\",\"from_std\":\"19:26\",\"from_sta\":\"19:24\",\"local_train_from_sta\":1164,\"to_sta\":\"20:08\",\"to_std\":\"20:13\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":1,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:42\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":21,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"CC\"]},{\"train_number\":\"11087\",\"train_name\":\"Veraval-PuneExpress\",\"run_days\":[\"Sat\"],\"train_src\":\"VRL\",\"train_dstn\":\"PUNE\",\"from_std\":\"21:15\",\"from_sta\":\"21:13\",\"local_train_from_sta\":1273,\"to_sta\":\"21:57\",\"to_std\":\"22:02\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:42\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":21,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"19309\",\"train_name\":\"SHANTIEXPRESS\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"GNC\",\"train_dstn\":\"INDB\",\"from_std\":\"20:31\",\"from_sta\":\"20:29\",\"local_train_from_sta\":1229,\"to_sta\":\"21:20\",\"to_std\":\"21:25\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"CYI\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":32,\"to_station_name\":\"CHHAYAPURI\",\"duration\":\"0:49\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":19,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"8KmsfromBRC\",\"duration_rating\":1,\"score_duration\":4,\"score_std\":10,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"SL\",\"3A\",\"2A\",\"1A\"]},{\"train_number\":\"12932\",\"train_name\":\"AHMEDABAD-MUMBAICENTRALACDoubleDeckerExp\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\"],\"train_src\":\"ADI\",\"train_dstn\":\"MMCT\",\"from_std\":\"06:44\",\"from_sta\":\"06:42\",\"local_train_from_sta\":402,\"to_sta\":\"07:13\",\"to_std\":\"07:18\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:29\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":15,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"CC\",\"EV\"]},{\"train_number\":\"20947\",\"train_name\":\"JanShatabdiExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"EKNR\",\"from_std\":\"08:46\",\"from_sta\":\"08:44\",\"local_train_from_sta\":524,\"to_sta\":\"09:15\",\"to_std\":\"09:18\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:29\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":15,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"CC\",\"EC\",\"EV\"]},{\"train_number\":\"12010\",\"train_name\":\"ShatabdiExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\"],\"train_src\":\"ADI\",\"train_dstn\":\"MMCT\",\"from_std\":\"16:00\",\"from_sta\":\"15:58\",\"local_train_from_sta\":958,\"to_sta\":\"16:30\",\"to_std\":\"16:35\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:30\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":15,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"CC\",\"EC\",\"EV\",\"EA\"]},{\"train_number\":\"20954\",\"train_name\":\"Ahmedabad-MGRChennaiCentralSFExpress\",\"run_days\":[\"Sat\"],\"train_src\":\"ADI\",\"train_dstn\":\"MAS\",\"from_std\":\"10:37\",\"from_sta\":\"10:35\",\"local_train_from_sta\":635,\"to_sta\":\"11:08\",\"to_std\":\"11:13\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:31\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":15,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"22924\",\"train_name\":\"HumsafarExpress\",\"run_days\":[\"Wed\",\"Sat\",\"Mon\"],\"train_src\":\"JAM\",\"train_dstn\":\"BDTS\",\"from_std\":\"02:56\",\"from_sta\":\"02:54\",\"local_train_from_sta\":1614,\"to_sta\":\"03:28\",\"to_std\":\"03:33\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:32\",\"special_train\":false,\"train_type\":\"HMSR\",\"score\":15,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\"]},{\"train_number\":\"12934\",\"train_name\":\"KarnavatiSFExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"MMCT\",\"from_std\":\"05:56\",\"from_sta\":\"05:54\",\"local_train_from_sta\":354,\"to_sta\":\"06:28\",\"to_std\":\"06:33\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:32\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":15,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"CC\",\"EV\"]},{\"train_number\":\"22954\",\"train_name\":\"GujaratSFExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"MMCT\",\"from_std\":\"08:07\",\"from_sta\":\"08:05\",\"local_train_from_sta\":485,\"to_sta\":\"08:39\",\"to_std\":\"08:44\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:32\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":15,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"CC\"]},{\"train_number\":\"20908\",\"train_name\":\"SayajiNagariSFExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"BHUJ\",\"train_dstn\":\"DDR\",\"from_std\":\"06:51\",\"from_sta\":\"06:49\",\"local_train_from_sta\":1849,\"to_sta\":\"07:25\",\"to_std\":\"07:30\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:34\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":15,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":10,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"SL\",\"3A\",\"2A\",\"1A\"]},{\"train_number\":\"16337\",\"train_name\":\"Okha-ErnakulamExpress\",\"run_days\":[\"Mon\",\"Sat\"],\"train_src\":\"OKHA\",\"train_dstn\":\"ERS\",\"from_std\":\"16:43\",\"from_sta\":\"16:41\",\"local_train_from_sta\":1001,\"to_sta\":\"17:17\",\"to_std\":\"17:22\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:34\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":13,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":8,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"12990\",\"train_name\":\"Ajmer-DadarWesternSFExpress\",\"run_days\":[\"Thu\",\"Sat\",\"Mon\"],\"train_src\":\"AII\",\"train_dstn\":\"DDR\",\"from_std\":\"06:05\",\"from_sta\":\"06:03\",\"local_train_from_sta\":1803,\"to_sta\":\"06:40\",\"to_std\":\"06:45\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:35\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":13,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":8,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"3E\"]},{\"train_number\":\"22956\",\"train_name\":\"KutchSFExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"BHUJ\",\"train_dstn\":\"BDTS\",\"from_std\":\"04:05\",\"from_sta\":\"04:03\",\"local_train_from_sta\":1683,\"to_sta\":\"04:41\",\"to_std\":\"04:46\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:36\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":13,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":8,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"1A\"]},{\"train_number\":\"12993\",\"train_name\":\"Gandhidham-PuriSFExpress\",\"run_days\":[\"Sat\"],\"train_src\":\"GIMB\",\"train_dstn\":\"PURI\",\"from_std\":\"05:22\",\"from_sta\":\"05:20\",\"local_train_from_sta\":1760,\"to_sta\":\"05:58\",\"to_std\":\"06:03\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:36\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":13,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":8,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"19204\",\"train_name\":\"VRLBDTSEXP\",\"run_days\":[\"Sat\"],\"train_src\":\"VRL\",\"train_dstn\":\"BDTS\",\"from_std\":\"08:57\",\"from_sta\":\"08:55\",\"local_train_from_sta\":1975,\"to_sta\":\"09:33\",\"to_std\":\"09:38\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:36\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":13,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":8,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"1A\"]},{\"train_number\":\"12473\",\"train_name\":\"SarvodayaSFExpress\",\"run_days\":[\"Sat\"],\"train_src\":\"GIMB\",\"train_dstn\":\"SVDK\",\"from_std\":\"15:44\",\"from_sta\":\"15:42\",\"local_train_from_sta\":942,\"to_sta\":\"16:20\",\"to_std\":\"16:25\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"CYI\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":32,\"to_station_name\":\"CHHAYAPURI\",\"duration\":\"0:36\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":11,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"8KmsfromBRC\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"1A\"]},{\"train_number\":\"12833\",\"train_name\":\"Ahmedabad-HowrahSFExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"HWH\",\"from_std\":\"01:25\",\"from_sta\":\"01:23\",\"local_train_from_sta\":83,\"to_sta\":\"02:04\",\"to_std\":\"02:11\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:39\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":11,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"19165\",\"train_name\":\"SabarmatiExpress\",\"run_days\":[\"Thu\",\"Sat\",\"Mon\"],\"train_src\":\"ADI\",\"train_dstn\":\"DBG\",\"from_std\":\"00:12\",\"from_sta\":\"00:10\",\"local_train_from_sta\":1450,\"to_sta\":\"00:52\",\"to_std\":\"00:57\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"CYI\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":32,\"to_station_name\":\"CHHAYAPURI\",\"duration\":\"0:40\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":11,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"8KmsfromBRC\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"12966\",\"train_name\":\"Bhuj-MumbaiBandraTSFExpress\",\"run_days\":[\"Sat\"],\"train_src\":\"BHUJ\",\"train_dstn\":\"BDTS\",\"from_std\":\"04:35\",\"from_sta\":\"04:33\",\"local_train_from_sta\":1713,\"to_sta\":\"05:15\",\"to_std\":\"05:20\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:40\",\"special_train\":false,\"train_type\":\"SUF\",\"score\":11,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"19489\",\"train_name\":\"Ahmedabad-GorakhpurExpress\",\"run_days\":[\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"GKP\",\"from_std\":\"10:03\",\"from_sta\":\"10:01\",\"local_train_from_sta\":601,\"to_sta\":\"10:43\",\"to_std\":\"10:48\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"CYI\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":32,\"to_station_name\":\"CHHAYAPURI\",\"duration\":\"0:40\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":11,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"8KmsfromBRC\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"16507\",\"train_name\":\"Jodhpur-KSRBengaluruExpress\",\"run_days\":[\"Thu\",\"Sat\"],\"train_src\":\"JU\",\"train_dstn\":\"SBC\",\"from_std\":\"15:24\",\"from_sta\":\"15:22\",\"local_train_from_sta\":922,\"to_sta\":\"16:05\",\"to_std\":\"16:15\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":35,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:41\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":11,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":6,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"1A\"]},{\"train_number\":\"19016\",\"train_name\":\"SaurashtraExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"PBR\",\"train_dstn\":\"DDR\",\"from_std\":\"09:08\",\"from_sta\":\"09:06\",\"local_train_from_sta\":1986,\"to_sta\":\"09:56\",\"to_std\":\"10:01\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":1,\"distance\":35,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:48\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":9,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":4,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\"]},{\"train_number\":\"22960\",\"train_name\":\"InterCityExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"JAM\",\"train_dstn\":\"BRC\",\"from_std\":\"11:42\",\"from_sta\":\"11:40\",\"local_train_from_sta\":700,\"to_sta\":\"12:30\",\"to_std\":\"12:30\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:48\",\"special_train\":false,\"train_type\":\"INT\",\"score\":9,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":1,\"score_duration\":4,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"2S\",\"CC\"]},{\"train_number\":\"19483\",\"train_name\":\"Ahmedabad-BarauniExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"BJU\",\"from_std\":\"01:32\",\"from_sta\":\"01:30\",\"local_train_from_sta\":90,\"to_sta\":\"02:25\",\"to_std\":\"02:30\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":0,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:53\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":9,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":2,\"score_duration\":4,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\",\"3A\",\"2A\",\"3E\"]},{\"train_number\":\"59550\",\"train_name\":\"AHMEDABAD-VADODARASankalpFastPassenger\",\"run_days\":[\"Sun\",\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\"],\"train_src\":\"ADI\",\"train_dstn\":\"BRC\",\"from_std\":\"16:35\",\"from_sta\":\"16:33\",\"local_train_from_sta\":993,\"to_sta\":\"17:30\",\"to_std\":\"17:30\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":1,\"distance\":35.5,\"to_station_name\":\"VADODARAJN\",\"duration\":\"0:55\",\"special_train\":false,\"train_type\":\"PASS\",\"score\":9,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":2,\"score_duration\":4,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\"]},{\"train_number\":\"19418\",\"train_name\":\"Ahmedabad-BorivaliExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"BVI\",\"from_std\":\"01:18\",\"from_sta\":\"01:13\",\"local_train_from_sta\":1513,\"to_sta\":\"02:45\",\"to_std\":\"02:50\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":2,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"1:27\",\"special_train\":false,\"train_type\":\"MEX\",\"score\":9,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":3,\"score_duration\":4,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\"]},{\"train_number\":\"19036\",\"train_name\":\"InterCityExpress\",\"run_days\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"train_src\":\"ADI\",\"train_dstn\":\"BRC\",\"from_std\":\"15:16\",\"from_sta\":\"15:14\",\"local_train_from_sta\":914,\"to_sta\":\"16:50\",\"to_std\":\"16:50\",\"from_day\":0,\"to_day\":0,\"d_day\":0,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":3,\"distance\":36,\"to_station_name\":\"VADODARAJN\",\"duration\":\"1:34\",\"special_train\":false,\"train_type\":\"MAILEXPRESS\",\"score\":9,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":4,\"score_duration\":4,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"CC\"]},{\"train_number\":\"59440\",\"train_name\":\"AHMEDABAD-MUMBAICENTRALPassenger\",\"run_days\":[\"Sun\",\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\"],\"train_src\":\"ADI\",\"train_dstn\":\"MMCT\",\"from_std\":\"00:59\",\"from_sta\":\"00:57\",\"local_train_from_sta\":1497,\"to_sta\":\"02:38\",\"to_std\":\"02:43\",\"from_day\":1,\"to_day\":1,\"d_day\":1,\"from\":\"ANND\",\"to\":\"BRC\",\"from_station_name\":\"ANANDJN\",\"halt_stn\":5,\"distance\":35.5,\"to_station_name\":\"VADODARAJN\",\"duration\":\"1:39\",\"special_train\":false,\"train_type\":\"PASS\",\"score\":9,\"score_train_type\":5,\"score_booking_requency\":0,\"frequency_perc\":0,\"from_distance_text\":\"\",\"to_distance_text\":\"\",\"duration_rating\":4,\"score_duration\":4,\"score_std\":0,\"score_user_preferred\":0,\"train_date\":\"16-03-2024\",\"class_type\":[\"SL\"]}]}";

        JSONObject jsonObject = new JSONObject(s);
        JSONArray dataArray = jsonObject.getJSONArray("data");
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject trainObject = dataArray.getJSONObject(i);
            String trainNumber = trainObject.getString("train_number");
            String trainName = trainObject.getString("train_name");
            String fromStd = trainObject.getString("from_std");
            String toStd = trainObject.getString("to_std");
            String timeStr = fromStd + " - " + toStd;
            boolean m=false,tu=false,w=false,th=false,f=false,sa=false,su=false;

                                    JSONArray runDaysArray = trainObject.getJSONArray("run_days");
                                    boolean[] runDays = new boolean[7];
                                    for (int j = 0; j < runDaysArray.length(); j++) {
                                        String day = runDaysArray.get(j).toString();
                                        Log.d("day1", "makejsonrequest: "+day);
                                        if(day.equals("Mon") && m==false){
                                            m=true;
                                            continue;
                                        } else if (day.equals("Tue") && tu!=true) {
                                            tu=true;
                                            continue;
                                        }else if (day.equals("Wed") && w!=true) {
                                            w=true;
                                            continue;
                                        }else if (day.equals("Thu") && th!=true) {
                                            th=true;
                                            continue;
                                        }else if (day.equals("Fri") && f!=true) {
                                            f=true;
                                            continue;
                                        }else if (day.equals("Sat") && sa!=true) {
                                            sa=true;
                                            continue;
                                        }else if (day.equals("Sun") && su!=true) {
                                            su=true;
                                            continue;
                                        }
                                    }
                                    runDays[0] = m;
                                    runDays[1] = tu;
                                    runDays[2] = w;
                                    runDays[3] = th;
                                    runDays[4] = f;
                                    runDays[5] = sa;
                                    runDays[6] = su;
                                    trainlistpage.add(new TrainListPage(trainNumber, trainName, timeStr,runDays));


           Log.d("runday", "makejsonrequest: "+m+" , "+tu+" , "+w+" , "+th+" , "+f+" , "+sa+" , "+su+"\n\n");

        }
        TrainListAdapter adapter = new TrainListAdapter(TrainList.this, R.layout.train_list_adapter, trainlistpage);
        train_list.setAdapter(adapter);
    }
}