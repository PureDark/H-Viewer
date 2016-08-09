package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.DataProvider;

public class Tag extends DataProvider.Data{
	public int tid;
	public String title;

	public Tag(int tid, String title){
		this.tid = tid;
		this.title = title;
	}

	@Override
	public int getId() {
		return tid;
	}
}
