package com.example.parkinglot.util;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkinglot.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DatabaseActivity extends AppCompatActivity {

    private static String IP_ADDRESS = "192.168.77.198";
    private static String TAG = "phptest";

    private EditText mEditTextName;
    private EditText mEditTextCountry;
    private TextView mTextViewResult;
    private ArrayList<PersonalData> mArrayList;
    private UsersAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private EditText mEditTextSearchKeyword;
    private String mJsonString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        mTextViewResult = (TextView)findViewById(R.id.textView_main_result);
//        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.listView_main_list);
        mRecyclerView = (RecyclerView) findViewById(R.id.listView_main_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());



        mArrayList = new ArrayList<>();

        mAdapter = new UsersAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);


        Button button_all = (Button) findViewById(R.id.button_main_all);
        button_all.setOnClickListener(v -> {

            mArrayList.clear();
            mAdapter.notifyDataSetChanged();

            GetData task = new GetData();
            task.execute( "http://" + IP_ADDRESS + "/getjson.php", "");
        });

    }



    private class GetData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(DatabaseActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){

                mTextViewResult.setText(errorString);
            }
            else {

                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }


    private void showResult(){

        String TAG_JSON="skwoo";
        String PARK_NUM = "park_num";
        String PARK_TEMP = "park_temp";
        String PARK_LIGHT ="park_light";
        String PARK_LINE = "park_line";
        String PARK_1ST = "park_1st";
        String PARK_2ND ="park_2nd";
        String PARK_3RD = "park_3rd";
        String PARK_4TH ="park_4th";




        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String park_num = item.getString(PARK_NUM);
                //String park_temp = item.getString(PARK_TEMP);
                //String park_light = item.getString(PARK_LIGHT);///////////////
                String park_line = item.getString(PARK_LINE);
                String park_1st = item.getString(PARK_1ST);
                String park_2nd = item.getString(PARK_2ND);
                String park_3rd = item.getString(PARK_3RD);
                String park_4th = item.getString(PARK_4TH);


                PersonalData personalData = new PersonalData();

                personalData.setPark_num(park_num);
                //personalData.setPark_temp(park_temp);
                //personalData.setPark_light(park_light);///////////////////
                personalData.setPark_line(park_line);
                personalData.setPark_1st(park_1st);
                personalData.setPark_2nd(park_2nd);
                personalData.setPark_3rd(park_3rd);
                personalData.setPark_4th(park_4th);

                mArrayList.add(personalData);
                mAdapter.notifyDataSetChanged();
            }



        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

}