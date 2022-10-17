package com.example.parkinglot.util;

import android.content.Context;
import com.example.parkinglot.entity.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {

    /* 숫자 체크 */
    public static boolean isNumeric(String str) {
        boolean chk = false;

        try{
            Double.parseDouble(str) ;
            chk = true ;
        } catch (Exception ignored) {}

        return chk;
    }

    /* 날자 구하기 */
    public static String getDate(String format, long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date(timeMillis);

        return dateFormat.format(date);
    }

    /* 시간 표현 값 얻기(시,분) */
    public static String getDisplayTime(long millis) {
        StringBuilder str = new StringBuilder();

        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;

        if(hour > 0) {
            str.append(hour).append("시간");
        }
        if(minute > 0) {
            str.append(minute).append("분");
        }
        if(second > 0) {
            str.append(second).append("초");
        }

        if (TextUtils.isEmpty(str.toString())) {
            str.append("0초");
        }

        return str.toString();
    }

    /* 거리 계산 */
    public static double getDistance(double lat1 , double lng1 , double lat2 , double lng2 ){
        double distance;

        Location locationA = new Location("point A");
        locationA.setLatitude(lat1);
        locationA.setLongitude(lng1);

        Location locationB = new Location("point B");
        locationB.setLatitude(lat2);
        locationB.setLongitude(lng2);

        distance = locationA.distanceTo(locationB);

        return distance;
    }

    /* 거리 */
    public static String getDistanceStr(double distance) {
        String distanceStr;

        // 1km 이상이면
        if (distance > 1000) {
            distance = distance / 1000;
            // 소수점 한자리까지 표시 (반올림)
            distanceStr = (Math.round(distance * 10) / 10.0) + "km";
        } else {
            // 소수점 버림
            distanceStr = (int) Math.floor(distance) + "m";
        }

        return distanceStr;
    }

    /* 주소로 GPS (위도,경도) 얻기 */
    public static Point getGpsFromAddress(Context context, String address) {
        // 지오코더... 주소 를 GPS 로 변환
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocationName(address, 1);
        } catch (IllegalArgumentException illegalArgumentException) {
            return null;
        } catch (IOException ioException) {
            //네트워크 문제
            return null;
        }

        if (addresses == null || addresses.size() == 0) {
            return null;
        } else {
            // 위도 경도 넘김
            return new Point((int)Math.round(addresses.get(0).getLatitude()),
                    (int)Math.round(addresses.get(0).getLongitude()));
        }
    }

    /* GPS 정보로 주소 얻기 */
    public static String getAddressFromGps(Context context, double lat, double lng) {
        // 지오코더... GPS 를 주소로 변환
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IllegalArgumentException illegalArgumentException) {
            return "잘못된 GPS 좌표";
        } catch (IOException ioException) {
            //네트워크 문제
            return "네트워크 오류";
        }

        if (addresses == null || addresses.size() == 0) {
            return "주소정보가 없습니다.";
        } else {
            Address address = addresses.get(0);
            String addr = address.getAddressLine(0);
            addr = addr.replace("대한민국", "").trim();
            return addr;
        }
    }
}