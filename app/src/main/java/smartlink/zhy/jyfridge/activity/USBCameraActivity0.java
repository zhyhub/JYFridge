package smartlink.zhy.jyfridge.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.jiangdg.usbcamera.USBCameraManager;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import smartlink.zhy.jyfridge.ConstantPool;
import smartlink.zhy.jyfridge.R;
import smartlink.zhy.jyfridge.utils.L;


/**
 * AndroidUSBCamera引擎
 */

public class USBCameraActivity0 extends AppCompatActivity implements CameraDialog.CameraDialogParent, SensorEventListener {

    private static final String TAG = USBCameraActivity0.class.getSimpleName();
    public View mTextureView;

    private USBCameraManager mUSBManager;

    private CameraViewInterface mUVCCameraView;
    private boolean isRequest;
    private boolean isPreview;

    private SensorManager sm;
    private Sensor mGyroscope;
    private double X_max = 0, Y_max = 0, Z_max = 0;
    private double degree_X = 0;
    private double degree_Y = 0;
    private double degree_Z = 0;
    private long lasttimestamp = 0;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    float dT;

    private int isTake = 0;

    private OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    L.e(TAG, message);
                }
            }).setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    okhttp3.Request request = chain.request().newBuilder()
                            .build();
                    return chain.proceed(request);
                }
            })
            .build();

    /**
     * USB设备事件监听器
     */
    private USBCameraManager.OnMyDevConnectListener listener = new USBCameraManager.OnMyDevConnectListener() {
        // 插入USB设备
        @Override
        public void onAttachDev(UsbDevice device) {
            if (mUSBManager == null || mUSBManager.getUsbDeviceCount() == 0) {
                showShortMsg("未检测到USB摄像头设备");
                return;
            }
            // 请求打开摄像头
            if (!isRequest) {
                isRequest = true;
                if (mUSBManager != null) {
                    mUSBManager.requestPermission(ConstantPool.Camera_0);
                }
            }
        }

        // 拔出USB设备
        @Override
        public void onDettachDev(UsbDevice device) {
            if (isRequest) {
                // 关闭摄像头
                isRequest = false;
                mUSBManager.closeCamera();
                showShortMsg(device.getDeviceName() + "已拨出");
            }
        }

        // 连接USB设备成功
        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("连接失败，请检查分辨率参数是否正确");
                isPreview = false;
            } else {
                isPreview = true;
            }
        }

        // 与USB设备断开连接
        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("连接失败");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbcamera);

        mTextureView = (View) findViewById(R.id.camera_view);
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(new CameraViewInterface.Callback() {
            @Override
            public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
                if (!isPreview && mUSBManager.isCameraOpened()) {
                    mUSBManager.startPreview(mUVCCameraView, new AbstractUVCCameraHandler.OnPreViewResultListener() {
                        @Override
                        public void onPreviewResult(boolean result) {

                        }
                    });
                    isPreview = true;
                }
            }

            @Override
            public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

            }

            @Override
            public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
                if (isPreview && mUSBManager.isCameraOpened()) {
                    mUSBManager.stopPreview();
                    isPreview = false;
                }
            }
        });
        // 初始化引擎
        mUSBManager = USBCameraManager.getInstance();
        mUSBManager.initUSBMonitor(this, listener);
        mUSBManager.createUVCCamera(mUVCCameraView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUSBManager == null)
            return;
        // 注册USB事件广播监听器
        mUSBManager.registerUSB();
        mUVCCameraView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 注销USB事件广播监听器
        if (mUSBManager != null) {
            mUSBManager.unregisterUSB();
        }
        mUVCCameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUSBManager != null) {
            mUSBManager.release();
            L.e(TAG, "onDestroy");
        }
        mUVCCameraView = null;
        sm.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
        lasttimestamp = 0;
        degree_X = 0;
        degree_Y = 0;
        degree_Z = 0;
        L.e(TAG, "onPause");
    }

    private void showShortMsg(String msg) {
        L.e(TAG, msg);
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBManager.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
    }

    private double abs(double val) {
        if (val < 0) return -val;
        return val;
    }

    @Override
    protected void onResume() {
        super.onResume();
        lasttimestamp = 0;
        degree_X = 0;
        degree_Y = 0;
        degree_Z = 0;
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (mGyroscope == null) {
            Toast.makeText(this, "您的设备不支持陀螺仪！", Toast.LENGTH_SHORT).show();
        } else {
            sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            assert sm != null;
            sm.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
        L.e(TAG, "onResume");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (lasttimestamp == 0) {
            lasttimestamp = event.timestamp;
            return;
        }

        if (event.sensor == mGyroscope) {
            if (abs(event.values[0]) > abs(X_max)) {
                X_max = event.values[0];
            }
            if (abs(event.values[1]) > abs(Y_max)) {
                Y_max = event.values[1];
            }
            if (abs(event.values[2]) > abs(Z_max)) {
                Z_max = event.values[2];
            }
            float axisX = 0;
            float axisY = 0;
            float axisZ = 0;
            if (timestamp != 0) {
                dT = (event.timestamp - timestamp) * NS2S;
                axisX = event.values[0];
                axisY = event.values[1];
                axisZ = event.values[2];
            }
            degree_X += axisX * dT;
            degree_Y += axisY * dT;
            degree_Z += axisZ * dT;

            timestamp = event.timestamp;

            L.e(TAG, "axis_x_degree=" + String.valueOf(180 * degree_X / Math.PI));
//            L.e(TAG, "axis_y_degree=" + String.valueOf(180 * degree_Y / Math.PI));
//            L.e(TAG, "axis_z_degree=" + String.valueOf(180 * degree_Z / Math.PI));

            if (180 * degree_X / Math.PI > 40 && 180 * degree_X / Math.PI < 45) {
                    if (mUSBManager == null && !mUSBManager.isCameraOpened()) {
                        showShortMsg("抓拍异常，摄像头未开启");
                        return;
                    }
                    String picPath = USBCameraManager.ROOT_PATH + "camera0"
                            + USBCameraManager.SUFFIX_PNG;
                    mUSBManager.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {
                        @Override
                        public void onCaptureResult(final String path) {
                            showShortMsg("USBCameraActivity0  保存路径：" + path);
                            if (mUSBManager != null) {
                                mUSBManager.unregisterUSB();
                                mUSBManager.closeCamera();
                                mUSBManager.release();
                            }
                            upLoadImg(path, 1);
                        }
                    });
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 上传多张图片
     *
     * @param imgUrls 图片集合   正常开关门情况下有四张图片    每天凌晨自动拍照只有三张  img0冰箱门上的不会有
     */
    private void upLoadImg(final String imgUrls, int place) {

        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        File f = new File(imgUrls);

        builder.addFormDataPart("img1", f.getName(), RequestBody.create(MEDIA_TYPE_PNG, f))
                .addFormDataPart("img.pid", String.valueOf(ConstantPool.UserID))
                .addFormDataPart("img.menuId", String.valueOf(ConstantPool.FridgeId))
                .addFormDataPart("place", String.valueOf(place));

        MultipartBody requestBody = builder.build();

        final Request request = new Request.Builder()
                .url(ConstantPool.UpLoadInfo)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e(TAG, "img0 请求失败   " + call.request().toString() + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                L.e(TAG, "img0 请求成功   " + response.toString());
                USBCameraActivity0.this.setResult(RESULT_OK);
                USBCameraActivity0.this.finish();
            }
        });
    }
}
