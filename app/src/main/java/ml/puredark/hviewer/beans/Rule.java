package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;

public class Rule {
    public Selector item, idCode, title, uploader, cover, category, datetime, rating, tags, description,
            pictureUrl, pictureThumbnail, pictureHighRes,
            commentItem, commentAvatar, commentAuthor, commentDatetime, commentContent;

    public Rule() {
    }

    public boolean isEmpty() {
        boolean notEmpty = false;
        Field[] fs = Collection.class.getDeclaredFields();
        try {
            for (Field f : fs) {
                f.setAccessible(true);
                Object value = f.get(this);
                notEmpty |= (value != null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return !notEmpty;
    }

}
