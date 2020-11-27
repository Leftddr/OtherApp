package com.example.api_usage_java;

import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.TextView;

import com.odsay.odsayandroidsdk.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CovidActivity extends AppCompatActivity {
    SwipeRefreshLayout swipe;
    TextView text;
    String input_text = "";
    Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covid);

        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);
        text = (TextView)findViewById(R.id.text);
        init_swipe();
    }

    private void init_swipe(){
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            requestCovid();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                text.setText(input_text);
                                swipe.setRefreshing(false);
                            }
                        });
                    }
                });
                th.start();
            }
        });
    }

    public void requestCovid() throws IOException {
        Calendar calendar = Calendar.getInstance();
        String date = "";
        String yester_date = "";
        date += String.valueOf(calendar.get(Calendar.YEAR));
        yester_date += String.valueOf(calendar.get(Calendar.YEAR));
        date += String.valueOf(calendar.get(Calendar.MONTH) + 1);
        yester_date += String.valueOf(calendar.get(Calendar.MONTH) + 1);
        date += String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if(Calendar.DAY_OF_MONTH - 1 <= 0){
            if(Calendar.DAY_OF_MONTH + 1 == 2)
                yester_date += "28";
            else if(Calendar.DAY_OF_MONTH + 1 == 1 || Calendar.DAY_OF_MONTH + 1 == 2 || Calendar.DAY_OF_MONTH + 1 == 4 || Calendar.DAY_OF_MONTH + 1 == 6 || Calendar.DAY_OF_MONTH + 1 == 8 || Calendar.DAY_OF_MONTH + 1 == 9 || Calendar.DAY_OF_MONTH + 1 == 11)
                yester_date += "31";
            else
                yester_date += "30";
        }
        else{
            yester_date += String.valueOf(calendar.get(Calendar.DAY_OF_MONTH) - 1);
        }

        StringBuilder urlBuilder = new StringBuilder("http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson"); /*URL*/
        urlBuilder.append("?" + "serviceKey=" + "aZyc1Eibkz0Spmkj4oqrF%2Bd8k1FK0maWmCZn4bor%2FDTRyfHz3cPaQ1wfh8DBWx8GwBuC4d19onos3Gw6WozScA%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + "pageNo" + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + "numOfRows" + "=" + URLEncoder.encode("1", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + "startCreateDt" + "=" + URLEncoder.encode(date, "UTF-8")); /*검색할 생성일 범위의 시작*/
        urlBuilder.append("&" + "endCreateDt" + "=" + URLEncoder.encode(date, "UTF-8")); /*검색할 생성일 범위의 종료*/

        StringBuilder urlBuilder2 = new StringBuilder("http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson");
        urlBuilder2.append("?" + "serviceKey=" + "aZyc1Eibkz0Spmkj4oqrF%2Bd8k1FK0maWmCZn4bor%2FDTRyfHz3cPaQ1wfh8DBWx8GwBuC4d19onos3Gw6WozScA%3D%3D"); /*Service Key*/
        urlBuilder2.append("&" + "pageNo" + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder2.append("&" + "numOfRows" + "=" + URLEncoder.encode("1", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder2.append("&" + "startCreateDt" + "=" + URLEncoder.encode(yester_date, "UTF-8")); /*검색할 생성일 범위의 시작*/
        urlBuilder2.append("&" + "endCreateDt" + "=" + URLEncoder.encode(yester_date, "UTF-8")); /*검색할 생성일 범위의 종료*/

        try {
            DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
            Document doc = dBuilder.parse(urlBuilder.toString());
            Document doc2 = dBuilder.parse(urlBuilder2.toString());
            doc.getDocumentElement().normalize();
            doc2.getDocumentElement().normalize();

            input_text = "";
            System.out.println(urlBuilder.toString());
            System.out.println(urlBuilder2.toString());
            System.out.println(doc.toString());

            NodeList decide_cnt = doc.getElementsByTagName("decideCnt");
            NodeList exam_cnt = doc.getElementsByTagName("examCnt");
            NodeList death_cnt = doc.getElementsByTagName("deathCnt");
            NodeList create_dt = doc.getElementsByTagName("createDt");
            NodeList decide_cnt_yester = doc2.getElementsByTagName("decideCnt");

            if(decide_cnt.getLength() >= 1){
                Element nd1 = (Element)decide_cnt.item(0);
                Element nd2 = (Element)exam_cnt.item(0);
                Element nd3 = (Element)death_cnt.item(0);
                Element nd4 = (Element)create_dt.item(0);
                Element yes = (Element)decide_cnt_yester.item(0);

                input_text += "오늘 확진자 수 : " + String.valueOf(Integer.parseInt(nd1.getTextContent()) - Integer.parseInt(yes.getTextContent())) + "\n";
                input_text += "누적 확진자 수 : " + nd1.getTextContent() + "\n";
                input_text += "검사진행 수 : " + nd2.getTextContent() + "\n";
                input_text += "사망자 수 : " + nd3.getTextContent() + "\n";
                input_text += "등록일시 : " + nd4.getTextContent() + "\n";
            }
            else{
                input_text = "오늘 등록된 데이터가 존재하지 않습니다.";
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

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event){
        float size = text.getTextSize();
        System.out.println(size + "------------------------");
        switch(KeyCode){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(size + 1 >= 200) return true;
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size - 1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(size - 1 <= 10) return true;
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size + 1);
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }
        return false;
    }
}
