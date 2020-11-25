package com.example.api_usage_java;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class BusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        String kind = intent.getExtras().getString("kind");
        setNotification(context, kind);
    }

    //노티피케이션 알람을 설정하는 함수
    public void setNotification(Context context, String kind){
        //노티피케이션 알람 매니저를 생성
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        //노티피케이션 건축가를 생성
        NotificationCompat.Builder builder = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //여기서는 채널 id와 이름을 넣어주어야 한다.
            String channelID = "busChannel";
            String channelName = "myBusChannel";
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(context, channelID);
        } else {
            //여기서는 null을 넣으면 알아서 잡아주나 보다
            builder = new NotificationCompat.Builder(context, null);
        }

        if(kind.equals("bus")) {
            builder.setSmallIcon(android.R.drawable.ic_menu_view);
            builder.setContentTitle("버스 도착 알림");
            builder.setContentText("버스가 몇 분 안에 도착합니다.");

            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.bus);
            builder.setLargeIcon(bm);
        }
        else{
            builder.setSmallIcon(android.R.drawable.ic_menu_view);
            builder.setContentTitle("지하철 도착 알림");
            builder.setContentText("지하철이 몇 분 안에 도착합니다.");

            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.subway);
            builder.setLargeIcon(bm);
        }

        //누르면 다시 이쪽으로 넘어오도록 인텐트를 생성
        //만들때 context를 넘겨줬기 문에 context로 구분이 가능한 듯 보인다.
        Intent intent;
        if(kind.equals("bus"))
            intent = new Intent(context, BusActivity.class);
        else
            intent = new Intent(context, SubwayActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        builder.setAutoCancel(true);
        builder.setVibrate(new long[]{0, 2000, 1000, 3000});

        Notification notification = builder.build();

        notificationManager.notify(1, notification);
    }
}
