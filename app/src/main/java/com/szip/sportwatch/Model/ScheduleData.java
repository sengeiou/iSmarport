package com.szip.sportwatch.Model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public class ScheduleData implements Comparable<ScheduleData> , Serializable {
    private String msg;
    private long time;
    private int index;
    private int type;

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
