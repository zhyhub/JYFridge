package smartlink.zhy.jyfridge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.joyoungdevlibrary.interface_sdk.CallBack;
import com.joyoungdevlibrary.interface_sdk.CommandCallBack;
import com.joyoungdevlibrary.utils.JoyoungDevLinkSDK;

import smartlink.zhy.jyfridge.R;
import smartlink.zhy.jyfridge.service.PlayerService;
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

        JoyoungDevLinkSDK.init(MainActivity.this, "12289", "01", new CommandCallBack() {
            @Override
            public void connectionLost(String msg) {
                L.e("connectionLost", "----------------" + msg);
            }

            @Override
            public void messageArrived(String msg) {
                L.e("messageArrived init ", "----------------" + msg);
            }

            @Override
            public void deliveryComplete(String token) {

            }
        }, new CallBack() {
            @Override
            public void onSuccess() {
                L.e(" new CallBack() ", "----------------  + onSuccess");
            }

            @Override
            public void onError() {
                L.e(" new CallBack() ", "----------------  + onError");

            }
        });


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
