package com.joyoungdevlibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joyoungdevlibrary.config.CommandConst;
import com.joyoungdevlibrary.config.Cons_Sdk;
import com.joyoungdevlibrary.http.JyHttp;
import com.joyoungdevlibrary.info.BaseRequesRes;
import com.joyoungdevlibrary.info.Data;
import com.joyoungdevlibrary.info.Device;
import com.joyoungdevlibrary.interface_sdk.CallBack;
import com.joyoungdevlibrary.interface_sdk.CommandCallBack;
import com.joyoungdevlibrary.utils.DataUtils;
import com.joyoungdevlibrary.utils.RequesUtil;
import com.joyoungdevlibrary.utils.encryptdecryptutil.XXTea;

import org.apaches.commons.codec.DecoderException;
import org.apaches.commons.codec.binary.Hex;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liuwei on 2017/12/5.
 */

public class JoyounglinkDevServers extends Service {

    private String upTopic;
    private String downTopic;
    private String uplinkTopic;

    private String userName;
    private String passWord;
    private String host;
    private String port;
    private MqttClient client;
    private MqttConnectOptions options;

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    private static final int CONNECT = 2;
    private static final int RESTART_CONNECT = 3;
    private static final int MQTTDATA = 5;
    private static final int RESTART_CONNECT_MQTTDATA = 10;
    private static int CONNECTTIME = 10;
    private boolean isRConnect = true;
    private String devTypeId;
    private String devID;
    private String mcu;


    private MyMQTTHandler mQttHandler;


    private CallBack callBack;
    private CommandCallBack mCommandCallBack;


    /***
     * MQTT返回信息的回调
     *
     * @param commandCallBack
     */
    public void setOnMqttCallback(CommandCallBack commandCallBack) {
        this.mCommandCallBack = commandCallBack;
    }

    /***
     * MQTT连接成功或失败的回调
     *
     * @param callback
     */
    public void setCallback(CallBack callback) {
        this.callBack = callback;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new MyBind();
    }

    /***
     *返回一个Service的实例
     *
     */
    public class MyBind extends Binder {
        public JoyounglinkDevServers getServers() {
            return JoyounglinkDevServers.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        devID = Cons_Sdk.UUID;
        devTypeId = Cons_Sdk.devTypeId;
        executorService = Executors.newScheduledThreadPool(1000);
        scheduledExecutorService = Executors.newScheduledThreadPool(1000);

        if (Cons_Sdk.clientId.equals("1")) {
            Cons_Sdk.clientId = UUID.randomUUID().toString();
        }
        init();
    }


    /***
     * 发送指令
     * @param cmd
     */
    public void sendCMD(String cmd) throws UnsupportedEncodingException {
        //FAFB 0 1 00 00 00 00b7 0006 40 01 01 02 02 00 A6CB
        //FAFB01000000CCC0000400101020200A6CB
        //[85, -86, -64, 0, 32, 108, 106, 82, 10, 10, 10, 118, 118, 118, 11, 19, 14, 12, 10, 0, 0, 0, 0, 0, 0, 0, 3, 48, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        uplinkTopic = "/jy/cms/" + devTypeId + "/upLink/" + devID;
        String ctrl = cmd.substring(10, 12);
        mcu=cmd.substring(8,10);
        String changeTitle = cmd.substring(20, cmd.length()).trim();
        Log.e("changeTitle", changeTitle);
        String title = getTitle() + changeTitle;
        Log.e("changeTitle", title);
        String ord = title.substring(20, 24);
        Data data = null;
        if (Integer.parseInt(ord, 16) >= Integer.parseInt("6001", 16) && Integer.parseInt(ord, 16) <= Integer.parseInt("81FF", 16)) {
            data = new Data(devID, devTypeId, title, "rmsReq", "" + seq, ord);
        } else {
            switch (ctrl) {
                case "80":
                    data = new Data(devID, devTypeId, title, "ctrlResp", "" + seq, ord);
                    break;
                case "40":
                case "00":
                    data = new Data(devID, devTypeId, title, "devInfo", "" + seq, ord);
                    break;
            }

        }
        MqttMessage mqttMessage = new MqttMessage();
        String json = new Gson().toJson(data);
        Log.e("发送的指令", json);
        try {
            byte[] bytes = XXTea.encrypt(json.getBytes("utf-8"), Cons_Sdk.XXTEA_key.getBytes());
            mqttMessage.setPayload(bytes);
            client.publish(upTopic, mqttMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***
     * 订阅主题
     *
     * @param devTypeId 设备类型
     * @param devID     设备ID
     */
    public void subscription(String devTypeId, String devID) {
        Log.e("订阅：", devTypeId + "----" + devID);
        try {
            downTopic = "/jy/app/" + devTypeId + "/upLink/" + devID;
            upTopic = "/jy/dev/" + devTypeId + "/downLink/" + devID;
            if (client != null) {
                client.subscribe(new String[]{downTopic, upTopic}, new int[]{0, 0});
                Log.e("订阅主题", downTopic);
                Log.e("订阅主题", upTopic);

            } else {
                mHandler.sendEmptyMessage(RESTART_CONNECT_MQTTDATA);
            }

        } catch (MqttException e) {
            e.printStackTrace();
            Log.e("msg", "订阅失败");
        }
    }

    private void init() {
        if (Cons_Sdk.device_dev != null) {
            Log.e("msg", "init");
            userName = Cons_Sdk.device_dev.getUser();
            passWord = Cons_Sdk.device_dev.getPass();
            host = "tcp://" + Cons_Sdk.device_dev.getAddr();
            port = Cons_Sdk.device_dev.getPort();
            if (TextUtils.isEmpty(userName) || userName.equals("") || TextUtils.isEmpty(passWord) || passWord.equals("") || TextUtils.isEmpty(host) || host.equals("") || TextUtils.isEmpty(port) || port.equals("")) {
                if (mQttHandler == null) {
                    Looper.prepare();
                    mQttHandler = new MyMQTTHandler();
                    mQttHandler.sendEmptyMessage(RESTART_CONNECT);
                    Looper.loop();
                } else {
                    mQttHandler.sendEmptyMessage(RESTART_CONNECT);
                }
            } else {
                host = host + ":" + port;
                try {
                    client = new MqttClient(host, Cons_Sdk.clientId, new MemoryPersistence());
                } catch (MqttException e) {
                    mHandler.sendEmptyMessage(RESTART_CONNECT_MQTTDATA);
                    return;
                }
                setMqttConnectOptions();
                connect();
            }
        } else {
            initMqtt();
        }
    }


    /**
     * 连接
     */
    private void connect() {
        Log.e("msg", "connect");
        executorService.execute(runnable);
    }


    /***
     * 10秒判断连接的线程
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (client != null) {
                    client.connect(options);
                    if (client.isConnected()) {
                        client.setCallback(callback);
                        Message msg = new Message();
                        msg.what = CONNECT;
                        mHandler.sendMessage(msg);
                    } else {
                        connect();
                    }
                } else {
                    mHandler.sendEmptyMessage(RESTART_CONNECT_MQTTDATA);
                }


            } catch (MqttException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = RESTART_CONNECT;
                mHandler.sendMessage(msg);
            }
        }
    };


    private MqttCallback callback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            mCommandCallBack.connectionLost(cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage messages) throws Exception {
            if (Cons_Sdk.XXTEA_key != null) {
                try {
                    String message = new String(XXTea.decrypt(messages.getPayload(), Cons_Sdk.XXTEA_key.getBytes()));
                    Log.e("topic", topic + "");
                    Log.e("message", message + "");
                    JSONObject jsonObject = new JSONObject(message);
                    String func = jsonObject.optString("func");
                    if (func != null && !func.equals("") && func.equals("devKeepResp")) {
                        number++;
                    } else {
                        String data = jsonObject.optString("data");
                        Log.e("messageArrived1",data);
                        ArrayList<String> cmdArray = DataUtils.bytesToHexStringArray2(data);
                        String valueint_String = cmdArray.get(10) + cmdArray.get(11);
                        switch (valueint_String) {
                            case "ff0a":
                                break;
                            case "ff06":
                                break;
                            default:
                                String cmd = data.substring(14, data.length());
                                String title = getTitle(valueint_String);
                                String newData = title + cmd;
                                mCommandCallBack.messageArrived(newData);
                                break;
                        }
                    }
                } catch (Throwable throwable) {
                    String str = throwable.getMessage();
                    Log.e("error is Log", str);
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            try {
                mCommandCallBack.deliveryComplete(token.getResponse().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private String getTitle(String contral) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FAFB");
        stringBuilder.append("00");
        stringBuilder.append("00");
        stringBuilder.append("00");
        stringBuilder.append(contral);
        return stringBuilder.toString();
    }

    private String getTitle() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CC00");//包起始标志
        stringBuilder.append("0001");//	通讯协议版本号
        stringBuilder.append("00");//加密类型
        stringBuilder.append("0020");//链路数据密文包长度
        if (mcu!=null){
            stringBuilder.append(mcu);//CRU（F）指令
        }else {
            stringBuilder.append("00");//CRU（F）指令
        }

        String devTypeId = Cons_Sdk.devTypeId;
        String hexString = DataUtils.bytesToHexString(CommandConst.hexIntU16(Integer.parseInt(devTypeId)), CommandConst.hexIntU16(Integer.parseInt(devTypeId)).length);
        stringBuilder.append(hexString);
        return stringBuilder.toString().trim();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT:
                    subscription(devTypeId, devID);
                    sendHeartbeat();
                    callBack.onSuccess();
                    if (isRConnect) {
                        startReconnection();
                        isRConnect = false;
                    }
                    break;
                case RESTART_CONNECT:
                    if (isRConnect) {
                        startReconnection();
                        isRConnect = false;
                    }
                    callBack.onError();
                    break;
                case RESTART_CONNECT_MQTTDATA:
                    initMqtt();
                    break;
            }

        }

    };

    private int number = 3;
    private int seq = 0;

    private void sendHeartbeat() {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                uplinkTopic = "/jy/cms/" + devTypeId + "/upLink/" + devID;
                MqttMessage message = new MqttMessage();
                Gson gson = new Gson();
                Data data = new Data(devID, devTypeId, "devKeep", "" + seq++);
                String json = gson.toJson(data);
                Log.e(uplinkTopic, json);
                try {
                    byte[] bytes = XXTea.encrypt(json.getBytes("utf-8"), Cons_Sdk.XXTEA_key.getBytes());
                    message.setPayload(bytes);
                    if (number > 0) {
                        client.publish(uplinkTopic, message);
                        number--;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MqttPersistenceException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 1 * 1000, Integer.parseInt(Cons_Sdk.device_dev.getKeepalive()) * 1000, TimeUnit.MILLISECONDS);
    }


    private void createHandler() {
        if (mQttHandler == null) {
            Looper.prepare();
            mQttHandler = new MyMQTTHandler();
            mQttHandler.sendEmptyMessage(RESTART_CONNECT);
            Looper.loop();
        } else {
            mQttHandler.sendEmptyMessage(RESTART_CONNECT);
        }

    }


    private class MyMQTTHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MQTTDATA:
                    init();
                    break;
                case RESTART_CONNECT:
                    try {
                        Thread.sleep(CONNECTTIME * 1000);
                        CONNECTTIME += CONNECTTIME;
                        initMqtt();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;

            }
        }
    }

    private void initMqtt() {
        OkHttpClient okHttpClient = JyHttp.getInstance().getClient();
        String url_string = "/cms/v1/dev/getPush?devId=" + Cons_Sdk.UUID + "&appId=&stamp=";
        String sign = RequesUtil.getSign(url_string, "".getBytes(), Cons_Sdk.SIGN_key);
        String url = Cons_Sdk.BASE_URL_CMS_DEVICE + url_string + "&sign=" + sign;
        Log.e("url", url);
        okHttpClient.newCall(getRequest(url)).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Gson gson = new Gson();
                BaseRequesRes<String> res = gson.fromJson(result, new TypeToken<BaseRequesRes<String>>() {
                }.getType());
                if (res != null && res.getCode() == 0 && res.getData() != null) {
                    String data = null;

                    try {
                        data = new String((XXTea.decrypt(Hex.decodeHex(res.getData().toCharArray()), Cons_Sdk.XXTEA_key.getBytes())));
                    } catch (DecoderException e) {
                        e.printStackTrace();
                    }

                    Log.e("mqtt地址", data);
                    Device device = gson.fromJson(data, new TypeToken<Device>() {
                    }.getType());
                    if (device != null) {
                        Cons_Sdk.device_dev = device;
                        if (mQttHandler == null) {
                            Looper.prepare();
                            mQttHandler = new MyMQTTHandler();
                            mQttHandler.sendEmptyMessage(MQTTDATA);
                            Looper.loop();
                        } else {
                            mQttHandler.sendEmptyMessage(MQTTDATA);
                        }
                    } else {
                        createHandler();
                    }
                } else {
                    createHandler();
                }
            }
        });

    }


    /***
     * mqtt连接设置
     *
     * @param
     */
    private void setMqttConnectOptions() {
        //MQTT的连接设置
        options = new MqttConnectOptions();
        //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
        options.setCleanSession(false);
        //设置连接的用户名
        options.setUserName(userName);
        //设置连接的密码
        options.setPassword(passWord.toCharArray());
        // 设置超时时间 单位为秒
        options.setConnectionTimeout(10);
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        options.setKeepAliveInterval(20);
    }


    private Request getRequest(String url) {
        Request.Builder request = null;
        if (request == null) {
            request = new Request.Builder();
        }

        return request.url(url).build();
    }

    /**
     * 重新连接
     */
    private void startReconnection() {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (client == null || !client.isConnected()) {
                    if (client == null) {
                        mHandler.sendEmptyMessage(RESTART_CONNECT_MQTTDATA);
                    } else {
                        connect();
                    }
                } else if (client != null) {
                    Log.e("msg", "mqtt连接成功");
                }
            }
        }, 1000, 30 * 1000, TimeUnit.MILLISECONDS);
    }


    /***
     * 解除订阅主题，在重新订阅之前必须解除订阅
     */
    public void unSubscription() {
        try {
            if (client != null && upTopic != null && uplinkTopic != null&&downTopic!=null) {
                client.unsubscribe(new String[]{upTopic, uplinkTopic,downTopic});
                Log.e("解除订阅主题", upTopic);
                Log.e("解除订阅主题", uplinkTopic);
                Log.e("解除订阅主题", downTopic);
            } else {
                mHandler.sendEmptyMessage(RESTART_CONNECT_MQTTDATA);
            }
        } catch (MqttException e) {
            Log.e("解除订阅主题失败", e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (scheduledExecutorService != null)
                scheduledExecutorService.shutdown();
            if (executorService != null)
                executorService.shutdown();
            if (downTopic == null || uplinkTopic == null||upTopic==null) {
            } else {
                unSubscription();
            }
            if (mHandler != null && runnable != null)
                mHandler.removeCallbacks(runnable);
            if (mQttHandler != null)
                mQttHandler.removeCallbacks(null);
            if (null != client && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
