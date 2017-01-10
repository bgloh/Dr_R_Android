package com.example.hosea.dr_r_android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.example.hosea.dr_r_android.R;
import com.example.hosea.dr_r_android.activity.TimeActivity;
import com.example.hosea.dr_r_android.adapter.FeedAdapter;
import com.example.hosea.dr_r_android.adapter.SleepAdapter;
import com.example.hosea.dr_r_android.dao.FeedVO;
import com.example.hosea.dr_r_android.dao.SleepVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import im.dacer.androidcharts.ClockPieHelper;

/**
 * Created by hosea on 2016-12-29.
 */

public class FeedTimeFragment extends Fragment {
    private AQuery aq = new AQuery(getActivity());
    private TextView myOutput, myToday, myToggle, myPowderToggle;
    private ImageView myCircle,myPowderCircle;
    private FeedAdapter feedAdapter;
    private ArrayList<FeedVO> feedDataList;
    Date today = new Date();
    int user_id;
    long startTime ,endTime;
    Date s_start;
    Date s_end;
    long outTime;
    final static int Init = 0;
    final static int Run = 1;
    final static int Pause = 2;
    int cur_Status = Init; //현재의 상태를 저장할변수를 초기화함.
    long myBaseTime;
    long myPauseTime;
    final SimpleDateFormat curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
    final SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);
    final SimpleDateFormat curDayFormat = new SimpleDateFormat("dd", Locale.KOREA);
    private String feed;
    Date date = new Date();
    int year, month, day;
    SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
    EditText powderAmount;
    public FeedTimeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate the layout for this fragment

        Bundle args = getActivity().getIntent().getExtras();
        user_id = args.getInt("u_id");
        feed = "좌";
        final View view = inflater.inflate(R.layout.fragment_feedtime, container, false);
        myToday = (TextView) view.findViewById(R.id.today_feed);
        myOutput = (TextView) view.findViewById(R.id.time_out_feed);
        myToggle = (TextView) view.findViewById(R.id.feed_toggle);
        myCircle = (ImageView) view.findViewById(R.id.feed_toggle_img);
        myCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myOnClick(v);
            }
        });
        myToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myOnClick(v);
            }
        });

        myPowderToggle = (TextView) view.findViewById(R.id.powder_toggle);
        myPowderCircle = (ImageView) view.findViewById(R.id.powder_toggle_img);
        myPowderCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myOnClick(v);
            }
        });
        myPowderToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myOnClick(v);
            }
        });

        powderAmount = (EditText) view.findViewById(R.id.powder_amount);
        final ListView listView = (ListView) view.findViewById(R.id.feed_listView);
        final RadioButton right = (RadioButton) view.findViewById(R.id.feed_right);
        final RadioButton left = (RadioButton) view.findViewById(R.id.feed_left);
        final RadioButton powder = (RadioButton) view.findViewById(R.id.feed_powder);
        final RelativeLayout feedLayout = (RelativeLayout) view.findViewById(R.id.feed);
        final RelativeLayout powderLayout = (RelativeLayout) view.findViewById(R.id.powder);

        powderLayout.setVisibility(View.GONE);
        RadioButton.OnClickListener optionOnClickListener = new RadioButton.OnClickListener() {
            public void onClick(View v) {
                if (right.isChecked()) {
                    feed = "우";
                    powderLayout.setVisibility(View.GONE);
                    feedLayout.setVisibility(View.VISIBLE);
                } else if (left.isChecked()) {
                    Toast.makeText(getActivity(), "좌", Toast.LENGTH_SHORT).show();
                    powderLayout.setVisibility(View.GONE);
                    feedLayout.setVisibility(View.VISIBLE);
                } else if (powder.isChecked()) {
                    feed = "분유";
                    feedLayout.setVisibility(View.GONE);
                    powderLayout.setVisibility(View.VISIBLE);
                }
            }
        };

        right.setOnClickListener(optionOnClickListener);
        left.setOnClickListener(optionOnClickListener);
        powder.setOnClickListener(optionOnClickListener);
        feedDataList = new ArrayList<>();
//        feedDataList.add(new FeedVO(1,2,"ghtpdk",new Timestamp(12123123), new Timestamp(12123123), 3,"우"));
//        feedDataList.add(new FeedVO(1,2,"ghtpdk",new Timestamp(12123123), new Timestamp(12123123), 3,"우"));
//        feedDataList.add(new FeedVO(1,2,"ghtpdk",new Timestamp(12123123), new Timestamp(12123123), 3,"우"));
//        feedDataList.add(new FeedVO(1,2,"ghtpdk",new Timestamp(12123123), new Timestamp(12123123), 3,"우"));
        feedAdapter = new FeedAdapter(view.getContext(),R.layout.itemsforfeedlist, feedDataList);
        try {
            listView.setAdapter(feedAdapter); // uses the view to get the context instead of getActivity().
        } catch (NullPointerException e) {

        }

        //현재 날짜 받아오기
        // 년,월,일로 쪼갬
        year = Integer.parseInt(curYearFormat.format(date));
        month =Integer.parseInt(curMonthFormat.format(date));
        day = Integer.parseInt(curDayFormat.format(date));

        myToday.setText(year+"년 "+(month)+"월 "+day+"일 "+ getDayKor());
        readFeed();
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void myOnClick(View v) {
        switch (v.getId()) {
            case R.id.feed_toggle: //시작버튼을 클릭했을때 현재 상태값에 따라 다른 동작을 할수있게끔 구현
            case R.id.feed_toggle_img:
                switch (cur_Status) {
                    case Init:
                        myBaseTime = SystemClock.elapsedRealtime();
                        s_start = new Date();
                        startTime = s_start.getTime()/1000;
                        System.out.println(myBaseTime);
                        //myTimer이라는 핸들러를 빈 메세지를 보내서 호출
                        myTimer.sendEmptyMessage(0);
                        myToggle.setText("기록 중지"); //버튼의 문자"시작"을 "멈춤"으로 변경
                        cur_Status = Run; //현재상태를 런상태로 변경
                        break;
                    case Run:
                        myTimer.removeMessages(0); //핸들러 메세지 제거
                        myPauseTime = SystemClock.elapsedRealtime();
                        s_end = new Date();
                        endTime = s_end.getTime() /1000;
                        myToggle.setText("기록 시작");
                        cur_Status = Pause;
                        outTime = 0;
                        String easy_outTime = String.format("%02d:%02d:%02d", outTime / 1000 / 3600 , (outTime / 1000 % 3600) / 60, (outTime / 1000 %3600 %60 ));
                        myOutput.setText(easy_outTime);
                        writeDiary();
                        break;
                    case Pause:
                        myBaseTime = SystemClock.elapsedRealtime();
                        s_start = new Date();
                        startTime = s_start.getTime()/1000;
                        myTimer.sendEmptyMessage(0);
                        myToggle.setText("기록 중지");
                        cur_Status = Run;
                        break;
                }
                break;
            case R.id.powder_toggle:
            case R.id.powder_toggle_img:
                String pAmount = powderAmount.getText().toString();
                if(!pAmount.isEmpty() && !pAmount.equals("")) {
                    writePowder(Integer.parseInt(pAmount));
                }
                break;
        }
    }

    Handler myTimer = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    myOutput.setText(getTimeOut());
                    this.sendEmptyMessageDelayed(0, 1000);
                    CircleTouchAnimation();
                    break;
            }
        }
    };

    private void CircleTouchAnimation() {
        Animation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(1000);
        animation.setStartOffset(30);
        animation.setStartOffset(1);
        if (cur_Status == Run) {
            myCircle.startAnimation(animation);
        }
    }

    //현재시간을 계속 구해서 출력하는 메소드
    String getTimeOut() {
        long now = SystemClock.elapsedRealtime(); //애플리케이션이 실행되고나서 실제로 경과된 시간(??)^^;
        outTime = now - myBaseTime;
        String easy_outTime = String.format("%02d:%02d:%02d", outTime / 1000 / 3600 , (outTime / 1000 % 3600) / 60, (outTime / 1000 %3600 %60 ));
        return easy_outTime;

    }

    public static String getDayKor(){
        Calendar cal = Calendar.getInstance();
        int cnt = cal.get(Calendar.DAY_OF_WEEK) - 1;
        String[] week = { "일", "월", "화", "수", "목", "금", "토" };

        return "( "+week[cnt]+" )";
    }
    public void writeDiary() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("u_id" , user_id);
        params.put("f_start" , dateFormat.format(s_start));
        params.put("f_end" , dateFormat.format(s_end));
        params.put("f_total" , endTime-startTime);
        params.put("feed", feed);
        aq.ajax("http://52.41.218.18:8080/addFeedTime", params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject html, AjaxStatus status) {
                try {
                    if (html.getString("msg").equals("정상 작동")) {
                        Toast.makeText(getActivity(), "작성 되었습니다.", Toast.LENGTH_SHORT).show();
                        readFeed();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void readFeed() {
        Map<String, Object> params = new HashMap<String, Object>();
        SimpleDateFormat dateFormat2 = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        params.put("u_id", user_id);
        params.put("f_start", dateFormat2.format(date));
        aq.ajax("http://52.41.218.18:8080/getFeedTimeByDate", params, JSONArray.class, new AjaxCallback<JSONArray>() {
            @Override
            public void callback(String url, JSONArray html, AjaxStatus status) {
                if (html != null) {
                    try {
                        feedDataList.clear();
                        jsonArrayToSleepArray(html);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(),"연결상태가 좋지않아 목록을 부를 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void writePowder(int amount) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("u_id" , user_id);
        params.put("f_start" , dateFormat.format(today));
        params.put("f_end" , dateFormat.format(today));
        params.put("f_total" , amount);
        params.put("feed", feed);
        aq.ajax("http://52.41.218.18:8080/addFeedTime", params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject html, AjaxStatus status) {
                try {
                    if (html.getString("msg").equals("정상 작동")) {
                        Toast.makeText(getActivity(), "작성 되었습니다.", Toast.LENGTH_SHORT).show();
                        powderAmount.setText("");
                        readFeed();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void jsonArrayToSleepArray(JSONArray jsonArr) throws JSONException {
        for (int i = 0; i < jsonArr.length(); i++) {
            feedDataList.add(new FeedVO(jsonArr.getJSONObject(i)));
            feedAdapter.notifyDataSetChanged();
        }
    }
}

