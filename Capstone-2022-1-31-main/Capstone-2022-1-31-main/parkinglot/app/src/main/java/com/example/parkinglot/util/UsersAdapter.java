package com.example.parkinglot.util;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.parkinglot.R;

import java.util.ArrayList;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.CustomViewHolder> {

    private ArrayList<PersonalData> mList;
    private Activity context;

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView park_num;
        //protected TextView park_temp;
        //protected TextView park_light;//////////////////
        protected TextView park_line;
        protected TextView park_1st;
        protected TextView park_2nd;
        protected TextView park_3rd;
        protected TextView park_4th;
        //protected FrameLayout park_line_f;
        protected FrameLayout park_1st_f;
        protected FrameLayout park_2nd_f;
        protected FrameLayout park_3rd_f;
        protected FrameLayout park_4th_f;


        public CustomViewHolder(View view) {
            super(view);
//            this.park_num = (TextView) view.findViewById(R.id.textView_park_num);
            //this.park_temp = (TextView) view.findViewById(R.id.textView_park_temp);
            //this.park_light = (TextView) view.findViewById(R.id.textView_park_light);///////////////////
            this.park_line = (TextView) view.findViewById(R.id.textView_park_line);
            this.park_1st = (TextView) view.findViewById(R.id.textView_park_1st);
            this.park_2nd = (TextView) view.findViewById(R.id.textView_park_2nd);
            this.park_3rd = (TextView) view.findViewById(R.id.textView_park_3rd);
            this.park_4th = (TextView) view.findViewById(R.id.textView_park_4th);
            this.park_1st_f = (FrameLayout) view.findViewById(R.id.frameView_park_1st);
            this.park_2nd_f = (FrameLayout) view.findViewById(R.id.frameView_park_2nd);
            this.park_3rd_f = (FrameLayout) view.findViewById(R.id.frameView_park_3rd);
            this.park_4th_f = (FrameLayout) view.findViewById(R.id.frameView_park_4th);
        }
    }

    public UsersAdapter(Activity context, ArrayList<PersonalData> list) {
        this.context = context;
        this.mList = list;
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        //int par_temp = Integer.parseInt(mList.get(position).getPark_temp());
        //int par_light = Integer.parseInt(mList.get(position).getPark_light());//////////////////////

        int park_1st_int = Integer.parseInt(mList.get(position).getPark_1st());
        int park_2nd_int = Integer.parseInt(mList.get(position).getPark_2nd());
        int park_3rd_int = Integer.parseInt(mList.get(position).getPark_3rd());
        int park_4th_int = Integer.parseInt(mList.get(position).getPark_4th());

        if(park_1st_int == 2){
            viewholder.park_1st.setText(" 1 ");
            // 입차
            viewholder.park_1st_f.setBackgroundResource(R.color.red_icon_color);
            viewholder.park_1st_f.setVisibility(View.VISIBLE);
        }
        else if (park_1st_int == 1) {
            viewholder.park_1st.setText(" 1 ");
            // 입차
            viewholder.park_1st_f.setBackgroundResource(R.color.grey);
            viewholder.park_1st_f.setVisibility(View.VISIBLE);
        }
        else{
            viewholder.park_1st.setText(" 1 ");
            // 출차
            viewholder.park_1st_f.setBackgroundResource(R.color.blue_icon_color);
            viewholder.park_1st_f.setVisibility(View.VISIBLE);
        }

        if(park_2nd_int == 2){
            viewholder.park_2nd.setText(" 2 ");
            // 입차
            viewholder.park_2nd_f.setBackgroundResource(R.color.red_icon_color);
            viewholder.park_2nd_f.setVisibility(View.VISIBLE);
        }
        else if (park_1st_int == 1) {
            viewholder.park_1st.setText(" 2 ");
            // 입차
            viewholder.park_1st_f.setBackgroundResource(R.color.grey);
            viewholder.park_1st_f.setVisibility(View.VISIBLE);
        }
        else{
            viewholder.park_2nd.setText(" 2 ");
            // 출차
            viewholder.park_2nd_f.setBackgroundResource(R.color.blue_icon_color);
            viewholder.park_2nd_f.setVisibility(View.VISIBLE);
        }

        if(park_3rd_int == 2){
            viewholder.park_3rd.setText(" 3 ");
            // 입차
            viewholder.park_3rd_f.setBackgroundResource(R.color.red_icon_color);
            viewholder.park_3rd_f.setVisibility(View.VISIBLE);
        }
        else if (park_1st_int == 1) {
            viewholder.park_1st.setText(" 3 ");
            // 입차
            viewholder.park_1st_f.setBackgroundResource(R.color.grey);
            viewholder.park_1st_f.setVisibility(View.VISIBLE);
        }
        else{
            viewholder.park_3rd.setText(" 3 ");
            // 출차
            viewholder.park_3rd_f.setBackgroundResource(R.color.blue_icon_color);
            viewholder.park_3rd_f.setVisibility(View.VISIBLE);
        }

        if(park_4th_int == 2){
            viewholder.park_4th.setText(" 4 ");
            // 입차
            viewholder.park_4th_f.setBackgroundResource(R.color.red_icon_color);
            viewholder.park_4th_f.setVisibility(View.VISIBLE);
        }
        else if (park_1st_int == 1) {
            viewholder.park_1st.setText(" 4 ");
            // 입차
            viewholder.park_1st_f.setBackgroundResource(R.color.grey);
            viewholder.park_1st_f.setVisibility(View.VISIBLE);
        }
        else{
            viewholder.park_4th.setText(" 4 ");
            // 출차
            viewholder.park_4th_f.setBackgroundResource(R.color.blue_icon_color);
            viewholder.park_4th_f.setVisibility(View.VISIBLE);
        }

//        viewholder.park_num.setText(mList.get(position).getPark_num());
//        viewholder.park_temp.setText(mList.get(position).getPark_temp());
//        viewholder.park_light.setText(mList.get(position).getPark_light());
        viewholder.park_line.setText("라인" + mList.get(position).getPark_line());
//        viewholder.park_1st.setText(mList.get(position).getPark_1st());
//        viewholder.park_2nd.setText(mList.get(position).getPark_2nd());
//        viewholder.park_3rd.setText(mList.get(position).getPark_3rd());
//        viewholder.park_4th.setText(mList.get(position).getPark_4th());
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}
