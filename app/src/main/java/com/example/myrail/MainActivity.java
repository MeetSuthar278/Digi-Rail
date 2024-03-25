package com.example.myrail;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity {

    TrackingFragment tf = new TrackingFragment();
    ReservationFragment rf = new ReservationFragment();
    MonitorFragment mf = new MonitorFragment();
    BottomNavigationView bn;
    private ImageButton nav_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("lapi", "onCreate: Activity Created");
        bn = findViewById(R.id.bottom_nav);
        nav_button = findViewById(R.id.nav_btn);
        final boolean[] liveapi = {false};


        nav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.live) {
                            liveapi[0] = true;
                            Log.d("lapi", "onCreateView: "+liveapi[0]);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.nav);
                popupMenu.show();
            }
        });


        if(savedInstanceState == null){
        getSupportFragmentManager().beginTransaction().replace(R.id.container,tf).commit();
        }

        bn.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                    if(item.getItemId() == R.id.Tracking){
                       ReplaceF(tf,liveapi[0]);
                        return true;
                    }
                    else if(item.getItemId() == R.id.Reserve) {
                        ReplaceF(rf,liveapi[0]);
                        return true;
                    }
                    else if(item.getItemId() == R.id.Monitor) {
                        ReplaceF(mf,liveapi[0]);
                        return true;
                    }
            return false;
            }
        });
    }

    public void ReplaceF(Fragment f,boolean lapi){

        Bundle bundle = new Bundle();
        bundle.putBoolean("liveapi", lapi);
        Log.d("lapi", "onCreateView: "+lapi);
        f.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.container,f).addToBackStack(null).commit();
    }
}
