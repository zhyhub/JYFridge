package smartlink.zhy.jyfridge.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jiangdg.usbcamera.USBCameraManager;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import smartlink.zhy.jyfridge.R;
import smartlink.zhy.jyfridge.utils.L;
import smartlink.zhy.jyfridge.utils.PreViewSize;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Camera.open;

/**
 * Created by mac on 2017/2/22 下午5:25.
 */

public class CameraFragment0 extends Fragment implements Camera.PreviewCallback,SensorEventListener {
    private static final String TAG = CameraFragment0.class.getSimpleName();

    private final int layout = R.layout.camerafragment;
    private PreViewSize previewSize;
    private int sw, sh;

    public SurfaceView cameraView;
    public SurfaceView drawView;
    private Camera camera = null;
    private SurfaceHolder surfaceHolder = null;
    private int cameraFacing = 0;
    private Context context;
    CapturrTask mCapturrTask ;
    public CameraFragment0() {

    }

    @SuppressLint("ValidFragment")
    private CameraFragment0(PreViewSize prewviewSize) {
        this.previewSize = prewviewSize;
    }

    public static CameraFragment0 newInstance(PreViewSize prewviewSize) {
        return new CameraFragment0(prewviewSize);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(layout, container, false);
        cameraView = (SurfaceView) view.findViewById(R.id.camera_view);
        drawView = (SurfaceView) view.findViewById(R.id.draw_view);
        return view;
    }


    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();

        L.e(TAG, "onResume");
        if (camera == null) {
            lasttimestamp = 0;
            degree_X = 0;
            degree_Y = 0;
            degree_Z = 0;
            sm = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
            mGyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (mGyroscope == null) {
                Toast.makeText(getActivity(), "您的设备不支持陀螺仪！", Toast.LENGTH_SHORT).show();
            } else {
                sm = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
                assert sm != null;
                sm.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            }
            surfaceHolder = cameraView.getHolder();
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    openCamera();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    //实现自动对焦
                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                initCamera();//实现相机的参数初始化
                                camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                            }
                        }

                    });
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    stopCamera();
                }
            });

            cameraView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  //  switchCamera();
                    takePhoto();
                }
            });
        } else {
            openCamera();
        }
    }

    private void takePhoto() {
        camera.setOneShotPreviewCallback(mPreviewCallback);
    }

    PreviewCallback mPreviewCallback = new PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera arg1) {
            if (null != mCapturrTask) {
                switch (mCapturrTask.getStatus()) {
                    case RUNNING:
                        return;
                    case PENDING:
                        mCapturrTask.cancel(false);
                        break;
                    case FINISHED:
                        break;
                    default:
                        break;
                }
            }
            mCapturrTask = new CapturrTask(data, camera, 0);
            mCapturrTask.execute((Void) null);

        }
    };
    private void openCamera() {
        stopCamera();
        camera = open(cameraFacing);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initCamera();
    }

    private void initCamera() {
        if (null != camera) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                camera.setParameters(parameters);
                startPreview();
                camera.cancelAutoFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startPreview() {
        camera.startPreview();
        camera.setPreviewCallbackWithBuffer(this);
        camera.addCallbackBuffer(new byte[((previewSize.width * previewSize.height) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
    }


    public void stopCamera() {
        Log.i("TAG" , "stopCamera camera = " + camera);
        if (null != camera) {
            camera.setPreviewCallbackWithBuffer(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
    }


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
    private double abs(double val) {
        if (val < 0) return -val;
        return val;
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

            L.e(TAG,"axis_x_degree=" + String.valueOf(180 * degree_X / Math.PI));
            L.e(TAG,"axis_y_degree=" + String.valueOf(180 * degree_Y / Math.PI));
            L.e(TAG,"axis_z_degree=" + String.valueOf(180 * degree_Z / Math.PI));


            if (180 * degree_X / Math.PI > 40  && 180 * degree_X / Math.PI < 50) {
                String picPath = USBCameraManager.ROOT_PATH+ System.currentTimeMillis()
                        + USBCameraManager.SUFFIX_PNG;
                takePhoto();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class CapturrTask extends AsyncTask<Void, Void, Bitmap> {

        private byte[] mData;
        Camera mCamera;
        int mId ;

        // 构造函数
        CapturrTask(byte[] data, Camera camera, int id) {
            this.mData = data;
            this.mCamera = camera;
            this.mId = id;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            // TODO Auto-generated method stub
            Size size = mCamera.getParameters().getPreviewSize(); // 获取预览大小
            final int w = size.width; // 宽度
            final int h = size.height;
            final YuvImage image = new YuvImage(mData, ImageFormat.NV21, w, h,
                    null);
            ByteArrayOutputStream os = new ByteArrayOutputStream(mData.length);
            if (!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)) {
                return null;
            }
            byte[] tmp = os.toByteArray();
            Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
            saveBitmap(bmp); // 实时处理预览帧
            return bmp;
        }

        /**
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
//            showPicture.setImageBitmap(bitmap);
        }

        // 该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
        @Override
        protected void onPreExecute() {
        }
    }
    public void saveBitmap(Bitmap bm) {
        File f = new File("/sdcard/png/", System.currentTimeMillis() + ".png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
