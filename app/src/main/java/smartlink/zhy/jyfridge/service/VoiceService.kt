package smartlink.zhy.jyfridge.service

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.iflytek.cloud.*
import com.iflytek.sunflower.FlowerCollector
import org.json.JSONException
import org.json.JSONObject
import smartlink.zhy.jyfridge.R
import smartlink.zhy.jyfridge.json.JsonParser
import java.util.LinkedHashMap

/**
 * 用于监听android板唤醒信号
 */
class VoiceService : AccessibilityService() {

    private val TAG = VoiceService::class.java.simpleName

    // 语音听写对象
    private var mIat: SpeechRecognizer? = null
    // 用HashMap存储听写结果
    private val mIatResults = LinkedHashMap<String, String>()
    // 引擎类型
    private val mEngineType = SpeechConstant.TYPE_CLOUD

    private var mTranslateEnable = false

    private var mTts: SpeechSynthesizer? = null
    private val voicer = "xiaoyan"

    // 函数调用返回值
    internal var ret = 0
    // 缓冲进度
    private var mPercentForBuffering = 0
    // 播放进度
    private var mPercentForPlaying = 0


    override fun onCreate() {
        super.onCreate()
        // 初始化识别无UI识别对象
        mIat = SpeechRecognizer.createRecognizer(this@VoiceService, mInitListener)
        mTts = SpeechSynthesizer.createSynthesizer(this@VoiceService, mTtsInitListener)
    }

    /**
     * 初始化监听器。
     */
    private val mInitListener = InitListener { code ->
        Log.d(TAG, "SpeechRecognizer init() code = " + code)
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败，错误码：" + code)
        }
    }

    /**
     * 初始化监听。
     */
    private val mTtsInitListener = InitListener { code ->
        Log.d(TAG, "InitListener init() code = " + code)
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：" + code)
        } else {
            // 初始化成功，之后可以调用startSpeaking方法
            // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
            // 正确的做法是将onCreate中的startSpeaking调用移至这里
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        when (event?.keyCode) {
            KeyEvent.KEYCODE_F1 -> {
                //接受到f1信号，设备已经被唤醒，调用讯飞语音识别
                Log.e(TAG, "接受到f1信号，设备已经被唤醒，调用讯飞语音识别")
                // 移动数据分析，收集开始合成事件
                FlowerCollector.onEvent(this@VoiceService, "tts_play")
                // 设置参数
                setTtsParam()
                val code = mTts?.startSpeaking("有什么吩咐", mTtsListener)
                /**
                 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
                 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
                 */
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);
                if (code != ErrorCode.SUCCESS) {
                    showTip("语音合成失败,错误码: " + code)
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun showTip(str: String) {
        Log.e(TAG, str)
    }

    /**
     * 语音合成参数设置
     */
    private fun setTtsParam() {
        // 清空参数
        mTts!!.setParameter(SpeechConstant.PARAMS, null)
        // 根据合成引擎设置相应参数
        if (mEngineType == SpeechConstant.TYPE_CLOUD) {
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
            // 设置在线合成发音人
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, voicer)
            //设置合成语速
            mTts!!.setParameter(SpeechConstant.SPEED, "50")
            //设置合成音调
            mTts!!.setParameter(SpeechConstant.PITCH, "50")
            //设置合成音量
            mTts!!.setParameter(SpeechConstant.VOLUME, "50")
        } else {
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL)
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, "")
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts!!.setParameter(SpeechConstant.STREAM_TYPE, "3")
        // 设置播放合成音频打断音乐播放，默认为true
        mTts!!.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true")
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
        mTts!!.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory().toString() + "/msc/tts.wav")
    }

    /**
     * 合成回调监听。
     */
    private val mTtsListener = object : SynthesizerListener {

        override fun onSpeakBegin() {
            showTip("开始播放")
        }

        override fun onSpeakPaused() {
            showTip("暂停播放")
        }

        override fun onSpeakResumed() {
            showTip("继续播放")
        }

        override fun onBufferProgress(percent: Int, beginPos: Int, endPos: Int,
                                      info: String) {
            // 合成进度
            mPercentForBuffering = percent
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying))
        }

        override fun onSpeakProgress(percent: Int, beginPos: Int, endPos: Int) {
            // 播放进度
            mPercentForPlaying = percent
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying))
        }

        override fun onCompleted(error: SpeechError?) {
            if (error == null) {
                showTip("播放完成")
                // 移动数据分析，收集开始听写事件
                FlowerCollector.onEvent(this@VoiceService, "iat_recognize")
                mIatResults.clear()
                // 设置参数
                setParam()
                ret = mIat!!.startListening(mRecognizerListener)
                if (ret != ErrorCode.SUCCESS) {
                    showTip("听写失败,错误码：" + ret)
                } else {
                    showTip(getString(R.string.text_begin))
                }
            } else if (error != null) {
                showTip(error.getPlainDescription(true))
            }
        }

        override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    }

    /**
     * 听写监听器。
     */
    private val mRecognizerListener = object : RecognizerListener {

        override fun onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话")
        }

        override fun onError(error: SpeechError) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            if (mTranslateEnable && error.errorCode == 14002) {
                showTip(error.getPlainDescription(true) + "\n请确认是否已开通翻译功能")
            } else {
                showTip(error.getPlainDescription(true))
            }
        }

        override fun onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话")
        }

        override fun onResult(results: RecognizerResult, isLast: Boolean) {
            Log.d(TAG, results.resultString)
            if (mTranslateEnable) {
                printTransResult(results)
            } else {
                printResult(results)
            }
            if (isLast) {
                // TODO 最后的结果
                Log.e(TAG, results.resultString)
            }
        }

        override fun onVolumeChanged(volume: Int, data: ByteArray) {
            showTip("当前正在说话，音量大小：" + volume)
            Log.d(TAG, "返回音频数据：" + data.size)
        }

        override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    }

    private fun printTransResult(results: RecognizerResult) {
        val trans = JsonParser.parseTransResult(results.resultString, "dst")
        val oris = JsonParser.parseTransResult(results.resultString, "src")

        if (TextUtils.isEmpty(trans) || TextUtils.isEmpty(oris)) {
            showTip("解析结果失败，请确认是否已开通翻译功能。")
        } else {
            Log.e(TAG, "TAG   printTransResult原始语言:\n$oris\n目标语言:\n$trans")
        }
    }

    private fun printResult(results: RecognizerResult) {
        val text = JsonParser.parseIatResult(results.resultString)

        var sn: String? = null
        // 读取json结果中的sn字段
        try {
            val resultJson = JSONObject(results.resultString)
            sn = resultJson.optString("sn")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        mIatResults.put(sn.toString(), text)

        val resultBuffer = StringBuffer()
        for (key in mIatResults.keys) {
            resultBuffer.append(mIatResults[key])
        }

        Log.e(TAG, "TAG   printResult " + resultBuffer.toString())

        val test = Intent()
        test.action = "test.test.test.text"
        test.putExtra("count", resultBuffer.toString())
        sendBroadcast(test)
        Log.e("TAG", "IatService  广播发送了" + resultBuffer.toString())
    }


    /**
     * 参数设置
     *
     * @return
     */
    fun setParam() {
        // 清空参数
        mIat!!.setParameter(SpeechConstant.PARAMS, null)
        // 设置听写引擎
        mIat!!.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
        // 设置返回结果格式
        mIat!!.setParameter(SpeechConstant.RESULT_TYPE, "json")

        Log.i(TAG, "translate enable")
        mIat!!.setParameter(SpeechConstant.ASR_SCH, "1")
        mIat!!.setParameter(SpeechConstant.ADD_CAP, "translate")
        mIat!!.setParameter(SpeechConstant.TRS_SRC, "its")

        // 设置语言
        mIat!!.setParameter(SpeechConstant.LANGUAGE, "zh_cn")
        // 设置语言区域
        mIat!!.setParameter(SpeechConstant.ACCENT, "mandarin")

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat!!.setParameter(SpeechConstant.VAD_BOS, "4000")
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat!!.setParameter(SpeechConstant.VAD_EOS, "1000")
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat!!.setParameter(SpeechConstant.ASR_PTT, "1")
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
        mIat!!.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory().toString() + "/msc/iat.wav")
    }

    override fun onDestroy() {
        val service = Intent(this, VoiceService::class.java)
        this.startService(service)
        super.onDestroy()
    }
}