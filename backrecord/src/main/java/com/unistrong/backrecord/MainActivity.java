package com.unistrong.backrecord;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "gh0st";
    public static Camera mCamera;
    public static Camera mCamera7;
    public static final int cameraID = 6;
    public static final int camera7ID = 6;
    private Button record;
    private boolean isRecording = false;
    private Chronometer tvRecordingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvRecordingTime = (Chronometer) findViewById(R.id.tv_recording_time);
        record = (Button) findViewById(R.id.btn_record);
        record.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    private void initCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraID);
                Log.i(TAG, "open camera");
            } catch (Exception e) {
                Log.e(TAG, "camera is not available check[android.permission.CAMERA]");
            }
        }
        if (mCamera7 != null) {
            mCamera7.stopPreview();
            mCamera7.release();
            mCamera7 = null;
        }
        if (mCamera7 == null) {
            try {
                mCamera7 = Camera.open(camera7ID);
                Log.i(TAG, "open camera");
            } catch (Exception e) {
                Log.e(TAG, "camera is not available check[android.permission.CAMERA]");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record:
                record();
                break;
        }
    }

    private void record() {
        if (isRecording) {
            tvRecordingTime.setVisibility(View.GONE);
            isRecording = false;
            record.setText("录像");
            tvRecordingTime.stop();
            stopRecording();
            stopRecording7();
        } else {
            if (startRecording() && startRecording7()) {
                tvRecordingTime.setVisibility(View.VISIBLE);
                tvRecordingTime.setBase(SystemClock.elapsedRealtime());
                tvRecordingTime.start();
                record.setText("停止");
                isRecording = true;
            } else {
                tvRecordingTime.setVisibility(View.GONE);
                isRecording = false;
                record.setText("录像");
                tvRecordingTime.stop();
            }
        }
    }

    private MediaRecorder mMediaRecorder;
    private MediaRecorder mMediaRecorder7;

    public boolean startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    public boolean startRecording7() {
        if (prepareVideoRecorder7()) {
            mMediaRecorder7.start();
            return true;
        } else {
            releaseMediaRecorder7();
        }
        return false;
    }


    public void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
        }
        releaseMediaRecorder();
    }

    public void stopRecording7() {
        if (mMediaRecorder7 != null) {
            mMediaRecorder7.stop();
        }
        releaseMediaRecorder7();
    }

    private boolean prepareVideoRecorder() {
        try {
            mMediaRecorder = new MediaRecorder(1);
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(8);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(720, 576);
            mMediaRecorder.setVideoEncodingBitRate(6000000);//6M
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setOutputFile(FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_VIDEO).toString());
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "6 IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "6 IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "6 Exception preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private boolean prepareVideoRecorder7() {
        try {
            mMediaRecorder7 = new MediaRecorder(1);
            mCamera7.unlock();
            mMediaRecorder7.setCamera(mCamera7);
            mMediaRecorder7.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder7.setOutputFormat(8);
            mMediaRecorder7.setVideoFrameRate(30);
            mMediaRecorder7.setVideoSize(720, 576);
            mMediaRecorder7.setVideoEncodingBitRate(6000000);//6M
            mMediaRecorder7.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder7.setOutputFile(FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_VIDEO).toString());
            mMediaRecorder7.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "7 IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "7 IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "7 Exception preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private void releaseMediaRecorder7() {
        if (mMediaRecorder7 != null) {
            mMediaRecorder7.reset();
            mMediaRecorder7.release();
            mMediaRecorder7 = null;
            mCamera7.lock();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            stopRecording();
            stopRecording7();
        }
    }
}
