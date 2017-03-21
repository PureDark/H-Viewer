package ml.puredark.hviewer.beans;

import java.io.Serializable;

import ml.puredark.hviewer.ui.dataproviders.AbstractDataProvider;

public class Tag extends AbstractDataProvider.Data implements Serializable {
    public int tid;
    public String title = "";
    public String url;
    public boolean selected = false;

    public Tag(int tid, String title) {
        this.tid = tid;
        this.title = title;
    }

    public Tag(int tid, String title, String url) {
        this.tid = tid;
        this.title = title;
        this.url = url;
    }

    @Override
    public int getId() {
        return tid;
    }
}
