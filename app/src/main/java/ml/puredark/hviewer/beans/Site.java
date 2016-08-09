package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.ListDataProvider;

public class Site extends ListDataProvider.Data{
	public int rid;
	public String title;
	public String indexUrl;
	public Rule rule;

	public Site(int rid, String title, String indexUrl, Rule rule){
		this.rid = rid;
		this.title = title;
		this.indexUrl = indexUrl;
		this.rule = rule;
	}

	@Override
	public int getId() {
		return rid;
	}
}
