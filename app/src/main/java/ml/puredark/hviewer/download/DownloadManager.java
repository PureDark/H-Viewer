package ml.puredark.hviewer.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.Pair;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.beans.CollectionGroup;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Video;
import ml.puredark.hviewer.configs.Names;
import ml.puredark.hviewer.dataholders.DownloadTaskHolder;
import ml.puredark.hviewer.helpers.FileHelper;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;
import ml.puredark.hviewer.utils.SimpleFileUtil;

import static android.content.Context.BIND_AUTO_CREATE;
import static ml.puredark.hviewer.beans.DownloadItemStatus.STATUS_DOWNLOADING;
import static ml.puredark.hviewer.beans.DownloadItemStatus.STATUS_WAITING;

/**
 * Created by PureDark on 2016/8/15.
 */

public class DownloadManager {
    public final static String DEFAULT_PATH = Uri.encode(getAlbumStorageDir().getAbsolutePath());
    private DownloadTaskHolder holder;
    private DownloadService.DownloadBinder binder;
    private static List<DownloadTask> downloadingTasks;

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
        new Thread(()->{
            checkDownloadedTask();
            checkNoMediaFile();
        }).start();
    }

    public static File getAlbumStorageDir() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Names.appdirname);
        return file;
    }

    public static String getDownloadPath() {
        String downloadPath = (String) SharedPreferencesUtil.getData(HViewerApplication.mContext, SettingFragment.KEY_PREF_DOWNLOAD_PATH, DEFAULT_PATH);
        if (downloadPath == null)
            return DEFAULT_PATH;
        else
            return downloadPath;
    }

    public static String generateDirName(LocalCollection collection, int i) {
        final int limit = 255;
        String posfix = (i == 0) ? "" : "_" + i;
        String dirName = FileHelper.filenameFilter(collection.title + "_" + collection.site.title + "_" + collection.idCode + posfix);
        if (dirName.length() > limit) {
            dirName = FileHelper.filenameFilter(collection.title + posfix);
            if (dirName.length() > limit)
                dirName = dirName.substring(0, limit - 3 - posfix.length()) + "..." + posfix;
        }
        return dirName;
    }

    public synchronized void checkDownloadedTask(){
        List<DownloadTask> tasks = getDownloadingTasks();
        for(int i = 0; i < tasks.size(); i++){
            DownloadTask task = tasks.get(i);
            if(task.status==DownloadTask.STATUS_COMPLETED){
                if(task.collection.gid == -1) {
                    task.collection.gid = 0;
                    holder.updateDownloadTasks(task);
                }
                tasks.remove(i--);
            }
        }
        holder.checkNoGroupDLItems();
    }

    private synchronized void checkNoMediaFile() {
        boolean nomedia = (boolean) SharedPreferencesUtil.getData(HViewerApplication.mContext, SettingFragment.KEY_PREF_DOWNLOAD_NOMEDIA, true);
        String path = Uri.decode(getDownloadPath());
        if (nomedia) {
            try {
                FileHelper.createFileIfNotExist(".nomedia", getDownloadPath());
            } catch (Exception e) {
                SimpleFileUtil.createIfNotExist(path + "/.nomedia");
            }
        } else {
            DocumentFile file = FileHelper.createDirIfNotExist(getDownloadPath());
            Logger.d("DownloadManager", "file:" + file + " file.getName:" + ((file != null) ? file.getName() : "null"));
            Logger.d("DownloadManager", "file.exists():" + file.exists());
            if (file == null || !file.exists())
                SimpleFileUtil.createDirIfNotExist(path);
        }
    }

    public boolean isDownloading() {
        return (binder.getCurrTask() != null && binder.getCurrTask().status == DownloadTask.STATUS_GETTING);
    }

    public List<DownloadTask> getDownloadingTasks() {
        if (downloadingTasks == null)
            downloadingTasks = holder.getDownloadingTasksFromDB();
        return downloadingTasks;
    }

    public List<Pair<CollectionGroup, List<DownloadTask>>> getDownloadedTasks() {
        return holder.getDownloadedTasksFromDB();
    }

    public boolean createDownloadTask(LocalCollection collection) {
        String dirName = generateDirName(collection, 0);
        String path = getDownloadPath() + "/" + Uri.encode(dirName);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        collection.datetime = dateFormat.format(calendar.getTime());
        // -1 标明为下载中item
        collection.gid = -1;
        DownloadTask task = new DownloadTask(getDownloadingTasks().size() + 1, collection, path);
        if (binder == null)
            //||holder.isInList(task))
            return false;
        int i = 2;
        while (FileHelper.isFileExist(dirName, getDownloadPath())) {
            dirName = generateDirName(collection, i++);
        }
        DocumentFile dir = FileHelper.createDirIfNotExist(getDownloadPath(), dirName);
        if (dir == null) {
            holder.deleteDownloadTask(task);
            return false;
        }
        dirName = dir.getName();
        path = getDownloadPath() + "/" + Uri.encode(dirName);
        task.path = path;
        holder.addDownloadTask(task);
        downloadingTasks.add(task);
        // 统计添加下载次数
        MobclickAgent.onEvent(HViewerApplication.mContext, "DownloadTaskCreated");
        if (!isDownloading())
            startDownload(task);
        return true;
    }

    public void saveDownloadingTasks() {
        if (downloadingTasks == null)
            return;
        for(DownloadTask item : downloadingTasks){
            holder.updateDownloadTasks(item);
        }
    }

    public void setAllPaused() {
        if (downloadingTasks == null)
            return;
        for (DownloadTask task : downloadingTasks) {
            if (task.status == DownloadTask.STATUS_GETTING) {
                task.status = DownloadTask.STATUS_PAUSED;
            }
            if (task.collection.pictures != null) {
                for (Picture picture : task.collection.pictures) {
                    if (picture.status == STATUS_DOWNLOADING) {
                        picture.status = STATUS_WAITING;
                    }
                }
            }
            if (task.collection.videos != null) {
                for (Video video : task.collection.videos) {
                    if (video.status == STATUS_DOWNLOADING) {
                        video.status = STATUS_WAITING;
                    }
                }
            }
        }
    }

    public void startDownload(DownloadTask task) {
        binder.start(task);
    }

    public void restartDownload(DownloadTask task) {
        binder.restart(task);
    }

    public void pauseDownload() {
        binder.pause();
    }

    public void deleteDownloadTask(DownloadTask downloadTask) {
        holder.deleteDownloadTask(downloadTask);
        downloadingTasks.remove(downloadTask);
        if (binder.getCurrTask() == downloadTask)
            binder.stop();
    }

    public void unbindService(Context context) {
        try {
            context.unbindService(conn);
        } catch (Exception e) {
        }
    }

}
