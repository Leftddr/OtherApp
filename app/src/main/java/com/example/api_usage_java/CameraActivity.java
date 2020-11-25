package com.example.api_usage_java;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CameraActivity extends AppCompatActivity {
    Button start, to_home, translate;
    ImageView img;
    TextView text;
    final private static String TAG = "bong";
    final static int TAKE_PICTURE = 1;
    final static String api_key = "KakaoAK 57d0ca75ae350a50f04484e796be1a51";
    final static String ocr_url = "https://dapi.kakao.com/v2/vision/text/ocr";
    static String file_name = "";
    Bitmap storeBitmap = null;
    ArrayList<String> words = new ArrayList<>();
    Handler mHandler = new Handler();

    String mCurrentPhotoPath = null;
    final static int REQUEST_TAKE_PHOTO = 1;

    class BtnOnClickListener implements Button.OnClickListener{
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.start:
                    dispatchTakePictureIntent();
                    break;
                case R.id.translate:
                    if(text.getText().toString().equals("")){Toast.makeText(getApplicationContext(), "parsing된 단어가 없습니다.", Toast.LENGTH_LONG).show(); break;}
                    String parsingText = text.getText().toString();
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject tmp;
                            String src = "";
                            try{
                                tmp = new JSONObject(detect_lang(parsingText).toString());
                                src = tmp.getString("langCode");
                            } catch(JSONException e){
                                text.setText("detect_lang error");
                                return;
                            }
                            System.out.println("src : " + src);
                            Object result = translate(parsingText, src);
                            try {
                                final JSONObject object = new JSONObject(result.toString());
                                mHandler.post(new Runnable(){
                                    @Override
                                    public void run(){
                                        try {
                                            text.setText(object.getJSONObject("message").getJSONObject("result").getString("translatedText"));
                                        }

                                        catch(JSONException e){
                                            text.setText("translate error");
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                ;
                            }
                        }
                    });
                    th.start();
                    break;
                case R.id.to_home:
                    startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        BtnOnClickListener btnClick = new BtnOnClickListener();

        start = (Button)findViewById(R.id.start);
        to_home = (Button)findViewById(R.id.to_home);
        start.setOnClickListener(btnClick);
        to_home.setOnClickListener(btnClick);
        translate = (Button)findViewById(R.id.translate);
        translate.setOnClickListener(btnClick);

        img = (ImageView)findViewById(R.id.img);
        text = (TextView)findViewById(R.id.text);
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        file_name = imageFileName;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try{photoFile = createImageFile();}
            catch(IOException e){e.printStackTrace();}
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.api_usage_java.fileprovider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        try{
            switch(requestCode){
                case REQUEST_TAKE_PHOTO:
                    if(resultCode == RESULT_OK){
                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap;
                        if(Build.VERSION.SDK_INT >= 29){
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));
                            try{
                                bitmap = ImageDecoder.decodeBitmap(source);
                                if(bitmap != null){
                                    img.setImageBitmap(bitmap); storeBitmap = bitmap;
                                    Bitmap smallBitmap = resizeBitmap(storeBitmap);
                                    SaveBitmapToFileCache(smallBitmap, mCurrentPhotoPath);
                                    requestPost(ocr_url, new File(mCurrentPhotoPath));
                                }
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                        } else{
                            try{
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                                if(bitmap != null){
                                    img.setImageBitmap(bitmap); storeBitmap = bitmap;
                                    Bitmap smallBitmap = resizeBitmap(storeBitmap);
                                    SaveBitmapToFileCache(smallBitmap, mCurrentPhotoPath);
                                    requestPost(ocr_url, new File(mCurrentPhotoPath));
                                }
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void requestPost(String url, File _file) {

        MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/jpeg");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", _file.getName(), RequestBody.create(MEDIA_TYPE_IMAGE, _file))
                .build();


        //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", api_key)
                .addHeader("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                try {
                    String to_text = "";
                    JSONObject object = new JSONObject(responseBody);
                    JSONArray arr = object.getJSONArray("result");
                    for(int i = 0 ; i < arr.length() ; i++){
                        JSONObject tmp = arr.getJSONObject(i);
                        words.add(tmp.getString("recognition_words"));
                        String to_clear = tmp.getString("recognition_words");
                        String to_clear2 = "";
                        for(int j = 2 ; j < to_clear.length() - 2 ; j++){
                            to_clear2 += to_clear.charAt(j);
                        }
                        to_text += to_clear2 + " ";
                    }
                    text.setText(to_text);
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private Bitmap resizeBitmap(Bitmap bitmap){
        Bitmap b = Bitmap.createScaledBitmap(bitmap, 1000, 1000, false);
        return b;
    }

    private void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {
        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;
        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String byteArrayToBinaryString(byte[] b){
        StringBuilder sb=new StringBuilder();
        for(int i=0; i<b.length; ++i){
            sb.append(byteToBinaryString(b[i]));
        }
        return sb.toString();
    }

    public static String byteToBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }

    private static Object detect_lang(String editText){
        String clientId = "XtaunbyuK4kKKVK7iHOv";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "HexFLNsvfe";//애플리케이션 클라이언트 시크릿값";

        String apiURL = "https://openapi.naver.com/v1/papago/detectLangs";
        String text;
        try{
            text = URLEncoder.encode(editText, "UTF-8");
        } catch (UnsupportedEncodingException e){
            throw new RuntimeException("인코딩 실패", e);
        }

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);
        Object responseBody = post(apiURL, requestHeaders, text, 2, "");
        return responseBody;
    }

    private static Object translate(String editText, String src){
        String clientId = "XtaunbyuK4kKKVK7iHOv";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "HexFLNsvfe";//애플리케이션 클라이언트 시크릿값";

        String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
        String text;
        try {
            text = URLEncoder.encode(editText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("인코딩 실패", e);
        }

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        Object responseBody = post(apiURL, requestHeaders, text, 1, src);
        return responseBody;
    }

    private static Object post(String apiUrl, Map<String, String> requestHeaders, String text, int kind, String src){
        HttpURLConnection con = connect(apiUrl);
        String postParams;
        //kind 변수는 이것이 언어감지인지 번역인지를 검사하기 위해 필요한 변수
        //src는 원형이 무엇인지에 대해 검사한다.
        if(kind == 1) {
            //src는 원 언어가 어느나라 것인지를 판별하기 위해 존재하는 변수
            if(src.equals("ko"))
                postParams = "source=ko&target=" + "en" + "&text=" + text;
            else {
                postParams = "source=" + src + "&target=" + "ko" + "&text=" + text;
            }
        }
        else
            postParams = "query=" + text;
        try{
            //post방식으로 지정한다.
            con.setRequestMethod("POST");
            //map을 for문 돌릴 때 이렇게 사용한다.
            for(Map.Entry<String, String> header : requestHeaders.entrySet()){
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            con.setDoOutput(true);
            //위에까지 uri와 header를 설정해주고 output stream을 만들어 놓는다.
            try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())){
                //여기서 파라미터를 보내준다.
                wr.write(postParams.getBytes());
                wr.flush();
            }
            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                return readBody(con.getInputStream());
            } else {
                return readBody(con.getErrorStream());
            }
        } catch (IOException e){
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
