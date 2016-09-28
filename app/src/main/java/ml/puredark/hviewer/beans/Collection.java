package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;
import java.util.List;

import ml.puredark.hviewer.ui.dataproviders.AbstractDataProvider;

public class Collection extends AbstractDataProvider.Data {
    public int cid;
    public String idCode = "";
    public String title = "", uploader = "", cover = "", category = "", datetime = "";
    public String description;
    public float rating;
    public List<Tag> tags;
    public List<Picture> pictures;
    public String referer;
    public boolean preloaded = false;

    public Collection(int cid) {
        this.cid = cid;
    }

    public Collection(int cid, String idCode, String title, String uploader, String cover, String category,
                      String datetime, String description, float rating, String referer, List<Tag> tags, List<Picture> pictures, boolean preloaded) {
        this.cid = cid;
        this.idCode = idCode;
        this.title = title;
        this.uploader = uploader;
        this.cover = cover;
        this.category = category;
        this.datetime = datetime;
        this.description = description;
        this.rating = rating;
        this.referer = referer;
        this.tags = tags;
        this.pictures = pictures;
        this.preloaded = preloaded;
    }


    @Override
    public int getId() {
        return cid;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof Collection)) {
            Collection item = (Collection) obj;
            boolean result = true;
            Field[] fs = Collection.class.getDeclaredFields();
            try {
                for (Field f : fs) {
                    if ("cid".equals(f.getName())
                            || "cover".equals(f.getName())
                            || "tags".equals(f.getName())
                            || "pictures".equals(f.getName())
                            || "referer".equals(f.getName()))
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
