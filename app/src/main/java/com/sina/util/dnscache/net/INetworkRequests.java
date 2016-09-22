/**
 * 
 */
package com.sina.util.dnscache.net;

import java.util.HashMap;

/**
 *
 * 项目名称: DNSCache
 * 类名称: INetworkRequests
 * 类描述: 由于该工程是一个辅助lib 尽量复用项目工程里在使用的网络库。
 * 创建人: fenglei
 * 创建时间: 2015-3-30 上午11:34:07
 * 
 * 修改人:
 * 修改时间: 
 * 修改备注:
 * 
 * @version V1.0
 */
public interface INetworkRequests {

	/**
	 * 请求网络接口
	 * @return 返回url数据内容
	 */
	public String requests(String url) ;

    /**
     * 请求网络接口 (带host访问)
     *
     * @return 返回url数据内容
     */
    public String requests(String url, String host) ;
    
    /**
     * 请求网络接口 (多个head访问)
     *
     * @return 返回url数据内容
     */
    public String requests(String url, HashMap<String, String> head) ;
    
    /**
     * 请求网络接口 (多个head访问)
     *
     * @return 返回url数据内容
     */
    public byte[] requestsByteArr(String url, HashMap<String, String> head) ;
	
}
