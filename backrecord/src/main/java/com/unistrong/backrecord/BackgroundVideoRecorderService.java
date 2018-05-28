package com.unistrong.backrecord;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.Date;

public class BackgroundVideoRecorderService extends Service {
    private WindowManager windowManager;
    private SurfaceView mSurfaceView;
    private Camera mCamera = null;
    private MediaRecorder mediaRecorder = null;

    public BackgroundVideoRecorderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Background Video Recorder")
                .setContentText("")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(1234, notification);
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        //windowManager.addView(surfaceView, layoutParams);
        //surfaceView.getHolder().addCallback(this);
    }

    public void startRecord(Camera camera, SurfaceView surfaceView) {
        mCamera = camera;
        mSurfaceView = surfaceView;
        camera = Camera.open();
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/" + DateFormat.format("yyyy-MM-dd_kk-mm-ss", new Date().getTime()) + ".mp4");
        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
        }
        mediaRecorder.start();
    }

    public void stopRecord() {
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mCamera.lock();
        mCamera.release();
        mSurfaceView = null;
    }
}
