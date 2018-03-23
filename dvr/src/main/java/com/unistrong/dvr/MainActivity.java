package com.unistrong.dvr;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "gh0st0";
    public static Camera mCamera;
    public static final int cameraID = 6;
    private TextureView video0;
    private Button record;
    private boolean isRecording = false;
    private Chronometer tvRecordingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        video0 = (TextureView) findViewById(R.id.video);
        tvRecordingTime = (Chronometer) findViewById(R.id.tv_recording_time);
        findViewById(R.id.btn_take_picture).setOnClickListener(this);
        record = (Button) findViewById(R.id.btn_record);
        record.setOnClickListener(this);
        CountDownTimer cdt = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
        initTextureView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
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
                startPreview(video0.getSurfaceTexture());
            } catch (Exception e) {
                Log.e(TAG, "camera is not available check[android.permission.CAMERA]");
            }
        }
    }

    public void startPreview(SurfaceTexture texture) {
        if (mCamera != null) {
            try {
                mCamera.startPreview();
                SystemClock.sleep(200);
                mCamera.setPreviewTexture(texture);
            } catch (IOException e) {
                Log.e(TAG, "startPreview IOException");
                e.printStackTrace();
            }
            Log.i(TAG, "startPreview");
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            Log.i(TAG, "stopPreview");
        }
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void initTextureView() {
        if (video0 != null) {
            video0.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    startPreview(surface);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    stopPreview();
                    closeCamera();
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_take_picture:
                takePicture();
                break;
            case R.id.btn_record:
                record();
                break;
        }
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private String outputMediaFileType;

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "DVR_VID_" + timeStamp + ".mp4");
            outputMediaFileType = "video/*";
        } else {
            outputMediaFileType = "image/*";
            return mediaFile = new File(mediaStorageDir.getPath() + File.separator + "DVR_" + timeStamp + ".dvr");
        }
        return mediaFile;
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (mCamera != null) {
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                        camera.startPreview();
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "拍照成功:" + pictureFile.getAbsolutePath());
                    Toast.makeText(MainActivity.this, "拍照成功", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 录像
     */
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
            mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
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
        stopPreview();
    }
}