package ml.puredark.hviewer.beans;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class Picture extends AbstractDataProvider.Data{
	public int pid;
	public String thumbnail, url, pic;
	
	public Picture(int pid, String url, String thumbnail){
		this.pid = pid;
        this.url = url;
		this.thumbnail = thumbnail;
	}

	@Override
	public int getId() {
		return pid;
	}
}
