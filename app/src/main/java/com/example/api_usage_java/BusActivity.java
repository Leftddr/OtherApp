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

public class BusActivity extends AppCompatActivity {
    final int REQUEST_CODE = 101;
    final String api_key = "hntByA0IwEpLfxOfkDA2f6YMTYUtzksFlmYcYgsh53Y";
    final String api_key_ar = "aZyc1Eibkz0Spmkj4oqrF%2Bd8k1FK0maWmCZn4bor%2FDTRyfHz3cPaQ1wfh8DBWx8GwBuC4d19onos3Gw6WozScA%3D%3D";
    double x = -1.0, y = -1.0;
    LocationManager lm;
    ODsayService odsayService;
    ArrayList<String> stationName = new ArrayList<String>();
    ArrayList<String> stationId = new ArrayList<String>();
    ArrayList<String> busNum = new ArrayList<String>();
    ArrayList<String> arriveTime = new ArrayList<String>();
    ListView listView1;
    ListView listView2;
    arriveAdapter arriveadapter;
    stationAdapter stationadapter;
    Handler mHandler = new Handler();
    String cur_station_id = "";
    String cur_time = "";


    class BtnOnClickListener implements Button.OnClickListener{
        @Override
        public void onClick(View v){
            System.out.println("click");
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
                    startActivityForResult(new Intent(getApplicationContext(), MenuActivity.class), REQUEST_CODE);
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
                            odsayService.requestPointSearch(String.valueOf(x), String.valueOf(y), String.valueOf(300), String.valueOf(1), (OnResultCallbackListener) result);
                        }
                    });
                    th.start();
                    break;
                case R.id.position:
                    get_position();
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
        calendar.set(Calendar.MINUTE, min + 1);
        System.out.println("-----------" + calendar.toString() + "---------------");

        //한번만 등록한다.
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);
        Button set_alarm = (Button)findViewById(R.id.set_alarm);
        Button to_home = (Button)findViewById(R.id.to_home);
        Button search = (Button)findViewById(R.id.search);
        Button position = (Button)findViewById(R.id.position);

        BtnOnClickListener btnClick = new BtnOnClickListener();

        set_alarm.setOnClickListener(btnClick);
        to_home.setOnClickListener(btnClick);
        search.setOnClickListener(btnClick);
        position.setOnClickListener(btnClick);

        odsayService = ODsayService.init(this, api_key);
        odsayService.setReadTimeout(5000);
        odsayService.setConnectionTimeout(5000);
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
                            requestTime();
                            mHandler.post(new Runnable(){
                                @Override
                                public void run() {
                                    timeListInit();
                                }
                            });
                        } catch (IOException e){
                            System.out.println("-------------------------" + stationId.toString());
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
            arriveadapter.addItem(new ArriveItem(busNum.get(i), arriveTime.get(i)));
            System.out.println(busNum.get(i));
        }
        listView2.setAdapter(arriveadapter);
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                ArriveItem item = (ArriveItem) stationadapter.getItem(position);
                TextView tmp = (TextView)findViewById(R.id.show_arrivetime);
                tmp.setText(item.getNumber());
                cur_time = item.getArrive();
            }
        });
    }

    public void requestTime() throws IOException{
        StringBuilder urlBuilder = new StringBuilder("http://ws.bus.go.kr/api/rest/stationinfo/getLowStationByUid"); /*URL*/
        urlBuilder.append("?ServiceKey=" + api_key_ar); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode(cur_station_id,"UTF-8")); /**/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
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
        rd.close();
        conn.disconnect();
        try{
            DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
            Document doc = dBuilder.parse(urlBuilder.toString());
            doc.getDocumentElement().normalize();
            NodeList num1 = doc.getElementsByTagName("plainNo1");
            NodeList num2 = doc.getElementsByTagName("plainNo2");
            NodeList arr1 = doc.getElementsByTagName("arrmsg1");
            NodeList arr2 = doc.getElementsByTagName("arrmsg2");

            Node tmp = num1.item(0);
            Element element = (Element)tmp;
            busNum.add(tmp.getTextContent());

            tmp = num2.item(0);
            element = (Element)tmp;
            busNum.add(tmp.getTextContent());

            tmp = arr1.item(0);
            element = (Element)tmp;
            arriveTime.add(tmp.getTextContent());

            tmp = arr2.item(0);
            element = (Element)tmp;
            arriveTime.add(tmp.getTextContent());

        } catch (ParserConfigurationException e){
            System.out.println("Dom Parsing error");
        } catch( SAXException e){
            System.out.println("Dom Parsing error");
        }
        System.out.println(sb.toString());
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
                    for(int i = 0 ; i < arr.length() ; i++){
                        JSONObject tmp = arr.getJSONObject(i);
                        String stat_name = tmp.getString("stationName");
                        stationName.add(stat_name);
                        String arsId = tmp.getString("arsID");
                        String [] split_ = arsId.split("-");
                        stationId.add(split_[0] + split_[1]);
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

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location == null){
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 1, gpsLocationListener);
        }
        else {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
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

        void setBusArrive(String cur_stat) {bus_arrive.setText(cur_stat);}
        void setBusNumber(String cur_stat) {bus_number.setText(cur_stat);}
    }

    class ArriveItem{
        private String arrive;
        private String number;

        ArriveItem(String arrive, String number){
            this.arrive = arrive;
            this.number = number;
        }

        String getArrive() {return this.arrive;}
        void setArrive(String tmp) {this.arrive = tmp;}
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
            view.setBusArrive(item.getArrive());
            view.setBusNumber(item.getNumber());
            return view;
        }

        void addItem(ArriveItem item) {items.add(item);}
        void cleanItem() {items.clear();}
    }

}
