package smartlink.zhy.jyfridge.activity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.TimeUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import smartlink.zhy.jyfridge.ConstantPool;
import smartlink.zhy.jyfridge.R;
import smartlink.zhy.jyfridge.bean.PlayEvent;
import smartlink.zhy.jyfridge.bean.RemindBean;
import smartlink.zhy.jyfridge.bean.Song;
import smartlink.zhy.jyfridge.service.PlayerService;
import smartlink.zhy.jyfridge.service.VoiceService;
import smartlink.zhy.jyfridge.utils.L;

/**
 * 陀螺仪达到一定度数后自动跳转到拍照界面
 */

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();

//    private Handler handler = new Handler();

//    /**
//     * 打开陀螺仪摄像头
//     */
//    private void startCamera_0() {
//        Intent intent = new Intent(MainActivity.this, USBCameraActivity0.class);
//        MainActivity.this.startActivityForResult(intent, ConstantPool.Camera_0);
//    }
//
//    /**
//     * 打开普通摄像头1
//     */
//    private void startCamera_1() {
//        Intent intent = new Intent(MainActivity.this, USBCameraActivity1.class);
//        MainActivity.this.startActivityForResult(intent, ConstantPool.Camera_1);
//    }
//
//    /**
//     * 打开普通摄像头2
//     */
//    private void startCamera_2() {
//        Intent intent = new Intent(MainActivity.this, USBCameraActivity2.class);
//        MainActivity.this.startActivityForResult(intent, ConstantPool.Camera_2);
//    }
//
//    /**
//     * 打开普通摄像头3
//     */
//    private void startCamera_3() {
//        Intent intent = new Intent(MainActivity.this, USBCameraActivity3.class);
//        MainActivity.this.startActivityForResult(intent, ConstantPool.Camera_3);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            switch (requestCode) {
//                case ConstantPool.Camera_0:
//                    L.e(TAG, "USBCameraActivity0  关闭了  ");
////                    L.e(TAG,"USBCameraActivity0  关闭了  打开USBCameraActivity1");
////                    handler.postDelayed(new Runnable() {
////                        @Override
////                        public void run() {
////                            startCamera_1();
////                        }
////                    },2000);
//                    break;
//                case ConstantPool.Camera_1:
//                    L.e(TAG, "USBCameraActivity1  关闭了  打开USBCameraActivity2");
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            startCamera_2();
//                        }
//                    }, 2000);
//                    break;
//                case ConstantPool.Camera_2:
//                    L.e(TAG, "USBCameraActivity2  关闭了  打开USBCameraActivity3");
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            startCamera_3();
//                        }
//                    }, 2000);
//                    break;
//                case ConstantPool.Camera_3:
//                    L.e(TAG, "USBCameraActivity3  关闭了   ");
//                    break;
//            }
//        }
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, PlayerService.class));

//        AppCompatButton button0 = findViewById(R.id.button_0);
//        AppCompatButton button1 = findViewById(R.id.button_1);
//        AppCompatButton button2 = findViewById(R.id.button_2);
//        AppCompatButton button3 = findViewById(R.id.button_3);
//
//        button0.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startCamera_0();
//            }
//        });
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startCamera_1();
//            }
//        });
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startCamera_2();
//            }
//        });
//        button3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startCamera_3();
//            }
//        });

    }

    private Song getSong(String url) {
        Song song = new Song();
        song.setPath(url);
        return song;
    }

    @Override
    protected void onStart() {
        super.onStart();
        L.e(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.e(TAG, "onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.e(TAG, "onResume");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.e(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.e(TAG, "onPause");
    }

}
