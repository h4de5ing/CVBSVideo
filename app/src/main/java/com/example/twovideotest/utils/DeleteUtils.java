package com.example.twovideotest.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 每次删除一半的文件
 */
public class DeleteUtils {
    public static void deleteAHalfFile(String path) {
        List<FileBean> fileList = new ArrayList<FileBean>();
        File[] files = new File(path).listFiles();
        for (File file : files) {
            fileList.add(new FileBean(file.getAbsolutePath(), file.lastModified()));
        }
        Collections.sort(fileList, new StepComparator());
        for (int i = 0; i < fileList.size() / 2; i++) {
            String name = fileList.get(i).getName();
            boolean delete = new File(name).delete();
            System.out.println("delete:" + name + " ,success: " + (delete));
        }
    }

    public static class StepComparator implements Comparator<FileBean> {

        @Override
        public int compare(FileBean o1, FileBean o2) {
            if (o1.getDate() > o2.getDate()) return 1;
            return -1;
        }
    }
}
