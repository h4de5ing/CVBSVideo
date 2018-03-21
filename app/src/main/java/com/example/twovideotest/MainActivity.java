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
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


//readme
/*for T3 Camera
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
 * 6:mIsSupport2Video true:2 camera false:1 camera
 *
 *
 * CSI or usb camera
 *video0
 *video1
 *video2
 *video3
 *
 *
 *CVBS Camera
 *video4
 *video5
 *video6
 *video7
 *
 *If you use sonix camera with two camera video ,please reference
 *android\device\softwinner\common\prebuild\CarVideo\
 *
 *This apk support:
 *camera0 preview (yuv,mjpeg)
 *camera1 recorder (720P or 1080p)
 *
 *
 *
 *
 *
 *
 *
 * */
public class MainActivity extends Activity implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener, View.OnClickListener {

    private static final String TAG = "gh0st MainActivity";
    private static final int MAX_NUM_OF_CAMERAS = 2;
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
    private static final int VIDEO0 = 0;
    private static final int VIDEO1 = 1;
    private static final int VIDEO2 = 2;
    private static final int VIDEO3 = 3;
    private static final int VIDEO4 = 4;
    private static final int VIDEO5 = 5;
    private static final int VIDEO6 = 6;
    private static final int VIDEO7 = 7;

    private int cameraid6 = VIDEO6;
    private int cameraid7 = VIDEO7;

    private static final boolean mIsSupport2Video = true;
    private static final String videoStateChange = "android.hardware.tvd.state.change";
    private final Handler mHandler = new MainHandler();

    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage message: " + msg.what);
            switch (msg.what) {
                case UPDATE_RECORD_TIME: {
                    //updateRecordingTime((String)msg.obj);
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
            //Log.d(TAG,"onUpdateTimes index=" + index);
            if (index == cameraid6) {
                message.what = UPDATE_RECORD_TIME;
            } else if (index == cameraid7) {
                message.what = UPDATE_RECORD_TIME1;
            }
            message.obj = times;
            mHandler.sendMessage(message);
        }
    };

    private ServiceConnection mVideoServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
            mService = ((VideoService.LocalBinder) obj).getService();
            initVideo6();
            initVideo7();
            if (mService != null) {
                mService.registerCallback(mVideoCallback);
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            if (mService != null) {
                mService.unregisterCallback(mVideoCallback);
            }
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
    protected void onResume() {
        Log.d(TAG, "onResume ################");
        super.onResume();
        bindVideoService();
        initVideoView();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause ################");
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
            unbindVideoService();
            stopVideoService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean getRecordingState(int index) {
        if (mService != null)
            return mService.getRecordingState(index);
        return false;
    }

    private void startPreview(int cameraId, SurfaceTexture surfaceTexture) {
        Log.i(TAG, "startPreview !=null ");
        if (mService != null && (surfaceTexture != null)) {
            Log.i(TAG, "mService !=null ");
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
        Log.d(TAG, "#############startVideoService####################");
        Intent intent = new Intent(MainActivity.this, VideoService.class);
        startService(intent);
    }

    private void stopVideoService() {
        Log.d(TAG, "###########stopVideoService##################");
        Intent intent = new Intent(this, VideoService.class);
        stopService(intent);
    }

    private TvStateReceiver mTvReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate start");
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Constacts.setZhi(Constacts.path, Constacts.zhi);
        Constacts.setZhi(Constacts.path2, Constacts.zhi);
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
        //initVideoView();
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                Log.d(TAG, "Intent action=" + arg1.getAction());
                int startRecord = arg1.getIntExtra("start", -1);
                int stopRecord = arg1.getIntExtra("stop", -1);
                Log.d(TAG, "startRecord=" + startRecord + " stopRecord=" + stopRecord);
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
        IntentFilter filter = new IntentFilter("com.android.twovideotest");
        registerReceiver(mReceiver, filter);
        mTvReceiver = new TvStateReceiver();
        registerReceiver(mTvReceiver, new IntentFilter(videoStateChange));
        Log.d(TAG, "onCreate finish");
        //initTask();
    }

    private static final String tvState = "/sys/devices/virtual/switch/tvd_signal/state";


    private void initTask() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentstate = readState();
            }
        }, 1000);
       /*    new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
             int state = readState();
                if (currentstate != state && state == 1) {
                    Log.i(TAG, "开启预览");
                    initVideo6();
                    initVideo7();
                } else if (currentstate != state && state == 0) {
                    stopPreview(cameraid6);
                    Log.i(TAG, "停止预览");
                }
                currentstate = state;
                Log.i(TAG, "two video  " + (state == 1 ? " on " : " off "));
            }
        }, 0, 1000);*/
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.recordbutton:
                if (new File(VideoStorage.TFCardPath).exists()) {
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
                    Toast.makeText(MainActivity.this, "请插入外置TF卡", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.recordbutton2:
                if (new File(VideoStorage.TFCardPath).exists()) {
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
                    Toast.makeText(MainActivity.this, "请插入外置TF卡", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    int currentstate = -1;

    private class TvStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
         /*   String action = intent.getAction();
            //Log.d(TAG, "action=" + action);
            if (videoStateChange.equals(action)) {
                int cameraid = intent.getIntExtra("index", -1);
                int status = intent.getIntExtra("state", 0);
                Log.d(TAG, "cameraid=" + cameraid + " status=" + status + " currentstate " + currentstate);
                if (currentstate == 0 && currentstate != status && status == 1) {
                    //Log.i(TAG, "重启app");
                    //RestartAPPTool.restartAPP(MainActivity.this, 0);
                }
                if (status == 1) {
                    if (cameraid == 6) {
                        initVideo6();
                    } else if (cameraid == 7) {
                        initVideo7();
                    }
                }
            }*/
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "##########onDestroy#############");
        if (mService != null) {
            if (!getRecordingState(cameraid6) && !getRecordingState(cameraid7)) {
                stopVideoService();
            }
        }
        if (mReceiver != null) unregisterReceiver(mReceiver);
        if (mTvReceiver != null) unregisterReceiver(mTvReceiver);
        super.onDestroy();
    }

    private TextureView textureView6;
    private TextureView textureView7;

    private void initVideoView() {
        Log.i(TAG, "initVideoView");
        textureView6.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                startPreview(cameraid6, surface);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTexture0  Destroyed ");
                if (mService != null) {
                    mService.stopPreview(cameraid6);
                    mService.closeCamera(cameraid6);
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
        textureView7.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                startPreview(cameraid7, surface);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTexture1 destroy");
                if (mService != null) {
                    mService.stopPreview(cameraid7);
                    mService.closeCamera(cameraid7);
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

    private void initVideo6() {
        Log.i(TAG, "initVideo6 mService == null   " + (mService == null));
        if (mService != null) {
            //stopPreview(cameraid6);
            //closeCamera(cameraid6);
            startPreview(cameraid6, textureView6.getSurfaceTexture());
        }
    }

    private void initVideo7() {
        if (mService != null) {
            //stopPreview(cameraid7);
            //closeCamera(cameraid7);
            startPreview(cameraid7, textureView7.getSurfaceTexture());
        }
    }

    @Override
    public void onInfo(MediaRecorder arg0, int arg1, int arg2) {

    }

    @Override
    public void onError(MediaRecorder arg0, int arg1, int arg2) {

    }
}
