package ml.puredark.hviewer.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 文件读写工具类
 *
 * @author bear
 */
public class SimpleFileUtil {

    /**
     * 如果文件不存在，就创建文件
     *
     * @param path 文件路径
     * @return
     */
    public static String createIfNotExist(String path) {
        File file = new File(path);
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return path;
    }

    /**
     * 如果文件夹不存在，就创建文件夹
     *
     * @param path 文件路径
     * @return
     */
    public static String createDirIfNotExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            boolean success = file.mkdirs();
            Log.d("SimpleFileUtil", "success:" + success);
        }
        return path;
    }

    /**
     * 向文件中写入数据
     *
     * @param filePath 目标文件全路径
     * @param data     要写入的数据
     * @return true表示写入成功  false表示写入失败
     */
    public static boolean writeBytes(String filePath, byte[] data) {
        return writeBytes(filePath, data, false);
    }

    /**
     * 向文件中写入数据
     *
     * @param filePath 目标文件全路径
     * @param data     要写入的数据
     * @param append   是否写入文件末尾
     * @return true表示写入成功  false表示写入失败
     */
    public static boolean writeBytes(String filePath, byte[] data, boolean append) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath, append);
            fos.write(data);
            fos.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    /**
     * 从文件中读取数据
     *
     * @param file
     * @return
     */
    public static byte[] readBytes(String file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            int len = fis.available();
            byte[] buffer = new byte[len];
            fis.read(buffer);
            fis.close();
            return buffer;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;

    }

    /**
     * 向文件中写入字符串String类型的内容
     *
     * @param file    文件路径
     * @param content 文件内容
     * @param charset 写入时候所使用的字符集
     */
    public static void writeString(String file, String content, String charset) {
        try {
            byte[] data = content.getBytes(charset);
            writeBytes(file, data);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * 从文件中读取数据，返回类型是字符串String类型
     *
     * @param file    文件路径
     * @param charset 读取文件时使用的字符集，如utf-8、GBK等
     * @return
     */
    public static String readString(String file, String charset) {
        byte[] data = readBytes(file);
        String ret = null;

        try {
            ret = new String(data, charset);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return ret;
    }

    public static String getFromAssets(Context context, String fileName) throws IOException {
        InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
        BufferedReader bufReader = new BufferedReader(inputReader);
        String line = "";
        String Result = "";
        while ((line = bufReader.readLine()) != null)
            Result += "\n" + line;
        return Result;
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }

}
