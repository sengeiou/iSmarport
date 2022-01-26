package com.szip.sportwatch.Model.EvenBusModel;

import com.szip.sportwatch.Model.ScheduleData;

import java.util.ArrayList;

public class UpdateSchedule {

    private ArrayList<ScheduleData> scheduleDataArrayList;
    private int type;
    private int state;

    public UpdateSchedule(ArrayList<ScheduleData> scheduleDataArrayList) {
        this.scheduleDataArrayList = scheduleDataArrayList;
    }

    public UpdateSchedule(int type, int state) {
        this.type = type;
        this.state = state;
    }

    public ArrayList<ScheduleData> getScheduleDataArrayList() {
        return scheduleDataArrayList;
    }

    public int getType() {
        return type;
    }

    public int getState() {
        return state;
    }

}
