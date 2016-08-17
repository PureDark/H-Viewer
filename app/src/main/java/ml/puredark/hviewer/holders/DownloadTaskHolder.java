package ml.puredark.hviewer.holders;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.DownloadTask;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

/**
 * Created by PureDark on 2016/8/12.
 */

public class DownloadTaskHolder {
    private static List<DownloadTask> downloadTasks;
    private Context mContext;

    public DownloadTaskHolder(Context context) {
        this.mContext = context;
        if(downloadTasks==null) {
            String downloadStr = (String) SharedPreferencesUtil.getData(context, "DownloadTask", "[]");
            downloadTasks = new Gson().fromJson(downloadStr, new TypeToken<ArrayList<DownloadTask>>() {
            }.getType());
        }
    }

    public void saveDownloadTasks() {
        setAllPaused();
        SharedPreferencesUtil.saveData(mContext, "DownloadTask", new Gson().toJson(downloadTasks));
    }

    public void addDownloadTask(DownloadTask item) {
        if (item == null) return;
        downloadTasks.add(item);
        saveDownloadTasks();
    }

    public void deleteDownloadTask(DownloadTask item) {
        for (int i = 0, size = downloadTasks.size(); i < size; i++) {
            if (downloadTasks.get(i).equals(item)) {
                downloadTasks.remove(i);
                size--;
                i--;
            }
        }
        saveDownloadTasks();
    }

    public List<DownloadTask> getDownloadTasks() {
        if (downloadTasks == null)
            return new ArrayList<>();
        else
            return downloadTasks;
    }

    public boolean isInList(DownloadTask item) {
        for (int i = 0, size = downloadTasks.size(); i < size; i++) {
            if (downloadTasks.get(i).equals(item))
                return true;
        }
        return false;
    }

    private void setAllPaused(){
        for(DownloadTask task : downloadTasks){
            task.status = DownloadTask.STATUS_PAUSED;
        }
    }

}
