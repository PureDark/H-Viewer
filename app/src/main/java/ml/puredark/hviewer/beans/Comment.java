package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.ui.dataproviders.AbstractDataProvider;


public class Comment extends AbstractDataProvider.Data {
    public int cid;
    public String avatar, author, datetime, content;
    public String referer;

    public Comment(int cid, String avatar, String author, String datetime, String content, String referer) {
        this.cid = cid;
        this.avatar = avatar;
        this.author = author;
        this.datetime = datetime;
        this.content = content;
        this.referer = referer;
    }

    @Override
    public int getId() {
        return cid;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof Comment)) {
            Comment item = (Comment) obj;
            return equals(item.author, author) &&
                    equals(item.datetime, datetime);
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
