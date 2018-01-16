package com.joyoungdevlibrary.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joyoungdevlibrary.config.Cons_Sdk;
import com.joyoungdevlibrary.http.JyHttp;
import com.joyoungdevlibrary.info.BaseRequesRes;
import com.joyoungdevlibrary.info.Device;
import com.joyoungdevlibrary.interface_sdk.CallBack;
import com.joyoungdevlibrary.interface_sdk.CommandCallBack;
import com.joyoungdevlibrary.service.JoyounglinkDevServers;
import com.joyoungdevlibrary.utils.encryptdecryptutil.XXTea;

import org.apaches.commons.codec.DecoderException;
import org.apaches.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liuwei on 2017/12/5.
 */

public class JoyoungDevSDK {

    private Context mContext;
    private CommandCallBack commandCallBack;
    private CallBack mCallBack;
    private static JoyoungDevSDK joyoungDevSDK = null;

    private JoyoungDevSDK() {
    }

    /***
     * 单例模式，保证只有一个实例对象
     * @return JoyoungDevSDK
     */
    synchronized static JoyoungDevSDK getInstance() {
        if (joyoungDevSDK == null) {
            joyoungDevSDK = new JoyoungDevSDK();
        }
        return joyoungDevSDK;
    }


    void regist(Context context, final String devTypeId, String mcu, CommandCallBack commandCallBack, final CallBack callBack) {
        this.mContext = context;
        this.commandCallBack = commandCallBack;
        this.mCallBack = callBack;
        Cons_Sdk.devTypeId = devTypeId;
        OkHttpClient okHttpClient = JyHttp.getInstance().getClient();
        String sign = RequesUtil.getSign("/cms/v1/dev/getMsgFromRms?mcu_ver=" + mcu + "&mode_ver=9&mac=" + RequesUtil.getmobile_id(context) + "&mode_code=1&dev_code=" + devTypeId + "&stamp=");
        String url = Cons_Sdk.BASE_URL_CMS_DEVICE + "/cms/v1/dev/getMsgFromRms?mcu_ver=" + mcu + "&mode_ver=9&mac=" + RequesUtil.getmobile_id(context) + "&mode_code=1&dev_code=" + devTypeId + "&stamp=&sign=" + sign;
        Request request = getRequest(url);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("regist", result);
                Gson gson = new Gson();
                BaseRequesRes<String> baseRequesRes = gson.fromJson(result, new TypeToken<BaseRequesRes<String>>() {
                }.getType());
                if (baseRequesRes.getCode() == 0) {
                    Cons_Sdk.UUID = baseRequesRes.getData();
                    getRsaKey(devTypeId);
                } else {
                    callBack.onError();
                }

            }
        });
    }


    private void getRsaKey(final String devTypeId) {
        if (devTypeId == null)
            return;
        OkHttpClient okHttpClient = JyHttp.getInstance().getClient();
        String url = Cons_Sdk.BASE_URL_CMS_DEVICE + "/cms/v1/dev/getRsaKey?param=appId=null&devTypeId=" + devTypeId + "&sign=null&stamp=null";
        Request request = getRequest(url);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mCallBack.onError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("getRSAKey", result);
                Gson gson = new Gson();
                BaseRequesRes<String> baseRequesRes = gson.fromJson(result, new TypeToken<BaseRequesRes<String>>() {
                }.getType());

                if (baseRequesRes.getCode() == 0) {
                    Cons_Sdk.RsaKey = baseRequesRes.getData();
                    upDevKey(devTypeId);
                } else {
                    mCallBack.onError();
                }

            }
        });
    }

    private void upDevKey(String devTypeId) {
        if (devTypeId == null) {
            return;
        }
        String xXTEA_key = generateShortUuid();
        String SIGN_key = generateShortUuid();
        Cons_Sdk.SIGN_key = SIGN_key;
        Cons_Sdk.XXTEA_key = xXTEA_key;
        String xXTEA_key_SIGN_key = xXTEA_key + ";" + SIGN_key;
        RSAPublicKey publicKey = RSAUtil.getPublicKey(Cons_Sdk.RsaKey, "10001");
        String encode = null;
        try {
            encode = RSAUtil.encryptByPublicKey(xXTEA_key_SIGN_key, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String xxteaHello_s = null;
        try {
            xxteaHello_s = Hex.encodeHexString(XXTea.encrypt("hello".getBytes(), xXTEA_key.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        OkHttpClient okHttpClient = JyHttp.getInstance().getClient();
        String url = Cons_Sdk.BASE_URL_CMS_DEVICE + "/cms/v1/dev/upDevKey?param=appId=&devId=" + Cons_Sdk.UUID + "&devTypeId=" + devTypeId + "&stamp=&hello=" + xxteaHello_s + "&encode=" + encode;

        Request request = getRequest(url);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("msg", e.getLocalizedMessage());
                mCallBack.onError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Gson gson = new Gson();
                BaseRequesRes<String> baseRequesRes = gson.fromJson(result, new TypeToken<BaseRequesRes<String>>() {
                }.getType());
                if (baseRequesRes.getCode() == 0) {
                    getPush();
                } else {
                    mCallBack.onError();
                }
            }
        });
    }

    private void getPush() {
        OkHttpClient okHttpClient = JyHttp.getInstance().getClient();
        String url_string = "/cms/v1/dev/getPush?devId=" + Cons_Sdk.UUID + "&appId=&stamp=";
        String sign = RequesUtil.getSign(url_string, "".getBytes(), Cons_Sdk.SIGN_key);
        String url = Cons_Sdk.BASE_URL_CMS_DEVICE + url_string + "&sign=" + sign;
        Log.e("url", url);
        okHttpClient.newCall(getRequest(url)).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mCallBack.onError();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String result = response.body().string();
                    Gson gson = new Gson();
                    BaseRequesRes<String> res = gson.fromJson(result, new TypeToken<BaseRequesRes<String>>() {
                    }.getType());
                    if (res != null && res.getCode() == 0 && res.getData() != null) {
                        String data = new String((XXTea.decrypt(Hex.decodeHex(res.getData().toCharArray()), Cons_Sdk.XXTEA_key.getBytes())));
                        Device device = gson.fromJson(data, new TypeToken<Device>() {
                        }.getType());
                        if (device != null) {
                            Cons_Sdk.device_dev = device;
                            if (mContext != null) {
                                start_service();
                            }
                        } else {
                            mCallBack.onError();
                        }
                    } else {
                        mCallBack.onError();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DecoderException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void sendCMD(String cmd) throws UnsupportedEncodingException {
        if (servers != null) {
            servers.sendCMD(cmd);
        } else {
            start_service();
        }
    }

    private void start_service() {
        Log.e("start_service", "start_service-----" + mContext);
        Intent intent = new Intent(mContext, JoyounglinkDevServers.class);
        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private JoyounglinkDevServers servers;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            servers = ((JoyounglinkDevServers.MyBind) service).getServers();
            servers.setCallback(mCallBack);
            servers.setOnMqttCallback(commandCallBack);
            Log.e("msg", "服务开启");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            servers = null;
            Log.e("msg", "服务停止");
        }
    };


    private Request getRequest(String url) {
        Request.Builder request = null;
        if (request == null) {
            request = new Request.Builder();
        }

        return request.url(url).build();
    }

    /**
     * 生成15位数的随机数
     *
     * @return String
     */
    public String generateShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.substring(0, 16);
    }

    public void onDestroy() {
        if (mContext != null && connection != null) {
            mContext.unbindService(connection);
        }
    }

}
