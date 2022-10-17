package com.example.parkinglot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.parkinglot.entity.ParkingLot;
import com.example.parkinglot.util.Constants;
import com.example.parkinglot.util.DBHelper;
import com.example.parkinglot.util.Utils;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.Objects;

public class NationalParkingLotActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = NationalParkingLotActivity.class.getSimpleName();

    // GPS
    private LocationManager locationManager;
    private Location location;

    private TMapView tMapView;                                          // T Map
    private TextView txtParkingLot, txtDistance;

    private TMapMarkerItem startMarkerItem, endMarkerItem;
    private String parkingLotNo;                                        // 주차장관리번호 (마커, 라인 삭제에 필요)

    private ParkingLot parkingLot;                                      // 주차장

    private static final long GPS_MIN_TIME_BW_UPDATES = 1000;           // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1초
    private static final long GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;  // 최소 GPS 정보 업데이트 거리 0미터 (0인경우 거리변화 사용안함)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_national_parking_lot);

        // 제목 표시
        setTitle(getString(R.string.title_parking_lot_find));

        // 홈버튼(<-) 표시
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.txtParkingLot = findViewById(R.id.txtParkingLot);
        this.txtDistance = findViewById(R.id.txtDistance);
        this.txtParkingLot.setText("");
        this.txtDistance.setText("");

        findViewById(R.id.imgSearch).setOnClickListener(view -> {
            // 주차장 검색
            if (this.location == null) {
                return;
            }

            // 주차장 검색
            Intent intent = new Intent(this, NationalParkingLotSearchActivity.class);
            intent.putExtra("latitude", this.location.getLatitude());
            intent.putExtra("longitude", this.location.getLongitude());
            this.parkingLotActivityLauncher.launch(intent);
        });

        findViewById(R.id.btnBookmarkAdd).setOnClickListener(view -> {
            if (this.parkingLot == null) {
                Toast.makeText(this, R.string.msg_parking_lot_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            // 즐겨찾기에 추가
            addBookmark();
        });

        LinearLayout layMap = findViewById(R.id.layMap);
        // T Map
        this.tMapView = new TMapView(this);
        // T Map key 값 설정
        this.tMapView.setSKTMapApiKey(getString(R.string.tmap_api_key));
        layMap.addView(this.tMapView);

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 위치정보 사용여부 체크
        if (checkLocationServicesStatus()) {
            // Location 초기화
            initLocation();
        } else {
            // 위치정보 설정값으로 보여주기
            showLocationSettings();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.locationManager != null) {
            // 위치정보 갱신 리스너 제거
            this.locationManager.removeUpdates(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // main 메뉴 생성
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_bookmark:
                // 즐겨찾기
                intent = new Intent(this, BookmarkActivity.class);
                this.parkingLotActivityLauncher.launch(intent);
                return true;
            case R.id.menu_history:
                // 내역
                intent = new Intent(this, HistoryActivity.class);
                this.parkingLotActivityLauncher.launch(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "GPS: " + "위도 " + location.getLatitude() + ", 경도 " + location.getLongitude());
        this.location = location;

        // 중심점 이동 (애니메이션 효과)
        this.tMapView.setCenterPoint(this.location.getLongitude(), this.location.getLatitude(), true);

        // 현재마커 삭제 후 다시 생성
        this.tMapView.removeMarkerItem("my");
        createMarker(this.location.getLatitude(), this.location.getLongitude(), "my", "", "나", "", R.drawable.ic_map_marker_my);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "GPS OFF");

        // GPS OFF 될때
        if (this.locationManager != null) {
            this.locationManager.removeUpdates(this);
        }
        this.location = null;
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "GPS ON");

        // GPS ON 될때
        initLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    /* Location 초기화 */
    private void initLocation() {
        // GPS 사용여부
        boolean gpsEnabled = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 사용여부
        boolean networkEnabled = this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            // GPS 와 네트워크사용이 가능하지 않음
            Toast.makeText(this, getString(R.string.msg_location_disable), Toast.LENGTH_SHORT).show();
        } else {
            try {
                // 네트워크 정보로 부터 위치값 가져오기
                if (networkEnabled) {
                    this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            GPS_MIN_TIME_BW_UPDATES, GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    // 이전에 저장된 위치정보가 있으면 가져옴
                    this.location = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                // GPS 로 부터 위치값 가져오기
                if (gpsEnabled && this.locationManager != null) {
                    this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            GPS_MIN_TIME_BW_UPDATES, GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (this.location == null) {
                        // 이전에 저장된 위치정보가 있으면 가져옴
                        this.location = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }

                if (this.location != null) {
                    Log.d(TAG, "GPS: " + "위도 " + this.location.getLatitude() + ", 경도 " + this.location.getLongitude());
                } else {
                    // 처음 위치정보 가져올 때는 이전 정보가 없기 때문에 null 값임
                    Log.d(TAG, "GPS: NULL");
                }
            } catch (SecurityException e) {
                Log.d(TAG, "Error: " + e.toString());
            }
        }
    }

    /* 위치정보 사용여부 체크 */
    private boolean checkLocationServicesStatus() {
        return this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /* 위치정보 설정값으로 보여주기 */
    private void showLocationSettings() {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.dialog_ok, (dialog, id) -> {
                    // 위치 서비스 설정창
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.cancel())
                .setCancelable(true)
                .setTitle(R.string.dialog_title_location_setting)
                .setMessage(R.string.dialog_msg_location_setting)
                .show();
    }

    /* 마커 생성 */
    private TMapMarkerItem createMarker(double latitude, double longitude, String id, String name,
                                        String title, String subTitle, int iconRes) {
        // 마커
        TMapMarkerItem markerItem = new TMapMarkerItem();
        // 마커 위치
        TMapPoint mapPoint = new TMapPoint(latitude, longitude);

        markerItem.setCanShowCallout(true);                         // 풍선뷰 사용
        markerItem.setCalloutTitle(title);                          // 풍선뷰 타이틀
        markerItem.setCalloutSubTitle(subTitle);                    // 풍선뷰 서브 타이틀

        // 마커 아이콘
        Bitmap icon = BitmapFactory.decodeResource(getResources(), iconRes);

        markerItem.setIcon(icon);                                   // 마커 아이콘 지정
        markerItem.setPosition(0.5f, 1.0f);                         // 마커의 중심점을 중앙, 하단으로 설정
        markerItem.setTMapPoint(mapPoint);                          // 마커의 좌표 지정
        markerItem.setName(name);                                   // 마커의 타이틀 지정
        markerItem.setVisible(TMapMarkerItem.VISIBLE);
        this.tMapView.addMarkerItem(id, markerItem);                // 지도에 마커 추가

        return markerItem;
    }

    /* 검색 내역 저장 */
    private void save(ParkingLot lot) {
        SQLiteDatabase db = DBHelper.getInstance(this).getReadableDatabase();

        try {
            // 내역 저장
            String sql = "INSERT INTO " + Constants.DBTableName.HISTORY + "(parkingLotNo, parkingLotName, latitude, longitude, inputTimeMillis) " +
                    "VALUES(?, ?, ?, ?, ?)";
            Object[] args = { lot.parkingLotNo, lot.parkingLotName, lot.latitude, lot.longitude, System.currentTimeMillis() };
            db.execSQL(sql, args);
        } catch (SQLException ignored) {
            // 오류
            Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    /* 즐겨찾기에 추가 */
    private void addBookmark() {
        SQLiteDatabase db = DBHelper.getInstance(this).getReadableDatabase();
        long count = 0;

        // select 쿼리문 (즐겨찾기에 있는지 체크)
        String sql = "SELECT count(*) FROM " + Constants.DBTableName.BOOKMARK + " WHERE parkingLotNo = ?";
        String[] args = { this.parkingLot.parkingLotNo };
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor.moveToFirst()) {
            count = cursor.getLong(0);
        }
        cursor.close();

        if (count == 0) {
            // 즐겨찾기에 추가
            try {
                // 즐겨찾기 저장
                sql = "INSERT INTO " + Constants.DBTableName.BOOKMARK + "(parkingLotNo, parkingLotName, latitude, longitude) " +
                        "VALUES(?, ?, ?, ?)";
                Object[] args1 = { this.parkingLot.parkingLotNo, this.parkingLot.parkingLotName, this.parkingLot.latitude, this.parkingLot.longitude };
                db.execSQL(sql, args1);

                Toast.makeText(this, R.string.msg_bookmark_add, Toast.LENGTH_SHORT).show();
            } catch (SQLException ignored) {
                // 오류
                Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.msg_bookmark_exist, Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    /* 주차장선택 ActivityForResult */
    private final ActivityResultLauncher<Intent> parkingLotActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // 주차장 선택
                    Intent data = result.getData();
                    if (data != null) {
                        final String no = data.getStringExtra("parking_no");
                        String parkingLot = data.getStringExtra("parking_lot");
                        double latitude = data.getDoubleExtra("latitude", 0);
                        double longitude = data.getDoubleExtra("longitude", 0);

                        // 주차장
                        this.parkingLot = new ParkingLot(no, parkingLot, latitude, longitude);

                        if (data.getBooleanExtra("save", false)) {
                            // 검색 내역 저장
                            save(this.parkingLot);
                        }
                        this.txtParkingLot.setText(parkingLot);         // 주차장

                        // 거리
                        double distance = Utils.getDistance(this.location.getLatitude(), this.location.getLongitude(),
                                latitude, longitude);
                        this.txtDistance.setText(Utils.getDistanceStr(distance));

                        // 출발지마커 삭제 후 다시 생성
                        if (!TextUtils.isEmpty(this.parkingLotNo)) {
                            this.tMapView.removeMarkerItem(this.parkingLotNo + "_s");
                        }
                        this.startMarkerItem = createMarker(this.location.getLatitude(), this.location.getLongitude(),
                                no + "_s", "", "츨발", "", R.drawable.ic_map_marker_start);

                        // 주차장마커 삭제 후 다시 생성
                        if (!TextUtils.isEmpty(this.parkingLotNo)) {
                            this.tMapView.removeMarkerItem(this.parkingLotNo + "_e");
                        }
                        this.endMarkerItem = createMarker(latitude, longitude,
                                no + "_e", parkingLot, "도착", parkingLot, R.drawable.ic_map_marker_parking_lot);

                        // 경로 표시는 쓰레드로 구현해야됨
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    if (!TextUtils.isEmpty(parkingLotNo)) {
                                        // 이전 경로 삭제
                                        tMapView.removeTMapPolyLine(parkingLotNo);
                                    }

                                    parkingLotNo = no;

                                    // TMapData.TMapPathType.CAR_PATH : 자동차 경로
                                    // TMapData.TMapPathType.PEDESTRIAN_PATH : 보행자 경로
                                    TMapPolyLine tMapPolyLine = new TMapData().findPathDataWithType(TMapData.TMapPathType.CAR_PATH,
                                            startMarkerItem.getTMapPoint(), endMarkerItem.getTMapPoint());

                                    tMapPolyLine.setLineColor(Color.BLUE);
                                    tMapPolyLine.setLineWidth(2);
                                    tMapView.addTMapPolyLine(parkingLotNo, tMapPolyLine);
                                } catch(Exception e) {
                                    Log.d(TAG, "오류:" + e.toString());
                                }
                            }
                        }.start();
                    }
                }
            });
}
