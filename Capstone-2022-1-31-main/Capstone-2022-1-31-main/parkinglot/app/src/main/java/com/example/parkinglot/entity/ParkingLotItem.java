package com.example.parkinglot.entity;

public class ParkingLotItem {

    public String prkplceNo;            // 주차장관리번호
    public String prkplceNm;            // 주차장명
    public String prkplceSe;            // 주차장구분
    public String prkplceType;          // 주차장유형
    public String rdnmadr;              // 소재지도로명주소
    public String lnmadr;               // 소재지지번주소
    public String prkcmprt;             // 주차구획수
    public String feedingSe;            // 급지구분
    public String enforceSe;            // 부제시행구분
    public String operDay;              // 운영요일
    public String weekdayOperOpenHhmm;  // 평일운영시작시각
    public String weekdayOperColseHhmm; // 평일운영종료시각
    public String satOperOperOpenHhmm;  // 토요일운영시작시각
    public String satOperCloseHhmm;     // 토요일운영종료시각
    public String holidayOperOpenHhmm;  // 공휴일운영시작시각
    public String holidayCloseOpenHhmm; // 공휴일운영종료시각
    public String parkingchrgeInfo;     // 요금정보
    public String basicTime;            // 주차기본시간
    public String basicCharge;          // 주차기본요금
    public String addUnitTime;          // 추가단위시간
    public String addUnitCharge;        // 추가단위요금
    public String dayCmmtktAdjTime;     // 1일주차권요금적용시간
    public String dayCmmtkt;            // 1일주차권요금
    public String monthCmmtkt;          // 월정기권요금
    public String metpay;               // 결제방법
    public String spcmnt;               // 특기사항
    public String institutionNm;        // 관리기관명
    public String phoneNumber;          // 전화번호
    public String latitude;             // 위도
    public String longitude;            // 경도
    public String referenceDate;        // 데이터기준일자
    public String instt_code;           // 제공기관코드
    public String instt_nm;             // 제공기관기관명

    public double distance;             // 나와의 거리
}