package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

/**
 * Created by PureDark on 2016/8/15.
 */

public class DownloadTask extends AbstractDataProvider.Data{
    public final static int STATUS_PAUSED = 1;
    public final static int STATUS_IN_QUEUE = 2;
    public final static int STATUS_DOWNLOADING = 3;
    public final static int STATUS_COMPLETED = 4;
    public int did;
    public LocalCollection collection;
    public String path;
    public int curPosition;
    public int status = STATUS_IN_QUEUE;

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
