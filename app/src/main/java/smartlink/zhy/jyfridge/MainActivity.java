package smartlink.zhy.jyfridge;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


import smartlink.zhy.jyfridge.utils.L;

/**
 * 陀螺仪达到一定度数后自动跳转到拍照界面
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener{
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

            L.i(TAG + "axis_x_degree=" + String.valueOf(180 * degree_X / Math.PI));
            L.i(TAG + "axis_y_degree=" + String.valueOf(180 * degree_Y / Math.PI));
            L.i(TAG + "axis_z_degree=" + String.valueOf(180 * degree_Z / Math.PI));

            if (180 * degree_X / Math.PI > 40  && 180 * degree_X / Math.PI < 50) {
                startActivity(new Intent(MainActivity.this,USBCameraActivity.class));
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
