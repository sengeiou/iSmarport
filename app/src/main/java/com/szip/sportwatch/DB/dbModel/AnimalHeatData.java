package com.szip.sportwatch.DB.dbModel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.sportwatch.DB.AppDatabase;

import androidx.annotation.NonNull;

@Table(database = AppDatabase.class)
public class AnimalHeatData extends BaseModel implements Comparable<AnimalHeatData>{

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long time;

    @Column
    public int tempData;

    @Column
    public String deviceCode;

    public AnimalHeatData(long time, int tempData) {
        this.time = time;
        this.tempData = tempData;
    }

    public AnimalHeatData() {}

    public long getTime() {
        return time;
    }

    public int animalHeatData() {
        return tempData;
    }

    @Override
    public int compareTo(@NonNull AnimalHeatData o) {
        return (int)(this.time-o.time);
    }

}
