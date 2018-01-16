package com.joyoungdevlibrary.interface_sdk;

/**
 * Created by liuwei on 2016/7/26.
 */
public interface CallBack {

    /***
     *MQTT连接成功
     */
    void onSuccess();

    /***
     * MQTT连接失败
     */
    void onError();
}
