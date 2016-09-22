package com.sina.util.dnscache.score;

import java.util.ArrayList;

import com.sina.util.dnscache.model.IpModel;


public interface IPlugIn {

	/**
	 * 插件实现计算分值的方法
	 */
	public abstract void run(ArrayList<IpModel> list);
	
	abstract float getWeight();
	
	abstract boolean isActivated();
}
