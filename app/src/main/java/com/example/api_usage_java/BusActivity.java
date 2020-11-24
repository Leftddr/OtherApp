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
import android.view.View;
import android.widget.Button;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class BusActivity extends AppCompatActivity {
    final int REQUEST_CODE = 101;

    class BtnOnClickListener implements Button.OnClickListener{
        @Override
        public void onClick(View v){
            System.out.println("click");
            switch(v.getId()){
                case R.id.set_alarm:
                    System.out.println("setting alarm");
                    Intent alarmBroadCast = new Intent(getApplicationContext(), BusReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmBroadCast, 0);
                    setAlarm(pendingIntent);
                    break;
                case R.id.to_home:
                    System.out.println("switch screen");
                    //startActivityForResult(new Intent(getApplicationContext(), MenuActivity.class), REQUEST_CODE);
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
        System.out.println("-----------------" + day + "-------------------" + hour + "----------------" + min);
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

        BtnOnClickListener btnClick = new BtnOnClickListener();

        set_alarm.setOnClickListener(btnClick);
        to_home.setOnClickListener(btnClick);
        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.GPS_PROVIDER;
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = lm.getLastKnownLocation(locationProvider);
            if(location == null){
                locationProvider = LocationManager.NETWORK_PROVIDER;
                location = lm.getLastKnownLocation(locationProvider);
            }
            if(location != null) {
                String provider = location.getProvider();
                double longitute = location.getAltitude();
                System.out.println(longitute);
            }
            else{
                System.out.println("null");
            }
        }
    }
}
