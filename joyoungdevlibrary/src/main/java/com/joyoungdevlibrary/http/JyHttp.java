package com.joyoungdevlibrary.http;

import android.content.Context;

import com.joyoungdevlibrary.config.Cons_Sdk;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Joyoung on 2016/5/26.
 */
public class JyHttp {

    protected static final String TAG = JyHttp.class.getSimpleName();
    private MediaType MEDIA_TYPE_TXT = MediaType.parse("text/plain;charset=utf-8");
    private OkHttpClient mOkHttpClient;
    private Context context;
    private String ssName;
    private static JyHttp jyHttp;


    private JyHttp() {
    }

    public static JyHttp getInstance() {
        if (jyHttp == null) {
            jyHttp = new JyHttp();
        }
        return jyHttp;
    }

    private void getClientSSName() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder().sslSocketFactory(getSSLContext(context, ssName)).build();
        }
    }

    /***
     * 实例化OkHttpClient
     */
    public OkHttpClient getClient() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder().build();
        }
        return mOkHttpClient;
    }

    private Request.Builder getRequest(String source) {
        Request.Builder request = null;
        if (request == null) {
            request = new Request.Builder();
            request.header("source", source);
        }
        return request;
    }


    /****
     * 发送指令
     *
     * @param context  上下文
     * @param url  URL
     * @param sslName 证书路径和名称
     * @param params1 指令
     * @param source app类型
     * @param callback 回调
     */
    public void sendCMD(Context context, String url, String sslName, byte[] params1, String source, Callback callback) {
        this.context = context;
        this.ssName = sslName;
        if (ssName != null) {
            getClientSSName();
        } else {
            getClient();
        }
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_TXT, params1);
        Request request = getRequest(source).url(url).post(requestBody).build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    /****
     * 发送指令
     *
     * @param context  上下文
     * @param url  URL
     * @param sslName 证书路径和名称
     * @param params1 指令
     * @param source app类型
     * @param callback 回调
     */
    public void sendCMD(Context context, String url, String sslName, String params1, String source, Callback callback) {
        this.context = context;
        this.ssName = sslName;
        if (ssName != null) {
            getClientSSName();
        } else {
            getClient();
        }

        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_TXT, params1);
        Request request = getRequest(source).url(url).post(requestBody).build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    /****
     * 获取MQTT地址
     *
     * @param context  上下文
     * @param url  URL
     * @param sslName 证书路径和名称
     * @param callback 回调
     */
    public void getMqtt(Context context, String url, String sslName, Callback callback) {
        this.context = context;
        this.ssName = sslName;
        if (ssName != null) {
            getClientSSName();
        } else {
            getClient();
        }
        Request source = getRequest(Cons_Sdk.source).url(url).build();
        mOkHttpClient.newCall(source).enqueue(callback);
    }

    public javax.net.ssl.SSLSocketFactory getSSLContext(Context context, String sslName) {
        // 生成SSLContext对象
        try {
            InputStream inStream = context.getAssets().open(sslName);
            return setCertificates(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 从assets中加载证书
        return null;


    }

    public javax.net.ssl.SSLSocketFactory setCertificates(InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
