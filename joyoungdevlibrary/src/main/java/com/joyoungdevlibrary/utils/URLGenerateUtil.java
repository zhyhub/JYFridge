package com.joyoungdevlibrary.utils;


import com.joyoungdevlibrary.config.Cons_Sdk;
import com.joyoungdevlibrary.utils.encryptdecryptutil.MakeUUID;
import com.joyoungdevlibrary.utils.encryptdecryptutil.XXTea;

/**
 * Created by Joyoung on 2016/5/23.
 */
public class URLGenerateUtil {


    public static String GenerateSing(String baseUrl, String param, long stamp) throws Exception {
        baseUrl += "&sessionId=" + Cons_Sdk.sessionId + "&msgId=1&stamp=" + stamp + "&enc=2";
        byte[] pdata = param.getBytes();
        byte[] xxtea = XXTea.encrypt(pdata, Cons_Sdk.dataKey.getBytes("utf-8"));
        String sign = RequesUtil.getSign(baseUrl, xxtea, Cons_Sdk.sessionkey);
        return sign;
    }


    /***
     * 生成RMS 的URL
     * @param baseUrl 请求的URL如（/rms/v1/app/homePage?action=queryShssList）
     * @param param 请求的参数
     * @return 生成一个新的URL
     */
    public static String urlGenerateRMS(String baseUrl, String param) {
        long stamp = System.currentTimeMillis();
        baseUrl += "&sessionId=" + Cons_Sdk.sessionId + "&msgId=1&stamp=" + stamp + "&enc=2";
        if (param == null) {
            param = "";
        }
        byte[] pdata = param.getBytes();
        byte[] xxtea = new byte[0];
        if (Cons_Sdk.dataKey != null) {
            try {
                xxtea = XXTea.encrypt(pdata, Cons_Sdk.dataKey.getBytes("utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String sign = "";
        if (Cons_Sdk.sessionkey != null) {
            sign = RequesUtil.getSign(baseUrl, xxtea, Cons_Sdk.sessionkey);
        }
        baseUrl += "&sign=" + sign;
        return Cons_Sdk.BASE_URL_CMS + baseUrl;
    }

    /***
     * @param baseUrl 请求的URL如（/rms/v1/app/homePage?action=queryShssList）
     * @param param 请求的参数
     * @return  返回生成的一个新的URL
     */
    public static String noSessionUrl(String baseUrl, String param) {
        //生成签名
        byte[] pdata = null;
        if (param != null && !"".equals(param)) {
            pdata = param.getBytes();
        }
        long stamp = System.currentTimeMillis();
        //&enc=0 不加密；&enc=1 AESCoder密；&enc=2 xxtea加密
        baseUrl = baseUrl + "&msgId=1&stamp=" + stamp + "&enc=0";
        String sign = RequesUtil.getSign(baseUrl, pdata, "");
        baseUrl += "&sign=" + sign;
        baseUrl = Cons_Sdk.BASE_URL_CMS + baseUrl;
        return baseUrl;
    }

    /***
     * 获得没有sessionkey加密后的参数
     * @param param 请求参数的JSON字符串
     * @return 加密后的参数
     */
    public static String noSessionParma(String param) {
        byte[] pdata = null;
        if (param != null && !"".equals(param)) {
            pdata = param.getBytes();
            return new String(MakeUUID.base64Encode(pdata));
        }
        return null;
    }

    /***
     * 获得加密后的参数
     * @param param 请求参数的JSON字符串
     * @return 加密后的参数
     */
    public static String getParam(String param) {

        if (Cons_Sdk.dataKey == null || param == null || param.equals("")) {
            return null;
        }
        try {
            byte[] pdata = null;
            pdata = param.getBytes();
            byte[] xxTea = null;

            xxTea = XXTea.encrypt(pdata, Cons_Sdk.dataKey.getBytes("UTF-8"));
            return MakeUUID.base64Encode(xxTea);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /***
     *生成CMS的URL
     * @param baseUrl 请求的URL如（/cms/v1/app/controlDev）
     * @param param 请求的参数
     * @return 生成一个新的URL
     */
    public static String urlGenerateCMS(String baseUrl, String param) {
        long stamp = System.currentTimeMillis();
        baseUrl += "?sessionId=" + Cons_Sdk.sessionId + "&msgId=1&stamp=" + stamp + "&enc=2";
        String sign = null;
        if (param != null) {
            byte[] pdata = param.getBytes();
            byte[] xxtea = new byte[0];
            try {
                xxtea = XXTea.encrypt(pdata, Cons_Sdk.dataKey.getBytes("utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            sign = RequesUtil.getSign(baseUrl, xxtea, Cons_Sdk.sessionkey);
        } else {
            sign = RequesUtil.getSign(baseUrl, null, Cons_Sdk.sessionkey);
        }
        baseUrl += "&sign=" + sign;

        return Cons_Sdk.BASE_URL_CMS + baseUrl;
    }
}
