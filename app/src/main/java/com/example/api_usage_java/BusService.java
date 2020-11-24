package com.example.api_usage_java;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class BusService extends IntentService {

    public BusService(){
        super("BusService");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        System.out.println("get-Service");
        //노티피케이션 알람을 울려준다.
    }
}
