package com.example.parkinglot.util;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkinglot.R;
import com.example.parkinglot.entity.ParkingLotItem;

import java.util.ArrayList;

public class ParkingLotAdapter extends RecyclerView.Adapter<ParkingLotAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private ArrayList<ParkingLotItem> items;

    private double latitude, longitude;     // 나의 위도 / 경도

    public ParkingLotAdapter(OnItemClickListener listener, ArrayList<ParkingLotItem> items,
                             double latitude, double longitude) {
        this.listener = listener;
        this.items = items;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking_lot, null);

        // Item 사이즈 조절
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        // ViewHolder 생성
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtName.setText(this.items.get(position).prkplceNm);             // 주차장명

        // 주소
        String address = this.items.get(position).rdnmadr;      // 도로명
        if (TextUtils.isEmpty(address)) {
            address = this.items.get(position).lnmadr;          // 지번
        }
        if (!TextUtils.isEmpty(address)) {
            holder.txtAddress.setText(address);
            holder.txtAddress.setVisibility(View.VISIBLE);
        } else {
            holder.txtAddress.setVisibility(View.GONE);
        }

        // 주차장구분 주차장유형 요금정보
        String detail = this.items.get(position).prkplceSe + "  " + this.items.get(position).prkplceType +
                "  " + this.items.get(position).parkingchrgeInfo;
        holder.txtDetail.setText(detail);

        if (this.items.get(position).distance == Constants.NO_DISTANCE) {
            // 거리정보가 없으면
            holder.txtDistance.setText("");
        } else {
            // 거리 표시
            holder.txtDistance.setText(Utils.getDistanceStr(this.items.get(position).distance));
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtName, txtDistance, txtAddress, txtDetail;

        ViewHolder(View view) {
            super(view);

            this.txtName = view.findViewById(R.id.txtName);
            this.txtDistance = view.findViewById(R.id.txtDistance);
            this.txtAddress = view.findViewById(R.id.txtAddress);
            this.txtDetail = view.findViewById(R.id.txtDetail);

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
