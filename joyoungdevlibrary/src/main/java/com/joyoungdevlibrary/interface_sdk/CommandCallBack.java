package com.joyoungdevlibrary.interface_sdk;

public interface CommandCallBack {
    /***
     * 重新连接
     * @param msg
     */
    void connectionLost(String msg);

    /***
     * 返回的指令
     * @param msg  返回的解密后的指令
     */
    void messageArrived(String msg);

    /***
     * 连接失败的方法
     * @param token 返回的失败信息
     */
    void deliveryComplete(String token);
}
