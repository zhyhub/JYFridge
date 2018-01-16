package com.joyoungdevlibrary.utils;

import android.content.Context;


import com.joyoungdevlibrary.interface_sdk.CallBack;
import com.joyoungdevlibrary.interface_sdk.CommandCallBack;

import java.io.UnsupportedEncodingException;

/**
 * Created by liuwei on 2017/11/30.
 */

public final class JoyoungDevLinkSDK {


    private static JoyoungDevLinkSDK joyoungLinkSDK = null;

    private JoyoungDevLinkSDK() {
    }

    /***
     * 单例模式，保证只有一个实例对象
     * @return
     */
    private synchronized static JoyoungDevLinkSDK getInstance() {
        if (joyoungLinkSDK == null) {
            joyoungLinkSDK = new JoyoungDevLinkSDK();
        }
        return joyoungLinkSDK;
    }

    /***
     * 设备发送指令
     * @param cmd
     */
    public static void sendCMD(String cmd) {
        try {
            JoyoungDevSDK.getInstance().sendCMD(cmd);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /***
     * 初始化
     * @param context 上下文
     * @param devTypeId  设备类型
     * @param mcu
     * @param commandCallBack  指令回调
     * @param callBack  连接服务器回调
     */
    public static void init(Context context, String devTypeId, String mcu, CommandCallBack commandCallBack, CallBack callBack) {
        JoyoungDevSDK.getInstance().regist(context, devTypeId, mcu, commandCallBack, callBack);
    }

    ;

    /***
     * 服务停止
     * 解除订阅
     */
    public static void onDestroy() {
        JoyoungDevSDK.getInstance().onDestroy();
    }

}
