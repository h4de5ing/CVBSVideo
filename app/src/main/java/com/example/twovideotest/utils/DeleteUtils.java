package com.example.twovideotest.utils;

import android.content.ComponentName;

import java.io.File;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * 每次删除一半的文件
 */
public class DeleteUtils {
    public static void deleteFile(String path) {
        TreeMap fileList = new TreeMap<Long, String>();
        File[] files = new File(path).listFiles();
        if (files != null) {
            for (File file : files) {
                fileList.put(file.lastModified(), file.getAbsolutePath());
            }
            Iterator<Long> iterator = fileList.keySet().iterator();
            int i = 0;
            while (iterator.hasNext()) {
                i++;
                if (i <= (fileList.size() / 2)) {
                    Long key = iterator.next();
                    String name = fileList.get(key).toString();
                    boolean delete = new File(name).delete();
                    System.out.println("deleteFile:" + name + " ,success: " + (delete));
                }
            }
        }
       //ComponentName ss;
       //ss.getPackageName().contains()
    }
}
