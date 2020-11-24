package com.example.api_usage_java;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MenuActivity extends AppCompatActivity {
    final int REQUEST_CODE = 101;

    class BtnOnClickListener implements Button.OnClickListener{
        @Override
        public void onClick(View v){
            System.out.println("click");
            switch(v.getId()){
                case R.id.to_translate:
                    System.out.println("switch screen");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.id.to_location:
                    System.out.println("switch screen");
                    String []permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                    Permission(permissions);

            }
        }
    }

    public boolean hasPermissions(Context context, String[] permissions){
        if(context != null && permissions != null){
            for (String permission : permissions){
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    public void Permission(String[] permissions){
        boolean permissionCheck = hasPermissions(this, permissions);
        if(!permissionCheck){
            Toast.makeText(this, "위치 권한 승인이 필요합니다.", Toast.LENGTH_LONG).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "위치를 확인하기 위해서는 권한이 필요합니다. up", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            }
            else{
                Toast.makeText(this, "위치를 확인하기 위해서는 권한이 필요합니다. down", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permission[], int[] grantResults){
        switch(requestCode){
            case REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "승인이 허가되었습니다.", Toast.LENGTH_LONG).show();
                } else{

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("어플 권한");
                    alertDialog.setMessage("해당 앱의 원활한 기능을 이용하시려면 정보 > 권한에서 위치 권한을 허용해 주십시오");
                    alertDialog.setPositiveButton("권한 설정",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                    dialogInterface.cancel();
                                }
                            });
                    alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    alertDialog.show();
                }
        }
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        BtnOnClickListener btnClick = new BtnOnClickListener();

        Button to_translate = (Button)findViewById(R.id.to_translate);
        to_translate.setOnClickListener(btnClick);
        Button to_location = (Button)findViewById(R.id.to_location);
        to_location.setOnClickListener(btnClick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            Toast.makeText(getApplicationContext(), "처음 화면", Toast.LENGTH_SHORT).show();
        }
    }

}
