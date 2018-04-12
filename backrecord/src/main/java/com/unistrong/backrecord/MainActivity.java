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
    public static final int cameraID = 6;
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
        } else {
            if (startRecording()) {
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

    public boolean startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }


    public void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
        }
        releaseMediaRecorder();
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
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "Exception preparing MediaRecorder: " + e.getMessage());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            stopRecording();
        }
    }
}
