package com.szip.sportwatch.DB.dbModel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.sportwatch.DB.AppDatabase;

@Table(database = AppDatabase.class)
public class NotificationData extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String packageName;

    @Column
    public int packageImgId;

    @Column
    public String name;

    @Column
    public boolean state;

    public NotificationData(String packageName, int packageImgId, String name, boolean state) {
        this.packageName = packageName;
        this.packageImgId = packageImgId;
        this.name = name;
        this.state = state;
    }

    public NotificationData() {
    }
}
