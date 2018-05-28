package com.unistrong.dvr.camera2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.unistrong.dvr.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class TestCameraActivity extends Activity {

    private String mCameraId6 = "6";
    private String mCameraId7 = "7";
    private Size mPreviewSize = new Size(720, 480);
    private Size mCaptureSize = new Size(720, 480);
    private HandlerThread mCameraThread6;
    private HandlerThread mCameraThread7;
    private Handler mCameraHandler6;
    private Handler mCameraHandler7;
    private CameraDevice mCameraDevice6;
    private CameraDevice mCameraDevice7;
    private TextureView mTextureView6;
    private TextureView mTextureView7;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder6;
    private CaptureRequest.Builder mCaptureRequestBuilder7;
    private CaptureRequest mCaptureRequest6;
    private CaptureRequest mCaptureRequest7;
    private CameraCaptureSession mCameraCaptureSession6;
    private CameraCaptureSession mCameraCaptureSession7;
    private CameraManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera);
        mTextureView6 = (TextureView) findViewById(R.id.camera0);
        mTextureView7 = (TextureView) findViewById(R.id.camera1);
        mManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
       /* try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (mManager != null) mManager.openCamera(mCameraId6, mStateCallback6, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraThread6();
        startCameraThread7();
        if (!mTextureView6.isAvailable()) {
            mTextureView6.setSurfaceTextureListener(mTextureListener6);
        }
        if (!mTextureView7.isAvailable()) {
            mTextureView7.setSurfaceTextureListener(mTextureListener7);
        }
    }

    private void startCameraThread6() {
        mCameraThread6 = new HandlerThread("CameraThread");
        mCameraThread6.start();
        mCameraHandler6 = new Handler(mCameraThread6.getLooper());
    }
    private void startCameraThread7() {
        mCameraThread7 = new HandlerThread("CameraThread");
        mCameraThread7.start();
        mCameraHandler7 = new Handler(mCameraThread7.getLooper());
    }

    private TextureView.SurfaceTextureListener mTextureListener6 = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupImageReader6();
            openCamera6();
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
    };
    private TextureView.SurfaceTextureListener mTextureListener7 = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupImageReader7();
            openCamera7();
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
    };

    private void openCamera6() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (mManager != null) mManager.openCamera(mCameraId6, mStateCallback6, mCameraHandler6);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void openCamera7() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (mManager != null) mManager.openCamera(mCameraId7, mStateCallback7, mCameraHandler7);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback mStateCallback6 = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Toast.makeText(TestCameraActivity.this, "打开相机", Toast.LENGTH_SHORT).show();
            mCameraDevice6 = camera;
            startPreview6();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Toast.makeText(TestCameraActivity.this, "关闭相机", Toast.LENGTH_SHORT).show();
            camera.close();
            mCameraDevice6 = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Toast.makeText(TestCameraActivity.this, "相机出错", Toast.LENGTH_SHORT).show();
            camera.close();
            mCameraDevice6 = null;
        }
    };
    private CameraDevice.StateCallback mStateCallback7 = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Toast.makeText(TestCameraActivity.this, "打开相机", Toast.LENGTH_SHORT).show();
            mCameraDevice7 = camera;
            startPreview7();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Toast.makeText(TestCameraActivity.this, "关闭相机", Toast.LENGTH_SHORT).show();
            camera.close();
            mCameraDevice7 = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Toast.makeText(TestCameraActivity.this, "相机出错", Toast.LENGTH_SHORT).show();
            camera.close();
            mCameraDevice7 = null;
        }
    };

    private void startPreview6() {
        SurfaceTexture mSurfaceTexture = mTextureView6.getSurfaceTexture();
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            mCaptureRequestBuilder6 = mCameraDevice6.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder6.addTarget(previewSurface);
            mCameraDevice6.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        mCaptureRequest6 = mCaptureRequestBuilder6.build();
                        mCameraCaptureSession6 = session;
                        mCameraCaptureSession6.setRepeatingRequest(mCaptureRequest6, null, mCameraHandler6);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mCameraHandler6);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview7() {
        SurfaceTexture mSurfaceTexture = mTextureView7.getSurfaceTexture();
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            mCaptureRequestBuilder7 = mCameraDevice7.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder7.addTarget(previewSurface);
            mCameraDevice6.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        mCaptureRequest7 = mCaptureRequestBuilder7.build();
                        mCameraCaptureSession7 = session;
                        mCameraCaptureSession7.setRepeatingRequest(mCaptureRequest7, null, mCameraHandler7);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mCameraHandler7);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraCaptureSession6 != null) {
            mCameraCaptureSession6.close();
            mCameraCaptureSession6 = null;
        }
        if (mCameraCaptureSession7 != null) {
            mCameraCaptureSession7.close();
            mCameraCaptureSession7 = null;
        }

        if (mCameraDevice6 != null) {
            mCameraDevice6.close();
            mCameraDevice6 = null;
        }

        if (mCameraDevice7 != null) {
            mCameraDevice7.close();
            mCameraDevice7 = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void setupImageReader6() {
        //2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraHandler6.post(new imageSaver(reader.acquireNextImage()));
            }
        }, mCameraHandler6);
    }
    private void setupImageReader7() {
        //2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraHandler7.post(new imageSaver(reader.acquireNextImage()));
            }
        }, mCameraHandler7);
    }
    public static class imageSaver implements Runnable {

        private Image mImage;

        public imageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String path = Environment.getExternalStorageDirectory() + "/DCIM/CameraV2/";
            File mImageFile = new File(path);
            if (!mImageFile.exists()) {
                mImageFile.mkdir();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = path + "IMG_" + timeStamp + ".jpg";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileName);
                fos.write(data, 0, data.length);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
