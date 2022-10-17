package com.example.parkinglot.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkinglot.R;
import com.example.parkinglot.entity.History;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private ArrayList<History> items;

    public HistoryAdapter(OnItemClickListener listener, ArrayList<History> items) {
        this.listener = listener;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null);

        // Item 사이즈 조절
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        // ViewHolder 생성
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtParkingLotName.setText(this.items.get(position).parkingLotName);  // 주차장명

        // 주소
        holder.txtAddress.setText(Utils.getAddressFromGps(holder.txtAddress.getContext(),
                this.items.get(position).latitude, this.items.get(position).longitude));

        holder.txtDateTime.setText(Utils.getDate("yyyy-MM-dd HH:mm", this.items.get(position).inputTimeMillis));    // 등록일시
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtParkingLotName, txtAddress, txtDateTime;

        private ViewHolder(View view) {
            super(view);

            this.txtParkingLotName = view.findViewById(R.id.txtParkingLotName);
            this.txtAddress = view.findViewById(R.id.txtAddress);
            this.txtDateTime = view.findViewById(R.id.txtDateTime);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // 리스트 선택
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(view, position);
            }
        }
    }
}