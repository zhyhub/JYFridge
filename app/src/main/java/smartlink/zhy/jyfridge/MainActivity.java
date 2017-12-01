package smartlink.zhy.jyfridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import smartlink.zhy.jyfridge.bean.BaseEntity;
import smartlink.zhy.jyfridge.utils.BaseCallBack;
import smartlink.zhy.jyfridge.utils.BaseOkHttpClient;
import smartlink.zhy.jyfridge.utils.L;

/**
 * 陀螺仪达到一定度数后自动跳转到拍照界面
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = MainActivity.class.getSimpleName();
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

    private MainReceiver mainReceiver;

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
                BaseEntity entity = gson.fromJson(o.toString(),BaseEntity.class);
                Map<String,Object> map = entity.getResult().getIntents().get(0).getParameters();
                for (String key:map.keySet()){                        //遍历取出key，再遍历map取出value。
                    L.e(TAG,"key  " + key);
                    L.e(TAG,"map.get(key).toString()  " + map.get(key).toString());
                }
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

    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String txt = intent.getStringExtra("txt");
            L.e(TAG, "MainActivity MainReceiver 收到广播了 ++" + txt);
            sendMsg(txt);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainReceiver = new MainReceiver();
        registerReceiver(mainReceiver, new IntentFilter("smartlink.zhy.jyfridge.service"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        lasttimestamp = 0;
        degree_X = 0;
        degree_Y = 0;
        degree_Z = 0;
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        assert sm != null;
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

            if (180 * degree_X / Math.PI > 40 && 180 * degree_X / Math.PI < 50) {
                startActivity(new Intent(MainActivity.this, USBCameraActivity.class));
            }
        }
    }

    private double abs(double val) {
        if (val < 0) return -val;
        return val;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sm.unregisterListener(this);
        unregisterReceiver(mainReceiver);
        L.e(TAG, "onDestroy");

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
