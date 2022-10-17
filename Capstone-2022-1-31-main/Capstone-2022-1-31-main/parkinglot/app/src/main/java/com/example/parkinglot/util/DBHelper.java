package com.example.parkinglot.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private volatile static DBHelper _instance = null;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ParkingLot.db";

    /* 싱글톤 패턴 적용 */
    public static DBHelper getInstance(Context context) {
        if (_instance == null) {
            synchronized (DBHelper.class) {
                if (_instance == null) {
                    _instance = new DBHelper(context);
                }
            }
        }

        return _instance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 테이블 생성

        // 내역 (_id(일련번호), parkingLotNo(주차장관리번호), parkingLotName(주차장명), latitude(위도), longitude(경도), inputTimeMillis(등록일시를 millisecond 로 표현))
        db.execSQL("CREATE TABLE " + Constants.DBTableName.HISTORY + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "parkingLotNo TEXT NOT NULL, parkingLotName TEXT NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, inputTimeMillis INTEGER NOT NULL);");

        // 즐겨찾기 (parkingLotNo(주차장관리번호), parkingLotName(주차장명), latitude(위도), longitude(경도))
        db.execSQL("CREATE TABLE " + Constants.DBTableName.BOOKMARK + "(parkingLotNo TEXT PRIMARY KEY, " +
                "parkingLotName TEXT NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 테이블 삭제
        db.execSQL("DROP TABLE IF EXISTS "  + Constants.DBTableName.HISTORY);
        db.execSQL("DROP TABLE IF EXISTS "  + Constants.DBTableName.BOOKMARK);

        // 테이블 삭제후 다시 생성하기 위함
        onCreate(db);
    }
}