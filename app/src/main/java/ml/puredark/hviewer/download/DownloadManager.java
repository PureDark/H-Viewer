package ml.puredark.hviewer.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import java.util.List;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.dataholders.DownloadTaskHolder;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import ml.puredark.hviewer.utils.SimpleFileUtil;

import static android.content.Context.BIND_AUTO_CREATE;
import static ml.puredark.hviewer.helpers.FileHelper.createDirIfNotExist;

/**
 * Created by PureDark on 2016/8/15.
 */

public class DownloadManager {
    private final static String DEFAULT_PATH = Uri.encode("/sdcard/Pictures/H-Viewer/download");
    private DownloadTaskHolder holder;
    private DownloadService.DownloadBinder binder;

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof DownloadService.DownloadBinder)
                binder = (DownloadService.DownloadBinder) service;
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public DownloadManager(Context context) {
        holder = new DownloadTaskHolder(context);
        context.bindService(new Intent(context, DownloadService.class), conn, BIND_AUTO_CREATE);
        checkNoMediaFile();
    }

    private void checkNoMediaFile() {
        String path = Uri.decode(getDownloadPath());
        if (path.startsWith("/"))
            SimpleFileUtil.createIfNotExist(path + "/.nomedia");
        else
            FileHelper.createFileIfNotExist(".nomedia", getDownloadPath());
    }

    public static String getDownloadPath() {
        String downloadPath = (String) SharedPreferencesUtil.getData(HViewerApplication.mContext, SettingFragment.KEY_PREF_DOWNLOAD_PATH, DEFAULT_PATH);
        if (downloadPath != null)
            return downloadPath;
        else
            return DEFAULT_PATH;
    }

    public boolean isDownloading() {
        return (binder.getCurrTask() != null && binder.getCurrTask().status == DownloadTask.STATUS_DOWNLOADING);
    }

    public List<DownloadTask> getDownloadTasks() {
        return holder.getDownloadTasks();
    }

    public boolean createDownloadTask(LocalCollection collection) {
        String dirName = generateDirName(collection, 0);
        String path = getDownloadPath() + "/" + Uri.encode(dirName);
        DownloadTask task = new DownloadTask(holder.getDownloadTasks().size() + 1, collection, path);
        if (holder.isInList(task) || binder == null)
            return false;
        int did = holder.addDownloadTask(task);
        task.did = did;
        int i = 2;
        while (FileHelper.isFileExist(dirName, getDownloadPath())) {
            dirName = generateDirName(collection, i);
        }
        DocumentFile dir = FileHelper.createDirIfNotExist(getDownloadPath(), dirName);
        dirName = dir.getName();
        path = getDownloadPath() + "/" + Uri.encode(dirName);
        task.path = path;
        holder.updateDownloadTasks(task);
        // 统计添加下载次数
        MobclickAgent.onEvent(HViewerApplication.mContext, "DownloadTaskCreated");
        if (!isDownloading())
            startDownload(task);
        return true;
    }

    private String generateDirName(LocalCollection collection, int i) {
        final int limit = 255;
        String posfix = (i == 0) ? "" : "_" + i;
        String dirName = FileHelper.filenameFilter(collection.title + "_" + collection.site.title + "_" + collection.idCode + posfix);
        if (dirName.length() > limit) {
            dirName = FileHelper.filenameFilter(collection.title+posfix);
            if(dirName.length()>limit)
                dirName = dirName.substring(0,limit-3-posfix.length())+"..."+posfix;
        }
        return dirName;
    }

    public void startDownload(DownloadTask task) {
        binder.start(task);
    }

    public void pauseDownload() {
        binder.pause();
    }

    public void deleteDownloadTask(DownloadTask downloadTask) {
        holder.deleteDownloadTask(downloadTask);
        binder.stop();
    }

    public void unbindService(Context context) {
        try {
            context.unbindService(conn);
        } catch (Exception e) {
        }
    }

}
