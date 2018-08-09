package com.github.usbcamerarecored;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;

import com.example.twovideotest.IVideoCallback;
import com.example.twovideotest.VideoService;

public class MainActivity extends AppCompatActivity {
    private static final int cameraid0 = 0;
    private VideoService mService = null;
    private SurfaceTexture mSurfaceTexture0;
    private TextureView textureView0;
    private Chronometer mRecordTime;
    private ImageButton mRecordButton;
    private static final int UPDATE_RECORD_TIME = 1;
    private final Handler mHandler = new MainHandler();

    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_RECORD_TIME: {
                    if (mRecordTime != null) {
                        mRecordTime.setText((String) msg.obj);
                    }
                    break;
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView0 = (TextureView) findViewById(R.id.video);
        mRecordTime = (Chronometer) findViewById(R.id.tv_recording_time);
        mRecordButton = (ImageButton) findViewById(R.id.btn_record);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService != null) {
                    if (getRecordingState(cameraid0)) {
                        mService.stopVideoRecording(cameraid0);
                        mRecordTime.setVisibility(View.GONE);
                        mRecordButton.setImageResource(R.drawable.record_select);
                    } else {
                        mService.startVideoRecording(cameraid0, mSurfaceTexture0);
                        mRecordTime.setVisibility(View.VISIBLE);
                        mRecordButton.setImageResource(R.drawable.pause_select);
                    }
                }
            }
        });
        initVideoView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindVideoService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindVideoService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (getRecordingState(cameraid0)) {
                mService.stopVideoRecording(cameraid0);
                mRecordTime.setVisibility(View.GONE);
                mRecordButton.setImageResource(R.drawable.record_select);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!getRecordingState(cameraid0)) stopVideoService();
    }

    private ServiceConnection mVideoServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
            mService = ((VideoService.LocalBinder) obj).getService();
            initVideo();
            mService.registerCallback(mVideoCallback);
        }

        public void onServiceDisconnected(ComponentName classname) {
            if (mService != null) {
                mService.unregisterCallback(mVideoCallback);
            }
            mService = null;
        }
    };
    private IVideoCallback.Stub mVideoCallback = new IVideoCallback.Stub() {
        @Override
        public void onUpdateTimes(int index, String times) throws RemoteException {
            mHandler.removeMessages(UPDATE_RECORD_TIME);
            Message message = new Message();
            message.what = UPDATE_RECORD_TIME;
            message.obj = times;
            mHandler.sendMessage(message);
        }
    };

    private void initVideoView() {
        textureView0.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceTexture0 = surface;
                if (getRecordingState(cameraid0)) {
                    mService.startRender(cameraid0, surface);
                    mRecordButton.setImageResource(R.drawable.pause_select);
                    mRecordTime.setVisibility(View.VISIBLE);
                } else {
                    startPreview(cameraid0, surface);
                    mRecordButton.setImageResource(R.drawable.record_select);
                    mRecordTime.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });
    }

    private void initVideo() {
        if (mService != null) {
            startPreview(cameraid0, textureView0.getSurfaceTexture());
        }
    }

    private void bindVideoService() {
        Intent intent = new Intent(this, VideoService.class);
        bindService(intent, mVideoServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void unbindVideoService() {
        if (mVideoServiceConn != null) {
            unbindService(mVideoServiceConn);
        }
    }

    private boolean getRecordingState(int index) {
        if (mService != null)
            return mService.getRecordingState(index);
        return false;
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

    private void closeCamera(int cameraId) {
        if (mService != null) {
            mService.closeCamera(cameraId);
        }
    }

    private void startVideoService() {
        Intent intent = new Intent(MainActivity.this, VideoService.class);
        startService(intent);
    }

    private void stopVideoService() {
        Intent intent = new Intent(this, VideoService.class);
        stopService(intent);
    }
}
