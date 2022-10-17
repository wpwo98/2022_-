package com.example.parkinglot.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkinglot.R;
import com.example.parkinglot.entity.ParkingLot;

import java.util.ArrayList;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private ArrayList<ParkingLot> items;

    public BookmarkAdapter(OnItemClickListener listener, ArrayList<ParkingLot> items) {
        this.listener = listener;
        this.items = items;
    }

    /* 삭제 */
    public ParkingLot remove(int position){
        ParkingLot data = null;

        if (position < getItemCount()) {
            data = this.items.get(position);
            // 즐겨찾기 삭제
            this.items.remove(position);
            // 삭제된 즐겨찾기를 리스트에 적용하기 위함
            notifyItemRemoved(position);
        }

        return data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, null);

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
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtParkingLotName, txtAddress;
        ImageView imgDelete;

        ViewHolder(View view) {
            super(view);

            this.txtParkingLotName = view.findViewById(R.id.txtParkingLotName);
            this.txtAddress = view.findViewById(R.id.txtAddress);
            this.imgDelete = view.findViewById(R.id.imgDelete);

            this.imgDelete.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // 리스트 선택 및 삭제
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(view, position);
            }
        }
    }
}