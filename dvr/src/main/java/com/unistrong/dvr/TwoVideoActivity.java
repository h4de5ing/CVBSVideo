package com.unistrong.dvr;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class TwoVideoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "gh0st2";
    public static Camera mCamera6;
    public static Camera mCamera7;
    public static final int camera6ID = 6;
    public static final int camera7ID = 7;
    private TextureView video6;
    private TextureView video7;
    private Button btnTakePicture6;
    private Button btnTakePicture7;
    private Button btnRecord6;
    private Button btnRecord7;
    private Chronometer tvRecordingTime6;
    private Chronometer tvRecordingTime7;
    private boolean isRecording6 = false;
    private boolean isRecording7 = false;
    private final static int tagTackPicture6 = 60;
    private final static int tagTackPicture7 = 70;
    private final static int tagRecording6 = 61;
    private final static int tagRecording7 = 71;
    public static String path6 = "/sys/devices/soc.0/1c33000.tvd2/tvd2_attr/tvd_system";
    public static String path7 = "/sys/devices/soc.0/1c34000.tvd3/tvd3_attr/tvd_system";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_video);

        View view6 = findViewById(R.id.video6);
        video6 = (TextureView) view6.findViewById(R.id.video);
        btnTakePicture6 = (Button) view6.findViewById(R.id.btn_take_picture);
        btnRecord6 = (Button) view6.findViewById(R.id.btn_record);
        tvRecordingTime6 = (Chronometer) view6.findViewById(R.id.tv_recording_time);

        View view7 = findViewById(R.id.video7);
        video7 = (TextureView) view7.findViewById(R.id.video);
        btnTakePicture7 = (Button) view7.findViewById(R.id.btn_take_picture);
        btnRecord7 = (Button) view7.findViewById(R.id.btn_record);
        tvRecordingTime7 = (Chronometer) view7.findViewById(R.id.tv_recording_time);
        btnTakePicture6.setTag(tagTackPicture6);
        btnTakePicture7.setTag(tagTackPicture7);
        btnRecord6.setTag(tagRecording6);
        btnRecord7.setTag(tagRecording7);
        //initCamera();
        //initTextureView();
        btnTakePicture6.setOnClickListener(this);
        btnTakePicture7.setOnClickListener(this);
        btnRecord6.setOnClickListener(this);
        btnRecord7.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int tag = (int) view.getTag();
        switch (tag) {
            case tagTackPicture6:
                takePicture6();
                break;
            case tagTackPicture7:
                takePicture7();
                break;
            case tagRecording6:
                record6();
                break;
            case tagRecording7:
                record7();
                break;
        }
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
/*        try {
            if (isRecording6) {
                tvRecordingTime6.stop();
                tvRecordingTime6.setVisibility(View.GONE);
                isRecording6 = false;
                btnRecord6.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                stopRecording6();
            }
            if (isRecording7) {
                tvRecordingTime7.stop();
                tvRecordingTime7.setVisibility(View.GONE);
                isRecording7 = false;
                btnRecord7.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                stopRecording7();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        stopPreview();
    }

    private void record6() {
        if (isRecording6) {
            tvRecordingTime6.setVisibility(View.GONE);
            isRecording6 = false;
            btnRecord6.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
            tvRecordingTime6.stop();
            stopRecording6();
        } else {
            if (startRecording6()) {
                tvRecordingTime6.setVisibility(View.VISIBLE);
                tvRecordingTime6.setBase(SystemClock.elapsedRealtime());
                tvRecordingTime6.start();
                btnRecord6.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                isRecording6 = true;
            } else {
                tvRecordingTime6.setVisibility(View.GONE);
                isRecording6 = false;
                btnRecord6.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                tvRecordingTime6.stop();
            }
        }
    }

    private void record7() {
        if (isRecording7) {
            tvRecordingTime7.setVisibility(View.GONE);
            isRecording7 = false;
            btnRecord7.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
            tvRecordingTime7.stop();
            stopRecording7();
        } else {
            if (startRecording7()) {
                tvRecordingTime7.setVisibility(View.VISIBLE);
                tvRecordingTime7.setBase(SystemClock.elapsedRealtime());
                tvRecordingTime7.start();
                btnRecord7.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                isRecording7 = true;
            } else {
                tvRecordingTime7.setVisibility(View.GONE);
                isRecording7 = false;
                btnRecord7.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                tvRecordingTime7.stop();
            }
        }
    }

    private MediaRecorder mMediaRecorder6;
    private MediaRecorder mMediaRecorder7;

    public boolean startRecording6() {
        if (prepareVideoRecorder6()) {
            mMediaRecorder6.start();
            return true;
        } else {
            releaseMediaRecorder6();
        }
        return false;
    }


    public void stopRecording6() {
        if (mMediaRecorder6 != null) {
            mMediaRecorder6.stop();
        }
        releaseMediaRecorder6();
    }

    public boolean startRecording7() {
        if (prepareVideoRecorder7()) {
            mMediaRecorder7.start();
            return true;
        } else {
            releaseMediaRecorder6();
        }
        return false;
    }


    public void stopRecording7() {
        if (mMediaRecorder7 != null) {
            mMediaRecorder7.stop();
        }
        releaseMediaRecorder6();
    }

    private boolean prepareVideoRecorder6() {
        try {
            mMediaRecorder6 = new MediaRecorder(1);
            mCamera6.unlock();
            mMediaRecorder6.setCamera(mCamera6);
            mMediaRecorder6.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder6.setOutputFormat(8);
            mMediaRecorder6.setVideoFrameRate(30);
            mMediaRecorder6.setVideoSize(720, 576);
            mMediaRecorder6.setVideoEncodingBitRate(6000000);//6M
            mMediaRecorder6.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder6.setOutputFile(FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_VIDEO).toString());
            mMediaRecorder6.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder6();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder6();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "Exception preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder6();
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
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder7();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder7();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "Exception preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder7();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder6() {
        if (mMediaRecorder6 != null) {
            mMediaRecorder6.reset();
            mMediaRecorder6.release();
            mMediaRecorder6 = null;
            mCamera6.lock();
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

    private void initCamera() {
        try {
            if (mCamera6 == null) {
                setStandard(path6, 1);//0 NTSC  1 PAL
                mCamera6 = Camera.open(camera6ID);
            }
            if (mCamera7 == null) {
                setStandard(path7, 1);//0 NTSC  1 PAL
                mCamera7 = Camera.open(camera7ID);
            }
        } catch (Exception e) {
            Log.e(TAG, "camera is not available check[android.permission.CAMERA]");
        }
    }

    public void initTextureView() {
        if (video6 != null) {
            video6.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    if (mCamera6 != null) {
                        try {
                            Log.i(TAG, "startPreview 6 start");
                            mCamera6.startPreview();
                            SystemClock.sleep(200);
                            mCamera6.setPreviewTexture(surface);
                            Log.i(TAG, "startPreview 6 end");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }
        if (video7 != null) {
            video7.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    if (mCamera7 != null) {
                        try {
                            Log.i(TAG, "startPreview 7 start");
                            mCamera7.startPreview();
                            SystemClock.sleep(200);
                            mCamera7.setPreviewTexture(surface);
                            Log.i(TAG, "startPreview 7 end");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }
    }

    public void stopPreview() {
        if (mCamera6 != null) {
            mCamera6.stopPreview();
            Log.i(TAG, "stopPreview 6 ");
        }
        if (mCamera7 != null) {
            mCamera7.stopPreview();
            Log.i(TAG, "stopPreview 7 ");
        }
    }

    public void closeCamera() {
        if (mCamera6 != null) {
            mCamera6.release();
            mCamera6 = null;
        }
        if (mCamera7 != null) {
            mCamera7.release();
            mCamera7 = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopPreview();
        closeCamera();
    }

    private void takePicture6() {
        if (mCamera6 != null) {
            mCamera6.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    File pictureFile = FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_IMAGE);
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
                    Log.i(TAG, "done:" + pictureFile.getAbsolutePath());
                    Toast.makeText(TwoVideoActivity.this, "take done 6", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void takePicture7() {
        if (mCamera7 != null) {
            mCamera7.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    File pictureFile = FileUtils.getOutputMediaFile(FileUtils.MEDIA_TYPE_IMAGE);
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
                    Log.i(TAG, "done:" + pictureFile.getAbsolutePath());
                    Toast.makeText(TwoVideoActivity.this, "take done 7", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void setStandard(String path, int value) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
            Log.i("gh0st", "path:" + path + ",write:" + value);
            writer.write("#" + value);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
