package com.example.parkinglot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkinglot.entity.ParkingLotData;
import com.example.parkinglot.entity.ParkingLotItem;
import com.example.parkinglot.entity.ParkingLotSingleData;
import com.example.parkinglot.entity.Point;
import com.example.parkinglot.entity.SingleCheckData;
import com.example.parkinglot.util.Constants;
import com.example.parkinglot.util.GlobalVariable;
import com.example.parkinglot.util.OnItemClickListener;
import com.example.parkinglot.util.ParkingLotAdapter;
import com.example.parkinglot.util.Utils;
import com.google.gson.Gson;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPolyLine;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NationalParkingLotSearchActivity extends AppCompatActivity {
    private static final String TAG = NationalParkingLotSearchActivity.class.getSimpleName();

    // 로딩 레이아웃, 데이터 없을때 표시할 레이아웃
    private LinearLayout layLoading, layNoData;

    private RecyclerView recyclerView;
    private ParkingLotAdapter adapter;
    private ArrayList<ParkingLotItem> items;

    private Spinner spSection, spType, spChargeInfo;
    private EditText editKeyword;

    private InputMethodManager imm;                 // 키보드를 숨기기 위해 필요함

    private double latitude, longitude;             // 나의 위도 / 경도

    private int page;                               // 페이지
    private int dataCount;                          // 데이터 수

    private static final String API_URL = "http://api.data.go.kr/openapi/tn_pubr_prkplce_info_api"; // 주차장 api url
    private static final int ITEM_PAGE_SIZE =  500;             // 페이지당 아이템 수 (open api 호출시 요청 아이템 수)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_national_parking_lot_search);

        // 나의 위도 / 경도 정보
        Intent intent = getIntent();
        this.latitude = intent.getDoubleExtra("latitude", 0);
        this.longitude = intent.getDoubleExtra("longitude", 0);

        // 제목 표시
        setTitle(getString(R.string.title_parking_lot_search));

        // 홈버튼(<-) 표시
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // 로딩 레이아웃
        this.layLoading = findViewById(R.id.layLoading);
        ((ProgressBar) findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        // 데이터 없을때 표시할 레이아웃
        this.layNoData = findViewById(R.id.layNoData);

        this.recyclerView = findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        this.editKeyword = findViewById(R.id.editKeyword);
        this.editKeyword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        this.editKeyword.setHint("주차장명 또는 지역");

        this.spSection = findViewById(R.id.spSection);
        this.spType = findViewById(R.id.spType);
        this.spChargeInfo = findViewById(R.id.spChargeInfo);

        // 주차장구분 spinner 구성
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, R.layout.spinner_item,
                getResources().getStringArray(R.array.parking_lot_section));
        adapter1.setDropDownViewResource(R.layout.spinner_dropdown_item);
        this.spSection.setAdapter(adapter1);

        // 주차장유형 spinner 구성
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.spinner_item,
                getResources().getStringArray(R.array.parking_lot_type));
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_item);
        this.spType.setAdapter(adapter2);

        // 주차장구분 spinner 구성
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(this, R.layout.spinner_item,
                getResources().getStringArray(R.array.parking_charge_info));
        adapter3.setDropDownViewResource(R.layout.spinner_dropdown_item);
        this.spChargeInfo.setAdapter(adapter3);

        findViewById(R.id.btnSearch).setOnClickListener(view -> {
            // 주차장 입력 체크
            String keyword = this.editKeyword.getText().toString();
            if (TextUtils.isEmpty(keyword)) {
                Toast.makeText(this, R.string.msg_parking_lot_check_empty, Toast.LENGTH_SHORT).show();
                this.editKeyword.requestFocus();
                return;
            }

            // 주차장 자리수 체크
            if (keyword.length() < 2) {
                Toast.makeText(this, R.string.msg_parking_lot_check_empty, Toast.LENGTH_SHORT).show();
                this.editKeyword.requestFocus();
                return;
            }

            // 키보드 숨기기
            this.imm.hideSoftInputFromWindow(this.editKeyword.getWindowToken(), 0);

            this.layNoData.setVisibility(View.GONE);

            // 로딩 레이아웃 보임
            this.layLoading.setVisibility(View.VISIBLE);

            // 로딩레이아웃  표시하기 위함
            new Handler(Looper.getMainLooper()).post(() -> {
                // ArrayList 초기화
                this.items = new ArrayList<>();

                boolean download = false;
                if (GlobalVariable.parkingLotItems != null) {
                    if (GlobalVariable.parkingLotItems.size() > 0) {
                        download = true;
                    }
                }

                if (download) {
                    // 전국 주차장 정보를 다운받았으면
                    searchParkingLot();
                } else {
                    GlobalVariable.parkingLotItems = new ArrayList<>();

                    this.page = 1;
                    this.dataCount = 0;

                    // 오픈 api 호출
                    callOpenApi();
                }
            });
        });

        findViewById(R.id.layLoading).setOnClickListener(view -> {
            // 클릭 방지
        });

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        this.editKeyword.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 주차장 및 지역으로 찾기 */
    private void searchParkingLot() {
        Log.d(TAG, "download total:" +  GlobalVariable.parkingLotItems.size());

        String keyword = this.editKeyword.getText().toString();
        // 검색어가 있으면
        if (!TextUtils.isEmpty(keyword)) {
            for (ParkingLotItem item : GlobalVariable.parkingLotItems) {
                boolean exist = true;

                // 주차장구분
                if (this.spSection.getSelectedItemPosition() > 0) {
                    exist = item.prkplceSe.equals(this.spSection.getSelectedItem().toString());
                }

                if (exist) {
                    // 주차장유형
                    if (this.spType.getSelectedItemPosition() > 0) {
                        exist = item.prkplceType.equals(this.spType.getSelectedItem().toString());
                    }
                }

                if (exist) {
                    // 요금정보
                    if (this.spChargeInfo.getSelectedItemPosition() > 0) {
                        exist = item.parkingchrgeInfo.equals(this.spChargeInfo.getSelectedItem().toString());
                    }
                }

                if (exist) {
                    exist = false;

                    // 주차장 이름에 검색어가 포함 되어 있는지 체크
                    if (item.prkplceNm.contains(keyword)) {
                        exist = true;
                    } else {
                        // 도로명 주소가 있으면
                        if (!TextUtils.isEmpty(item.rdnmadr)) {
                            // 지역이 포함 되어 있는지 체크
                            if (item.rdnmadr.contains(keyword)) {
                                exist = true;
                            } else {
                                // 지번 주소가 있으면
                                if (!TextUtils.isEmpty(item.lnmadr)) {
                                    // 지역이 포함 되어 있는지 체크
                                    if (item.lnmadr.contains(keyword)) {
                                        exist = true;
                                    }
                                }
                            }
                        }
                    }
                }

                // 결과 item 넣기
                if (exist) {
                    // 나와의 거리 계산
                    if (Utils.isNumeric(item.latitude) && Utils.isNumeric(item.longitude)) {
                        item.distance = Utils.getDistance(latitude, longitude,
                                Double.parseDouble(item.latitude), Double.parseDouble(item.longitude));
                    } else {
                        // 거리 계산 안됨
                        item.distance = Constants.NO_DISTANCE;
                    }

                    items.add(item);
                }
            }
        }

        complete();
    }

    /* 데이터 정렬을 위한 Comparator (거리 ASC) */
    private Comparator<ParkingLotItem> getComparator() {
        Comparator<ParkingLotItem> comparator = (sort1, sort2) -> {
            // 정렬
            return Double.compare(sort1.distance, sort2.distance);
        };

        return comparator;
    }

    /* 검색 완료 */
    private void complete() {
        // 로딩 레이아웃 숨김
        this.layLoading.setVisibility(View.GONE);

        if (this.items.size() == 0) {
            this.layNoData.setVisibility(View.VISIBLE);
        } else {
            // 거리순으로 정렬
            Collections.sort(this.items, getComparator());
        }

        // 어뎁터 적용
        this.adapter = new ParkingLotAdapter(mItemClickListener, this.items, this.latitude, this.longitude);
        this.recyclerView.setAdapter(this.adapter);
    }

    /* Open api 호출 (주차장) */
    private void callOpenApi() {
        try {
            // 오픈 api 호출
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            String url = API_URL;
            url += "?serviceKey=" + getString(R.string.open_api_key);       // 인증키
            //url += "&prkplceNm=" + URLEncoder.encode(this.editKeyword.getText().toString(), "UTF-8");   // 주차장명
            //url += "&prkplceSe=" + URLEncoder.encode("공영", "UTF-8");   // 주차장구분
            //url += "&prkplceType=" + URLEncoder.encode("노외", "UTF-8");   // 주차장유형
            //url += "&prkplceSe=" + URLEncoder.encode("무료", "UTF-8");   // 요금정보
            // json 타입은 주차장 검색이 안됨, (json 타입은 구조가 약간 다름 item 이 항상 배열임)
            //url += "&type=" + "json";                                     // xml / json
            url += "&pageNo=" + this.page;                                  // 페이지
            url += "&numOfRows=" + ITEM_PAGE_SIZE;                          // 요청 데이터 수

            Log.d(TAG, "url:" + url);

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            okHttpClient.newCall(request).enqueue(mCallback);
        } catch (Exception e) {
            // Error
            Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
        }
    }

    /* 주차장 조회 결과값 Callback */
    private final Callback mCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "콜백오류:" + e.getMessage());

            new Handler(Looper.getMainLooper()).post(() -> {
                // 로딩 레이아웃 숨김
                layLoading.setVisibility(View.GONE);
                Toast.makeText(NationalParkingLotSearchActivity.this, R.string.msg_error, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String responseData = response.body().string();

            if (!TextUtils.isEmpty(responseData)) {
                //Log.d(TAG, "서버에서 응답한 데이터:" + responseData);

                new Handler(Looper.getMainLooper()).post(() -> {
                    // XML 을 JSON 으로 변환
                    XmlToJson xmlToJson = new XmlToJson.Builder(responseData).build();
                    String json = xmlToJson.toString();
                    Log.d(TAG, "json:" + json);

                    if (json.contains("{\"response\":{\"header\":{\"code\":")) {
                        // 오류
                        complete();
                        return;
                    }

                    // JSON to Object
                    Gson gson = new Gson();

                    // 성공여부 체크 (xml 일경우 데이터가 1개 일때하고 2개이상일때하고 구조가 다르기 때문에 수량 체크를 먼저 함)
                    SingleCheckData checkData = gson.fromJson(json, SingleCheckData.class);

                    if (checkData.response.header.resultCode.equals("00")) {
                        // 성공

                        // 페이지 번호
                        int pageNo = Integer.parseInt(checkData.response.body.pageNo);
                        // 페이지당 데이터 수
                        int numOfRows = Integer.parseInt(checkData.response.body.numOfRows);
                        // 총 검색수
                        int totalCount = Integer.parseInt(checkData.response.body.totalCount);

                        Log.d(TAG, "totalCount:" + totalCount);

                        if (totalCount > 0) {
                            if ((totalCount - ((pageNo - 1) * numOfRows)) == 1) {
                                // 단일 데이터
                                ParkingLotSingleData singleData = gson.fromJson(json, ParkingLotSingleData.class);

                                // 전체 주차장 array 추가
                                GlobalVariable.parkingLotItems.add(singleData.response.body.items.item);

                                // 현재까지 받은 데이터수
                                dataCount ++;
                            } else {

                                // 다중 데이터
                                // json 으로 데이터를 받으면 항상 배열로 넘어옴
                                ParkingLotData data = gson.fromJson(json, ParkingLotData.class);

                                // 전체 주차장 array 추가
                                GlobalVariable.parkingLotItems.addAll(data.response.body.items.item);

                                // 현재까지 받은 데이터수
                                dataCount += data.response.body.items.item.size();
                            }

                            if (totalCount > dataCount) {
                                // 결과 아이템이 더 존재함
                                page++;

                                // 오픈 api 호출 (다음 페이지)
                                callOpenApi();
                            } else {
                                // 주차장 및 지역으로 찾기
                                searchParkingLot();
                            }
                        } else {
                            // 주차장 및 지역으로 찾기
                            searchParkingLot();
                        }
                    } else {
                        // 실패
                        complete();
                        Toast.makeText(NationalParkingLotSearchActivity.this, checkData.response.header.resultMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    private final OnItemClickListener mItemClickListener = (view, position) -> {
        // 선택
        GlobalVariable.parkingLotItem = this.items.get(position);

        // 주차장 정보 Activity
        Intent intent = new Intent(this, ParkingLotInfoActivity.class);
        intent.putExtra("position", position);
        this.parkingLotActivityLauncher.launch(intent);
    };

    /* 주차장선택 ActivityForResult */
    private final ActivityResultLauncher<Intent> parkingLotActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // 길찾기
                    Intent data = result.getData();
                    if (data != null) {
                        int position = data.getIntExtra("position", -1);
                        if (position == -1) {
                            return;
                        }

                        double lat, lng;
                        ParkingLotItem item = this.items.get(position);
                        if (Utils.isNumeric(item.latitude) && Utils.isNumeric(item.longitude)) {
                            lat = Double.parseDouble(item.latitude);
                            lng = Double.parseDouble(item.longitude);
                        } else {
                            // 주소로 위도 / 경도 가져오기
                            Point point = null;
                            if (!TextUtils.isEmpty(item.rdnmadr)) {
                                point = Utils.getGpsFromAddress(this, item.rdnmadr);
                            } else {
                                if (!TextUtils.isEmpty(item.lnmadr)) {
                                    point = Utils.getGpsFromAddress(this, item.lnmadr);
                                }
                            }

                            if (point != null) {
                                lat = point.latitude;
                                lng = point.longitude;
                            } else {
                                // 주소정보가 없음
                                Toast.makeText(this, R.string.msg_parking_lot_location_empty, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        // Activity 에 전달
                        Intent intent = new Intent();
                        intent.putExtra("parking_no", item.prkplceNo);
                        intent.putExtra("parking_lot", item.prkplceNm);
                        intent.putExtra("latitude", lat);
                        intent.putExtra("longitude", lng);
                        intent.putExtra("save", true);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
            });
}