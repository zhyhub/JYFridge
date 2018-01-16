package com.joyoungdevlibrary.config;


import android.util.Log;

import com.joyoungdevlibrary.info.Device;
import com.joyoungdevlibrary.utils.DataUtils;

/**
 * Created by Joyoung on 2016/5/20.
 */
public class Cons_Sdk {
    public static String BASE_URL_CMS = "https://apitest.joyoung.com:8189";
    public static String sessionkey = null;
    public static String sessionId = null;
    public static String dataKey = null;
    public static String UUID=null;
    public static String RsaKey=null;
    public static String SIGN_key=null;
    public static String XXTEA_key=null;
    public static String BASE_URL_CMS_DEVICE = "http://apitest.joyoung.com:8188";
    /***
     * 指令解密钥匙
     */
    public static String decryptKey = null;
    public static Device device_dev=null;

    /***
     *  设备id
     */
    public static String deviceId = null;
    /***
     *  配置文件的路径
     */
    public static String configXmlName = null;
    /***
     *  项目类型
     */
    public static String source = "1";
    /***
     * clientId a client identifier that is unique on the server being connected to
     */
    public static String clientId = "1";
    /***
     * 是否需要解析命令
     *isResolve ==false 不解析
     *isResolve ==true 解析 需要赋值configXmlName的路径名称 否则会抛出异常
     */
    public static boolean isResolve = false;
    /***
     * 证书名
     */
    public static String sslName = null;

    /***
     * 设备型号
     */
    public static String devTypeId=null;

    public static String MCU;


    /***
     * UDP设备发现
     */
    public static byte[] UDP_FIND_MOUDLE = new byte[]{(byte) 0xcc, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xdd, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00};

    public static boolean checkCmsLoginRes(byte[] resCmd, int totalsize) {

        byte[] cmd = new byte[totalsize];
        //
        try {
            System.arraycopy(resCmd, 0, cmd, 0, totalsize);
            // result
            byte[] resByte = new byte[2];
            System.arraycopy(cmd, 30, resByte, 0, 2);
            String resultTag = DataUtils.bytesToHexString(resByte, 2);

            if (resultTag == null || !resultTag.equals("0000"))
                return false;
            else
                return true;

        } catch (Exception e) {
            Log.e("checkCmsLoginRes", e.getMessage());
        }

        return false;
    }


    public static String left0(String str) {
        int j = 4 - str.length();
        String tmp = "";
        for (int i = 0; i < j; i++) {
            tmp = tmp + "0";
        }

        return tmp + str;
    }

    public static byte[] hexIntU32(int i) {
        String b = Integer.toHexString(i);

        while (b.length() < 8) {
            b = "0" + b;
        }

        byte[] tb = new byte[4];

        int i1 = Integer.valueOf(b.substring(0, 2), 16);
        tb[0] = (byte) i1;
        int i2 = Integer.valueOf(b.substring(2, 4), 16);
        tb[1] = (byte) i2;
        int i3 = Integer.valueOf(b.substring(4, 6), 16);
        tb[2] = (byte) i3;
        int i4 = Integer.valueOf(b.substring(6, 8), 16);
        tb[3] = (byte) i4;
        return tb;

    }

    public static String getStringorByte(byte[] b) {
        String str = "";
        for (byte b1 : b) {
            str += b1;
        }
        return str;
    }
}
