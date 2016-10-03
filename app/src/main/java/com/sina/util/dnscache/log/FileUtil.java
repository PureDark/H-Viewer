package com.sina.util.dnscache.log;

import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作工具类
 * 
 * @author xingyu10
 *
 */
public class FileUtil {

    /**
     * 判断SD卡是否还有可用空间，小于10M为不可用
     * 
     * @return
     */
    public static boolean haveFreeSpaceInSD() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StatFs st = new StatFs(Environment.getExternalStorageDirectory().getPath());
            int blockSize = st.getBlockSize();
            long available = st.getAvailableBlocks();
            long availableSize = (blockSize * available);
            if (availableSize < 1024 * 1024 * 10) {// //sd卡空间如果小于10M，就认为sd卡空间不足
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 获取SD卡的路径
     * 
     * @return
     */
    public static String getSDPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 写文件内容，一次只写一行
     * 
     * @param file 目标文件
     * @param append 是否追加
     * @param line 一行字符串
     */
    public static void writeFileLine(File file, boolean append, String line) {
        FileWriter writer = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(file, append);
            String lineSeparator = System.getProperty("line.separator");
            writer.write(line + lineSeparator);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 调整文件行数
     * @param file 目标文件
     * @param maxSize 最大的文件大小
     * @param factor 缩容因子，每次缩容的百分比
     */
    public static void adjustFileSize(File file, int maxSize, float factor) {
        if (null == file || !file.exists()) {
            return;
        }
        if (file.length() < maxSize) {
            return;
        }
        BufferedReader reader = null;
        List<String> result = new ArrayList<String>();
        FileWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            file.delete();
            file.createNewFile();

            writer = new FileWriter(file, true);
            String lineSeparator = System.getProperty("line.separator");
            int size = result.size();
            int startPos = (int) (size * factor);
            for (int i = startPos; i < size; i++) {
                writer.write(result.get(i) + lineSeparator);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (file.exists()) {
                file.delete();
            }
        } finally {
            try {
                reader.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
