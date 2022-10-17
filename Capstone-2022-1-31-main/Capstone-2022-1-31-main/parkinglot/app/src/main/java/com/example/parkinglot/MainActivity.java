package com.example.parkinglot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.widget.Toast;

import com.example.parkinglot.util.Constants;
import com.example.parkinglot.util.DatabaseActivity;

public class MainActivity extends AppCompatActivity {

    private BackPressHandler backPressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(R.string.app_name);
        getTitleColor();

        findViewById(R.id.layNationalParkingLot).setOnClickListener(view -> {
            // 전국 주차장 현황
            Intent intent = new Intent(this, NationalParkingLotActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.layDatabase).setOnClickListener(view -> {
            // 학교 주차장 현황
            Intent intent = new Intent(this, DatabaseActivity.class);
            startActivity(intent);
        });

        this.backPressHandler = new BackPressHandler(this);
    }

    @Override
    public void onBackPressed() {
        this.backPressHandler.onBackPressed();
    }

    private class BackPressHandler {
        private final Context context;
        private Toast toast;

        private long backPressedTime = 0;
        public BackPressHandler(Context context) {
            this.context = context;
        }

        public void onBackPressed() {
            if(System.currentTimeMillis() > this.backPressedTime + (Constants.Load_Delay.LONG*2)) {
                this.backPressedTime = System.currentTimeMillis();

                this.toast = Toast.makeText(this.context, R.string.msg_back_press_end, Toast.LENGTH_SHORT);
                this.toast.show();
                return;
            }
            if (System.currentTimeMillis() <= this.backPressedTime + (Constants.Load_Delay.LONG*2)) {
                moveTaskToBack(true);
                finish();
                this.toast.cancel();
            }
        }
    }
}