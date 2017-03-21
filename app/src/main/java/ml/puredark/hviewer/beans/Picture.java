package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.ui.dataproviders.AbstractDataProvider;

import static ml.puredark.hviewer.beans.DownloadItemStatus.STATUS_WAITING;

public class Picture extends AbstractDataProvider.Data {
    public int pid;
    public String thumbnail, url, pic, highRes;
    public int retries;
    public int status = STATUS_WAITING;
    public String referer;
    public boolean loadedHighRes;

    public Picture(int pid, String url, String thumbnail, String highRes, String referer) {
        this.pid = pid;
        this.url = url;
        this.thumbnail = thumbnail;
        this.highRes = highRes;
        this.referer = referer;
    }

    public static boolean hasPicPosfix(String url) {
        return url != null && (url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".bmp") || url.endsWith(".gif") || url.endsWith(".webp"));
    }

    @Override
    public int getId() {
        return pid;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof Picture)) {
            Picture item = (Picture) obj;
            return equals(item.thumbnail, thumbnail) && equals(item.url, url);
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
}
