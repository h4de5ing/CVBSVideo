package com.unistrong.rearvideo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CameraService extends Service {
    private static final String TAG = "gh0st1 CameraService";
    private static final String tvState = "/sys/devices/virtual/switch/tvd_signal/state";
    public static Camera mCamera;
    public static final int cameraid6 = 6;
    private Receiver mReceiver;

    public CameraService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //initTask();
        initCamera();
        IntentFilter filter = new IntentFilter("android.hardware.tvd.state.change");
        mReceiver = new Receiver();
        registerReceiver(mReceiver, filter);
        Log.i(TAG, "CameraService onCreate");
    }

    int currentstate = -1;

    private void initTask() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentstate != readState() && readState() == 1) startA();
                Log.i(TAG, "rear video  " + (readState() == 1 ? " on " : " off "));
            }
        }, 0, 1000);
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
/*            String action = intent.getAction();
            if ("android.hardware.tvd.state.change".equals(action)) {
                int cameraid = intent.getIntExtra("index", -1);
                int status = intent.getIntExtra("state", 0);
                Log.i(TAG, "status :" + status);
                if (cameraid == cameraid6) {
                    if (status == 1) startServiceActivity();
                    else stopServiceActivity();
                }
            }*/
        }
    }

    private void startServiceActivity() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraid6);
                Log.i(TAG, "open camera");
            } catch (Exception e) {
                Log.e(TAG, "camera is not available 检查相机权限");
            }
        }
        startA();
    }

    private void startA() {
        Intent intent = new Intent(this, ServiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void stopServiceActivity() {
        stopPreview();
        Intent intent = new Intent("android.intent.action.FINISH_SERVICE_ACTIVITY");
        sendBroadcast(intent);
    }

    private void initCamera() {
        Log.i(TAG, "initCamera");
        if (mCamera == null) {
            Log.i(TAG, "camera is null");
            try {
                mCamera = Camera.open(cameraid6);
                Log.i(TAG, "camera open");
            } catch (Exception e) {
                Log.d(TAG, "camera is not available");
            }
        }
    }

    class LocalBinder extends Binder {
        public CameraService getService() {
            return CameraService.this;
        }
    }

    public synchronized void startPreview(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "startPreview ");
        try {
            //mCamera = Camera.open(cameraid6);
            if (mCamera != null) {
                mCamera.startPreview();
                Thread.sleep(200);
                mCamera.setPreviewTexture(surfaceTexture);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            //mCamera.release();
            //mCamera = null;
            Log.i(TAG, "release camera");
        }
/*        try {
            mCamera = Camera.open(cameraid6);
            Log.i(TAG, "stopPreview camera open");
        } catch (Exception e) {
            Log.d(TAG, "stopPreview camera is not available");
        }*/
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public int readState() {
        int state = -1;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(tvState)));
            String s = reader.readLine();
            state = Integer.valueOf(s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state & 0xff;
    }
}
