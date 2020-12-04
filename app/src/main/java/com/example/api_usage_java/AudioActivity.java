package com.example.api_usage_java;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechGovRecognizer;
import com.naver.speech.clientapi.SpeechRecognitionException;
import com.naver.speech.clientapi.SpeechRecognitionListener;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.naver.speech.clientapi.SpeechRecognizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.widget.Toast;

import java.util.logging.LogRecord;

import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

public class AudioActivity extends Activity {
    private static final String TAG = AudioActivity.class.getSimpleName();
    private static final String CLIENT_ID = "n3gce9qog0"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    private RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private TextView txtResult;
    private Button btnStart;
    private String mResult;
    private AudioWriterPCM writer;
    private ListView listView;
    private audioAdapter audioadapter;
    private ArrayList<String> audio_text = new ArrayList<>();
    private long first_click = -1, index = -1;
    // Handle speech recognition Messages.

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady: // 음성인식 준비 가능
                txtResult.setText("Connected");
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_AUDIOBOOKS);
                //try {
                    //File audio = File.createTempFile("test", "", storageDir);
                    writer = new AudioWriterPCM(storageDir.getAbsolutePath());
                    System.out.println("-----------------------------------------" + writer + "-------------------------------------");
                    writer.open("Test");
                //} catch (IOException e){
                //    e.printStackTrace();
                //}
                break;
            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                break;
            case R.id.partialResult:
                mResult = (String) (msg.obj);
                txtResult.setText(mResult);
                break;
            case R.id.finalResult: // 최종 인식 결과
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults();
                //StringBuilder strBuf = new StringBuilder();
                for(String result : results) {
                    //strBuf.append(result);
                    //strBuf.append("\n\n");
                    audio_text.add(result);
                }
                txtResult.setText("Completed!!");
                init_list();
                break;
            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }
                mResult = "Error code : " + msg.obj.toString();
                btnStart.setText(R.string.str_start);
                btnStart.setEnabled(true);
                break;
            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }
                btnStart.setText(R.string.str_start);
                btnStart.setEnabled(true);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        listView = (ListView)findViewById(R.id.listview);
        txtResult = (TextView)findViewById(R.id.txtResult);
        btnStart = (Button) findViewById(R.id.btn_start);
        //listview를 안보이게
        listView.setVisibility(View.GONE);
        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, CLIENT_ID);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!naverRecognizer.getSpeechRecognizer().isRunning()) {
                    listView.setVisibility(View.GONE);
                    txtResult.setVisibility(View.VISIBLE);
                    audio_text.clear();
                    mResult = "";
                    txtResult.setText("Connecting...");
                    btnStart.setText(R.string.str_stop);
                    naverRecognizer.recognize();
                } else {
                    listView.setVisibility(View.VISIBLE);
                    txtResult.setVisibility(View.GONE);
                    Log.d(TAG, "stop and wait Final Result");
                    btnStart.setEnabled(false);
                    naverRecognizer.getSpeechRecognizer().stop();
                }
            }
        });
    }

    protected void init_list(){
        audioadapter = new audioAdapter();
        for(int i = 0 ; i < audio_text.size() ; i++){
            audioadapter.addItem(new AudioItem(audio_text.get(i)));
        }
        listView.setAdapter(audioadapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AudioItem tmp = (AudioItem) audioadapter.getItem(position);
                if(index == -1 || first_click == -1){
                    first_click = System.currentTimeMillis();
                    index = position;
                }
                else if(index == position && System.currentTimeMillis() - first_click <= 1000) {
                    Toast.makeText(getApplicationContext(), "저장합니다.", Toast.LENGTH_LONG).show();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                        try {
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String nowTime = sdf.format(date);

                            File text_file = File.createTempFile("test", "_" + nowTime + ".txt", storageDir);
                            System.out.println("-----------------------" + text_file.getAbsolutePath() + "-----------------------------");
                            BufferedWriter buf = new BufferedWriter(new FileWriter(text_file.getAbsolutePath(), true));
                            buf.append(tmp.getText());
                            buf.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    index = position; first_click = System.currentTimeMillis();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart(); // 음성인식 서버 초기화는 여기서
        naverRecognizer.getSpeechRecognizer().initialize();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mResult = "";
        txtResult.setText("");
        btnStart.setText(R.string.str_start);
        btnStart.setEnabled(true);
    }
    @Override
    protected void onStop() {
        super.onStop(); // 음성인식 서버 종료
        naverRecognizer.getSpeechRecognizer().release();
    }
    // Declare handler for handling SpeechRecognizer thread's Messages.
    static class RecognitionHandler extends Handler {
        private final WeakReference<AudioActivity> mActivity;
        RecognitionHandler(AudioActivity activity) {
            mActivity = new WeakReference<AudioActivity>(activity);
        }
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void handleMessage(Message msg) {
            AudioActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    class audioView extends LinearLayout {
        TextView audio_text;

        public audioView(Context context){
            super(context);
            init(context);
        }
        public audioView(Context context, AttributeSet attrs){
            super(context, attrs);
            init(context);
        }

        private void init(Context context){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.audio_item, this, true);

            audio_text = (TextView)findViewById(R.id.audio);
        }

        void setAudio(String cur_audio) {audio_text.setText(cur_audio);}
    }

    class AudioItem{
        private String text;

        AudioItem(String text){
            this.text = text;
        }

        String getText() {return this.text;}
        void setText(String tmp) {this.text = tmp;}
    }

    class audioAdapter extends BaseAdapter {
        private ArrayList<AudioItem> items = new ArrayList<>();
        @Override
        public int getCount() {return items.size();}
        @Override
        public Object getItem(int position){return items.get(position);}
        @Override
        public long getItemId(int position){return position;}
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup){
            audioView view = (audioView) convertView;
            if(convertView == null){
                view = new audioView(getApplicationContext());
            }
            AudioItem item = items.get(position);
            view.setAudio(item.getText());
            return view;
        }

        void addItem(AudioItem item) {items.add(item);}
        void cleanItem() {items.clear();}
    }
}

class NaverRecognizer implements SpeechRecognitionListener{
    private final static String TAG = NaverRecognizer.class.getSimpleName();
    private Handler mHandler;
    private SpeechRecognizer mRecognizer;
    public NaverRecognizer(Context context, Handler handler, String clientId){
        this.mHandler = handler;
        try{
            mRecognizer = new SpeechRecognizer(context, clientId);
        } catch (SpeechRecognitionException e){
            e.printStackTrace();
        }
        mRecognizer.setSpeechRecognitionListener(this);
    }

    public SpeechRecognizer getSpeechRecognizer(){
        return mRecognizer;
    }

    public void recognize(){
        try{
            mRecognizer.recognize(new SpeechConfig(SpeechConfig.LanguageType.KOREAN, SpeechConfig.EndPointDetectType.MANUAL));
        }
        catch(SpeechRecognitionException e){
            e.printStackTrace();
        }
    }

    @Override
    @WorkerThread
    public void onInactive(){
        Message msg = Message.obtain(mHandler, R.id.clientInactive);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onReady() {
        Message msg = Message.obtain(mHandler, R.id.clientReady);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onRecord(short[] speech) {
        Message msg = Message.obtain(mHandler, R.id.audioRecording, speech);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onPartialResult(String result) {
        Message msg = Message.obtain(mHandler, R.id.partialResult, result);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onEndPointDetected() {
        Log.d(TAG, "Event occurred : EndPointDetected");
    }
    @Override
    @WorkerThread
    public void onResult(SpeechRecognitionResult result) {
        Message msg = Message.obtain(mHandler, R.id.finalResult, result);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onError(int errorCode) {
        Message msg = Message.obtain(mHandler, R.id.recognitionError, errorCode);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onEndPointDetectTypeSelected(SpeechConfig.EndPointDetectType epdType) {
        Message msg = Message.obtain(mHandler, R.id.endPointDetectTypeSelected, epdType);
        msg.sendToTarget();
    }


}
