package com.example.myrail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.mbms.StreamingServiceInfo;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class LiveTrain extends AppCompatActivity {

    private Button prev_station,next_station;
    private TextView np_label,live_trainno,live_trainname,live_traindate,srcstation,srctime,dststation,dsttime,currentmessage,npsourcecode,npstationname,arrival_time,dept_time;
    private FrameLayout np_station;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_train);

        prev_station = findViewById(R.id.prev_station);
        next_station = findViewById(R.id.next_station);
        np_label = findViewById(R.id.nplabel);
        np_station = findViewById(R.id.next_previous_station);
        live_trainno = findViewById(R.id.live_trainno);
        live_trainname = findViewById(R.id.live_trainname);
        live_traindate = findViewById(R.id.live_traindate);
        srcstation = findViewById(R.id.srcstation);
        srctime = findViewById(R.id.srctime);
        dststation = findViewById(R.id.dststation);
        dsttime = findViewById(R.id.dsttime);
        currentmessage = findViewById(R.id.currentmessage);
        npsourcecode = findViewById(R.id.npsourcecode);
        npstationname = findViewById(R.id.npstationname);
        arrival_time = findViewById(R.id.arrival_time);
        dept_time = findViewById(R.id.dept_time);

        Intent intent = getIntent();
        String train_name = intent.getStringExtra("train_name");
        boolean liveapi = intent.getBooleanExtra("liveapi",false);
        Log.d("lapi", "onCreateView: "+liveapi);
//        String train_no = intent.getStringExtra("train_no");
        String train_no = "20960";

        Log.d("Live", "onCreate: "+train_no+" , "+train_name);

        live_trainno.setText(train_no);
        live_trainname.setText(train_name);

        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
        String formattedDate = sdf.format(today);
        live_traindate.setText(formattedDate);


        if(liveapi == true) {
            //makeApiRequest(train_no);
            currentmessage.setText("True");
        } else {
            try {
                makejsonrequest(train_no);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void makeApiRequest(String train_no){

    OkHttpClient client = new OkHttpClient();

    Request request = new Request.Builder()
            .url("https://irctc1.p.rapidapi.com/api/v1/liveTrainStatus?trainNo="+train_no+"&startDay=0")
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
                        JSONObject dataObject = jsonObject.getJSONObject("data");

                        // Setting readable_message from current_location_info



                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                try {

                                    JSONArray srcStation = dataObject.getJSONArray("previous_stations");
                                    if(srcStation.length() >= 0){
                                        JSONObject srcObject = srcStation.getJSONObject(0);
                                        String src = srcObject.getString("station_name");
                                        String srct = srcObject.getString("std");
                                        srcstation.setText(src);
                                        srctime.setText(srct);
                                    }

                                    JSONArray dstStation = dataObject.getJSONArray("upcoming_stations");
                                    if(dstStation.length() >= 0){
                                        JSONObject dstObject = dstStation.getJSONObject(dstStation.length() - 1);
                                        String dst = dstObject.getString("station_name");
                                        String dstt = dstObject.getString("std");
                                        dststation.setText(dst);
                                        dsttime.setText(dstt);
                                    }

                                    JSONArray currentLocationInfoArray = dataObject.getJSONArray("current_location_info");
                                    if (currentLocationInfoArray.length() > 0) {
                                        JSONObject currentLocation = currentLocationInfoArray.getJSONObject(0);
                                        String readableMessage = currentLocation.getString("readable_message");
                                        currentmessage.setText(readableMessage);
                                    }

                                    JSONArray previousStationsArray = dataObject.getJSONArray("previous_stations");
                                    JSONObject lastPreviousStation = previousStationsArray.getJSONObject(previousStationsArray.length() - 1);
                                    String lastPreviousStationName = lastPreviousStation.getString("station_name");
                                    String lastPreviousStationCode = lastPreviousStation.getString("station_code");
                                    String lastPreviousStationArrival = lastPreviousStation.getString("sta");
                                    String lastPreviousStationDept = lastPreviousStation.getString("std");
                                    prev_station.setOnClickListener(view -> {
                                        np_station.setVisibility(View.VISIBLE);
                                            if (previousStationsArray.length() > 0) {
                                                np_label.setText("PREVIOUS STATION");
                                                npstationname.setText(lastPreviousStationName);
                                                npsourcecode.setText(lastPreviousStationCode);
                                                arrival_time.setText(lastPreviousStationArrival);
                                                dept_time.setText(lastPreviousStationDept);
                                            }
                                    });

                                    JSONArray upcomingStationsArray = dataObject.getJSONArray("upcoming_stations");
                                    JSONObject firstUpcomingStation = upcomingStationsArray.getJSONObject(0);
                                    String firstUpcomingStationName = firstUpcomingStation.getString("station_name");
                                    String firstUpcomingStationCode = firstUpcomingStation.getString("station_code");
                                    String firstUpcomingStationArrival = firstUpcomingStation.getString("sta");
                                    String firstUpcomingStationDept = firstUpcomingStation.getString("std");
                                    next_station.setOnClickListener(view -> {
                                        np_station.setVisibility(View.VISIBLE);

                                            if (upcomingStationsArray.length() > 0) {
                                                np_label.setText("NEXT STATION");
                                                npstationname.setText(firstUpcomingStationName);
                                                npsourcecode.setText(firstUpcomingStationCode);
                                                arrival_time.setText(firstUpcomingStationArrival);
                                                dept_time.setText(firstUpcomingStationDept);
                                            }
                                    });
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
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
    void makejsonrequest(String train_no) throws JSONException {

        String responseBody = "{\"status\":true,\"message\":\"Success\",\"timestamp\":1710512468040,\"data\":{\"new_alert_msg\":\"\",\"total_distance\":422,\"at_dstn\":false,\"train_name\":\"IntercitySFExpress\",\"ir_train_name\":\"IntercitySFExpress\",\"spent_time\":0.27,\"status_as_of_min\":10,\"smart_city_id\":null,\"delay\":14,\"stoppage_number\":0,\"status_as_of\":\"Asof10minsago\",\"current_station_name\":\"SABARMATIACABIN~\",\"cur_stn_sta\":\"19:19\",\"etd\":\"19:33\",\"instance_alert\":0,\"seo_train_name\":\"IntercitySFExpress\",\"current_station_code\":\"SBTA\",\"current_location_info\":[{\"type\":1,\"readable_message\":\"Crossedsabarmatiacabinat19:33\",\"message\":\"CrossedSABARMATIACABIN~at19:33\",\"label\":\"Asof10minsago\",\"img_url\":\"\",\"hint\":\"Delay14m\",\"deeplink\":\"\"},{\"type\":4,\"readable_message\":\"4kilometersaheadofsabarmatiacabin\",\"message\":\"4kmsaheadofSABARMATIACABIN~\",\"label\":\"Asof10minsago\",\"img_url\":\"\",\"hint\":\"TravellingSouth\",\"deeplink\":\"\"}],\"at_src\":false,\"platform_number\":0,\"train_start_date\":\"2024-03-15\",\"source\":\"VDG\",\"update_time\":\"2024-03-15T19:41:00Z\",\"ahead_distance_text\":\"4kmsahead\",\"journey_time\":455,\"user_id\":0,\"destination\":\"BL\",\"current_state_code\":\"GJ\",\"notification_date\":\"2024-03-15\",\"cur_stn_std\":\"19:19\",\"previous_stations\":[{\"stoppage_number\":1,\"std\":\"17:00\",\"station_name\":\"VADNAGAR\",\"station_lng\":72.633276,\"station_lat\":23.784481,\"station_code\":\"VDG\",\"state_code\":\"GJ\",\"sta\":\"17:00\",\"smart_city_id\":null,\"si_no\":1,\"platform_number\":1,\"non_stops\":[],\"halt\":0,\"etd\":\"17:00\",\"eta\":\"17:00\",\"distance_from_source\":0,\"arrival_delay\":0,\"a_day\":0},{\"stoppage_number\":2,\"std\":\"17:12\",\"station_name\":\"VISNAGAR\",\"station_lng\":72.542038,\"station_lat\":23.70238,\"station_code\":\"VNG\",\"state_code\":\"GJ\",\"sta\":\"17:10\",\"smart_city_id\":null,\"si_no\":2,\"platform_number\":1,\"non_stops\":[],\"halt\":2,\"etd\":\"17:14\",\"eta\":\"17:12\",\"distance_from_source\":14,\"arrival_delay\":2,\"a_day\":0},{\"stoppage_number\":3,\"std\":\"17:32\",\"station_name\":\"MAHESANAJN\",\"station_lng\":72.389088,\"station_lat\":23.603003,\"station_code\":\"MSH\",\"state_code\":\"GJ\",\"sta\":\"17:30\",\"smart_city_id\":null,\"si_no\":3,\"platform_number\":1,\"non_stops\":[{\"std\":\"\",\"station_name\":\"KALOLJN\",\"station_code\":\"KLL\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":4,\"distance_from_source\":76}],\"halt\":15,\"etd\":\"17:51\",\"eta\":\"17:36\",\"distance_from_source\":34,\"arrival_delay\":6,\"a_day\":0},{\"stoppage_number\":4,\"std\":\"18:17\",\"station_name\":\"GANDHINAGARCAPITAL\",\"station_lng\":72.629242,\"station_lat\":23.233459,\"station_code\":\"GNC\",\"state_code\":\"GJ\",\"sta\":\"18:15\",\"smart_city_id\":null,\"si_no\":5,\"platform_number\":1,\"non_stops\":[{\"std\":\"\",\"station_name\":\"KHODIYAR\",\"station_code\":\"KHDB\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":6,\"distance_from_source\":109},{\"std\":\"\",\"station_name\":\"CHANDKHERAROAD\",\"station_code\":\"CDK\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":7,\"distance_from_source\":115},{\"std\":\"\",\"station_name\":\"SABARMATIDCABIN\",\"station_code\":\"SBID\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":8,\"distance_from_source\":116},{\"std\":\"\",\"station_name\":\"KALIROAD\",\"station_code\":\"KLRD\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":9,\"distance_from_source\":117},{\"std\":\"\",\"station_name\":\"SABARMATIJN\",\"station_code\":\"SBI\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":10,\"distance_from_source\":119},{\"std\":\"\",\"station_name\":\"SABARMATIBG\",\"station_code\":\"SBIB\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":11,\"distance_from_source\":119}],\"halt\":2,\"etd\":\"18:53\",\"eta\":\"18:51\",\"distance_from_source\":96,\"arrival_delay\":36,\"a_day\":0}],\"late_update\":false,\"ahead_distance\":4,\"is_ry_eta\":true,\"eta\":\"19:33\",\"run_days\":\"MON,TUE,WED,THU,FRI,SAT,SUN\",\"distance_from_source\":125,\"data_from\":\"ntes\",\"train_number\":\"20960\",\"upcoming_stations\":[{\"stoppage_number\":5,\"std\":\"19:35\",\"station_name\":\"AHMEDABADJN\",\"station_lng\":72.601476,\"station_lat\":23.025475,\"station_code\":\"ADI\",\"state_code\":\"GJ\",\"sta\":\"19:30\",\"smart_city_id\":null,\"si_no\":13,\"platform_number\":4,\"on_time_rating\":7,\"non_stops\":[{\"std\":\"\",\"station_name\":\"KANKARIA\",\"station_code\":\"KKF\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":14,\"distance_from_source\":127},{\"std\":\"\",\"station_name\":\"KANKARIASOUTH\",\"station_code\":\"KKEC\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":15,\"distance_from_source\":127},{\"std\":\"\",\"station_name\":\"MANINAGAR\",\"station_code\":\"MAN\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":16,\"distance_from_source\":128},{\"std\":\"\",\"station_name\":\"VATVA\",\"station_code\":\"VTA\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":17,\"distance_from_source\":133},{\"std\":\"\",\"station_name\":\"VATVAYARD\",\"station_code\":\"VY\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":18,\"distance_from_source\":134},{\"std\":\"\",\"station_name\":\"GERATPUR\",\"station_code\":\"GER\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":19,\"distance_from_source\":138},{\"std\":\"\",\"station_name\":\"BAREJADI\",\"station_code\":\"BJD\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":20,\"distance_from_source\":142},{\"std\":\"\",\"station_name\":\"KANIL\",\"station_code\":\"KANJ\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":21,\"distance_from_source\":146},{\"std\":\"\",\"station_name\":\"NENPUR\",\"station_code\":\"NEP\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":22,\"distance_from_source\":149},{\"std\":\"\",\"station_name\":\"MAHEMDAVADKHEDARD\",\"station_code\":\"MHD\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":23,\"distance_from_source\":153},{\"std\":\"\",\"station_name\":\"GOTHAJ\",\"station_code\":\"GTE\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":24,\"distance_from_source\":162}],\"halt\":15,\"food_available\":true,\"etd\":\"19:57\",\"eta_a_min\":1182,\"eta\":\"19:42\",\"distance_from_source\":125,\"distance_from_current_station_txt\":\"Arriving...\",\"distance_from_current_station\":0,\"day\":0,\"arrival_delay\":12,\"a_day\":0},{\"stoppage_number\":6,\"std\":\"20:23\",\"station_name\":\"NADIADJN\",\"station_lng\":72.856178,\"station_lat\":22.694566,\"station_code\":\"ND\",\"state_code\":\"GJ\",\"sta\":\"20:21\",\"smart_city_id\":null,\"si_no\":25,\"platform_number\":1,\"on_time_rating\":6,\"non_stops\":[{\"std\":\"\",\"station_name\":\"UTARSANDA\",\"station_code\":\"UTD\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":26,\"distance_from_source\":176},{\"std\":\"\",\"station_name\":\"KANJARIBORIYAV\",\"station_code\":\"KBRV\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":27,\"distance_from_source\":181},{\"std\":\"\",\"station_name\":\"XX-ANNC\",\"station_code\":\"XX-ANNC\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":28,\"distance_from_source\":188}],\"halt\":4,\"food_available\":true,\"etd\":\"20:40\",\"eta_a_min\":1236,\"eta\":\"20:36\",\"distance_from_source\":170,\"distance_from_current_station_txt\":\"Nextstop45kmstogo\",\"distance_from_current_station\":45,\"day\":0,\"arrival_delay\":15,\"a_day\":0},{\"stoppage_number\":7,\"std\":\"20:40\",\"station_name\":\"ANANDJN\",\"station_lng\":72.966213,\"station_lat\":22.561787,\"station_code\":\"ANND\",\"state_code\":\"GJ\",\"sta\":\"20:38\",\"smart_city_id\":null,\"si_no\":29,\"platform_number\":4,\"on_time_rating\":5,\"non_stops\":[],\"halt\":5,\"food_available\":false,\"etd\":\"20:57\",\"eta_a_min\":1252,\"eta\":\"20:52\",\"distance_from_source\":189,\"distance_from_current_station_txt\":\"Nextstop64kmstogo\",\"distance_from_current_station\":64,\"day\":0,\"arrival_delay\":14,\"a_day\":0},{\"stoppage_number\":8,\"std\":\"21:20\",\"station_name\":\"VADODARAJN\",\"station_lng\":73.181005,\"station_lat\":22.310696,\"station_code\":\"BRC\",\"state_code\":\"GJ\",\"sta\":\"21:15\",\"smart_city_id\":null,\"si_no\":30,\"platform_number\":1,\"on_time_rating\":6,\"non_stops\":[{\"std\":\"\",\"station_name\":\"VISHVAMITRI\",\"station_code\":\"VS\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":31,\"distance_from_source\":227},{\"std\":\"\",\"station_name\":\"MAKARPURA\",\"station_code\":\"MPR\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":32,\"distance_from_source\":233},{\"std\":\"\",\"station_name\":\"VARNAMA\",\"station_code\":\"VRM\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":33,\"distance_from_source\":238},{\"std\":\"\",\"station_name\":\"ITOLA\",\"station_code\":\"ITA\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":34,\"distance_from_source\":242},{\"std\":\"\",\"station_name\":\"KASHIPURASARAR\",\"station_code\":\"KSPR\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":35,\"distance_from_source\":246},{\"std\":\"\",\"station_name\":\"MIYAGAMKARJAN\",\"station_code\":\"MYG\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":36,\"distance_from_source\":254},{\"std\":\"\",\"station_name\":\"LAKODARA\",\"station_code\":\"LKD\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":37,\"distance_from_source\":262},{\"std\":\"\",\"station_name\":\"PALEJ\",\"station_code\":\"PLJ\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":38,\"distance_from_source\":269},{\"std\":\"\",\"station_name\":\"VAREDIYA\",\"station_code\":\"VRE\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":39,\"distance_from_source\":275},{\"std\":\"\",\"station_name\":\"NABIPUR\",\"station_code\":\"NIU\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":40,\"distance_from_source\":282},{\"std\":\"\",\"station_name\":\"CHAVAJ\",\"station_code\":\"CVJ\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":41,\"distance_from_source\":289},{\"std\":\"\",\"station_name\":\"XX-BHBC\",\"station_code\":\"XX-BHBC\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":42,\"distance_from_source\":293}],\"halt\":6,\"food_available\":true,\"etd\":\"21:28\",\"eta_a_min\":1282,\"eta\":\"21:22\",\"distance_from_source\":224,\"distance_from_current_station_txt\":\"Nextstop99kmstogo\",\"distance_from_current_station\":99,\"day\":0,\"arrival_delay\":7,\"a_day\":0},{\"stoppage_number\":9,\"std\":\"22:13\",\"station_name\":\"BHARUCHJN\",\"station_lng\":72.998099,\"station_lat\":21.707317,\"station_code\":\"BH\",\"state_code\":\"GJ\",\"sta\":\"22:11\",\"smart_city_id\":null,\"si_no\":43,\"platform_number\":4,\"on_time_rating\":4,\"non_stops\":[{\"std\":\"\",\"station_name\":\"CONCORCONTAINERTERMINAL\",\"station_code\":\"CCTA\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":44,\"distance_from_source\":304},{\"std\":\"\",\"station_name\":\"ANKLESHWARJN\",\"station_code\":\"AKV\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":45,\"distance_from_source\":304},{\"std\":\"\",\"station_name\":\"SANJALI\",\"station_code\":\"SNJL\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":46,\"distance_from_source\":310},{\"std\":\"\",\"station_name\":\"PANOLI\",\"station_code\":\"PAO\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":47,\"distance_from_source\":314},{\"std\":\"\",\"station_name\":\"HATHURAN\",\"station_code\":\"HAT\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":48,\"distance_from_source\":318},{\"std\":\"\",\"station_name\":\"KOSAMBAJN\",\"station_code\":\"KSB\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":49,\"distance_from_source\":323},{\"std\":\"\",\"station_name\":\"KIM\",\"station_code\":\"KIM\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":50,\"distance_from_source\":331},{\"std\":\"\",\"station_name\":\"KUDSAD\",\"station_code\":\"KDSD\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":51,\"distance_from_source\":334},{\"std\":\"\",\"station_name\":\"SAYAN\",\"station_code\":\"SYN\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":52,\"distance_from_source\":341},{\"std\":\"\",\"station_name\":\"GOTHANGAM\",\"station_code\":\"GTX\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":53,\"distance_from_source\":344},{\"std\":\"\",\"station_name\":\"KOSAD\",\"station_code\":\"KSE\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":54,\"distance_from_source\":348},{\"std\":\"\",\"station_name\":\"UTRAN\",\"station_code\":\"URN\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":55,\"distance_from_source\":351}],\"halt\":2,\"food_available\":false,\"etd\":\"22:13\",\"eta_a_min\":1331,\"eta\":\"22:11\",\"distance_from_source\":295,\"distance_from_current_station_txt\":\"Nextstop170kmstogo\",\"distance_from_current_station\":170,\"day\":0,\"arrival_delay\":0,\"a_day\":0},{\"stoppage_number\":10,\"std\":\"23:17\",\"station_name\":\"SURAT\",\"station_lng\":72.840643,\"station_lat\":21.206418,\"station_code\":\"ST\",\"state_code\":\"GJ\",\"sta\":\"23:14\",\"smart_city_id\":null,\"si_no\":56,\"platform_number\":2,\"on_time_rating\":8,\"non_stops\":[{\"std\":\"\",\"station_name\":\"UDHNAJN\",\"station_code\":\"UDN\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":57,\"distance_from_source\":357},{\"std\":\"\",\"station_name\":\"XX-UDNB\",\"station_code\":\"XX-UDNB\",\"state_code\":null,\"sta\":\"\",\"si_no\":58,\"distance_from_source\":360},{\"std\":\"\",\"station_name\":\"BHESTAN\",\"station_code\":\"BHET\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":59,\"distance_from_source\":362},{\"std\":\"\",\"station_name\":\"SACHIN\",\"station_code\":\"SCH\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":60,\"distance_from_source\":368},{\"std\":\"\",\"station_name\":\"MAROLI\",\"station_code\":\"MRL\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":61,\"distance_from_source\":374}],\"halt\":3,\"food_available\":true,\"etd\":\"23:17\",\"eta_a_min\":1394,\"eta\":\"23:14\",\"distance_from_source\":353,\"distance_from_current_station_txt\":\"Nextstop228kmstogo\",\"distance_from_current_station\":228,\"day\":0,\"arrival_delay\":0,\"a_day\":0},{\"stoppage_number\":11,\"std\":\"23:44\",\"station_name\":\"NAVSARI\",\"station_lng\":72.913814,\"station_lat\":20.946371,\"station_code\":\"NVS\",\"state_code\":\"GJ\",\"sta\":\"23:42\",\"smart_city_id\":null,\"si_no\":62,\"platform_number\":2,\"on_time_rating\":6,\"non_stops\":[{\"std\":\"\",\"station_name\":\"GANDHISMRITI\",\"station_code\":\"GNST\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":63,\"distance_from_source\":386},{\"std\":\"\",\"station_name\":\"HANSAPORE\",\"station_code\":\"HXR\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":64,\"distance_from_source\":388},{\"std\":\"\",\"station_name\":\"VEDCHHA\",\"station_code\":\"VDH\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":65,\"distance_from_source\":392},{\"std\":\"\",\"station_name\":\"ANCHELI\",\"station_code\":\"ACL\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":66,\"distance_from_source\":395},{\"std\":\"\",\"station_name\":\"AMALSAD\",\"station_code\":\"AML\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":67,\"distance_from_source\":398},{\"std\":\"\",\"station_name\":\"BILIMORAJN\",\"station_code\":\"BIM\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":68,\"distance_from_source\":404},{\"std\":\"\",\"station_name\":\"JORAVASAN\",\"station_code\":\"JRS\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":69,\"distance_from_source\":408},{\"std\":\"\",\"station_name\":\"DUNGRI\",\"station_code\":\"DGI\",\"state_code\":\"GJ\",\"sta\":\"\",\"si_no\":70,\"distance_from_source\":413}],\"halt\":2,\"food_available\":true,\"etd\":\"23:49\",\"eta_a_min\":1427,\"eta\":\"23:47\",\"distance_from_source\":383,\"distance_from_current_station_txt\":\"Nextstop258kmstogo\",\"distance_from_current_station\":258,\"day\":0,\"arrival_delay\":5,\"a_day\":0},{\"stoppage_number\":12,\"std\":\"00:35\",\"station_name\":\"VALSAD\",\"station_lng\":72.9335991,\"station_lat\":20.6086295,\"station_code\":\"BL\",\"state_code\":\"GJ\",\"sta\":\"00:35\",\"smart_city_id\":null,\"si_no\":71,\"platform_number\":1,\"on_time_rating\":10,\"non_stops\":[],\"halt\":0,\"food_available\":true,\"etd\":\"00:35\",\"eta_a_min\":1475,\"eta\":\"00:35\",\"distance_from_source\":422,\"distance_from_current_station_txt\":\"Nextstop297kmstogo\",\"distance_from_current_station\":297,\"day\":1,\"arrival_delay\":0,\"a_day\":1}],\"avg_speed\":0,\"related_alert\":0,\"is_run_day\":true,\"status\":\"T\",\"si_no\":12,\"new_alert_id\":0,\"success\":true,\"std\":\"2024-03-1517:00\",\"at_src_dstn\":false,\"a_day\":0}}";
        JSONObject jsonObject = new JSONObject(responseBody);
        try{
        JSONObject dataObject = jsonObject.getJSONObject("data");

        JSONArray srcStation = dataObject.getJSONArray("previous_stations");
        if(srcStation.length() >= 0){
            JSONObject srcObject = srcStation.getJSONObject(0);
            String src = srcObject.getString("station_name");
            String srct = srcObject.getString("std");
            srcstation.setText(src);
            srctime.setText(srct);
        }

        JSONArray dstStation = dataObject.getJSONArray("upcoming_stations");
        if(dstStation.length() >= 0){
            JSONObject dstObject = dstStation.getJSONObject(dstStation.length() - 1);
            String dst = dstObject.getString("station_name");
            String dstt = dstObject.getString("std");
            dststation.setText(dst);
            dsttime.setText(dstt);
        }

        JSONArray currentLocationInfoArray = dataObject.getJSONArray("current_location_info");
        if (currentLocationInfoArray.length() > 0) {
            JSONObject currentLocation = currentLocationInfoArray.getJSONObject(0);
            String readableMessage = currentLocation.getString("readable_message");
            currentmessage.setText(readableMessage);
        }

        prev_station.setOnClickListener(view -> {
            np_station.setVisibility(View.VISIBLE);
            try {
                JSONArray previousStationsArray = dataObject.getJSONArray("previous_stations");
                if (previousStationsArray.length() > 0) {
                    JSONObject lastPreviousStation = previousStationsArray.getJSONObject(previousStationsArray.length() - 1);
                    String lastPreviousStationName = lastPreviousStation.getString("station_name");
                    String lastPreviousStationCode = lastPreviousStation.getString("station_code");
                    String lastPreviousStationArrival = lastPreviousStation.getString("sta");
                    String lastPreviousStationDept = lastPreviousStation.getString("std");
                    np_label.setText("PREVIOUS STATION");
                    npstationname.setText(lastPreviousStationName);
                    npsourcecode.setText(lastPreviousStationCode);
                    arrival_time.setText(lastPreviousStationArrival);
                    dept_time.setText(lastPreviousStationDept);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        next_station.setOnClickListener(view -> {
            np_station.setVisibility(View.VISIBLE);
            try {
                JSONArray upcomingStationsArray = dataObject.getJSONArray("upcoming_stations");
                if (upcomingStationsArray.length() > 0) {
                    JSONObject firstUpcomingStation = upcomingStationsArray.getJSONObject(0);
                    String firstUpcomingStationName = firstUpcomingStation.getString("station_name");
                    String firstUpcomingStationCode = firstUpcomingStation.getString("station_code");
                    String firstUpcomingStationArrival = firstUpcomingStation.getString("sta");
                    String firstUpcomingStationDept = firstUpcomingStation.getString("std");

                    np_label.setText("NEXT STATION");
                    npstationname.setText(firstUpcomingStationName);
                    npsourcecode.setText(firstUpcomingStationCode);
                    arrival_time.setText(firstUpcomingStationArrival);
                    dept_time.setText(firstUpcomingStationDept);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    } catch (JSONException e) {
        throw new RuntimeException(e);
    }
    }
}
