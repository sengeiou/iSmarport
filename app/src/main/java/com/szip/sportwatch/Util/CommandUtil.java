package com.szip.sportwatch.Util;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by KB on 2017/5/8.
 */

public class CommandUtil {

    public static final int SYNC_TYPE_STEP = 0x01;//记步数据
    public static final int SYNC_TYPE_HEART = 0x02;//心率数据
    public static final int SYNC_TYPE_SLEEP = 0x03;//睡眠数据
    public static final int SYNC_TYPE_RUN = 0x04;//跑步数据
    public static final int SYNC_TYPE_ONFOOT = 0x05;//徒步数据
    public static final int SYNC_TYPE_MARATHON = 0x06;//马拉松
    public static final int SYNC_TYPE_ROPE_SHIPPING = 0x07;//跳绳
    public static final int SYNC_TYPE_SWIMMING = 0x08;//户外游泳
    public static final int SYNC_TYPE_ROCK_CLIMBING = 0x09;//攀岩
    public static final int SYNC_TYPE_SKIING = 0x0A;//滑雪
    public static final int SYNC_TYPE_RIDING = 0x0B;//骑行
    public static final int SYNC_TYPE_ROWING = 0x0C;//划船
    public static final int SYNC_TYPE_BUNGEE_JUMPING = 0x0D;//蹦极
    public static final int SYNC_TYPE_MOUNTAINEERING = 0x0E;//登山
    public static final int SYNC_TYPE_PARACHUTE = 0x0F;//跳伞
    public static final int SYNC_TYPE_GOLF = 0x10;//高尔夫
    public static final int SYNC_TYPE_SURFING = 0x11;//冲浪
    public static final int SYNC_TYPE_TREADMILL = 0x14;//跑步机
    public static final int SYNC_SOS = 0x13;//这个是手表发了SOS信号
    public static final int SYNC_TYPE_TIME = 0x30;//同步时间
    public static final int SYNC_TYPE_USERINFO = 0x31;//用户信息
    public static final int SYNC_TYPE_SMARTDEVICE_INFO = 0x32;//设备信息
    public static final int SYNC_TYPE_WEATHER = 0x33;//天气
    public static final int SYNC_TYPE_INCALL = 0x34;//来电
    public static final int SYNC_TYPE_CRUISE_COORDINATE = 0x36;//巡航坐标
    public static final int SYNC_ALARM_CLOCK = 0x37;//闹钟

    private static int[] sportTypeValue = new int[]{SYNC_TYPE_RUN, SYNC_TYPE_ONFOOT, SYNC_TYPE_MARATHON,
            /*SYNC_TYPE_ROPE_SHIPPING,*/ SYNC_TYPE_SWIMMING, SYNC_TYPE_ROCK_CLIMBING, SYNC_TYPE_SKIING, SYNC_TYPE_RIDING, SYNC_TYPE_ROWING,
            /*SYNC_TYPE_BUNGEE_JUMPING,*/ SYNC_TYPE_MOUNTAINEERING, /*SYNC_TYPE_PARACHUTE,*/ SYNC_TYPE_GOLF, SYNC_TYPE_SURFING, SYNC_TYPE_TREADMILL,
            SYNC_TYPE_HEART, SYNC_TYPE_SLEEP, SYNC_TYPE_STEP};

    /**
     * 获得指定下标对应的运动类型
     *
     * @param index
     * @return
     */
    public static int getSportType(int index) {
        if (index < sportTypeValue.length) {
            return sportTypeValue[index];
        }
        return sportTypeValue[0];
    }

    /**
     * 获得运动类型的总个数
     *
     * @return
     */
    public static int getSportTypeCount() {
        return sportTypeValue.length;
    }

    /**
     * 将同步手表信息的指令转为byte[]
     *
     * @param syncType   指令的类型,对照文档，比如记步数据是0x01,心率数据是0x02
     * @param byteLength byte[]的长度,
     *                   如果是请求手表上的数据，byte[]长度为8；如果是向手机发送数据，则具体长度看具体情况，比如同步手机时间到手表是16(前8个bit用于标识信息，后8个bit标识时间)
     * @param dataLength 手机发往手表的需要手表解析的数据包的长度
     *                   比如，手机将当前的系统时间发给手表，则时间数据的长度是8
     *                   如果是请求手表上的运动数据，则数据长度为0，因为发送的是请求同步数据的指令，并没有其他信息需要手表解析
     * @return
     */
    public static byte[] getCommandbyteArray(int syncType, int byteLength, int dataLength, boolean isNewConnectType) {
        long time = System.currentTimeMillis() / 1000;
        byte[] data = new byte[byteLength];

        data[0] = (byte) 0xAA;
        data[1] = (byte) syncType;
        data[2] = (byte) dataLength;
        data[3] = 0;
        if (isNewConnectType) {
            data[4] = (byte) (0xF0);
            data[5] = (byte) (0xF0);
            data[6] = (byte) (0xF0);
            data[7] = (byte) (0xF0);
        } else {
            data[4] = (byte) 0;
            data[5] = (byte) 0;
            data[6] = (byte) 0;
            data[7] = (byte) 0;
        }

        if (syncType == SYNC_TYPE_TIME) {
            int[] dateArray = DateUtil.getNowDate();
            data[8] = (byte) (dateArray[0] & 0xFF);
            data[9] = (byte) ((dateArray[0] >> 8) & 0xFF);
            data[10] = (byte) (dateArray[1] & 0xFF);
            data[11] = (byte) (dateArray[2] & 0xFF);
            data[12] = (byte) (dateArray[3] & 0xFF);
            data[13] = (byte) (dateArray[4] & 0xFF);
            data[14] = (byte) (dateArray[5] & 0xFF);
            data[15] = (byte) (dateArray[6] & 0xFF);
            data[16] = 0x12;
            data[17] = 0x34;
            data[18] = 0x56;
            data[19] = 0x78;
        }
//        else if (syncType == SYNC_TYPE_USERINFO) {
//            //全部转为国际单位制传到手表
//            AccountInfo mAccountInfo = UserInfoBLL.getInstance().getUserInfos();
//            SharedPreferences sharedPreferences = Utils.getSharedPreferences(BTNotificationApplication.SHARED_NAME, MODE_PRIVATE);
//            boolean isSyncEarlyWarmHeartRate = sharedPreferences.getBoolean(BTNotificationApplication.EARLY_WARM_HEART_RATE_STATE, false);
//            int language = Utils.getLanguage();
//            int height = mAccountInfo.getStature();
//            float weight = mAccountInfo.getWeight();
//            int stepCounts = 0;
//            int stepLength = 0;
//            int unit = mAccountInfo.getUnit();
//            int clock = mAccountInfo.getClockStyle();
//            if (unit == 1) {
//                height = Utils.ft2cm(height);
//                weight = Utils.lb2kg(weight);
//            }
//            int mWeight = Math.round(weight);
//            data[8] = (byte) (height & 0xff);
//            data[9] = (byte) ((height >> 8) & 0xff);
//            data[10] = (byte) (mWeight & 0xff);
//            data[11] = (byte) ((mWeight >> 8) & 0xff);
//            data[12] = (byte) stepCounts;
//            data[13] = 0;
//            data[14] = (byte) stepLength;
//            data[15] = (byte) unit;
//            data[16] = (byte) clock;//时钟样式
//            //预警心率
//            if (isSyncEarlyWarmHeartRate) {
//                data[17] = (byte) mAccountInfo.getEarlyWarmHeartRate();
//            } else {
//                data[17] = 0;
//            }
//            //年龄
//            data[18] = (byte) (mAccountInfo.getAge() <= 0 ? 27 : mAccountInfo.getAge());
//            //保留字节
//            data[19] = 0;
//            //语言ID
//            data[20] = (byte) (language & 0xff);
//            data[21] = (byte) ((language >> 8) & 0xff);
//        }

        Log.d("SZIP******","发送的蓝牙数据:"+ DateUtil.byteToHexString(data));
        return data;
    }


    /**
     * @param syncType
     * @param byteLength
     * @param dataLength
     * @param name
     * @param number
     * @return
     */
    public static byte[] getIncallbyteArray(int syncType, int byteLength, int dataLength, String name, String number, int callState) {
        try {
            long time = System.currentTimeMillis() / 1000;

            byte[] numByte = new byte[24];
            byte[] nameByte = new byte[24];
            byte[] numTemp = number.getBytes();
            byte[] nameTemp = name.getBytes("UTF-8");
            int numLength = numTemp.length;
            int nameLength = nameTemp.length;

            byte[] data = new byte[8 + 1 + 24 + 24];

            data[0] = (byte) 0xAA;
            data[1] = (byte) syncType;
            data[2] = (byte) 0x31;
            data[3] = 0;
            data[4] = (byte) (time & 0xFF);
            data[5] = (byte) ((time >> 8) & 0xFF);
            data[6] = (byte) ((time >> 16) & 0xFF);
            data[7] = (byte) ((time >> 24) & 0xFF);

            data[8] = (byte) callState;

            if (numLength > 24) {
                System.arraycopy(numTemp, 0, numByte, 0, 24);
                numLength = numByte.length;
            } else {
                System.arraycopy(numTemp, 0, numByte, 0, numTemp.length);
                numLength = numTemp.length;
            }
            for (int i = 0; i < numLength; i++) {
                data[9 + i] = (byte) (numByte[i] & 0xFF);
            }

            int index = 9 + 24;

            if (nameLength > 24) {
                System.arraycopy(nameTemp, 0, nameByte, 0, 24);
                nameLength = nameByte.length;
            } else {
                System.arraycopy(nameTemp, 0, nameByte, 0, nameTemp.length);
                nameLength = nameTemp.length;
            }
            for (int i = 0; i < nameLength; i++) {
                data[index + i] = (byte) (nameByte[i] & 0xFF);
            }
            return data;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得同步天气的byte
     *
     * @param syncType
     * @param mp25
     * @param weatherCode
     * @param weatherName
     * @param press
     * @param max_tmp
     * @param min_tmp
     * @return
     */
    public static byte[] getWeatherByteArray(int syncType, String mp25, String weatherCode, String weatherName, String press, String max_tmp, String min_tmp) {
        try {
            long time = System.currentTimeMillis() / 1000;
            int mMinTmp = Integer.valueOf(min_tmp);
            int mMaxTmp = Integer.valueOf(max_tmp);
            int mPress = Integer.valueOf(press);
            int mWeatherCode = Integer.valueOf(weatherCode);
            int mMP25 = Integer.valueOf(mp25);
            byte[] data = new byte[20];
            data[0] = (byte) 0xAA;
            data[1] = (byte) syncType;
            data[2] = (byte) 0xC;
            data[3] = 0;
            data[4] = (byte) (time & 0xFF);
            data[5] = (byte) ((time >> 8) & 0xFF);
            data[6] = (byte) ((time >> 16) & 0xFF);
            data[7] = (byte) ((time >> 24) & 0xFF);
            data[8] = (byte) mMinTmp;
            data[9] = (byte) mMaxTmp;
            data[10] = 0;//当前气温
            data[11] = 0;//保留字节
            //气压
            data[12] = (byte) (mPress & 0xff);
            data[13] = (byte) ((mPress >> 8) & 0xff);
            data[14] = (byte) ((mPress >> 16) & 0xff);
            data[15] = (byte) ((mPress >> 24) & 0xff);
            //图片编码
            data[16] = (byte) (mWeatherCode & 0xff);
            data[17] = (byte) ((mWeatherCode >> 8) & 0xff);
            //MP25
            data[18] = (byte) (mMP25 & 0xff);
            data[19] = (byte) ((mMP25 >> 8) & 0xff);

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    /**
//     * 获取3天天气信息byte[]文件
//     *
//     * @param syncType
//     * @param info
//     * @param type     0表示今天天气，1表示明天天气，2表示后天天气
//     * @return
//     */
//    public static byte[] get3DaysWeather(int syncType, WeatherInfo info, int type) {
//        try {
//            long time = System.currentTimeMillis() / 1000;
//            int mMinTmp = Integer.valueOf(info.getMin());
//            int mMaxTmp = Integer.valueOf(info.getMax());
//            int mPress = Integer.valueOf(info.getPres());
//            int mWeatherCode = Integer.valueOf(info.getCode());
//            int mMP25 = Integer.valueOf(info.getPm25());
//            int windDeg = Integer.valueOf(info.getWindDeg());//风向360角
//            //int sc = Integer.valueOf(info.getWindSc());//风力
//            String sc = info.getWindSc();//风力
//            byte[] scbyte = sc.getBytes(Charset.forName("UTF-8"));
//            int windSpd = Integer.valueOf(info.getWindSpd());//风速
//
//            byte[] data = new byte[24];
//            data[0] = (byte) 0xAA;
//            data[1] = (byte) syncType;
//            data[2] = (byte) 0x10;
//            data[3] = 0;
//            data[4] = (byte) (time & 0xFF);
//            data[5] = (byte) ((time >> 8) & 0xFF);
//            data[6] = (byte) ((time >> 16) & 0xFF);
//            data[7] = (byte) ((time >> 24) & 0xFF);
//            data[8] = (byte) mMinTmp;
//            data[9] = (byte) mMaxTmp;
//            data[10] = 0;//当前气温
//            data[11] = (byte) type;//由于表示日期
//            //气压
//            data[12] = (byte) (mPress & 0xff);
//            data[13] = (byte) ((mPress >> 8) & 0xff);
//            data[14] = (byte) ((mPress >> 16) & 0xff);
//            data[15] = (byte) ((mPress >> 24) & 0xff);
//            //图片编码
//            data[16] = (byte) (mWeatherCode & 0xff);
//            data[17] = (byte) ((mWeatherCode >> 8) & 0xff);
//            //MP25
//            data[18] = (byte) (mMP25 & 0xff);
//            data[19] = (byte) ((mMP25 >> 8) & 0xff);
//            //风向
//            data[20] = (byte) (windDeg & 0xff);
//            data[21] = (byte) ((windDeg >> 8) & 0xff);
//            //风力
//            //data[22] = (byte) (sc & 0xff);
//            data[22] = scbyte[0];
//            //风速
//            data[23] = (byte) (windSpd);
//
//            return data;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    /**
//     * 将巡航坐标信息转为byte[]
//     *
//     * @param type
//     * @param infos
//     * @return
//     */
//    public static byte[] getCoordinateBytes(int type, ArrayList<CruiseCoordinateInfo> infos) {
//        int size = infos.size();
//        int dataLength = 0;
//        if (size > 0) {
//            dataLength = 4 + size * 2 * 4;
//        }
//        long time = System.currentTimeMillis() / 1000;
//        byte[] data = new byte[8 + dataLength];
//        data[0] = (byte) 0xAA;
//        data[1] = (byte) type;
//        data[2] = (byte) (dataLength & 0xff);
//        data[3] = (byte) (((dataLength >> 8)) & 0xff);
//        data[4] = (byte) (time & 0xFF);
//        data[5] = (byte) ((time >> 8) & 0xFF);
//        data[6] = (byte) ((time >> 16) & 0xFF);
//        data[7] = (byte) ((time >> 24) & 0xFF);
//        if (size > 0) {
//            data[8] = (byte) (size & 0xFF);
//            data[9] = (byte) ((size >> 8) & 0xFF);
//            data[10] = (byte) ((size >> 16) & 0xFF);
//            data[11] = (byte) ((size >> 24) & 0xFF);
//            for (int i = 0; i < size; i++) {
//                CruiseCoordinateInfo info = infos.get(i);
//                int lat = (int) (Float.valueOf(info.getLat()) * 100000);
//                int lng = (int) (Float.valueOf(info.getLng()) * 100000);
//                data[12 + i * 4] = (byte) (lng & 0xFF);
//                data[13 + i * 4] = (byte) ((lng >> 8) & 0xff);
//                data[14 + i * 4] = (byte) ((lng >> 16) & 0xff);
//                data[15 + i * 4] = (byte) ((lng >> 24) & 0xff);
//
//                data[12 + (size + i) * 4] = (byte) (lat & 0xFF);
//                data[13 + (size + i) * 4] = (byte) ((lat >> 8) & 0xFF);
//                data[14 + (size + i) * 4] = (byte) ((lat >> 16) & 0xFF);
//                data[15 + (size + i) * 4] = (byte) ((lat >> 24) & 0xFF);
//            }
//        }
//        return data;
//    }
//
//
//    /**
//     * 将闹钟信息，转为byte[]
//     *
//     * @param type
//     * @param info
//     * @return
//     */
//    public static byte[] getAlarmClockType(int type, AlarmClockInfo info, int sortNum) {
//        int alarmDataLength = 5;
//        int dataLength = 8 + alarmDataLength;
//        long time = System.currentTimeMillis() / 1000;
//
//        int cycle = info.getAlarmCycle();
//        boolean state = info.isAlarmState();
//        int[] hm = DateUtil.getHHmm(info.getAlarmTime());
//        byte[] data = new byte[dataLength];
//        data[0] = (byte) 0xAA;
//        data[1] = (byte) type;
//        data[2] = (byte) (alarmDataLength & 0xff);
//        data[3] = (byte) (((alarmDataLength >> 8)) & 0xff);
//        data[4] = (byte) (time & 0xFF);
//        data[5] = (byte) ((time >> 8) & 0xFF);
//        data[6] = (byte) ((time >> 16) & 0xFF);
//        data[7] = (byte) ((time >> 24) & 0xFF);
//
//        //闹钟序号
//        data[8] = (byte) sortNum;
//        //重复周期
//        if (!state) {
//            cycle = 0xff;
//        } else {
//            if (cycle == 127) {
//                cycle = 0x80;
//            }
//        }
//        data[9] = (byte) cycle;
//        //小时
//        data[10] = (byte) (hm[0]);
//        //分钟
//        data[11] = (byte) (hm[1]);
//        //备注
//        data[12] = 0;
//
//        return data;
//
//    }


    /**
     * 计算列表平均值
     * */
    public static HashMap<Integer,String> getAvenrage(byte[] datas, int lenght){
        HashMap<Integer,String> hashMap = new HashMap<>();
        int sum = 0;
        int allData = 0;
        StringBuffer str = new StringBuffer();
        for (int i = 0;i<datas.length;i+=lenght){
            if (lenght==1){
                allData+=datas[i];
                str.append(String.format(Locale.ENGLISH,",%d",datas[i]));
            }else {
                allData+=(datas[i] & 0xff) + ((datas[i+1] & 0xFF) << 8);
                str.append(String.format(Locale.ENGLISH,",%d",(datas[i] & 0xff) + ((datas[i+1] & 0xFF) << 8)));
            }
            sum++;
        }
        hashMap.put(sum==0?0:allData/sum,str.length()==0?"":str.substring(1));
        return hashMap;
    }

    /**
     * 将16进制的String转为byte
     *
     * @param hexString
     * @return
     */
    private static byte[] hexString2Bytes(String hexString) {
        if (TextUtils.isEmpty(hexString)) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 将String转为16进制的String
     *
     * @param str
     * @return
     */
    private static String str2HexString(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();

        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString();
    }

    /**
     * 将Byte[]转为16进制String
     *
     * @param b
     * @return
     */
    public static String byte2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xff);
            if (hex.length() == 1) {
                ret = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    /**
     * 将16进制Sting转为Sting
     *
     * @param hexStr
     * @return
     */
    public static String hexString2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

//    /**
//     * S 8
//     * 获得经纬度数据
//     * 将byte[]数据解析为指定格式的StringBuilder
//     *
//     * @param laArray 纬度的增量数组
//     * @param loArray 经度的增量数组
//     * @param startLa 开始纬度值
//     * @param startLo 开始精度值
//     * @param length  数组的长度
//     * @return {0.00001,0.00001},{0.00001,0.00001},{纬度，经度}
//     */
//    public static StringBuilder getLaLoSB(byte[] laArray, byte[] loArray, int startLa, int startLo, int length) {
//        int[] laIntArray = new int[length];
//        int[] loIntArray = new int[length];
//        for (int i = 0; i < length; i++) {
//            // int laInt = laArray[i];
//            //byte la = laArray[i];
//            startLa += laArray[i];
//            startLo += loArray[i];
//            laIntArray[i] = startLa;
//            loIntArray[i] = startLo;
//        }
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < length; i++) {
//            sb.append("{");
//            sb.append(Utils.getFloatScale(5, laIntArray[i] * 0.00001f)).append(",").append(Utils.getFloatScale(5, loIntArray[i] * 0.00001f)).append("}");
//            if (i != length - 1) {
//                sb.append(",");
//            }
//        }
//        return sb;
//    }
//
//    /**
//     * U 8
//     * 获得速度数据
//     * 将byte[]数组解释为StringBuilder,蓝牙上传的数据的精度是整数，规定的精度是单位0.1km/h，我们转为经度为0.1km/h
//     *
//     * @param speedArr 要解释的速度数组
//     * @param length   数组的长度
//     * @return 0.1, 1.0, 速度
//     */
//    public static StringBuilder getSpeedStringBuilder(byte[] speedArr, int length) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < length; i++) {
//            sb.append(Utils.getFloatScale(1, (speedArr[i] & 0xff)) * 1.0d);
//            if (i != length - 1) {
//                sb.append(",");
//            }
//        }
//        return sb;
//    }

    /**
     * U8
     * 获得每洞杆数
     *
     * @param speedArr
     * @param length
     * @return 第一洞杆数，第二洞杆数……
     */
    public static StringBuilder getPoleNumberStringBuilder(byte[] speedArr, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((speedArr[i] & 0xff));
            if (i != length - 1) {
                sb.append(",");
            }
        }
        return sb;
    }

    /**
     * S 16
     * 获取海拔数据
     * 将byte[]转为StringBuilder，单位：米
     *
     * @param altitudeArr
     * @param length
     * @return 海拔，海拔，123
     */
    public static StringBuilder getAltitudeStringBuilder(byte[] altitudeArr, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            //对于海拔，不进行无符号计算，计算出是负数的直接舍去
            int a = (altitudeArr[2 * i]);
            int b = ((altitudeArr[2 * i + 1]) << 8);
            //a = ((a >= 0) ? a : a + 256);
            b = ((b >= 0) ? b : b + 256);
            //int value = ((altitudeArr[2 * i] /*& 0xff*/) + (((altitudeArr[2 * i + 1]) /*& 0xff*/) << 8));
            int value = a + b;
            //if (value>=0){
            sb.append(value);
            if (i != length - 1) {
                sb.append(",");
            }
            // }
        }
        return sb;
    }

    /**
     * 高尔夫击球距离
     *
     * @param hitBallDistance
     * @param length
     * @return
     */
    public static StringBuilder getHitBallStringBuilder(byte[] hitBallDistance, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((hitBallDistance[2 * i] & 0xff) + (((hitBallDistance[2 * i + 1]) & 0xff) << 8));
            if (i != length - 1) {
                sb.append(",");
            }
        }
        return sb;
    }

    /**
     * U16
     * 获取配速数据，单位是秒
     *
     * @param paceArr
     * @param length
     * @return 配速，配速
     */
    public static StringBuilder getPaceStringBuilder(byte[] paceArr, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((paceArr[2 * i] & 0xff) + ((paceArr[2 * i + 1] & 0xff) << 8));
            if (i != length - 1) {
                sb.append(",");
            }
        }
        return sb;
    }

    /**
     * U8
     * 获取温度数据，单位：摄氏度
     *
     * @param tempArr
     * @param length
     * @return
     */
    public static StringBuilder getTempStringBuilder(byte[] tempArr, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((tempArr[i] & 0xff));
            if (i != length - 1) {
                sb.append(",");
            }
        }
        return sb;
    }

    /**
     * 获取步频数据 ，每分钟多少步
     *
     * @param paceArr
     * @param length
     * @return
     */
    public static StringBuilder getStepFreqStringBuidler(byte[] paceArr, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((paceArr[2 * i] & 0xff) + ((paceArr[2 * i + 1] & 0xff) << 8));
            if (i != length - 1) {
                sb.append(",");
            }
        }
        return sb;
    }

    /**
     * U 8
     * 获得心率数据U8
     *
     * @param heartArr
     * @param length
     * @return 心率, 心率
     */
    public static StringBuilder getHeartStringBuilder(byte[] heartArr, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(heartArr[i] & 0xff);
            if (i != length - 1) {
                sb.append(",");
            }
        }
        return sb;
    }

    /**
     * 获得字符串
     *
     * @param values
     * @param length
     * @return
     */
    public static StringBuilder getStringBuilder(byte[] values, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(values[i] & 0xff);
        }
        return sb;
    }

    /**
     * 将16进制字符串转为String
     *
     * @param values
     * @return
     */
    public static String byte2String(byte[] values) {
        String value = byte2HexStr(values);
        return hexStr2Str(value).trim();
    }

    /**
     * 十六进制转换字符串
     *
     * @param hexStr
     * @return
     */
    private static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    private String decode(String bytes) {
        String str = "0123456789ABCDEF";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        //将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((str.indexOf(bytes.charAt(i)) << 4 | str.indexOf(bytes.charAt(i + 1))));
        String bb = "";
        try {
            bb = new String(baos.toByteArray(), "GB2312");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bb;
    }

    /**
     * 16进制byte[]转为16进制字符串
     *
     * @param b
     * @return
     */
    private static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
        }
        return sb.toString().toUpperCase().trim();
    }
}

























