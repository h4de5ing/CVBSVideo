package com.example.twovideotest;

import android.annotation.SuppressLint;
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
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class TwoVideoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "gh0st TwoVideoActivity";
    private static final int MAX_NUM_OF_CAMERAS = 2;
    private static final int UPDATE_RECORD_TIME = 1;
    private static final int HIDDEN_CTL_MENU_BAR = 2;
    private static final int UPDATE_RECORD_TIME1 = 3;
    private static final int mRecordTag0 = 0;
    private static final int mRecordTag1 = 1;
    private TextureView textureView0;
    private TextureView textureView1;
    private SurfaceTexture mSurfaceTexture0;
    private SurfaceTexture mSurfaceTexture1;
    private TextView mRecordTime0;
    private TextView mRecordTime1;
    private ImageButton mRecordButton0;
    private ImageButton mRecordButton1;
    private VideoService mService = null;
    private static final int VIDEO6 = 6;
    private static final int VIDEO7 = 7;
    private int cameraid0 = VIDEO6;
    private int cameraid1 = VIDEO7;
    private final Handler mHandler = new MainHandler();

    @Override
    public void onClick(View view) {
        switch ((int) view.getTag()) {
            case mRecordTag0:
                if (mService != null) {
                    int cameraid = mService.isUVCCameraSonix(cameraid0);
                    if (getRecordingState(cameraid)) {//如果正在录像,就停止录像
                        mService.stopVideoRecording(cameraid);
                        mRecordTime0.setVisibility(View.GONE);
                        mRecordButton0.setImageResource(R.drawable.record_select);
                    } else {
                        mService.startVideoRecording(cameraid, mSurfaceTexture0);
                        mRecordTime0.setVisibility(View.VISIBLE);
                        mRecordButton0.setImageResource(R.drawable.pause_select);
                    }
                }
                break;
            case mRecordTag1:
                if (mService != null) {
                    int cameraid = mService.isUVCCameraSonix(cameraid1);
                    if (getRecordingState(cameraid)) {
                        mService.stopVideoRecording(cameraid);
                        mRecordTime1.setVisibility(View.GONE);
                        mRecordButton1.setImageResource(R.drawable.record_select);
                    } else {
                        mService.startVideoRecording(cameraid, mSurfaceTexture1);
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
            Log.d(TAG, "handleMessage message: " + msg.what);
            switch (msg.what) {
                case UPDATE_RECORD_TIME:
                    mRecordTime0.setText((String) msg.obj);
                    break;
                case UPDATE_RECORD_TIME1:
                    mRecordTime1.setText((String) msg.obj);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_video);
        View videoView0 = findViewById(R.id.video_0);
        View videoView1 = findViewById(R.id.video_1);
        textureView0 = (TextureView) videoView0.findViewById(R.id.video);
        mRecordTime0 = (TextView) videoView0.findViewById(R.id.recording_time);
        mRecordButton0 = (ImageButton) videoView0.findViewById(R.id.recordbutton);
        textureView1 = (TextureView) videoView1.findViewById(R.id.video);
        mRecordTime1 = (TextView) videoView1.findViewById(R.id.recording_time);
        mRecordButton1 = (ImageButton) videoView1.findViewById(R.id.recordbutton);
        mRecordButton0.setTag(mRecordTag0);
        mRecordButton1.setTag(mRecordTag1);
        mRecordButton0.setOnClickListener(this);
        mRecordButton1.setOnClickListener(this);
        startVideoService();
        initVideoView();
        IntentFilter intentFilter = new IntentFilter("com.android.twovideotest");
        registerReceiver(RecordBroadcastReceiver, intentFilter);
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
        L.d(TAG, "bindVideoService###############");
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
        Log.d(TAG, "onResume ################");
        super.onResume();
        bindVideoService();

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause ################");
        super.onPause();
        unbindVideoService();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "##########onDestroy#############");
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
        unregisterReceiver(RecordBroadcastReceiver);
        super.onDestroy();
    }

    private IVideoCallback.Stub mVideoCallback = new IVideoCallback.Stub() {
        @Override
        public void onUpdateTimes(int index, String times) throws RemoteException {
            mHandler.removeMessages(UPDATE_RECORD_TIME);
            Message message = new Message();
            //Log.d(TAG,"onUpdateTimes index=" + index);
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
    private View.OnLayoutChangeListener mLayoutListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        }
    };

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

    public BroadcastReceiver RecordBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Intent action=" + intent.getAction());
            int startRecord = intent.getIntExtra("start", -1);
            int stopRecord = intent.getIntExtra("stop", -1);
            Log.d(TAG, "startRecord=" + startRecord + " stopRecord=" + stopRecord);
            if (startRecord == 0) {
                mService.startVideoRecording(cameraid0, mSurfaceTexture0);
                mRecordTime0.setVisibility(View.VISIBLE);
                mRecordButton0.setImageResource(R.drawable.pause_select);
            } else if (startRecord == 1) {
                mService.startVideoRecording(cameraid1, mSurfaceTexture1);
                mRecordTime1.setVisibility(View.VISIBLE);
                mRecordButton1.setImageResource(R.drawable.pause_select);
            } else if (startRecord == 2) {
                mService.startVideoRecording(cameraid0, mSurfaceTexture0);
                mRecordTime0.setVisibility(View.VISIBLE);
                mRecordButton0.setImageResource(R.drawable.pause_select);
                mService.startVideoRecording(cameraid1, mSurfaceTexture1);
                mRecordTime1.setVisibility(View.VISIBLE);
                mRecordButton1.setImageResource(R.drawable.pause_select);
            }
            if (stopRecord == 0) {
                mService.stopVideoRecording(cameraid0);
                mRecordTime0.setVisibility(View.GONE);
                mRecordButton0.setImageResource(R.drawable.record_select);
            } else if (stopRecord == 1) {
                mService.stopVideoRecording(cameraid1);
                mRecordTime1.setVisibility(View.GONE);
                mRecordButton1.setImageResource(R.drawable.record_select);
            } else if (stopRecord == 2) {
                mService.stopVideoRecording(cameraid0);
                mRecordTime0.setVisibility(View.GONE);
                mRecordButton0.setImageResource(R.drawable.record_select);
                mService.stopVideoRecording(cameraid1);
                mRecordTime1.setVisibility(View.GONE);
                mRecordButton1.setImageResource(R.drawable.record_select);
            }
        }
    };

    private void initVideoView() {
        textureView0.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceTexture0 = surface;
                if (mService != null) {
                    int cameraid = mService.isUVCCameraSonix(cameraid0);
                    Log.i("gh0st", "canmeraid:" + cameraid + ",width:" + width + ",height" + height);
                    if (cameraid == cameraid0) {
                        if (getRecordingState(cameraid0)) {
                            //startVideoRecording();
                            mService.startRender(cameraid0, surface);
                            mRecordButton0.setImageResource(R.drawable.pause_select);
                            mRecordTime0.setVisibility(View.VISIBLE);
                        } else {
                            startPreview(cameraid0, surface);
                            mRecordButton0.setImageResource(R.drawable.record_select);
                            mRecordTime0.setVisibility(View.GONE);
                        }
                    } else {
                        if (getRecordingState(cameraid)) {
                            //startVideoRecording();
                            startPreview(cameraid0, surface);
                            //mService.startRender(cameraid0, surface);
                            mRecordButton0.setImageResource(R.drawable.pause_select);
                            mRecordTime0.setVisibility(View.VISIBLE);
                        } else {
                            startPreview(cameraid0, surface);
                            mRecordButton0.setImageResource(R.drawable.record_select);
                            mRecordTime0.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mSurfaceTexture0 = null;
                Log.d(TAG, "onSurfaceTexture0  Destroyed ");
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
        textureView0.addOnLayoutChangeListener(mLayoutListener);
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
                Log.d(TAG, "onSurfaceTexture1 destroy");
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
        textureView1.addOnLayoutChangeListener(mLayoutListener);
    }
}
