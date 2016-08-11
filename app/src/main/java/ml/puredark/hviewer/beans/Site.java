package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class Site extends AbstractDataProvider.Data {
    public int rid;
    public String title;
    public String indexUrl, galleryUrl, searchUrl;
    public Rule indexRule, galleryRule;
    public Selector picUrlSelector;

    public Site(int rid, String title, String indexUrl, String galleryUrl, String searchUrl, Rule indexRule, Rule galleryRule, Selector picUrlSelector) {
        this.rid = rid;
        this.title = title;
        this.indexUrl = indexUrl;
        this.galleryUrl = galleryUrl;
        this.searchUrl = searchUrl;
        this.indexRule = indexRule;
        this.galleryRule = galleryRule;
        this.picUrlSelector = picUrlSelector;
    }

    @Override
    public int getId() {
        return rid;
    }
}
