package com.unistrong.rearvideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "gh0st1 MainActivity";
    private TextureView video0;
    public static Camera mCamera;
    public static final int cameraid6 = 6;
    private static final String videoStateChange = "android.hardware.tvd.state.change";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCamera();
        video0 = (TextureView) findViewById(R.id.video);
        video0.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                if (mCamera != null) {
                    try {
                        mCamera.startPreview();
                        Thread.sleep(300);
                        mCamera.setPreviewTexture(surfaceTexture);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });
        mTvReceiver = new TvStateReceiver();
        registerReceiver(mTvReceiver, new IntentFilter(videoStateChange));
    }

    private TvStateReceiver mTvReceiver;
    int currentstate = -1;

    private class TvStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (videoStateChange.equals(action)) {
                int cameraid = intent.getIntExtra("index", -1);
                int status = intent.getIntExtra("state", 0);
                Log.d(TAG, "cameraid=" + cameraid + " status=" + status);
                if (currentstate != status && status == 1) {
                    //RestartAPPTool.restartAPP(MainActivity.this, 0);
                }
                currentstate = status;
            }
        }
    }

    private void initCamera() {
        Log.i(TAG, "initCamera");
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraid6);
                Camera.Parameters parameters = mCamera.getParameters();
                //parameters.setPreviewFormat(ImageFormat.NV21);
                Log.i(TAG, "camera open");
            } catch (Exception e) {
                Log.d(TAG, "camera is not available");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTvReceiver != null) {
            unregisterReceiver(mTvReceiver);
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
