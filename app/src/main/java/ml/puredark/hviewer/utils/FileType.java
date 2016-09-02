package ml.puredark.hviewer.utils;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by PureDark on 2016/9/2.
 */

public class FileType {
    public final static int TYPE_UNKNOWN = 0;
    public final static int TYPE_IMAGE = 1;
    public final static int TYPE_VIDEO = 1;

    public static final HashMap<String, String> mFileTypes = new HashMap<>();

    static {
        //images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E", "png");
        mFileTypes.put("474946", "gif");
        mFileTypes.put("49492A", "tif");
        mFileTypes.put("424D38", "bmp");

        mFileTypes.put("41433130", "dwg"); //CAD
        mFileTypes.put("38425053", "psd");
        mFileTypes.put("7B5C727466", "rtf"); //日记本
        mFileTypes.put("3C3F786D6C", "xml");
        mFileTypes.put("68746D6C3E", "html");
        mFileTypes.put("44656C69766572792D646174653A", "eml"); //邮件
        mFileTypes.put("D0CF11E0", "doc");
        mFileTypes.put("5374616E64617264204A", "mdb");
        mFileTypes.put("252150532D41646F6265", "ps");
        mFileTypes.put("255044462D312E", "pdf");
        mFileTypes.put("504B0304", "zip");
        mFileTypes.put("52617221", "rar");
        mFileTypes.put("57415645", "wav");
        mFileTypes.put("41564920", "avi");
        mFileTypes.put("2E524D46", "rm");
        mFileTypes.put("000001BA", "mpg");
        mFileTypes.put("000001B3", "mpg");
        mFileTypes.put("6D6F6F76", "mov");
        mFileTypes.put("3026B2758E66CF11", "asf");
        mFileTypes.put("4D546864", "mid");
        mFileTypes.put("1F8B08", "gz");
        mFileTypes.put("", "");
    }

    public static String getFileType(byte[] src) {
        return getFileType(src, TYPE_UNKNOWN);
    }

    public static String getFileType(byte[] src, int type) {
        if (type == TYPE_IMAGE) {
            String fileType = mFileTypes.get(getFileHeader(src, 3));
            return (fileType == null) ? "" : fileType;
        } else if (type == TYPE_VIDEO) {
            String fileType = mFileTypes.get(getFileHeader(src, 4));
            return (fileType == null) ? "" : fileType;
        } else {
            String header = getFileHeader(src, 3);
            Set<String> keys = mFileTypes.keySet();
            for (String key : keys) {
                if (key.startsWith(header))
                    return mFileTypes.get(key);
            }
            return "";
        }
    }

    public static String getFileType(String filePath) {
        return getFileType(filePath, TYPE_UNKNOWN);
    }

    public static String getFileType(String filePath, int type) {
        if (type == TYPE_IMAGE) {
            String fileType = mFileTypes.get(getFileHeader(filePath, 3));
            return (fileType == null) ? "" : fileType;
        } else if (type == TYPE_VIDEO) {
            String fileType = mFileTypes.get(getFileHeader(filePath, 4));
            return (fileType == null) ? "" : fileType;
        } else {
            String header = getFileHeader(filePath, 3);
            Set<String> keys = mFileTypes.keySet();
            for (String key : keys) {
                if (key.startsWith(header))
                    return mFileTypes.get(key);
            }
            return "";
        }
    }

    //获取文件头信息
    public static String getFileHeader(byte[] src, int length) {
        String value;
        byte[] b = new byte[length];
        System.arraycopy(src, 0, b, 0, length);
        value = bytesToHexString(b);
        Log.d("FileType", value);
        return value;
    }

    //获取文件头信息
    public static String getFileHeader(String filePath, int length) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[length];
            is.read(b, 0, length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }
}
