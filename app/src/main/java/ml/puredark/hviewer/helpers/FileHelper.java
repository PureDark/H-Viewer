package ml.puredark.hviewer.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.utils.DocumentUtil;

import static android.R.attr.data;
import static java.lang.System.out;


/**
 * Created by PureDark on 2016/9/24.
 */

public class FileHelper {

    public static boolean isFileExist(String fileName, String rootPath, String... subDirs){
        return DocumentUtil.isFileExist(HViewerApplication.mContext, fileName, rootPath, subDirs);
    }

    public static DocumentFile createFileIfNotExist(String fileName, String path, String... subDirs) {
        Logger.d("FileHelper", "fileName:" + fileName);
        Logger.d("FileHelper", "path:" + path);
        Logger.d("FileHelper", TextUtils.join("/", subDirs));
        if (!path.startsWith("content://") && path.startsWith("/")) {
            path = "file://" + path;
        }
        return DocumentUtil.createFileIfNotExist(HViewerApplication.mContext, fileName, path, subDirs);
    }

    public static DocumentFile createDirIfNotExist(String path, String... subDirs) {
        if (!path.startsWith("content://") && path.startsWith("/")) {
            path = "file://" + path;
        }
        return DocumentUtil.createDirIfNotExist(HViewerApplication.mContext, path, subDirs);
    }

    public static boolean deleteFile(String fileName, String rootPath, String... subDirs){
        return DocumentUtil.deleteFile(HViewerApplication.mContext, fileName, rootPath, subDirs);
    }

    public static boolean writeString(String string, String fileName, String rootPath, String... subDirs){
        return DocumentUtil.writeBytes(HViewerApplication.mContext, string.getBytes(), fileName, rootPath, subDirs);
    }

    public static boolean writeBytes(byte[] data, String fileName, String rootPath, String... subDirs){
        return DocumentUtil.writeBytes(HViewerApplication.mContext, data, fileName, rootPath, subDirs);
    }

    public static boolean writeBytes(byte[] data, DocumentFile file) {
        if (file == null)
            return false;
        return DocumentUtil.writeBytes(HViewerApplication.mContext, data, file);
    }

    public static void saveBitmapToFile(Bitmap bitmap, DocumentFile file) throws IOException {
        saveBitmapToFile(bitmap, file.getUri());
    }

    public static void saveBitmapToFile(Bitmap bitmap, Uri fileUri) throws IOException {
        OutputStream out = HViewerApplication.mContext.getContentResolver().openOutputStream(fileUri);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();
    }

    public static OutputStream getFileOutputSteam(String fileName, String rootPath, String... subDirs){
        return DocumentUtil.getFileOutputSteam(HViewerApplication.mContext, fileName, rootPath, subDirs);
    }

    public static InputStream getFileInputSteam(String fileName, String rootPath, String... subDirs){
        return DocumentUtil.getFileInputSteam(HViewerApplication.mContext, fileName, rootPath, subDirs);
    }

}
