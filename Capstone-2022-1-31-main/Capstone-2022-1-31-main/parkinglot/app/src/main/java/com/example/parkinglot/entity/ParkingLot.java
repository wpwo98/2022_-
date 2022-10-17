package com.example.parkinglot.entity;

public class ParkingLot {
    public String parkingLotNo;
    public String parkingLotName;
    public double latitude;
    public double longitude;

    public ParkingLot(String parkingLotNo, String parkingLotName, double latitude, double longitude) {
        this.parkingLotNo = parkingLotNo;
        this.parkingLotName = parkingLotName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
