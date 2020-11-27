package com.example.api_usage_java;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.jar.Attributes;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class SubwayActivity extends AppCompatActivity {
    ListView listview;
    SubwayAdapter adapter;
    TextView time;
    LinearLayout ll;
    SwipeRefreshLayout swipe;
    boolean to_init = false;
    EditText edit;
    String cur_station = "";
    ArrayList<String> statNm = new ArrayList<String>();
    ArrayList<String> bstatNm = new ArrayList<String>();
    ArrayList<String> barvlDt = new ArrayList<String>();
    ArrayList<String> arvlMsg2 = new ArrayList<String>();
    ArrayList<String> arvlMsg3 = new ArrayList<String>();
    String wait_time = "";
    Handler mHandler = new Handler();
    SoftKeyboard mSoftKeyboard;
    InputMethodManager im;

    class BtnOnClickListener implements Button.OnClickListener{
        @Override
        public void onClick(View v){
            Intent intent;
            switch(v.getId()){
                case R.id.search:
                    cur_station = edit.getText().toString();
                    if(cur_station.equals("")) {
                        Toast.makeText(getApplicationContext(), "값을 입력해 주세요", Toast.LENGTH_LONG).show();
                        break;
                    }
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            requestTime();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    init_list();
                                }
                            });
                        }
                    });
                    th.start();
                    break;
                case R.id.to_home:
                    intent = new Intent(getApplicationContext(), MenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    break;
                case R.id.alarm:
                    Intent alarmBroadCast = new Intent(getApplicationContext(), BusReceiver.class);
                    alarmBroadCast.putExtra("kind", "subway");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmBroadCast, 0);
                    setAlarm(pendingIntent);
                    break;
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subway);

        init_list();
        init_touch();
        BtnOnClickListener btnClick = new BtnOnClickListener();
        Button search = (Button)findViewById(R.id.search);
        Button to_home = (Button)findViewById(R.id.to_home);
        Button alarm = (Button)findViewById(R.id.alarm);
        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);
        edit = (EditText)findViewById(R.id.edit);
        time = (TextView)findViewById(R.id.time);
        ll = (LinearLayout)findViewById(R.id.parent);
        search.setOnClickListener(btnClick);
        to_home.setOnClickListener(btnClick);
        alarm.setOnClickListener(btnClick);
        im = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        mSoftKeyboard = new SoftKeyboard(ll, im);
        keyboard_event();
        init_swipe();
    }

    public void init_swipe(){
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        requestTime();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                init_list();
                                swipe.setRefreshing(false);
                            }
                        });
                    }
                });
                th.start();
            }
        });
    }

    public void keyboard_event(){
        mSoftKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        to_init = true;
                    }
                });
            }

            @Override
            public void onSoftKeyboardShow() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        to_init = true;
                    }
                });
            }
        });
    }

    public void requestTime(){
        StringBuilder urlBuilder = new StringBuilder("http://swopenapi.seoul.go.kr/api/subway/484456485771686431303644416c4769/xml/realtimeStationArrival/0/5/"); /*URL*/
        try {
            urlBuilder.append(URLEncoder.encode(cur_station, "UTF-8")); /*Service Key*/
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        try {
            DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
            Document doc = dBuilder.parse(urlBuilder.toString());
            doc.getDocumentElement().normalize();

            NodeList statNms = doc.getElementsByTagName("statnNm");
            NodeList bstatNms = doc.getElementsByTagName("bstatnNm");
            NodeList barvlDts = doc.getElementsByTagName("barvlDt");
            NodeList arvlMsgs2 = doc.getElementsByTagName("arvlMsg2");
            NodeList arvlMsgs3 = doc.getElementsByTagName("arvlMsg3");

            statNm.clear(); bstatNm.clear(); barvlDt.clear(); arvlMsg2.clear(); arvlMsg3.clear();
            for(int i = 0 ; i < statNms.getLength() ; i++){
                Node tmp1 = statNms.item(i);
                Node tmp2 = bstatNms.item(i);
                Node tmp3 = barvlDts.item(i);
                Node tmp4 = arvlMsgs2.item(i);
                Node tmp5 = arvlMsgs3.item(i);

                Element el1 = (Element)tmp1;
                Element el2 = (Element)tmp2;
                Element el3 = (Element)tmp3;
                Element el4 = (Element)tmp4;
                Element el5 = (Element)tmp5;

                statNm.add(el1.getTextContent());
                bstatNm.add(el2.getTextContent());
                barvlDt.add(el3.getTextContent());
                arvlMsg2.add(el4.getTextContent());
                arvlMsg3.add(el5.getTextContent());
            }
            if(statNm.size() <= 0){
                statNm.add("역을 찾을 수 없습니다.");
                bstatNm.add("역을 찾을 수 없습니다.");
                barvlDt.add("-1");
                arvlMsg2.add("역을 찾을 수 없습니다.");
                arvlMsg3.add("역을 찾을 수 없습니다.");
            }
        } catch (ParserConfigurationException e){
            System.out.println("Dom Parsing error");
            e.printStackTrace();
        } catch(SAXException e){
            System.out.println("Dom Parsing error");
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void setAlarm(PendingIntent pendingIntent){
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        try{
            if(Integer.parseInt(wait_time) <= 0 || Integer.parseInt(wait_time) - 60 <= 1){
                Toast.makeText(getApplicationContext(), "곧 도착하거나 시간 정보가 없어 알람을 등록할 수 없습니다.", Toast.LENGTH_LONG).show();
                return;
            }
        }catch(NumberFormatException e){
            Toast.makeText(getApplicationContext(), "시간 정보가 없어 알람을 등록할 수 없습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
        calendar.set(Calendar.SECOND, sec + Integer.parseInt(wait_time) - 60);

        //한번만 등록한다.
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(getApplicationContext(), "알람을 성공적으로 등록했습니다.", Toast.LENGTH_LONG).show();
    }

    public void init_touch(){
        listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                edit.clearFocus();
                if(to_init) {init_list(); to_init = false;}
                return false;
            }
        });

        edit = (EditText)findViewById(R.id.edit);
        edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                to_init = true;
                return false;
            }
        });
    }

    public void init_list(){
        listview = (ListView)findViewById(R.id.sublist);
        adapter = new SubwayAdapter();
        for(int i = 0 ; i < statNm.size() ; i++)
            adapter.addItem(new SubwayItem(statNm.get(i), bstatNm.get(i), barvlDt.get(i), arvlMsg2.get(i), arvlMsg3.get(i)));
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                SubwayItem item = (SubwayItem)adapter.getItem(position);
                Toast.makeText(getApplicationContext(), item.getArvlMsg2(), Toast.LENGTH_LONG).show();
                wait_time = item.getBarvlDt();
                time.setText(wait_time);
            }
        });
    }


    class SubwayItemView extends LinearLayout{
        TextView statnNm;
        TextView bstatnNm;
        TextView barvlDt;
        TextView arvlMsg2;
        TextView arvlMsg3;

        public SubwayItemView(Context context){
            super(context);
            init(context);
        }
        public SubwayItemView(Context context, AttributeSet attrs){
            super(context, attrs);
            init(context);
        }

        private void init(Context context){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.subway_item, this, true);

            statnNm = (TextView)findViewById(R.id.statnNm);
            bstatnNm = (TextView)findViewById(R.id.bstatnNm);
            barvlDt = (TextView)findViewById(R.id.barvlDt);
            arvlMsg2 = (TextView)findViewById(R.id.arvlMsg2);
            arvlMsg3 = (TextView)findViewById(R.id.arvlMsg3);
        }

        void setStatnNm(String cur_stat) {statnNm.setText("현재역 : " + cur_stat);}
        void setBstatnNm(String end_stat) {bstatnNm.setText("종착역 : " + end_stat);}
        void setBarvlDt(String arrive_time) {barvlDt.setText("도착 시간 : " + arrive_time + "sec");}
        void setArvlMsg2(String arrive1) {arvlMsg2.setText(arrive1);}
        void setArvlMsg3(String arrive2) {arvlMsg3.setText(arrive2);}
    }

    class SubwayItem{
        private String statnNm;
        private String bstatnNm;
        private String barvlDt;
        private String arvlMsg2;
        private String arvlMsg3;

        SubwayItem(String statnNm, String bstatnNm, String barvlDt, String arvlMsg2, String arvlMsg3){
            this.statnNm = statnNm; this.bstatnNm = bstatnNm; this.barvlDt = barvlDt; this.arvlMsg2 = arvlMsg2; this.arvlMsg3 = arvlMsg3;
        }

        String getStatnNm() {return this.statnNm;}
        void setStatnNm(String tmp) {this.statnNm = tmp;}
        String getBstatnNm() {return this.bstatnNm;}
        void setBstatnNm(String tmp) {this.bstatnNm = tmp;}
        String getBarvlDt() {return this.barvlDt;}
        void setBarvlDt(String tmp) {this.barvlDt = tmp;}
        String getArvlMsg2() {return this.arvlMsg2;}
        void setArvlMsg2(String tmp) {this.arvlMsg2 = tmp;}
        String getArvlMsg3() {return this.arvlMsg3;}
        void setArvlMsg3(String tmp) {this.arvlMsg3 = tmp;}
    }

    class SubwayAdapter extends BaseAdapter{
        private ArrayList<SubwayItem> items = new ArrayList<>();
        @Override
        public int getCount() {return items.size();}
        @Override
        public Object getItem(int position){return items.get(position);}
        @Override
        public long getItemId(int position){return position;}
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup){
            if(convertView != null) return convertView;
            SubwayItemView view = (SubwayItemView)convertView;
            if(convertView == null){
                view = new SubwayItemView(getApplicationContext());
            }
            SubwayItem item = items.get(position);
            view.setStatnNm(item.getStatnNm());
            view.setBstatnNm(item.getBstatnNm());
            view.setBarvlDt(item.getBarvlDt());
            view.setArvlMsg2(item.getArvlMsg2());
            view.setArvlMsg3(item.getArvlMsg3());
            return view;
        }

        void addItem(SubwayItem item) {items.add(item);}
        void cleanItem() {items.clear();}
    }
}
