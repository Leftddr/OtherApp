package com.example.api_usage_java;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class BusActivity extends AppCompatActivity {
    final int REQUEST_CODE = 101;
    final String api_key = "hntByA0IwEpLfxOfkDA2f6YMTYUtzksFlmYcYgsh53Y";
    final String api_key_ar = "aZyc1Eibkz0Spmkj4oqrF%2Bd8k1FK0maWmCZn4bor%2FDTRyfHz3cPaQ1wfh8DBWx8GwBuC4d19onos3Gw6WozScA%3D%3D";
    double x = -1.0, y = -1.0;
    SwipeRefreshLayout swipe;
    LocationManager lm;
    ODsayService odsayService;
    ArrayList<String> stationName = new ArrayList<String>();
    ArrayList<String> stationId = new ArrayList<String>();
    ArrayList<String> busNum = new ArrayList<String>();
    ArrayList<String> arriveTime1 = new ArrayList<String>();
    ArrayList<Double> xs = new ArrayList<>(), ys = new ArrayList<>();
    ListView listView1;
    ListView listView2;
    arriveAdapter arriveadapter;
    stationAdapter stationadapter;
    Handler mHandler = new Handler();
    String cur_station_id = "";
    String cur_time = "";
    ProgressDialog customProgressDialog;


    class BtnOnClickListener implements Button.OnClickListener{
        @Override
        public void onClick(View v){
            System.out.println("click");
            Intent intent;
            switch(v.getId()){
                case R.id.set_alarm:
                    System.out.println("setting alarm");
                    Intent alarmBroadCast = new Intent(getApplicationContext(), BusReceiver.class);
                    alarmBroadCast.putExtra("kind", "bus");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmBroadCast, 0);
                    setAlarm(pendingIntent);
                    break;
                case R.id.to_home:
                    System.out.println("switch screen");
                    intent = new Intent(getApplicationContext(), MenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    break;
                case R.id.search:
                    if(x == -1 || y == -1){
                        Toast.makeText(getApplicationContext(), "현재 위치를 가져와 주세요!", Toast.LENGTH_LONG).show();
                        break;
                    }
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run(){
                            Object result = requestBusStation();
                            odsayService.requestPointSearch(String.valueOf(x), String.valueOf(y), String.valueOf(500), String.valueOf(1), (OnResultCallbackListener) result);
                        }
                    });
                    th.start();
                    break;
                case R.id.position:
                    customProgressDialog.show();
                    get_position();
                    break;
                case R.id.to_map:
                    intent = new Intent(getApplicationContext(), MapActivity.class);
                    if(x == -1 || y == -1){
                        Toast.makeText(getApplicationContext(), "현재 위치를 가져와 주세요", Toast.LENGTH_LONG).show();
                        break;
                    }
                    if(xs.size() == 0 || ys.size() == 0){
                        Toast.makeText(getApplicationContext(), "주변 버스 정류장을 가져와 주세요", Toast.LENGTH_LONG).show();
                        break;
                    }
                    intent.putExtra("init_x", x);
                    intent.putExtra("init_y", y);
                    intent.putExtra("xs", xs);
                    intent.putExtra("ys", ys);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
            }
        }
    }

    public void setAlarm(PendingIntent pendingIntent){
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        if(cur_time.equals("-1")){
            Toast.makeText(getApplicationContext(), "시간 정보가 없으므로 알람을 등록할 수 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }
        if(Integer.parseInt(cur_time) <= 1){
            Toast.makeText(getApplicationContext(), "곧 도착하므로 알람을 등록할 필요가 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }
        calendar.set(Calendar.MINUTE, min + Integer.parseInt(cur_time) - 1);
        System.out.println("-----------" + calendar.toString() + "---------------");

        //한번만 등록한다.
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(getApplicationContext(), "알람을 등록했습니다", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("ENTER RIGHT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if(resultCode == RESULT_OK){
            //요청이 맞게 돌아왔다.
            System.out.println("RESULTOK RIGHT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if(requestCode == REQUEST_CODE){
                System.out.println("REQUESTCODE RIGHT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                double xs_ = data.getDoubleExtra("choice_x", -1);
                double ys_ = data.getDoubleExtra("choice_y", -1);
                for(int i = 0 ; i < xs.size() ; i++){
                    if(xs.get(i) == xs_ && ys.get(i) == ys_){
                        cur_station_id = stationId.get(i);
                        break;
                    }
                }
                System.out.println("-----------------------------------+" + xs_ + "---------------------------+" + ys_);
                if(xs_ != -1 && ys_ != -1) {
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("REQUEST TIME RIGHT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            try {
                                requestTime();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        timeListInit();
                                    }
                                });
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    });
                    th.start();
                }
            }
        }
        else if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "취소 되었습니다.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);

        Button set_alarm = (Button)findViewById(R.id.set_alarm);
        Button to_home = (Button)findViewById(R.id.to_home);
        Button search = (Button)findViewById(R.id.search);
        Button position = (Button)findViewById(R.id.position);
        Button to_map = (Button)findViewById(R.id.to_map);
        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);

        BtnOnClickListener btnClick = new BtnOnClickListener();

        set_alarm.setOnClickListener(btnClick);
        to_home.setOnClickListener(btnClick);
        search.setOnClickListener(btnClick);
        position.setOnClickListener(btnClick);
        to_map.setOnClickListener(btnClick);
        customProgressDialog = new ProgressDialog(this);

        odsayService = ODsayService.init(this, api_key);
        odsayService.setReadTimeout(5000);
        odsayService.setConnectionTimeout(5000);

        init_swipe();
    }

    public void init_swipe(){
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            requestTime();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                timeListInit();
                                swipe.setRefreshing(false);
                            }
                        });
                    }
                });
               th.start();
            }
        });
    }

    public void stationListInit(){
        System.out.println("--------------------------------" + stationName.size() + "----------------------------");
        listView1 = (ListView)findViewById(R.id.station);
        stationadapter = new stationAdapter();
        for(int i = 0 ; i < stationName.size() ; i++) {
            stationadapter.addItem(new StationItem(stationName.get(i), stationId.get(i)));
            System.out.println(stationName.get(i));
        }
        listView1.setAdapter(stationadapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                StationItem item = (StationItem) stationadapter.getItem(position);
                TextView tmp = (TextView)findViewById(R.id.show_station);
                tmp.setText(item.getStation());
                cur_station_id = item.getStationid();

                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("-------------------------Request start--------------------------");
                            busNum.clear();
                            arriveTime1.clear();
                            requestTime();
                            mHandler.post(new Runnable(){
                                @Override
                                public void run() {
                                    timeListInit();
                                }
                            });
                        } catch (IOException e){
                            e.printStackTrace();
                            System.out.println("time 요청 에러 발생");
                        }
                    }
                });
                th.start();
            }
        });
    }

    public void timeListInit(){
        System.out.println("--------------------------------" + busNum.size() + "----------------------------");
        listView2 = (ListView)findViewById(R.id.arrivetime);
        arriveadapter = new arriveAdapter();
        for(int i = 0 ; i < busNum.size() ; i++) {
            arriveadapter.addItem(new ArriveItem(busNum.get(i), arriveTime1.get(i)));
            System.out.println(busNum.get(i));
        }
        listView2.setAdapter(arriveadapter);
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                ArriveItem item = (ArriveItem) arriveadapter.getItem(position);
                TextView tmp = (TextView)findViewById(R.id.show_arrivetime);
                tmp.setText(item.getNumber());
                cur_time = item.getArrive1();
                if(cur_time.contains("분")){
                    String []split_ = cur_time.split("분");
                    cur_time = split_[0];
                }
                else if(cur_time.contains("곧 도착")){
                    cur_time = "1";
                }
                else{
                    cur_time = "-1";
                }
            }
        });
    }

    public void requestTime() throws IOException{
        StringBuilder urlBuilder = new StringBuilder("http://ws.bus.go.kr/api/rest/stationinfo/getLowStationByUid"); /*URL*/
        urlBuilder.append("?ServiceKey=" + api_key_ar); /*Service Key*/
        urlBuilder.append("&arsId=" + cur_station_id); /**/
        System.out.println(urlBuilder.toString());
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        System.out.println(sb.toString());
        rd.close();
        conn.disconnect();
        try{
            DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
            Document doc = dBuilder.parse(urlBuilder.toString());
            doc.getDocumentElement().normalize();
            NodeList num = doc.getElementsByTagName("rtNm");
            NodeList arr1 = doc.getElementsByTagName("arrmsg1");
            NodeList arr2 = doc.getElementsByTagName("arrmsg2");
            busNum.clear();
            arriveTime1.clear();
            for(int i = 0 ; i < num.getLength() ; i++){
                Node tmp1 = num.item(i);
                Node tmp2 = arr1.item(i);
                Node tmp3 = arr2.item(i);

                Element el1 = (Element)tmp1;
                Element el2 = (Element)tmp2;
                Element el3 = (Element)tmp3;

                busNum.add(el1.getTextContent());
                //우선 처음 도착 정보만 넣는다.
                arriveTime1.add(el2.getTextContent());

                busNum.add(el1.getTextContent());
                arriveTime1.add(el3.getTextContent());
            }

        } catch (ParserConfigurationException e){
            System.out.println("Dom Parsing error");
        } catch( SAXException e){
            System.out.println("Dom Parsing error");
        }
    }

    private Object requestBusStation(){
        OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
            @Override
            public void onSuccess(ODsayData oDsayData, API api) {
                try{
                    int count = Integer.parseInt(oDsayData.getJson().getJSONObject("result").getString("count"));
                    JSONArray arr = oDsayData.getJson().getJSONObject("result").getJSONArray("station");
                    System.out.println("---------------------------" + arr.toString());
                    stationName.clear();
                    stationId.clear();
                    xs.clear(); ys.clear();
                    for(int i = 0 ; i < arr.length() ; i++){
                        JSONObject tmp = arr.getJSONObject(i);
                        String stat_name = tmp.getString("stationName");
                        stationName.add(stat_name);
                        String arsId = tmp.getString("arsID");
                        String [] split_ = arsId.split("-");
                        stationId.add(split_[0] + split_[1]);
                        xs.add(tmp.getDouble("x"));
                        ys.add(tmp.getDouble("y"));
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            stationListInit();
                        }
                    });
                    System.out.println("----------------------" + stationName.toString());
                } catch (JSONException e){
                    Toast.makeText(getApplicationContext(), "에러가 발생했습니다. 2", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(int i, String s, API api) {
                Toast.makeText(getApplicationContext(), "에러가 발생했습니다. 1", Toast.LENGTH_LONG).show();
            }
        };
        return onResultCallbackListener;
    }

    private void get_position(){
        String locationContext = Context.LOCATION_SERVICE;
        lm = (LocationManager)getSystemService(locationContext);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location == null){
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 1, gpsLocationListener);
        }
        else {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);
        }
    }

    //////////////////////////////////////////////////////////////////////////////

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            x = longitude; y = latitude;
            System.out.println("x : " + x + " y : " + y);
            if(x != -1 && y != -1){
                TextView txt = (TextView)findViewById(R.id.show_pos);
                txt.setText("경도 : " + x + " 위도 : " + y);
            }
            lm.removeUpdates(this);
            customProgressDialog.dismiss();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    ///////////////////////////////////////////////////////////////////////

    class busStationView extends LinearLayout {
        TextView bus_station;

        public busStationView(Context context){
            super(context);
            init(context);
        }
        public busStationView(Context context, AttributeSet attrs){
            super(context, attrs);
            init(context);
        }

        private void init(Context context){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.bus_station_item, this, true);

            bus_station = (TextView)findViewById(R.id.bus_station);
        }

        void setBus_station(String cur_stat) {bus_station.setText(cur_stat);}
    }

    class StationItem{
        private String station;
        private String stationid;

        StationItem(String station, String stationid){
            this.station = station;
            this.stationid = stationid;
        }

        String getStation() {return this.station;}
        void setStation(String tmp) {this.station = tmp;}
        String getStationid() {return this.stationid;}
        void setStationid(String tmp){this.stationid = tmp;}
    }

    class stationAdapter extends BaseAdapter {
        private ArrayList<StationItem> items = new ArrayList<>();
        @Override
        public int getCount() {return items.size();}
        @Override
        public Object getItem(int position){return items.get(position);}
        @Override
        public long getItemId(int position){return position;}
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup){
            busStationView view = (busStationView)convertView;
            if(convertView == null){
                view = new busStationView(getApplicationContext());
            }
            StationItem item = items.get(position);
            view.setBus_station(item.getStation());
            return view;
        }

        void addItem(StationItem item) {items.add(item);}
        void cleanItem() {items.clear();}
    }

    ////////////////////////////////////////////////////////////////////////

    class busArriveView extends LinearLayout {
        TextView bus_arrive;
        TextView bus_number;

        public busArriveView(Context context){
            super(context);
            init(context);
        }
        public busArriveView(Context context, AttributeSet attrs){
            super(context, attrs);
            init(context);
        }

        private void init(Context context){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.bus_time_item, this, true);

            bus_arrive = (TextView)findViewById(R.id.bus_time);
            bus_number = (TextView)findViewById(R.id.bus_number);
        }

        void setBusArrive1(String cur_stat) {bus_arrive.setText(cur_stat);}
        void setBusNumber(String cur_stat) {bus_number.setText(cur_stat);}
    }

    class ArriveItem{
        private String arrive1;
        private String number;

        ArriveItem(String number, String arrive1){
            this.arrive1 = arrive1;
            this.number = number;
        }

        String getArrive1() {return this.arrive1;}
        void setArrive1(String tmp) {this.arrive1 = tmp;}
        String getNumber() {return this.number;}
        void setNumber(String tmp) {this.number = tmp;}
    }

    class arriveAdapter extends BaseAdapter {
        private ArrayList<ArriveItem> items = new ArrayList<>();
        @Override
        public int getCount() {return items.size();}
        @Override
        public Object getItem(int position){return items.get(position);}
        @Override
        public long getItemId(int position){return position;}
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup){
            busArriveView view = (busArriveView)convertView;
            if(convertView == null){
                view = new busArriveView(getApplicationContext());
            }
            ArriveItem item = items.get(position);
            view.setBusArrive1(item.getArrive1());
            view.setBusNumber(item.getNumber());
            return view;
        }

        void addItem(ArriveItem item) {items.add(item);}
        void cleanItem() {items.clear();}
    }

}
