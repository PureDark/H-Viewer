package ml.puredark.hviewer.helpers;

import android.content.Context;

import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.holders.DownloadTaskHolder;

/**
 * Created by PureDark on 2016/8/15.
 */

public class DownloadManager {
    private final static String DEFAULT_PATH = "/sdcard/H-Viewer/download";
    private String downloadPath;
    private DownloadTaskHolder holder;

    public DownloadManager(Context context) {
        holder = new DownloadTaskHolder(context);
        reorganizeTasks();
    }

    public void reorganizeTasks() {
        int size = holder.getDownloadTasks().size();
        for (int i = 0; i < size; i++) {
            holder.getDownloadTasks().get(i).did = i + 1;
        }
    }

    public String getDownloadPath() {
        if (downloadPath != null)
            return downloadPath;
        else
            return DEFAULT_PATH;
    }

    public boolean createDownloadTask(Collection collection) {
        String path = getDownloadPath() + "/" + collection.title + "/";
        DownloadTask task = new DownloadTask(holder.getDownloadTasks().size() + 1, collection, path);
        if(holder.isInList(task))
            return false;
        holder.addDownloadTask(task);
        return true;
    }

}
