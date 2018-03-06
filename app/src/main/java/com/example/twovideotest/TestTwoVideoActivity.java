package com.example.twovideotest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

/**
 * 只是单纯的预览 没有录制功能
 */
public class TestTwoVideoActivity extends AppCompatActivity {
    private static final String TAG = "gh0st2 TwoVideoActivity";
    private TextureView textureView6;
    private TextureView textureView7;
    private VideoService mService = null;
    private int cameraid6 = 6;
    private int cameraid7 = 7;
    private Receiver mReceiver;
    private Handler mHandler = new Handler();
    private static final String videoStateChange = "android.hardware.tvd.state.change";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testactivity_two_video);
        //Constacts.setZhi(Constacts.path, Constacts.zhi);
        //Constacts.setZhi(Constacts.path2, Constacts.zhi);
        startVideoService();
        View videoView0 = findViewById(R.id.video_0);
        View videoView1 = findViewById(R.id.video_1);
        textureView6 = (TextureView) videoView0.findViewById(R.id.video);
        textureView7 = (TextureView) videoView1.findViewById(R.id.video);
        //initVideoView();
        mReceiver = new Receiver();
        registerReceiver(mReceiver, new IntentFilter(videoStateChange));
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume ################");
        super.onResume();
        bindVideoService();
        //initVideo6();
        //initVideo7();
        initVideoView();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause ################");
        super.onPause();
        unbindVideoService();
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
/*             String action = intent.getAction();
            //Log.d(TAG, "action=" + action);
            if (videoStateChange.equals(action)) {
                int cameraid = intent.getIntExtra("index", -1);
                int status = intent.getIntExtra("state", 0);
                //Log.d(TAG, "cameraid=" + cameraid + " status=" + status);
                if (status == 1) {//上电
                   if (cameraid == 7) {
                        initVideo7();
                    } else if (cameraid == 6) {
                        initVideo6();
                    }
                }
            }*/
        }
    }


    private void startVideoService() {
        Log.d(TAG, "#############startVideoService####################");
        Intent intent = new Intent(this, VideoService.class);
        startService(intent);
    }

    private void stopVideoService() {
        Log.d(TAG, "###########stopVideoService##################");
        Intent intent = new Intent(this, VideoService.class);
        stopService(intent);
    }

    private ServiceConnection mVideoServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
            mService = ((VideoService.LocalBinder) obj).getService();
            initVideo6();
            initVideo7();
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private void bindVideoService() {
        Log.d(TAG, "bindVideoService###############");
        Intent intent = new Intent(this, VideoService.class);
        bindService(intent, mVideoServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void unbindVideoService() {
        if (mVideoServiceConn != null) {
            unbindService(mVideoServiceConn);
        }
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "##########onDestroy#############");
        if (mService != null) {
            stopVideoService();
        }
        if (mReceiver != null) unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    private void startPreview(int cameraId, SurfaceTexture surfaceTexture) {
        if (mService != null && (surfaceTexture != null)) {
            mService.startPreview(cameraId, surfaceTexture);
        }
    }

    private void stopPreview(int cameraId) {
        if (mService != null) {
            mService.stopPreview(cameraId);
        }
    }


    private void initVideo6() {
        if (mService != null) {
            //mService.stopPreview(cameraid6);
            //mService.closeCamera(cameraid6);
            startPreview(cameraid6, textureView6.getSurfaceTexture());
        }
    }

    private void initVideo7() {
        if (mService != null) {
            //mService.stopPreview(cameraid7);
            //mService.closeCamera(cameraid7);
            startPreview(cameraid7, textureView7.getSurfaceTexture());
        }
    }

    private void initVideoView() {
        textureView6.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                startPreview(cameraid6, surface);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTexture0  Destroyed ");
                mService.stopPreview(cameraid6);
                mService.closeCamera(cameraid6);
                return true;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        textureView7.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                startPreview(cameraid7, surface);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTexture1 destroy");
                mService.stopPreview(cameraid7);
                mService.closeCamera(cameraid7);
                return true;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
    }
}
