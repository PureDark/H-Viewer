package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class Site extends AbstractDataProvider.Data{
	public int rid;
	public String title;
	public String indexUrl;
	public Rule indexRule, galleryRule;
	public Selector picture;

	public Site(int rid, String title, String indexUrl, Rule indexRule, Rule galleryRule, Selector picture){
		this.rid = rid;
		this.title = title;
		this.indexUrl = indexUrl;
		this.indexRule = indexRule;
        this.galleryRule = galleryRule;
        this.picture = picture;
	}

	@Override
	public int getId() {
		return rid;
	}
}
