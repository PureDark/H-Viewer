package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class Picture extends AbstractDataProvider.Data {
    public final static int STATUS_WAITING = 1;
    public final static int STATUS_DOWNLOADING = 2;
    public final static int STATUS_DOWNLOADED = 3;
    public int pid;
    public String thumbnail, url, pic;
    public int retries;
    public int status = STATUS_WAITING;
    public String referer;

    public Picture(int pid, String url, String thumbnail, String referer) {
        this.pid = pid;
        this.url = url;
        this.thumbnail = thumbnail;
        this.referer = referer;
    }

    @Override
    public int getId() {
        return pid;
    }

    //重写equals方法，对比thumbnail, url, pic属性，全部相同则判定为相等
    //因为pid只是在列表中的编号，每次都会变
    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof Picture)) {
            Picture item = (Picture) obj;
            boolean result = true;
            Field[] fs = Picture.class.getDeclaredFields();
            try {
                for (Field f : fs) {
                    if ("pid".equals(f.getName()) || "pic".equals(f.getName()) || "retries".equals(f.getName()))
                        continue;
                    f.setAccessible(true);
                    Object v1 = f.get(this);
                    Object v2 = f.get(item);
                    result &= equals(v1, v2);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return result;
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
