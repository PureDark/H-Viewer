package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.ui.dataproviders.AbstractDataProvider;

public class Category extends AbstractDataProvider.Data {
    public int cid;
    public String title, url;

    public Category(int cid, String title, String url) {
        this.cid = cid;
        this.title = title;
        this.url = url;
    }

    @Override
    public int getId() {
        return cid;
    }
}
