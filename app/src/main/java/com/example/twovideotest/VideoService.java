package com.example.twovideotest;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.widget.Toast;

import com.example.twovideotest.VideoStorage.OnMediaSavedListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class VideoService extends Service implements MediaRecorder.OnErrorListener, ServiceData, MediaRecorder.OnInfoListener {

    private static final String TAG = "gh0st VideoService";

    private static final int SAVE_VIDEO = 8;
    private static final int MAX_NUM_OF_CAMERAS = 8;
    private final IBinder mBinder = new LocalBinder();
    private final Handler mHandler = new MainHandler();
    private MediaRecorder[] mMediaRecorder;
    private boolean[] mPreviewing; // True if preview is started.
    private boolean[] mMediaRecorderRecording;
    private ContentValues[] mCurrentVideoValues;
    private String[] mVideoFilename;
    private Camera[] mCameraDevice;
    private int[] mVideoWidth;
    private int[] mVideoHeight;
    private int[] mOutFormat;
    private int[] mFrameRate;
    private int[] mRecorderBitRate;
    private long[] mRecordingStartTime;
    private SurfaceTexture[] mSurfaceTexture;
    private Context mContext;
    private RemoteCallbackList<IVideoCallback> mCallbackList = new RemoteCallbackList<IVideoCallback>();
    private int mCameraUVC = -1;

    private void initVideoMemembers() {
        mCameraUVC = -1;
        mCameraDevice = new Camera[MAX_NUM_OF_CAMERAS];
        mSurfaceTexture = new SurfaceTexture[MAX_NUM_OF_CAMERAS];
        mPreviewing = new boolean[MAX_NUM_OF_CAMERAS];
        mMediaRecorderRecording = new boolean[MAX_NUM_OF_CAMERAS];
        mMediaRecorder = new MediaRecorder[MAX_NUM_OF_CAMERAS];
        mVideoFilename = new String[MAX_NUM_OF_CAMERAS];
        mRecordingStartTime = new long[MAX_NUM_OF_CAMERAS];
        mCurrentVideoValues = new ContentValues[MAX_NUM_OF_CAMERAS];
        mVideoWidth = new int[MAX_NUM_OF_CAMERAS];
        mVideoHeight = new int[MAX_NUM_OF_CAMERAS];
        mOutFormat = new int[MAX_NUM_OF_CAMERAS];
        mFrameRate = new int[MAX_NUM_OF_CAMERAS];
        mRecorderBitRate = new int[MAX_NUM_OF_CAMERAS];
        for (int i = 0; i < MAX_NUM_OF_CAMERAS; i++) {
            mCameraDevice[i] = null;
            mSurfaceTexture[i] = null;
            mPreviewing[i] = false;
            mMediaRecorderRecording[i] = false;
            mRecordingStartTime[i] = 0;
            mCurrentVideoValues[i] = null;
            if (i == 0) {
                mVideoWidth[i] = 1280;
                mVideoHeight[i] = 720;
            } else if (i >= 4 && i <= 7) {
                mVideoWidth[i] = 720;
                mVideoHeight[i] = 480;
            }

            mOutFormat[i] = VideoStorage.OUTPUTFORMAT;
            mFrameRate[i] = 30;
            mRecorderBitRate[i] = 6000000;//6M
        }
    }


    public void setNextFileName(int index) {
        if (mMediaRecorder[index] != null) {
            saveVideo(index);
            DeleteFileThread thread = new DeleteFileThread(index);
            thread.start();
        }
    }

    synchronized public void handleFileDelete(int index) {
        //空间不够将会删除文件在录制下一个文件
        Log.d(TAG, "handleFileDelete video" + index + " start");
        if (!VideoStorage.storageSpaceIsAvailable(getContentResolver(), mOutFormat[index])) {
            Log.e(TAG, "Not enough storage space!!!");
            Toast.makeText(mContext, "Not enough storage space!!!", Toast.LENGTH_LONG).show();
            //stopVideoRecording();
        }
        mVideoFilename[index] = generateVideoFilename(index, mOutFormat[index]);

        Log.d(TAG, "setNextFileName video" + index + "=" + mVideoFilename[index]);
        mRecordingStartTime[index] = SystemClock.uptimeMillis();
        try {
            mMediaRecorder[index].setNextSaveFile(mVideoFilename[index]);
        } catch (IOException ex) {
            Log.e(TAG, "setNextSaveFile failed");
        }
        Log.d(TAG, "handleFileDelete video" + index + " end");
    }

    class DeleteFileThread extends Thread {
        private int mIndex;

        public DeleteFileThread(int index) {
            mIndex = index;
        }

        @Override
        public void run() {
            handleFileDelete(mIndex);
        }
    }

    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SAVE_VIDEO:
                    Log.d(TAG, "MainHandler handleMessage SAVE_VIDEO");
                    setNextFileName(msg.arg1);
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    updateRecordingTime(msg.what);
                    break;
                default:
                    break;
            }

        }
    }

    private OnMediaSavedListener mMediaSavedListener = new OnMediaSavedListener() {

        @Override
        public void onMediaSaved(Uri uri) {
            Log.d(TAG, "onMediaSaved uri=" + uri);
        }
    };

    public class LocalBinder extends Binder {
        public VideoService getService() {
            return VideoService.this;
        }
    }

    private static String millisecondToTimeString(long milliSeconds, boolean displayCentiSeconds) {
        long seconds = milliSeconds / 1000; // round down to compute seconds
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (hours * 60);
        long remainderSeconds = seconds - (minutes * 60);
        StringBuilder timeStringBuilder = new StringBuilder();
        // Hours
        if (hours > 0) {
            if (hours < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(hours);
            timeStringBuilder.append(':');
        }

        // Minutes
        if (remainderMinutes < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderMinutes);
        timeStringBuilder.append(':');
        // Seconds
        if (remainderSeconds < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderSeconds);
        // Centi seconds
        if (displayCentiSeconds) {
            timeStringBuilder.append('.');
            long remainderCentiSeconds = (milliSeconds - seconds * 1000) / 10;
            if (remainderCentiSeconds < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(remainderCentiSeconds);
        }
        return timeStringBuilder.toString();
    }

    private void updateRecordingTime(int index) {
        //Log.d(TAG,"updateRecordingTime mMediaRecorderRecording=" + mMediaRecorderRecording);
        if (!mMediaRecorderRecording[index]) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        long deltaAdjusted = now - mRecordingStartTime[index];
        String text;
        long targetNextUpdateDelay;
        text = millisecondToTimeString(deltaAdjusted, false);
        targetNextUpdateDelay = 1000;
        if (deltaAdjusted >= 1000 * 60) {// 1 minute
            mRecordingStartTime[index] = now;
            Message message = new Message();
            message.what = SAVE_VIDEO;
            message.arg1 = index;
            mHandler.sendMessage(message);
        }
        onUpdateTimes(index, text);
        long actualNextUpdateDelay = targetNextUpdateDelay - (deltaAdjusted % targetNextUpdateDelay);
        mHandler.sendEmptyMessageDelayed(index, actualNextUpdateDelay);
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.e(TAG, "MediaRecorder error. what=" + what + ". extra=" + extra);
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.e(TAG, "MediaRecorder.OnInfoListener onInfo what=" + what);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            Message message = new Message();
            message.what = SAVE_VIDEO;
            mHandler.sendMessage(message);
        }
    }

    public int openCamera(int index) {
        if (mCameraDevice[index] != null) {
            Log.d(TAG, "openCamera222 index=" + index);
            return 0;
        }
        try {
            mCameraDevice[index] = Camera.open(index);
        } catch (Exception ex) {
            mCameraDevice[index] = null;
            Log.e(TAG, "Camera id=" + index + " does not exist! can not be opened.");
            return -1;
        }
        return 0;
    }

    public void closeCamera(int index) {
        Log.d(TAG, "closeCamera");
        if (mCameraDevice[index] == null) {
            Log.d(TAG, "already stopped.");
            return;
        }
        mCameraDevice[index].setErrorCallback(null);
        mCameraDevice[index].release();
        mCameraDevice[index] = null;
        mPreviewing[index] = false;
        mMediaRecorderRecording[index] = false;
        Log.d(TAG, "closeCamera mMediaRecorderRecording=false");
    }

    public int startRender(int index, SurfaceTexture surfaceTexture) {
        Log.d(TAG, "startRender " + index);
        if (mCameraDevice[index] == null)
            return -1;
        try {
            mCameraDevice[index].setPreviewTexture(surfaceTexture);
            mCameraDevice[index].startRender();
        } catch (IOException ex) {
            Log.e(TAG, "startRender error" + ex);
        }
        return 0;
    }

    public int stopRender(int index) {
        if (mCameraDevice[index] == null)
            return -1;
        mCameraDevice[index].stopRender();
        return 0;
    }

    public synchronized int startPreview(int index, SurfaceTexture surfaceTexture) {
        int state = openCamera(index);
        Log.d(TAG, "startPreview index=" + index + " openCamera:" + state);
        if (state == -1) {
            return FAIL;
        } else {
            if (mPreviewing[index]) {
                try {
                    SystemClock.sleep(300);
                    mCameraDevice[index].setPreviewTexture(surfaceTexture);
                } catch (IOException ex) {
                    mPreviewing[index] = false;
                    //closeCamera();
                    Log.e(TAG, "startPreview failed", ex);
                    return FAIL;
                }
            } else {
                Log.d(TAG, "mCameraDevice not previewing");
                try {
                    //mCameraDevice[index].setDisplayOrientation(180);
                    mCameraDevice[index].startPreview();
                    SystemClock.sleep(500);
                    mCameraDevice[index].setPreviewTexture(surfaceTexture);
                    mPreviewing[index] = true;
                } catch (IOException ex) {
                    mPreviewing[index] = false;
                    //closeCamera();
                    Log.e(TAG, "startPreview failed", ex);
                    return FAIL;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return OK;
    }

    public int stopPreview(int index) {
        if (!mPreviewing[index]) return BAD_VALUE;
        if (mCameraDevice[index] == null)
            return BAD_VALUE;
        mCameraDevice[index].stopPreview();
        mPreviewing[index] = false;
        return OK;
    }

    public void stopService() {
        Log.d(TAG, "StopService()");
        stopSelf();
    }

    private void pauseAudioPlayback() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);
    }

    @SuppressLint("SimpleDateFormat")
    private String createName(int index, long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("'VID'_yyyyMMdd_HHmmss_" + index);
        return dateFormat.format(date);
    }

    private void deleteVideoFile(String fileName) {
        Log.d(TAG, "Deleting video " + fileName);
        File f = new File(fileName);
        if (!f.delete()) {
            Log.v(TAG, "Could not delete " + fileName);
        }
    }

    private void saveVideo(int index) {
        long duration = SystemClock.uptimeMillis() - mRecordingStartTime[index];
        Log.i("saveVideo", "saveVideo" + mVideoFilename[index]);
        VideoStorage.addVideo(mVideoFilename[index], duration, mCurrentVideoValues[index], mMediaSavedListener, getContentResolver());
    }

    private String generateVideoFilename(int index, int outputFileFormat) {
        long dateTaken = System.currentTimeMillis();
        String title = createName(index, dateTaken);
        // Used when emailing.
        String filename = title + VideoStorage.convertOutputFormatToFileExt(outputFileFormat);
        String mime = VideoStorage.convertOutputFormatToMimeType(outputFileFormat);
        String path = VideoStorage.DIRECTORY + File.separator + filename;

        mCurrentVideoValues[index] = new ContentValues(9);
        mCurrentVideoValues[index].put(Video.Media.TITLE, title);
        mCurrentVideoValues[index].put(Video.Media.DISPLAY_NAME, filename);
        mCurrentVideoValues[index].put(Video.Media.DATE_TAKEN, dateTaken);
        mCurrentVideoValues[index].put(MediaColumns.DATE_MODIFIED, dateTaken / 1000);
        mCurrentVideoValues[index].put(Video.Media.MIME_TYPE, mime);
        mCurrentVideoValues[index].put(Video.Media.DATA, path);
        mCurrentVideoValues[index].put(Video.Media.RESOLUTION, Integer.toString(mVideoWidth[index]) + "x" + Integer.toString(mVideoHeight[index]));
        return path;
    }

    private void initializeRecorder(int index) {
        Log.d(TAG, "initializeRecorder:" + index);
        if (mCameraDevice[index] == null) return;
        mMediaRecorder[index] = new MediaRecorder(1);
        // Unlock the camera object before passing it to media recorder.
        mCameraDevice[index].unlock();
        mMediaRecorder[index].setCamera(mCameraDevice[index]);
        mMediaRecorder[index].setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder[index].setOutputFormat(mOutFormat[index]);
        mMediaRecorder[index].setVideoFrameRate(mFrameRate[index]);
        Parameters parameters = null;
        try {
            parameters = mCameraDevice[index].getParameters();
        } catch (Exception ex) {
            Log.e(TAG, "getParameters:" + ex);
        }

        if (parameters != null) {
            List<Size> sizes = null;
            sizes = parameters.getSupportedVideoSizes();
            Iterator<Size> it;
            if ((index >= 4) && (index <= 7)) { //cvbs must be 720*480 or 720*576
                if (sizes == null) {
                    mMediaRecorder[index].setVideoSize(720, 480);
                } else { // Driver supports separates outputs for preview and video.
                    it = sizes.iterator();
                    Size size = it.next();
                    Log.d(TAG, "size.width=" + size.width + " size.height= " + size.height);
                    mMediaRecorder[index].setVideoSize(size.width, size.height);
                }
            } else {
                int width = mVideoWidth[index];
                int height = mVideoHeight[index];
                boolean first = true;
                if (sizes != null) {
                    it = sizes.iterator();
                    while (it.hasNext()) {
                        Size size = it.next();
                        Log.d(TAG, "find size.width=" + size.width + " size.height=" + size.height);
                        if (first) {
                            width = size.width;
                            height = size.height;
                            first = false;
                        }
                        if ((size.width == mVideoWidth[index]) && (size.height == mVideoHeight[index])) {
                            width = size.width;
                            height = size.height;
                            break;
                        }
                    }
                }
                Log.d(TAG, "video size.width=" + width + " size.height= " + height);
                mMediaRecorder[index].setVideoSize(width, height);
            }
        } else {
            Log.d(TAG, "video set default size.width=" + mVideoWidth[index] + " size.height= " + mVideoHeight[index]);
            mMediaRecorder[index].setVideoSize(mVideoWidth[index], mVideoHeight[index]);
        }
        mMediaRecorder[index].setVideoEncodingBitRate(mRecorderBitRate[index]);
        mMediaRecorder[index].setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mVideoFilename[index] = generateVideoFilename(index, mOutFormat[index]);
        Log.d(TAG, "mVideoFilename= " + mVideoFilename[index]);
        mMediaRecorder[index].setOutputFile(mVideoFilename[index]);
        try {
            mMediaRecorder[index].prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare failed for " + mVideoFilename[index], e);
            releaseMediaRecorder(index);
            throw new RuntimeException(e);
        }
        mMediaRecorder[index].setOnErrorListener(this);
        mMediaRecorder[index].setOnInfoListener(this);
    }

    private void cleanupEmptyFile(int index) {
        if (mVideoFilename[index] != null) {
            File f = new File(mVideoFilename[index]);
            if (f.length() == 0 && f.delete()) {
                Log.v(TAG, "Empty video file deleted: " + mVideoFilename[index]);
                mVideoFilename[index] = null;
            }
        }
    }

    private void releaseMediaRecorder(int index) {
        try {
            Log.v(TAG, "Releasing media recorder.");
            if (mMediaRecorder[index] != null) {
                cleanupEmptyFile(index);
                mMediaRecorder[index].reset();
                mMediaRecorder[index].release();
                mMediaRecorder[index] = null;
                mCameraDevice[index].lock();
            }
            mVideoFilename[index] = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     *
     * 返回值和index一样表示 不是uvc
     * 返回值和index不一样 表示是uvccamera
     * 在camera open之后有效，表示状态不可用
     */
    @Deprecated
    public int isUVCCameraSonix(int index) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(index, cameraInfo);
        //Log.d(TAG,"camera:video" + index +" is_uvc:" + cameraInfo.is_uvc);
        if (cameraInfo.is_uvc > 0) {
            mCameraUVC = index + 1;
        } else {
            mCameraUVC = index;
        }
        //mCameraUVC = index + 1;
        return mCameraUVC;
    }

    public int startVideoRecording(int index, SurfaceTexture surfaceTexture) {
        if (!VideoStorage.storageSpaceIsAvailable(getContentResolver(), mOutFormat[index])) {
            Log.e(TAG, "Not enough storage space!!!");
            Toast.makeText(this, "Not enough storage space!!!", Toast.LENGTH_LONG).show();
            return FAIL;
        }
        if (mMediaRecorderRecording[index]) {
            Toast.makeText(this, "recording!!!", Toast.LENGTH_LONG).show();
            return FAIL;
        }
        openCamera(index);
        if (surfaceTexture != null) {
            mSurfaceTexture[index] = surfaceTexture;
        } else {
            surfaceTexture = mSurfaceTexture[index];
        }
        if (mMediaRecorderRecording[index]) {
            if (isUVCCameraSonix(index) == index) {
                try {
                    mCameraDevice[index].setPreviewTexture(surfaceTexture);
                } catch (IOException io) {
                    Log.e(TAG, "startVideoRecording setPreviewTexture set error!!");
                }
                Log.d(TAG, "startVideoRecording return");
                return BAD_VALUE;
            }
        }
        initializeRecorder(index);
        if (mMediaRecorder[index] == null) {
            Log.e(TAG, "Fail to initialize media recorder");
            return FAIL;
        }
        pauseAudioPlayback();
        mRecordingStartTime[index] = SystemClock.uptimeMillis();
        Log.d(TAG, "index=" + index);
        try {
            mMediaRecorder[index].start(); // Recording is now started
        } catch (RuntimeException e) {
            Log.e(TAG, "Could not start media recorder. ", e);
            releaseMediaRecorder(index);
            // If start fails, frameworks will not lock the camera for us.
            mCameraDevice[index].lock();
            return FAIL;
        }
        mMediaRecorderRecording[index] = true;
        Log.d(TAG, "mMediaRecorderRecording[ " + index + " ]=" + mMediaRecorderRecording[index]);
        updateRecordingTime(index);
        return OK;
    }

    public int stopVideoRecording(int index) {
        Log.d(TAG, "stopVideoRecording");
        boolean fail = false;
        if (mMediaRecorderRecording[index]) {
            try {
                mMediaRecorder[index].setOnErrorListener(null);
                mMediaRecorder[index].setOnInfoListener(null);
                mMediaRecorder[index].stop();
                Log.d(TAG, "stopVideoRecording: Setting current video filename: " + mVideoFilename[index]);
            } catch (RuntimeException e) {
                Log.e(TAG, "stop fail", e);
                if (mVideoFilename[index] != null) deleteVideoFile(mVideoFilename[index]);
                fail = true;
            }
            mMediaRecorderRecording[index] = false;
            Log.d(TAG, "stopRecording mMediaRecorderRecording=false");
            if (!fail) {
                saveVideo(index);
            }
            releaseMediaRecorder(index);
        }
        return OK;
    }

    private void onUpdateTimes(int index, String times) {
        int i = mCallbackList.beginBroadcast();
        while (i > 0) {
            i--;
            try {
                mCallbackList.getBroadcastItem(i).onUpdateTimes(index, times);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbackList.finishBroadcast();
    }

    public void registerCallback(IVideoCallback callback) {
        if (callback != null) {
            mCallbackList.register(callback);
        }
    }

    public void unregisterCallback(IVideoCallback callback) {
        Log.d(TAG, "unregisterCallback callback:" + callback);
        if (callback != null) {
            mCallbackList.unregister(callback);
        }
    }

    public boolean getPreviewState(int index) {
        return mPreviewing[index];
    }

    public boolean getRecordingState(int index) {
        Log.d(TAG, "mMediaRecorderRecording[ " + index + " ]= " + mMediaRecorderRecording[index]);
        return mMediaRecorderRecording[index];
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    void setPath() {
        //  扩展sd卡目录 /mnt/media_rw/card
        //VideoStorage.setPath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DCIM + File.separator);
        String path = VideoStorage.TFCardPath + Environment.DIRECTORY_DCIM;
        if (!new File(path).exists()) {
            new File(path).mkdir();
        }
        VideoStorage.setPath(path);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mContext = this;
        setPath();
        initVideoMemembers();
    }

    private void stopPreview() {
        stopPreview(0);
        stopPreview(1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "videService onDestroy###############");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}