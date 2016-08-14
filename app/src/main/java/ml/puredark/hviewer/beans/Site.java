package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class Site extends AbstractDataProvider.Data {
    public int sid;
    public String title = "";
    public String indexUrl = "", galleryUrl = "", searchUrl = "";
    public Rule indexRule, galleryRule;
    public Selector picUrlSelector;

    public Site(){
    }

    public Site(int sid, String title, String indexUrl, String galleryUrl, String searchUrl, Rule indexRule, Rule galleryRule, Selector picUrlSelector) {
        this.sid = sid;
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
        return sid;
    }

}
