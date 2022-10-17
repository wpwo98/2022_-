package com.example.parkinglot.util;

import com.example.parkinglot.entity.ParkingLotItem;

import java.util.ArrayList;

public class GlobalVariable {

    public static ArrayList<ParkingLotItem> parkingLotItems;    // 전국 주차장 array
    public static ParkingLotItem parkingLotItem;                // 주차장 정보 객체

    public static long parkingTimeMillis1;                      // 주차장1 주차시간
    public static long parkingTimeMillis2;                      // 주차장2 주차시간
    public static long parkingTimeMillis3;                      // 주차장3 주차시간
    public static long parkingTimeMillis4;                      // 주차장4 주차시간
}