package com.sina.util.dnscache.dnsp;

import com.sina.util.dnscache.model.HttpDnsPack;

public interface IDnsProvider {

    /**
     * 请求dns server，返回指定的域名解析信息
     * @return
     */
    public HttpDnsPack requestDns(String domain) ;
    
    /**
     * 被执行的优先级
     * @return
     */
    public int getPriority();
    
    /**
     * 是否是激活状态
     * @return
     */
    public boolean isActivate();
    
    
    /**
     * 获取dns server的地址
     * @return
     */
    public String getServerApi();
}