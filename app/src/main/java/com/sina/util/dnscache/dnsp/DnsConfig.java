package com.sina.util.dnscache.dnsp;

import java.util.ArrayList;

/**
*
* 项目名称: DNSCache <br>
* 类名称: HttpDnsConfig <br>
* 类描述: HTTPDNS 接口配置类 <br>
* 创建人: fenglei <br>
* 创建时间: 2015-4-15 下午9:10:10 <br>
* 
* 修改人:  <br>
* 修改时间:  <br>
* 修改备注:  <br>
* 
* @version V1.0
*/
public class DnsConfig {

	/**
	 * 是否使用 自己的httpdns 服务器
	 */
	public static boolean enableSinaHttpDns = true ; 
	
	/**
	 * DNSPOD http dns 开端
	 */
	public static boolean enableDnsPod = true ; 
	
	/**
	 * DNSPOD http dns 开端
	 */
	public static boolean enableUdpDns = true ; 
	
    /**
     * httpdns 服务器地址
     */
    public static ArrayList<String> SINA_HTTPDNS_SERVER_API = new ArrayList<String>();
    
    /**
     * DNSPOD 服务器地址
     */
    public static String DNSPOD_SERVER_API = "" ;
    
    /**
     * dns 服务器地址
     */
    public static String UDPDNS_SERVER_API = "" ;
    
    
}
