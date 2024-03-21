package com.example.myrail;

import android.os.Bundle;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_train);

        coach_list = findViewById(R.id.coach_list);
        coacharray = new ArrayList<>();
        for (int i=0; i<15; i++){
            coacharray.add(new CoachItem(R.drawable.coach, "GN "+i));
        }
        // Add more items as needed

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        coach_list.setLayoutManager(layoutManager);

        coachListAdapter = new CoachListAdapter(coacharray);
        coach_list.setAdapter(coachListAdapter);

    }
}