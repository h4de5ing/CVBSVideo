package com.example.twovideotest;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by gh0st on 2018/3/23.
 */

public class FileUtils {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "BACK_IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "BACK_VID_" + timeStamp + ".mp4");
        } else {
            return mediaFile = new File(mediaStorageDir.getPath() + File.separator + "DVR_" + timeStamp + ".dvr");
        }
        return mediaFile;
    }
    public static String getNodeValue() {
        try {
            String path = "/sys/class/misc/sunxi-gps/rf-ctrl/lcd_select_state";
            return new BufferedReader(new FileReader(new File(path))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
