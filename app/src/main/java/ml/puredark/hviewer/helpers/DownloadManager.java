package ml.puredark.hviewer.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.beans.LocalCollection;
import ml.puredark.hviewer.holders.DownloadTaskHolder;
import ml.puredark.hviewer.services.DownloadService;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by PureDark on 2016/8/15.
 */

public class DownloadManager {
    private final static String DEFAULT_PATH = "/sdcard/H-Viewer/download";
    private String downloadPath;
    private DownloadTaskHolder holder;
    private DownloadService.DownloadBinder binder;

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("DownloadManager", "serviceConnected");
            if(service instanceof DownloadService.DownloadBinder)
                binder = (DownloadService.DownloadBinder) service;
            Log.d("DownloadManager", "serviceConnected binder="+binder);
        }
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public DownloadManager(Context context) {
        holder = new DownloadTaskHolder(context);
        reorganizeTasks();
        Log.d("DownloadManager", "DownloadManager created");
        context.bindService(new Intent(context, DownloadService.class), conn, BIND_AUTO_CREATE);
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

    public List<DownloadTask> getDownloadTasks(){
        return holder.getDownloadTasks();
    }

    public boolean createDownloadTask(LocalCollection collection) {
        String path = getDownloadPath() + "/" + collection.title + "/";
        DownloadTask task = new DownloadTask(holder.getDownloadTasks().size() + 1, collection, path);
        if(holder.isInList(task)||binder==null)
            return false;
        Log.d("DownloadManager", "task.collection.pictures.size():" + task.collection.pictures.size());
        holder.addDownloadTask(task);
        if(binder.getCurrTask()==null)
            startDownload(task);
        return true;
    }

    public void startDownload(DownloadTask task){
        binder.start(task);
    }

    public void pauseDownload(){
        binder.pause();
    }

    public void deleteDownloadTask(DownloadTask downloadTask){
        holder.deleteDownloadTask(downloadTask);
    }

    public void unbindService(Context context){
        context.unbindService(conn);
    }

}
