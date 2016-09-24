package ml.puredark.hviewer.utils;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

/**
 * Created by PureDark on 2016/9/24.
 */

public class DocumentUtil {

    public static DocumentFile createDirIfNotExist(Context context, String rootPath, String... subDirs){
        return createDirIfNotExist(context, Uri.parse(rootPath), subDirs);
    }

    public static DocumentFile createDirIfNotExist(Context context, Uri rootUri, String... subDirs){
        DocumentFile root = DocumentFile.fromTreeUri(context, rootUri);
        DocumentFile parent = root;
        for(int i = 0; i < subDirs.length; i++){
            String subDirName = subDirs[i];
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
        DocumentFile file = parent.findFile(fileName);
        if(file == null){
            file = parent.createFile(mimeType, fileName);
        }
        return file;
    }

}
