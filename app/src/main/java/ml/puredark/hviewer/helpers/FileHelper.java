package ml.puredark.hviewer.helpers;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.utils.DocumentUtil;

import static ml.puredark.hviewer.helpers.DownloadManager.getDownloadPath;

/**
 * Created by PureDark on 2016/9/24.
 */

public class FileHelper {

    public static void createIfNotExist(){
        DocumentUtil.createFileIfNotExist(HViewerApplication.mContext, ".nomedia", getDownloadPath());
    }

}
