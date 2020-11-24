package com.example.api_usage_java;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Handler mHandler = new Handler();
    ArrayList<String> arrayList;
    ArrayList<String> targetList;
    ArrayAdapter<String> arrayAdapter;
    static String target = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button)findViewById(R.id.click);
        TextView text = (TextView)findViewById(R.id.translate);
        EditText edit = (EditText)findViewById(R.id.edit);
        Button move_btn = (Button)findViewById(R.id.move);

        arrayList = new ArrayList<String>();
        targetList = new ArrayList<String>();
        arrayList.add("한국어");
        targetList.add("ko");
        arrayList.add("영어");
        targetList.add("en");
        arrayList.add("중국어-간체");
        targetList.add("zh-CN");
        arrayList.add("중국어-번체");
        targetList.add("zh-TW");
        arrayList.add("스페인어");
        targetList.add("es");
        arrayList.add("프랑스어");
        targetList.add("fr");
        arrayList.add("베트남어");
        targetList.add("vi");
        arrayList.add("태국어");
        targetList.add("th");
        arrayList.add("인도네시아어");
        targetList.add("id");

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayList);
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                target = targetList.get(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView){}
        });

        /*
        move_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int parentWidth = ((ViewGroup)view.getParent()).getWidth();
                int parentHeight = ((ViewGroup)view.getParent()).getHeight();

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    float oldXvalue = motionEvent.getX();
                    float oldYvalue = motionEvent.getY();
                    Log.d("viewTEST", "oldXvalue : " + oldXvalue + " oldYvalue : " + oldYvalue);
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
                    view.setX(view.getX() - (motionEvent.getX()) - (view.getWidth() / 2));
                    view.setY(view.getY() - (motionEvent.getY()) - (view.getHeight() / 2));
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    // 뷰에서 손을 뗌

                    if(view.getX() < 0){
                        view.setX(0);
                    }else if((view.getX() + view.getWidth()) > parentWidth){
                        view.setX(parentWidth - view.getWidth());
                    }

                    if(view.getY() < 0){
                        view.setY(0);
                    }else if((view.getY() + view.getHeight()) > parentHeight) {
                        view.setY(parentHeight - view.getHeight());
                    }
                }
                return true;
            }

            //onTouchEvent나 그런것은 view를 상속할때 사용하는 것이다.
        });
        */


        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String editText = edit.getText().toString();
                if(editText == ""){
                    text.setText("you must input sentence!");
                    return;
                }
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject tmp;
                        String src = "";
                        try{
                            tmp = new JSONObject(detect_lang(editText).toString());
                            src = tmp.getString("langCode");
                        } catch(JSONException e){
                            text.setText("detect_lang error");
                            return;
                        }
                        System.out.println("src : " + src);
                        Object result = translate(editText, src);
                        try {
                            final JSONObject object = new JSONObject(result.toString());
                            mHandler.post(new Runnable(){
                                @Override
                                public void run(){
                                   try {
                                        text.setText(object.getJSONObject("message").getJSONObject("result").getString("translatedText"));
                                    }

                                    catch(JSONException e){
                                        text.setText("translate error");
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            ;
                        }
                    }
                });
                th.start();
            }
        });

        move_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("click");
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private static Object detect_lang(String editText){
        String clientId = "XtaunbyuK4kKKVK7iHOv";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "HexFLNsvfe";//애플리케이션 클라이언트 시크릿값";

        String apiURL = "https://openapi.naver.com/v1/papago/detectLangs";
        String text;
        try{
            text = URLEncoder.encode(editText, "UTF-8");
        } catch (UnsupportedEncodingException e){
            throw new RuntimeException("인코딩 실패", e);
        }

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);
        Object responseBody = post(apiURL, requestHeaders, text, 2, "");
        return responseBody;
    }

    private static Object translate(String editText, String src){
        String clientId = "XtaunbyuK4kKKVK7iHOv";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "HexFLNsvfe";//애플리케이션 클라이언트 시크릿값";

        String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
        String text;
        try {
            text = URLEncoder.encode(editText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("인코딩 실패", e);
        }

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        Object responseBody = post(apiURL, requestHeaders, text, 1, src);
        return responseBody;
    }

    private static Object post(String apiUrl, Map<String, String> requestHeaders, String text, int kind, String src){
        HttpURLConnection con = connect(apiUrl);
        String postParams;
        //kind 변수는 이것이 언어감지인지 번역인지를 검사하기 위해 필요한 변수
        //src는 원형이 무엇인지에 대해 검사한다.
        if(kind == 1) {
            //src는 원 언어가 어느나라 것인지를 판별하기 위해 존재하는 변수
            if(src.equals("ko"))
                postParams = "source=ko&target=" + target + "&text=" + text;
            else {
                postParams = "source=" + src + "&target=" + target + "&text=" + text;
            }
        }
        else
            postParams = "query=" + text;
        try{
            //post방식으로 지정한다.
            con.setRequestMethod("POST");
            //map을 for문 돌릴 때 이렇게 사용한다.
            for(Map.Entry<String, String> header : requestHeaders.entrySet()){
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            con.setDoOutput(true);
            //위에까지 uri와 header를 설정해주고 output stream을 만들어 놓는다.
            try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())){
                //여기서 파라미터를 보내준다.
                wr.write(postParams.getBytes());
                wr.flush();
            }
            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                return readBody(con.getInputStream());
            } else {
                return readBody(con.getErrorStream());
            }
        } catch (IOException e){
            throw new RuntimeException("API 요청응답 실패");
        } finally {
            con.disconnect();
        }
    }

    //이게 우선 uri를 바탕으로 연결문을 여는 코드이다.
    private static HttpURLConnection connect(String apiUrl){
        try{
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch(MalformedURLException e){
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch(IOException e){
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static Object readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);
        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }
}