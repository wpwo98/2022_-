package com.example.parkinglot.entity;

public class History {

    public long id;                     // 일련번호
    public String parkingLotNo;         // 주차장관리번호
    public String parkingLotName;       // 주차장명
    public double latitude;             // 위도
    public double longitude;            // 경도
    public long inputTimeMillis;        // 등록일시를 millisecond 로 표현

    public History(long id, String parkingLotNo, String parkingLotName,
                   double latitude, double longitude, long inputTimeMillis) {
        this.id = id;
        this.parkingLotNo = parkingLotNo;
        this.parkingLotName = parkingLotName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.inputTimeMillis = inputTimeMillis;
    }
}