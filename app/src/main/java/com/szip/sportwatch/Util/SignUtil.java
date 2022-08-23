package com.szip.sportwatch.Util;

import android.util.Log;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SignUtil {
    public static String FIELD_SIGN = "sign";

    public static final String HMACSHA256 = "HMAC-SHA256";

    public static final String MD5 = "MD5";

    /**
     * 生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
     *
     * @param data 待签名数据
     * @param signType 签名方式
     * @return 签名
     */
    public static String generateSignature(final Map<String, Object> data,String key, String signType) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals(FIELD_SIGN)) {
                continue;
            }
            if (data.get(k).toString().trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.get(k).toString().trim()).append("&");
        }
        sb.append("key=").append(key);
        Log.d("data******","拼接的字符串 = "+sb.toString());
        if (MD5.equals(signType)) {
            return MD5(MD5(sb.toString()).toUpperCase()).toUpperCase();
        }
        else {
            throw new Exception(String.format("Invalid sign_type: %s", signType));
        }
    }



    /**
     * 生成 MD5
     *
     * @param data 待处理数据
     * @return MD5结果
     */
    public static String MD5(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    public static String getRandomStr(){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random1=new Random();
        //指定字符串长度，拼接字符并toString
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i < 32; i++) {
            //获取指定长度的字符串中任意一个字符的索引值
             int number=random1.nextInt(str.length());
            //根据索引值获取对应的字符
            char charAt = str.charAt(number);
            sb.append(charAt);
        }
        return sb.toString();
    }

}
