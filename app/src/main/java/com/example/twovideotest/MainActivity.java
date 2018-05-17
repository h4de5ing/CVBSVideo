package com.example.twovideotest;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


//readme
/*
 * 1:support 2 camera preview and recorder
 * 2:support recorder in background
 * {
 * 		startRender
 * 		stopRender
 * }
 * 3:support watemart turn on and turn off
 * {
 * 		mCameraDevice[index].startWaterMark();
 * }
 *
 * 4:support get cvbs status
 * {
 * 		int status = Camera.getCVBSInStatus(index);
 * }
 * 5:support notification when cvbs out or in
 *
 * {
 * 		IntentFilter filter = new IntentFilter("android.hardware.tvd.state.change");
		mReceiver = new Receiver();
		registerReceiver(mReceiver, filter);
 * }
 * 6:support cvbs set brightness and so on
 * {
 * 		mCameraDevice[index].setAnalogInputColor(67, 50, 100); //setting brightness and so on
 * }
 *
 *CVBS Camera
 *video6
 *video7
 *
  *This apk support:
 *camera0 preview (yuv,mjpeg)
 *camera1 recorder (720P or 1080p)
 *
 * */
public class MainActivity extends Activity implements View.OnClickListener {

    private static final int UPDATE_RECORD_TIME = 1;
    private static final int HIDDEN_CTL_MENU_BAR = 2;
    private static final int UPDATE_RECORD_TIME1 = 3;
    private VideoService mService = null;
    private ImageButton mRecordButton;
    private ImageButton mRecordButton1;
    private SurfaceTexture mSurfaceTexture0;
    private SurfaceTexture mSurfaceTexture1;
    private TextView mRecordTime;
    private TextView mRecordTime1;
    private BroadcastReceiver mReceiver;
    private static final int VIDEO6 = 6;
    private static final int VIDEO7 = 7;

    private int cameraid6 = VIDEO6;
    private int cameraid7 = VIDEO7;

    private static final boolean mIsSupport2Video = true;
    private final Handler mHandler = new MainHandler();

    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            L.d("handleMessage message: " + msg.what);
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
            }
        }
    }

    private IVideoCallback.Stub mVideoCallback = new IVideoCallback.Stub() {
        @Override
        public void onUpdateTimes(int index, String times) throws RemoteException {
            mHandler.removeMessages(UPDATE_RECORD_TIME);
            Message message = new Message();
            if (index == cameraid6) {
                message.what = UPDATE_RECORD_TIME;
            } else if (index == cameraid7) {
                message.what = UPDATE_RECORD_TIME1;
            }
            message.obj = times;
            mHandler.sendMessage(message);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.init(true, "CVBSCamera");
        setContentView(R.layout.activity_main);
        Constants.zhi = (int) SPUtils.getSp(MainActivity.this, Constants.STANDARD_KEY, 1);
        startVideoService();
        mRecordButton = (ImageButton) findViewById(R.id.recordbutton);
        mRecordTime = (TextView) findViewById(R.id.recording_time);
        mRecordTime1 = (TextView) findViewById(R.id.recording_time1);
        mRecordButton1 = (ImageButton) findViewById(R.id.recordbutton2);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.surface2);
        textureView6 = (TextureView) findViewById(R.id.video0);
        textureView7 = (TextureView) findViewById(R.id.video1);
        if (!mIsSupport2Video)
            layout.setVisibility(View.GONE);
        mRecordButton.setOnClickListener(this);
        mRecordButton1.setOnClickListener(this);
        register();
        IntentFilter filter = new IntentFilter("com.android.twovideotest");
        registerReceiver(mReceiver, filter);
    }

    private void register() {
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                L.d("Intent action=" + arg1.getAction());
                int startRecord = arg1.getIntExtra("start", -1);
                int stopRecord = arg1.getIntExtra("stop", -1);
                L.d("startRecord=" + startRecord + " stopRecord=" + stopRecord);
                if (startRecord == 0) {
                    mService.startVideoRecording(cameraid6, mSurfaceTexture0);
                    mRecordTime.setVisibility(View.VISIBLE);
                    mRecordButton.setImageResource(R.drawable.pause_select);
                } else if (startRecord == 1) {
                    mService.startVideoRecording(cameraid7, mSurfaceTexture1);
                    mRecordTime1.setVisibility(View.VISIBLE);
                    mRecordButton1.setImageResource(R.drawable.pause_select);
                } else if (startRecord == 2) {
                    mService.startVideoRecording(cameraid6, mSurfaceTexture0);
                    mRecordTime.setVisibility(View.VISIBLE);
                    mRecordButton.setImageResource(R.drawable.pause_select);

                    mService.startVideoRecording(cameraid7, mSurfaceTexture1);
                    mRecordTime1.setVisibility(View.VISIBLE);
                    mRecordButton1.setImageResource(R.drawable.pause_select);
                }

                if (stopRecord == 0) {
                    mService.stopVideoRecording(cameraid6);
                    mRecordTime.setVisibility(View.GONE);
                    mRecordButton.setImageResource(R.drawable.record_select);
                } else if (stopRecord == 1) {
                    mService.stopVideoRecording(cameraid7);
                    mRecordTime1.setVisibility(View.GONE);
                    mRecordButton1.setImageResource(R.drawable.record_select);

                } else if (stopRecord == 2) {
                    mService.stopVideoRecording(cameraid6);
                    mRecordTime.setVisibility(View.GONE);
                    mRecordButton.setImageResource(R.drawable.record_select);

                    mService.stopVideoRecording(cameraid7);
                    mRecordTime1.setVisibility(View.GONE);
                    mRecordButton1.setImageResource(R.drawable.record_select);
                }

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindVideoService();
        initVideoView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (getRecordingState(cameraid6)) {
                mService.stopVideoRecording(cameraid6);
                mRecordTime.setVisibility(View.GONE);
                mRecordButton.setImageResource(R.drawable.record_select);
            }
            if (getRecordingState(cameraid7)) {
                mService.stopVideoRecording(cameraid7);
                mRecordTime1.setVisibility(View.GONE);
                mRecordButton1.setImageResource(R.drawable.record_select);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopPreview(cameraid6);
        stopPreview(cameraid7);
        unbindVideoService();
    }

    @Override
    protected void onDestroy() {
        if (!getRecordingState(cameraid6) && !getRecordingState(cameraid7)) stopVideoService();
        if (mReceiver != null) unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    private ServiceConnection mVideoServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
            mService = ((VideoService.LocalBinder) obj).getService();
            initVideo6();
            initVideo7();
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

    private boolean getRecordingState(int index) {
        if (mService != null)
            return mService.getRecordingState(index);
        return false;
    }

    private void startPreview(int cameraId, SurfaceTexture surfaceTexture) {
        //L.i( "startPreview " + cameraId + " (mService != null) :" + (mService != null));
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


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.recordbutton:
                if (new File(VideoStorage.tfPath).exists()) {
                    if (mService != null) {
                        if (getRecordingState(cameraid6)) {
                            mService.stopVideoRecording(cameraid6);
                            mRecordTime.setVisibility(View.GONE);
                            mRecordButton.setImageResource(R.drawable.record_select);
                        } else {
                            mService.startVideoRecording(cameraid6, mSurfaceTexture0);
                            mRecordTime.setVisibility(View.VISIBLE);
                            mRecordButton.setImageResource(R.drawable.pause_select);
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, VideoStorage.tfPath + " not exists", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.recordbutton2:
                if (new File(VideoStorage.tfPath).exists()) {
                    if (mService != null) {
                        if (getRecordingState(cameraid7)) {
                            mService.stopVideoRecording(cameraid7);
                            mRecordTime1.setVisibility(View.GONE);
                            mRecordButton1.setImageResource(R.drawable.record_select);
                        } else {
                            mService.startVideoRecording(cameraid7, mSurfaceTexture1);
                            mRecordTime1.setVisibility(View.VISIBLE);
                            mRecordButton1.setImageResource(R.drawable.pause_select);
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, VideoStorage.tfPath + " not exists", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private TextureView textureView6;
    private TextureView textureView7;

    private void initVideoView() {
        textureView6.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                L.i("video6 1 initTextureView onSurfaceTextureAvailable");
                mSurfaceTexture0 = surface;
                startPreview(cameraid6, surface);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                L.d("onSurfaceTexture0  Destroyed ");
                stopPreview(cameraid6);
                closeCamera(cameraid6);
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
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                L.i("video7 1 initTextureView onSurfaceTextureAvailable");
                mSurfaceTexture1 = surface;
                startPreview(cameraid7, surface);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                L.d("onSurfaceTexture1 destroy");
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

    private void initVideo6() {
        L.i("initVideo6 " + (mService != null));
        if (mService != null) {
            startPreview(cameraid6, textureView6.getSurfaceTexture());
        }
    }

    private void initVideo7() {
        L.i("initVideo7 " + (mService != null));
        if (mService != null) {
            startPreview(cameraid7, textureView7.getSurfaceTexture());
        }
    }
}
