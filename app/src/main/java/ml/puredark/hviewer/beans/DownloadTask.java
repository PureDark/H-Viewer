package ml.puredark.hviewer.beans;

/**
 * Created by PureDark on 2016/8/15.
 */

public class DownloadTask {
    public int did;
    public Collection collection;
    public String path;

    public DownloadTask(int did, Collection collection, String path){
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
}
