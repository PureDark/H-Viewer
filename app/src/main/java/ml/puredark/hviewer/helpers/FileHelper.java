package ml.puredark.hviewer.helpers;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.utils.DocumentUtil;


/**
 * Created by PureDark on 2016/9/24.
 */

public class FileHelper {

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

    public static boolean writeBytes(DocumentFile file, byte[] data) {
        if (file == null)
            return false;
        return DocumentUtil.writeBytes(HViewerApplication.mContext, file, data);
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

}
