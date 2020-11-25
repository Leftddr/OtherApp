package com.example.api_usage_java;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.jar.Attributes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class SubwayActivity extends AppCompatActivity {
    ListView listview;
    SubwayAdapter adapter;
    boolean to_init = false;
    EditText edit;

    class BtnOnClickListener implements Button.OnClickListener{
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.search:
                    break;
                case R.id.to_home:
                    startActivity(new Intent(getApplicationContext(), MenuActivity.class));
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
        search.setOnClickListener(btnClick);
        to_home.setOnClickListener(btnClick);
        alarm.setOnClickListener(btnClick);

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
        calendar.set(Calendar.SECOND, sec + 10);
        System.out.println("-----------" + calendar.toString() + "---------------");

        //한번만 등록한다.
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
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
        for(int i = 0 ; i < 10 ; i++)
            adapter.addItem(new SubwayItem("hi", "hello", "this", "is", "hi"));
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                SubwayItem item = (SubwayItem)adapter.getItem(position);
                Toast.makeText(getApplicationContext(), item.getArvlMsg2(), Toast.LENGTH_LONG).show();
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

        void setStatnNm(String cur_stat) {statnNm.setText(cur_stat);}
        void setBstatnNm(String end_stat) {bstatnNm.setText(end_stat);}
        void setBarvlDt(String arrive_time) {barvlDt.setText(arrive_time);}
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
