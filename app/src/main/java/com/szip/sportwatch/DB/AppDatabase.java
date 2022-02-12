package com.szip.sportwatch.DB;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

    //版本号
    public static final int VERSION = 11;
    //数据库名称
    public static final String NAME = "iSmarportDatabase";

}
