package com.example.api_usage_java;

import android.os.Bundle;
import android.os.Handler;
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
        date += String.valueOf(calendar.get(Calendar.YEAR));
        date += String.valueOf(calendar.get(Calendar.MONTH) + 1);
        date += String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        StringBuilder urlBuilder = new StringBuilder("http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson"); /*URL*/
        urlBuilder.append("?" + "serviceKey=" + URLEncoder.encode("aZyc1Eibkz0Spmkj4oqrF%2Bd8k1FK0maWmCZn4bor%2FDTRyfHz3cPaQ1wfh8DBWx8GwBuC4d19onos3Gw6WozScA%3D%3D", "UTF-8")); /*Service Key*/
        urlBuilder.append("&" + "pageNo" + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + "numOfRows" + "=" + URLEncoder.encode("1", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + "startCreateDt" + "=" + URLEncoder.encode(date, "UTF-8")); /*검색할 생성일 범위의 시작*/
        urlBuilder.append("&" + "endCreateDt" + "=" + URLEncoder.encode(date, "UTF-8")); /*검색할 생성일 범위의 종료*/
        try {
            DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
            Document doc = dBuilder.parse(urlBuilder.toString());
            doc.getDocumentElement().normalize();

            input_text = "";
            System.out.println(urlBuilder.toString());
            System.out.println(doc.toString());

            NodeList decide_cnt = doc.getElementsByTagName("DECIDE_CNT");
            NodeList exam_cnt = doc.getElementsByTagName("EXAM_CNT");
            NodeList death_cnt = doc.getElementsByTagName("DEATH_CNT");
            NodeList create_dt = doc.getElementsByTagName("CREATE_DT");

            if(decide_cnt.getLength() >= 1){
                Element nd1 = (Element)decide_cnt.item(0);
                Element nd2 = (Element)exam_cnt.item(0);
                Element nd3 = (Element)death_cnt.item(0);
                Element nd4 = (Element)create_dt.item(0);

                input_text += "확진자 수 : " + nd1.getTextContent() + "\n";
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
}
