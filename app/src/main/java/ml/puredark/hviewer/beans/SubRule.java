package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;

/**
 * Created by PureDark on 2016/10/31.
 */

public class SubRule {

    public void replace(SubRule rule) {
        if (rule == null)
            return;
        Field[] fs = getClass().getDeclaredFields();
        try {
            for (Field f : fs) {
                f.setAccessible(true);
                if (f.getType() == Selector.class) {
                    Selector newProp = (Selector) f.get(rule);
                    f.set(this, newProp);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
