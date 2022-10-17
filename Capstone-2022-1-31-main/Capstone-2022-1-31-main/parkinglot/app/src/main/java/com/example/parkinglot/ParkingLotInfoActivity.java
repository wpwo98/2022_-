package com.example.parkinglot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkinglot.util.Constants;
import com.example.parkinglot.util.DatabaseActivity;
import com.example.parkinglot.util.GlobalVariable;
import com.example.parkinglot.util.Utils;

import java.util.Objects;

public class ParkingLotInfoActivity extends AppCompatActivity {
    //private static final String TAG = ParkingLotInfoActivity.class.getSimpleName();
    private static final String TAG = "ParkingLot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);
        setContentView(R.layout.activity_parking_lot_info);

        // 나의 위도 / 경도 정보
        Intent intent = getIntent();
        final int position = intent.getIntExtra("position", -1);

        // 제목 표시
        setTitle(getString(R.string.title_parking_lot_info));

        // 홈버튼(<-) 표시
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Button checkButton = findViewById(R.id.btnCheckParkingLot);
        checkButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(ParkingLotInfoActivity.this, DatabaseActivity.class);
                startActivity(intent2);
            }
        });

        findViewById(R.id.btnGetDirections).setOnClickListener(view -> {
            // 길찾기
            // Activity 에 전달
            Intent intent1 = new Intent();
            intent1.putExtra("position", position);
            setResult(Activity.RESULT_OK, intent1);
            finish();
        });

        info();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 주차장 정보 */
    private void info() {
        ((TextView) findViewById(R.id.txtParkingLotName)).setText(GlobalVariable.parkingLotItem.prkplceNm);

        TextView txtDistance = findViewById(R.id.txtDistance);
        if (GlobalVariable.parkingLotItem.distance == Constants.NO_DISTANCE) {
            // 거리정보가 없으면
            txtDistance.setVisibility(View.GONE);
        } else {
            // 거리 표시
            txtDistance.setText(Utils.getDistanceStr(GlobalVariable.parkingLotItem.distance));
        }

        ((TextView) findViewById(R.id.txtPrkplceNo)).setText(GlobalVariable.parkingLotItem.prkplceNo);
        ((TextView) findViewById(R.id.txtPrkplceSe)).setText(GlobalVariable.parkingLotItem.prkplceSe);
        ((TextView) findViewById(R.id.txtPrkplceType)).setText(GlobalVariable.parkingLotItem.prkplceType);
        ((TextView) findViewById(R.id.txtRdnmadr)).setText(GlobalVariable.parkingLotItem.rdnmadr);
        ((TextView) findViewById(R.id.txtLnmadr)).setText(GlobalVariable.parkingLotItem.lnmadr);
        ((TextView) findViewById(R.id.txtPrkcmprt)).setText(GlobalVariable.parkingLotItem.prkcmprt);
        ((TextView) findViewById(R.id.txtFeedingSe)).setText(GlobalVariable.parkingLotItem.feedingSe);
        ((TextView) findViewById(R.id.txtEnforceSe)).setText(GlobalVariable.parkingLotItem.enforceSe);
        ((TextView) findViewById(R.id.txtOperDay)).setText(GlobalVariable.parkingLotItem.operDay);
        ((TextView) findViewById(R.id.txtWeekdayOperOpenHhmm)).setText(GlobalVariable.parkingLotItem.weekdayOperOpenHhmm);
        ((TextView) findViewById(R.id.txtWeekdayOperColseHhmm)).setText(GlobalVariable.parkingLotItem.weekdayOperColseHhmm);
        ((TextView) findViewById(R.id.txtSatOperOperOpenHhmm)).setText(GlobalVariable.parkingLotItem.satOperOperOpenHhmm);
        ((TextView) findViewById(R.id.txtSatOperCloseHhmm)).setText(GlobalVariable.parkingLotItem.satOperCloseHhmm);
        ((TextView) findViewById(R.id.txtHolidayOperOpenHhmm)).setText(GlobalVariable.parkingLotItem.holidayOperOpenHhmm);
        ((TextView) findViewById(R.id.txtHolidayCloseOpenHhmm)).setText(GlobalVariable.parkingLotItem.holidayCloseOpenHhmm);
        ((TextView) findViewById(R.id.txtParkingchrgeInfo)).setText(GlobalVariable.parkingLotItem.parkingchrgeInfo);
        ((TextView) findViewById(R.id.txtBasicTime)).setText(GlobalVariable.parkingLotItem.basicTime);
        ((TextView) findViewById(R.id.txtBasicCharge)).setText(GlobalVariable.parkingLotItem.basicCharge);
        ((TextView) findViewById(R.id.txtAddUnitTime)).setText(GlobalVariable.parkingLotItem.addUnitTime);
        ((TextView) findViewById(R.id.txtAddUnitCharge)).setText(GlobalVariable.parkingLotItem.addUnitCharge);
        ((TextView) findViewById(R.id.txtDayCmmtktAdjTime)).setText(GlobalVariable.parkingLotItem.dayCmmtktAdjTime);
        ((TextView) findViewById(R.id.txtDayCmmtkt)).setText(GlobalVariable.parkingLotItem.dayCmmtkt);
        ((TextView) findViewById(R.id.txtMonthCmmtkt)).setText(GlobalVariable.parkingLotItem.monthCmmtkt);
        ((TextView) findViewById(R.id.txtMetpay)).setText(GlobalVariable.parkingLotItem.metpay);
        ((TextView) findViewById(R.id.txtSpcmnt)).setText(GlobalVariable.parkingLotItem.spcmnt);
        ((TextView) findViewById(R.id.txtInstitutionNm)).setText(GlobalVariable.parkingLotItem.institutionNm);
        ((TextView) findViewById(R.id.txtPhoneNumber)).setText(GlobalVariable.parkingLotItem.phoneNumber);
        ((TextView) findViewById(R.id.txtLatitude)).setText(GlobalVariable.parkingLotItem.latitude);
        ((TextView) findViewById(R.id.txtLongitude)).setText(GlobalVariable.parkingLotItem.longitude);
        ((TextView) findViewById(R.id.txtReferenceDate)).setText(GlobalVariable.parkingLotItem.referenceDate);
        ((TextView) findViewById(R.id.txtInsttCode)).setText(GlobalVariable.parkingLotItem.instt_code);
        ((TextView) findViewById(R.id.txtInsttNm)).setText(GlobalVariable.parkingLotItem.instt_nm);
    }
}