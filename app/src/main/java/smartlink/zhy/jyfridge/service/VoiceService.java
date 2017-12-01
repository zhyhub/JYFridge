package smartlink.zhy.jyfridge.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;
import com.signway.SignwayManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import smartlink.zhy.jyfridge.ConstantPool;
import smartlink.zhy.jyfridge.R;
import smartlink.zhy.jyfridge.bean.BaseEntity;
import smartlink.zhy.jyfridge.json.JsonParser;
import smartlink.zhy.jyfridge.utils.BaseCallBack;
import smartlink.zhy.jyfridge.utils.BaseOkHttpClient;
import smartlink.zhy.jyfridge.utils.L;

/**
 * 唤醒android板   调用讯飞语音
 */

public class VoiceService extends AccessibilityService {

    private static String TAG = VoiceService.class.getSimpleName();
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private SpeechSynthesizer mTts;

    /**
     * 串口接入
     */

    private byte MODE;

    private byte DATA_0 = ConstantPool.Data0_beginning_commend;
    private byte DATA_1 = ConstantPool.Data1_beginning_commend;
    private byte DATA_2 = ConstantPool.Zero;
    private byte DATA_3 = ConstantPool.Zero;
    private byte DATA_4 = ConstantPool.Zero;
    private byte DATA_5 = ConstantPool.Zero;
    private byte DATA_6 = ConstantPool.Default;
    private byte DATA_7 = ConstantPool.Default;
    private byte DATA_8 = ConstantPool.Default;
    private byte DATA_9 = ConstantPool.Zero;
    private byte DATA_10 = ConstantPool.Zero;
    private byte DATA_11 = ConstantPool.Zero;
    private byte DATA_12 = ConstantPool.Zero;
    private byte DATA_13 = ConstantPool.Zero;
    private byte DATA_14 = ConstantPool.Zero;
    private byte DATA_15 = ConstantPool.Zero;
    private byte DATA_16 = ConstantPool.Zero;
    private byte DATA_17 = ConstantPool.Zero;
    private byte DATA_18 = ConstantPool.Zero;
    private byte DATA_19 = ConstantPool.Zero;
    private byte DATA_20 = ConstantPool.Zero;
    private byte DATA_21 = ConstantPool.Zero;
    private byte DATA_22 = ConstantPool.Zero;
    private byte DATA_23;


    private byte DATA_24 = ConstantPool.Zero;
    private byte DATA_25 = ConstantPool.Zero;
    private byte DATA_26 = ConstantPool.Zero;
    private byte DATA_27 = ConstantPool.Zero;
    private byte DATA_28 = ConstantPool.Zero;
    private byte DATA_29 = ConstantPool.Zero;
    private byte DATA_30 = ConstantPool.Zero;
    private byte DATA_31 = ConstantPool.Zero;
    private byte DATA_32 = ConstantPool.Zero;
    private byte DATA_33 = ConstantPool.Zero;
    private byte DATA_34 = ConstantPool.Zero;
    private byte DATA_35 = ConstantPool.Zero;
    private byte DATA_36 = ConstantPool.Zero;
    private byte DATA_37 = ConstantPool.Zero;
    private byte DATA_38 = ConstantPool.Zero;
    private byte DATA_39 = ConstantPool.Zero;
    private byte DATA_40 = ConstantPool.Zero;
    private byte DATA_41 = ConstantPool.Zero;
    private byte DATA_42 = ConstantPool.Zero;
    private byte DATA_43 = ConstantPool.Zero;
    private byte DATA_44 = ConstantPool.Zero;
    private byte DATA_45 = ConstantPool.Zero;
    private byte DATA_46;

    private byte[] data = new byte[]{};

    boolean isWrite = false;
    SignwayManager mSignwayManager = null;
    int MAX_SIZE = 100;
    byte[] rbuf = new byte[MAX_SIZE];

    Handler writeHandler = null;
    Handler readHandler = null;
    Runnable writeUpdate = null;
    Runnable readUpdate = null;

    int readLength;
    int fid = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(VoiceService.this, mInitListener);
        mTts = SpeechSynthesizer.createSynthesizer(VoiceService.this, mTtsInitListener);

        initUart();
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            L.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            }
        }
    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成");
                // 移动数据分析，收集开始听写事件
                FlowerCollector.onEvent(VoiceService.this, "iat_recognize");
                mIatResults.clear();
                // 设置参数
                setParam();
                ret = mIat.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("听写失败,错误码：" + ret);
                } else {
                    showTip(getString(R.string.text_begin));
                }
            } else {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            L.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {
        L.d(TAG, "Interrupt");
    }

    int ret = 0; // 函数调用返回值
    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_F1:
                //接受到f1信号，设备已经被唤醒，调用讯飞语音识别
                L.e(TAG, "接受到f1信号，设备已经被唤醒，调用讯飞语音识别");
                // 移动数据分析，收集开始合成事件
                FlowerCollector.onEvent(VoiceService.this, "tts_play");
                // 设置参数
                setTtsParam();
                int code = mTts.startSpeaking("有什么吩咐", mTtsListener);
                /*
                 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
		         * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
		        */
                if (code != ErrorCode.SUCCESS) {
                    showTip("语音合成失败,错误码: " + code);
                }
                break;
        }
        return super.onKeyEvent(event);
    }

    private void showTip(final String str) {
        L.e(TAG, str);
    }

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            L.d(TAG, results.getResultString());

            String msg = printResult(results);

            if (isLast) {
                // TODO 最后的结果
                L.e(TAG, "msg    " + msg);

                sendMsg(msg);

//                if (!msg.equals("")) {
//                    Intent test = new Intent();
//                    test.setAction("smartlink.zhy.jyfridge.service");
//                    test.putExtra("txt", msg);
//                    sendBroadcast(test);
//                    Log.e(TAG, "VoiceService  广播发送了   " + msg);
//                }
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private String printResult(RecognizerResult results) {
        L.e(TAG, "printResult");
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        L.e(TAG, "TAG   printResult " + resultBuffer.toString());

        return resultBuffer.toString();
    }

    /**
     * 语音合成参数设置
     */
    private void setTtsParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            String voicer = "nannan";
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, "55");
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, "60");
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, "100");
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /*
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    /**
     * 参数设置
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    @Override
    public void onDestroy() {
        Intent sevice = new Intent(this, VoiceService.class);
        this.startService(sevice);
        super.onDestroy();
        writeHandler.removeCallbacks(writeUpdate);//停止指令
        readHandler.removeCallbacks(readUpdate);//停止指令
    }

    //=============================================================  下面是请求海知语音获取意图  ======================================================================================================

    private void sendMsg(String txt) {
        BaseOkHttpClient.newBuilder()
                .addParam("q", txt)
                .addParam("app_key", ConstantPool.APP_KEY)
                .addParam("user_id", "123456")
                .get()
                .url(ConstantPool.BASE_RUYI + "v1/message")
                .build().enqueue(new BaseCallBack() {
            @Override
            public void onSuccess(Object o) {
                L.e(TAG, "onSuccess" + o.toString());
                Gson gson = new Gson();
                BaseEntity entity = gson.fromJson(o.toString(), BaseEntity.class);
                Map<String, Object> map = entity.getResult().getIntents().get(0).getParameters();
                if (map != null) {
                    for (String key : map.keySet()) {                        //遍历取出key，再遍历map取出value。
                        L.e(TAG, "key  " + key);
                        L.e(TAG, "map.get(key).toString()  " + map.get(key).toString());

                        if (map.get(key).toString().equals("mode")) {//模式设置  关闭quit    打开set
                            for (String newKey1 : map.keySet()) {
                                if (newKey1.equals("attr_value")) {
                                    switch (map.get(newKey1).toString()) {
                                        case "智能模式":
                                            for (String newKey2 : map.keySet()) {
                                                if (newKey2.equals("operation")) {
                                                    if (map.get(newKey2).toString().equals("set")) {//打开智能模式
                                                        MODE |= ConstantPool.Intelligent_Model;
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "打开智能模式   MODE" + MODE);
                                                    } else if (map.get(newKey2).toString().equals("quit")) {//关闭智能模式
                                                        MODE &= (~ConstantPool.Intelligent_Model);
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "关闭智能模式   MODE" + MODE);
                                                    }
                                                }
                                            }
                                            break;
                                        case "假日模式":
                                            for (String newKey2 : map.keySet()) {
                                                if (newKey2.equals("operation")) {
                                                    if (map.get(newKey2).toString().equals("set")) {//打开智能模式
                                                        MODE |= ConstantPool.Holiday_Mode;
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "打开假日模式   MODE" + MODE);
                                                    } else if (map.get(newKey2).toString().equals("quit")) {//关闭智能模式
                                                        MODE &= (~ConstantPool.Holiday_Mode);
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "关闭假日模式   MODE" + MODE);
                                                    }
                                                }
                                            }
                                            break;
                                        case "速冻模式":
                                            for (String newKey2 : map.keySet()) {
                                                if (newKey2.equals("operation")) {
                                                    if (map.get(newKey2).toString().equals("set")) {//打开智能模式
                                                        MODE |= ConstantPool.Quick_Freezing_Mode;
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "打开速冻模式   MODE" + MODE);
                                                    } else if (map.get(newKey2).toString().equals("quit")) {//关闭智能模式
                                                        MODE &= (~ConstantPool.Quick_Freezing_Mode);
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "关闭速冻模式   MODE" + MODE);
                                                    }
                                                }
                                            }
                                            break;
                                        case "速冷模式":
                                            for (String newKey2 : map.keySet()) {
                                                if (newKey2.equals("operation")) {
                                                    if (map.get(newKey2).toString().equals("set")) {//打开智能模式
                                                        MODE |= ConstantPool.Quick_Cooling_Mode;
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "打开速冷模式   MODE" + MODE);
                                                    } else if (map.get(newKey2).toString().equals("quit")) {//关闭智能模式
                                                        MODE &= (~ConstantPool.Quick_Cooling_Mode);
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "关闭速冷模式   MODE" + MODE);
                                                    }
                                                }
                                            }
                                            break;
                                        case "净味模式":
                                            for (String newKey2 : map.keySet()) {
                                                if (newKey2.equals("operation")) {
                                                    if (map.get(newKey2).toString().equals("set")) {//打开智能模式
                                                        MODE |= ConstantPool.LECO_Mode;
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "打开净味模式   MODE" + MODE);
                                                    } else if (map.get(newKey2).toString().equals("quit")) {//关闭智能模式
                                                        MODE &= (~ConstantPool.LECO_Mode);
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "关闭净味模式   MODE" + MODE);
                                                    }
                                                }
                                            }
                                            break;
                                        case "变温关闭模式":
                                            for (String newKey2 : map.keySet()) {
                                                if (newKey2.equals("operation")) {
                                                    if (map.get(newKey2).toString().equals("set")) {//打开智能模式
                                                        MODE |= ConstantPool.BianWen_Shutdown_Model;
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "打开变温关闭模式   MODE" + MODE);
                                                    } else if (map.get(newKey2).toString().equals("quit")) {//关闭智能模式
                                                        MODE &= (~ConstantPool.BianWen_Shutdown_Model);
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "关闭变温关闭模式   MODE" + MODE);
                                                    }
                                                }
                                            }
                                            break;
                                        case "冷藏关闭模式":
                                            for (String newKey2 : map.keySet()) {
                                                if (newKey2.equals("operation")) {
                                                    if (map.get(newKey2).toString().equals("set")) {//打开智能模式
                                                        MODE |= ConstantPool.LengCang_Shutdown_Model;
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "打开冷藏关闭模式   MODE" + MODE);
                                                    } else if (map.get(newKey2).toString().equals("quit")) {//关闭智能模式
                                                        MODE &= (~ConstantPool.LengCang_Shutdown_Model);
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "关闭冷藏关闭模式   MODE" + MODE);
                                                    }
                                                }
                                            }
                                            break;
                                        case "童锁模式":
                                            for (String newKey2 : map.keySet()) {
                                                if (newKey2.equals("operation")) {
                                                    if (map.get(newKey2).toString().equals("set")) {//打开智能模式
                                                        MODE |= ConstantPool.Child_Lock_Mode;
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "打开童锁模式   MODE" + MODE);
                                                    } else if (map.get(newKey2).toString().equals("quit")) {//关闭智能模式
                                                        MODE &= (~ConstantPool.Child_Lock_Mode);
                                                        DATA_0 = ConstantPool.Data0_beginning_commend;
                                                        DATA_1 = ConstantPool.Data1_beginning_commend;
                                                        DATA_2 = ConstantPool.Data2_Modify_Mode;
                                                        DATA_9 = MODE;
                                                        sendByte();
                                                        Log.e(TAG, "关闭童锁模式   MODE" + MODE);
                                                    }
                                                }
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
                mTts.startSpeaking(entity.getResult().getIntents().get(0).getOutputs().get(1).getProperty().getText(), mTtsListener);
            }

            @Override
            public void onError(int code) {
                L.e(TAG, "onError");
            }

            @Override
            public void onFailure(Call call, IOException e) {
                L.e(TAG, "onFailure");
            }
        });
    }
//=============================================================  上面是请求海知语音获取意图  ======================================================================================================


//=============================================================  下面是串口调用逻辑  ======================================================================================================

    private void initUart() {
        mSignwayManager = SignwayManager.getInstatnce();
        if (fid < 0) {
            fid = mSignwayManager.openUart("dev/ttyS2", 9600);
        }

        writeHandler = new Handler();
        writeUpdate = new Runnable() {
            @Override
            public void run() {
                Log.e("TAG", "读");
                readTTyDevice();
                writeHandler.postDelayed(writeUpdate, 1000); //1秒后再调用
            }
        };
        writeHandler.post(writeUpdate);

        readHandler = new Handler();
        readUpdate = new Runnable() {
            @Override
            public void run() {
                Log.e("TAG", "写");
                DATA_2 = (byte) 0x04;
                sendByte();
                readHandler.postDelayed(readUpdate, 2000); //1秒后再调用
            }
        };
        readHandler.post(readUpdate);
    }

    private void sendByte() {
        DATA_23 = (byte) (DATA_0 + DATA_1 + DATA_2 + DATA_3 + DATA_4 + DATA_5 + DATA_6
                + DATA_7 + DATA_8 + DATA_9 + DATA_10 + DATA_11 + DATA_12 + DATA_13 + DATA_14
                + DATA_15 + DATA_16 + DATA_17 + DATA_18 + DATA_19 + DATA_20 + DATA_21 + DATA_22);
        data = new byte[]{DATA_0, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, DATA_11, DATA_12, DATA_13, DATA_14, DATA_15, DATA_16, DATA_17, DATA_18, DATA_19, DATA_20, DATA_21, DATA_22, DATA_23};
        writeTTyDevice(fid, data);
    }

    /**
     * 读串口数据
     */
    public void readTTyDevice() {
        if (fid < 0) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isWrite) {
                    continue;
                }
                readLength = mSignwayManager.readUart(fid, rbuf, rbuf.length);
                if (readLength > 47) {
                    setNewData(rbuf);
                }
            }
        }).start();
    }

    /**
     * 想串口写数据
     */
    public void writeTTyDevice(final int fid, final byte[] buf) {
        if (fid < 0) {
            return;
        }
        isWrite = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSignwayManager.writeUart(fid, buf, buf.length);
                isWrite = false;
            }
        }).start();
    }

    /**
     * 获取最新的串口数据
     */
    private void setNewData(byte[] newData) {
        if (newData != null && newData.length != 0) {
            int i = 0;
            while ((newData[i] != 0x55) && (newData[i + 1] != 0xAA)) {
                i++;
                if (i > (newData.length - 46)) {
                    return;
                }
            }
            Log.e("TTTTTTTTT 2 = ", newData[i + 2] + "");
            Log.e("TTTTTTTTT 5 = ", newData[i + 5] + "");
            Log.e("TTTTTTTTT 6 = ", newData[i + 6] + "");
            Log.e("TTTTTTTTT 7 = ", newData[i + 7] + "");
            Log.e("TTTTTTTTT 8 = ", newData[i + 8] + "");
            Log.e("TTTTTTTTT 9 = ", newData[i + 9] + "");
            Log.e("TTTTTTTTT 10 = ", newData[i + 10] + "");
            MODE = newData[i + 2];
            Log.e("TTTTTTTTT MODE = ", MODE + "");
        }
    }

    /**
     * 发送查询指令
     */
    private void InquiryStatus() {
        Log.e("TAG", "查询");
        DATA_0 = ConstantPool.Data0_beginning_commend;
        DATA_1 = ConstantPool.Data1_beginning_commend;
        DATA_2 = ConstantPool.Data2_Running_State;
        sendByte();
    }

//=============================================================  上面是串口调用逻辑  ======================================================================================================

}
