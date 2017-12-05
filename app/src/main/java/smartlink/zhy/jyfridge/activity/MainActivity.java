package smartlink.zhy.jyfridge.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import smartlink.zhy.jyfridge.ConstantPool;
import smartlink.zhy.jyfridge.R;
import smartlink.zhy.jyfridge.utils.L;

/**
 * 陀螺仪达到一定度数后自动跳转到拍照界面
 */

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainReceiver mainReceiver;

    private AppCompatButton button0,button1,button2,button3;

//    private void sendMsg(String txt) {
//        BaseOkHttpClient.newBuilder()
//                .addParam("q", txt)
//                .addParam("app_key", ConstantPool.APP_KEY)
//                .addParam("user_id", "123456")
//                .get()
//                .url(ConstantPool.BASE_RUYI + "v1/message")
//                .build().enqueue(new BaseCallBack() {
//            @Override
//            public void onSuccess(Object o) {
//                L.e(TAG, "onSuccess" + o.toString());
//                Gson gson = new Gson();
//                BaseEntity entity = gson.fromJson(o.toString(),BaseEntity.class);
//                Map<String,Object> map = entity.getResult().getIntents().get(0).getParameters();
//                for (String key:map.keySet()){                        //遍历取出key，再遍历map取出value。
//                    L.e(TAG,"key  " + key);
//                    L.e(TAG,"map.get(key).toString()  " + map.get(key).toString());
//                }
//            }
//
//            @Override
//            public void onError(int code) {
//                L.e(TAG, "onError");
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                L.e(TAG, "onFailure");
//            }
//        });
//    }

    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isOpen = intent.getBooleanExtra("isOpen",false);
            L.e(TAG, "MainActivity MainReceiver 收到广播了 ++" + isOpen);
//            sendMsg(txt);

//            if(isOpen){
//                startCamera_0();
//            }else {
//                startCamera_1();
//            }
        }
    }

    private void startCamera_0() {
        Intent intent = new Intent(MainActivity.this, USBCameraActivity0.class);
        MainActivity.this.startActivityForResult(intent,ConstantPool.Camera_0);
    }

    private void startCamera_1() {
        Intent intent = new Intent(MainActivity.this, USBCameraActivity1.class);
        MainActivity.this.startActivityForResult(intent,ConstantPool.Camera_1);
    }

    private void startCamera_2() {
        Intent intent = new Intent(MainActivity.this, USBCameraActivity2.class);
        MainActivity.this.startActivity(intent);
        MainActivity.this.startActivityForResult(intent,ConstantPool.Camera_2);
    }

    private void startCamera_3() {
        Intent intent = new Intent(MainActivity.this, USBCameraActivity3.class);
        MainActivity.this.startActivity(intent);
        MainActivity.this.startActivityForResult(intent,ConstantPool.Camera_3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 0:
                    L.e(TAG,"USBCameraActivity0  关闭了  打开USBCameraActivity1");
//                    startCamera_1();
                    break;
                case 1:
                    L.e(TAG,"USBCameraActivity1  关闭了  打开USBCameraActivity2");
//                    startCamera_2();
                    break;
                case 2:
                    L.e(TAG,"USBCameraActivity2  关闭了  打开USBCameraActivity3");
//                    startCamera_3();
                    break;
                case 3:
                    L.e(TAG,"USBCameraActivity3  关闭了");
                    break;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainReceiver = new MainReceiver();
        registerReceiver(mainReceiver, new IntentFilter("smartlink.zhy.jyfridge.service"));

        button0 = findViewById(R.id.button_0);
        button1 = findViewById(R.id.button_1);
        button2 = findViewById(R.id.button_2);
        button3 = findViewById(R.id.button_3);

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
