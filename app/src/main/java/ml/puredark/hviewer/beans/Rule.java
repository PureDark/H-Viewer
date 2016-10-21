package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;

public class Rule {
    public Selector item, idCode, title, uploader, cover, category, datetime, rating, tags, description;

    @Deprecated
    public Selector pictureId, pictureUrl, pictureThumbnail, pictureHighRes;
    @Deprecated
    public Selector commentItem, commentAvatar, commentAuthor, commentDatetime, commentContent;

    public PictureRule pictureRule;
    public TagRule tagRule;
    public CommentRule commentRule;

    public Rule() {
    }

    public boolean isEmpty() {
        boolean notEmpty = false;
        Field[] fs = Rule.class.getDeclaredFields();
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
