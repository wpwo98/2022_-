package com.example.parkinglot.util;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.parkinglot.R;

public class QRCodeResultPopup extends PopupWindow {

    public QRCodeResultPopup(View view, String code, long parkingTimeMillis) {
        super(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        String parkingLotName, message;

        switch (code) {
            case "A001":
                parkingLotName = "주차장 1";
                if (GlobalVariable.parkingTimeMillis1 == 0) {
                    message = "입차일시 " + Utils.getDate("yyyy-MM-dd HH:mm:ss", parkingTimeMillis);
                    GlobalVariable.parkingTimeMillis1 = parkingTimeMillis;
                } else {
                    message = "주차시간 " + Utils.getDisplayTime(parkingTimeMillis - GlobalVariable.parkingTimeMillis1);
                    GlobalVariable.parkingTimeMillis1 = 0;
                }
                break;
            case "A002":
                parkingLotName = "주차장 2";
                message = "주차 가능 공간이 없습니다.";
                break;
            case "A003":
                parkingLotName = "주차장 3";
                if (GlobalVariable.parkingTimeMillis3 == 0) {
                    message = "입차일시 " + Utils.getDate("yyyy-MM-dd HH:mm:ss", parkingTimeMillis);
                    GlobalVariable.parkingTimeMillis3 = parkingTimeMillis;
                } else {
                    message = "주차시간 " + Utils.getDisplayTime(parkingTimeMillis - GlobalVariable.parkingTimeMillis3);
                    GlobalVariable.parkingTimeMillis3 = 0;
                }
                break;
            case "A004":
                parkingLotName = "주차장 4";
                if (GlobalVariable.parkingTimeMillis4 == 0) {
                    message = "입차일시 " + Utils.getDate("yyyy-MM-dd HH:mm:ss", parkingTimeMillis);
                    GlobalVariable.parkingTimeMillis4 = parkingTimeMillis;
                } else {
                    message = "주차시간 " + Utils.getDisplayTime(parkingTimeMillis - GlobalVariable.parkingTimeMillis4);
                    GlobalVariable.parkingTimeMillis4 = 0;
                }
                break;
            default:
                parkingLotName = "확인불가";
                message = "알수없음";
                break;
        }

        ((TextView) view.findViewById(R.id.txtTitle)).setText(parkingLotName);
        ((TextView) view.findViewById(R.id.txtMessage)).setText(message);

        view.findViewById(R.id.btnOk).setOnClickListener(view1 -> {
            // 확인
            dismiss();
        });
    }
}