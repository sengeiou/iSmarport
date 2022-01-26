package com.szip.sportwatch.Activity.schedule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.necer.calendar.BaseCalendar;
import com.necer.calendar.MonthCalendar;
import com.necer.listener.OnCalendarChangedListener;
import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.Model.EvenBusModel.UpdateSchedule;
import com.szip.sportwatch.Model.ScheduleData;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.MathUitl;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.szip.sportwatch.View.CharacterPickerWindow;
import com.szip.sportwatch.View.character.OnOptionChangedListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ScheduleEditActivity extends BaseActivity implements View.OnClickListener {

    private MonthCalendar monthCalendar;
    private LinearLayout deleteLl;
    private EditText msgEt;
    private TextView timeTv,dateTv,lengthTv;
    private ScheduleData scheduleData;
    private String date;
    private CharacterPickerWindow window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_schedule_edit);
        StatusBarCompat.translucentStatusBar(ScheduleEditActivity.this,true);
        setAndroidNativeLightStatusBar(this,true);
        setTitleText(getString(R.string.schedule_info));
        scheduleData = (ScheduleData) getIntent().getSerializableExtra("schedule");
        if (scheduleData!=null){
            date = DateUtil.getStringDateFromSecond(scheduleData.getTime(),"yyyy-MM-dd");
        } else{
            date = DateUtil.getStringDateFromSecond(DateUtil.getTimeOfToday(),"yyyy-MM-dd");
        }
        initView();
        initEvent();
        initWindow();
    }


    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateList(UpdateSchedule updateSchedule){
        ProgressHudModel.newInstance().diss();
        if (updateSchedule.getType() == 0x52){//添加
            if (updateSchedule.getState()!=0xff){
                Intent intent = new Intent();
                intent.putExtra("schedule",scheduleData);
                setResult(101,intent);
            }else {
                showToast(getString(R.string.schedule_add_fail));
            }
        }else if (updateSchedule.getType() == 0x53){//删除
            if (updateSchedule.getState()==1){
                Intent intent = new Intent();
                intent.putExtra("index",scheduleData.getIndex());
                setResult(102,intent);
            }else {
                showToast(getString(R.string.schedule_delete_fail));
            }
        }else if (updateSchedule.getType() == 0x54){//修改
            if (updateSchedule.getState()==1){
                Intent intent = new Intent();
                intent.putExtra("schedule",scheduleData);
                setResult(103,intent);
            }else {
                showToast(getString(R.string.schedule_edit_fail));
            }
        }
        finish();
    }


    private void initView() {
        deleteLl = findViewById(R.id.deleteLl);
        timeTv = findViewById(R.id.timeTv);
        msgEt = findViewById(R.id.msgEt);
        dateTv = findViewById(R.id.dateTv);
        lengthTv = findViewById(R.id.lengthTv);
        if (scheduleData!=null){
            deleteLl.setVisibility(View.VISIBLE);
            timeTv.setText(DateUtil.getStringDateFromSecond(scheduleData.getTime(),"HH:mm"));
            msgEt.setText(scheduleData.getMsg());
            lengthTv.setText(String.format("%d/30",scheduleData.getMsg().length()));
        }else {
            timeTv.setText(DateUtil.getStringDateFromSecond(Calendar.getInstance().getTimeInMillis()/1000,"HH:mm"));
            lengthTv.setText("0/30");
        }
        monthCalendar = findViewById(R.id.monthCalendar);
        monthCalendar.setInitializeDate(date);
    }

    private void initEvent() {
        findViewById(R.id.timeRl).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
        findViewById(R.id.rightIv).setOnClickListener(this);
        findViewById(R.id.lastMonthIv).setOnClickListener(this);
        findViewById(R.id.nextMonthIv).setOnClickListener(this);
        findViewById(R.id.deleteLl).setOnClickListener(this);
        monthCalendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarChange(BaseCalendar baseCalendar, int year, int month, LocalDate localDate, boolean isTouch) {
                dateTv.setText(String.format(Locale.ENGLISH,"%d-%02d",year,month));
                date = localDate.toString();
            }
        });

        msgEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int len = s.toString().length();
                lengthTv.setText(String.format("%d/30",len));
                if (len>30)
                    lengthTv.setTextColor(Color.RED);
                else
                    lengthTv.setTextColor(getResources().getColor(R.color.black1));
            }
        });
    }

    /**
     * 初始化选择器
     * */
    private void initWindow() {
        //步行计划选择器
        window = new CharacterPickerWindow(this,getString(R.string.stepPlan));

        final List<String> hourList = MathUitl.getNumberList(24);
        final List<String> minList = MathUitl.getNumberList(60);
        //初始化选项数据
        window.getPickerView().setPickerWithoutLink(hourList,minList);
        //设置默认选中的三级项目
        String str[] = timeTv.getText().toString().split(":");
        int hour = Integer.valueOf(str[0]);
        int min = Integer.valueOf(str[1]);
        window.setCurrentPositions(hour, min, 0);
        window.setTitleTv(getString(R.string.time));
        //监听确定选择按钮
        window.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                timeTv.setText(String.format("%02d:%02d",option1,option2));
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.timeRl:
                window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.backIv:
                finish();
                break;
            case R.id.rightIv:{
                long time = DateUtil.getTimeScope(date+" "+
                        timeTv.getText().toString(),"yyyy-MM-dd HH:mm");
                if (msgEt.getText().toString().length()>30)
                    return;
                if(time<Calendar.getInstance().getTimeInMillis()/1000){
                    showToast(getString(R.string.schedule_past));
                    return;
                }
                ProgressHudModel.newInstance().show(ScheduleEditActivity.this,getString(R.string.loading),
                        "error",10000);
                if (scheduleData!=null){
                    scheduleData.setMsg(msgEt.getText().toString().trim());
                    scheduleData.setTime(time);
                    scheduleData.setType(0);
                    BleClient.getInstance().writeForEditSchedule(scheduleData);
                }else {
                    scheduleData = new ScheduleData();
                    scheduleData.setMsg(msgEt.getText().toString().trim());
                    scheduleData.setTime(time);
                    scheduleData.setType(0);
                    BleClient.getInstance().writeForAddSchedule(scheduleData);
                }
            }
                break;
            case R.id.lastMonthIv:
                monthCalendar.toLastPager();
                break;
            case R.id.nextMonthIv:
                monthCalendar.toNextPager();
                break;
            case R.id.deleteLl:
                BleClient.getInstance().writeForDeleteSchedule(scheduleData);
                break;
        }
    }
}