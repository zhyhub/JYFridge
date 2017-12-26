package smartlink.zhy.jyfridge.service;

import android.accessibilityservice.AccessibilityService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import com.signway.SignwayManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import smartlink.zhy.jyfridge.ConstantPool;
import smartlink.zhy.jyfridge.R;
import smartlink.zhy.jyfridge.bean.BaseEntity;
import smartlink.zhy.jyfridge.bean.RemindBean;
import smartlink.zhy.jyfridge.bean.Song;
import smartlink.zhy.jyfridge.bean.ZigbeeBean;
import smartlink.zhy.jyfridge.json.JsonParser;
import smartlink.zhy.jyfridge.player.MusicPlayer;
import smartlink.zhy.jyfridge.utils.BaseCallBack;
import smartlink.zhy.jyfridge.utils.BaseOkHttpClient;
import smartlink.zhy.jyfridge.utils.L;

/**
 * 唤醒android板   调用讯飞语音
 */

public class VoiceService extends AccessibilityService {

    private static String TAG = VoiceService.class.getSimpleName();

    //行程提醒广播
    private AlarmReceiver alarmReceiver;
    private int requestCode = 0;

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private SpeechSynthesizer mTts;

    /**
     * 音量控制
     */
    private AudioManager audioManager;

    /**
     * 串口接入
     */

    private boolean isOpenDoor1 = false;
    private boolean isOpenDoor2 = false;
    private boolean isOpenDoor8 = false;

    private byte MODE;

    private static int CurrentTemp = 0;

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

    private byte[] data = new byte[]{};//写的数据

    private byte[] sendData = new byte[48];//读的数据

    boolean isWrite = false;
    SignwayManager mSignwayManager = null;
    int MAX_SIZE = 100;
    byte[] rbuf = new byte[MAX_SIZE];

    Handler writeHandler = null;
    Handler readHandler = null;
    Handler redHandler = null;
    Handler resultHandler = null;

    Runnable redUpdate = null;
    Runnable writeUpdate = null;
    Runnable readUpdate = null;
    Runnable resultUpdate = null;

    private boolean isRed = false;

    int readLength;
    int fid = -1;
    AlarmManager am;

    private NetWorkStateReceiver receiver;

    private boolean isPause = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createSocket();
        am = (AlarmManager) VoiceService.this.getSystemService(Context.ALARM_SERVICE);
        alarmReceiver = new AlarmReceiver();
        registerReceiver(alarmReceiver, new IntentFilter("smartlink.zhy.jyfridge.RING"));

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(VoiceService.this, mInitListener);
        mTts = SpeechSynthesizer.createSynthesizer(VoiceService.this, mTtsInitListener);
        // 设置参数
        setTtsParam();
        initUart();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert audioManager != null;
        L.e(TAG, "当前音量    " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + "  最大音量  " + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        DataSupport.deleteAll(RemindBean.class, "triggerAtMillis<?", String.valueOf(System.currentTimeMillis()));

        List<RemindBean> remindBeanList = DataSupport.select("triggerAtMillis").where("triggerAtMillis >= ?", String.valueOf(System.currentTimeMillis())).find(RemindBean.class);

        if (remindBeanList.size() > 0) {
            for (RemindBean remindBean : remindBeanList) {
                Intent intent = new Intent();
                intent.setAction("smartlink.zhy.jyfridge.RING");
                intent.putExtra("time", remindBean.getTriggerAtMillis());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(VoiceService.this, requestCode, intent, 0);
                am.set(AlarmManager.RTC_WAKEUP, remindBean.getTriggerAtMillis(), pendingIntent);
                requestCode++;
            }
            L.e(TAG, "还有没有过期的日程提醒");
        }

        if (receiver == null) {
            receiver = new NetWorkStateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
        L.e(TAG, "ControlService ControlService ControlService ControlService");
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

    /**
     * 讯飞唤醒监听
     *
     * @param event 监听事件
     * @return
     */
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_F1:
                mSignwayManager.openGpioDevice();
                if (mTts.isSpeaking()) {
                    mTts.stopSpeaking();
                }
                if (mIat.isListening()) {
                    mIat.stopListening();
                }

                mSignwayManager.setHighGpio(SignwayManager.ExterGPIOPIN.SWH5528_J9_PIN23);

                //接受到f1信号，设备已经被唤醒，调用讯飞语音识别
                L.e(TAG, "接受到f1信号，设备已经被唤醒，调用讯飞语音识别");

                if (MusicPlayer.getPlayer().isPlaying()) {
                    MusicPlayer.getPlayer().pause();
                    isPause = true;
                    L.e(TAG, "onKeyEvent  播放睡前故事暂停");
                }
                int code = mTts.startSpeaking("我在", mTtsListener);
                /*
                 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
		         * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
		        */
                if (code != ErrorCode.SUCCESS) {
                    showTip("语音合成失败,错误码: " + code);
                }
                break;
            case KeyEvent.KEYCODE_F10://按键音量增加
                L.e(TAG, "接受到f10信号，音量增加按钮");
                if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1, 0);
                }
                break;
            case KeyEvent.KEYCODE_F11://按键音量减少
                L.e(TAG, "接受到f11信号，音量减少按钮");
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1, 0);
                break;
        }
        return super.onKeyEvent(event);
    }

    private void showTip(final String str) {
        L.e(TAG, str);
    }

    /**
     * 讯飞听写监听器。
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
            mSignwayManager.setLowGpio(SignwayManager.ExterGPIOPIN.SWH5528_J9_PIN23);
            if (isPause) {
                MusicPlayer.getPlayer().resume();
                isPause = false;
                L.e(TAG, "onCompleted  播放睡前故事恢复播放");
            }
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
//                if (msg.equals("打开灯。")) {
//                    DATA_2 = ConstantPool.Data2_Modify_Mode;
//                    DATA_9 = 0x10;
//                    sendByte();
//                }
                if (!msg.equals("")) {
                    sendMsg(msg, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC), audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                }
//                else if(msg.equals("关闭灯。")){
//                    DATA_2 = ConstantPool.Data2_Modify_Mode;
//                    DATA_9 = 0x00;
//                    sendByte();
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

    /**
     * 拼接语音
     *
     * @param results 讯飞识别返回的结果
     * @return 返回拼接的结果
     */
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
            mTts.setParameter(SpeechConstant.SPEED, "80");
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
        mIat.setParameter(SpeechConstant.VAD_BOS, "10000");
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
        unregisterReceiver(alarmReceiver);
        Intent sevice = new Intent(this, VoiceService.class);
        this.startService(sevice);
        writeHandler.removeCallbacks(writeUpdate);//停止指令
        readHandler.removeCallbacks(readUpdate);//停止指令
        redHandler.removeCallbacks(redUpdate);//停止指令
        requestCode = 0;
        closeSocket();
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void NearOverDue() {
        BaseOkHttpClient.newBuilder()
                .addParam("Ingredients.refrigeratorId", ConstantPool.FridgeId)
                .addParam("Ingredients.userId", ConstantPool.UserID)
                .get()
                .url(ConstantPool.NearOverdue)
                .build().enqueue(new BaseCallBack() {
            @Override
            public void onSuccess(Object o) {
                L.e(TAG, "NearOverDue  onSuccess");
                Gson gson = new Gson();
                BaseEntity entity = gson.fromJson(o.toString(), BaseEntity.class);
                if (entity != null && entity.getCode() == 1) {
                    if (entity.getText() != null && !"".equals(entity.getText())) {
                        if (mTts.isSpeaking()) {
                            mTts.stopSpeaking();
                        }
                        if (mIat.isListening()) {
                            mIat.stopListening();
                        }
                        mTts.startSpeaking("冰箱里的" + entity.getText() + "快过期了，请尽快食用", mTtsListener);
                    }
                }
            }

            @Override
            public void onError(int code) {
                L.e(TAG, "NearOverDue  onError");
            }

            @Override
            public void onFailure(Call call, IOException e) {
                L.e(TAG, "NearOverDue  onFailure");
            }
        });
    }

//=============================================================  下面是请求海知语音获取意图 并做相应的指令操作 ======================================================================================================

    private Song getSong(String url) {
        Song song = new Song();
        song.setPath(url);
        return song;
    }

    private void sendMsg(String txt, int currentVolume, final int maxVolume) {
        L.e(TAG, "  sendMsg   " + Arrays.toString(sendData) + "   currentVolume   " + currentVolume + "   maxVolume   " + maxVolume);
        BaseOkHttpClient.newBuilder()
                .addParam("q", txt)
                .addParam("currentVolume", currentVolume)
                .addParam("maxVolume", maxVolume)
                .addParam("data", Arrays.toString(sendData))
                .addParam("user_id", ConstantPool.UserID)
                .addParam("refrigeratorId", ConstantPool.FridgeId)
                .get()
                .url(ConstantPool.AI)
                .build().enqueue(new BaseCallBack() {
            @Override
            public void onSuccess(Object o) {
                L.e(TAG, "onSuccess" + o.toString());
                if (mIat.isListening()) {
                    mIat.stopListening();
                }
                Gson gson = new Gson();
                BaseEntity entity = gson.fromJson(o.toString(), BaseEntity.class);
                if (entity.getCode() == 1) {
                    switch (entity.getType()) {
                        case 0://不操作指令
                            TTS(entity);
                            break;
                        case 1://冰箱操作
                            if (entity.getData() != null && entity.getData().length != 0) {
                                writeTTyDevice(fid, entity.getData());
                                TTS(entity);
                            }
                            break;
                        case 2://音量操作
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, entity.getVolume(), 0);
                            TTS(entity);
                            break;
                        case 3://日程提醒
                            RemindBean remindBean = new RemindBean();
                            remindBean.setTriggerAtMillis(entity.getTime_start());
                            remindBean.setMsg(entity.getDetails());
                            remindBean.save();
                            if (remindBean.save()) {
                                L.e(TAG, "Connector   存储成功");
                            } else {
                                L.e(TAG, "Connector   存储失败");
                            }
                            Intent intent = new Intent();
                            intent.setAction("smartlink.zhy.jyfridge.RING");
                            intent.putExtra("time", entity.getTime_start());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(VoiceService.this, requestCode, intent, 0);
                            am.set(AlarmManager.RTC_WAKEUP, entity.getTime_start(), pendingIntent);
                            requestCode++;
                            if ("".equals(entity.getDetails())) {
                                TTS(entity);
                            }
                            break;
                        case 4://获取睡前故事url
                            TTS(entity);
                            if (entity.getUrl() != null && !"".equals(entity.getUrl())) {
                                List<Song> queue = new ArrayList<>();
                                queue.add(getSong(entity.getUrl().replace("\\", "")));
                                MusicPlayer.getPlayer().setQueue(queue, 0);
                                L.e(TAG, "sendMsg  播放睡前故事播放" + entity.getUrl().replace("\\", ""));
                                if (mTts.isSpeaking()) mTts.stopSpeaking();
                                if (mIat.isListening()) mIat.stopListening();
                                isPause = true;
                            }
                            break;
                        case 5://暂停
                            L.e(TAG, "sendMsg  播放睡前故事暂停");
                            MusicPlayer.getPlayer().pause();
                            TTS(entity);
                            isPause = false;
                            break;
                        case 6://恢复播放
                            L.e(TAG, "sendMsg  播放睡前故事恢复播放");
                            MusicPlayer.getPlayer().resume();
                            isPause = false;
                            break;
                        case 7://停止
                            L.e(TAG, "sendMsg  播放睡前故事停止播放");
                            MusicPlayer.getPlayer().stop();
                            TTS(entity);
                            isPause = false;
                            break;
//                        case 101://启动-手机无线充电设备
//                            TTS(entity);
//                            break;
//                        case 102://关闭-手机无线充电设备
//                            TTS(entity);
//                            break;
//                        case 103://启动-台灯无线充电设备
//                            TTS(entity);
//                            break;
//                        case 104://关闭-台灯无线充电设备
//                            TTS(entity);
//                            break;
                        case 107://启动-电灯
                            CurrentTemp = 60;
                            ZigbeeBean zigbeeOpen = new ZigbeeBean();
                            zigbeeOpen.setSourceId("009569B4662A");
                            zigbeeOpen.setRequestType("cmd");
                            zigbeeOpen.setSerialNum(-1);
                            zigbeeOpen.setId("00124B000B277AD8");

                            ZigbeeBean.AttributesBean beanOpen = new ZigbeeBean.AttributesBean();
                            beanOpen.setTYP("LT-CTM");
                            beanOpen.setLEV(String.valueOf(CurrentTemp));
                            beanOpen.setSWI("ON");

                            zigbeeOpen.setAttributes(beanOpen);

                            L.e(TAG, "ZigbeeBean  Open " + new Gson().toJson(zigbeeOpen));
                            sendData(new Gson().toJson(zigbeeOpen));
                            TTS(entity);
                            break;
                        case 108://关闭-电灯
                            CurrentTemp = 0;
                            ZigbeeBean zigbeeClose = new ZigbeeBean();
                            zigbeeClose.setSourceId("009569B4662A");
                            zigbeeClose.setRequestType("cmd");
                            zigbeeClose.setSerialNum(-1);
                            zigbeeClose.setId("00124B000B277AD8");

                            ZigbeeBean.AttributesBean beanClose = new ZigbeeBean.AttributesBean();

                            beanClose.setLEV(String.valueOf(CurrentTemp));
                            beanClose.setSWI("OFF");
                            beanClose.setTYP("LT-CTM");

                            zigbeeClose.setAttributes(beanClose);

                            L.e(TAG, "ZigbeeBean  Close " + new Gson().toJson(zigbeeClose));
                            sendData(new Gson().toJson(zigbeeClose));
                            TTS(entity);
                            break;
                        case 110://点灯亮度调高
                            CurrentTemp = CurrentTemp + 20;
                            if (CurrentTemp <= 100) {
                                ZigbeeBean zigbeeUp = new ZigbeeBean();
                                zigbeeUp.setSourceId("009569B4662A");
                                zigbeeUp.setRequestType("cmd");
                                zigbeeUp.setSerialNum(-1);
                                zigbeeUp.setId("00124B000B277AD8");

                                ZigbeeBean.AttributesBean Up = new ZigbeeBean.AttributesBean();

                                Up.setTYP("LT-CTM");
                                Up.setLEV(String.valueOf(CurrentTemp));
                                Up.setSWI("ON");
                                zigbeeUp.setAttributes(Up);

                                L.e(TAG, "ZigbeeBean  Up " + new Gson().toJson(zigbeeUp));
                                sendData(new Gson().toJson(zigbeeUp));
                                TTS(entity);
                            } else {
                                mTts.startSpeaking("已经是最大亮度了", mTtsListener);
                                CurrentTemp = 100;
                            }
                            break;
                        case 111://点灯亮度调底
                            CurrentTemp = CurrentTemp - 20;
                            if (CurrentTemp >= 20) {
                                ZigbeeBean zigbeeDown = new ZigbeeBean();
                                zigbeeDown.setSourceId("009569B4662A");
                                zigbeeDown.setRequestType("cmd");
                                zigbeeDown.setSerialNum(-1);
                                zigbeeDown.setId("00124B000B277AD8");

                                ZigbeeBean.AttributesBean Down = new ZigbeeBean.AttributesBean();

                                Down.setTYP("LT-CTM");
                                Down.setLEV(String.valueOf(CurrentTemp));
                                Down.setSWI("ON");

                                zigbeeDown.setAttributes(Down);

                                L.e(TAG, "ZigbeeBean  Down " + new Gson().toJson(zigbeeDown));
                                sendData(new Gson().toJson(zigbeeDown));
                                TTS(entity);
                            } else {
                                mTts.startSpeaking("已经是最低亮度了", mTtsListener);
                                CurrentTemp = 20;
                            }
                            break;
                        case 112://点灯亮度调到最高
                            CurrentTemp = 100;
                            ZigbeeBean zigbeeMax = new ZigbeeBean();
                            zigbeeMax.setSourceId("009569B4662A");
                            zigbeeMax.setRequestType("cmd");
                            zigbeeMax.setSerialNum(-1);
                            zigbeeMax.setId("00124B000B277AD8");

                            ZigbeeBean.AttributesBean Max = new ZigbeeBean.AttributesBean();

                            Max.setTYP("LT-CTM");
                            Max.setLEV(String.valueOf(CurrentTemp));
                            Max.setSWI("ON");
                            zigbeeMax.setAttributes(Max);

                            L.e(TAG, "ZigbeeBean  Max " + new Gson().toJson(zigbeeMax));
                            sendData(new Gson().toJson(zigbeeMax));
                            TTS(entity);
                            break;
                        case 113://点灯亮度调到最底
                            CurrentTemp = 20;
                            ZigbeeBean zigbeeMin = new ZigbeeBean();
                            zigbeeMin.setSourceId("009569B4662A");
                            zigbeeMin.setRequestType("cmd");
                            zigbeeMin.setSerialNum(-1);
                            zigbeeMin.setId("00124B000B277AD8");

                            ZigbeeBean.AttributesBean Min = new ZigbeeBean.AttributesBean();

                            Min.setTYP("LT-CTM");
                            Min.setLEV(String.valueOf(CurrentTemp));
                            Min.setSWI("ON");
                            zigbeeMin.setAttributes(Min);

                            L.e(TAG, "ZigbeeBean  Min " + new Gson().toJson(zigbeeMin));
                            sendData(new Gson().toJson(zigbeeMin));
                            TTS(entity);
                            break;
                        case 105://启动-窗帘
                            ZigbeeBean zigbeeCurtainsOpen = new ZigbeeBean();
                            zigbeeCurtainsOpen.setSourceId("009569B4662A");
                            zigbeeCurtainsOpen.setRequestType("cmd");
                            zigbeeCurtainsOpen.setSerialNum(-1);
                            zigbeeCurtainsOpen.setId("00124B0009E8D140");

                            ZigbeeBean.AttributesBean CurtainsOpen = new ZigbeeBean.AttributesBean();
                            CurtainsOpen.setTYP("WD-RXJ");
                            CurtainsOpen.setWIN("OPEN");

                            zigbeeCurtainsOpen.setAttributes(CurtainsOpen);

                            L.e(TAG, "ZigbeeBean  CurtainsOpen " + new Gson().toJson(zigbeeCurtainsOpen));
                            sendData(new Gson().toJson(zigbeeCurtainsOpen));
                            TTS(entity);
                            break;
                        case 106://关闭-窗帘
                            ZigbeeBean zigbeeCurtainsClose = new ZigbeeBean();
                            zigbeeCurtainsClose.setSourceId("009569B4662A");
                            zigbeeCurtainsClose.setRequestType("cmd");
                            zigbeeCurtainsClose.setSerialNum(1);
                            zigbeeCurtainsClose.setId("00124B0009E8D140");

                            ZigbeeBean.AttributesBean CurtainsClose = new ZigbeeBean.AttributesBean();
                            CurtainsClose.setTYP("WD-RXJ");
                            CurtainsClose.setWIN("CLOSE");

                            zigbeeCurtainsClose.setAttributes(CurtainsClose);

                            L.e(TAG, "ZigbeeBean  CurtainsClose " + new Gson().toJson(zigbeeCurtainsClose));
                            sendData(new Gson().toJson(zigbeeCurtainsClose));
                            TTS(entity);
                            break;
                        case 109://停止-窗帘
                            ZigbeeBean zigbeeCurtainsStop = new ZigbeeBean();
                            zigbeeCurtainsStop.setSourceId("009569B4662A");
                            zigbeeCurtainsStop.setRequestType("cmd");
                            zigbeeCurtainsStop.setSerialNum(1);
                            zigbeeCurtainsStop.setId("00124B0009E8D140");

                            ZigbeeBean.AttributesBean CurtainsStop = new ZigbeeBean.AttributesBean();
                            CurtainsStop.setTYP("WD-RXJ");
                            CurtainsStop.setWIN("STOP");

                            zigbeeCurtainsStop.setAttributes(CurtainsStop);

                            L.e(TAG, "ZigbeeBean  CurtainsStop " + new Gson().toJson(zigbeeCurtainsStop));
                            sendData(new Gson().toJson(zigbeeCurtainsStop));
                            TTS(entity);
                            break;
                    }
                }
            }

            @Override
            public void onError(int code) {
                L.e(TAG, "sendMsg onError");
                mTts.startSpeaking("哎呀，好像出问题了", mTtsListener);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                L.e(TAG, "onFailure" + e.getMessage());
            }
        });
    }

    private void TTS(BaseEntity entity) {
        if (entity != null && entity.getText() != null && !entity.getText().equals("")) {
            mTts.startSpeaking(entity.getText(), mTtsListener);
        }
    }

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
                DATA_2 = ConstantPool.Data2_Running_State;
                sendByte();
                readHandler.postDelayed(readUpdate, 1000); //1秒后再调用
            }
        };
        readHandler.post(readUpdate);

        /*P24 红外线感应打开关闭*/
        redHandler = new Handler();
        redUpdate = new Runnable() {
            @Override
            public void run() {
                mSignwayManager.openGpioDevice();
                mSignwayManager.setGpioNum(SignwayManager.ExterGPIOPIN.SWH5528_J9_PIN24,
                        SignwayManager.GPIOGroup.GPIO0, SignwayManager.GPIONum.PD2);
                int state = mSignwayManager.getGpioStatus(SignwayManager.ExterGPIOPIN.SWH5528_J9_PIN24);
                L.e(TAG, "  state  : " + state);
                if (state == 1 && !isRed) {
                    NearOverDue();
                    isRed = true;
                } else if (state == 0) {
                    isRed = false;
                }
                redHandler.postDelayed(redUpdate, 500);
            }
        };
        redHandler.post(redUpdate);

//        /*P23 呼吸灯打开关闭*/
//        lightHandler = new Handler();
//        lightUpdate = new Runnable() {
//            @Override
//            public void run() {
//                mSignwayManager.setGpioNum(SignwayManager.ExterGPIOPIN.SWH5528_J9_PIN23,
//                        SignwayManager.GPIOGroup.GPIO0, SignwayManager.GPIONum.PD4);
//                int state = mSignwayManager.getGpioStatus(SignwayManager.ExterGPIOPIN.SWH5528_J9_PIN23);
//                if (state == 1 && !isLight) {
//                    L.e(TAG, "  Light  高   开");
//                    isLight = true;
//                } else if (state == 0) {
//                    L.e(TAG, "  Light  低   关");
//                    isLight = false;
//                }
//                lightHandler.postDelayed(lightUpdate, 500);
//            }
//        };
//        lightHandler.post(lightUpdate);

    }

    private void sendByte() {
        DATA_23 = (byte) (DATA_0 + DATA_1 + DATA_2 + DATA_3 + DATA_4 + DATA_5 + DATA_6
                + DATA_7 + DATA_8 + DATA_9 + DATA_10 + DATA_11 + DATA_12 + DATA_13 + DATA_14
                + DATA_15 + DATA_16 + DATA_17 + DATA_18 + DATA_19 + DATA_20 + DATA_21 + DATA_22);
        data = new byte[]{DATA_0, DATA_1, DATA_2, DATA_3, DATA_4, DATA_5, DATA_6, DATA_7, DATA_8, DATA_9, DATA_10, DATA_11, DATA_12, DATA_13, DATA_14, DATA_15, DATA_16, DATA_17, DATA_18, DATA_19, DATA_20, DATA_21, DATA_22, DATA_23};
        writeTTyDevice(fid, data);
        L.e(TAG, "sendByte  " + Arrays.toString(data));
    }

    private boolean isSet = false;

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
                if (!isSet) {
                    isSet = true;
                    readLength = mSignwayManager.readUart(fid, rbuf, rbuf.length);
                    L.e(TAG, "  readLength  " + readLength + "   " + Arrays.toString(sendData));
                    if (readLength > 47) {
                        setNewData(rbuf, readLength);
                    }
                    isSet = false;
                }
            }
        }).start();
    }

    /**
     * 向串口写数据
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
    private void setNewData(byte[] newData, int readLength) {

        if (newData != null && readLength > 0) {
            int i = 0;
            while ((newData[i] != 0x55) && (newData[i + 1] != 0xAA)) {
                i++;
                if (i > (readLength - 46)) {
                    return;
                }
            }

            for (int j = 0; j < 48; j++) {
                sendData[j] = newData[i];
                i++;
                if (i > (readLength - 1)) {
                    break;
                }
            }

            MODE = sendData[2];
            L.e("TTTTTTTTTTTTTTTTTTTTTT MODE = ", MODE + "    " + sendData[4]);

            /*
             * 现在冷藏室开关门的逻辑是反的  等冰箱板子改好了  android代码还要改回来
             */
            if ((sendData[4] & 0x01) != 0) {
                if (!isOpenDoor1) {
                    L.e(TAG, "冷藏室门   开了");
                    CloseDoor();
                    isOpenDoor1 = true;
                } else {
                    L.e(TAG, "冷藏室门   ------------开了");
                }
            } else {
                L.e(TAG, "冷藏室门   =============关了");
                isOpenDoor1 = false;
                OpenDoor();
            }

//            if ((sendData[4] & 0x02) != 0) {
//                if (!isOpenDoor2) {
//                    L.e(TAG, "冷冻门   开了");
//                    isOpenDoor2 = true;
//                }else {
//                    L.e(TAG, "冷冻门   --------------开了");
//                }
//            } else{
//                L.e(TAG, "冷冻门   ================关了");
//                isOpenDoor2 = false;
//            }

            //            if ((sendData[4] & 0x08) != 0) {
//                if (!isOpenDoor8) {
//                    L.e(TAG, "变温门   开了");
//                    isOpenDoor8 = true;
//                }else {
//                    L.e(TAG, "变温门   ---------------开了");
//                }
//            } else{
//                L.e(TAG, "变温门   ================关了");
//                isOpenDoor8 = false;
//            }

            if ((sendData[4] & 0x02) != 0) {
                if (!isOpenDoor8) {
                    L.e(TAG, "变温门   开了");
                    isOpenDoor8 = true;
                } else {
                    L.e(TAG, "变温门   ---------------开了");
                }
            } else {
                L.e(TAG, "变温门   ================关了");
                isOpenDoor8 = false;
            }

            if ((sendData[4] & 0x04) != 0) {
                if (!isOpenDoor2) {
                    L.e(TAG, "冷冻门   开了");
                    isOpenDoor2 = true;
                } else {
                    L.e(TAG, "冷冻门   --------------开了");
                }
            } else {
                L.e(TAG, "冷冻门   ================关了");
                isOpenDoor2 = false;
            }


        }
    }

//=============================================================  下面是日程提醒调用逻辑  ======================================================================================================

    private class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("smartlink.zhy.jyfridge.RING".equals(intent.getAction())) {
                L.e(TAG, "AlarmReceiver   时间到了  ");

                long time = intent.getLongExtra("time", 0);

                List<RemindBean> remindBeanList = DataSupport.select("msg").where("triggerAtMillis=?", String.valueOf(time)).find(RemindBean.class);

                StringBuffer stringBuffer = new StringBuffer();

                if (remindBeanList.size() > 0) {
                    for (RemindBean r : remindBeanList) {
                        stringBuffer = stringBuffer.append(r.getMsg()).append(",");
                    }
                    mTts.startSpeaking(stringBuffer.toString(), mTtsListener);
                    L.e(TAG, "  stringBuffer  " + stringBuffer);

                    DataSupport.deleteAll(RemindBean.class, "triggerAtMillis=?", String.valueOf(time));
                }
            }
        }
    }

//=============================================================  下面是LierDa调用逻辑  ======================================================================================================

    private Handler mMainHandler;
    private Socket socket;
    private ExecutorService mThreadPool;
    private InputStream is;
    private InputStreamReader isR;
    private BufferedReader br;
    private String response;
    private OutputStream outputStream;
    private static final int MSG_SOCKET = 1234;

    protected void createSocket() {
        L.e(TAG, "createSocket() called with: ip = [" + "192.168.100.1" + "], port = [" + 8888 + "]");

        //初始化线程池
        mThreadPool = Executors.newCachedThreadPool();

        //实例化主线程，用于更新接收过来的消息
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_SOCKET:
                        String response = (String) msg.obj;
                        String sourceId = "";
                        int serialNum = 0;
                        String requestType = "";
                        String id = "";
                        int state = 0;
                        if ("".equals(response)) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.has("stateCode")) {
                                    state = jsonObject.getInt("stateCode");
                                    serialNum = jsonObject.getInt("serialNum");
                                    sourceId = jsonObject.getString("sourceId");
                                    requestType = jsonObject.getString("requestType");
                                    id = jsonObject.getString("id");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (state == 1) {
                                L.e(TAG, "操作成功   " + "sourceId  " + sourceId + "   serialNum " + serialNum + "  requestType   " + requestType + "   id   " + id);
                            } else {
                                L.e(TAG, "操作失败");
                            }
                        }
                        break;
                }
            }
        };

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("192.168.100.1", 8888);
                    L.e(TAG, "Socket connected? " + socket.isConnected());
                } catch (IOException | NullPointerException e) {
                    L.e(TAG, e.getMessage() + "     " + e);
                }
            }
        });
    }

    private void closeSocket() {
        Log.d(TAG, "closeSocket() called");
        if (mThreadPool == null) {
            mThreadPool = Executors.newCachedThreadPool();
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                    if (socket != null) {
                        socket.close();
                        L.e(TAG, "DisConnected? " + !socket.isConnected());
                    }
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendData(final String content) {
        L.e(TAG, "sendData() called with: content = [" + content + "]");
        if (mThreadPool == null) {
            mThreadPool = Executors.newCachedThreadPool();
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket == null) {
                        socket = new Socket("192.168.100.1", 8888);
                    }
                    OutputStream outputStream = socket.getOutputStream();
                    byte buffer[] = content.getBytes();
                    int temp = buffer.length;
                    outputStream.write(buffer, 0, buffer.length);
                    outputStream.flush();
                } catch (IOException | NullPointerException e) {
                    L.e(TAG, e.getMessage() + "    " + e);
                }
            }
        });
        receiveData();
    }

    private void receiveData() {
        Log.d(TAG, "receiveData() called");
        if (mThreadPool == null) {
            mThreadPool = Executors.newCachedThreadPool();
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket == null) {
                        socket = new Socket("192.168.100.1", 8888);
                    }
                    is = socket.getInputStream();
                    isR = new InputStreamReader(is);
                    br = new BufferedReader(isR);
                    response = br.readLine();
                    L.d(TAG, "Result: " + response);

                    Message msg = Message.obtain();
                    msg.what = MSG_SOCKET;
                    msg.obj = response;
                    mMainHandler.sendMessage(msg);

                } catch (IOException | NullPointerException e) {
                    L.e(TAG, "操作失败" + e.getMessage() + "    " + e);
                }
            }
        });
    }

//=============================================================  下面是监听wifi连接情况  ======================================================================================================

    private static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info.isConnected();
    }

    private class NetWorkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            L.e("网络状态发生变化");
            if (isWifiConnected(VoiceService.this)) {
                mTts.startSpeaking("网络连接成功", mTtsListener);
            } else {
                mTts.startSpeaking("网络连接已断开", mTtsListener);
            }
        }
    }

//=============================================================  下面是控制图像识别逻辑 ======================================================================================================

    private Handler closeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    if (resultHandler != null && resultUpdate != null) {
                        resultHandler.removeCallbacks(resultUpdate);
                    }
                    break;
            }
        }
    };

    private void OpenDoor() {
        BaseOkHttpClient.newBuilder()
                .get()
                .url(ConstantPool.OPEN)
                .build().enqueue(new BaseCallBack() {
            @Override
            public void onSuccess(Object o) {
                L.e(TAG, "open  onSuccess" + o.toString());
                resultHandler = new Handler();
                resultUpdate = new Runnable() {
                    @Override
                    public void run() {
                        getResult();
                        resultHandler.postDelayed(resultUpdate, 1000); //1秒后再调用
                    }
                };
                resultHandler.post(resultUpdate);
            }

            @Override
            public void onError(int code) {
                L.e(TAG, "open onError");
                mTts.startSpeaking("哎呀，好像出问题了", mTtsListener);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                L.e(TAG, "open onFailure" + e.getMessage());
            }
        });
    }

    private void CloseDoor() {
        BaseOkHttpClient.newBuilder()
                .get()
                .url(ConstantPool.CLOSE)
                .build().enqueue(new BaseCallBack() {
            @Override
            public void onSuccess(Object o) {
                L.e(TAG, "close  onSuccess" + o.toString());
                closeHandler.sendEmptyMessage(100);
            }

            @Override
            public void onError(int code) {
                L.e(TAG, "close onError");
                mTts.startSpeaking("哎呀，好像出问题了", mTtsListener);
                closeHandler.sendEmptyMessage(100);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                L.e(TAG, "close onFailure" + e.getMessage());
                closeHandler.sendEmptyMessage(100);
            }
        });
    }

    private void getResult() {
        BaseOkHttpClient.newBuilder()
                .get()
                .url(ConstantPool.GetResult)
                .build().enqueue(new BaseCallBack() {
            @Override
            public void onSuccess(Object o) {
                L.e(TAG, "getResult  onSuccess" + o.toString());
                if (mIat.isListening()) {
                    mIat.stopListening();
                }
                Gson gson = new Gson();
                BaseEntity entity = gson.fromJson(o.toString(), BaseEntity.class);
                if (entity.getCode() == 1) {
                    mTts.startSpeaking(entity.getText(), mTtsListener);
                }
            }

            @Override
            public void onError(int code) {
                L.e(TAG, "getResult onError");
                mTts.startSpeaking("哎呀，好像出问题了", mTtsListener);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                L.e(TAG, "getResult onFailure" + e.getMessage());
            }
        });
    }

}
