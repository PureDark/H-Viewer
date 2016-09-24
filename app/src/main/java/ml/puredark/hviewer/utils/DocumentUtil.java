package ml.puredark.hviewer.utils;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;
import java.io.OutputStream;

import ml.puredark.hviewer.HViewerApplication;

import static android.R.attr.data;

/**
 * Created by PureDark on 2016/9/24.
 */

public class DocumentUtil {

    public static DocumentFile createDirIfNotExist(Context context, String rootPath, String... subDirs){
        return createDirIfNotExist(context, Uri.parse(rootPath), subDirs);
    }

    public static DocumentFile createDirIfNotExist(Context context, Uri rootUri, String... subDirs){
        DocumentFile root;
        if("content".equals(rootUri.getScheme()))
            root = DocumentFile.fromTreeUri(context, rootUri);
        else
            root = DocumentFile.fromFile(new File(rootUri.getPath()));
        return createDirIfNotExist(root, subDirs);
    }

    public static DocumentFile createDirIfNotExist(DocumentFile root, String... subDirs){
        DocumentFile parent = root;
        for(int i = 0; i < subDirs.length; i++){
            String subDirName = Uri.decode(subDirs[i]);
            DocumentFile subDir = parent.findFile(subDirName);
            if(subDir == null){
                subDir = parent.createDirectory(subDirName);
            }
            parent = subDir;
        }
        return parent;
    }

    public static DocumentFile createFileIfNotExist(Context context, String fileName, String rootPath, String... subDirs){
        return createFileIfNotExist(context, "", fileName, Uri.parse(rootPath), subDirs);
    }

    public static DocumentFile createFileIfNotExist(Context context, String fileName, Uri rootUri, String... subDirs){
        return createFileIfNotExist(context, "", fileName, rootUri, subDirs);
    }

    public static DocumentFile createFileIfNotExist(Context context, String mimeType, String fileName, String rootPath, String... subDirs){
        return createFileIfNotExist(context, mimeType, fileName, Uri.parse(rootPath), subDirs);
    }

    public static DocumentFile createFileIfNotExist(Context context, String mimeType, String fileName, Uri rootUri, String... subDirs){
        DocumentFile parent = createDirIfNotExist(context, rootUri, subDirs);
        fileName = Uri.decode(fileName);
        DocumentFile file = parent.findFile(fileName);
        if(file == null){
            file = parent.createFile(mimeType, fileName);
        }
        return file;
    }

    public static boolean writeBytes(Context context, String filePath, byte[] data) {
        return writeBytes(context, Uri.parse(filePath), data);
    }

    public static boolean writeBytes(Context context, DocumentFile file, byte[] data) {
        return writeBytes(context, file.getUri(), data);
    }

    public static boolean writeBytes(Context context, Uri fileUri, byte[] data) {
        try {
            OutputStream out = context.getContentResolver().openOutputStream(fileUri);
            out.write(data);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
