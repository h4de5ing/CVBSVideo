package com.example.twovideotest;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by John on 2018/3/5.
 */

public class Constacts {
    public static int zhi = 1;//摄像头制式 0 NTSC  1 PAL
    public static String path = "/sys/devices/soc.0/1c33000.tvd2/tvd2_attr/tvd_system";
    public static String path2 = "/sys/devices/soc.0/1c34000.tvd3/tvd3_attr/tvd_system";

    public static void setZhi(String path, int value) {
        //String path = "/sdcard/txt.txt";
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
