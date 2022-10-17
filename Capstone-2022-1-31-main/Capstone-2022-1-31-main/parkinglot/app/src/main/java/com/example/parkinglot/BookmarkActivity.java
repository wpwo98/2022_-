package com.example.parkinglot;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkinglot.entity.ParkingLot;
import com.example.parkinglot.util.BookmarkAdapter;
import com.example.parkinglot.util.Constants;
import com.example.parkinglot.util.DBHelper;
import com.example.parkinglot.util.OnItemClickListener;

import java.util.ArrayList;
import java.util.Objects;

public class BookmarkActivity extends AppCompatActivity {
    //private static final String TAG = BookmarkActivity.class.getSimpleName();
    private static final String TAG = "ParkingLot";

    private RecyclerView recyclerView;
    private BookmarkAdapter adapter;

    private ArrayList<ParkingLot> items;

    private LinearLayout layNoData;         // 데이터 없을때 표시할 레이아웃
    private TextView txtCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        // 제목 표시
        setTitle(getString(R.string.title_bookmark));

        // 홈버튼(<-) 표시
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // 리사이클러뷰
        this.recyclerView = findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        this.layNoData = findViewById(R.id.layNoData);

        // 즐겨찾기보기
        listBookmark();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 즐겨찾기보기 */
    private void listBookmark() {
        // 내역 얻기
        this.items = getItems();

        this.adapter = new BookmarkAdapter(mItemClickListener, this.items);
        this.recyclerView.setAdapter(this.adapter);

        if (this.items.size() == 0) {
            // 즐겨찾기가 없으면
            this.layNoData.setVisibility(View.VISIBLE);
        }
    }

    /* 즐겨찾기 얻기 */
    private ArrayList<ParkingLot> getItems() {
        ArrayList<ParkingLot> bookmarks = new ArrayList<>();

        SQLiteDatabase db = DBHelper.getInstance(this).getReadableDatabase();

        // select 쿼리문 (주차장명순으로 정렬)
        String sql = "SELECT parkingLotNo, parkingLotName, latitude, longitude FROM " +
                Constants.DBTableName.BOOKMARK + " ORDER BY parkingLotName";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            // 즐겨찾기 데이터
            ParkingLot parkingLot = new ParkingLot(cursor.getString(0), cursor.getString(1),
                    cursor.getDouble(2), cursor.getDouble(3));
            bookmarks.add(parkingLot);
        }
        cursor.close();

        db.close();
        return bookmarks;
    }

    /* 즐겨찾기 삭제 */
    private void deleteBookmark(String parkingLotNo, int position) {
        SQLiteDatabase db = DBHelper.getInstance(this).getReadableDatabase();

        try {
            // 즐겨찾기 삭제
            String sql = "DELETE FROM " + Constants.DBTableName.BOOKMARK + " WHERE parkingLotNo = ?";
            Object[] args = { parkingLotNo };
            db.execSQL(sql, args);

            // 리스트에서 삭제
            this.adapter.remove(position);

            if (this.items.size() == 0) {
                this.layNoData.setVisibility(View.VISIBLE);
            }
        } catch (SQLException ignored) {
            // 오류
            Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    private final OnItemClickListener mItemClickListener = (view, position) -> {
        if (view.getId() == R.id.imgDelete) {
            // 삭제
            new AlertDialog.Builder(this)
                    .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                        // 삭제
                        deleteBookmark(items.get(position).parkingLotNo, position);
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .setCancelable(false)
                    .setTitle(R.string.dialog_title_delete)
                    .setMessage(R.string.dialog_msg_delete)
                    .show();
        } else {
            // 선택 (주차장)
            // Activity 에 전달
            Intent intent = new Intent();
            intent.putExtra("parking_no", this.items.get(position).parkingLotNo);
            intent.putExtra("parking_lot", this.items.get(position).parkingLotName);
            intent.putExtra("latitude", this.items.get(position).latitude);
            intent.putExtra("longitude", this.items.get(position).longitude);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
}