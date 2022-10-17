package com.example.parkinglot.entity;

public class Point {

    public double latitude;                     // 위도
    public double longitude;                    // 경도

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}