package com.szip.sportwatch.Activity.schedule;

import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.Adapter.ScheduleAdapter;
import com.szip.sportwatch.BLE.BleClient;
import com.szip.sportwatch.DB.LoadDataUtil;
import com.szip.sportwatch.Model.EvenBusModel.UpdateSchedule;
import com.szip.sportwatch.DB.dbModel.ScheduleData;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.LogUtil;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ScheduleActivity extends BaseActivity {

    private ImageView rightIv;
    private ListView listView;
    private ScheduleAdapter scheduleAdapter;
    private List<ScheduleData> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_schedule);
        StatusBarCompat.translucentStatusBar(ScheduleActivity.this,true);
        setAndroidNativeLightStatusBar(this,true);
        setTitleText(getString(R.string.schedule));
        SQLite.delete()
                .from(ScheduleData.class)
                .execute();
        initView();
        initEvent();
        ProgressHudModel.newInstance().show(this,getString(R.string.loading));
        BleClient.getInstance().writeForGetSchedule();
    }

    private void initData() {
        list = LoadDataUtil.newInstance().getScheduleData();
        if(list!=null&&list.size()!=0)
            findViewById(R.id.listEmptyTv).setVisibility(View.GONE);
        else {
            findViewById(R.id.listEmptyTv).setVisibility(View.VISIBLE);
        }
        scheduleAdapter.setList(list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateList(UpdateSchedule updateSchedule){
        ProgressHudModel.newInstance().diss();
        initData();
    }


    private void initView() {
        rightIv = findViewById(R.id.rightIv);
        rightIv.setImageResource(R.mipmap.schedule_icon_add);
        listView = findViewById(R.id.listView);
        scheduleAdapter = new ScheduleAdapter(getApplicationContext());
        listView.setAdapter(scheduleAdapter);
    }

    private void initEvent() {
        rightIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.size()<10)
                    startActivityForResult(new Intent(ScheduleActivity.this,ScheduleEditActivity.class),100);
                else
                    showToast(getString(R.string.schedule_fall));
            }
        });

        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ScheduleActivity.this,ScheduleEditActivity.class);
                intent.putExtra("schedule",list.get(position));
                startActivityForResult(intent,100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            if (resultCode==101){
                ScheduleData scheduleData = (ScheduleData) data.getSerializableExtra("schedule");
                if (scheduleData==null)
                    return;
                findViewById(R.id.listEmptyTv).setVisibility(View.GONE);
                list.add(scheduleData);
                scheduleAdapter.setList(list);
            }else if (resultCode == 102){
                int index = data.getIntExtra("index",-1);
                LogUtil.getInstance().logd("data******","index = "+index);
                if (index == -1)
                    return;
                for (int i = 0;i<list.size();i++){
                    if (list.get(i).getIndex()==index){
                        list.remove(i);
                        break;
                    }
                }
                scheduleAdapter.setList(list);
                if (list.size()==0)
                    findViewById(R.id.listEmptyTv).setVisibility(View.VISIBLE);
            }else if (resultCode == 103){
                ScheduleData scheduleData = (ScheduleData) data.getSerializableExtra("schedule");
                if (scheduleData == null)
                    return;
                for (int i = 0;i<list.size();i++){
                    if (list.get(i).getIndex()==scheduleData.getIndex()){
                        list.set(i,scheduleData);
                        break;
                    }
                }
                scheduleAdapter.setList(list);
            }
        }
    }
}