package com.joyoungdevlibrary.interface_sdk;

/**
 * Created by liuwei on 2017/10/20.
 */

public interface JoyoungLinkCallBack {
    /***
     *连接成功，可以切换设备了
     */
    void onSuccess();

    /***
     * 连接失败
     */
    void onError();
}
