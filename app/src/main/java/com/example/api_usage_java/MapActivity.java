package com.example.api_usage_java;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    LocationManager lm;
    double x, y;
    ProgressDialog customProgressDialog;
    NaverMap global_naverMap;
    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    Handler mHandler = new Handler();
    ArrayList<Double> xs, ys;
    List<Marker> markers;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        x = intent.getDoubleExtra("init_x", -1);
        y = intent.getDoubleExtra("init_y", -1);
        xs = (ArrayList<Double>)intent.getSerializableExtra("xs");
        ys = (ArrayList<Double>)intent.getSerializableExtra("ys");

        System.out.println("-----------------------------" + xs.size() + "-------------------------------------");
        System.out.println("----------------------------" + x + "--------------------------" + y);

        NaverMapOptions options = new NaverMapOptions()
                .camera(new CameraPosition(new LatLng(y, x), 15));
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if(mapFragment == null){
            mapFragment = MapFragment.newInstance(options);
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap){
        //현재 위치가 있는지 확인.
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true);
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, true);
        global_naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        //현재 위치를 지도에 표시해준다.
        Marker tmp = new Marker();
        tmp.setPosition(new LatLng(y, x));
        tmp.setCaptionText("현재 위치");
        tmp.setMap(naverMap);

        //여기서 받아온 주변 정류장의 마커들을 표시해준다.
        markers = new ArrayList<>();
        for(int i = 0 ; i < xs.size() ; i++){
            Marker marker = new Marker();
            final int index = i;
            marker.setOnClickListener(new Overlay.OnClickListener() {
              @Override
              public boolean onClick(@NonNull Overlay overlay) {
                  Intent intent = new Intent(getApplicationContext(), BusActivity.class);
                  intent.putExtra("choice_x", xs.get(index));
                  intent.putExtra("choice_y", ys.get(index));
                  setResult(RESULT_OK, intent);
                  finish();
                  return true;
              }
          });
            marker.setPosition(new LatLng(ys.get(i), xs.get(i)));
            marker.setWidth(50);
            marker.setHeight(80);
            markers.add(marker);
        }

        for(Marker marker : markers){
            marker.setMap(naverMap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            System.out.println("권한 승인됨!!");
            if (!locationSource.isActivated()) { // 권한 거부
                global_naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }
}
