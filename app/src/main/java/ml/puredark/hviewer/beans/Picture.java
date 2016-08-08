package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class Picture extends AbstractDataProvider.Data{
	public int pid;
	public String title, url;
	
	public Picture(int pid, String title, String url){
		this.pid = pid;
		this.title = title;
		this.url = url;
	}

	@Override
	public int getId() {
		return pid;
	}
}
