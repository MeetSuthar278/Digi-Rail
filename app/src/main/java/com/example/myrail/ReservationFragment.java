package com.example.myrail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ResevationFragment extends Fragment {


    Button irctc;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getContext();
        View v = inflater.inflate(R.layout.fragment_resevation,container,false);

        irctc = v.findViewById(R.id.irctc);

        irctc.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), IrctcReservationWebview.class);
            startActivity(i);
        });


        return v;
    }
}