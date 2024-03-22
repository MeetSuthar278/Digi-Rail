package com.example.myrail;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MonitoringTrain extends AppCompatActivity {

    private CoachListAdapter coachListAdapter;
    private ArrayList<CoachItem> coacharray;
    private RecyclerView coach_list;
    private TextView mtr_data,mtr_dsp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_train);

        mtr_data = findViewById(R.id.mtr_data);
        mtr_dsp = findViewById(R.id.mtr_des);

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
                int ppl = position*5 + 100;
                String cch = "GN";
                String strper = Integer.toString(per);
                mtr_data.setText(strper+"%");
                mtr_dsp.setText("Approximately "+ppl+" people are present in "+cch+" coach");
            }

        });

    }
}