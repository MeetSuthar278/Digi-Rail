package com.example.myrail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CoachListAdapter extends RecyclerView.Adapter<CoachListAdapter.ViewHolder> {
    private ArrayList<CoachItem> mData;
    private static OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener =  listener;
    }

    public CoachListAdapter(ArrayList<CoachItem> data) {
        mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coach_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CoachItem item = mData.get(position);
        holder.coach_btn.setImageResource(item.getImageResource());
        holder.coach_id.setText(item.getCid());
        holder.coach_no.setText(item.getCno());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton coach_btn;
        public TextView coach_id, coach_no;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coach_btn = itemView.findViewById(R.id.coach_btn);
            coach_id = itemView.findViewById(R.id.coach_id);
            coach_no = itemView.findViewById(R.id.coach_no);


            coach_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
        });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(v, position);
                        }
                    }
                }
            });

            coach_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(v, position);
                        }
                    }
                }
            });
        }
    }
}

