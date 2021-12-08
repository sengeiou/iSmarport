package com.szip.sportwatch.DB.dbModel;


import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.sportwatch.DB.AppDatabase;

import java.io.Serializable;
import java.util.Random;

import androidx.annotation.NonNull;

@Table(database = AppDatabase.class)
public class SportData extends BaseModel implements Comparable<SportData>,Serializable{

    @PrimaryKey(autoincrement = true)
    public long id;

    /**
     * 1:健走  2：跑步  3：跑步机 4：登山 5：马拉松 6：训练跑 7：8：跳绳 9：羽毛球 10：篮球 11：骑行 12：滑雪 13：健身房 14：瑜伽 15：网球 16：乒乓球 17：足球
     * 18：游泳 19：攀岩 20：划船 21:高尔夫 22:冲浪
     * */
    @Column
    public int type;

    /**
     * 运动开始的时间(时间戳)
     * */
    @Column
    public long time;

    /**
     * 运动时长(秒)
     * */
    @Column
    public int sportTime;

    /**
     * 里程(米)
     * */
    @Column
    public int distance;

    /**
     * 卡路里(卡)
     * */
    @Column
    public int calorie;

    /**
     * 平均配速
     * */
    @Column
    public int speed;

    /**
     * 配速数组
     * */
    @Column
    public String speedArray;

    /**
     * 平均心率
     * */
    @Column
    public int heart;
    /**
     * 心率数组
     * */
    @Column
    public String heartArray;

    /**
     * 平均步频
     * */
    @Column
    public int stride;

    /**
     * 步频数组
     * */
    @Column
    public String strideArray;

    /**
     * 计步
     * */
    @Column
    public int step;

    /**
     * 平均海拔
     * */
    @Column
    public int altitude;

    /**
     * 海拔数组
     * */
    @Column
    public String altitudeArray;

    /**
     * 平均温度
     * */
    @Column
    public int temp;

    /**
     * 温度数组
     * */
    @Column
    public String tempArray;

    /**
     * 高尔夫杆数
     * */
    @Column
    public int pole;

    /**
     * 攀爬高度
     * */
    @Column
    public int height;

    /**
     * 设备号
     * */
    @Column
    public String deviceCode;

    /**
     * 时速
     * */
    @Column
    public int speedPerHour;

    /**
     * 时速列表
     * */
    @Column
    public String speedPerHourArray;

    /**
     * 经度列表
     * */
    @Column
    public String lngArray;

    /**
     * 纬度列表
     * */
    @Column
    public String latArray;

    public SportData() {}

    public SportData(long time, int sportTime, int distance, int calorie, int speed,int type,int heart,int stride) {
        this.time = time;
        this.sportTime = sportTime;
        this.distance = distance;
        this.calorie = calorie;
        this.speed = speed;
        this.type = type;
        this.heart = heart;
        this.stride = stride;
        getTableData();
    }


    /**
     * 如果没有数组数据，则造假数据
     *
     * */
    private void getTableData(){
        if (heart!=0){
            heartArray = new String();
            if (sportTime/30+(sportTime%30==0?0:1)==1){
                heartArray = String.valueOf(heart);
            }else {
                for (int i = 0;i<sportTime/30+(sportTime%30==0?0:1);i++){
                    heartArray += ((new Random().nextInt(10)-5)+heart+",");
                }
                if (heartArray.length()>1)
                    heartArray = heartArray.substring(0,heartArray.length()-1);
            }
        }
        if (altitude!=0){
            altitudeArray = new String();
            if (sportTime/(5*60)+(sportTime%(5*60)==0?0:1)==1){
                altitudeArray = String.valueOf(altitude);
            }else {
                for (int i = 0; i< sportTime/(5*60)+(sportTime%(5*60)==0?0:1); i++){
                    altitudeArray += ((new Random().nextInt(20)-10)+altitude+",");
                }
                if (altitudeArray.length()>1)
                    altitudeArray = altitudeArray.substring(0,altitudeArray.length()-1);
            }
        }

        if (stride!=0){
            strideArray = new String();
            if (sportTime/60+(sportTime%60==0?0:1)==1){
                strideArray = String.valueOf(stride);
            }else {
                for (int i = 0; i< sportTime/60+(sportTime%60==0?0:1); i++){
                    strideArray += ((new Random().nextInt(10)-5)+stride+",");
                }
                if (strideArray.length()>1)
                strideArray = strideArray.substring(0,strideArray.length()-1);
            }
        }

        if (speed!=0){
            speedArray = new String();
            if (distance/1000+(distance%1000==0?0:1)==1){
                speedArray = String.valueOf(speed);
            }else {
                for (int i = 0;i<distance/1000+(distance%1000==0?0:1);i++){
                    speedArray += ((new Random().nextInt(20)-10)+speed+",");
                }
                if (speedArray.length()>1)
                speedArray = speedArray.substring(0,speedArray.length()-1);
            }
        }

    }


    @Override
    public int compareTo(@NonNull SportData o) {
        return (int)(o.time-this.time);
    }

    public String getSpeedPerHourArray() {
        return speedPerHourArray==null?"":speedPerHourArray;
    }

    public String getSpeedArray() {
        return speedArray==null?"":speedArray;
    }

    public String getHeartArray() {
        return heartArray==null?"":heartArray;
    }

    public String getStrideArray() {
        return strideArray==null?"":strideArray;
    }

    public String getTempArray() {
        return tempArray==null?"":tempArray;
    }

    public String getAltitudeArray() {
        return altitudeArray==null?"":altitudeArray;
    }

    public String getDeviceCode() {
        return deviceCode;
    }
}
