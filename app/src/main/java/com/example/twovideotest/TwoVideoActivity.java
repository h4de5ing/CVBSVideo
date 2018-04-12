package com.example.twovideotest;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TwoVideoActivity extends AppCompatActivity implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener, View.OnClickListener {
    private static final String TAG = "gh0sttwo";
    private static final int UPDATE_RECORD_TIME = 1;
    private static final int HIDDEN_CTL_MENU_BAR = 2;
    private static final int UPDATE_RECORD_TIME1 = 3;
    private VideoService mService = null;
    private ImageButton mRecordButton;
    private ImageButton mRecordButton1;
    private ImageButton mBackButton;
    private ImageButton mBackButton1;
    private SurfaceTexture mSurfaceTexture0;
    private SurfaceTexture mSurfaceTexture1;
    private TextView mRecordTime;
    private TextView mRecordTime1;
    private BroadcastReceiver mReceiver;
    private static final int VIDEO6 = 6;
    private static final int VIDEO7 = 7;

    private int cameraid0 = VIDEO6;
    private int cameraid1 = VIDEO7;

    private static final boolean mIsSupport2Video = true;

    private final Handler mHandler = new MainHandler();

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.recordbutton:
                int cameraid = mService.isUVCCameraSonix(cameraid0);
                if (getRecordingState(cameraid)) {
                    if (mService != null) {
                        mService.stopVideoRecording(cameraid);
                        mRecordTime.setVisibility(View.GONE);
                        mRecordButton.setImageResource(R.drawable.record_select);
                    }
                } else {
                    if (mService != null) {
                        mService.startVideoRecording(cameraid, mSurfaceTexture0);
                        mRecordTime.setVisibility(View.VISIBLE);
                        mRecordButton.setImageResource(R.drawable.pause_select);
                    }

                }
                break;
            case R.id.recordbutton2:
                if (getRecordingState(cameraid1)) {
                    if (mService != null) {
                        mService.stopVideoRecording(cameraid1);
                        mRecordTime1.setVisibility(View.GONE);
                        mRecordButton1.setImageResource(R.drawable.record_select);
                    }
                } else {
                    if (mService != null) {
                        mService.startVideoRecording(cameraid1, mSurfaceTexture1);
                        mRecordTime1.setVisibility(View.VISIBLE);
                        mRecordButton1.setImageResource(R.drawable.pause_select);
                    }

                }
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_RECORD_TIME: {
                    mRecordTime.setText((String) msg.obj);
                    break;
                }
                case UPDATE_RECORD_TIME1:
                    mRecordTime1.setText((String) msg.obj);
                    break;
                case HIDDEN_CTL_MENU_BAR: {
                }
                break;
                default:

                    break;
            }
        }
    }

    private IVideoCallback.Stub mVideoCallback = new IVideoCallback.Stub() {
        @Override
        public void onUpdateTimes(int index, String times) throws RemoteException {
            mHandler.removeMessages(UPDATE_RECORD_TIME);
            Message message = new Message();
            if (index != mService.isUVCCameraSonix(index)) {
                message.what = UPDATE_RECORD_TIME;
            } else if (index == cameraid0) {
                message.what = UPDATE_RECORD_TIME;
            } else {
                message.what = UPDATE_RECORD_TIME1;
            }
            message.obj = times;
            mHandler.sendMessage(message);
        }
    };

    private ServiceConnection mVideoServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
            mService = ((VideoService.LocalBinder) obj).getService();
            mService.registerCallback(mVideoCallback);
        }

        public void onServiceDisconnected(ComponentName classname) {
            if (mService != null) {
                mService.unregisterCallback(mVideoCallback);
            }
            mService = null;
        }
    };

    private void bindVideoService() {
        Intent intent = new Intent(this, VideoService.class);
        bindService(intent, mVideoServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void unbindVideoService() {
        if (mVideoServiceConn != null) {
            unbindService(mVideoServiceConn);
        }
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

    private void startVideoService() {
        Intent intent = new Intent(TwoVideoActivity.this, VideoService.class);
        startService(intent);
    }

    private void stopVideoService() {
        Intent intent = new Intent(this, VideoService.class);
        stopService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startVideoService();
        mRecordButton = (ImageButton) findViewById(R.id.recordbutton);
        mBackButton = (ImageButton) findViewById(R.id.settingbutton);
        mRecordTime = (TextView) findViewById(R.id.recording_time);
        mRecordTime1 = (TextView) findViewById(R.id.recording_time1);
        mRecordButton1 = (ImageButton) findViewById(R.id.recordbutton2);
        mBackButton1 = (ImageButton) findViewById(R.id.settingbutton2);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.surface2);
        mRecordButton.setOnClickListener(this);
        mRecordButton1.setOnClickListener(this);
        if (!mIsSupport2Video)
            layout.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mBackButton1.setVisibility(View.GONE);
        initVideoView();
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                Log.d(TAG, "Intent action=" + arg1.getAction());
                int startRecord = arg1.getIntExtra("start", -1);
                int stopRecord = arg1.getIntExtra("stop", -1);
                Log.d(TAG, "startRecord=" + startRecord + " stopRecord=" + stopRecord);
                if (startRecord == 0) {
                    mService.startVideoRecording(cameraid0, mSurfaceTexture0);
                    mRecordTime.setVisibility(View.VISIBLE);
                    mRecordButton.setImageResource(R.drawable.pause_select);
                } else if (startRecord == 1) {
                    mService.startVideoRecording(cameraid1, mSurfaceTexture1);
                    mRecordTime1.setVisibility(View.VISIBLE);
                    mRecordButton1.setImageResource(R.drawable.pause_select);
                } else if (startRecord == 2) {
                    mService.startVideoRecording(cameraid0, mSurfaceTexture0);
                    mRecordTime.setVisibility(View.VISIBLE);
                    mRecordButton.setImageResource(R.drawable.pause_select);

                    mService.startVideoRecording(cameraid1, mSurfaceTexture1);
                    mRecordTime1.setVisibility(View.VISIBLE);
                    mRecordButton1.setImageResource(R.drawable.pause_select);
                }


                if (stopRecord == 0) {
                    mService.stopVideoRecording(cameraid0);
                    mRecordTime.setVisibility(View.GONE);
                    mRecordButton.setImageResource(R.drawable.record_select);
                } else if (stopRecord == 1) {
                    mService.stopVideoRecording(cameraid1);
                    mRecordTime1.setVisibility(View.GONE);
                    mRecordButton1.setImageResource(R.drawable.record_select);

                } else if (stopRecord == 2) {
                    mService.stopVideoRecording(cameraid0);
                    mRecordTime.setVisibility(View.GONE);
                    mRecordButton.setImageResource(R.drawable.record_select);

                    mService.stopVideoRecording(cameraid1);
                    mRecordTime1.setVisibility(View.GONE);
                    mRecordButton1.setImageResource(R.drawable.record_select);
                }

            }
        };
        IntentFilter filter = new IntentFilter("com.android.twovideotest");
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        int cameraid = mService.isUVCCameraSonix(cameraid0);
        if (cameraid == cameraid0) {
            if (!getRecordingState(cameraid0) && !getRecordingState(cameraid1)) {
                stopVideoService();
            }
        } else {
            if (!getRecordingState(cameraid) && !getRecordingState(cameraid1)) {
                stopVideoService();
            }
        }

        unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    private void initVideoView() {
        TextureView textureView0 = (TextureView) findViewById(R.id.video0);
        textureView0.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceTexture0 = surface;
                int cameraid = mService.isUVCCameraSonix(cameraid0);
                if (cameraid == cameraid0) {
                    if (getRecordingState(cameraid0)) {
                        //startVideoRecording();
                        mService.startRender(cameraid0, surface);
                        mRecordButton.setImageResource(R.drawable.pause_select);
                        mRecordTime.setVisibility(View.VISIBLE);


                    } else {
                        startPreview(cameraid0, surface);
                        mRecordButton.setImageResource(R.drawable.record_select);
                        mRecordTime.setVisibility(View.GONE);
                    }
                } else {
                    if (getRecordingState(cameraid)) {
                        //startVideoRecording();
                        startPreview(cameraid0, surface);
                        //mService.startRender(cameraid0, surface);
                        mRecordButton.setImageResource(R.drawable.pause_select);
                        mRecordTime.setVisibility(View.VISIBLE);


                    } else {
                        startPreview(cameraid0, surface);
                        mRecordButton.setImageResource(R.drawable.record_select);
                        mRecordTime.setVisibility(View.GONE);
                    }
                }


            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mSurfaceTexture0 = null;
                if (mService.isUVCCameraSonix(cameraid0) == cameraid0) {
                    if (getRecordingState(cameraid0)) {
                        mService.stopRender(cameraid0);
                    } else {
                        mService.stopPreview(cameraid0);
                        mService.closeCamera(cameraid0);
                    }
                } else {
                    mService.stopPreview(cameraid0);
                    mService.closeCamera(cameraid0);
                }

                return true;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        if (mIsSupport2Video) {
            TextureView textureView1 = (TextureView) findViewById(R.id.video1);
            textureView1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    mSurfaceTexture1 = surface;


                    if (getRecordingState(cameraid1)) {
                        //startVideoRecording();
                        mService.startRender(cameraid1, surface);
                        mRecordButton1.setImageResource(R.drawable.pause_select);
                        mRecordTime1.setVisibility(View.VISIBLE);


                    } else {
                        startPreview(cameraid1, surface);
                        mRecordButton1.setImageResource(R.drawable.record_select);
                        mRecordTime1.setVisibility(View.GONE);
                    }


                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    mSurfaceTexture1 = null;

                    if (getRecordingState(cameraid1)) {
                        mService.stopRender(cameraid1);

                    } else {
                        mService.stopPreview(cameraid1);
                        mService.closeCamera(cameraid1);
                    }

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

    @Override
    public void onInfo(MediaRecorder arg0, int arg1, int arg2) {

    }

    @Override
    public void onError(MediaRecorder arg0, int arg1, int arg2) {
    }
}
