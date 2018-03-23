package com.unistrong.dvr;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
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
                break;
            case tagRecording7:
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
        stopPreview();
    }

    private void initCamera() {
        try {
            if (mCamera6 == null) {
                mCamera6 = Camera.open(camera6ID);
            }
            if (mCamera7 == null) {
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
                            mCamera6.startPreview();
                            SystemClock.sleep(200);
                            mCamera6.setPreviewTexture(surface);
                        } catch (IOException e) {
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
                            mCamera7.startPreview();
                            SystemClock.sleep(200);
                            mCamera7.setPreviewTexture(surface);
                        } catch (IOException e) {
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
            Log.i(TAG, "stopPreview");
        }
        if (mCamera7 != null) {
            mCamera7.stopPreview();
            Log.i(TAG, "stopPreview");
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
        stopPreview();
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
}
