package com.example.twovideotest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;

public class FourVideoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int VIDEO4 = 4;
    private static final int VIDEO5 = 5;
    private static final int VIDEO6 = 6;
    private static final int VIDEO7 = 7;
    private int cameraid4 = VIDEO4;
    private int cameraid5 = VIDEO5;
    private int cameraid6 = VIDEO6;
    private int cameraid7 = VIDEO7;
    private VideoService mService = null;
    private TextureView textureView4;
    private TextureView textureView5;
    private TextureView textureView6;
    private TextureView textureView7;
    private SurfaceTexture mSurfaceTexture4;
    private SurfaceTexture mSurfaceTexture5;
    private SurfaceTexture mSurfaceTexture6;
    private SurfaceTexture mSurfaceTexture7;
    private ImageButton mRecordButton4;
    private ImageButton mRecordButton5;
    private ImageButton mRecordButton6;
    private ImageButton mRecordButton7;
    private Chronometer mRecordTime4;
    private Chronometer mRecordTime5;
    private Chronometer mRecordTime6;
    private Chronometer mRecordTime7;

    @Override
    public void onClick(View view) {
        int tag = (int) view.getTag();
        Log.i("gh0st", "onClick:" + tag);
        switch (tag) {
            case VIDEO4:
                if (getRecordingState(cameraid4)) {
                    mService.stopVideoRecording(cameraid4);
                    mRecordTime4.setVisibility(View.GONE);
                    mRecordTime4.stop();
                    mRecordButton4.setImageResource(R.drawable.record_select);
                } else {
                    mService.startVideoRecording(cameraid4, mSurfaceTexture4);
                    mRecordTime4.setVisibility(View.VISIBLE);
                    mRecordTime4.setBase(SystemClock.elapsedRealtime());
                    mRecordTime4.start();
                    mRecordButton4.setImageResource(R.drawable.pause_select);
                }
                break;
            case VIDEO5:
                if (getRecordingState(cameraid5)) {
                    mService.stopVideoRecording(cameraid5);
                    mRecordTime5.setVisibility(View.GONE);
                    mRecordTime5.stop();
                    mRecordButton5.setImageResource(R.drawable.record_select);
                } else {
                    mService.startVideoRecording(cameraid5, mSurfaceTexture5);
                    mRecordTime5.setVisibility(View.VISIBLE);
                    mRecordTime5.setBase(SystemClock.elapsedRealtime());
                    mRecordTime5.start();
                    mRecordButton5.setImageResource(R.drawable.pause_select);
                }
                break;
            case VIDEO6:
                if (getRecordingState(cameraid6)) {
                    mService.stopVideoRecording(cameraid6);
                    mRecordTime6.setVisibility(View.GONE);
                    mRecordTime6.stop();
                    mRecordButton6.setImageResource(R.drawable.record_select);
                } else {
                    mService.startVideoRecording(cameraid6, mSurfaceTexture6);
                    mRecordTime6.setVisibility(View.VISIBLE);
                    mRecordTime6.setBase(SystemClock.elapsedRealtime());
                    mRecordTime6.start();
                    mRecordButton6.setImageResource(R.drawable.pause_select);
                }
                break;
            case VIDEO7:
                if (getRecordingState(cameraid7)) {
                    mService.stopVideoRecording(cameraid7);
                    mRecordTime7.setVisibility(View.GONE);
                    mRecordTime7.stop();
                    mRecordButton7.setImageResource(R.drawable.record_select);
                } else {
                    mService.startVideoRecording(cameraid7, mSurfaceTexture7);
                    mRecordTime7.setVisibility(View.VISIBLE);
                    mRecordTime7.setBase(SystemClock.elapsedRealtime());
                    mRecordTime7.start();
                    mRecordButton7.setImageResource(R.drawable.pause_select);
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four_video);
        Constants.zhi = (int) SPUtils.getSp(this, Constants.STANDARD_KEY, 1);
        View video4 = findViewById(R.id.video4);
        View video5 = findViewById(R.id.video5);
        View video6 = findViewById(R.id.video6);
        View video7 = findViewById(R.id.video7);
        textureView4 = (TextureView) video4.findViewById(R.id.video);
        textureView5 = (TextureView) video5.findViewById(R.id.video);
        textureView6 = (TextureView) video6.findViewById(R.id.video);
        textureView7 = (TextureView) video7.findViewById(R.id.video);
        mRecordButton4 = (ImageButton) video4.findViewById(R.id.recordbutton);
        mRecordButton5 = (ImageButton) video5.findViewById(R.id.recordbutton);
        mRecordButton6 = (ImageButton) video6.findViewById(R.id.recordbutton);
        mRecordButton7 = (ImageButton) video7.findViewById(R.id.recordbutton);
        mRecordTime4 = (Chronometer) video4.findViewById(R.id.tv_recording_time);
        mRecordTime5 = (Chronometer) video5.findViewById(R.id.tv_recording_time);
        mRecordTime6 = (Chronometer) video6.findViewById(R.id.tv_recording_time);
        mRecordTime7 = (Chronometer) video7.findViewById(R.id.tv_recording_time);
        mRecordButton4.setTag(VIDEO4);
        mRecordButton5.setTag(VIDEO5);
        mRecordButton6.setTag(VIDEO6);
        mRecordButton7.setTag(VIDEO7);
        mRecordButton4.setOnClickListener(this);
        mRecordButton5.setOnClickListener(this);
        mRecordButton6.setOnClickListener(this);
        mRecordButton7.setOnClickListener(this);
        startVideoService();
        initVideoView();
    }

    private void startVideoService() {
        Intent intent = new Intent(FourVideoActivity.this, VideoService.class);
        startService(intent);
    }

    private void stopVideoService() {
        Intent intent = new Intent(this, VideoService.class);
        stopService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindVideoService();
        //initVideoView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopPreview(cameraid6);
        //stopPreview(cameraid7);
        unbindVideoService();
    }

    private ServiceConnection mVideoServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
            mService = ((VideoService.LocalBinder) obj).getService();
            initVideo4();
            initVideo5();
            initVideo6();
            initVideo7();
        }

        public void onServiceDisconnected(ComponentName classname) {
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

    private boolean getRecordingState(int index) {
        if (mService != null)
            return mService.getRecordingState(index);
        return false;
    }

    private void initVideo4() {
        if (mService != null) {
            startPreview(cameraid4, textureView4.getSurfaceTexture());
        }
    }

    private void initVideo5() {
        if (mService != null) {
            startPreview(cameraid5, textureView5.getSurfaceTexture());
        }
    }

    private void initVideo6() {
        if (mService != null) {
            startPreview(cameraid6, textureView6.getSurfaceTexture());
        }
    }

    private void initVideo7() {
        if (mService != null) {
            startPreview(cameraid7, textureView7.getSurfaceTexture());
        }
    }

    private void initVideoView() {
        textureView4.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                mSurfaceTexture4 = surface;
                //startPreview(cameraid4, surface);
                if (mService != null) {
                    if (getRecordingState(cameraid4)) {
                        mService.startRender(cameraid4, surface);
                        mRecordButton4.setImageResource(R.drawable.pause_select);
                        mRecordTime4.setVisibility(View.VISIBLE);
                    } else {
                        startPreview(cameraid4, surface);
                        mRecordButton4.setImageResource(R.drawable.record_select);
                        mRecordTime4.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                //stopPreview(cameraid4);
                //closeCamera(cameraid4);
                if (mService != null) {
                    if (getRecordingState(cameraid4)) {
                        mService.stopRender(cameraid4);
                    } else {
                        mService.stopPreview(cameraid4);
                        mService.closeCamera(cameraid4);
                    }
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
        textureView5.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                mSurfaceTexture5 = surface;
                //startPreview(cameraid5, surface);
                if (mService != null) {
                    if (getRecordingState(cameraid5)) {
                        mService.startRender(cameraid5, surface);
                        mRecordButton5.setImageResource(R.drawable.pause_select);
                        mRecordTime5.setVisibility(View.VISIBLE);
                    } else {
                        startPreview(cameraid5, surface);
                        mRecordButton5.setImageResource(R.drawable.record_select);
                        mRecordTime5.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                //mService.stopPreview(cameraid5);
                //mService.closeCamera(cameraid5);
                if (mService != null) {
                    if (getRecordingState(cameraid5)) {
                        mService.stopRender(cameraid5);
                    } else {
                        mService.stopPreview(cameraid5);
                        mService.closeCamera(cameraid5);
                    }
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
        textureView6.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                mSurfaceTexture6 = surface;
                //startPreview(cameraid6, surface);
                if (mService != null) {
                    if (getRecordingState(cameraid6)) {
                        mService.startRender(cameraid6, surface);
                        mRecordButton6.setImageResource(R.drawable.pause_select);
                        mRecordTime6.setVisibility(View.VISIBLE);
                    } else {
                        startPreview(cameraid6, surface);
                        mRecordButton6.setImageResource(R.drawable.record_select);
                        mRecordTime6.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                //stopPreview(cameraid6);
                //closeCamera(cameraid6);
                if (mService != null) {
                    if (getRecordingState(cameraid6)) {
                        mService.stopRender(cameraid6);
                    } else {
                        mService.stopPreview(cameraid6);
                        mService.closeCamera(cameraid6);
                    }
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
                mSurfaceTexture7 = surface;
                //startPreview(cameraid7, surface);
                if (mService != null) {
                    if (getRecordingState(cameraid7)) {
                        mService.startRender(cameraid7, surface);
                        mRecordButton7.setImageResource(R.drawable.pause_select);
                        mRecordTime7.setVisibility(View.VISIBLE);
                    } else {
                        startPreview(cameraid7, surface);
                        mRecordButton7.setImageResource(R.drawable.record_select);
                        mRecordTime7.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                //mService.stopPreview(cameraid7);
                //mService.closeCamera(cameraid7);
                if (mService != null) {
                    if (getRecordingState(cameraid7)) {
                        mService.stopRender(cameraid7);
                    } else {
                        mService.stopPreview(cameraid7);
                        mService.closeCamera(cameraid7);
                    }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i("gh0st", "onBackPressed");
        if (mService != null) {
            try {
                if (getRecordingState(cameraid4)) {
                    mService.stopVideoRecording(cameraid4);
                    mRecordTime4.setVisibility(View.GONE);
                    mRecordButton4.setImageResource(R.drawable.record_select);
                }
                if (getRecordingState(cameraid5)) {
                    mService.stopVideoRecording(cameraid5);
                    mRecordTime5.setVisibility(View.GONE);
                    mRecordButton5.setImageResource(R.drawable.record_select);
                }
                if (getRecordingState(cameraid6)) {
                    mService.stopVideoRecording(cameraid6);
                    mRecordTime6.setVisibility(View.GONE);
                    mRecordButton6.setImageResource(R.drawable.record_select);
                }
                if (getRecordingState(cameraid7)) {
                    mService.stopVideoRecording(cameraid7);
                    mRecordTime7.setVisibility(View.GONE);
                    mRecordButton7.setImageResource(R.drawable.record_select);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!getRecordingState(cameraid4) && !getRecordingState(cameraid5) && !getRecordingState(cameraid6) && !getRecordingState(cameraid7))
            stopVideoService();
    }
}
