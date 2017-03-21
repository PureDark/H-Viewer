package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.ui.dataproviders.AbstractDataProvider;

import static ml.puredark.hviewer.beans.DownloadItemStatus.STATUS_WAITING;

public class Video extends AbstractDataProvider.Data {
    public int vid;
    public String thumbnail, content;
    public String vlink;
    public int status = STATUS_WAITING;
    public int percent = 0;
    public int retries;

    public Video(int vid, String thumbnail, String content) {
        this.vid = vid;
        this.thumbnail = thumbnail;
        this.content = content;
    }

    @Override
    public int getId() {
        return vid;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof Video)) {
            Video item = (Video) obj;
            return equals(item.thumbnail, thumbnail) && equals(item.content, content);
        }
        return false;
    }

    public boolean equals(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }

    @Override
    public String toString() {
        return "vid=" + vid + "\n" +
                "thumbnail=" + thumbnail + "\n" +
                "content=" + content + "\n";
    }
}
