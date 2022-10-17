package com.example.parkinglot;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkinglot.entity.History;
import com.example.parkinglot.util.Constants;
import com.example.parkinglot.util.DBHelper;
import com.example.parkinglot.util.HistoryAdapter;
import com.example.parkinglot.util.OnItemClickListener;

import java.util.ArrayList;
import java.util.Objects;

public class HistoryActivity extends AppCompatActivity {
    //private static final String TAG = HistoryActivity.class.getSimpleName();
    private static final String TAG = "ParkingLot";

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;

    private ArrayList<History> items;

    private LinearLayout layNoData;         // 데이터 없을때 표시할 레이아웃
    private TextView txtCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 제목 표시
        setTitle(getString(R.string.title_history));

        // 홈버튼(<-) 표시
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // 리사이클러뷰
        this.recyclerView = findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        this.layNoData = findViewById(R.id.layNoData);

        // 내역보기
        listHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 내역보기 */
    private void listHistory() {
        // 내역 얻기
        this.items = getItems();

        this.adapter = new HistoryAdapter(mItemClickListener, this.items);
        this.recyclerView.setAdapter(this.adapter);

        if (this.items.size() == 0) {
            // 내역이 없으면
            this.layNoData.setVisibility(View.VISIBLE);
        }
    }

    /* 내역 얻기 */
    private ArrayList<History> getItems() {
        ArrayList<History> historys = new ArrayList<>();

        SQLiteDatabase db = DBHelper.getInstance(this).getReadableDatabase();

        // select 쿼리문 (최근순으로 정렬)
        String sql = "SELECT _id, parkingLotNo, parkingLotName, latitude, longitude, inputTimeMillis FROM " +
                Constants.DBTableName.HISTORY + " ORDER BY inputTimeMillis DESC";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            // 내역 데이터
            History history = new History(cursor.getLong(0), cursor.getString(1), cursor.getString(2),
                    cursor.getDouble(3), cursor.getDouble(4), cursor.getLong(5));
            historys.add(history);
        }
        cursor.close();

        db.close();
        return historys;
    }

    private final OnItemClickListener mItemClickListener = (view, position) -> {
        // 선택 (주차장)
        // Activity 에 전달
        Intent intent = new Intent();
        intent.putExtra("parking_no", this.items.get(position).parkingLotNo);
        intent.putExtra("parking_lot", this.items.get(position).parkingLotName);
        intent.putExtra("latitude", this.items.get(position).latitude);
        intent.putExtra("longitude", this.items.get(position).longitude);
        setResult(Activity.RESULT_OK, intent);
        finish();
    };
}