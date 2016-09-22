package com.sina.util.dnscache.net.networktype;

public class Constants {

	// //网络类型////

	/**
	 * 无网络
	 */
	public final static short NETWORK_TYPE_UNCONNECTED = -1;
	/**
	 * 未知网络
	 */
	public final static short NETWORK_TYPE_UNKNOWN = 0;
	/**
	 * WIFI网络
	 */
	public final static short NETWORK_TYPE_WIFI = 1;
	/**
	 * 2G网络
	 */
	public final static short NETWORK_TYPE_2G = 2;
	/**
	 * 3G网络
	 */
	public final static short NETWORK_TYPE_3G = 3;
	/**
	 * 4G网络
	 */
	public final static short NETWORK_TYPE_4G = 4;

	// //网络运营商标识码///

	/**
	 * 未知运营商
	 */
	public final static int MOBILE_UNKNOWN = 0 ; // 未知运营商
	/**
	 * mobile-中国电信
	 */
	public final static int MOBILE_TELCOM = 3 ; // 中国电信
	/**
	 * mobile-中国联通
	 */
	public final static int MOBILE_UNICOM = 5; // 中国联通
	/**
	 * mobile-中国移动
	 */
	public final static int MOBILE_CHINAMOBILE = 4; // 中国移动


	// 有时间可以把网络类型常量定义方式改成自学习模式，就不用每次有新发现的类型在重新填写。

	public final static String NETWORK_TYPE_TO_STR(int type) {
		String str = "";
		switch (type) {
		case NETWORK_TYPE_UNCONNECTED:
			str = "无网络";
			break;
		case NETWORK_TYPE_UNKNOWN:
			str = "未知网络";
			break;
		case NETWORK_TYPE_WIFI:
			str = "WIFI网络";
			break;
		case NETWORK_TYPE_2G:
			str = "2G网络";
			break;
		case NETWORK_TYPE_3G:
			str = "3G网络";
			break;
		case NETWORK_TYPE_4G:
			str = "4G网络";
			break;
		}
		return str;
	}

	public final static String SP_TO_STR(int type) {

		String str = "";

		switch (type) {
		case MOBILE_UNKNOWN:
			str = "未知运营商";
			break;
		case MOBILE_TELCOM:
			str = "中国电信";
			break;
		case MOBILE_UNICOM:
			str = "中国联通";
			break;
		case MOBILE_CHINAMOBILE:
			str = "中国移动 ";
			break;
		}
		
		return str;

	}

}
