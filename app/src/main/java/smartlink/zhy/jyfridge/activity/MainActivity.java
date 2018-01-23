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

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, PlayerService.class));
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
