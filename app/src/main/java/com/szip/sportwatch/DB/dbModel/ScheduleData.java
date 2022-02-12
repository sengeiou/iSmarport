package com.szip.sportwatch.DB.dbModel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.sportwatch.DB.AppDatabase;

import java.io.Serializable;

@Table(database = AppDatabase.class)
public class ScheduleData extends BaseModel implements Comparable<ScheduleData> , Serializable {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String msg;
    @Column
    public long time;
    @Column
    public int index;
    @Column
    public int type;

    public ScheduleData() {
    }

    public ScheduleData(String msg, long time, int index, int type) {
        this.msg = msg;
        this.time = time;
        this.index = index;
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int compareTo(ScheduleData o) {
        return (int)(this.time-o.time);
    }
}
