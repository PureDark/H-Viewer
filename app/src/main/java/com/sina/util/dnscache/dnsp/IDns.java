package com.sina.util.dnscache.dnsp;

import com.sina.util.dnscache.model.HttpDnsPack;

import java.util.ArrayList;

public interface IDns {

    /**
     * 请求dns server，解析该domain域名
     * @param domain
     * @return
     */
    public HttpDnsPack requestDns(String domain) ;
    
    /**
     * debug调试信息
     * @return
     */
    public ArrayList<String> getDebugInfo() ;

    /**
     * 初始化debug信息
     * @return
     */
    public void initDebugInfo() ;

}