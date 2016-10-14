package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;

/**
 * Created by PureDark on 2016/10/11.
 */

public class CommentRule {
    public Selector item, avatar, author, datetime, content;

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
