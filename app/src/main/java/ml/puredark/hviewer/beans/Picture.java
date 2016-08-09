package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.ListDataProvider;

public class Picture extends ListDataProvider.Data{
	public int pid;
	public String url;
	
	public Picture(int pid, String url){
		this.pid = pid;
		this.url = url;
	}

	@Override
	public int getId() {
		return pid;
	}
}
