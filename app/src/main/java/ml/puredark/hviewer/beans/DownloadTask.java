package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

/**
 * Created by PureDark on 2016/8/15.
 */

public class DownloadTask extends AbstractDataProvider.Data{
    public int did;
    public LocalCollection collection;
    public String path;
    public int curPosition;
    public boolean paused = true;
    public boolean isCompleted = false;

    public DownloadTask(int did, LocalCollection collection, String path){
        this.did = did;
        this.collection = collection;
        this.path = path;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof DownloadTask)) {
            DownloadTask item = (DownloadTask) obj;
            return collection.equals(item.collection);
        }
        return false;
    }

    @Override
    public int getId() {
        return did;
    }
}
