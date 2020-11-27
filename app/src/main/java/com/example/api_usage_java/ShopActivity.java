package com.example.api_usage_java;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.security.Provider;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ShopActivity  extends AppCompatActivity {
    EditText edit;
    ListView listview;
    TextView txtPage;
    LinearLayout ll;
    ShopAdapter shopAdapter;
    String search_word;
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> lprices = new ArrayList<String>();
    ArrayList<String> mallnames = new ArrayList<String>();
    ArrayList<String> links = new ArrayList<String>();
    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    Handler mHandler = new Handler();
    boolean to_refresh = false;
    static int page = 1;
    float x = -1, y = -1;
    int beforeSelected = -1;
    Calendar beforeTime;
    final String init_edit_value = "쇼핑할 품목을 입력하세요";

    SoftKeyboard mSoftKeyboard;
    InputMethodManager im;

    ProgressDialog customProgressDialog;

    class BtnOnClickListener implements Button.OnClickListener{
        @Override
        public void onClick(View v){
            Intent intent;
            switch(v.getId()){
                case R.id.search:
                    customProgressDialog.show();
                    Thread api_thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            titles.clear(); lprices.clear(); mallnames.clear(); links.clear(); bitmaps.clear();
                            if(edit.getText().toString().equals("")){
                                Toast.makeText(getApplicationContext(), "검색어를 입력해주세요", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Object object = get_shopping_information(edit.getText().toString());
                            try {
                                parsing_json(new JSONObject(object.toString()));
                            } catch (JSONException e){
                                e.printStackTrace();
                                return;
                            }
                        }
                    });
                    api_thread.start();
                    break;
                case R.id.to_home:
                    intent = new Intent(getApplicationContext(), MenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        Button search = (Button)findViewById(R.id.search);
        Button to_home = (Button)findViewById(R.id.to_home);
        txtPage = (TextView)findViewById(R.id.page);

        BtnOnClickListener btnClick = new BtnOnClickListener();
        search.setOnClickListener(btnClick);
        to_home.setOnClickListener(btnClick);
        listview = (ListView)findViewById(R.id.listview);
        edit = (EditText)findViewById(R.id.edit);
        ll = (LinearLayout)findViewById(R.id.parent);
        im = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        mSoftKeyboard = new SoftKeyboard(ll, im);
        customProgressDialog = new ProgressDialog(this);

        keyboard_event();
        init_touch();

    }

    public void keyboard_event(){
        mSoftKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        to_refresh = true;
                    }
                });
            }

            @Override
            public void onSoftKeyboardShow() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        to_refresh = true;
                    }
                });
            }
        });
    }

    public void init_touch(){
        listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(to_refresh){edit.clearFocus(); refresh_listview(); to_refresh = false;}
                if(edit.getText().toString().equals("")){
                    edit.setText(init_edit_value);
                }
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x = motionEvent.getX();
                        y = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float up_x = motionEvent.getX();
                        float up_y = motionEvent.getY();
                        if(x - up_x > 200){
                            if(page + 1 > 10) break;
                            System.out.println("swipe 동작 인식!!" + page);
                            page++;
                            customProgressDialog.show();
                            Thread th = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    titles.clear(); lprices.clear(); mallnames.clear(); links.clear(); bitmaps.clear();
                                    if(edit.getText().toString().equals("")){
                                        Toast.makeText(getApplicationContext(), "검색어를 입력해주세요", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    Object object = get_shopping_information(edit.getText().toString());
                                    try {
                                        parsing_json(new JSONObject(object.toString()));
                                    } catch (JSONException e){
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            });
                            th.start();
                        }
                        else if(up_x - x > 200){
                            if(page - 1 <= 0) break;
                            System.out.println("swipe 동작 인식!!" + page);
                            page--;
                            customProgressDialog.show();
                            Thread th = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    titles.clear(); lprices.clear(); mallnames.clear(); links.clear(); bitmaps.clear();
                                    if(edit.getText().toString().equals("")){
                                        Toast.makeText(getApplicationContext(), "검색어를 입력해주세요", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    Object object = get_shopping_information(edit.getText().toString());
                                    try {
                                        parsing_json(new JSONObject(object.toString()));
                                    } catch (JSONException e){
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            });
                            th.start();
                        }
                        break;
                }
                return false;
            }
        });

        edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(edit.getText().toString().equals(init_edit_value)){
                    edit.setText("");
                }
                to_refresh = true;
                return false;
            }
        });
    }

    private void refresh_listview(){
        shopAdapter = new ShopAdapter();
        System.out.println(titles.size() + " " + bitmaps.size() + " " + lprices.size() + " " + mallnames.size() + " " + links.size());
        for(int i = 0 ; i < titles.size() ; i++)
            shopAdapter.addItem(new ShopItem(titles.get(i), bitmaps.get(i), lprices.get(i), mallnames.get(i), links.get(i)));
        listview.setAdapter(shopAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                ShopItem item = (ShopItem)shopAdapter.getItem(position);
                Calendar time = Calendar.getInstance();
                if(beforeSelected == -1){
                    beforeTime = Calendar.getInstance();
                    beforeSelected = position;
                    return;
                }
                else if(beforeSelected == position && (time.getTimeInMillis() - beforeTime.getTimeInMillis() < 1000)){
                    makeAlert(item.getLink());
                    beforeSelected = -1;
                }
                else if(beforeSelected != position){beforeSelected = position;}
            }
        });
    }

    public void makeAlert(String uri){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("쇼핑 웹페이지 이동");
        alertDialog.setMessage("해당 품목의 사이트로 이동하시겠습니까?");
        alertDialog.setPositiveButton("이동",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        dialogInterface.cancel();
                    }
                });
        alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                beforeSelected = -1;
                dialogInterface.cancel();
            }
        });
        alertDialog.show();
    }

    class ShopItem{
        private String title;
        private Bitmap img;
        private String lprice;
        private String mallname;
        private String link;

        ShopItem(String title, Bitmap img, String lprice, String mallname, String link){
            this.title = title; this.img = img; this.lprice = lprice; this.mallname = mallname; this.link = link;
        }

        void setTitle(String title){this.title = title;}
        String getTitle(){return this.title;}
        void setImg(Bitmap img){this.img = img;}
        Bitmap getImg(){return this.img;}
        void setLprice(String lprice){this.lprice = lprice;}
        String getLprice(){return this.lprice;}
        void setMallname(String mallname){this.mallname = mallname;}
        String getMallname(){return this.mallname;}
        void setLink(String link){this.link = link;}
        String getLink(){return this.link;}
    }

    class ShopItemView extends LinearLayout {
        TextView title;
        ImageView img;
        TextView lprice;
        TextView mallname;

        public ShopItemView(Context context) {
            super(context);
            init(context);
        }

        public ShopItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        private void init(Context context){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.shop_item, this, true);

            title = (TextView)findViewById(R.id.title);
            img = (ImageView) findViewById(R.id.img);
            lprice = (TextView)findViewById(R.id.lprice);
            mallname = (TextView)findViewById(R.id.mallname);
        }

        void setTitleView(String title){this.title.setText("상품 명 : " + title);}
        void setLpriceView(String lprice){this.lprice.setText("최저 가격 : " + lprice);}
        void setMallnameView(String mallname){this.mallname.setText("판매처 : " + mallname);}
        void setImgView(Bitmap img){this.img.setImageBitmap(img);}
    }

    class ShopAdapter extends BaseAdapter{
        private ArrayList<ShopItem> items = new ArrayList<>();
        @Override
        public int getCount() {return items.size();}
        @Override
        public Object getItem(int position){return items.get(position);}
        @Override
        public long getItemId(int position){return position;}
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup){
            if(convertView != null) return convertView;
            ShopItemView view = (ShopItemView) convertView;
            if(convertView == null){
                view = new ShopItemView(getApplicationContext());
            }
            ShopItem item = items.get(position);
            view.setTitleView(item.getTitle());
            view.setLpriceView(item.getLprice());
            view.setMallnameView(item.getMallname());
            view.setImgView(item.getImg());
            return view;
        }

        void addItem(ShopItem item) {items.add(item);}
        void cleanItem() {items.clear();}
    }

    private void parsing_json(JSONObject object){
        System.out.println(object.toString());
        try {
            JSONArray arr = object.getJSONArray("items");
            for(int i = 0 ; i < arr.length() ; i++){
                JSONObject tmp = arr.getJSONObject(i);
                titles.add(tmp.getString("title"));
                links.add(tmp.getString("link"));
                lprices.add(tmp.getString("lprice"));
                mallnames.add(tmp.getString("mallName"));
                final String image_path = tmp.getString("image");
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            URL url = new URL(image_path);
                            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                            conn.setDoInput(true);
                            conn.connect();
                            InputStream is = conn.getInputStream();
                            Bitmap tmp = BitmapFactory.decodeStream(is);
                            if(tmp == null) {
                                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                                bitmaps.add(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.noimage));
                            }
                            else {
                                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                                bitmaps.add(tmp);
                            }
                        } catch (MalformedURLException e){
                            e.printStackTrace();
                            bitmaps.add(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.noimage));
                            return;
                        } catch(IOException e){
                            e.printStackTrace();
                            bitmaps.add(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.noimage));
                            return;
                        }
                    }
                });
                th.start();
                try{
                    th.join();
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                refresh_listview();
                txtPage.setText(String.valueOf(page) + " Page");
                customProgressDialog.dismiss();
                beforeSelected = -1;
            }
        });
    }

    private static Object get_shopping_information(String editText){
        String clientId = "0gtTaRyZDxxtlsxPvhn2";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "UuljAklNy_";//애플리케이션 클라이언트 시크릿값";

        String apiURL = "https://openapi.naver.com/v1/search/shop.json";

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        Object responseBody = post(apiURL, requestHeaders, editText);
        return responseBody;
    }

    private static Object post(String apiUrl, Map<String, String> requestHeaders, String text){
        String postParams;
        try {
            postParams = "?query=" + URLEncoder.encode(text, "UTF-8") + "&display=10&start=" + String.valueOf(page) + "&sort=sim";
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return null;
        }
        HttpURLConnection con = connect(apiUrl + postParams);
        System.out.println(apiUrl + postParams);
        try{
            //post방식으로 지정한다.
            con.setRequestMethod("GET");
            //map을 for문 돌릴 때 이렇게 사용한다.
            for(Map.Entry<String, String> header : requestHeaders.entrySet()){
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                return readBody(con.getInputStream());
            } else {
                return readBody(con.getErrorStream());
            }
        } catch (IOException e){
            e.printStackTrace();
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
