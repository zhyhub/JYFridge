package smartlink.zhy.jyfridge.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import smartlink.zhy.jyfridge.ConstantPool;
import smartlink.zhy.jyfridge.R;
import smartlink.zhy.jyfridge.utils.L;

/**
 * 陀螺仪达到一定度数后自动跳转到拍照界面
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainReceiver mainReceiver;

    private Handler handler = new Handler();

    private OkHttpClient client;
    private MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private List<String> imgUrls = new ArrayList<>();

    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isOpen = intent.getBooleanExtra("isOpen", false);
            L.e(TAG, "MainActivity MainReceiver 收到广播了 ++" + isOpen);
//            sendMsg(txt);

//            if(isOpen){
//            }else {
//            }
        }
    }

    private void startCamera_0() {
        Intent intent = new Intent(MainActivity.this, USBCameraActivity0.class);
        MainActivity.this.startActivityForResult(intent, ConstantPool.Camera_0);
    }

    private void startCamera_1() {
        Intent intent = new Intent(MainActivity.this, USBCameraActivity1.class);
        MainActivity.this.startActivityForResult(intent, ConstantPool.Camera_1);
    }

    private void startCamera_2() {
        Intent intent = new Intent(MainActivity.this, USBCameraActivity2.class);
        MainActivity.this.startActivityForResult(intent, ConstantPool.Camera_2);
    }

    private void startCamera_3() {
        Intent intent = new Intent(MainActivity.this, USBCameraActivity3.class);
        MainActivity.this.startActivityForResult(intent, ConstantPool.Camera_3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                switch (requestCode) {
                    case ConstantPool.Camera_0:
//                    L.e(TAG,"USBCameraActivity0  关闭了  打开USBCameraActivity1");
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            startCamera_1();
//                        }
//                    },2000);
                        String path0 = data.getStringExtra("img0");
                        imgUrls.add(path0);
                        break;
                    case ConstantPool.Camera_1:
                        L.e(TAG, "USBCameraActivity1  关闭了  打开USBCameraActivity2");
                        String path1 = data.getStringExtra("img1");
                        imgUrls.add(path1);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startCamera_2();
                            }
                        }, 2000);
                        break;
                    case ConstantPool.Camera_2:
                        L.e(TAG, "USBCameraActivity2  关闭了  打开USBCameraActivity3");
                        String path2 = data.getStringExtra("img2");
                        imgUrls.add(path2);
                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startCamera_3();
                            }
                        }, 2000);
                        break;
                    case ConstantPool.Camera_3:
                        L.e(TAG, "USBCameraActivity3  关闭了   准备上传图片");
                        String path3 = data.getStringExtra("img3");
                        imgUrls.add(path3);
                        mHandler.sendEmptyMessage(ConstantPool.Camera_3);
                        break;
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantPool.Camera_3:
                    if (imgUrls != null && imgUrls.size() != 0) {
                        upLoadImg(imgUrls);
                    }
                    break;
            }
        }
    };

    /**
     * 上传多张图片
     * @param imgUrls  图片集合   正常开关门情况下有四张图片    每天凌晨自动拍照只有三张  img0冰箱门上的不会有
     */
    private void upLoadImg(List<String> imgUrls) {
        if (imgUrls != null && imgUrls.size() != 0) {

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            for (String path : imgUrls) {
                builder.addFormDataPart("imgs", null, RequestBody.create(MEDIA_TYPE_PNG, new File(path)));
            }
            builder.addFormDataPart("img.pid", "123456");

            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url(ConstantPool.UpLoadInfo)//地址
                    .post(requestBody)//添加请求体
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    L.e(TAG, "上传失败:e.getLocalizedMessage() = " + e.getLocalizedMessage());

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    L.e(TAG, "上传照片成功：response = " + response.body().string());

                }
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient();
        mainReceiver = new MainReceiver();
        registerReceiver(mainReceiver, new IntentFilter("smartlink.zhy.jyfridge.service"));

        AppCompatButton button0 = findViewById(R.id.button_0);
        AppCompatButton button1 = findViewById(R.id.button_1);
        AppCompatButton button2 = findViewById(R.id.button_2);
        AppCompatButton button3 = findViewById(R.id.button_3);

        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera_0();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera_1();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera_2();
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera_3();
            }
        });
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
        unregisterReceiver(mainReceiver);
        L.e(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.e(TAG, "onPause");
    }

}
