package com.example.myrail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    TrackingFragment tf = new TrackingFragment();
    ReservationFragment rf = new ReservationFragment();
    MonitorFragment mf = new MonitorFragment();
    BottomNavigationView bn;
    TextView dt;
    private Button db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bn = findViewById(R.id.bottom_nav);
        if(savedInstanceState == null){
        getSupportFragmentManager().beginTransaction().replace(R.id.container,tf).commit();
        }

        bn.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                    if(item.getItemId() == R.id.Tracking){
                       ReplaceF(tf);
                        return true;
                    }
                    else if(item.getItemId() == R.id.Reserve) {
                        ReplaceF(rf);
                        return true;
                    }
                    else if(item.getItemId() == R.id.Monitor) {
                        ReplaceF(mf);
                        return true;
                    }
            return false;
            }
        });
    }

    public void ReplaceF(Fragment f){
        getSupportFragmentManager().beginTransaction().replace(R.id.container,f).addToBackStack(null).commit();
    }
}