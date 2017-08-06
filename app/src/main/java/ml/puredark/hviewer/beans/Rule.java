package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;

public class Rule {
    public Selector item, idCode, title, uploader, cover, category, datetime, rating, tags, description;

    @Deprecated
    public Selector pictureId, pictureUrl, pictureThumbnail, pictureHighRes;
    @Deprecated
    public Selector commentItem, commentAvatar, commentAuthor, commentDatetime, commentContent;

    public PictureRule pictureRule;
    public VideoRule videoRule;
    public TagRule tagRule;
    public CommentRule commentRule;

    public String js;

    public Rule() {
    }

    public boolean isEmpty() {
        boolean notEmpty = false;
        Field[] fs = getClass().getDeclaredFields();
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

    public void replace(Rule rule) {
        if (rule == null)
            return;
        Field[] fs = Rule.class.getDeclaredFields();
        try {
            for (Field f : fs) {
                f.setAccessible(true);
                if (f.getType() == Selector.class) {
                    Selector oldProp = (Selector) f.get(this);
                    Selector newProp = (Selector) f.get(rule);
                    if (oldProp == null)
                        oldProp = newProp;
                    else
                        oldProp.replace(newProp);
                    f.set(this, oldProp);
                } else if (f.get(rule) instanceof SubRule) {
                    SubRule oldProp = (SubRule) f.get(this);
                    SubRule newProp = (SubRule) f.get(rule);
                    if (oldProp == null)
                        oldProp = newProp;
                    else
                        oldProp.replace(newProp);
                    f.set(this, oldProp);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
