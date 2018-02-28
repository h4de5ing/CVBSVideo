package com.unistrong.rearvideo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ServiceActivity extends AppCompatActivity {
    private static final String TAG = "gh0st1";
    private TextureView video0;
    private Receiver mReceiver;
    private Handler mHandler = new Handler();
    Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏导航栏
        window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        //params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        window.setAttributes(params);
        setContentView(R.layout.activity_service);
        video0 = (TextureView) findViewById(R.id.video);
        //startService(new Intent(this, CameraService.class));
        IntentFilter filter = new IntentFilter("android.intent.action.FINISH_SERVICE_ACTIVITY");
        mReceiver = new Receiver();
        registerReceiver(mReceiver, filter);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bindVideoService();
            }
        }, 500);
    }

    private void bindVideoService() {
        Intent intent = new Intent(this, CameraService.class);
        bindService(intent, mVideoServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void startPreview() {
        if (mService != null) {
            mService.startPreview(video0.getSurfaceTexture());
        }
    }

    private void stopPreview() {
        if (mService != null) {
            mService.stopPreview();
        }
    }

    private CameraService mService = null;
    private ServiceConnection mVideoServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
            mService = ((CameraService.LocalBinder) obj).getService();
            startPreview();
        }

        public void onServiceDisconnected(ComponentName classname) {
            if (mService != null) {
                stopPreview();
            }
            mService = null;
        }
    };

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.FINISH_SERVICE_ACTIVITY".equals(action)) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopService(new Intent(this, CameraService.class));
        if (mReceiver != null) unregisterReceiver(mReceiver);
        try {
            if (mService != null) unbindService(mVideoServiceConn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //stopPreview();
    }
}
